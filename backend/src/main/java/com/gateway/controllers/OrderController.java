package com.gateway.controllers;

import com.gateway.dto.OrderCreateRequest;
import com.gateway.exception.ApiException;
import com.gateway.models.Merchant;
import com.gateway.models.Order;
import com.gateway.services.OrderService;
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
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderCreateRequest request, Authentication authentication) {
        Merchant merchant = getMerchant(authentication);
        Order order = orderService.createOrder(merchant, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toOrderResponse(order));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable("orderId") String orderId, Authentication authentication) {
        Merchant merchant = getMerchant(authentication);
        Order order = orderService.getOrderForMerchant(orderId, merchant);
        return ResponseEntity.ok(toOrderResponse(order));
    }

    @GetMapping("/{orderId}/public")
    public ResponseEntity<?> getOrderPublic(@PathVariable("orderId") String orderId) {
        var result = orderService.getOrder(orderId);
        if (result.isPresent()) {
            return ResponseEntity.ok(toPublicOrderResponse(result.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body((Object) new com.gateway.dto.ErrorResponse("NOT_FOUND_ERROR", "Order not found"));
        }
    }

    private Merchant getMerchant(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Merchant)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_ERROR", "Invalid API credentials");
        }
        return (Merchant) authentication.getPrincipal();
    }

    private Map<String, Object> toOrderResponse(Order order) {
        Map<String, Object> body = new HashMap<>();
        body.put("id", order.getId());
        body.put("merchant_id", order.getMerchant().getId());
        body.put("amount", order.getAmount());
        body.put("currency", order.getCurrency());
        body.put("receipt", order.getReceipt());
        body.put("notes", order.getNotes() == null ? new HashMap<>() : order.getNotes());
        body.put("status", order.getStatus());
        body.put("created_at", order.getCreatedAt());
        body.put("updated_at", order.getUpdatedAt());
        return body;
    }

    private Map<String, Object> toPublicOrderResponse(Order order) {
        Map<String, Object> body = new HashMap<>();
        body.put("id", order.getId());
        body.put("amount", order.getAmount());
        body.put("currency", order.getCurrency());
        body.put("status", order.getStatus());
        return body;
    }
}
