package com.gateway.repositories;

import com.gateway.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Order entity.
 * Provides database access and query methods for order retrieval.
 * Supports both public queries (by order ID) and merchant-scoped queries (with merchant verification).
 */
public interface OrderRepository extends JpaRepository<Order, String> {
    /**
     * Find an order by its ID and verify it belongs to the specified merchant.
     * Used to enforce merchant ownership and prevent cross-merchant access.
     * @param id the order ID (format: order_XXXXXXXXXXXXXXXX)
     * @param merchantId the merchant ID that must own the order
     * @return Optional containing the order if found and belongs to the merchant, empty otherwise
     */
    Optional<Order> findByIdAndMerchantId(String id, UUID merchantId);
}
