---
description: 4가지 아키텍처(RDB Single / Redis Cache / SSE Pub/Sub / RDB Replication)에 대한 읽기·쓰기 혼합 성능 테스트(k6) 수행 가이드
---

# 성능 테스트 가이드 (Performance Testing)

Yabam POS 주문 API를 기준으로 4가지 아키텍처를 **읽기(폴링) + 쓰기(상태 변경) 동시 실행** 조건에서 비교 측정한다.

---

## 비교 아키텍처 개요

| 번호 | 아키텍처 | 읽기 경로 | 쓰기 경로 | 전제 조건 |
|------|----------|-----------|-----------|-----------|
| 1 | **RDB Single** | MySQL 직접 조회 (`bypassCache=true`) | MySQL 트랜잭션 | 기본 docker-compose-test.yml |
| 2 | **Redis ZSET Cache** | Redis ZSET → DB Fallback | MySQL + Write-Through (affectedStatuses + Lua Fence) | 기본 docker-compose-test.yml |
| 3 | **SSE + Redis Pub/Sub** | SSE 구독 (push) | MySQL → Kafka → yabam-event → SSE 푸시 | yabam-event 앱 기동 필요 |
| 4 | **RDB + Replication** | MySQL Replica 읽기 | MySQL Master 쓰기 | docker-compose-replication.yml |

---

## 1. 환경 구성

### 1-1. 기본 환경 (시나리오 1, 2 공통)

```bash
# Docker 컨테이너 기동 (MySQL:3315, Redis:6380)
docker-compose -f docker-compose-test.yml up -d

# 테스트 데이터 삽입 (30,000건 주문, 5개 매장)
docker exec -i yabam-mysql-test mysql -uroot -p1234 local_mydb < ./scripts/full_init_data_v3.sql

# Spring Boot 애플리케이션 기동
./gradlew :application:yabam:yabam-core:bootRun --args='--spring.profiles.active=local'
# 또는 JAR 실행: java -jar application/yabam/yabam-core/build/libs/yabam-core-*.jar
```

### 1-2. SSE + Redis Pub/Sub (시나리오 3 추가)

```bash
# yabam-event 앱을 별도 포트(예: 8012)에서 기동
./gradlew :application:yabam:yabam-event:bootRun \
  --args='--spring.profiles.active=local --server.port=8012'
```

> yabam-event는 Kafka Consumer를 사용한다. 로컬 환경에서는 Kafka 없이 `DummySseEventHandler`
> (yabam-core)로 대체 동작한다. 실제 SSE 푸시 테스트는 Kafka 기동이 필요하다.

### 1-3. MySQL Replication (시나리오 4 추가)

```bash
# 기존 단일 MySQL 중지
docker-compose -f docker-compose-test.yml down

# Master(3315) + Replica(3316) 기동
docker-compose -f docker-compose-replication.yml up -d

# 레플리케이션 초기화 (최초 1회)
bash scripts/init-replication.sh

# 테스트 데이터를 Master에 삽입 (Replica로 자동 복제)
docker exec -i yabam-mysql-master mysql -uroot -p1234 local_mydb < ./scripts/full_init_data_v3.sql

# Replica DataSource 설정된 Spring 프로파일로 기동
./gradlew :application:yabam:yabam-core:bootRun \
  --args='--spring.profiles.active=local,replication'
```

---

## 2. 데이터 구조 (full_init_data_v3.sql 기준)

| 항목 | 내용 |
|------|------|
| 매장 수 | 5개 (`storeId` / `saleId` = 1~5) |
| 매장당 주문 | 6,000건 (총 30,000건) |
| ORDERED | `orderId = (N-1)*6000 + k*10`, k=1..600 (매장당 600건) |
| RECEIVED | `orderId = (N-1)*6000 + k*10 - 9`, k=1..600 (매장당 600건) |
| COMPLETED | 매장당 4,200건 (전체의 70%) |
| CANCELED | 매장당 600건 |

---

## 3. 테스트 실행

### 3-1. 전체 4-way 비교 테스트

```bash
# Redis 캐시 초기화 (cache 시나리오 워밍업 전 필수)
docker exec yabam-redis-test redis-cli FLUSHALL

# k6 실행 (전체 4가지 시나리오, 약 3분 소요)
./k6.exe run ./scripts/pos_order_4way_comparison_v5.js --out json=k6-report-v5.json > k6-report-v5.txt

# SSE 시나리오 포함 시 (yabam-event URL 지정)
./k6.exe run ./scripts/pos_order_4way_comparison_v5.js \
  --env SSE_BASE_URL=http://localhost:8012 \
  --out json=k6-report-v5.json > k6-report-v5.txt

# MySQL Replication 시나리오 포함 시
./k6.exe run ./scripts/pos_order_4way_comparison_v5.js \
  --env REPL_BASE_URL=http://localhost:8011 \
  --out json=k6-report-v5.json > k6-report-v5.txt
```

