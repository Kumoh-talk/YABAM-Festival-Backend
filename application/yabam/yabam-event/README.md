# 야밤 이벤트 시스템 아키텍처 설명서

## 1. 시스템 개요

야밤 이벤트 시스템은 점주와 테이블 간의 실시간 주문 정보를 전달하기 위한 이벤트 기반 아키텍처입니다. Server-Sent Events(SSE) 기술을 활용하여 클라이언트에게 실시간으로 데이터를 푸시합니다.
<br>점주는 실시간으로(real-time) 데이터를 안정적이고 신뢰성 있게 주문 데이터를 전달 받아야합니다.

일관적인 실시간 데이터 수집을 위해 야밤은 리소스를 최소화하면서 SSE 연결을 유지하고, 장애 발생 시에도 자동으로 복구할 수 있는 아키텍처를 설계했습니다.

## 2. SSE 아키텍처

![img.png](img.png)

## 3. Real-Time 지원과 시스템 안정성

### 3.1 재연결 및 동기화 메커니즘

SSE 연결이 timeout 되거나 장애 발생 시 클라이언트는 동일한 Fetch Sync API를 호출하여 데이터를 동기화합니다. 이 API는 일반적인 재연결 상황과 장애 상황 모두에서 fallback 메커니즘으로
작동합니다.

``` mermaid
sequenceDiagram
    participant C as 클라이언트
    participant ES as 이벤트 서버
    participant YC as Yabam Core (Fetch Sync API)
    
    C->>ES: SSE 연결 요청
    ES-->>C: 연결 수립 및 이미터 제공
    ES->>C: 실시간 이벤트 스트림 전송
    Note over ES,C: 짧은 timeout 기간 설정됨
    ES->>C: 연결 timeout
    
    C->>YC: Fetch Sync API 호출 (마지막 수신 이벤트 ID 포함)
    YC-->>C: 누락된 데이터 응답 (상태 동기화)
    
    C->>ES: SSE 재연결 시도
    alt 재연결 성공
        ES-->>C: 새로운 연결 수립
        ES->>C: 실시간 이벤트 스트림 재개
    else 재연결 실패 (장애 상황)
        Note over C,ES: 이벤트 서버 장애 감지
        C->>YC: 동일한 Fetch Sync API 호출 (fallback)
        YC-->>C: 최신 데이터 응답
        Note over C: 주기적으로 API 호출하여 데이터 갱신
        C->>ES: SSE 연결 주기적 재시도
    end
```

### 3.2 통합된 Fetch Sync API

재연결 시와 장애 상황에서 모두 동일한 API를 사용하는 통합 접근 방식:

``` mermaid
graph TD
    A[클라이언트] -->|"1. SSE 연결 요청"| B[이벤트 서버]
    B -->|"2. 연결 수립 (짧은 timeout)"| A
    B -->|"3. 이벤트 스트림 전송"| A
    
    A -->|"4. timeout 발생"| C{재연결 전 동기화}
    C -->|"5. Fetch Sync API 호출"| D[Yabam Core]
    D -->|"6. 누락 데이터 제공"| A
    
    A -->|"7. 재연결 시도"| E{연결 성공?}
    E -->|"8. Yes"| B
    E -->|"9. No (장애 상황)"| F[주기적 Fetch Sync API 호출]
    F -->|"10. 데이터 갱신"| A
    F -->|"11. 주기적으로"| E
```

## 4. 통합 동기화 전략의 이점

### 4.1 단순화된 아키텍처

- **일관된 API**: 재연결 시와 장애 상황에서 모두 동일한 API를 사용하여 코드 복잡성 감소
- **중복 로직 제거**: 동일한 동기화 로직을 재사용하여 코드 유지보수성 향상
- **에러 처리 통합**: 모든 데이터 동기화 상황에서 일관된 에러 처리 방식 적용

### 4.2 강화된 데이터 일관성

