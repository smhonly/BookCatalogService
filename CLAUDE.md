# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Assignment context

This project is a homework submission for a "Book Catalog Service"
assignment. The five requirements and how each is satisfied live in
[`docs/spec.md`](docs/spec.md); the design patterns are documented in
[`docs/design.md`](docs/design.md); the submission self-check is in
[`docs/submission-checklist.md`](docs/submission-checklist.md).

When working in this repo, respect these boundaries:

- **Scope is fixed.** Don't add auth, search, sorting, pagination
  metadata beyond what `Pageable` already gives, or any feature not
  asked for in `docs/spec.md`. If the user requests it, fine — but
  don't volunteer.
- **Patterns are fixed.** The design patterns documented in
  `docs/design.md` are intentional choices, not defaults. Don't replace
  them with alternatives unless the user asks.
- **AI usage must be tracked.** Any non-trivial change should be
  reflected in `docs/ai-inputs.md` (the assignment requires submitting
  AI prompts/inputs as part of the deliverable).

## Project

`bookDO-catalog-service` is a Spring Boot 3.2 REST API for managing a bookDO
catalog. Java 17, Maven, H2 in-memory database, JPA persistence. All
endpoints are public (no authentication layer). See `README.md` for the
public API surface and `docs/spec.md` for the assignment requirements.

## Common commands

```bash
mvn spring-boot:run               # run the service on :8080
mvn test                          # run all tests
mvn test -Dtest=BookServiceTest   # run a single test class
mvn test -Dtest=BookServiceTest#createPersistsBookWhenIsbnIsUnique  # one method
mvn -DskipTests package           # build the jar without running tests
mvn compile                       # quick compile check
```

H2 console is at <http://localhost:8080/h2-console> when running
(JDBC URL: `jdbc:h2:mem:bookcatalog`, user `sa`, no password).

## Architecture

The codebase follows a standard layered Spring Boot structure under
`src/main/java/com/example/bookcatalog/`:

```
controller/  HTTP layer. Maps requests to DTOs, returns DTOs. Throws domain exceptions;
             the GlobalExceptionHandler converts them to JSON error responses.
service/     Business logic. Transaction boundaries live here (@Transactional).
repository/  Spring Data JPA interfaces. Query methods use the derived-query DSL.
model/       JPA entities (Book). Entities are not exposed over the wire —
             always go through the DTOs.
dto/         Java records for request/response bodies. Validation annotations live here.
exception/   Domain exceptions + @RestControllerAdvice that maps them to HTTP status codes.
```

### Request flow

1. Request hits the dispatcher and is routed to a controller method.
2. Controller method runs inside a `@Transactional` service method.
3. Service talks to a JPA repository; entities become DTOs at the controller
   boundary.
4. Uncaught exceptions are translated by `GlobalExceptionHandler`.

## Conventions

- **DTOs at the boundary.** Don't return JPA entities from controllers. Use
  the `BookResponse.from(Book)` factory.
- **Records for DTOs, Lombok for entities.** DTOs are immutable Java records;
  entities use Lombok (`@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`).
- **Validation** lives on the request DTOs (`@NotBlank`, `@Size`, etc.) and is
  triggered by `@Valid` in controllers. Validation failures are converted to
  HTTP 400 by `GlobalExceptionHandler`.
- **Errors.** Throw `ResourceNotFoundException` (→ 404) or
  `DuplicateResourceException` (→ 409) from services. The global handler
  turns them into a uniform `{timestamp, status, error, message}` body.
- **Transactions.** Annotate service methods that mutate state with
  `@Transactional` (no annotation on read-only service methods is fine;
  package-level read-only transactions are not configured).
- **Tests.** Service tests use plain Mockito (`@ExtendWith(MockitoExtension.class)`).
  Controller tests use `@WebMvcTest` with `@AutoConfigureMockMvc(addFilters = false)`
  — no security filter chain to bypass.
- **Persistence.** H2 in-memory by default. To switch to PostgreSQL/MySQL,
  update the `spring.datasource.*` properties and the JPA dialect; no code
  changes are required because entities use standard JPA annotations.

## Files worth knowing

- `pom.xml` — Spring Boot parent, H2, Lombok.
- `src/main/resources/application.properties` — port, H2 URL.
- `src/main/java/com/example/bookcatalog/BookCatalogApplication.java` — main class.
