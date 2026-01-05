package com.gateway.repositories;

import com.gateway.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    List<Payment> findByOrderId(String orderId);
    List<Payment> findByMerchantId(UUID merchantId);
}
