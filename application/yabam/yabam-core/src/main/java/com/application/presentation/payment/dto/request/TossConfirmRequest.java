package com.application.presentation.payment.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TossConfirmRequest(
    @NotBlank String paymentKey,
    @NotBlank String orderId,
    @NotNull @Min(value = 1, message = "결제 금액은 1원 이상이어야 합니다.") Integer amount
) {
}
