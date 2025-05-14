package com.auth.presentation.api;

import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.response.ResponseBody;
import com.vo.Token;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface FakeAuthApi {
	@Operation(
		summary = "가짜 유저 로그인 API",
		description = "가상의 유저를 생성합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = Token.class)))
	ResponseEntity<ResponseBody<Void>> fakeUserLogin(HttpServletResponse response) throws JsonProcessingException;

	@Operation(
		summary = "가짜 점주 로그인 API",
		description = "가상의 점주를 생성합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = Token.class)))
	ResponseEntity<ResponseBody<Void>> fakeOwnerLogin(HttpServletResponse response) throws JsonProcessingException;
}
