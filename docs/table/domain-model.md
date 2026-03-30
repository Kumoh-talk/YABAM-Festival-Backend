# 테이블 (Table) 도메인 요구사항

> **참고**: 이 문서는 구현 코드 기반의 실제 요구사항입니다.
> 도메인 규칙 변경 시 반드시 이 문서를 함께 수정하세요.

---

## 1. 도메인 개요

**Table(테이블)**은 가게의 물리적 좌석 단위입니다. 고객이 QR 코드로 테이블을 점유하면 영수증이 발급되고, 테이블의 활성화 상태가 전환됩니다.

- **관련 도메인**: Store, Receipt
- **핵심 패키지**: `domain.pos.table`

---

## 2. 용어 정의

| 한글 | 영어 | 의미 |
|------|------|------|
| 테이블 | Table | 가게의 물리적 좌석 단위 |
| 테이블 번호 | TableNumber | 가게 내 고유 식별 번호 |
| 테이블 위치 | TablePoint | 2차원 좌표 (x, y) |
| 테이블 수용 인원 | TableCapacity | 최대 착석 가능 인원 |
| 테이블 활성화 | Table Active | 고객이 테이블을 점유 중인 상태 |

---

## 3. 핵심 엔티티 및 속성

### Table

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| tableId | UUID | - | 식별자 (자동 생성) |
| storeId | Long | ✅ | 소속 가게 |
| tableNumber | Integer | ✅ | 가게 내 고유 번호 |
| tablePoint (x, y) | Integer | ✅ | 2차원 좌표 |
| tableCapacity | Integer | ✅ | 수용 인원 |
| isActive | Boolean | - | 점유 여부 (true = 점유 중) |

---

## 4. 비즈니스 규칙

### 테이블 생성
- 점주(`ROLE_OWNER`) 권한 필요
- **가게가 마감(Sale 없음 또는 CLOSED) 상태이어야 생성 가능**
- 테이블 번호는 **같은 가게 내에서 고유**해야 함

### 테이블 수정
- 점주 본인의 가게 테이블만 수정 가능
- **가게가 마감 상태이어야 수정 가능**
- 수정 가능 항목: 테이블 번호, 위치, 수용 인원

### 테이블 삭제
- 점주 본인의 가게 테이블만 삭제 가능
- **가게가 마감 상태이어야 삭제 가능**

### 테이블 활성화 상태
- QR 코드 스캔 → 영수증 발급 시 테이블 활성화(`isActive = true`)
- 영수증 정산 완료 또는 영수증 삭제 시 테이블 비활성화(`isActive = false`)
- 영수증 테이블 이동 시 이전 테이블은 비활성화, 새 테이블은 활성화

### 테이블 조회
- 인증 불필요 (`ROLE_ANONYMOUS` 포함)
- storeId로 해당 가게의 전체 테이블 목록 조회

---

## 5. API 명세

| Method | URI | 권한 | 설명 |
|--------|-----|------|------|
| `POST` | `/api/v1/table` | `ROLE_OWNER` | 테이블 생성 |
| `PATCH` | `/api/v1/table` | `ROLE_OWNER` | 테이블 수정 |
| `DELETE` | `/api/v1/table?tableId={id}` | `ROLE_OWNER` | 테이블 삭제 |
| `GET` | `/api/v1/table?storeId={id}` | 없음(익명 가능) | 가게 테이블 목록 조회 |

### POST /api/v1/table 요청 바디
```json
{
  "storeId": 1,
  "tableNumber": 1,
  "tablePointX": 100,
  "tablePointY": 200,
  "tableCapacity": 4
}
```

### PATCH /api/v1/table 요청 바디
```json
{
  "tableId": "uuid",
  "tableNumber": 2,
  "tablePointX": 150,
  "tablePointY": 250,
  "tableCapacity": 6
}
```

---

## 6. 연관 관계

```
Store (1) ──── (*) Table
Table (1) ──── (0..1) Receipt (미정산 영수증 최대 1개)
```
