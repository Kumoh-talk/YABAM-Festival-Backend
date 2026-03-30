package com.pg.toss.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestClient;

@EnableRetry
@Configuration
@EnableConfigurationProperties(TossPaymentProperties.class)
public class TossPaymentConfig {

	@Bean
	public RestClient tossRestClient(TossPaymentProperties properties) {
		String encoded = Base64.getEncoder()
			.encodeToString((properties.getSecretKey() + ":").getBytes(StandardCharsets.UTF_8));

		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(Duration.ofSeconds(properties.getConnectTimeoutSeconds()));
		factory.setReadTimeout(Duration.ofSeconds(properties.getReadTimeoutSeconds()));

		return RestClient.builder()
			.requestFactory(factory)
			.baseUrl(properties.getBaseUrl())
			.defaultHeader("Authorization", "Basic " + encoded)
			.defaultHeader("Content-Type", "application/json")
			.build();
	}
}
