package com.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "로그인 후 Jwt 토큰 생성 응답")
public class Token {
	@Schema(description = "액세스 토큰",
		example = "wiLCJVU0VSX0lEIjoyLCJpYXQiOjE3NDY4Nzg4NDcsImV4cCI6MTc0Njg4MDY0N30.mQq...")
	private final String accessToken;
	@Schema(description = "리프레시 토큰", example = "f2fc88dd-d1f7-4a3e-b4cc-11...")
	private final String refreshToken;

	@Builder
	public Token(String accessToken, String refreshToken) {
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
	}
}
