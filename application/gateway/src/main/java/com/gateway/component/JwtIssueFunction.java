package com.gateway.component;

import static com.response.ResponseUtil.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.jwt.JwtHandler;
import com.gateway.jwt.JwtUserClaim;
import com.http.HttpHeaderName;
import com.response.ResponseBody;
import com.vo.Token;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtIssueFunction implements RewriteFunction<String, String> {

	private final ObjectMapper objectMapper;

	private final JwtHandler jwtHandler;

	@Override
	public Mono<String> apply(ServerWebExchange exchange, String originalBody) {
		return Mono.defer(() -> {
			if (exchange.getResponse().isCommitted()) {
				return Mono.just(originalBody);
			}

			String userInfoHeader = exchange.getResponse()
				.getHeaders()
				.getFirst(HttpHeaderName.RESPONSE_USER_INFO_HEADER);
			if (!StringUtils.hasText(userInfoHeader)) {
				return Mono.just(originalBody);
			}

			return Mono.fromCallable(
					() -> objectMapper.readValue(URLDecoder.decode(userInfoHeader, StandardCharsets.UTF_8),
						JwtUserClaim.class))
				.flatMap(jwtHandler::createTokens)
				.flatMap(token -> {
					try {
						ResponseBody<Token> responseBody = createSuccessResponse(token);
						String json = objectMapper.writeValueAsString(responseBody);
						return Mono.just(json);
					} catch (JsonProcessingException e) {
						return Mono.error(new RuntimeException("JSON serialization failed", e));
					}
				});
		});
	}
}
