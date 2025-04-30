# Kafka 모듈을 Yabam Core에서 활용하기 위한 가이드

## 1. build.gradle 의존성 추가

`application:yabam:yabam-core` 모듈의 build.gradle 파일에 다음과 같이 의존성을 추가합니다:

```textmate
dependencies {
    // 기존 의존성들...
    
    // infra:mq:kafka-pos 모듈 의존성 추가
    implementation project(':infra:mq:kafka-pos')
}
```

## 2. 컴포넌트 스캔 범위 설정

`application:yabam:yabam-core` 모듈의 메인 애플리케이션 클래스나 설정 클래스에서 `infra:mq:kafka-pos` 모듈의 컴포넌트를 스캔하도록 설정합니다:

```java
package com.yabam.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@Import(KafkaProcuerConfig.class) // Kafka 관련 설정과 Producer를 빈으로 로딩 합니다.
public class YabamCoreApplication {
	public static void main(String[] args) {
		SpringApplication.run(YabamCoreApplication.class, args);
	}
}
```

## 3. Kafka 설정 추가

`application:yabam:yabam-core` 모듈의 application.yml(또는 properties) 파일에 KafkaStoreOrderProperties 설정을 추가합니다:

```yaml
kafka:
  store:
    order:
      topic: store-order
      group-id: store-order-group
      bootstrap-servers: localhost:9092
```

## 4. Order 저장 후 Order 정보를 발행해야하는 Kafka Producer 모듈 활용 예제

`StoreOrderProducer`를 사용하는 예제:

```java
package com.yabam.core.service;

import com.yabam.domain.pos.model.Order;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

	private final StoreOrderProducer storeOrderProducer;

	public void postOrder(Order order) {
		// 비즈니스 로직 처리

		// Kafka 메시지 발행
		storeOrderProducer.produceStoreOrder(store,
			table,
			order);
	}
}
```

## 주의사항

1. **순환 의존성 방지**: 두 모듈 간에 순환 의존성이 생기지 않도록 주의하세요.
2. **권한 분리**: 모듈 간 필요한 기능만 노출하고 사용하도록 인터페이스를 설계하세요.
3. **설정 충돌**: 두 모듈에서 동일한 빈이나 설정이 충돌하지 않도록 확인하세요.
4. **버전 관리**: 모듈 간 의존성 버전이 일치하는지 확인하세요.

이 가이드를 따라 `infra:mq:kafka-pos` 모듈을 `application:yabam:yabam-core` 모듈에서 사용할 수 있도록 설정하면, 코어 모듈에서 Kafka를 통한 이벤트 발행 기능을 활용할
수 있습니다.
