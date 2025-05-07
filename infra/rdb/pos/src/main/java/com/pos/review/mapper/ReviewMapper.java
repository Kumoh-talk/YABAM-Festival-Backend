package com.pos.review.mapper;

import com.pos.receipt.entity.ReceiptEntity;
import com.pos.review.entity.ReviewEntity;
import com.pos.review.entity.vo.ReviewUser;

import domain.pos.member.entity.UserPassport;
import domain.pos.receipt.entity.ReceiptInfo;
import domain.pos.review.entity.Review;
import domain.pos.review.entity.ReviewInfo;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ReviewMapper {

	public static ReviewEntity toReviewEntity(UserPassport userPassport, ReceiptInfo receiptInfo,
		ReviewInfo reviewInfo) {
		ReviewUser reviewUser = ReviewUser.of(userPassport.getUserId(), userPassport.getUserNickname());
		ReceiptEntity receiptEntity = ReceiptEntity.from(receiptInfo.getReceiptId());
		return ReviewEntity.of(
			reviewUser,
			reviewInfo.getContent(),
			reviewInfo.getRating(),
			receiptEntity
		);
	}

	public static Review toReview(ReviewEntity savedReviewEntity, UserPassport userPassport) {
		return Review.of(
			savedReviewEntity.getId(),
			ReviewInfo.of(savedReviewEntity.getContent(), savedReviewEntity.getRating()),
			userPassport,
			null,
			savedReviewEntity.getCreatedAt()
		);
	}

	public static Review toReview(ReviewEntity reviewEntity) {
		return Review.of(
			reviewEntity.getId(),
			ReviewInfo.of(reviewEntity.getContent(), reviewEntity.getRating()),
			UserPassport
				.of(reviewEntity.getReviewUser().getUserId(), reviewEntity.getReviewUser().getUserNickname(), null),
			null,
			reviewEntity.getCreatedAt()
		);
	}
}
