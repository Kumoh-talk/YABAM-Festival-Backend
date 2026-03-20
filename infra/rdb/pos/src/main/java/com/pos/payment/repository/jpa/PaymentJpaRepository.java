package com.pos.payment.repository.jpa;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pos.payment.entity.PaymentEntity;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {

    @Query("SELECT p FROM PaymentEntity p WHERE p.receipt.id = :receiptId")
    Optional<PaymentEntity> findByReceiptId(@Param("receiptId") UUID receiptId);

    Optional<PaymentEntity> findByTossPaymentKey(String tossPaymentKey);
}
