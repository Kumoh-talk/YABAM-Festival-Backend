package com.application.presentation.store.dto.response;

import java.util.List;

import domain.pos.store.entity.Store;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(name = "MyStoreResponse", description = "내 가게 응답")
@Builder
public record MyStoreResponse(
	@Schema(description = "store 상세 정보")
	List<StoreInfoResponse> storeInfoResponses
) {
	public static MyStoreResponse from(List<Store> stores) {
		return MyStoreResponse.builder()
			.storeInfoResponses(
				stores.stream()
					.map(StoreInfoResponse::of)
					.toList())
			.build();
	}
}
