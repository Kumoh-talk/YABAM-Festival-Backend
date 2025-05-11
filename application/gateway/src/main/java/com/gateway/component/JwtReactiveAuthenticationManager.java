package com.gateway.component;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.gateway.exception.JwtAccessDeniedException;
import com.gateway.exception.JwtTokenExpiredException;
import com.gateway.exception.JwtTokenInvalidException;
import com.gateway.jwt.JwtAuthentication;
import com.gateway.jwt.JwtAuthenticationToken;
import com.gateway.jwt.JwtHandler;
import com.gateway.jwt.JwtUserClaim;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

	private final JwtHandler jwtHandler;
	// private final UserRestTemplate userRestTemplate;

	@Override
	public Mono<Authentication> authenticate(Authentication authentication) {
		JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken)authentication;
		String tokenValue = jwtAuthenticationToken.token();

		if (tokenValue == null) {
			return Mono.just(JwtAuthentication.anonymousAuthentication());
		}

		return Mono.fromCallable(() -> {
			try {
				JwtUserClaim claims = jwtHandler.parseToken(tokenValue);
				// validateAdminRole(claims);
				return new JwtAuthentication(claims);
			} catch (ExpiredJwtException e) {
				throw new JwtTokenExpiredException(e);
			} catch (JwtAccessDeniedException e) {
				throw e; // 예외를 그대로 던짐
			} catch (Exception e) {
				throw new JwtTokenInvalidException(e);
			}
		});
	}

	// private void validateAdminRole(JwtUserClaim claims) {
	// 	if (Role.ROLE_ADMIN.equals(claims.userRole())
	// 		// TODO : RestTemplate 사용하여 admin인지 확인
	// 		&& !userRestTemplate.isAdmin(claims.userId())) {
	// 		throw new JwtAccessDeniedException();
	// 	}
	// }
}
