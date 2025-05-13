package com.gateway.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.data.redis.core.ReactiveRedisTemplate;

import com.gateway.user.Role;
import com.vo.Token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class JwtHandler {

	public static final String USER_ID = "USER_ID";
	public static final String USER_NICKNAME = "USER_NICKNAME";
	public static final String USER_ROLE = "USER_ROLE";
	private static final long MILLI_SECOND = 1000L;

	private final SecretKey secretKey;
	private final JwtProperties jwtProperties;
	private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

	public JwtHandler(JwtProperties jwtProperties, ReactiveRedisTemplate<String, String> reactiveRedisTemplate) {
		this.jwtProperties = jwtProperties;
		secretKey = new SecretKeySpec(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8),
			Jwts.SIG.HS256.key().build().getAlgorithm());
		this.reactiveRedisTemplate = reactiveRedisTemplate;
	}

	public Mono<Token> createTokens(JwtUserClaim jwtUserClaim) {
		Map<String, Object> tokenClaims = this.createClaims(jwtUserClaim);
		Date now = new Date(System.currentTimeMillis());
		long accessTokenExpireIn = jwtProperties.getAccessTokenExpireIn();

		String accessToken = Jwts.builder()
			.claims(tokenClaims)
			.issuedAt(now)
			.expiration(new Date(now.getTime() + accessTokenExpireIn * MILLI_SECOND))
			.signWith(secretKey)
			.compact();

		String refreshKey = String.valueOf(jwtUserClaim.userId());
		String refreshToken = UUID.randomUUID().toString();

		return reactiveRedisTemplate
			.opsForValue()
			.set(refreshKey, refreshToken, Duration.ofSeconds(jwtProperties.getRefreshTokenExpireIn()))
			.thenReturn(Token.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.build());
	}

	public Map<String, Object> createClaims(JwtUserClaim jwtUserClaim) {
		return Map.of(
			USER_ID, jwtUserClaim.userId(),
			USER_NICKNAME, jwtUserClaim.userNickname(),
			USER_ROLE, jwtUserClaim.userRole()
		);
	}

	// 필터에서 토큰의 상태를 검증하기 위한 메서드 exception은 사용하는 곳에서 처리
	public JwtUserClaim parseToken(String token) {
		Claims claims = Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload();

		return this.convert(claims);
	}

	public Optional<JwtUserClaim> getClaims(String token) {
		try {
			Claims claims = Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
			return Optional.of(this.convert(claims));
		} catch (ExpiredJwtException e) {
			Claims claims = e.getClaims();
			return Optional.of(this.convert(claims));
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	public JwtUserClaim convert(Claims claims) {
		return new JwtUserClaim(
			claims.get(USER_ID, Long.class),
			claims.get(USER_NICKNAME, String.class),
			Role.valueOf(claims.get(USER_ROLE, String.class))
		);
	}

	public Mono<Void> deleteRefreshToken(String userId) {
		return reactiveRedisTemplate.delete(userId).then();
	}

	public Mono<String> getRefreshToken(Long userId) {
		return reactiveRedisTemplate.opsForValue().get(String.valueOf(userId));
	}
}
