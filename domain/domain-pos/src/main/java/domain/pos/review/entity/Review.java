package domain.pos.review.entity;

import domain.pos.member.entity.UserPassport;
import domain.pos.receipt.entity.Receipt;
import lombok.Getter;

@Getter
public class Review {
	private final Long reviewId;
	private final ReviewInfo reviewInfo;
	private final UserPassport userPassport;
	private final Receipt receipt;

	private Review(Long reviewId, ReviewInfo reviewInfo, UserPassport userPassport, Receipt receipt) {
		this.reviewId = reviewId;
		this.reviewInfo = reviewInfo;
		this.userPassport = userPassport;
		this.receipt = receipt;
	}

	public static Review of(Long reviewId, ReviewInfo reviewInfo, UserPassport userPassport, Receipt receipt) {
		return new Review(reviewId, reviewInfo, userPassport, receipt);
	}

	public boolean isUser(UserPassport userPassport) {
		return this.userPassport.getUserId().equals(userPassport.getUserId());
	}
}
