package com.gateway.config;

import java.util.Collections;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import com.gateway.exception.handler.CustomAccessDeniedHandler;
import com.gateway.exception.handler.CustomAuthenticationEntryPoint;
import com.gateway.exception.handler.CustomAuthenticationFailureHandler;
import com.gateway.filter.pre.AuthenticationToHeaderFilter;

import lombok.RequiredArgsConstructor;

@EnableMethodSecurity
@RequiredArgsConstructor
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
	// TODO : 익명 사용자도 필터 통과시키는 것이 확정되면, entryPoint, accessDeniedHandler 삭제해야함
	private final CustomAuthenticationEntryPoint authenticationEntryPoint;
	private final CustomAccessDeniedHandler accessDeniedHandler;
	private final CustomAuthenticationFailureHandler authenticationFailureHandler;
	private static final String[] SWAGGER_WHITELIST = {
		// springdoc-webflux 기본
		"/swagger-ui.html",
		"/swagger-ui/**",
		"/v3/api-docs",
		"/v3/api-docs/**",
		// 사용자 정의 ‘/docs’ 진입점
		"/docs",
		"/docs/**",
	};

	/* ───── ① Swagger 전용 체인 ───── */
	@Bean
	@Order(0)
	public SecurityWebFilterChain swaggerChain(ServerHttpSecurity http) {
		return http
			.securityMatcher(ServerWebExchangeMatchers.pathMatchers(SWAGGER_WHITELIST))
			.csrf(csrf -> csrf.disable())
			.securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
			.authorizeExchange(ex -> ex.anyExchange().permitAll())
			.build();
	}

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
		ReactiveAuthenticationManager authenticationManager,
		ServerAuthenticationConverter authenticationConverter,
		ServerSecurityContextRepository serverSecurityContextRepository) {
		AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(authenticationManager);
		authenticationWebFilter.setServerAuthenticationConverter(authenticationConverter);
		authenticationWebFilter.setAuthenticationFailureHandler(authenticationFailureHandler);
		authenticationWebFilter.setSecurityContextRepository(serverSecurityContextRepository);

		http
			.csrf(ServerHttpSecurity.CsrfSpec::disable)
			.httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
			.formLogin(ServerHttpSecurity.FormLoginSpec::disable)
			.securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
			.addFilterAt(authenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION) // JWT 인증 필터 추가
			// .addFilterBefore(new ExceptionHandlerFilter(), SecurityWebFiltersOrder.AUTHENTICATION) // 예외 처리 필터 추가
			.addFilterAfter(
				new AuthenticationToHeaderFilter(serverSecurityContextRepository, authenticationFailureHandler),
				SecurityWebFiltersOrder.AUTHENTICATION) // 사용자 정보 헤더 추가 필터 추가
			.authorizeExchange(exchange -> exchange
				.anyExchange().authenticated())
			.exceptionHandling(exceptionHandling ->
				exceptionHandling
					.authenticationEntryPoint(authenticationEntryPoint)
					.accessDeniedHandler(accessDeniedHandler));

		return http.build();
	}

	@LoadBalanced
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public ServerSecurityContextRepository securityContextRepository() {
		return new WebSessionServerSecurityContextRepository();
	}

	@Bean
	public CorsWebFilter corsWebFilter() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOriginPatterns(Collections.singletonList("*"));
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		return new CorsWebFilter(source);
	}

}
