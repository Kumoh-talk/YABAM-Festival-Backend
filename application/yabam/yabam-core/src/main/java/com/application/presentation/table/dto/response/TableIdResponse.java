package com.application.presentation.table.dto.response;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "테이블 ID 응답 DTO", name = "TableIdResponse")
public record TableIdResponse(
	@Schema(description = "테이블 ID", example = "123e4567-e89b-12d3-a456-426614174000")
	UUID tableId
) {
	public static TableIdResponse from(UUID tableId) {
		return new TableIdResponse(tableId);
	}

}
