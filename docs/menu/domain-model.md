# 메뉴 (Menu) 도메인 요구사항

> **참고**: 이 문서는 구현 코드 기반의 실제 요구사항입니다.
> 도메인 규칙 변경 시 반드시 이 문서를 함께 수정하세요.

---

## 1. 도메인 개요

**Menu(메뉴)**는 가게가 제공하는 음식 항목입니다. **MenuCategory(카테고리)**로 분류되며, 순서(order) 기반으로 정렬됩니다. 카테고리 및 메뉴 생성/수정/삭제는 **가게 마감 상태**가 필요하며, 품절 여부 변경은 영업 중에도 가능합니다.

- **관련 도메인**: Store, Order
- **핵심 패키지**: `domain.pos.menu`

---

## 2. 용어 정의

| 한글 | 영어 | 의미 |
|------|------|------|
| 메뉴 | Menu | 가게의 음식 메뉴 |
| 메뉴 카테고리 | MenuCategory | 메뉴를 분류하는 그룹 |
| 품절 여부 | isSoldOut | 현재 주문 불가 상태 |
| 추천 여부 | isRecommended | 대표 추천 메뉴 표시 |
| 메뉴 순서 | MenuOrder | 카테고리 내 표시 순서 (1부터 시작) |

---

## 3. 핵심 엔티티 및 속성

### MenuCategory

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| menuCategoryId | Long | - | 식별자 |
| storeId | Long | ✅ | 소속 가게 |
| categoryName | String | ✅ | 카테고리 이름 |
| order | Integer | - | 표시 순서 |

> **제약**: 카테고리 수 **100개 이하** (순서 재정렬 알고리즘 제약)

### Menu

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| menuId | Long | - | 식별자 |
| menuCategoryId | Long | ✅ | 소속 카테고리 |
| storeId | Long | ✅ | 소속 가게 |
| menuName | String | ✅ | 메뉴 이름 |
| menuPrice | Integer | ✅ | 가격 |
| menuDescription | String | - | 설명 |
| menuImageUrl | String | - | 대표 이미지 |
| isSoldOut | Boolean | ✅ | 품절 여부 |
| isRecommended | Boolean | ✅ | 추천 여부 |
| order | Integer | - | 카테고리 내 표시 순서 |

> **제약**: 카테고리당 메뉴 수 **100개 이하** (순서 재정렬 알고리즘 제약)

---

## 4. 비즈니스 규칙

### 공통 제약
- 카테고리/메뉴 CRUD는 **점주(`ROLE_OWNER`) 권한** 필요
- 본인 가게의 카테고리/메뉴만 조작 가능

### 카테고리 규칙
- **생성/수정/순서변경/삭제**: 가게가 **마감 상태**이어야 함
- **삭제 제약**: 해당 카테고리에 메뉴가 존재하면 삭제 불가
- 카테고리 순서 변경: 요청한 order 위치로 이동, 기존 메뉴들의 순서 재조정

### 메뉴 규칙
- **생성/수정/순서변경/삭제**: 가게가 **마감 상태**이어야 함
- **품절 여부(`isSoldOut`) 수정**: **영업 중에도 가능** (가게 상태 무관)
- 메뉴 순서 변경: 요청한 order 위치로 이동, 같은 카테고리 내 순서 재조정

### 조회 규칙
- 가게 메뉴 전체 슬라이스 조회: 인증 불필요, 커서 기반 (`lastMenuId`, `lastMenuCategoryId`)
- 카테고리별 메뉴 목록 조회: 인증 불필요
- 메뉴 상세 조회: 인증 불필요

---

## 5. API 명세

### 메뉴 카테고리

| Method | URI | 권한 | 설명 |
|--------|-----|------|------|
| `POST` | `/api/v1/stores/{storeId}/menu-categories` | `ROLE_OWNER` | 카테고리 생성 |
| `GET` | `/api/v1/stores/{storeId}/menu-categories` | 없음 | 카테고리 목록 조회 |
| `PATCH` | `/api/v1/stores/{storeId}/menu-categories/{menuCategoryId}/info` | `ROLE_OWNER` | 카테고리 정보 수정 |
| `PATCH` | `/api/v1/stores/{storeId}/menu-categories/{menuCategoryId}/order?patchOrder={n}` | `ROLE_OWNER` | 카테고리 순서 변경 |
| `DELETE` | `/api/v1/stores/{storeId}/menu-categories/{menuCategoryId}` | `ROLE_OWNER` | 카테고리 삭제 |

### 메뉴

| Method | URI | 권한 | 설명 |
|--------|-----|------|------|
| `POST` | `/api/v1/stores/{storeId}/menus` | `ROLE_OWNER` | 메뉴 생성 |
| `GET` | `/api/v1/stores/{storeId}/menus/{menuId}` | 없음 | 메뉴 상세 조회 |
| `GET` | `/api/v1/stores/{storeId}/menus?pageSize={n}&lastMenuId={id}&lastMenuCategoryId={id}` | 없음 | 메뉴 슬라이스 조회 |
| `GET` | `/api/v1/stores/{storeId}/menu-category/{menuCategoryId}/menus` | 없음 | 카테고리별 메뉴 목록 |
| `PATCH` | `/api/v1/stores/{storeId}/menus/{menuId}/info` | `ROLE_OWNER` | 메뉴 정보 수정 |
| `PATCH` | `/api/v1/stores/{storeId}/menus/{menuId}/order?patchOrder={n}` | `ROLE_OWNER` | 메뉴 순서 변경 |
| `PATCH` | `/api/v1/stores/{storeId}/menus/{menuId}/sold-out?isSoldOut={bool}` | `ROLE_OWNER` | 품절 여부 수정 |
| `DELETE` | `/api/v1/stores/{storeId}/menus/{menuId}` | `ROLE_OWNER` | 메뉴 삭제 |

---

## 6. 연관 관계

```
Store (1) ──── (*) MenuCategory
MenuCategory (1) ──── (*) Menu
Menu (1) ──── (*) OrderMenu
```
