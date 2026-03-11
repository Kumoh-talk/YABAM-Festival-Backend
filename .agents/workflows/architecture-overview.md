---
description: 멀티 모듈 아키텍처 및 각 모듈의 역할 개요
---

# YABAM 프로젝트 멀티 모듈 아키텍처 개요

본 문서는 현재 스프링 서버의 멀티 모듈 구조와 레이어별 역할을 명세합니다. (AI 코딩 어시스턴트 참조용)

## 1. 아키텍처 핵심 모델
프로젝트는 **클린 아키텍처 / 헥사고날 아키텍처(Ports and Adapters)**에 기반하여 모듈이 분리되어 있습니다.
의존성 방향은 항상 **외부(API, Infra)에서 내부(Domain)**로 향해야 하며, Domain은 인프라의 기술적 세부사항에 의존하지 않습니다.

## 2. 모듈별 역할

### 🔸 Application (진입점 계층)
클라이언트의 요청을 받아 응답을 반환하는 API 진입점 모듈입니다.
- **역할**: HTTP 라우팅, 요청 데이터(Validator) 검증, 도메인의 UseCase(Service) 호출 후 결과를 View Model(DTO)로 매핑.
- **구성**: `config`, `discovery`, `gateway`, `auth`, `yabam-core`, `yabam-event` 등. 

### 🔸 Domain (핵심 비즈니스 로직 계층)
이 프로젝트의 중심이자 비즈니스 규칙이 담긴 순수 자바 객체(POJO) 모듈입니다.
- **역할**: 도메인 엔티티(Entity), 값 객체(VO, Info), 핵심 비즈니스 로직 (Service) 제공.
- **특징**: 데이터베이스나 특정 외부 라이브러리(JPA, Redis, Kafka)의 애노테이션이나 종속성을 가지지 않도록 설계합니다. 
- **포트(Port) 제공**: 외부 인프라스트럭처가 확장해야 할 저장소 인터페이스(`Repository`) 등을 선언만 해두고 구현은 다른 모듈에 위임합니다.
- **구성**: `domain-pos`, `domain-event:store-event`.

### 🔸 Infra (인프라스트럭처/영속성 계층)
도메인이 정의한 인터페이스(포트)를 실제 기술로 구현하는 어댑터 모듈입니다.
- **역할**: Domain 레벨에 선언된 Repository 인터페이스들을 실제 JPA, QueryDSL, AWS S3, Redis, Kafka 기술을 활용하여 구현(Impl).
- **특징**: `JpaRepository` 인터페이스 및 DB 테이블과 매핑되는 JPA용 `@Entity`들이 위치합니다.
- **구성**: `rdb:pos`, `mq:kafka-pos`, `aws:infra-s3`, `mq:redis-pos` 등.

### 🔸 Common & Utils (공통 계층)
전역적으로 재사용되는 유틸 및 기반 설정 객체들을 정의하는 모듈입니다.
- **Common**: 전역 예외 처리(Advice), JWT를 비롯한 인증 주체(`UserPassport`), 재사용 BaseEntity, MVC 공통 설정 등 (`base`, `mvc`, `domain-entity`)
- **Utils**: 특정 로직에서만 사용되는 순수 유틸리티 메서드/클래스 모음 (`util-uuid`, `util-url` 등)

## 3. 핵심 규칙
- **Domain 모듈은 Infra 모듈을 참조해서는 안 됩니다.**
  - infra 코드를 domain으로 끌어오는 것은 절대 금지됩니다. (Domain <- Infra 방향만 존재)
