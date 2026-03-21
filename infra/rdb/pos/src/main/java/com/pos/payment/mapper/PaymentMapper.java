package com.pos.payment.mapper;

import com.pos.payment.entity.PaymentEntity;
import com.pos.receipt.entity.ReceiptEntity;

import domain.pos.payment.entity.Payment;

public class PaymentMapper {

    private PaymentMapper() {
    }

    public static Payment toDomain(PaymentEntity entity) {
        return Payment.builder()
            .paymentId(entity.getId())
            .receiptId(entity.getReceiptId())
            .tossPaymentKey(entity.getTossPaymentKey())
            .tossOrderId(entity.getTossOrderId())
            .amount(entity.getAmount())
            .status(entity.getStatus())
            .paymentMethod(entity.getPaymentMethod())
            .approvedAt(entity.getApprovedAt())
            .build();
    }

    public static PaymentEntity toEntity(Payment domain) {
        return PaymentEntity.builder()
            .receipt(ReceiptEntity.from(domain.getReceiptId()))
            .tossPaymentKey(domain.getTossPaymentKey())
            .tossOrderId(domain.getTossOrderId())
            .amount(domain.getAmount())
            .status(domain.getStatus())
            .paymentMethod(domain.getPaymentMethod())
            .approvedAt(domain.getApprovedAt())
            .build();
    }
}
