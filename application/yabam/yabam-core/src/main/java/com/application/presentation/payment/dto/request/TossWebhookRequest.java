package com.application.presentation.payment.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossWebhookRequest(
	String eventType,
	WebhookData data
) {
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record WebhookData(
		String paymentKey,
		String orderId,
		String status
	) {
	}
}
