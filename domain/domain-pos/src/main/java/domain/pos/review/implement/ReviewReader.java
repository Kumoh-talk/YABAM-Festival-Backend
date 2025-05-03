package domain.pos.review.implement;

import java.util.Optional;

import org.springframework.stereotype.Component;

import domain.pos.member.entity.UserPassport;
import domain.pos.review.entity.Review;
import domain.pos.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReviewReader {
	private final ReviewRepository reviewRepository;

	public boolean isExitsReview(Long receiptId, UserPassport userPassport) {
		return reviewRepository.existsReview(receiptId, userPassport);
	}

	public Optional<Review> getReview(Long reviewId) {
		return reviewRepository.findById(reviewId);
	}
}
