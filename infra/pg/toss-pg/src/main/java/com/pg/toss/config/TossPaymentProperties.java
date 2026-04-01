package com.pg.toss.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "toss.payment")
public class TossPaymentProperties {
	private String secretKey;
	private String baseUrl;
	private int connectTimeoutSeconds = 5;
	private int readTimeoutSeconds = 30;
	private int confirmMaxRetries = 1;
}
