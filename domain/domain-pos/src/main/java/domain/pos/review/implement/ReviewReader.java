package domain.pos.review.implement;

import org.springframework.stereotype.Component;

import domain.pos.member.entity.UserPassport;
import domain.pos.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReviewReader {
	private final ReviewRepository reviewRepository;

	public boolean isExitsReview(Long receiptId, UserPassport userPassport) {
		return reviewRepository.existsReview(receiptId, userPassport);
	}
}
