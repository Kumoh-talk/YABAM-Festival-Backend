package com.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.exception.handler.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interceptor.InterceptorTestHelper;

@Configuration
@Import({WebMvcConfig.class, GlobalExceptionHandler.class})
public class InterceptorTestConfig {

	@Bean
	public InterceptorTestHelper interceptorTestHelper() {
		return new InterceptorTestHelper();
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}
}
