package com.auth.presentation.dto.request;

import com.auth.domain.vo.OidcProvider;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "OIDC 기반 로그인/회원가입 요청 DTO")
public record OidcLoginRequest(
	@Schema(description = "Oidc 대상 플랫폼", example = "KAKAO")
	@NotNull(message = "OIDC 플랫폼은 필수입니다.")
	OidcProvider provider,
	@Schema(description = "Oidc ID 토큰", example = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEyMzQ1Njc4OTAifQ...")
	@NotBlank(message = "ID 토큰은 필수입니다.")
	String idToken,

	@Schema(description = "Oidc 클라이언트 ID", example = "1234567890")
	@NotBlank(message = "클라이언트 ID는 필수입니다.")
	String oauthId,

	@Schema(description = "Oidc Nonce", example = "abc123")
	@NotBlank(message = "Nonce는 필수입니다.")
	String nonce
) {

}
