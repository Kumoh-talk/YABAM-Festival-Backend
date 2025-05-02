package com.pos.consumer.store.order;

import static org.mockito.Mockito.*;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.pos.consumer.SseEventHandler;
import com.pos.consumer.store.order.config.KafkaConsumerConfig;
import com.pos.producer.store.order.KafkaStoreOrderProducer;
import com.pos.producer.store.order.config.KafkaProcuerConfig;

import domain.pos.order.entity.Order;
import domain.pos.store.entity.Store;
import domain.pos.table.entity.Table;
import fixtures.order.OrderFixture;

@SpringBootTest
@Import(value = {KafkaProcuerConfig.class,
	KafkaConsumerConfig.class,
	KafkaStoreOrderEventListener.class
})
@DirtiesContext
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"${kafka.store.order.topic}"},
	brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
class KafkaStoreOrderDtoEventListenerTest {

	@MockitoBean
	private SseEventHandler sseEventHandler;

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	@Autowired
	private KafkaStoreOrderProducer kafkaStoreOrderProducer;

	@Test
	void kafkaListenerTest() throws Exception {
		// given
		Order order = OrderFixture.GENERAL_ORDER();
		Store store = order.getReceipt().getSale().getStore();
		Table table = order.getReceipt().getTable();
		TimeUnit.SECONDS.sleep(1);
		// when
		kafkaStoreOrderProducer.produceStoreOrder(store, table, order);
		kafkaTemplate.flush();

		// then
		verify(sseEventHandler, timeout(1000))
			.handleEventWithSSE(any(), anyString(), anyString(), any());
	}
}
