# 주문 (Order) 도메인 요구사항

> **참고**: 이 문서는 구현 코드 기반의 실제 요구사항입니다.
> 도메인 규칙 변경 시 반드시 이 문서를 함께 수정하세요.

---

## 1. 도메인 개요

**주문 도메인**은 장바구니(Cart), 주문(Order), 주문메뉴(OrderMenu) 세 개념으로 구성됩니다.

- **Cart**: 고객이 주문 전 메뉴를 담는 임시 저장소 (Redis 기반)
- **Order**: 영수증에 귀속된 주문 단위
- **OrderMenu**: 주문 내 개별 메뉴 항목

주문 생성에는 **동시성 제어를 위한 주문 세션(Order Session)** 토큰이 필요합니다.

- **관련 도메인**: Receipt, Menu, Sale
- **핵심 패키지**: `domain.pos.cart`, `domain.pos.order`

---

## 2. 용어 정의

| 한글 | 영어 | 의미 |
|------|------|------|
| 장바구니 | Cart | 주문 전 메뉴를 담는 임시 저장소 |
| 주문 세션 | Order Session | 장바구니 → 주문 전환 시 필요한 락 토큰 |
| 주문 | Order | 영수증에 귀속된 주문 단위 |
| 직접 주문 | Direct Order | 점주가 메뉴를 직접 지정해 생성하는 주문 |
| 커스텀 주문 | Custom Order | 메뉴 없이 금액과 설명만으로 만드는 특수 주문 |
| 주문 메뉴 | OrderMenu | 주문 내 개별 메뉴 항목 |

---

## 3. 핵심 엔티티 및 속성

### Cart (Redis 저장)

| 필드 | 타입 | 설명 |
|------|------|------|
| receiptId | UUID | 영수증 식별자 (Key) |
| menuId | Long | 메뉴 식별자 |
| quantity | Integer | 수량 (최소 1) |

### Order

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| orderId | Long | - | 식별자 |
| receiptId | UUID | ✅ | 소속 영수증 |
| orderStatus | OrderStatus | ✅ | 주문 상태 |
| totalPrice | Integer | - | 총 금액 |
| orderMenus | List\<OrderMenu\> | - | 주문 메뉴 목록 |

### OrderMenu

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| orderMenuId | Long | - | 식별자 |
| orderId | Long | ✅ | 소속 주문 |
| menuId | Long | - | 메뉴 ID (커스텀 주문은 null) |
| menuName | String | ✅ | 메뉴 이름 |
| quantity | Integer | ✅ | 주문 수량 |
| completedCount | Integer | - | 조리 완료 수량 |
| orderMenuStatus | OrderMenuStatus | ✅ | 메뉴 처리 상태 |

---

## 4. 비즈니스 규칙

### 장바구니 (Cart)

- 메뉴 추가(Upsert): `receiptId + menuId`로 추가 또는 수량 덮어쓰기
- 메뉴 삭제: `receiptId + menuId`로 특정 메뉴 삭제
- 조회: `receiptId`로 장바구니 전체 조회 (없으면 빈 Cart 반환)
- 인증 불필요

### 주문 세션 (Order Session) — 동시성 제어

고객이 장바구니 → 주문을 시도할 때 **중복 주문 방지**를 위해 세션 토큰이 필요합니다.

1. `POST /api/v1/receipts/{receiptId}/order-session` → 세션 토큰(UUID) 발급
   - 영수증 단위 락: 동시에 하나의 세션만 존재
2. 발급된 토큰을 `X-Order-Session-Token` 헤더에 담아 주문 생성
3. 주문 완료 후 세션 자동 해제 또는 `DELETE /api/v1/receipts/{receiptId}/order-session`으로 취소

### 주문 생성

| 방식 | 엔드포인트 | 권한 | 설명 |
|------|-----------|------|------|
| 장바구니 기반 주문 | `POST /api/v1/receipts/{receiptId}/orders/with-cart` | 없음 | `X-Order-Session-Token` 헤더 필요 |
| 직접 주문 (수동) | `POST /api/v1/receipts/{receiptId}/orders/direct` | `ROLE_OWNER` | 메뉴 ID + 수량 직접 지정 |
| 커스텀 주문 | `POST /api/v1/receipts/{receiptId}/orders/custom` | `ROLE_OWNER` | 금액 + 설명만 |

### 주문 상태 전이 (OrderStatus)

```
ORDERED(주문) ──[점주: 접수]──► RECEIVED(접수)
ORDERED(주문) ──[점주/고객: 취소]──► CANCELED(취소)
RECEIVED(접수) ──[점주: 취소]──► CANCELED(취소)
CANCELED(취소) ──[점주: 재접수]──► RECEIVED(접수)
COMPLETED(완료) ──[점주: 재접수]──► RECEIVED(접수)
COMPLETED(완료) ──[점주: 취소]──► CANCELED(취소)
```

**상태 전이 규칙:**
- `ORDERED → RECEIVED` (접수): **점주만** 가능
- `ORDERED → CANCELED` (취소): 점주 및 고객 모두 가능
- `RECEIVED → CANCELED` (취소): **점주만** 가능
- `CANCELED → RECEIVED` (재접수): **점주만** 가능
- `COMPLETED → RECEIVED / CANCELED`: **점주만** 가능
- 주문이 `RECEIVED`로 변경되면 → 모든 OrderMenu가 `COOKING` 상태로 자동 전환

