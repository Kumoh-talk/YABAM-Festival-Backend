package domain.pos.review.service;

import org.springframework.stereotype.Service;

import com.exception.ErrorCode;
import com.exception.ServiceException;

import domain.pos.member.entity.UserPassport;
import domain.pos.receipt.entity.ReceiptInfo;
import domain.pos.receipt.implement.ReceiptReader;
import domain.pos.review.entity.Review;
import domain.pos.review.entity.ReviewInfo;
import domain.pos.review.implement.ReviewReader;
import domain.pos.review.implement.ReviewWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {
	private final ReceiptReader receiptReader;
	private final ReviewWriter reviewWriter;
	private final ReviewReader reviewReader;

	public Review postReview(final UserPassport userPassport, final Long receiptId, final ReviewInfo reviewInfo) {
		ReceiptInfo receiptInfo = receiptReader.getReceiptInfo(receiptId)
			.orElseThrow(() -> new ServiceException(ErrorCode.RECEIPT_NOT_FOUND));
		if (!receiptInfo.isAdjustment()) {
			throw new ServiceException(ErrorCode.REVIEW_NOT_ADJUSTMENT);
		}
		if (reviewReader.isExitsReview(receiptId, userPassport)) {
			throw new ServiceException(ErrorCode.REVIEW_ALREADY_EXITS);
		}
		return reviewWriter.postReview(userPassport, receiptInfo, reviewInfo);
	}

	public Review updateReview(final UserPassport userPassport, final Long reviewId,
		final ReviewInfo updateReviewInfo) {
		Review review = reviewReader.getReview(reviewId)
			.orElseThrow(() -> new ServiceException(ErrorCode.REVIEW_NOT_FOUND));
		if (!review.isUser(userPassport)) {
			throw new ServiceException(ErrorCode.REVIEW_NOT_USER);
		}
		return reviewWriter.updateReview(review, updateReviewInfo);
	}

	public void deleteReview(final UserPassport userPassport, final Long reviewId) {
		Review review = reviewReader.getReview(reviewId)
			.orElseThrow(() -> new ServiceException(ErrorCode.REVIEW_NOT_FOUND));
		if (!review.isUser(userPassport)) {
			throw new ServiceException(ErrorCode.REVIEW_NOT_USER);
		}
		reviewWriter.deleteReview(review);
	}

}
