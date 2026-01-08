package com.gateway.services;

import com.gateway.dto.PaymentCreateRequest;
import com.gateway.exception.ApiException;
import com.gateway.models.Merchant;
import com.gateway.models.Order;
import com.gateway.models.Payment;
import com.gateway.repositories.OrderRepository;
import com.gateway.repositories.PaymentRepository;
import com.gateway.util.IdGenerator;
import com.gateway.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.Random;

@Service
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final Random random = new Random();

    @Value("${app.payment.upi-success-rate:0.90}")
    private double upiSuccessRate;

    @Value("${app.payment.card-success-rate:0.95}")
    private double cardSuccessRate;

    @Value("${app.payment.processing-delay-min:5000}")
    private long processingDelayMin;

    @Value("${app.payment.processing-delay-max:10000}")
    private long processingDelayMax;

    @Value("${app.payment.test-mode:false}")
    private boolean testMode;

    @Value("${app.payment.test-success:true}")
    private boolean testPaymentSuccess;

    @Value("${app.payment.test-processing-delay:1000}")
    private long testProcessingDelay;

    public PaymentService(OrderRepository orderRepository, PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Payment createPayment(Merchant merchant, PaymentCreateRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND_ERROR", "Order not found"));
        
        if (!order.getMerchant().getId().equals(merchant.getId())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND_ERROR", "Order not found");
        }

        String method = request.getMethod().toLowerCase();
        Payment payment = new Payment();
        payment.setId(generateUniquePaymentId());
        payment.setOrder(order);
        payment.setMerchant(merchant);
        payment.setAmount(order.getAmount());
        payment.setCurrency(order.getCurrency());
        payment.setMethod(method);
        payment.setStatus("processing");
        Instant now = Instant.now();
        payment.setCreatedAt(now);
        payment.setUpdatedAt(now);

        if ("upi".equals(method)) {
            handleUpi(request, payment);
        } else if ("card".equals(method)) {
            handleCard(request, payment);
        } else {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST_ERROR", "Unsupported payment method");
        }

        paymentRepository.save(payment);

        simulateProcessing(payment);
        paymentRepository.save(payment);
        return payment;
    }

    public Payment getPaymentForMerchant(String paymentId, Merchant merchant) {
        Optional<Payment> payment = paymentRepository.findById(paymentId);
        if (payment.isEmpty() || !payment.get().getMerchant().getId().equals(merchant.getId())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND_ERROR", "Payment not found");
        }
        return payment.get();
    }

    public Optional<Payment> getPayment(String paymentId) {
        return paymentRepository.findById(paymentId);
    }

    public java.util.List<Payment> listPaymentsForMerchant(Merchant merchant) {
        return paymentRepository.findByMerchantId(merchant.getId());
    }

    private void handleUpi(PaymentCreateRequest request, Payment payment) {
        // Validate VPA format before storing (prevents invalid data in database)
        if (!ValidationUtil.isValidVpa(request.getVpa())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_VPA", "VPA format invalid");
        }
        // Store VPA for future reference (used in receipts/dashboards)
        payment.setVpa(request.getVpa());
    }

    private void handleCard(PaymentCreateRequest request, Payment payment) {
        PaymentCreateRequest.CardDetails card = request.getCard();
        // Null check: ensure card object is present for card payments
        if (card == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST_ERROR", "card details required");
        }
        // Validate card number using Luhn algorithm (catches typos and invalid card numbers)
        if (!ValidationUtil.isValidCardNumber(card.getNumber())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_CARD", "Card validation failed");
        }
        // Validate expiry date is in the future (prevents expired card usage)
        if (!ValidationUtil.isValidExpiry(card.getExpiryMonth(), card.getExpiryYear())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "EXPIRED_CARD", "Card expiry date invalid");
        }
        // Detect card network (visa/mastercard/amex/rupay) for categorization
        String network = ValidationUtil.detectCardNetwork(card.getNumber());
        payment.setCardNetwork(network);
        // Extract and store last 4 digits only (security best practice: never store full card number)
        String digits = card.getNumber().replaceAll("[\\s-]", "");
        payment.setCardLast4(digits.substring(digits.length() - 4));
    }

    private void simulateProcessing(Payment payment) {
        try {
            // Use test mode delay (1s) for rapid development/testing, or realistic delay for demo
            long delay = testMode ? testProcessingDelay : randomDelay();
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            // Restore interrupt flag if thread is interrupted during sleep
            Thread.currentThread().interrupt();
        }
        // Randomly decide success/failure based on configured success rates
        boolean success = decideSuccess(payment.getMethod());
        payment.setUpdatedAt(Instant.now());
        if (success) {
            // Mark payment as successful
            payment.setStatus("success");
        } else {
            // Mark payment as failed with generic error (real gateways would provide specific reasons)
            payment.setStatus("failed");
            payment.setErrorCode("PAYMENT_FAILED");
            payment.setErrorDescription("Payment could not be processed");
        }
    }

    /**
     * Calculate random delay between min/max bounds for payment processing simulation.
     * Simulates real payment gateway authorization times (5-10 seconds typical).
     * Returns min delay if max <= min (edge case protection).
     */
    private long randomDelay() {
        // Edge case: if max <= min, return minimum delay (prevents infinite loop)
        if (processingDelayMax <= processingDelayMin) {
            return processingDelayMin;
        }
        // Generate random delay: min + random(0 to range)
        return processingDelayMin + (long) (random.nextDouble() * (processingDelayMax - processingDelayMin));
    }

    /**
     * Determine payment success based on configured success rates and test mode.
     * 
     * Behavior:
     * - Test mode: Return configured test outcome (true/false)
     * - Production: Roll random number (0-1), compare against method-specific success rate
     *   - UPI: 90% success (0.90)
     *   - Card: 95% success (0.95)
     * 
     * @param method Payment method (upi or card)
     * @return true if payment succeeds, false if it fails
     */
    private boolean decideSuccess(String method) {
        // In test mode, honor configured test outcome (allows deterministic testing)
        if (testMode) {
            return testPaymentSuccess;
        }
        // Generate random decision in real mode
        double roll = random.nextDouble();
        if ("upi".equals(method)) {
            // UPI success rate configured separately (typically 90%)
            return roll < upiSuccessRate;
        }
        // Card success rate (typically 95%)
        return roll < cardSuccessRate;
    }

    private String generateUniquePaymentId() {
        String id;
        do {
            id = IdGenerator.generatePaymentId();
        } while (paymentRepository.existsById(id));
        return id;
    }
}
