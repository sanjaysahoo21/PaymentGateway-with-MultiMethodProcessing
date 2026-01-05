package com.gateway.repositories;

import com.gateway.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for Payment entity.
 * Provides database access and query methods for payment retrieval.
 * Supports both order-scoped queries (all payments for an order) and merchant-scoped queries (all payments by a merchant).
 */
public interface PaymentRepository extends JpaRepository<Payment, String> {
    /**
     * Find all payments for a specific order.
     * @param orderId the order ID (format: order_XXXXXXXXXXXXXXXX)
     * @return list of all payments associated with the order
     */
    List<Payment> findByOrderId(String orderId);

    /**
     * Find all payments created by a specific merchant.
     * Used for the merchant dashboard transaction listing.
     * @param merchantId the merchant's unique identifier
     * @return list of all payments created by this merchant
     */
    List<Payment> findByMerchantId(UUID merchantId);
}
