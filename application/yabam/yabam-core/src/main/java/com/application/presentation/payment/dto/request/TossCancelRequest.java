package com.application.presentation.payment.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record TossCancelRequest(
	@NotBlank String cancelReason,
	@Min(value = 1, message = "취소 금액은 1원 이상이어야 합니다.") Integer cancelAmount
) {
}
