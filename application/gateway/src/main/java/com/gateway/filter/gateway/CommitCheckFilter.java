package com.gateway.filter.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class CommitCheckFilter implements GatewayFilter {

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		if (exchange.getResponse().isCommitted()) {
			log.warn("Response already committed. Skipping filter chain.");
			return exchange.getResponse().setComplete();
		}
		return chain.filter(exchange);
	}
}
