package com.pos.global.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan(basePackages = "com.pos")
@EnableTransactionManagement
@EntityScan(basePackages = "com.pos")
@EnableJpaRepositories(basePackages = "com.pos")
public class JpaComponentConfig {
}

