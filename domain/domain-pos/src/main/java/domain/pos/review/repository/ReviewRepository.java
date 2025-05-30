package domain.pos.review.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;

import com.vo.UserPassport;

import domain.pos.receipt.entity.ReceiptInfo;
import domain.pos.review.entity.Review;
import domain.pos.review.entity.ReviewInfo;

@Repository
public interface ReviewRepository {
	Review createReview(UserPassport userPassport, Long storeId, ReceiptInfo receiptInfo, ReviewInfo reviewInfo);

	boolean existsReview(UUID receiptId, UserPassport userPassport);

	Optional<Review> findById(Long reviewId);

	Review updateReview(Review review, ReviewInfo updateReviewInfo);

	void deleteReview(Review review);

	Slice<Review> getReviewsWithUser(Long storeId, Long lastReviewId, int size);
}
