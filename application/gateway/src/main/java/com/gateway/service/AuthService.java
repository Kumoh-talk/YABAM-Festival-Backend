package com.gateway.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.exception.ErrorCode;
import com.exception.ServiceException;
import com.gateway.exception.JwtAccessDeniedException;
import com.gateway.exception.JwtTokenExpiredException;
import com.gateway.exception.JwtTokenInvalidException;
import com.gateway.jwt.JwtAuthentication;
import com.gateway.jwt.JwtAuthenticationToken;
import com.gateway.jwt.JwtHandler;
import com.gateway.jwt.JwtUserClaim;
import com.vo.Token;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final JwtHandler jwtHandler;

	public Mono<Void> logout(Authentication authentication) {
		JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken)authentication;

		return Mono.fromCallable(() -> {
				try {
					if (jwtAuthenticationToken.token() == null) {
						throw new JwtTokenInvalidException();
					}

					JwtUserClaim claims = jwtHandler.parseToken(jwtAuthenticationToken.token());
					return new JwtAuthentication(claims);
				} catch (ExpiredJwtException e) {
					throw new JwtTokenExpiredException(e);
				} catch (JwtAccessDeniedException e) {
					throw e; // 예외를 그대로 던짐
				} catch (Exception e) {
					throw new JwtTokenInvalidException(e);
				}
			})
			.flatMap(claims -> jwtHandler.deleteRefreshToken(String.valueOf(claims.userId())))
			.then();
	}

	public Mono<Token> refresh(String accessToken, String refreshToken) {
		return Mono.fromCallable(() -> jwtHandler.getClaims(accessToken))
			.flatMap(claims -> {
				if (claims.isEmpty()) {
					return Mono.error(new ServiceException(ErrorCode.INVALID_ACCESS_TOKEN));
				}
				JwtUserClaim claim = claims.get();
				return jwtHandler.getRefreshToken(claim.userId())
					.flatMap(token -> {
						if (token.isEmpty() || !token.equals(refreshToken)) {
							return Mono.error(new ServiceException(ErrorCode.INVALID_REFRESH_TOKEN));
						}
						return jwtHandler.createTokens(claim);
					});
			});
	}
}
