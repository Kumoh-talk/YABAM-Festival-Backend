package com.application.presentation.payment.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TossWebhookRequest(
    @NotBlank String eventType,
    @NotNull WebhookData data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WebhookData(
        @NotBlank String paymentKey,
        String orderId,
        @NotBlank String status
    ) {
    }
}
