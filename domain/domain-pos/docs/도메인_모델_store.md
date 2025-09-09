## Domain Model (Store)

### Store

_Aggregate Root_

#### 속성

- `id`: 가게의 고유 식별자
- `name`: 가게 이름
- `isOpen`: 가게의 영업 상태 (오픈/마감)
- `storeInfo`: 가게 세부 정보 (StoreInfo)
- `ownerPassport`: 점주 식별 정보
- `ownerId`:`Long` : 가게 주인 고유 ID

#### 행위

- create() : 가게 생성
- update() : 가게 수정
- delete() : 가게 삭제
- open() : 가게 영업 시작
- close() : 가게 영업 마감

#### 제약

- create 연산에는 가게이름, 위치, 대표 이미지, 대학 이름, 테이블 비용은 필수값이다.
- update 연산에는 StoreInfo 만 변경 가능하다.
    - update 연산은 가게 소유자만 변경 가능하다.
- delete 연산은 가게 소유자만 가능하다.
- open 연산은 가게 소유자만 가능하다.
    - open 연산은 가게가 마감 상태이어야 영업을 시작할 수 있다.
    - Sale이라는 도메인 엔티티를 생성한다.

### StoreInfo

_Value Object_

- `location`: 가게 위치 좌표 (위도/경도)
- `description`: 가게 소개 설명
- `headImageUrl`: 대표 이미지 URL
- `university`: 소속 대학
- `tableTime`: 테이블 사용 시간
- `tableCost`: 테이블 기본 비용

----

## DetailImages

_Aggregate_

### 속성

- `storeId`: 가게 ID
- `imageUrls : List<String>`: 이미지 URLs

### 행위

- register(String imageUrl) : 이미지 등록
- remove(String imageUrl) : 이미지 삭제

### 제약

- register 연산에는 String 값에 해당하는 URL 값이 들어간다
    - URL 연산에 대한 정규식 검산이 필요하다.
- remove 연산에는 String 값에 해당하는 URL 값이 들어간다
    - URL 연산에 대한 정규식 검산이 필요하다.
    - 포함되지 않은 이미지 url 을 삭제하려 한다면 에러를 반환한다.

---

# View Model (행동 X , 제약 X)

## StoreView (상세 조회 DTO)

// TODO : 해당 도메인 View 모델 재 설계 필요 이유는 Store 와 DetailImages 를 같이 집계 해야할듯 함 Store가 DetailImage와 분리됨

## StoreHeadDto (가게 목록 View DTO)

- 'storeId': 가게 ID
- 'storeName': 가게 이름
- 'isOpen': 가게 영업 상태 (오픈/마감)
- 'headImageUrl': 가게 대표 이미지 URL
- 'description': 가게 소개 설명
- 'storeDetailImageUrls': 가게 상세 이미지 URL 목록



