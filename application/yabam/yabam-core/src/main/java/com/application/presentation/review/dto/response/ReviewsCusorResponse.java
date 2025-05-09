package com.application.presentation.review.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import domain.pos.review.entity.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "리뷰 커서 응답 DTO", name = "ReviewsCusorResponse")
@Builder
public record ReviewsCusorResponse(
	@Schema(description = "리뷰 데이터 리스트", example = "리뷰 데이터 리스트")
	List<ReviewInfoDto> reviewsContents,
	@Schema(description = "데이터 수", example = "10")
	Integer totalCount,
	@Schema(description = "다음 데이터 존재 여부", example = "true")
	Boolean hasNextPage,
	@Schema(description = "마지막 reviewId", example = "1")
	Long lastReviewId
) {
	@Schema(description = "리뷰 상세 내용 DTO", name = "ReviewInfoDto")
	@Builder
	public record ReviewInfoDto(
		@Schema(description = "리뷰 ID", example = "1")
		Long reviewId,
		@Schema(description = "리뷰 내용", example = "리뷰 내용")
		String content,
		@Schema(description = "리뷰 평점 50점 만점", example = "50")
		Integer rating,
		@Schema(description = "리뷰 작성자 ID", example = "1")
		Long userId,
		@Schema(description = "리뷰 작성자 닉네임", example = "강민기")
		String userNickname,
		@Schema(description = "리뷰 작성 시간", example = "2023-10-01T12:00:00Z")
		String createdAt
	) {
		public static ReviewInfoDto from(Review review) {
			return ReviewInfoDto.builder()
				.reviewId(review.getReviewId())
				.content(review.getReviewInfo().getContent())
				.rating(review.getReviewInfo().getRating())
				.userId(review.getUserPassport().getUserId())
				.userNickname(review.getUserPassport().getUserNickname())
				.createdAt(review.getCreatedAt().toString())
				.build();
		}
	}

	public static ReviewsCusorResponse from(Slice<Review> reviews) {
		List<ReviewInfoDto> reviewInfoDtos = reviews.getContent().stream()
			.map(ReviewInfoDto::from)
			.toList();
		int size = reviews.getSize();
		boolean isNext = reviews.hasNext();
		Long lastReviewId = reviews.getContent().isEmpty() ? null : reviews.getContent().get(size - 1).getReviewId();
		return ReviewsCusorResponse.builder()
			.reviewsContents(reviewInfoDtos)
			.totalCount(size)
			.hasNextPage(isNext)
			.lastReviewId(lastReviewId)
			.build();
	}
}
