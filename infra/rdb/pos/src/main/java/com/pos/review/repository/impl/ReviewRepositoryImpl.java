package com.pos.review.repository.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.pos.review.entity.ReviewEntity;
import com.pos.review.mapper.ReviewMapper;
import com.pos.review.repository.jpa.ReviewJpaRepository;

import domain.pos.member.entity.UserPassport;
import domain.pos.receipt.entity.ReceiptInfo;
import domain.pos.review.entity.Review;
import domain.pos.review.entity.ReviewInfo;
import domain.pos.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepository {
	private final ReviewJpaRepository reviewJpaRepository;

	@Override
	public Review createReview(UserPassport userPassport, ReceiptInfo receiptInfo, ReviewInfo reviewInfo) {
		ReviewEntity reviewEntity = ReviewMapper.toReviewEntity(userPassport, receiptInfo, reviewInfo);
		ReviewEntity savedReviewEntity = reviewJpaRepository.save(reviewEntity);
		return ReviewMapper.toReview(savedReviewEntity, userPassport);
	}

	@Override
	public boolean existsReview(Long receiptId, UserPassport userPassport) {
		return reviewJpaRepository.existsByReceiptIdAndUserId(receiptId, userPassport.getUserId());
	}

	@Override
	public Optional<Review> findById(Long reviewId) {
		return reviewJpaRepository.findById(reviewId)
			.map(reviewEntity -> ReviewMapper.toReview(reviewEntity));
	}

	@Override
	@Transactional
	public Review updateReview(Review review, ReviewInfo updateReviewInfo) {
		ReviewEntity reviewEntity = reviewJpaRepository.findById(review.getReviewId())
			.orElseThrow(() -> new ServiceException(ErrorCode.REVIEW_NOT_FOUND));
		reviewEntity.changeReviewInfo(updateReviewInfo);
		return ReviewMapper.toReview(reviewEntity);
	}

	@Override
	public void deleteReview(Review review) {
		reviewJpaRepository.deleteById(review.getReviewId());
	}

	@Override
	public Slice<Review> getReviewsWithUser(Long receiptId, Long lastReviewId, int size) {
		Slice<ReviewEntity> reviewsWithUser = reviewJpaRepository.findReviewsWithUser(receiptId, lastReviewId, size);
		List<Review> reviews = reviewsWithUser.getContent().stream()
			.map(reviewEntity -> ReviewMapper.toReview(reviewEntity))
			.toList();
		return new SliceImpl<>(reviews, reviewsWithUser.getPageable(), reviewsWithUser.hasNext());
	}
}
