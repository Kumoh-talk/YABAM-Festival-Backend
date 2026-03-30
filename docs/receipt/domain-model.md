# 영수증 (Receipt) 도메인 요구사항

> **참고**: 이 문서는 구현 코드 기반의 실제 요구사항입니다.
> 도메인 규칙 변경 시 반드시 이 문서를 함께 수정하세요.

---

## 1. 도메인 개요

**Receipt(영수증)**는 고객이 테이블을 점유한 시점부터 정산 완료까지의 모든 주문 내역을 묶는 단위입니다. QR 코드로 테이블에 접근하면 영수증이 자동 발급되며, 점주가 정산을 완료하면 테이블 점유가 해제됩니다.

- **관련 도메인**: Table, Sale, Order
- **핵심 패키지**: `domain.pos.receipt`

---

## 2. 용어 정의

| 한글 | 영어 | 의미 |
|------|------|------|
| 영수증 | Receipt | 테이블 점유 시작 ~ 정산 완료까지의 주문 단위 |
| 미정산 영수증 | Non-Adjusted Receipt | 아직 정산되지 않은 영수증 |
| 정산 | Adjust | 점주가 영수증의 결제를 완료 처리하는 행위 |
| 고객 | Customer | 영수증(테이블)을 점유한 사용자 |
| 테이블 점유 | Table Occupy | 고객이 테이블에 착석한 상태 |

---

## 3. 핵심 엔티티 및 속성

### Receipt

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| receiptId | UUID | - | 식별자 (UUID, QR에 포함) |
| saleId | Long | ✅ | 소속 영업 세션 |
| tableId | UUID | ✅ | 점유 테이블 |
| customerId | Long | - | 고객 ID (로그인 시) |
| isAdjusted | Boolean | - | 정산 완료 여부 |
| usageStopped | Boolean | - | 점유 시간 정지 여부 |
| createdAt | LocalDateTime | - | 발급 시각 |
| adjustedAt | LocalDateTime | - | 정산 시각 |

---

## 4. 비즈니스 규칙

### 영수증 발급 (QR 흐름)
1. 고객이 테이블 QR 코드 스캔
2. `GET /api/v1/table/{tableId}/receipts/non-adjust` 로 미정산 영수증 조회
   - 미정산 영수증 있으면 → 해당 receiptId 반환
   - 없으면 → `POST /api/v1/receipts` 로 새 영수증 발급
3. 영수증 발급 시 **테이블 활성화** (`isActive = true`)
4. 영수증 UUID가 주문 페이지 URL에 포함됨 → 정산 후 해당 URL은 유효하지 않음

### 영수증 정산 흐름
1. `PATCH /api/v1/receipts/stop` → 테이블 점유 시간 정지 (정산 전 시간 확정)
2. `PATCH /api/v1/receipts/adjust` → 정산 처리 (isAdjusted = true, 테이블 비활성화)
3. 점유 시간 재시작: `PATCH /api/v1/receipts/re-start` (정산 전 시간 재개 가능)
- **시간이 정지된 영수증만 정산 가능**

### 영업 마감 제약
- 미정산 영수증이 존재하면 **영업 마감 불가**

### 영수증 삭제
- 점주 본인 가게의 영수증만 삭제 가능
- 미정산 상태 삭제 시 → 테이블 비활성화

### 영수증 테이블 이동
- 점주가 영수증을 다른 테이블로 이동 가능
- 이전 테이블: 비활성화 / 새 테이블: 활성화

### 조회 규칙
- 영수증 상세(주문 포함): 인증 불필요
- 전체 테이블 미정산 영수증 목록: 점주 권한, saleId 기준
- 정산된 영수증 페이지 조회: 점주 권한, saleId 기준, 페이지네이션
- 고객 영수증 이력: `ROLE_USER` 권한, 커서 기반

---

## 5. API 명세

| Method | URI | 권한 | 설명 |
|--------|-----|------|------|
| `POST` | `/api/v1/receipts?storeId={id}&tableId={id}` | 없음 | 영수증 발급 |
| `GET` | `/api/v1/receipts/{receiptId}` | 없음 | 영수증 상세 + 주문 조회 |
| `GET` | `/api/v1/table/{tableId}/receipts/non-adjust` | 없음 | 테이블 미정산 영수증 ID 조회 |
| `GET` | `/api/v1/sales/{saleId}/non-adjust-receipts` | `ROLE_OWNER` | 영업 전체 테이블 미정산 영수증 목록 |
| `GET` | `/api/v1/sales/{saleId}/receipts` | `ROLE_OWNER` | 영업별 정산 영수증 페이지 조회 |
| `PATCH` | `/api/v1/receipts/stop?receiptIds={ids}` | `ROLE_OWNER` | 점유 시간 정지 (복수) |
| `PATCH` | `/api/v1/receipts/re-start?receiptIds={ids}` | `ROLE_OWNER` | 점유 시간 재시작 (복수) |
| `PATCH` | `/api/v1/receipts/adjust?receiptIds={ids}` | `ROLE_OWNER` | 정산 처리 (복수) |
| `DELETE` | `/api/v1/receipts/{receiptId}` | `ROLE_OWNER` | 영수증 삭제 |
| `PATCH` | `/api/v1/receipt/table?receiptId={id}&tableId={id}` | `ROLE_OWNER` | 테이블 이동 |
| `GET` | `/api/v1/customers/{customerId}/receipts` | `ROLE_USER` | 고객 영수증 이력 조회 |

---

## 6. 연관 관계

```
Sale (1) ──── (*) Receipt
Table (1) ──── (0..1) Receipt [미정산]
Receipt (1) ──── (*) Order
```
