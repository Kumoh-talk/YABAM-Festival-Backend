package com.pos.review.repository.dsl;

import java.util.UUID;

import org.springframework.data.domain.Slice;

import com.pos.review.entity.ReviewEntity;

public interface ReviewDslRepository {
	boolean existsByReceiptIdAndUserId(UUID receiptId, Long userId);

	Slice<ReviewEntity> findReviewsWithUser(Long storeId, Long lastReviewId, int size);
}
