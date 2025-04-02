package com.gateway.exception.handler;

import static com.response.ResponseUtil.*;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.response.ResponseBody;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {
	ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
		log.error("JwtAuthenticationException: {}", ex.getMessage());

		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(HttpStatus.UNAUTHORIZED);
		response.getHeaders().setContentType(MediaType.valueOf("application/json; charset=UTF-8"));

		ResponseBody<Void> errorResponse = createFailureResponse(ErrorCode.NEED_AUTHORIZED);

		return Mono.fromCallable(() -> objectMapper.writeValueAsBytes(errorResponse))
			.map(bytes -> {
				DataBuffer dataBuffer = response.bufferFactory().wrap(bytes);
				return response.writeWith(Mono.just(dataBuffer));
			})
			.flatMap(mono -> mono)  // Mono<Void> 반환
			.onErrorResume(JsonProcessingException.class, e -> {
				// JSON 처리 중 오류 발생 시 처리 (예: 로그 기록)
				response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
				return response.writeWith(Mono.empty()); // 빈 응답 반환
			});
	}
}