- **연결 전 항상 동기화**: 매 연결 시도 전에 동기화를 수행하여 데이터 일관성 보장
- **누락 데이터 방지**: timeout과 재연결 과정에서 발생할 수 있는 데이터 누락 최소화
- **장애 복구 자동화**: 시스템 장애 이후 자동으로 최신 상태로 복구

### 4.3 시스템 안정성 확보

- **우아한 성능 저하**: 이벤트 서버 장애 시 폴링 모드로 자연스럽게 전환
- **리소스 효율화**: 짧은 SSE timeout으로 서버 리소스 관리 최적화
- **중복 요청 방지**: 정확한 lastEventId 추적으로 중복 데이터 요청 방지

### 4.4 사용자 경험 향상

- **끊김 없는 서비스**: 연결 문제나 장애 상황에서도 지속적인 데이터 업데이트
- **백그라운드 복구**: 사용자 인지 없이 자동으로 SSE 연결 복구 시도
- **네트워크 조건 적응**: 불안정한 네트워크 환경에서도 안정적인 서비스 제공

## 5. 설계 원칙 요약

``` mermaid
flowchart TD
    subgraph "야밤 이벤트 시스템 장애 허용 설계"
        A[짧은 SSE timeout] -->|"리소스 효율화"| F[시스템 안정성]
        B[연결 전 항상 동기화] -->|"데이터 일관성"| F
        C[통합 Fetch Sync API] -->|"코드 단순화"| F
        D[자동 fallback 모드] -->|"고가용성"| F
        E[주기적 재연결 시도] -->|"자가 복구"| F
    end
```

## 6. 모듈 의존도

```mermaid
graph LR

%% 도메인 계층을 중앙에 배치
    subgraph "도메인 계층 (domain-event:store-event)"
        A[StoreOrderEvent]
        E[SseEventHandler]
        E --- A
    end

%% 인프라 계층을 위쪽에 배치
    subgraph "인프라 계층 (infra:mq:kafka-pos)"
        F[KafkaStoreOrderEventListener]
        G[Kafka]
        D[StoreOrderProducer]
    end

%% 애플리케이션 계층을 아래쪽에 배치
    subgraph "애플리케이션 계층 (yabam-event)"
        H[SsePosController]
        I[SsePosService]
        J[SseChannel Interface]
        K[OwnerStoreChannel]
        P[SseEmitter]
        J --- K
    end

%% Yabam Core 애플리케이션 계층 추가
    subgraph "pos 도메인  계층 (domain:domain-pos)"
        C[OrderEventProducer]
        L[OrderService]
        RDB[RDB]
    end

    L -->|persistence| RDB
    L -->|Log Produce| C
%% 연결 관계 설정
    D -->|Log Produce| G
    G -->|Log Consume| F
    F -->|Log Proccessing| E
    I -->|채널 선택| J
    H -->|클라이언트 요청| I
    I -->|데이터 push| P
    I -->|concrete| E
%% Yabam Core에서 이벤트 생성 관계 추가
    D -->|concrete| C
```

### 핵심 모듈 구조

- **도메인 계층(domain-event:store-event)**: 주문 이벤트(StoreOrderEvent)의 핵심 도메인 모델과 이벤트 처리기(SseEventHandler)를 포함합니다.
- **인프라 계층(infra:mq:kafka-pos)**: Kafka 메시징 시스템과 이벤트 발행자(StoreOrderProducer), 구독자(KafkaStoreOrderEventListener)를 포함합니다.
- **애플리케이션 계층(yabam-event)**: SSE 컨트롤러, 서비스, 채널 관리 등 클라이언트와의 실시간 통신을 담당합니다.
- **도메인-POS 계층(domain:domain-pos)**: 주문 서비스와 이벤트 생성기(OrderEventProducer)를 포함합니다.

### 핵심 데이터 흐름

1. **이벤트 생성과 발행**: OrderService → OrderEventProducer → StoreOrderProducer → Kafka
2. **이벤트 소비와 처리**: Kafka → KafkaStoreOrderEventListener → SseEventHandler
3. **클라이언트 데이터 전송**: SsePosController → SsePosService → SseChannel → 클라이언트
