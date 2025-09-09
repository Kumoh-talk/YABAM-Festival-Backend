## Domain Model(Table)

### Table

__Aggregate Root__

#### 속성

- id: 테이블의 고유 식별자
- tableNumber: 테이블 번호
- capacity: 수용 인원
- tablePoint: 테이블 위치 좌표 (x, y)
- storeId: 가게의 고유 식별자

#### 행위

- create(storeId,createTableReq) : 테이블 생성
    - createTableReq : tableNumber(테이블번호), capacity(수용인원), tablePoint(위치)
        - tableNumber는 양수, 중복, null 불가
        - capacity는 양수, null
        - tablePoint는 null 불가
- modify(updateTableReq) : 테이블 수정
    - updateTableReq : tableNumber(테이블번호), capacity(수용인원), tablePoint(위치)
        - tableNumber는 양수, 중복, null 불가
        - capacity는 양수, null
        - tablePoint는 null 불가
- delete() : 테이블 삭제

#### 제약

- 테이블 생성은 가게가 마감 상태이어야 한다.
- 테이블 수정은 가게가 마감 상태이어야 한다.
- 테이블 삭제는 가게가 마감 상태이어야 한다.