### 3-2. 개별 시나리오 단독 실행 (선택)

```bash
# 기존 3-way 비교 테스트 (no_cache / cache_read_only / cache_mixed_write)
./k6.exe run ./scripts/pos_order_realistic_mixed_v4.js > k6-report-v4.txt
```

---

## 4. 시나리오 설계

### 읽기 VU (전체의 70%)

| 구성 | 비율 | API | 주기 |
|------|------|-----|------|
| 주방 뷰 | 67% | `GET /api/v1/sales/{saleId}/orders?orderStatuses=ORDERED,RECEIVED&pageSize=20` | 1초 |
| 관리자 뷰 | 33% | `GET /api/v1/sales/{saleId}/orders?orderStatuses=ORDERED,RECEIVED,COMPLETED&pageSize=30` | 2초 |

### 쓰기 VU (전체의 30%)

| 구성 | 비율 | API | 설명 |
|------|------|-----|------|
| 접수 처리 | 50% | `PATCH /api/v1/orders/{orderId}/status?orderStatus=RECEIVED` | ORDERED→RECEIVED |
| 완료 처리 | 50% | `PATCH /api/v1/orders/{orderId}/status?orderStatus=COMPLETED` | RECEIVED→COMPLETED |

> 400/422 응답은 이미 상태 변경된 주문에 대한 정상 도메인 응답이므로 에러로 집계하지 않는다.

### SSE 시나리오 VU (각 50%)

| 구성 | API | 설명 |
|------|-----|------|
| 구독 VU | `POST /api/v1/owner/subscribe?ownerId=&storeId=` (yabam-event) | SSE 커넥션 수립 시간(TTFB) 측정 |
| 이벤트 푸시 VU | `POST /api/v1/owner/unicast?storeId=` (yabam-event) | unicast 푸시 응답 시간 측정 |

---

## 5. 성능 목표 (SLO)

| 시나리오 | 지표 | 목표 |
|----------|------|------|
| rdb_single 읽기 | p(95) | < 2,000ms |
| cache_zset 읽기 | p(95) | < 100ms |
| sse_pubsub 연결 | p(95) | < 500ms |
| rdb_replication 읽기 | p(95) | < 1,500ms |
| 전체 에러율 | rate | < 5% |
| 예상 외 쓰기 에러율 | write_errors | < 2% |

---

## 6. 결과 분석 가이드

`k6-report-v5.txt`에서 다음 지표를 시나리오별로 비교한다.

```
# 읽기 성능 비교 (p95 기준)
http_req_duration{scenario:rdb_read}
http_req_duration{scenario:cache_read}
http_req_duration{scenario:repl_read}
http_req_duration{scenario:sse_subscribe}

# 쓰기 성능
http_req_duration{scenario:rdb_write}
http_req_duration{scenario:cache_write}
write_req_duration  (커스텀 메트릭)

# SSE 전용
sse_conn_duration   (SSE 커넥션 수립 시간)
sse_errors          (연결 실패율)

# 에러율
http_req_failed
write_errors
```

### 예상 결과 패턴

| 지표 | RDB Single | Cache ZSET | RDB Repl | SSE |
|------|-----------|------------|---------|-----|
| 읽기 p(95) | 500~2,000ms | 30~100ms | 300~1,000ms | N/A (push) |
| 쓰기 p(95) | 10~200ms | 10~200ms (+ 캐시 갱신) | 10~200ms | N/A |
| 쓰기→읽기 경쟁 | 높음 | 낮음 (캐시 분리) | 중간 (replica 분산) | N/A |
| 캐시 효과 | 없음 | 8~10x 향상 | 없음 | N/A |

---

## 7. 캐시 워밍업 동작

`pos_order_4way_comparison_v5.js`의 `setup()` 함수가 테스트 시작 전 자동으로 모든 매장(saleId 1~5)의 ZSET을 초기화한다. 별도로 Redis FLUSH를 할 경우 cache_read 그룹은 첫 요청이 DB fallback(캐시 미스)될 수 있으므로, **테스트 직전에 FLUSHALL을 실행하면 setup()이 이를 재워밍업**한다.

```bash
docker exec yabam-redis-test redis-cli FLUSHALL
./k6.exe run ./scripts/pos_order_4way_comparison_v5.js
```
