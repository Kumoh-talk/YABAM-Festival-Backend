package com.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
	"com.pos.consumer.store.order",
	"com.pos.consumer",
	"com.event"
})
public class EventApplication {
	public static void main(String[] args) {
		SpringApplication.run(EventApplication.class, args);
	}
}
