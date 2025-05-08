package domain.pos.review.entity;

import java.time.LocalDateTime;

import com.vo.UserPassport;

import domain.pos.receipt.entity.Receipt;
import lombok.Getter;

@Getter
public class Review {
	private final Long reviewId;
	private final ReviewInfo reviewInfo;
	private final UserPassport userPassport;
	private final Receipt receipt;
	private final LocalDateTime createdAt;

	private Review(Long reviewId, ReviewInfo reviewInfo, UserPassport userPassport, Receipt receipt,
		LocalDateTime createdAt) {
		this.reviewId = reviewId;
		this.reviewInfo = reviewInfo;
		this.userPassport = userPassport;
		this.receipt = receipt;
		this.createdAt = createdAt;
	}

	public static Review of(Long reviewId, ReviewInfo reviewInfo, UserPassport userPassport, Receipt receipt,
		LocalDateTime createdAt) {
		return new Review(reviewId, reviewInfo, userPassport, receipt, createdAt);
	}

	public boolean isUser(UserPassport userPassport) {
		return this.userPassport.getUserId().equals(userPassport.getUserId());
	}
}
