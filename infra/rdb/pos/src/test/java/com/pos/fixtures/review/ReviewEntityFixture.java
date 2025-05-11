package com.pos.fixtures.review;

import com.pos.receipt.entity.ReceiptEntity;
import com.pos.review.entity.ReviewEntity;
import com.pos.review.entity.vo.ReviewUser;
import com.pos.store.entity.StoreEntity;

public class ReviewEntityFixture {
	// 리뷰 유저 정보
	private static final Long USER_ID = 1L;
	private static final Long USER_ID_2 = 2L;

	// 리뷰 유저 닉네임
	private static final String USER_NICKNAME = "userNickname";
	private static final String USER_NICKNAME_2 = "userNickname2";

	// 리뷰 내용
	private static final String REVIEW_CONTENT = "리뷰 내용";
	private static final String REVIEW_CONTENT_2 = "리뷰 내용2";

	// 리뷰 점수
	private static final Integer REVIEW_RATTING = 30;
	private static final Integer REVIEW_RATTING_2 = 20;

	public static ReviewEntity CUSTOM_REVIEW(ReceiptEntity savedReceipt, StoreEntity savedStore) {
		return ReviewEntity.of(
			ReviewUser.of(USER_ID, USER_NICKNAME),
			REVIEW_CONTENT,
			REVIEW_RATTING,
			savedStore,
			savedReceipt
		);
	}
}
