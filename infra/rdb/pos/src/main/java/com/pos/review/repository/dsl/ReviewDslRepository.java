package com.pos.review.repository.dsl;

import org.springframework.data.domain.Slice;

import com.pos.review.entity.ReviewEntity;

public interface ReviewDslRepository {
	boolean existsByReceiptIdAndUserId(Long receiptId, Long userId);

	Slice<ReviewEntity> findReviewsWithUser(Long receiptId, Long lastReviewId, int size);
}
