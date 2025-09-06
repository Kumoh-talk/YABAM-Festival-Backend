## 직원 호출 도메인 모델

### 도메인

- **고객은 직원호출을 할 수 있다**
    - 고객은 자기가 점유한 활성화된 테이블과 정산되지 않는 영수증이어야 호출이 가능하다.
- **점주는 완료되지 않은 직원호출을 조회할 수 있다.**
- **점주는 직원 호출을 완료처리 할 수 있다**

### Call

__Aggregate Root__

#### 속성

- id : 직원 호출 ID
- saleId : 영업 ID
- receiptId : 영수증 ID
- createdAt : 생성 일시
- callMessage:CallMessage : 호출 메시지

#### 행위

- create(receiptId,createCallReq) : 고객이 직원 호출 생성
    - callMessage를 생성한다.
- complete() : 점주가 직원 호출 완료 처리

#### 제약

- create() : 고객은 자기가 점유한 활성화된 테이블과 정산되지 않는 영수증이어야 호출이 가능하다.
    - 고객은 테이블로 유일성을 확인한다.
- complete() : 완료되지 않은 직원 호출만 완료 처리할 수 있다.
    - 이미 완료된 직원 호출은 완료 처리할 수 없다.(에러를 반환해야한다.)

### CallMessage

__Domain Object__

#### 속성

- message : 호출 메시지
- isCompleted : 완료 여부

#### 행위

- create(message) : 호출 메시지 생성
- complete() : 호출 메시지 완료 처리

### CallInfoDto

__Read Model__

#### 속성

- callId : 직원 호출 ID
- callMessage : 호출 메시지
- isCompleted : 완료 여부
- tableId : 테이블 ID
- tableNumber : 테이블 번호
- createdAt : 생성 일시

