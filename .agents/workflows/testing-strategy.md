---
description: 계층별 테스트 전략 및 TestFixture 작성 가이드
---

# YABAM 프로젝트 테스트 전략 및 패턴 (Test Strategy)

본 문서는 YABAM 멀티 모듈 구조에서 각 계층의 역할을 검증하기 위한 **테스트 작성 시점, 목적, 패턴 및 픽스처(Fixture) 활용법**을 명세합니다.
모든 테스트 클래스는 각자 맡은 책임과 분리된 계층만을 집중하여 다루어야 합니다.

## 1. Infra 레벨 (Repository Impl) 테스트 패턴
*   **목적**: DB 레벨의 영속성 처리(Save/Update/Delete) 정상 여부와 QueryDSL의 N+1 문제 같은 조회 최적화가 올바르게 작동하는가?
*   **작성 가이드**:
    *   기본적으로 `RepositoryTest`(`@DataJpaTest` 등 포함된 Base Test)를 상속하여 JpaRepository 인터페이스와 단위 테스트 격리를 보장.
    *   `TestFixtureBuilder` 활용: 인프라에서 DB 객체 생성 시, `TestFixtureBuilder`를 사용하여 의존성 주입된 Repository들을 한 번에 persist 할 수 있는 헬퍼 매개체를 권장.
*   **픽스처 활용 규칙 (`XxxEntityFixture`)**:
    *   반드시 DB 레코드 스펙에 맞춘 **엔티티(`Entity` / `Id` 등) 픽스처**를 재활용. 
    *   Domain 모듈의 픽스처(`XxxInfoFixture`)를 일부 끌어오더라도 (`REQUEST_MENU_INFO` 등), 최종적으로 생성은 `Entity`의 builder나 `of` 메서드를 적극 활용합니다.
*   **영속성 캐시 주의사항 (Flush & Clear)**:
    *   JPA의 1차 캐시를 우회하고 **실제 쿼리 성능(N+1 조회 최적화 등)**을 검사해야 하므로 `Given` (setUp) 과정 후나, 업데이트 처리 이후 단언문(assert) 이전에 반드시 `testEntityManager.flush();`, `testEntityManager.clear();` 호출을 잊지 않습니다.
*   **결과 검증 (assertThat)**:
    *   Entity 매니징을 해제한 상태에서 ID 조회를 통해 업데이트된 값을 가져오고 기대한 값 또는 기대한 Exception(`NoSuchElementException`, `ServiceException`)을 제대로 `assertThatThrownBy` 로 잡는지 검증합니다.

## 2. Domain 레벨 테스트 패턴
*   **목적**: 외부 인프라스트럭처나 DB에 방해받지 않고, 순수 비즈니스 로직(도메인 상태 변화 로직, 검증 로직 등)만 **단위 테스트 (Unit Test)** 하는 것.
*   **작성 가이드**:
    *   `@ExtendWith(MockitoExtension.class)` 기반의 가장 가벼운 테스트 작성. 
    *   스프링 컨텍스트를 로드하지 않아야 합니다. 
    *   주입받을 인터페이스(Port - Ex: XxxRepository)들은 `@Mock` 으로, 테스트할 Service 본체만 `@InjectMocks` 로 구성하여 검증합니다.
*   **픽스처 활용 규칙 (`XxxFixture`, `XxxInfoFixture`)**:
    *   도메인 로직에 사용될 순수 `POJO` 객체들을 위한 생성 팩터리가 위치합니다. (`domain-pos/src/testFixtures/java/fixtures/...`)

## 3. Application 레벨 (Controller/Service) 테스트 패턴
*   **목적**: 웹 클라이언트와의 규약에 맞는 DTO(Request/Response) 직렬화, 파라미터 유효성 검증(Validator), 접근 권한 제어를 보장.
*   **작성 가이드**:
    *   `@WebMvcTest` 활용 기법. 필요한 필터나 시큐리티 체인이 있다면 해당 컴포넌트만 따로 모킹하거나 스프링 컨텍스트에 띄웁니다.
    *   `MockMvc`를 활용하여 요청 Payload에 따른 HTTP 상태 코드 검증 (200 OK, 400 Bad Request 등).
    *   요청 유니폼이 적절히 UseCase 또는 Service에 매핑되는지만 점검 (실제 로직 실행은 모킹 `when-thenReturn`). 

이와 같은 테스트 분리 원칙을 지킴으로써 인프라 구현체 수정 시 도메인 테스트가 깨지지 않고, 도메인 규칙 수정 시 불필요하게 쿼리 테스트까지 변경되는 악순환을 예방할 수 있습니다.
