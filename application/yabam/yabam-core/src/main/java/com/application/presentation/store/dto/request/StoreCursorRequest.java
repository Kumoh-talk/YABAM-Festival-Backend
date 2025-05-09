package com.application.presentation.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import lombok.Builder;

@Schema(name = "StoreCursorRequest", description = "가게 커서 요청")
@Builder
public record StoreCursorRequest(
	@Schema(description = "마지막 리뷰 수 만약 첫 조회면 null", example = "131")
	@Nullable
	Long lastReviewCount,
	@Schema(description = "마지막 가게 ID 만약 첫 조회면 null", example = "1")
	@Nullable
	Long lastStoreId,
	@Schema(description = "가게 개수", example = "10")
	Integer size
) {
}
