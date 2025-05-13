package com.gateway.controller;

import static com.response.ResponseUtil.*;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.gateway.component.JwtServerAuthenticationConverter;
import com.gateway.service.AuthService;
import com.response.ResponseBody;
import com.vo.Token;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/gateway/api/v1")
@RequiredArgsConstructor
public class AuthController implements AuthApi {
	private final JwtServerAuthenticationConverter jwtServerAuthenticationConverter;
	private final AuthService authService;

	@PostMapping("/logout")
	public Mono<ResponseEntity<ResponseBody<Void>>> logout(ServerWebExchange exchange) {
		return jwtServerAuthenticationConverter.convert(exchange)
			.flatMap(authentication ->
				authService.logout(authentication)
					.thenReturn(ResponseEntity.ok(createSuccessResponse()))
			);
	}

	@PostMapping("/refresh")
	public Mono<ResponseEntity<ResponseBody<Token>>> refresh(
		@RequestBody @Validated Mono<RefreshRequest> refreshRequest) {
		return refreshRequest
			.flatMap(request -> authService.refresh(request.accessToken(), request.refreshToken()))
			.map(token -> ResponseEntity.ok(createSuccessResponse(token)));
	}
}
