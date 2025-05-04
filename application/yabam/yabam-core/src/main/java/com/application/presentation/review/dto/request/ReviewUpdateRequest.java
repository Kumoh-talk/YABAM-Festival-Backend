package com.application.presentation.review.dto.request;

import domain.pos.review.entity.ReviewInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;

@Schema(description = "리뷰 수정 요청 DTO", name = "ReviewUpdateRequest")
@Builder
public record ReviewUpdateRequest(
	@Schema(description = "리뷰 ID", example = "1")
	Long reviewId,
	@Schema(description = "리뷰 내용", example = "리뷰 내용")
	String content,
	@Schema(description = "리뷰 평점 50점 만점", example = "50")
	@Max(50) @Min(0)
	Integer rating
) {
	public ReviewInfo getReviewInfo() {
		return ReviewInfo.of(
			content,
			rating
		);
	}
}
