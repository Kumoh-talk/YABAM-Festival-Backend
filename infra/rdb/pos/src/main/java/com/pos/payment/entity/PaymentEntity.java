package com.pos.payment.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.pos.global.base.entity.BaseEntity;
import com.pos.receipt.entity.ReceiptEntity;

import domain.pos.payment.entity.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@NoArgsConstructor
@Getter
public class PaymentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id", nullable = false, unique = true)
    private ReceiptEntity receipt;

    @Column(name = "receipt_id", insertable = false, updatable = false)
    private UUID receiptId;

    @Column(name = "toss_payment_key", nullable = false, unique = true)
    private String tossPaymentKey;

    @Column(name = "toss_order_id", nullable = false)
    private String tossOrderId;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Builder
    private PaymentEntity(ReceiptEntity receipt, String tossPaymentKey, String tossOrderId,
        Integer amount, PaymentStatus status, String paymentMethod, LocalDateTime approvedAt) {
        this.receipt = receipt;
        this.tossPaymentKey = tossPaymentKey;
        this.tossOrderId = tossOrderId;
        this.amount = amount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.approvedAt = approvedAt;
    }

    public void updateStatus(PaymentStatus status) {
        this.status = status;
    }
}
