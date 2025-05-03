package domain.pos.review.entity;

import lombok.Getter;

@Getter
public class ReviewInfo {
	private final String content;
	private final Integer rating;

	private ReviewInfo(String content, Integer rating) {
		this.content = content;
		this.rating = rating;
	}

	public static ReviewInfo of(String content, Integer rating) {
		return new ReviewInfo(content, rating);
	}
}
