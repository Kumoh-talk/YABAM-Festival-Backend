## Domain Model (Store)

### Sale

_Aggregate Root_

#### 속성

- `id`: 영업의 고유 식별자
- `storeId`: 가게의 고유 식별자
- `openDateTime` : 영업 시작 시간
- `closeDateTime` : 영업 종료 시간

#### 행위

- createOpenSale() : 영업 시작 생성
- close() : 영업 종료 생성

#### 제약

- `closeDateTime`은 null 일 수 있다.
    - close() 연산 전까지는 null 이다.
- `close(boolean isNotExistsNonAdjustReceipt)`
    - `closeDateTime`이 Optional.empty() 이어야 실행할 수 있다.
    - isNotExistsNonAdjustReceipt 값이 true 여야 실행할 수 있다(미정산 영수증이 없어야 영업 마감이 가능하다).
    - 정상적인 작동이 진행되면 closeDateTime에는 값이 채워진다.
