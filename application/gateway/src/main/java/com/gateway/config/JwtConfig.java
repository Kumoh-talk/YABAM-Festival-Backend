package com.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

import com.gateway.jwt.JwtHandler;
import com.gateway.jwt.JwtProperties;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class JwtConfig {
	private final JwtProperties jwtProperties;

	@Bean
	public JwtHandler jwtHandler(ReactiveRedisTemplate<String, String> reactiveRedisTemplate) {
		return new JwtHandler(jwtProperties, reactiveRedisTemplate);
	}

}
