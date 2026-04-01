package com.pos.review.mapper;

import com.pos.receipt.entity.ReceiptEntity;
import com.pos.review.entity.ReviewEntity;
import com.pos.review.entity.vo.ReviewUser;
import com.pos.store.entity.StoreEntity;
import com.vo.UserPassport;

import domain.pos.receipt.entity.ReceiptInfo;
import domain.pos.review.entity.Review;
import domain.pos.review.entity.ReviewInfo;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewMapper {

	public static ReviewEntity toReviewEntity(UserPassport userPassport, Long storeId, ReceiptInfo receiptInfo,
		ReviewInfo reviewInfo) {
		return ReviewEntity.of(
			ReviewUser.of(userPassport.getUserId(), userPassport.getUserNickname()),
			reviewInfo.getContent(),
			reviewInfo.getRating(),
			StoreEntity.from(storeId),
			ReceiptEntity.from(receiptInfo.getReceiptId())
		);
	}

	public static Review toReview(ReviewEntity savedReviewEntity, UserPassport userPassport) {
		return Review.of(
			savedReviewEntity.getId(),
			ReviewInfo.of(savedReviewEntity.getContent(), savedReviewEntity.getRating()),
			userPassport,
			null,
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
			null,
			reviewEntity.getCreatedAt()
		);
	}
}
