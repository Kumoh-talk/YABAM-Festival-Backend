# 결제 (Payment) 도메인 모델

> 이 문서는 구현 코드 기반의 실제 요구사항입니다.
> 도메인 규칙 변경 시 반드시 이 문서를 함께 수정하세요.

---

## 1. 도메인 개요

**Payment(결제)**는 토스페이먼츠 PG를 통해 처리된 온라인 결제 내역입니다. 고객이 결제를 완료하면 영수증이 자동 정산됩니다.

- **외부 연동**: 토스페이먼츠 Core API (`https://api.tosspayments.com`)
- **관련 도메인**: Receipt
- **핵심 패키지**: `domain.pos.payment`
- **인프라 모듈**: `infra/pg/toss-pg`

---

## 2. 핵심 엔티티 및 속성

### Payment

| 필드             | 타입            | 필수 | 설명                              |
|----------------|---------------|----|---------------------------------|
| paymentId      | Long          | -  | 식별자                             |
| receiptId      | UUID          | ✅  | 연결된 영수증                         |
| tossPaymentKey | String        | ✅  | 토스 결제 키 (Toss 발급)               |
| tossOrderId    | String        | ✅  | Toss에 전달한 orderId (= receiptId) |
| amount         | Integer       | ✅  | 결제 금액                           |
| status         | PaymentStatus | ✅  | 현재 결제 상태                        |
| paymentMethod  | String        | -  | 카드, 가상계좌, 간편결제 등                |
| approvedAt     | LocalDateTime | -  | 승인 시각 (가상계좌는 null)              |

### PaymentStatus

| 값                      | 설명                          |
|------------------------|-------------------------------|
| `READY`                | 결제 생성됨 (미완료)              |
| `IN_PROGRESS`          | 결제 진행 중 (선점 레코드)        |
| `WAITING_FOR_DEPOSIT`  | 가상계좌 입금 대기               |
| `DONE`                 | 결제 완료                       |
| `ABORTED`              | 결제 승인 실패 (선점 레코드 오염)  |
| `EXPIRED`              | 결제 만료                       |
| `CANCELED`             | 전액 취소                       |
| `PARTIAL_CANCELED`     | 부분 취소                       |

#### 상태별 데이터 흐름 의미

**`READY`** — 토스 결제 위젯 초기화 시 토스 측에서 발급하는 초기 상태.
결제 위젯을 열기만 하고 결제를 완료하지 않은 경우에 해당한다.
현재 구현에서는 로컬 DB에 READY 레코드를 저장하지 않으며, Toss 대시보드에서만 확인 가능하다.

---

**`IN_PROGRESS`** — `confirmPayment()` 진입 시 `validateAndPreempt()`에서 PG 호출 직전에 INSERT되는 **선점 레코드**.
이 레코드가 DB에 커밋된 순간부터 동일 receiptId로 들어오는 중복 요청은 `ALREADY_PAID_RECEIPT`로 즉시 차단된다.
PG 호출 성공 후 `finalizeAndSettle()`에서 실제 결과(DONE / WAITING_FOR_DEPOSIT)로 업데이트된다.
PG 호출 또는 저장 실패 시 ABORTED로 마킹된다.

> `IN_PROGRESS`가 DB에 남아 있다는 것은 결제 흐름이 진행 중이거나 아직 종료되지 않은 상태를 의미한다.

---

**`WAITING_FOR_DEPOSIT`** — 가상계좌 결제에서 발급이 완료되고 입금을 기다리는 상태.
`confirmPayment()` 흐름의 `finalizeAndSettle()` 단계에서 저장되며, 이 시점에는 영수증 정산이 일어나지 않는다.
이후 입금이 완료되면 토스 웹훅(`PAYMENT_STATUS_CHANGED`)으로 DONE 신호가 수신되고, 그때 영수증 정산과 테이블 비활성화가 처리된다.

---

**`DONE`** — 결제가 최종 완료된 상태.
카드 결제는 `confirmPayment()` 내 `finalizeAndSettle()` 단계에서 바로 이 상태로 저장되며, 즉시 영수증 정산 + 테이블 비활성화가 수행된다.
가상계좌 결제는 입금 완료 웹훅 수신 시 WAITING_FOR_DEPOSIT → DONE 으로 전환되고, 그때 정산 처리가 이루어진다.
DONE 상태에 도달한 이후에는 영수증이 이미 정산된 상태이므로 동일 영수증에 대한 재결제가 불가능하다.

---

