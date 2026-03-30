package com.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
	"domain.pos",
	"com.application",
	"com.pg",
	"com.pos"
})
@EnableDiscoveryClient
@EnableScheduling
public class YabamApplication {

	public static void main(String[] args) {
		SpringApplication.run(YabamApplication.class, args);
	}

}
