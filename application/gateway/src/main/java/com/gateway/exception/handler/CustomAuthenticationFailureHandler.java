package com.gateway.exception.handler;

import static com.response.ResponseUtil.*;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.exception.JwtAuthenticationException;
import com.response.ResponseBody;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class CustomAuthenticationFailureHandler implements ServerAuthenticationFailureHandler {
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange, AuthenticationException exception) {
		log.error("JwtAuthenticationException: {}", exception.getMessage());

		JwtAuthenticationException jwtAuthenticationException = (JwtAuthenticationException)exception;
		ErrorCode errorCode = jwtAuthenticationException.getErrorCode();

		ServerWebExchange exchange = webFilterExchange.getExchange();
		ServerHttpResponse response = exchange.getResponse();

		response.setStatusCode(errorCode.getStatus());
		response.getHeaders().setContentType(MediaType.valueOf("application/json; charset=UTF-8"));

		ResponseBody<Void> errorResponse = createFailureResponse(errorCode);

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
