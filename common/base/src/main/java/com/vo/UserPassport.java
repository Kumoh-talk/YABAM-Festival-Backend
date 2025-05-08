package com.vo;

import lombok.Getter;

@Getter
public class UserPassport {
	private final Long userId;
	private final String userNickname;
	private final UserRole userRole;

	private UserPassport(Long userId, String userNickname, UserRole userRole) {
		this.userId = userId;
		this.userNickname = userNickname;
		this.userRole = userRole;
	}

	public static UserPassport anonymous() {
		return new UserPassport(null, "Anonymous", UserRole.ROLE_ANONYMOUS);
	}

	public static UserPassport of(
		Long userId,
		String userNickname,
		UserRole userRole) {
		return new UserPassport(userId, userNickname, userRole);
	}
}
