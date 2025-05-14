package com.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserPassport {
	private Long userId;
	private String userNickname;
	private UserRole userRole;

	public UserPassport(Long userId, String userNickname, UserRole userRole) {
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
