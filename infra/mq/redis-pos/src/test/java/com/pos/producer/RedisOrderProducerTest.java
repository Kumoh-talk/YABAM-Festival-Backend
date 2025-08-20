package com.pos.producer;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.pos.consumer.RedisConsumerHandler;
import com.pos.consumer.SseEventHandler;
import com.pos.global.config.consumer.RedisConsumerConfig;
import com.pos.global.config.producer.RedisProducerConfig;
import com.pos.producer.config.RedisConfig;

import domain.pos.order.entity.Order;
import domain.pos.store.entity.Store;
import domain.pos.table.entity.Table;
import fixtures.order.OrderFixture;

@SpringBootTest(classes = {RedisProducerConfig.class, RedisConsumerConfig.class, RedisConfig.class})
@ActiveProfiles("test")
class RedisOrderProducerTest {

	@Autowired
	private RedisOrderProducer redisOrderProducer;

	@Autowired
	private RedisConsumerHandler redisConsumerHandler;

	@MockitoBean
	private SseEventHandler sseEventHandler;

	private static final String CHANNEL_PREFIX = "storeId:";

	@Test
	void produce_consume_통합테스트() throws InterruptedException {
		// given
		Order order = OrderFixture.GENERAL_ORDER();
		Store store = order.getReceipt().getSale().getStore();
		Table table = order.getReceipt().getTable();
		redisConsumerHandler.subscribe(store.getId());

		// when
		redisOrderProducer.produceStoreOrder(store, table, order);
		// then
		verify(sseEventHandler, timeout(1000))
			.handleEventWithSSE(any(), any(), any(), any());
	}
}
