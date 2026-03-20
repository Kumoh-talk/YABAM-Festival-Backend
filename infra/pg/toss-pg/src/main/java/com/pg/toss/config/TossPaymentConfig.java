package com.pg.toss.config;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(TossPaymentProperties.class)
public class TossPaymentConfig {

    @Bean
    public RestClient tossRestClient(TossPaymentProperties properties) {
        String encoded = Base64.getEncoder()
            .encodeToString((properties.getSecretKey() + ":").getBytes(StandardCharsets.UTF_8));

        return RestClient.builder()
            .baseUrl(properties.getBaseUrl())
            .defaultHeader("Authorization", "Basic " + encoded)
            .defaultHeader("Content-Type", "application/json")
            .build();
    }
}
