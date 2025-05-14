package domain.pos.review.implement;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

import com.vo.UserPassport;

import domain.pos.review.entity.Review;
import domain.pos.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReviewReader {
	private final ReviewRepository reviewRepository;

	public boolean isExistsReview(UUID receiptId, UserPassport userPassport) {
		return reviewRepository.existsReview(receiptId, userPassport);
	}

	public Optional<Review> getReview(Long reviewId) {
		return reviewRepository.findById(reviewId);
	}

	public Slice<Review> getReviews(Long storeId, Long lastReviewId, int size) {
		return reviewRepository.getReviewsWithUser(storeId, lastReviewId, size);
	}
}
