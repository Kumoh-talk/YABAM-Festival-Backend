## Domain Model (Receipt)

### Receipt

_Aggregate Root_

#### 속성

- `id`: `UUID` : 영수증의 고유 식별자
- `usageTime`: 영수증 시간정보 (UsageTime)
- `isAdjustment` : 영수증 정산 여부
- `saleId`:`Long` : 영업 고유 ID
- `tableId`:`UUID` : 테이블 고유 ID

#### 행위

- create() : 영수증 생성
- stopUsage() : 영수증 사용 종료
- restartUsage() : 영수증 재시작
- adjust() : 영수증 정산
- moveTable() : 영수증 테이블 이동
- syncStartUsageTime() : 영수증 시작 시간 동기화
- calculateUnits() : 테이블 비용 청구 단위 계산

#### 제약

- create 연산에는 필요한 입력값이 존재하지 않는다.
    - 연산 시 자동으로 시작 시간은 현재시간, 종료 시간은 null, 정산여부는 false로 설정된다.
- restartUsage 연산에는 영수증이 정산되지 않은 상태여야 가능하다.
- adjust 연산에는 종료시간이 null이 아니고, 영수증이 정산되지 않은 상태여야 가능하다.
- moveTable 연산에는 영수증이 정산되지 않은 상태여야 가능하다.
- syncStartUsageTime 연산에는 영수증이 정산되지 않은 상태여야 가능하다.
- calculateUnits 연산에는 정적 변수로 선언된 계산 기준 시간을 가지고 비용 청구 단위를 계산한다.

#### 특이사항

- 도메인의 서비스 로직 특성상 여러 도메인의 락을 동시에 거는 로직이 존재한다.
  **Deadlock 방지를 위해 항상 동일한 순서로 락을 걸도록 한다.**
    - Sale - Table - Receipt 순으로 락을 건다.
    - Receipt - Order 순으로 락을 건다.

---

### UsageTime

_Value Object_

- `start`: 영수증 시작 시간
- `stop`: 영수증 종료 시간

#### 행위

- startUse() : 영수증 생성
- stopUse() : 영수증 사용 종료
- units() : 테이블 비용 청구 단위 계산

#### 제약

- stopUsage 연산에는 시작 시간이 null 아니어야 가능하고, 종료 시간이 null 이어야 가능하다.
- units 연산에는 매개변수로 단위 기준 시간을 입력받아, 해당 기준 시간에 맞는 비용 청구 단위를 계산한다.
    - 비용 청구 단위는 시작시간과 종료시간을 기반으로 계산된다.
    - (0분 ~ 60분 = 1단위) 기준으로 계산된다.
