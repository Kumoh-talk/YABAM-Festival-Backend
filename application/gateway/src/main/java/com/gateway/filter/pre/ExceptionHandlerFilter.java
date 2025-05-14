package com.gateway.filter.pre;

import java.nio.charset.StandardCharsets;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class ExceptionHandlerFilter implements WebFilter {

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		return chain.filter(exchange)
			.onErrorResume(Exception.class, e -> {
				log.error("exception: {}", e.getMessage());
				return handleServiceException(exchange.getResponse(), e);
			});
	}

	private Mono<Void> handleServiceException(ServerHttpResponse response, Exception exception) {
		response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
		response.getHeaders().setContentType(MediaType.valueOf("application/json; charset=UTF-8"));

		String errorResponse = "{\"error\":\"Internal Server Error\"}";

		DataBuffer dataBuffer = response.bufferFactory().wrap(errorResponse.getBytes(StandardCharsets.UTF_8));
		return response.writeWith(Mono.just(dataBuffer));
	}
}
