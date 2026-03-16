# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

YABAM is a university festival table-order & POS service. The core service is a Spring Boot monolith (`yabam-core`) surrounded by supporting microservices (config server, discovery, gateway, auth). Built to handle 1000+ TPS in production.

## Build & Run Commands

```bash
# Build everything
./gradlew clean build

# Run the main application
./gradlew :application:yabam:yabam-core:bootRun --args='--spring.profiles.active=local'

# Build production JAR
./gradlew clean :application:yabam:yabam-core:bootJar

# Run all tests
./gradlew test

# Run tests for a specific module
./gradlew :domain:domain-pos:test

# Run a single test class
./gradlew :domain:domain-pos:test --tests "domain.pos.call.entity.CallTest"

# Code quality checks (checkstyle + editorconfig)
./gradlew check

# Checkstyle only
./gradlew checkstyleMain
```

## Module Structure

```
yabam/
├── application/
│   ├── yabam/yabam-core/     # Main POS Spring Boot app (bootJar)
│   ├── yabam/yabam-event/    # Event processing
│   ├── config/               # Spring Cloud Config Server
│   ├── discovery/            # Eureka Service Discovery
│   ├── gateway/              # Spring Cloud Gateway (WebFlux + JWT)
│   └── auth/                 # Authentication service
├── domain/
│   ├── domain-pos/           # Core business logic (call, cart, menu, order, receipt, review, sale, store, table)
│   └── domain-event/store-event/
├── common/
│   ├── base/                 # Global exception handling, error codes, response wrapper
│   ├── mvc/                  # Interceptors, custom annotations (@HasRole, @AssignUserPassport)
│   ├── discovery-client/
│   └── domain-entity/
├── infra/
│   ├── rdb/pos/              # JPA + QueryDSL (MySQL / H2 for tests)
│   ├── mq/kafka-pos/         # Kafka producer/consumer
│   ├── mq/redis-pos/         # Redis Reactive pub/sub + caching
│   └── aws/infra-s3/
└── utils/
    ├── util-uuid/
    └── util-url/
```

## Architecture

### Dependency Direction: Application → Domain ← Infra

The domain module must never reference infra. All dependencies flow inward to the domain.

### Development Order: Domain → Infra → Application

1. **Domain**: Pure POJO entities/VOs, repository interfaces (ports), service (business logic)
2. **Infra**: `XxxRepositoryImpl` implementing domain interfaces, JPA `XxxEntity`, QueryDSL repos, `XxxMapper`
3. **Application**: Controllers, request/response DTOs, `XxxApi` interface for Swagger

### Domain Feature Package Layout

Each feature under `domain/domain-pos/src/main/java/domain/pos/{feature}/`:
```
entity/          # Domain POJO entities, VOs, Infos (no JPA annotations)
implement/       # Internal helpers (Readers, Writers, Validators) — shared cross-aggregate concerns
repository/      # Data access interfaces (implemented in infra/rdb/pos)
service/         # Business logic orchestration (@Transactional lives here)
event/           # Domain events and listeners
```

Some features (e.g., `menu`) additionally have:
```
port/
  provided/      # Interfaces the domain exposes to outside callers (e.g., MenuCommand, MenuRead)
  required/      # Interfaces the domain needs from infrastructure
adapter/service/ # Implementations of provided ports (e.g., MenuCommandImpl, MenuReadImpl)
```

### Object Mapping: Domain vs JPA Entity

Domain objects (`domain.pos...`) are pure POJOs; JPA entities (`com.pos...`) live in `infra/rdb/pos` with the `Entity` suffix.

**Mapper pattern** (`XxxMapper.java` in infra layer):
- `Mapper.toDomain(XxxEntity)` → converts JPA entity to domain object
- `Mapper.toEntity(DomainObj)` → converts domain object to JPA entity
- Use `builder()` or `of(...)` static factory methods

`AuditStamp` (createdAt, updatedAt, deletedAt) is a read-only VO. Infra creates it via `AuditStamp.create()`, `.update()`, or `.delete()` inside `fromInfra()` static factory methods.

### Exception & Transaction Rules

