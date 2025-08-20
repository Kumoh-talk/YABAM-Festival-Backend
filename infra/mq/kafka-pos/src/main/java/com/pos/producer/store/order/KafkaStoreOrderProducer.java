package com.pos.producer.store.order;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pos.event.StoreOrderEvent;
import com.pos.global.properties.KafkaStoreOrderProperties;
import com.pos.producer.store.order.common.StoreOrderEventMapper;

import domain.pos.order.entity.Order;
import domain.pos.order.implement.StoreOrderProducer;
import domain.pos.store.entity.Store;
import domain.pos.table.entity.Table;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaStoreOrderProducer implements StoreOrderProducer {
	private final KafkaTemplate<String, String> kafkaTemplate;
	private final KafkaStoreOrderProperties properties;
	private final ObjectMapper objectMapper;

	@Override
	public void produceStoreOrder(Store store, Table table, Order order) {
		StoreOrderEvent storeOrderEvent = StoreOrderEventMapper.toStoreOrderEvent(table, order);
		String key = String.valueOf(store.getId());
		String message;
		try {
			message = objectMapper.writeValueAsString(storeOrderEvent);
		} catch (JsonProcessingException e) {
			log.error("객체 문자열 JSON으로 변환 실패 에러 메시지 : {} ", e.getMessage());
			throw new RuntimeException(e);
		}
		kafkaTemplate.send(properties.getTopic(), key, message)
			.whenComplete((record, ex) -> {
				if (ex != null) {
					log.error("카프카 메시지 전송 실패 토픽 : {}, key : {}, 에러 메시지 : {}  ",
						properties.getTopic(),
						key,
						ex.getMessage());
				} else {
					log.info("카프카 메시지 전송 성공, topic: {}, key: {}", properties.getTopic(), key);
				}
			});
	}
}
