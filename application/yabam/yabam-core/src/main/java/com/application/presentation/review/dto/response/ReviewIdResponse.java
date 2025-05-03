package com.application.presentation.review.dto.response;

import domain.pos.review.entity.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "리뷰 id 응답 DTO")
@Builder
public record ReviewIdResponse(
	@Schema(description = "리뷰 ID", example = "1")
	Long reviewId
) {
	public static ReviewIdResponse from(Review review) {
		return ReviewIdResponse.builder()
			.reviewId(review.getReviewId())
			.build();
	}
}
