# 가게 (Store) 도메인 요구사항

> **참고**: 이 문서는 구현 코드 기반의 실제 요구사항입니다.
> 도메인 규칙 변경 시 반드시 이 문서를 함께 수정하세요.

---

## 1. 도메인 개요

**Store(가게)**는 YABAM 플랫폼의 핵심 애그리거트입니다. 점주(Owner)가 가게를 등록하고, 고객은 가게 목록/상세를 조회하여 주문을 진행합니다.

- **관련 도메인**: Table, Menu, Sale, Receipt
- **핵심 패키지**: `domain.pos.store`

---

## 2. 용어 정의

| 한글 | 영어 | 의미 |
|------|------|------|
| 가게 | Store | 점주가 운영하는 식음료 부스 |
| 점주 | Owner | 가게 소유자 (`ROLE_OWNER`) |
| 테이블 점유비 | OccupancyFee | 테이블 총 점유 비용 (시간 단위 × 비용) |
| 테이블 시간 단위 | TableTime | 테이블 점유 시간 단위 |
| 테이블 비용 | TableCost | 시간 단위당 비용 |
| 상세 이미지 | StoreDetailImage | 가게 상세 페이지용 추가 이미지 |

---

## 3. 핵심 엔티티 및 속성

### Store

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| storeId | Long | - | 식별자 (자동 생성) |
| 가게 이름 | String | ✅ | |
| 위치 | String | ✅ | |
| 상세 설명 | String | - | |
| 대표 이미지 URL | String | ✅ | |
| 대학 이름 | String | ✅ | |
| tableTime | Integer | ✅ | 테이블 점유 시간 단위 |
| tableCost | Integer | ✅ | 시간 단위당 비용 |

### StoreDetailImage
- storeId + 이미지 URL로 구성

---

## 4. 비즈니스 규칙

### 가게 생성
- 가게 이름, 위치, 대표 이미지, 대학 이름, 테이블 비용은 **필수값**
- 점주(`ROLE_OWNER`) 권한 필요
- 생성 요청자가 UserPassport의 소유자로 기록됨

### 가게 수정
- 점주 본인의 가게만 수정 가능
- 수정 가능 항목: 가게 이름, 위치, 상세 설명, 대표 이미지, 대학 이름, tableTime, tableCost

### 가게 삭제
- 점주 본인의 가게만 삭제 가능

### 가게 이미지
- 점주 본인의 가게에만 상세 이미지 추가/삭제 가능
- 이미지 URL은 S3 Presigned URL을 통해 사전에 업로드 후 URL 등록

### 가게 조회
- 가게 목록: **인증 불필요**, 커서 기반 페이지네이션 (`lastStoreId`, `size`)
- 가게 상세: **인증 불필요**, `storeId`로 조회
- 내 가게 목록: 점주 본인의 가게 목록 조회 (로그인 필요)

---

## 5. API 명세

| Method | URI | 권한 | 설명 |
|--------|-----|------|------|
| `POST` | `/api/v1/store` | `ROLE_OWNER` | 가게 생성 |
| `GET` | `/api/v1/store?storeId={id}` | 없음 | 가게 상세 조회 |
| `GET` | `/api/v1/stores?lastStoreId={id}&size={n}` | 없음 | 가게 목록 커서 조회 |
| `PATCH` | `/api/v1/store?storeId={id}` | `ROLE_OWNER` | 가게 정보 수정 |
| `DELETE` | `/api/v1/store?storeId={id}` | `ROLE_OWNER` | 가게 삭제 |
| `POST` | `/api/v1/store/image?storeId={id}&detailImageUrl={url}` | `ROLE_OWNER` | 상세 이미지 추가 |
| `DELETE` | `/api/v1/store/image?storeId={id}&detailImageUrl={url}` | `ROLE_OWNER` | 상세 이미지 삭제 |
| `GET` | `/api/v1/mystore` | `ROLE_OWNER` | 내 가게 목록 조회 |
| `POST` | `/api/v1/presigned-url` | `ROLE_OWNER` | S3 Presigned URL 발급 |

---

## 6. 연관 관계

```
Store (1) ──── (*) Table
Store (1) ──── (*) MenuCategory ──── (*) Menu
Store (1) ──── (*) Sale
Store (1) ──── (*) StoreDetailImage
```
