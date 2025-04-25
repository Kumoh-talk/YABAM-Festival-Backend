package com.pos.consumer.store.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
		ConsumerFactory<String, String> consumerFactory) {
		ConcurrentKafkaListenerContainerFactory<String, String> factory =
			new ConcurrentKafkaListenerContainerFactory<>();

		factory.setConsumerFactory(consumerFactory);
		factory.getContainerProperties()
			.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
		return factory;
	}
}
