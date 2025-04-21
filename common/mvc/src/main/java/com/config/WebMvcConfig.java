package com.config;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.aop.AssignUserPassportAspect;
import com.interceptor.AuthorizationInterceptor;
import com.interceptor.DeserializingUserPassportInterceptor;

import lombok.RequiredArgsConstructor;

@Configuration
@ConditionalOnWebApplication
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

	@Bean
	public DeserializingUserPassportInterceptor deserializingUserPassportInterceptor() {
		return new DeserializingUserPassportInterceptor();
	}

	@Bean
	public AuthorizationInterceptor authorizationInterceptor() {
		return new AuthorizationInterceptor();
	}

	@Bean
	public AssignUserPassportAspect assignUserPassportAspect() {
		return new AssignUserPassportAspect();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(deserializingUserPassportInterceptor())
			.addPathPatterns("/**");
		registry.addInterceptor(authorizationInterceptor())
			.addPathPatterns("/**");
	}
}