**`ABORTED`** — `confirmPayment()` 흐름 중 오류가 발생했을 때 선점 레코드에 마킹되는 상태.
두 가지 경로로 발생한다:
① PG 호출(`tossPaymentPort.confirm()`) 자체가 실패한 경우
② PG 호출은 성공했으나 `finalizeAndSettle()` 저장·정산 단계에서 예외가 발생한 경우 (이 경우 DONE이라면 보상 취소도 시도)

ABORTED 상태의 레코드가 있는 영수증은 다음 `confirmPayment()` 호출 시 해당 레코드를 삭제하고 재시도가 허용된다.

> `unique` 제약(receipt_id)이 있기 때문에 삭제 후 재INSERT 방식으로 처리한다.

---

**`EXPIRED`** — 가상계좌 입금 기한이 초과되는 등 토스 측에서 결제를 만료 처리한 상태.
웹훅으로 수신되며, 현재 구현에서는 `processWebhook()` 내에서 처리 대상이 아닌 상태로 분류되어 로그만 남긴다.
필요 시 별도 처리 로직을 추가할 수 있다.

---

**`CANCELED`** — 전액 취소가 완료된 상태.
점주가 `cancelPayment()`를 호출하고 `cancelAmount`를 지정하지 않은 경우, 토스 취소 API 응답 후 이 상태로 업데이트된다.
또는 `confirmPayment()` 내 저장·정산 단계 실패 시 보상 취소가 성공하면 토스 측도 CANCELED 처리된다 (로컬 레코드는 ABORTED로 마킹).

---

**`PARTIAL_CANCELED`** — 부분 취소가 완료된 상태.
점주가 `cancelPayment()`를 호출하고 `cancelAmount`를 지정한 경우, 토스 취소 API 응답 후 이 상태로 업데이트된다.
웹훅으로도 수신 가능하며, `processWebhook()`에서 상태 동기화 처리된다.

---

## 3. 비즈니스 규칙

### 결제 승인 (confirmPayment)

- `paymentKey`, `orderId`(= receiptId UUID), `amount` 필수
- `amount`가 영수증 `occupancyFee`와 일치해야 함 (occupancyFee가 설정된 경우)
- 이미 Payment가 존재하는 영수증은 재결제 불가 (ABORTED 제외)
- 이미 정산된 영수증은 결제 불가
- **트랜잭션 분리** — DB 커넥션 점유 최소화 + 동시성 제어를 위해 3단계로 분리:
  1. **검증·잠금·선점 레코드 저장** (`@Transactional`) — 영수증 행을 `PESSIMISTIC_WRITE`로 잠금, 검증 후 `IN_PROGRESS` 선점 레코드를 INSERT하고 커밋. 이후 중복 요청은 선점 레코드를 감지해 즉시 차단됨.
  2. **외부 API 호출** (트랜잭션 없음) — 토스페이먼츠 confirm 호출
  3. **최종 저장·정산** (`@Transactional REQUIRES_NEW`) — 선점 레코드를 실제 결과로 UPDATE, 영수증 정산, 테이블 비활성화
- **카드 결제**: DONE 반환 → 즉시 영수증 자동 정산 + 테이블 비활성화
- **가상계좌 결제**: WAITING_FOR_DEPOSIT 반환 → Payment 저장만, 정산은 입금 완료 웹훅 후 처리
- **ABORTED 재시도**: ABORTED 상태의 선점 레코드가 있는 경우 삭제 후 재선점 허용
- **보상 취소 (Compensation)**: 저장·정산 단계에서 예외 발생 시 DONE 상태인 경우 Toss에 자동 취소 요청
  - 보상 취소도 실패할 경우 에러 로그 기록 후 수동 처리 필요
- **멱등성**: Idempotency-Key = orderId 헤더로 Toss에 전달 (중복 요청 방지)

### 타임아웃 처리

- RestClient Read timeout: **30초**, Connect timeout: **5초**
- 타임아웃(`ResourceAccessException`) 발생 시:
  1. `getPayment(paymentKey)` 로 현재 결제 상태 재확인
  2. DONE 또는 WAITING_FOR_DEPOSIT → 해당 결과로 정상 처리
  3. 그 외 (ABORTED 등) 또는 조회 실패 → `PAYMENT_CONFIRM_FAILED` 예외 반환

### ALREADY_PROCESSED_PAYMENT 처리

- 사전 검증 단계에서 로컬 Payment 미존재를 확인했음에도 Toss가 ALREADY_PROCESSED_PAYMENT 반환 시
  → 이전 요청의 응답 누락(타임아웃 등)으로 판단하여 `getPayment(paymentKey)` 로 조회 후 결과 반환
