package com.application.presentation.review.dto.request;

import java.util.UUID;

import domain.pos.review.entity.ReviewInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;

@Schema(description = "리뷰 생성 요청 DTO", name = "ReviewCreateRequest")
@Builder
public record ReviewCreateRequest(
	@Schema(description = "영수증 ID", example = "123e4567-e89b-12d3-a456-426614174000")
	UUID receiptId,
	@Schema(description = "가게 ID", example = "1")
	Long storeId,
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
