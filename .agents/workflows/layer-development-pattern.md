---
description: 아키텍처 계층별 개발 패턴 및 클래스 맵핑 규칙
---

# YABAM 프로젝트 개발 패턴 (아키텍처 계층별 가이드)

본 문서는 YABAM 멀티 모듈 서버 내에서 새로운 기능이나 도메인을 추가할 때 **반드시 지켜야 할 개발 패턴**을 명세합니다.

## 1. 전반적인 개발 순서 흐름 (Inward -> Outward)
1. **Domain 모듈 개발**: 
   - 요구사항 분석 후 핵심 도메인의 상태를 표현하는 **순수 자바 객체(Entity, VO, Info 등)** 작성
   - 리포지토리(DB 접근 등)의 명세(Interface) 작성 (Ex: `XxxRepository`)
   - 비즈니스 로직을 처리할 Service(또는 UseCase) 작성
2. **Infra 모듈 개발**: 
   - 앞서 선언한 도메인 Repository 인터페이스의 **구현체(`XxxRepositoryImpl`)** 작성
   - 데이터베이스 영속성 처리를 위한 **JPA 엔티티(`XxxEntity`)** 작성. (테이블 스키마와 직접 맵핑)
   - Spring Data JPA Repository(Ex: `XxxJpaRepository`) 및 복잡한 조회를 위한 `QueryDsl` 레포지토리 작성.
   - DB Entity 데이터를 Domain 객체로, Domain 객체를 DB Entity로 변환해주는 **Mapper(`XxxMapper`)** 구현.
3. **Application 모듈 개발**: 
   - 최종 사용자와의 접점 API (Controller/Handler) 구성
   - 클라이언트 요청 Request DTO 작성 및 Domain 객체로의 변환. API Response DTO 변환 및 반환.

## 2. 객체 매핑(Mapping) 원칙
도메인 객체와 인프라의 JPA 엔티티는 철저히 분리됩니다.
- **Domain Entity vs JPA Entity**:
  - `domain.pos...` 하위 객체는 POJO 형태의 `MenuInfo`, `Receipt`, `Order` 처럼 설계.
  - `infra...rdb:pos...` 하위의 데이터베이스 관리 객체는 `MenuEntity`, `ReceiptEntity`, `OrderEntity` 와 같이 명확한 접미사 지정.
- **Mapper 활용 (`xxxMapper.java`)**: 
  - Infra 계층에서 JpaRepository를 통해 조회한 뒤, Domain으로 **리턴할 때는 반드시 Mapper를 통해 Domain 객체로 변환**. (DTO <-> VO 변환 시에도 동일)
  - `Mapper.toDomain(...)`, `Mapper.toEntity(...)`처럼 변환 메서드를 체계적으로 두고, Builder나 정적 팩터리 메서드(`of(...)`)를 적극 활용합니다.

## 3. 예외 및 트랜잭션 처리 (Exception & Transaction)
- **도메인 익셉션**: 비즈니스 로직 에러는 Common 모듈에 선언된 `ServiceException`과 `ErrorCode` Enum을 통해 관리합니다. Infra를 참조하는 예외 클래스는 사용 금지.
- **트랜잭션 바운더리 (`@Transactional`)**: 
  - 기본적으로 **Application/Domain Service 레이어**에 트랜잭션을 선언합니다.
  - Infra 내부 구현체(Impl)에서 불가피하게 데이터 정합성이 중요할 때(Ex: 업데이트 로직 등) `@Transactional`을 명시하지만, Service에서 묶어주는 것이 원칙입니다.
- **예외 발생 `orElseThrow` 규칙**: ID 기반 조회 실패 등의 엣지 케이스는 반드시 Custom Exception(`ServiceException`)등을 터뜨리도록 구현하며 무차별적인 `null` 반환을 억제합니다.
