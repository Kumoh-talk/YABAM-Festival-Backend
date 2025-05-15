package com.application.presentation.call.dto.request;

import java.util.UUID;

import domain.pos.call.entity.CallMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "CallCreateRequest", description = "전화 API 요청")
public record CallCreateRequest(
	@Schema(description = "영수증 고유 ID", example = "1")
	@NotNull
	UUID receiptId,
	@Schema(description = "가게 고유 ID", example = "1")
	@NotNull
	Long storeId,
	@Schema(description = "직원 호출 메시지", example = "물 주세요")
	String callMessage
) {
	public CallMessage getCallMessage() {
		return CallMessage.of(this.callMessage, false);
	}
}
