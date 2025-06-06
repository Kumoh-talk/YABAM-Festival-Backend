package domain.pos.review.implement;

import org.springframework.stereotype.Component;

import com.vo.UserPassport;

import domain.pos.receipt.entity.ReceiptInfo;
import domain.pos.review.entity.Review;
import domain.pos.review.entity.ReviewInfo;
import domain.pos.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReviewWriter {
	private final ReviewRepository reviewRepository;

	public Review postReview(UserPassport userPassport, Long storeId, ReceiptInfo receiptInfo, ReviewInfo reviewInfo) {
		return reviewRepository.createReview(userPassport, storeId, receiptInfo, reviewInfo);
	}

	public Review updateReview(Review review, ReviewInfo updateReviewInfo) {
		return reviewRepository.updateReview(review, updateReviewInfo);
	}

	public void deleteReview(Review review) {
		reviewRepository.deleteReview(review);
	}
}
