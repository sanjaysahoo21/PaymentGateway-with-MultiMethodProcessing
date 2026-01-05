package com.gateway.repositories;

import com.gateway.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, String> {
    Optional<Order> findByIdAndMerchantId(String id, UUID merchantId);
}
