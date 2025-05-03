package domain.pos.review.implement;

import org.springframework.stereotype.Component;

import domain.pos.member.entity.UserPassport;
import domain.pos.receipt.entity.ReceiptInfo;
import domain.pos.review.entity.Review;
import domain.pos.review.entity.ReviewInfo;
import domain.pos.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReviewWriter {
	private final ReviewRepository reviewRepository;

	public Review postReview(UserPassport userPassport, ReceiptInfo receiptInfo, ReviewInfo reviewInfo) {
		return reviewRepository.createReview(userPassport, receiptInfo, reviewInfo);
	}
}
