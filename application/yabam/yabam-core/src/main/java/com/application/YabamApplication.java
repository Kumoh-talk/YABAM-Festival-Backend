package com.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {
	"domain.pos",
	"com.application"
})
@EnableDiscoveryClient
public class YabamApplication {

	public static void main(String[] args) {
		SpringApplication.run(YabamApplication.class, args);
	}

}
