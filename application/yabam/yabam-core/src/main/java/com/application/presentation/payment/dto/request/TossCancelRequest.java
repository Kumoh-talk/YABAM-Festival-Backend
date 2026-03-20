package com.application.presentation.payment.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TossCancelRequest(
    @NotBlank String cancelReason
) {
}
