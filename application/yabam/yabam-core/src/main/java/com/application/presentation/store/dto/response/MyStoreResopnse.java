package com.application.presentation.store.dto.response;

import java.util.List;

import domain.pos.store.entity.Store;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(name = "MyStoreResopnse", description = "내 가게 응답")
@Builder
public record MyStoreResopnse(
	@Schema(description = "store 상세 정보")
	List<StoreInfoResponse> storeInfoResponses
) {
	public static MyStoreResopnse from(List<Store> stores) {
		return MyStoreResopnse.builder()
			.storeInfoResponses(
				stores.stream()
					.map(StoreInfoResponse::of)
					.toList())
			.build();
	}
}
