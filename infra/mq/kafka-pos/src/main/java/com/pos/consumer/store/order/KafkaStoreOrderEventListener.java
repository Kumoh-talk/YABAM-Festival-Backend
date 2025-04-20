package com.pos.consumer.store.order;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pos.consumer.StoreOrderHandler;
import com.pos.event.StoreOrderEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "event.listener", havingValue = "kafka", matchIfMissing = true)
public class KafkaStoreOrderEventListener {
	private final StoreOrderHandler storeOrderHandler;
	private final ObjectMapper mapper;

	@KafkaListener(topics = "store-orders", groupId = "store-order-consumer")
	public void listen(@Header(KafkaHeaders.RECEIVED_KEY) String key, String json) {
		try {
			StoreOrderEvent storeOrderEvent = mapper.readValue(json, StoreOrderEvent.class);
			onOrderEvent(key, storeOrderEvent);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public void onOrderEvent(String key, StoreOrderEvent storeOrderEvent) {
		storeOrderHandler.handleStoreOrder(key, storeOrderEvent);
	}
}
