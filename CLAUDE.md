# Project Context

This project follows hexagonal architecture. Business code is separated from external dependencies through ports and adapters. In business code we avoid using Spring dependencies.

## About This Project

Backend service for calculating results of geographic quizzes played on a daily basis. 

## Tech Stack

- Java 25 (toolchain), built with Gradle (Kotlin DSL)
- Spring Boot 4.1 (Spring Framework 7) — Web MVC, Data JPA, Validation
- Hibernate ORM via Spring Data JPA, PostgreSQL as the database
- Flyway for schema migrations (`src/main/resources/db/migration`)
- JUnit 5, Mockito and AssertJ for testing; Testcontainers for integration tests against real PostgreSQL

## Key Directories

- `com/pretz/geographic/application/domain` - core business logic: `model` (domain objects), `service` (use case implementations & other business logic services), `validation` (validators). No Spring/jakarta dependencies here.
- `com/pretz/geographic/application/port` - inbound (use case interfaces) and outbound (persistence interfaces) ports
- `com/pretz/geographic/infrastructure/adapter` - inbound (API interfaces) and outbound (persistence layer) adapters
- `com/pretz/geographic/infrastructure/configuration` - `@Configuration` classes wiring domain services as Spring beans (`GeographicGamesConfig`)
- `src/test/java/com/pretz/geographic` - tests mirroring main package structure; unit tests under `application/domain`, integration tests under `infrastructure/adapter`
- `src/main/resources/db/migration` - Flyway SQL migrations (`V1__...`, `V2__...`, etc.)

## Standards

- **Hexagonal boundaries**: domain code (`application.domain`) must not import Spring, Jakarta, or persistence-specific types. Wiring happens exclusively through `@Bean` factory methods in `infrastructure/configuration`, not via stereotype annotations (`@Service`, `@Component`) on domain classes.
- **Naming conventions**:
    - Inbound ports: `XxxUseCase` interfaces, implemented by `XxxService` classes in `application.domain.service`
    - Outbound ports: `LoadXxxPort` / `SaveXxxPort` interfaces, implemented by `XxxPersistenceAdapter` classes
    - Persistence trio per aggregate: `XxxJpaEntity`, `XxxJpaRepository` (Spring Data), `XxxPersistenceAdapter`, `XxxPersistenceMapper` (domain <-> entity mapping)
    - Web layer: `XxxApi` interface (holds `@RequestMapping`/Javadoc contract) + `XxxController` implementation; request/response DTOs live in a nested `dto` package
    - Git branches: `GEOG_XX_YY` (Where `XX` is task number and `YY` short name)
    - Commit messages: Conventional Commits prefixed with the task id — `[GEOG-XX] <type>: <Description>` (e.g. `[GEOG-11] fix: Fix concurrent batches issue with guarded update mechanism`). Types in use: `feat`, `fix`, `refactor`, `test`, `infra`. Description starts with a capital letter, imperative mood, no trailing period.
- **Testing**: unit tests use JUnit 5 + Mockito + AssertJ, follow `given/when/then` comment structure and `shouldXxx...` method names. Integration tests extend `AbstractIntegrationTest` (tagged `@Tag("integration")`, Testcontainers-backed PostgreSQL) or `AbstractPostgresDataJpaTest`/`AbstractWebIntegrationTest` for narrower slices.
- - **Error handling**: domain/validation exceptions are translated into HTTP responses centrally in `ApiExceptionHandler` (`@RestControllerAdvice`), which maps each exception type to an `ApiErrorCode` and a JSON `{code, message}` body. New failure cases should be added as a handler there rather than handled ad hoc in controllers.
- **Domain identifiers**: entity IDs use typed wrapper records (e.g. `DailyEntryId`, `GameId`, `PlayerId`) instead of raw `Long`/`String`, to keep references type-safe across the domain model.
- **Javadocs**: keep them concise — describe the contract (inputs, ordering, error responses) and skip what is self-evident or standard behaviour (e.g. that reporting endpoints don't modify data).
- All endpoints use `/api/` prefix.

## Common Commands

- `./gradlew test` - run unit tests only (excludes tests tagged `integration`)
- `./gradlew integrationTest` - run integration tests (tagged `integration`, requires Docker for Testcontainers)
- `./gradlew assemble` - build the application jar
- `./gradlew build` - run unit tests and assemble
- `docker-compose up -d` - start a local PostgreSQL instance for manual/dev use (env vars: `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `DB_PORT`, default db `geographic`)

## CI Pipeline

`.github/workflows/ci.yml` runs on every push, in order: `unit-tests` -> `build` (uploads jar artifact) -> `integration-tests` -> `publish` (master branch only, currently a placeholder step).
