package com.pos.producer;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;

import com.pos.event.StoreOrderEvent;
import com.pos.producer.mapper.StoreOrderEventMapper;
import com.pos.util.ChannelPrefixUtil;

import domain.pos.order.entity.Order;
import domain.pos.order.implement.StoreOrderProducer;
import domain.pos.store.entity.Store;
import domain.pos.table.entity.Table;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.scheduler.Schedulers;

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisOrderProducer implements StoreOrderProducer {
	private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

	@Override
	public void produceStoreOrder(Store store, Table table, Order order) {
		String topic = ChannelPrefixUtil.STORE_ORDER_PREFIX + store.getId();
		StoreOrderEvent storeOrderEvent = StoreOrderEventMapper.toStoreOrderEvent(table, order);
		reactiveRedisTemplate.convertAndSend(topic, storeOrderEvent)
			.subscribeOn(Schedulers.boundedElastic())
			.subscribe(result -> {
				if (result > 0) {
					log.info("Message sent to channel: {}, Subscribers: {}", topic, result);
				} else {
					log.info("No subscribers found for channel: {}", topic);
				}
			}, error -> {
				log.warn("Failed to send message to channel: {}, Error: {}", topic, error.getMessage());
			});
	}
}
