# Redis Pub/Sub 모듈 활용 가이드

## 1. build.gradle 의존성 추가

`application:yabam:yabam-core` 모듈의 build.gradle 파일에 다음과 같이 의존성을 추가합니다:

```textmate
dependencies {
    // 기존 의존성들...
    
    // infra:mq:redis-pos 모듈 의존성 추가
    implementation project(':infra:mq:redis-pos')
}
```

## 2. 모듈 빈 설명 및 컨텍스트 로딩 설정

해당 모듈에서는 Store-Order Event 관련 Produce 와 Consume을 수행하는 Redis Pub/Sub 기반 기능을 수행하는 컴포넌트들이 존재합니다

크게 `Producer` 와 `Consumer` 관련 컴포넌트들이 존재합니다

해당 모듈을 의존하는 dependencing 모듈에서는 Config 클래스를 이용해 Producer 혹은 Consumer 관련 빈을 로드해서 사용할 수 있습니다

`driving` 모듈의 메인 애플리케이션 클래스나 설정 클래스에서 `infra:mq:redis-pos` 모듈의 컴포넌트를 스캔하도록 설정하는 예제입니다:

```java
package com.yabam.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@Import(RedisProducerConfig.class) // Redis 관련 설정과 Producer관련 빈으로 로딩 합니다.
@Import(RedisConsumerConfig.class) // Redis 관련 설정과 Consumer관련 빈으로 로딩 합니다.
public class YabamCoreApplication {
	public static void main(String[] args) {
		SpringApplication.run(YabamCoreApplication.class, args);
	}
}
```

## 3. Kafka 설정 추가

`application:yabam:yabam-core` 모듈의 application.yml(또는 properties) 파일에 KafkaStoreOrderProperties 설정을 추가합니다:

프로퍼티 설정은 Redis 설정에 맞게 해주세요

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6311
```

## 주의사항

1. **순환 의존성 방지**: 두 모듈 간에 순환 의존성이 생기지 않도록 주의하세요.
2. **권한 분리**: 모듈 간 필요한 기능만 노출하고 사용하도록 인터페이스를 설계하세요.
3. **설정 충돌**: 두 모듈에서 동일한 빈이나 설정이 충돌하지 않도록 확인하세요.
4. **버전 관리**: 모듈 간 의존성 버전이 일치하는지 확인하세요.

이 가이드를 따라 `infra:mq:redis-pos` 모듈을 `application:yabam:yabam-core` 모듈에서 사용할 수 있도록 설정하면, 코어 모듈에서 Kafka를 통한 이벤트 발행 기능을 활용할
수 있습니다.

# Redis Pub/Sub 구현사항

## 1. Producer

- 해당 컴포넌트는 메시지 통신 신뢰성 수준을 At-Least-Once 정도의 수준을 보장하므로 신뢰성보다는 고성능에 초점을 맞춤
- 기존 블록킹 I/O 작업을 수행하는 RedisTemplate 말고, ReactiveRedisTemplate을 채택하여 비동기 논블록킹 I/O 작업을 수행
-

```java

@Component
@Slf4j
@RequiredArgsConstructor
public class RedisOrderProducer implements StoreOrderProducer {
	// 생략
	@Override
	public void produceStoreOrder(Store store, Table table, Order order) {
		// 생략
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
```

- 비동기 처리가 가능하지만 result 를 받아서 핸들링이 가능하다.
- 현재는 단순한 로그만 찍히는 정도이며 result 가 정수형인데 이 데이터는 현재 Redis Pub/Sub에 해당 Topic을 Subscribe 하는 Consumer 의 갯수를 의미한다.

## 2. Consumer

- Spring Data Redis 에서 제공하는 MessageListener 를 Implement 하여 Redis Pub/Sub 메시지를 수신하고 처리하는 로직을 수행할 수 있음
- 수신하기 전에 해당 Topic 을 Subscribe 하는 동작이 선행되어야 하는데 이는 동적으로 Topic이 정해져야 하므로 Subscribe 동작과 Unsubscribe 하는 동작을 별도로 구현함
- SSE 세션이 추가됨에 따라 Topic subscriber 가 늘어나게 되고 해당 Topic에 해당하는 MessageListener를 RedisMessageListenerContainer에 등록하여 메시지를
  수신할 수 있도록 구현

```java

@Component
public class RedisConsumerHandler {
	private final RedisMessageListenerContainer container;
	private final Map<String, MessageListener> listenerMap = new ConcurrentHashMap<>(); // 동적 Topic 관리
	private final RedisStoreOrderListener redisStoreOrderListener;

	public void subscribe(String channel) {
		if (listenerMap.containsKey(channel)) {
			return;
		}
		listenerMap.put(channel, redisStoreOrderListener);
		container.addMessageListener(redisStoreOrderListener,
			ChannelTopic.of(channel)); // RedisMessageListenerContainer에 MessageListener 등록
	}

	public void unsubscribe(String channel) {
		if (!listenerMap.containsKey(channel)) {
			return;
		}
		container.removeMessageListener(listenerMap.get(channel), ChannelTopic.of(channel));
		listenerMap.remove(channel);
	}
}
```

## 3. Serialization

```java

@Configuration
@RequiredArgsConstructor
public class RedisSerializerConfig {
	@Bean
	public GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer(ObjectMapper objectMapper) {
		return new GenericJackson2JsonRedisSerializer(objectMapper);
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper()
			.registerModule(new ParameterNamesModule())
			.registerModule(new JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}
}
```

| 모듈/플래그                               | 역할                                                                                     |
|--------------------------------------|----------------------------------------------------------------------------------------|
| **`ParameterNamesModule`**           | 자바 **record**나 `@ConstructorBinding` 객체를 역직렬화할 때, 생성자 매개변수 이름을 자동 인식합니다.               |
| **`JavaTimeModule`**                 | `LocalDateTime`, `LocalDate`, `Instant` 등 **java.time** 타입을 ISO-8601 문자열로 직렬화/역직렬화합니다. |
| **`WRITE_DATES_AS_TIMESTAMPS` 비활성화** | 날짜·시간을 `1624025100000` 같은 UNIX 타임스탬프가 아니라 **`"2025-06-18T15:40:12.345"`** 형식으로 유지합니다.  |