- Business errors: use `ServiceException` + `ErrorCode` enum (from `common/base`). Never return `null` — throw on ID lookup failure.
- `@Transactional` belongs on Service layer. Infra `Impl` may add it only when strictly required for data consistency.
- Same-layer calls are forbidden (no Service calling another Service).

## Testing Strategy

### Domain Layer Tests
```java
@ExtendWith(MockitoExtension.class)  // No Spring context
class OrderServiceTest {
    @Mock OrderRepository orderRepository;
    @InjectMocks OrderService orderService;
}
```
Fixtures live in `domain-pos/src/testFixtures/java/fixtures/` (`XxxFixture`, `XxxInfoFixture`).

### Infra Layer Tests (Repository Impl)
```java
// Extends RepositoryTest base class which includes @DataJpaTest + @ActiveProfiles("test")
class OrderRepositoryImplTest extends RepositoryTest {
    @Autowired TestFixtureBuilder testFixtureBuilder;
    @Autowired TestEntityManager testEntityManager;

    @BeforeEach
    void setUp() {
        savedEntity = testFixtureBuilder.buildOrderEntity(...);
        testEntityManager.flush();   // Always flush+clear after setup
        testEntityManager.clear();   // to bypass 1st-level cache
    }
}
```
Use `assertThatThrownBy` to verify `ServiceException` or `NoSuchElementException` on error paths.

### Application Layer Tests
Use `@WebMvcTest` + `MockMvc`. Mock domain services with `when(...).thenReturn(...)`. Verify HTTP status codes and DTO serialization only — no real business logic.

## Code Quality

- **Checkstyle:** Naver rules (`rule-config/naver-checkstyle-rules.xml`), `maxWarnings=0`, hard failure.
- **EditorConfig:** Validated by `editorconfigCheck`; `build`, `generated`, and config files are excluded.
- Both run as part of `./gradlew check`.

## Commit Convention

Enforced by `commitlint` + `husky`. Format: `<type>: <한국어 설명>` (description must be in Korean).

| type | use |
|------|-----|
| `feat` | new feature |
| `fix` | bug fix |
| `refactor` | code restructure without behavior change |
| `test` | test code only |
| `docs` | documentation only |
| `style` | formatting, whitespace |
| `chore` | build, config, tooling |

Rules: no scope, header ≤ 100 chars, one purpose per commit (never mix `feat` + `test` + `chore`).

## Development Workflow & Planning Artifacts

For new features, follow this sequence:
1. Create `.plan/issue-{N}.md` — feature design doc (Korean). Get user approval before any code.
2. Create issue branch: `feature/issue-{N}-title`
3. Create `.plan/implementation_plan_{N}_{title}.md` and `.plan/task_{N}_{title}.md` — implementation plan.
4. Write code, then write tests (tests are mandatory for every code change).

Planning files (`.plan/`, `.claude/`, `.sisyphus/`) must not be mixed into production commits.

## Key Domain Terminology

Refer to `docs/도메인_용어집.md` for the canonical glossary. Key terms:
- **Sale (영업):** An active operating session for a store.
- **Table (테이블):** A physical dining table.
- **Call (콜):** A customer call-bell request.
- **Receipt (영수증):** A completed payment record.

## Spring Profiles

| Profile | Config Server |
|---------|--------------|
| `local` | http://localhost:8888 |
| `dev`   | http://192.168.0.191:8888 |
| `prod`  | http://192.168.0.191:8888 |

Sensitive config lives in `cloud-config-private/` (git-ignored).

## Detailed Workflow References

The following are available as Claude Code skills (invoke with `/skill-name`) and also as source files under `.agents/workflows/`:

| Skill | Purpose |
|-------|---------|
| `/feature-development` | Top-level workflow: issue design → approval → branch → plan → code → test |
| `/architecture-overview` | Module roles and dependency direction rules |
| `/layer-development-pattern` | Per-layer coding patterns and mapper rules |
| `/testing-strategy` | Detailed test fixture and assertion patterns |
| `/commit-convention` | Commit message format and separation principles |
| `/local-application-running` | Running the full stack locally (Docker + Config + Core) |
| `/performance-testing` | k6 load testing for Redis ZSET caching |
| `/notion-report` | 노션 "클로드 코드 보고서" 페이지 하위에 보고서 서브페이지 생성 |
