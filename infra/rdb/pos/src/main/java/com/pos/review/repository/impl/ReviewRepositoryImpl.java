package com.pos.review.repository.impl;

import java.util.Optional;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import domain.pos.member.entity.UserPassport;
import domain.pos.receipt.entity.ReceiptInfo;
import domain.pos.review.entity.Review;
import domain.pos.review.entity.ReviewInfo;
import domain.pos.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepository {
	@Override
	public Review createReview(UserPassport userPassport, ReceiptInfo receiptInfo, ReviewInfo reviewInfo) {
		return null;
	}

	@Override
	public boolean existsReview(Long receiptId, UserPassport userPassport) {
		return false;
	}

	@Override
	public Optional<Review> findById(Long reviewId) {
		return Optional.empty();
	}

	@Override
	public Review updateReview(Review review, ReviewInfo updateReviewInfo) {
		return null;
	}

	@Override
	public void deleteReview(Review review) {

	}

	@Override
	public Slice<Review> getReviewsWithUser(Long receiptId, Long lastReviewId, int size) {
		return null;
	}
}
