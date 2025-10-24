package com.config;

import javax.sql.DataSource;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.aop.AopTestHelper;

@Configuration
@Import(WebMvcConfig.class)
public class AopTestConfig {

	@Bean
	public AopTestHelper aopTestHelper() {
		return new AopTestHelper();
	}

	@Bean
	public DataSource dataSource() {
		return DataSourceBuilder.create()
			.url("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
			.driverClassName("org.h2.Driver")
			.username("sa")
			.password("")
			.build();
	}
}
