package com.application.presentation.payment.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import domain.pos.payment.entity.Payment;
import domain.pos.payment.entity.PaymentStatus;

public record PaymentResponse(
	Long paymentId,
	UUID receiptId,
	String tossPaymentKey,
	Integer amount,
	PaymentStatus status,
	String paymentMethod,
	LocalDateTime approvedAt
) {
	public static PaymentResponse from(Payment payment) {
		return new PaymentResponse(
			payment.getPaymentId(),
			payment.getReceiptId(),
			payment.getTossPaymentKey(),
			payment.getAmount(),
			payment.getStatus(),
			payment.getPaymentMethod(),
			payment.getApprovedAt()
		);
	}
}