### 주문 메뉴 상태 전이 (OrderMenuStatus)

```
ORDERED(주문) ──[점주]──► COOKING(조리중)
ORDERED(주문) ──[점주/고객]──► CANCELED(취소)
ORDERED(주문) ──[점주]──► COMPLETED(완료)
COOKING(조리중) ──[점주]──► CANCELED(취소)
COOKING(조리중) ──[점주]──► COMPLETED(완료)
CANCELED(취소) ──[점주]──► COOKING(조리중)
COMPLETED(완료) ──[점주]──► COOKING(조리중)
```

**자동 상태 전이:**
- 모든 OrderMenu가 `COMPLETED` 또는 `CANCELED` 상태가 되면 → Order 상태 자동 `COMPLETED`

### 주문 삭제 / 커스텀 주문 수정
- 점주만 가능
- 주문 삭제: `DELETE /api/v1/orders/{orderId}`
- 커스텀 주문 수정: `PATCH /api/v1/orders/custom/{orderId}`

### 주문 메뉴 추가/삭제/수량 수정
- 점주만 가능 (`ROLE_OWNER`)
- 주문 메뉴 추가: `POST /api/v1/orders/{orderId}/order-menus`
- 주문 메뉴 삭제: `DELETE /api/v1/order-menus/{orderMenuId}`
- 수량 수정: `PATCH /api/v1/order-menus/{orderMenuId}/quantity`

### 조리 완료 수량 수정
- 점주만 가능, 최소 0 이상
- `PATCH /api/v1/order-menus/{orderMenuId}/completed-count`

---

## 5. API 명세

### 장바구니 & 주문 세션

| Method | URI | 권한 | 설명 |
|--------|-----|------|------|
| `POST` | `/api/v1/cart?receiptId={id}&menuId={id}&quantity={n}` | 없음 | 장바구니 메뉴 추가/수량 수정 |
| `DELETE` | `/api/v1/cart/menu?receiptId={id}&menuId={id}` | 없음 | 장바구니 메뉴 삭제 |
| `GET` | `/api/v1/cart?receiptId={id}` | 없음 | 장바구니 조회 |
| `POST` | `/api/v1/receipts/{receiptId}/order-session` | 없음 | 주문 세션 진입 (토큰 발급) |
| `DELETE` | `/api/v1/receipts/{receiptId}/order-session` | 없음 | 주문 세션 취소 (`X-Order-Session-Token` 필요) |

### 주문

| Method | URI | 권한 | 설명 |
|--------|-----|------|------|
| `POST` | `/api/v1/receipts/{receiptId}/orders/with-cart` | 없음 | 장바구니 기반 주문 생성 (`X-Order-Session-Token` 필요) |
| `POST` | `/api/v1/receipts/{receiptId}/orders/direct` | `ROLE_OWNER` | 직접(수동) 주문 생성 |
| `POST` | `/api/v1/receipts/{receiptId}/orders/custom` | `ROLE_OWNER` | 커스텀 주문 생성 |
| `PATCH` | `/api/v1/orders/custom/{orderId}` | `ROLE_OWNER` | 커스텀 주문 수정 |
| `DELETE` | `/api/v1/orders/{orderId}` | `ROLE_OWNER` | 주문 삭제 |
| `PATCH` | `/api/v1/orders/{orderId}/status?orderStatus={status}` | 없음 | 주문 상태 변경 |
| `GET` | `/api/v1/receipts/{receiptId}/orders` | 없음 | 영수증별 주문 목록 조회 |
| `GET` | `/api/v1/orders/{orderId}` | 없음 | 주문 상세 조회 |
| `GET` | `/api/v1/sales/{saleId}/orders?orderStatuses={}&pageSize={n}&lastOrderId={id}` | `ROLE_OWNER` | 영업별 주문 슬라이스 조회 (PoS 화면) |

### 주문 메뉴

| Method | URI | 권한 | 설명 |
|--------|-----|------|------|
| `PATCH` | `/api/v1/order-menus/{orderMenuId}/status?orderMenuStatus={status}` | 없음 | 주문 메뉴 상태 변경 |
| `POST` | `/api/v1/orders/{orderId}/order-menus` | `ROLE_OWNER` | 주문 메뉴 추가 |
| `DELETE` | `/api/v1/order-menus/{orderMenuId}` | `ROLE_OWNER` | 주문 메뉴 삭제 |
| `PATCH` | `/api/v1/order-menus/{orderMenuId}/quantity?patchQuantity={n}` | `ROLE_OWNER` | 주문 메뉴 수량 수정 |
| `PATCH` | `/api/v1/order-menus/{orderMenuId}/completed-count?patchCompletedCount={n}` | `ROLE_OWNER` | 조리 완료 수량 수정 |

---

## 6. 연관 관계

```
Receipt (1) ──── (*) Order
Order (1) ──── (*) OrderMenu
Menu (1) ──── (*) OrderMenu [일반 주문]
```

## 7. 주문 세션 흐름 (동시성 제어)

```
[고객]
  1. POST /cart (메뉴 담기)
  2. POST /receipts/{receiptId}/order-session → sessionToken 반환
  3. POST /receipts/{receiptId}/orders/with-cart
     Header: X-Order-Session-Token: {sessionToken}
  4. (세션 자동 해제 or DELETE /order-session)
```
