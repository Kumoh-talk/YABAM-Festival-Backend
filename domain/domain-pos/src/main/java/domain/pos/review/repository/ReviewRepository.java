package domain.pos.review.repository;

import java.util.Optional;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.vo.UserPassport;

import domain.pos.receipt.entity.ReceiptInfo;
import domain.pos.review.entity.Review;
import domain.pos.review.entity.ReviewInfo;

@Repository
public interface ReviewRepository {
	Review createReview(UserPassport userPassport, ReceiptInfo receiptInfo, ReviewInfo reviewInfo);

	boolean existsReview(Long receiptId, UserPassport userPassport);

	Optional<Review> findById(Long reviewId);

	Review updateReview(Review review, ReviewInfo updateReviewInfo);

	void deleteReview(Review review);

	Slice<Review> getReviewsWithUser(Long receiptId, Long lastReviewId, int size);
}