- `TossPaymentAdapter` 레이어에서 처리하며 도메인 레이어로 전파하지 않음

### 결제 취소 (cancelPayment)

- 점주 권한 필요, 본인 가게 영수증만 취소 가능
- `cancelAmount` null → 전액 취소, 값 지정 → 부분 취소
- 이미 취소된 결제는 재취소 불가
- 토스페이먼츠 취소 API 호출 → Payment 상태 업데이트

### 웹훅 처리 (processWebhook)

- HMAC-SHA256 서명 검증 후 처리 (TossPayments-Signature 헤더)
- `CANCELED`, `PARTIAL_CANCELED` → 결제 상태 동기화
- `WAITING_FOR_DEPOSIT → DONE` → 영수증 자동 정산 + 테이블 비활성화 (중복 정산 방지)

---

## 4. API 명세

| Method | URI                                     | 권한           | 설명                |
|--------|-----------------------------------------|--------------|-------------------|
| `POST` | `/api/v1/payments/toss/confirm`         | 없음           | 결제 승인 + 영수증 자동 정산 |
| `POST` | `/api/v1/payments/{paymentKey}/cancel`  | `ROLE_OWNER` | 결제 취소             |
| `GET`  | `/api/v1/payments/receipts/{receiptId}` | 없음           | 영수증 결제 정보 조회      |
| `POST` | `/api/v1/payments/toss/webhook`         | 없음 (서명 검증)   | 토스페이먼츠 웹훅 처리      |
| `GET`  | `/api/v1/payments/{paymentKey}/toss`    | `ROLE_OWNER` | 토스 실시간 결제 상태 조회   |
| `GET`  | `/api/v1/payments/sales/{saleId}`       | `ROLE_OWNER` | 영업별 결제 목록 조회      |

### POST /api/v1/payments/toss/confirm 요청

```json
{
  "paymentKey": "toss_발급_결제키",
  "orderId": "receiptId(UUID)",
  "amount": 8000
}
```

### POST /api/v1/payments/{paymentKey}/cancel 요청

```json
{
  "cancelReason": "고객 요청에 의한 취소",
  "cancelAmount": 4000
}
```

> `cancelAmount` 생략 시 전액 취소

---

## 5. 인프라 구조

### 토스페이먼츠 API 인증

- `Authorization: Basic {base64(secretKey:)}`
- 테스트 키: `test_sk_...`, 운영 키: `live_sk_...` (cloud-config-private 관리)

### 모듈 구조

```
infra/pg/toss-pg/
  com/pg/toss/
    config/   TossPaymentProperties, TossPaymentConfig (RestClient Bean)
    client/   TossPaymentClient (실제 HTTP 호출 + 타임아웃·에러 처리)
              AlreadyProcessedAtTossException (모듈 내부 예외)
    adapter/  TossPaymentAdapter (TossPaymentPort 구현 + ALREADY_PROCESSED 복구)

domain/domain-pos/
  domain/pos/payment/
    implement/  PaymentProcessor (트랜잭션 분리된 검증·저장·정산)
                PaymentReader, PaymentWriter
    service/    PaymentService (결제 승인·취소·조회 흐름 오케스트레이션)
```

### confirmPayment 실행 흐름

```
PaymentService.confirmPayment()  ← @Transactional 없음
  │
  ├─ [1] PaymentProcessor.validateAndPreempt()  ← @Transactional
  │         영수증 행 PESSIMISTIC_WRITE 잠금
  │         정산 여부, 중복 결제, 금액 검증
  │         IN_PROGRESS 선점 레코드 INSERT → COMMIT (잠금 해제)
  │         └─ 이후 중복 요청은 선점 레코드 감지로 차단됨
  │
  ├─ [2] tossPaymentPort.confirm()              ← 트랜잭션 없음 (최대 30초)
  │         타임아웃 → getPayment()로 상태 재확인
  │         ALREADY_PROCESSED → getPayment()로 복구
  │         실패 시 → 선점 레코드 ABORTED 마킹
  │
  └─ [3] PaymentProcessor.finalizeAndSettle()   ← @Transactional(REQUIRES_NEW)
            선점 레코드 → 실제 결과로 UPDATE
            DONE: 테이블 비활성화 + 영수증 정산
            실패 시 → tossPaymentPort.cancel() 보상 취소 + ABORTED 마킹
```

---

## 6. 연관 관계

```
Receipt (1) ──── (0..1) Payment
```
