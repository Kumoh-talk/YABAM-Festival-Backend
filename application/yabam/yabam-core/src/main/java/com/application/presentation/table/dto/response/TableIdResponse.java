package com.application.presentation.table.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "테이블 ID 응답 DTO", name = "TableIdResponse")
public record TableIdResponse(
	Long tableId
) {
	public static TableIdResponse from(Long tableId) {
		return new TableIdResponse(tableId);
	}

}
