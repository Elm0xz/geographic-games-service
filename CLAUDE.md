# Project Context

This project follows hexagonal architecture. Business code is separated from external dependencies through ports and adapters. In business code we avoid using Spring dependencies.

## About This Project

Backend service for calculating results of geographic quizzes played on a daily basis. 

## Architecture (hexagonal)

Dependencies point inward: `infrastructure` depends on `application`, never the other way round.

- `application.domain` - business logic: `model`, `service` (use case implementations), `validation`
- `application.port.in` - use case interfaces with their commands/results (driving side); `application.port.out` - persistence interfaces and the exceptions adapters may raise through them (driven side)
- `infrastructure.adapter.in` - driving adapters (web, reporting) that call use cases; `infrastructure.adapter.out` - persistence adapters implementing outbound ports
- `infrastructure.configuration` - all Spring `@Configuration`: wiring domain services as beans (`GeographicGamesConfig`), security, local users

## Standards

- **Hexagonal boundaries**: the whole `application` package (domain and ports) must not import Spring, Jakarta, or persistence-specific types. Wiring happens exclusively through `@Bean` factory methods in `infrastructure/configuration`, not via stereotype annotations (`@Service`, `@Component`) on domain classes.
- **Naming conventions**:
    - Inbound ports: `XxxUseCase` interfaces, implemented by `XxxService` classes in `application.domain.service`
    - Outbound ports: `LoadXxxPort` / `SaveXxxPort` interfaces, implemented by `XxxPersistenceAdapter` classes
    - Persistence trio per aggregate: `XxxJpaEntity`, `XxxJpaRepository` (Spring Data), `XxxPersistenceAdapter`, `XxxPersistenceMapper` (domain <-> entity mapping)
    - Web layer: `XxxApi` interface (holds `@RequestMapping`/Javadoc contract) + `XxxController` implementation; request/response DTOs live in a nested `dto` package
    - Git branches: `GEOG_XX_YY` (Where `XX` is task number and `YY` short name)
    - Commit messages: Conventional Commits prefixed with the task id — `[GEOG-XX] <type>: <Description>` (e.g. `[GEOG-11] fix: Fix concurrent batches issue with guarded update mechanism`). Types in use: `feat`, `fix`, `refactor`, `test`, `infra`. Description starts with a capital letter, imperative mood, no trailing period.
    - Both conventions are enforced by the `.githooks/commit-msg` hook (task id in the message must match the branch). Never bypass it with `--no-verify`.
- **Testing**: unit tests use JUnit 5 + Mockito + AssertJ, follow `given/when/then` comment structure and `shouldXxx...` method names. Integration tests extend `AbstractIntegrationTest` (tagged `@Tag("integration")`, Testcontainers-backed PostgreSQL) or `AbstractPostgresDataJpaTest`/`AbstractWebIntegrationTest` for narrower slices.
- **Error handling**: domain/validation exceptions are translated into HTTP responses centrally in `ApiExceptionHandler` (`@RestControllerAdvice`), which maps each exception type to an `ApiErrorCode` and a JSON `{code, message}` body. New failure cases should be added as a handler there rather than handled ad hoc in controllers.
- **Domain identifiers**: entity IDs use typed wrapper records (e.g. `DailyEntryId`, `GameId`, `PlayerId`) instead of raw `Long`/`String`, to keep references type-safe across the domain model.
- **Javadocs**: keep them concise — describe the contract (inputs, ordering, error responses) and skip what is self-evident or standard behaviour (e.g. that reporting endpoints don't modify data).
- **MR descriptions**: keep them reasonably concise, describing the change in general terms as needed by a reviewer.
- All endpoints use `/api/` prefix.

## Common Commands

- `./gradlew test` - run unit tests only (excludes tests tagged `integration`)
- `./gradlew integrationTest` - run integration tests (tagged `integration`, requires Docker for Testcontainers)
- `./gradlew installGitHooks` - point git at `.githooks` (also runs automatically before `compileJava`)
- `./run_local.sh` - run the app locally: starts PostgreSQL via docker compose and runs `bootRun` with the `local` profile. On exit it runs `docker compose down -v`, wiping the local database
