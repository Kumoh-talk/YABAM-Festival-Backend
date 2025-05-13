package com.auth.presentation.dto.response;

import com.vo.UserRole;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "유저 정보 응답 DTO")
public class UserInfoResponse {
	@Schema(description = "유저 Id", example = "1")
	private Long userId;
	@Schema(description = "유저 닉네임", example = "수붕이")
	private String userNickname;
	@Schema(description = "유저 역할", example = "ROLE_OWNER")
	private UserRole userRole;

	private UserInfoResponse(Long userId, String userNickname, UserRole userRole) {
		this.userId = userId;
		this.userNickname = userNickname;
		this.userRole = userRole;
	}

	public static UserInfoResponse of(
		Long userId,
		String userNickname,
		UserRole userRole) {
		return new UserInfoResponse(userId, userNickname, userRole);
	}
}
