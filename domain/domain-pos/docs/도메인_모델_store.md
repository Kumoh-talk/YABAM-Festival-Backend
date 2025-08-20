## Domain Model (Store)

### Store

_Aggregate Root_

#### 속성

- `id`: 가게의 고유 식별자
- `name`: 가게 이름
- `isOpen`: 가게의 영업 상태 (오픈/마감)
- `storeInfo`: 가게 세부 정보 (StoreInfo)
- `ownerPassport`: 점주 식별 정보
- `detailImageUrls`: 상세 이미지 URL 목록
- `ownerId`:`Long` : 가게 주인 고유 ID

#### 행위

- create() : 가게 생성
- update() : 가게 수정
- delete() : 가게 삭제
- registerDetailImage() : 가게 상세 이미지 등록
- deleteDetailImage() : 가게 상세 이미지 삭제

#### 제약

- create 연산에는 가게이름, 위치, 대표 이미지, 대학 이름, 테이블 비용은 필수값이다.
- update 연산에는 StoreInfo 만 변경 가능하다.
    - update 연산은 가게 소유자만 변경 가능하다.
- delete 연산은 가게 소유자만 가능하다.

### StoreInfo

_Value Object_

- `location`: 가게 위치 좌표 (위도/경도)
- `description`: 가게 소개 설명
- `headImageUrl`: 대표 이미지 URL
- `university`: 소속 대학
- `tableTime`: 테이블 사용 시간
- `tableCost`: 테이블 기본 비용
