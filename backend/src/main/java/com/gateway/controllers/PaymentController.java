package com.gateway.controllers;

import com.gateway.dto.ErrorResponse;
import com.gateway.dto.PaymentCreateRequest;
import com.gateway.exception.ApiException;
import com.gateway.models.Merchant;
import com.gateway.models.Order;
import com.gateway.models.Payment;
import com.gateway.services.OrderService;
import com.gateway.services.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;

    public PaymentController(PaymentService paymentService, OrderService orderService) {
        this.paymentService = paymentService;
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<?> createPayment(@Valid @RequestBody PaymentCreateRequest request, Authentication authentication) {
        Merchant merchant = getMerchant(authentication);
        Payment payment = paymentService.createPayment(merchant, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toPaymentResponse(payment));
    }

    @PostMapping("/public")
    public ResponseEntity<?> createPaymentPublic(@Valid @RequestBody PaymentCreateRequest request) {
        Order order = orderService.getOrder(request.getOrderId())
                .orElse(null);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("NOT_FOUND_ERROR", "Order not found"));
        }
        Merchant merchant = order.getMerchant();
        Payment payment = paymentService.createPayment(merchant, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toPaymentResponse(payment));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<?> getPayment(@PathVariable("paymentId") String paymentId, Authentication authentication) {
        Merchant merchant = getMerchant(authentication);
        Payment payment = paymentService.getPaymentForMerchant(paymentId, merchant);
        return ResponseEntity.ok(toPaymentResponse(payment));
    }

    @GetMapping("/{paymentId}/public")
    public ResponseEntity<?> getPaymentPublic(@PathVariable("paymentId") String paymentId) {
        var result = paymentService.getPayment(paymentId);
        if (result.isPresent()) {
            return ResponseEntity.ok(toPaymentResponse(result.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body((Object) new ErrorResponse("NOT_FOUND_ERROR", "Payment not found"));
        }
    }

    @GetMapping
    public ResponseEntity<?> listPayments(Authentication authentication) {
        Merchant merchant = getMerchant(authentication);
        java.util.List<Payment> payments = paymentService.listPaymentsForMerchant(merchant);
        java.util.List<Map<String, Object>> response = payments.stream()
                .map(this::toPaymentResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    private Merchant getMerchant(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Merchant)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_ERROR", "Invalid API credentials");
        }
        return (Merchant) authentication.getPrincipal();
    }

    private Map<String, Object> toPaymentResponse(Payment payment) {
        Map<String, Object> body = new HashMap<>();
        body.put("id", payment.getId());
        body.put("order_id", payment.getOrder().getId());
        body.put("merchant_id", payment.getMerchant().getId());
        body.put("amount", payment.getAmount());
        body.put("currency", payment.getCurrency());
        body.put("method", payment.getMethod());
        body.put("status", payment.getStatus());
        body.put("vpa", payment.getVpa());
        body.put("card_network", payment.getCardNetwork());
        body.put("card_last4", payment.getCardLast4());
        body.put("error_code", payment.getErrorCode());
        body.put("error_description", payment.getErrorDescription());
        body.put("created_at", payment.getCreatedAt());
        body.put("updated_at", payment.getUpdatedAt());
        return body;
    }
}
