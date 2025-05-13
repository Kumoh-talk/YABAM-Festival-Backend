package com.auth.presentation.api;

import org.springframework.http.ResponseEntity;

import com.auth.presentation.dto.response.UserInfoResponse;
import com.response.ResponseBody;
import com.vo.UserPassport;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

public interface UserApi {

	@Operation(
		summary = "유저 정보 획득 API",
		description = "액세스 토큰 대상 유저 정보를 획득합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = UserInfoResponse.class)))
	ResponseEntity<ResponseBody<UserInfoResponse>> getUserInfo(@Parameter(hidden = true) UserPassport userPassport);
}
