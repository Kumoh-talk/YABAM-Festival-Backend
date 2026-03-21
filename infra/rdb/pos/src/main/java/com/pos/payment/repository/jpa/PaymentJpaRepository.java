package com.pos.payment.repository.jpa;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pos.payment.entity.PaymentEntity;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByReceiptId(UUID receiptId);

    Optional<PaymentEntity> findByTossPaymentKey(String tossPaymentKey);
}
