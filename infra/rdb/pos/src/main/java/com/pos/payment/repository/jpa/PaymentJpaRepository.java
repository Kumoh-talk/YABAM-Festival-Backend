package com.pos.payment.repository.jpa;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pos.payment.entity.PaymentEntity;

import domain.pos.payment.entity.PaymentStatus;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByReceiptId(UUID receiptId);

    Optional<PaymentEntity> findByTossPaymentKey(String tossPaymentKey);

    @Query("SELECT p FROM PaymentEntity p WHERE p.receipt.sale.id = :saleId ORDER BY p.id DESC")
    List<PaymentEntity> findBySaleId(@Param("saleId") Long saleId);

    List<PaymentEntity> findByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime threshold);
}
