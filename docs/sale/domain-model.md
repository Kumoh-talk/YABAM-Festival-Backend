# 영업 (Sale) 도메인 요구사항

> **참고**: 이 문서는 구현 코드 기반의 실제 요구사항입니다.
> 도메인 규칙 변경 시 반드시 이 문서를 함께 수정하세요.

---

## 1. 도메인 개요

**Sale(영업)**은 가게의 영업 세션 단위입니다. 영업이 시작되면 고객은 주문을 할 수 있고, 영업이 마감되면 더 이상 주문을 받지 않습니다. 모든 Receipt, Order, Call은 특정 Sale에 귀속됩니다.

- **관련 도메인**: Store, Receipt, Order, Call
- **핵심 패키지**: `domain.pos.store` (SaleService, SaleReader, SaleWriter, SaleValidator)

---

## 2. 용어 정의

| 한글 | 영어 | 의미 |
|------|------|------|
| 영업 | Sale | 가게의 활성 운영 세션 |
| 영업 시작 | Open Sale | 가게가 영업을 시작한 상태 |
| 영업 마감 | Close Sale | 가게가 영업을 종료한 상태 |

---

## 3. 핵심 엔티티 및 속성

### Sale

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| saleId | Long | - | 식별자 |
| storeId | Long | ✅ | 소속 가게 |
| openedAt | LocalDateTime | ✅ | 영업 시작 시각 |
| closedAt | LocalDateTime | - | 영업 마감 시각 (null = 영업 중) |

---

## 4. 비즈니스 규칙

### 영업 시작
- 점주(`ROLE_OWNER`) 권한 필요
- 해당 가게가 **마감 상태(활성 Sale 없음)**이어야 영업 시작 가능
- 영업 시작 시 새로운 Sale 레코드가 생성됨

### 영업 마감
- 점주(`ROLE_OWNER`) 권한 필요
- 해당 가게가 **오픈 상태(활성 Sale 있음)**이어야 마감 가능
- **미정산(non-adjusted) 영수증이 존재하면 마감 불가**
- 마감 시 Sale의 `closedAt` 기록

### 가게 상태 영향
영업 상태는 다른 도메인의 작업 허용 여부를 결정합니다:

| 작업 | 필요 상태 |
|------|---------|
| Table CRUD | 가게 마감 상태 |
| Menu/Category CRUD | 가게 마감 상태 |
| 메뉴 품절 수정 | 제한 없음 |
| 주문 생성 | 영업 중 |
| 영수증 발급 | 영업 중 |

### 영업 목록 조회
- 점주(`ROLE_OWNER`) 권한 필요 (헤더에 역할 포함, Passport 불필요)
- storeId로 특정 가게의 영업 목록 커서 기반 조회

---

## 5. API 명세

| Method | URI | 권한 | 설명 |
|--------|-----|------|------|
| `POST` | `/api/v1/sale/open?storeId={id}` | `ROLE_OWNER` | 영업 시작 |
| `PATCH` | `/api/v1/sale/close?saleId={id}` | `ROLE_OWNER` | 영업 마감 |
| `GET` | `/api/v1/sales?storeId={id}&lastSaleId={id}&size={n}` | `ROLE_OWNER` | 가게별 영업 목록 커서 조회 |

---

## 6. 연관 관계

```
Store (1) ──── (*) Sale
Sale (1) ──── (*) Receipt
Sale (1) ──── (*) Order (조회 단위)
Sale (1) ──── (*) Call (조회 단위)
```
