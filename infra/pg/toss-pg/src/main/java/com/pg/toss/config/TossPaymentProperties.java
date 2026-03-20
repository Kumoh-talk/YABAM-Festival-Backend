package com.pg.toss.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "toss.payment")
public class TossPaymentProperties {
    private final String secretKey;
    private final String baseUrl;
}
