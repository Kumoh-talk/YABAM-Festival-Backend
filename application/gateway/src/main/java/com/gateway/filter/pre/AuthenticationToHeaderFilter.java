package com.gateway.filter.pre;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.exception.JwtAccessDeniedException;
import com.gateway.exception.JwtTokenExpiredException;
import com.gateway.exception.JwtTokenInvalidException;
import com.gateway.jwt.JwtHandler;
import com.gateway.jwt.JwtUserClaim;
import com.http.HttpHeaderName;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class AuthenticationToHeaderFilter implements WebFilter {
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final ServerAuthenticationFailureHandler authenticationFailureHandler;
	private final JwtHandler jwtHandler;

	private final String USER_ID = "userId";
	private final String USER_NICKNAME = "userNickname";
	private final String USER_ROLE = "userRole";

	public AuthenticationToHeaderFilter(
		ServerAuthenticationFailureHandler authenticationFailureHandler,
		JwtHandler jwtHandler) {
		this.authenticationFailureHandler = authenticationFailureHandler;
		this.jwtHandler = jwtHandler;

	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

		JwtUserClaim jwtUserClaim;
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);
			try {
				jwtUserClaim = jwtHandler.parseToken(token);
			} catch (ExpiredJwtException e) {
				return authenticationFailureHandler.onAuthenticationFailure(
					new WebFilterExchange(exchange, chain),
					new JwtTokenExpiredException(e)
				);
			} catch (JwtAccessDeniedException e) {
				return authenticationFailureHandler.onAuthenticationFailure(
					new WebFilterExchange(exchange, chain), e
				);
			} catch (Exception e) {
				return authenticationFailureHandler.onAuthenticationFailure(
					new WebFilterExchange(exchange, chain),
					new JwtTokenInvalidException(e)
				);
			}
			log.info("Authenticate UserId : {}", jwtUserClaim.userId());
		} else {
			jwtUserClaim = jwtHandler.createAnonymous();
		}

		try {
			Map<String, Object> userInfo = new LinkedHashMap<>();
			userInfo.put(USER_ID, jwtUserClaim.userId());
			userInfo.put(USER_NICKNAME, jwtUserClaim.userNickname());
			userInfo.put(USER_ROLE, jwtUserClaim.userRole().name());

			String userInfoJson = objectMapper.writeValueAsString(userInfo);
			ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
				.header(HttpHeaderName.REQUEST_USER_INFO_HEADER, userInfoJson)
				.build();
			ServerWebExchange mutatedExchange = exchange.mutate()
				.request(mutatedRequest)
				.build();

			return chain.filter(mutatedExchange);

		} catch (JwtException | JsonProcessingException e) {
			return authenticationFailureHandler.onAuthenticationFailure(
				new WebFilterExchange(exchange, chain),
				new JwtTokenInvalidException(e)
			);
		}
	}
}
