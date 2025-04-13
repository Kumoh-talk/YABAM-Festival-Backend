## infra - rdb - pos 모듈

### 소개

pos 시스템에서 안정성 높고 영속화를 보장하는 RDBMS에 접근하는 구현체 모듈

domain-pos 모듈을 의존하며 domain-pos 의 Repository Interface를 구현하는 모듈이다.

mysql - jpa - query dsl - spring data jpa 를 사용하여 구현한다.

### 로컬에서 사용하는 법

```bash
docker compose -f infra/rdb/pos/docker/docker-compose.yml up -d
```

- 로컬 환경에서 docker compose 로 구성된 mysql 서버를 실행한다.

### 테스트 코드 작성하는 법

```java

@DataJpaTest
@Import({JpaConfig.class, JpaComponentConfig.class, QueryDSLConfig.class})
@ActiveProfiles("test")
@ContextConfiguration(classes = {JpaConfig.class, JpaComponentConfig.class, QueryDSLConfig.class})
public abstract class RepositoryTest {
}

```

위 클래스를 의존하여 테스트 코드 작성하면 됨

spring bean 인 Repository를 주입 받는다면 위 추상 클래스에 선언하도록 하는 것을 권장
-> 이유는 빈 컨테이너 초기화를 단축하여 Test 시간 최소화를 위해

