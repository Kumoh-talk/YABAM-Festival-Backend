package com.gateway.filter.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

public class CommitCheckFilter implements GatewayFilter {

	private static final Logger log = LoggerFactory.getLogger(CommitCheckFilter.class);

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		if (exchange.getResponse().isCommitted()) {
			log.warn("Response already committed. Skipping filter chain.");
			return exchange.getResponse().setComplete(); // 필터 체인 종료
		}
		return chain.filter(exchange);
	}
}
