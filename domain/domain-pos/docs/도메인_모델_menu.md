## Domain Model (Menu)

### Menu

_Aggregate Root_

#### 속성

- `id`: 메뉴의 고유 식별자
- `menuInfo`: 메뉴 세부 정보 (MenuInfo)
- `order`: 메뉴 순서
- `isSoldOut`: 메뉴 품절 여부
- `isRecommended`: 메뉴 추천 여부
- `auditStamp` : 생성, 수정, 삭제시간 (AuditStamp)
- `storeId`:`Long` : 가게 고유 ID
- `menuCategoryId`:`Long` : 메뉴 카테고리 고유 ID

#### 행위

- create() : 메뉴 생성
- updateMenuInfo() : 메뉴 세부 정보 수정
- updateOrder() : 메뉴 순서 수정
- updateIsSoldOut() : 메뉴 품절 여부 수정
- updateIsRecommended() : 메뉴 추천 여부 수정
- 기타 util 함수
    - setUpdatedAt() : 메뉴 수정 시간 변경
    - fromInfra() : Infra 에서 Menu 도메인으로 변환

#### 제약

- create 연산에는 메뉴 이름, 가격, 순서는 필수값이다.
    - 가게 소유자만 가능하다.
    - 영업전에만 가능하다.
    - 필수값인 순서는 사용자에게 입력받는 것이 아닌, 해당 카테고리의 메뉴 순서 최댓값 + 1로 설정해야한다.
- updateMenuInfo 연산에는 MenuInfo(이름, 가격, 설명, 대표 이미지 url) 만 변경 가능하다.
    - 가게 소유자만 가능하다.
    - 영업전에만 가능하다.
- updateOrder 연산에는 메뉴 순서만 변경 가능하다.
    - 가게 소유자만 가능하다.
    - 영업전에만 가능하다.
- updateIsSoldOut 연산에는 메뉴 품절 여부만 변경 가능하다.
    - 가게 소유자만 가능하다.
- updateIsRecommended 연산에는 메뉴 추천 여부만 변경 가능하다.
    - 가게 소유자만 가능하다.

### MenuInfo

_Value Object_

- `name`: 메뉴 이름
- `price`: 메뉴 가격
- `description`: 메뉴 설명
- `imageUrl`: 메뉴 대표 이미지




