package com.application.presentation.receipt.dto.response;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "영수증 id 정보 응답 DTO")
public record ReceiptIdResponse(
	@Schema(description = "영수증 id", example = "1")
	UUID receiptId
) {
	public static ReceiptIdResponse from(UUID receiptId) {
		return new ReceiptIdResponse(receiptId);
	}
}
