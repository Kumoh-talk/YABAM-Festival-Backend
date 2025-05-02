package com.pos.producer.store.order.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import com.pos.global.properties.KafkaStoreOrderProperties;
import com.pos.producer.store.order.KafkaStoreOrderProducer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@Import({KafkaStoreOrderProperties.class, KafkaStoreOrderProducer.class})
public class KafkaProcuerConfig {
	private final KafkaStoreOrderProperties kafkaStoreOrderProperties;

	@Bean
	public KafkaTemplate<String, String> kafkaTemplate() {
		Map<String, Object> producerProps = new HashMap<>();
		producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaStoreOrderProperties.getBootstrapServers());
		producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		producerProps.put(ProducerConfig.ACKS_CONFIG, "all");
		producerProps.put(ProducerConfig.RETRIES_CONFIG, 1000);
		producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false);
		ProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);
		return new KafkaTemplate<>(producerFactory);
	}
}
