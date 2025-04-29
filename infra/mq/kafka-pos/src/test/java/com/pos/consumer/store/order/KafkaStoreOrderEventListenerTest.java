package com.pos.consumer.store.order;

import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
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

import com.pos.consumer.StoreOrderHandler;
import com.pos.consumer.store.order.config.KafkaConsumerConfig;
import com.pos.event.StoreOrderEvent;
import com.pos.producer.store.order.KafkaStoreOrderProducer;
import com.pos.producer.store.order.config.KafkaProcuerConfig;

@SpringBootTest
@Import(value = {KafkaProcuerConfig.class,
	KafkaConsumerConfig.class,
	KafkaStoreOrderEventListener.class
})
@DirtiesContext
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {"${kafka.store.order.topic}"},
	brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
class KafkaStoreOrderEventListenerTest {

	@MockitoBean
	private StoreOrderHandler storeOrderHandler;

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	@Autowired
	private KafkaStoreOrderProducer kafkaStoreOrderProducer;

	@Test
	void kafkaListenerTest() throws Exception {
		// given
		StoreOrderEvent storeOrderEvent = new StoreOrderEvent(
			10L,
			5,
			new StoreOrderEvent.Order(
				1001L,
				LocalDateTime.now(),
				List.of(new StoreOrderEvent.Order.OrderMenu(1L, "Americano", 3000, 2, "ORDERED"))
			)
		);
		TimeUnit.SECONDS.sleep(1);
		// when
		kafkaStoreOrderProducer.produceStoreOrder("1", storeOrderEvent);
		kafkaTemplate.flush();

		// then
		verify(storeOrderHandler, timeout(1000))
			.handleStoreOrder(anyString(), anyString(), any(StoreOrderEvent.class));
	}
}
