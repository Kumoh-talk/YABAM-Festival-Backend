package com.application.presentation.store.dto.response;

import domain.pos.store.entity.Store;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "가게 생성 수정 응답 DTO")
@Builder
public record StoreIdResponse(
	@Schema(description = "가게 ID", example = "1")
	Long storeId
) {
	public static StoreIdResponse of(Long storeId) {
		return StoreIdResponse.builder()
			.storeId(storeId)
			.build();
	}

	public static StoreIdResponse from(Store store) {
		return StoreIdResponse.builder()
			.storeId(store.getId())
			.build();
	}
}
