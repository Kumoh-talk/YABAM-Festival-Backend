package com.gateway.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "토큰 리프레시 요청 DTO")
public record RefreshRequest(

	@Schema(description = "액세스 토큰 값",
		example = "wiLCJVU0VSX0lEIjoyLCJpYXQiOjE3NDY4Nzg4NDcsImV4cCI6MTc0Njg4MDY0N30.mQq...")
	@NotBlank(message = "액세스 토큰 값은 필수입니다.")
	String accessToken,

	@Schema(description = "리프레시 토큰 값",
		example = "f2fc88dd-d1f7-4a3e-b4cc-11...")
	@NotBlank(message = "리프레시 토큰 값은 필수입니다.")
	String refreshToken
) {
}
