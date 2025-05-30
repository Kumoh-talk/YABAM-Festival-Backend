package com.pos.consumer.store.order;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pos.consumer.SseEventHandler;
import com.pos.event.SseChannelProvider;
import com.pos.event.StoreOrderEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaStoreOrderEventListener {
	private final SseEventHandler sseEventHandler;
	private final ObjectMapper mapper;
	private static final String EVENT_NAME = "StoreOrderEvent";

	@KafkaListener(topics = "${kafka.store.order.topic}", groupId = "${kafka.store.order.group-id}")
	public void listen(@Header(KafkaHeaders.RECEIVED_KEY) String key, String json) {
		log.info("KafkaStoreOrderEventListener listen() - key: {}, json: {}", key, json);
		try {
			StoreOrderEvent storeOrderEvent = mapper.readValue(json, StoreOrderEvent.class);
			onOrderEvent(key, storeOrderEvent);
		} catch (JsonProcessingException e) {
			log.error("KafkaStoreOrderEventListener listen() - JSON 변환 실패, key: {}, json: {}, error: {}",
				key,
				json,
				e.getMessage());
			throw new RuntimeException(e);
		}
	}

	private void onOrderEvent(String key, StoreOrderEvent storeOrderEvent) {
		sseEventHandler.handleEventWithSSE(SseChannelProvider.OWNER_STORE, EVENT_NAME, key, storeOrderEvent);
	}
}
