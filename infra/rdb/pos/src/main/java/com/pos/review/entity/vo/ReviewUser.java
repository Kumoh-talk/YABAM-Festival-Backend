package com.pos.review.entity.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewUser {
	@Column(nullable = false)
	private Long userId;

	@Column(nullable = false)
	private String userNickname;

	public ReviewUser(Long userId, String userNickname) {
		this.userId = userId;
		this.userNickname = userNickname;
	}

	public static ReviewUser of(Long userId, String userNickname) {
		return new ReviewUser(userId, userNickname);
	}
}
