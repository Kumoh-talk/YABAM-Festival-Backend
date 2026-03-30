# 직원 호출 (Call) 도메인 요구사항

> **참고**: 이 문서는 구현 코드 기반의 실제 요구사항입니다.
> 도메인 규칙 변경 시 반드시 이 문서를 함께 수정하세요.

---

## 1. 도메인 개요

**Call(콜)**은 고객이 점유 중인 테이블에서 점주(직원)를 호출하는 기능입니다. 고객이 호출을 생성하면 점주는 PoS 화면에서 미완료 호출 목록을 조회하고 완료 처리합니다.

- **관련 도메인**: Receipt, Sale
- **핵심 패키지**: `domain.pos.call`

---

## 2. 용어 정의

| 한글 | 영어 | 의미 |
|------|------|------|
| 호출 | Call | 고객의 직원 호출 요청 |
| 호출 메시지 | CallMessage | 호출 시 전달하는 메시지 내용 |
| 미완료 호출 | Non-Complete Call | 아직 점주가 처리하지 않은 호출 |

---

## 3. 핵심 엔티티 및 속성

### Call

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| callId | Long | - | 식별자 |
| receiptId | UUID | ✅ | 소속 영수증 (점유 테이블 식별) |
| saleId | Long | ✅ | 소속 영업 세션 |
| callMessage | String | ✅ | 호출 메시지 |
| isCompleted | Boolean | - | 완료 여부 (false = 미처리) |
| createdAt | LocalDateTime | - | 호출 시각 |

---

## 4. 비즈니스 규칙

### 호출 생성
- 인증 불필요 (고객 비로그인 가능)
- 호출 가능 조건:
  - **점유 중인 활성화된 테이블**이어야 함
  - **미정산 영수증**이 존재해야 함
- 요청 바디: `receiptId`, `callMessage`

### 호출 완료 처리
- **점주(`ROLE_OWNER`) 권한** 필요
- `callId`로 특정 호출을 완료 처리 (`isCompleted = true`)
- 본인 가게의 호출만 완료 처리 가능 (UserPassport 검증)

### 미완료 호출 조회
- **점주(`ROLE_OWNER`) 권한** 필요
- `saleId`로 특정 영업의 미완료 호출만 조회
- 커서 기반 페이지네이션 (`lastCallId`, `size`)

---

## 5. API 명세

| Method | URI | 권한 | 설명 |
|--------|-----|------|------|
| `POST` | `/api/v1/call` | 없음 | 직원 호출 생성 |
| `PATCH` | `/api/v1/call/complete?callId={id}` | `ROLE_OWNER` | 호출 완료 처리 |
| `GET` | `/api/v1/calls?saleId={id}&lastCallId={id}&size={n}` | `ROLE_OWNER` | 미완료 호출 목록 커서 조회 |

### POST /api/v1/call 요청 바디
```json
{
  "receiptId": "uuid",
  "callMessage": "직원 호출합니다"
}
```

---

## 6. 연관 관계

```
Receipt (1) ──── (*) Call
Sale (1) ──── (*) Call [조회 단위]
```
