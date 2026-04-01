package com.application.presentation.payment.dto.response;

import java.time.LocalDateTime;

import domain.pos.payment.entity.TossConfirmResult;

public record TossPaymentQueryResponse(
	String tossPaymentKey,
	String tossOrderId,
	Integer amount,
	String status,
	String paymentMethod,
	LocalDateTime approvedAt
) {
	public static TossPaymentQueryResponse from(TossConfirmResult result) {
		return new TossPaymentQueryResponse(
			result.getTossPaymentKey(),
			result.getTossOrderId(),
			result.getAmount(),
			result.getStatus() != null ? result.getStatus().name() : null,
			result.getPaymentMethod(),
			result.getApprovedAt()
		);
	}
}
