package com.gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ServerWebExchange;

import com.response.ResponseBody;
import com.vo.Token;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import reactor.core.publisher.Mono;

@Schema(description = "인증 관련 API")
public interface AuthApi {

	@Operation(
		summary = "로그아웃 API",
		description = "로그아웃을 합니다"
	)
	Mono<ResponseEntity<ResponseBody<Void>>> logout(ServerWebExchange exchange);

	@Operation(
		summary = "토큰 refresh API",
		description = "refresh 토큰을 통해 access 토큰을 재생성하고, refresh 토큰도 갱신합니다."
	)
	@ApiResponse(content = @Content(
		mediaType = "application/json",
		schema = @Schema(implementation = Token.class)))
	Mono<ResponseEntity<ResponseBody<Token>>> refresh(@RequestBody Mono<RefreshRequest> refreshRequest);
}
