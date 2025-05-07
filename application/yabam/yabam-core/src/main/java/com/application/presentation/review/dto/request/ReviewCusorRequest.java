package com.application.presentation.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "리뷰 커서 요청 DTO", name = "ReviewCusorRequest")
public record ReviewCusorRequest(
	@Schema(description = "영수증 ID", example = "1")
	Long receiptId,
	@Schema(description = "마지막 리뷰 ID", example = "1")
	Long lastReviewId,
	@Schema(description = "리뷰 개수", example = "10")
	Integer size
) {
}
