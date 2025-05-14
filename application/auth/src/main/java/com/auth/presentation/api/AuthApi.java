package com.auth.presentation.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import com.auth.presentation.dto.request.OidcLoginRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.response.ResponseBody;
import com.vo.Token;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

public interface AuthApi {

	@Operation(
		summary = "OIDC id 토큰 기반 유저 로그인/회원가입 API",
		description = "유저 로그인 및 회원가입을 합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = Token.class)))
	ResponseEntity<ResponseBody<Void>> login(
		@Parameter(hidden = true) HttpServletResponse response,
		@RequestBody @Valid OidcLoginRequest request) throws JsonProcessingException;
}
