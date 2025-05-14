package com.gateway.filter.pre;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.exception.JwtTokenInvalidException;
import com.gateway.jwt.JwtAuthentication;
import com.http.HttpHeaderName;

import reactor.core.publisher.Mono;

public class AuthenticationToHeaderFilter implements WebFilter {
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final ServerSecurityContextRepository securityContextRepository;
	private final ServerAuthenticationFailureHandler authenticationFailureHandler;

	private final String USER_ID = "userId";
	private final String USER_NICKNAME = "userNickname";
	private final String USER_ROLE = "userRole";

	public AuthenticationToHeaderFilter(ServerSecurityContextRepository securityContextRepository,
		ServerAuthenticationFailureHandler authenticationFailureHandler) {
		this.securityContextRepository = securityContextRepository;
		this.authenticationFailureHandler = authenticationFailureHandler;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		return securityContextRepository.load(exchange)
			.map(SecurityContext::getAuthentication)
			.flatMap(authentication -> {
				JwtAuthentication jwtAuthentication = (JwtAuthentication)authentication;

				Map<String, Object> userInfo = new LinkedHashMap<>();
				userInfo.put(USER_ID, jwtAuthentication.userId());
				userInfo.put(USER_NICKNAME, jwtAuthentication.userNickname());
				userInfo.put(USER_ROLE, jwtAuthentication.userRole().name());

				try {
					String userInfoJson = objectMapper.writeValueAsString(userInfo);
					exchange.getRequest().getHeaders().add(HttpHeaderName.REQUEST_USER_INFO_HEADER, userInfoJson);
				} catch (JsonProcessingException e) {
					authenticationFailureHandler.onAuthenticationFailure(new WebFilterExchange(exchange, chain),
						new JwtTokenInvalidException(e));
				}

				return chain.filter(exchange);
			})
			.switchIfEmpty(chain.filter(exchange));
	}
}
