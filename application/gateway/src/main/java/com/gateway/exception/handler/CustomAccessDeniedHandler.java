package com.gateway.exception.handler;

import static com.response.ResponseUtil.*;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
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
public class CustomAccessDeniedHandler implements ServerAccessDeniedHandler {
	private final ObjectMapper objectMapper;

	@Override
	public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
		log.error("JwtAuthenticationException: {}", denied.getMessage());

		ServerHttpResponse response = exchange.getResponse();
		response.setStatusCode(HttpStatus.FORBIDDEN);
		response.getHeaders().setContentType(MediaType.valueOf("application/json; charset=UTF-8"));

		ResponseBody<Void> errorResponse = createFailureResponse(ErrorCode.ACCESS_DENIED);

		return Mono.fromCallable(() -> objectMapper.writeValueAsBytes(errorResponse))
			.map(bytes -> {
				DataBuffer dataBuffer = response.bufferFactory().wrap(bytes);
				return response.writeWith(Mono.just(dataBuffer));
			})
			.flatMap(mono -> mono)
			.onErrorResume(JsonProcessingException.class, e -> {
				response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
				return response.writeWith(Mono.empty());
			});
	}
}
