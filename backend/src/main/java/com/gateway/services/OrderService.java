package com.gateway.services;

import com.gateway.dto.OrderCreateRequest;
import com.gateway.exception.ApiException;
import com.gateway.models.Merchant;
import com.gateway.models.Order;
import com.gateway.repositories.OrderRepository;
import com.gateway.util.IdGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Order createOrder(Merchant merchant, OrderCreateRequest request) {
        // Validate amount >= 100 paise (minimum order value)
        if (request.getAmount() == null || request.getAmount() < 100) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST_ERROR", "amount must be at least 100");
        }
        String currency = request.getCurrency() == null ? "INR" : request.getCurrency();

        String orderId = generateUniqueOrderId();
        Order order = new Order();
        order.setId(orderId);
        order.setMerchant(merchant);
        order.setAmount(request.getAmount());
        order.setCurrency(currency);
        order.setReceipt(request.getReceipt());
        order.setNotes(request.getNotes());
        order.setStatus("created");
        Instant now = Instant.now();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        return orderRepository.save(order);
    }

    // Ensures merchant can only access their own orders
    public Order getOrderForMerchant(String orderId, Merchant merchant) {
        Optional<Order> order = orderRepository.findByIdAndMerchantId(orderId, merchant.getId());
        return order.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND_ERROR", "Order not found"));
    }

    // Public endpoint - used by hosted checkout page for initial order fetch
    public Optional<Order> getOrder(String orderId) {
        return orderRepository.findById(orderId);
    }

    // Generate unique order ID with collision detection (SecureRandom ensures true uniqueness)
    private String generateUniqueOrderId() {
        String id;
        do {
            id = IdGenerator.generateOrderId();
        } while (orderRepository.existsById(id));
        return id;
    }
}
