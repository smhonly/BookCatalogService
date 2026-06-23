# Assignment spec — Book Catalog Service

> Source: course assignment brief. The five requirements are quoted
> verbatim from the brief, followed by a mapping to the code that
> satisfies each one.

## Requirements (verbatim)

1. RESTful CRUD API (No UI needed).
2. Apply at least 2 design patterns in your code.
3. Write unit tests and API tests. Aim for ≥ 80% code coverage.
4. Should build it with AI tools.
5. Should submit all the AI prompts, skills or any other AI related
   inputs used in the homework.

## Mapping to code

| #   | Requirement                                          | Satisfied by                                                                                       | Evidence                                                |
| --- | ---------------------------------------------------- | -------------------------------------------------------------------------------------------------- | ------------------------------------------------------- |
| 1   | RESTful CRUD API                                     | `BookController` — 5 endpoints under `/api/v1/books` (full contract in `docs/api-design.md`)        | `src/main/java/com/example/bookcatalog/controller/`, `docs/api-design.md` |
| 2   | ≥ 2 design patterns                                  | Repository, DTO, Builder (full discussion in `docs/design.md`)                                     | `docs/design.md`                                        |
| 3   | Unit + API tests, ≥ 80% coverage                     | `BookServiceTest` (5), `BookControllerTest` (5). JaCoCo not yet wired — see checklist               | `src/test/java/com/example/bookcatalog/`                |
| 4   | Built with AI tools                                  | All code, tests, and docs produced in collaboration with Claude Code (Anthropic)                   | `docs/ai-inputs.md`, git history (when repo is init'd)  |
| 5   | Submit AI prompts / skills / inputs                  | Prompt log table in `docs/ai-inputs.md`; raw transcripts under `~/.claude/projects/.../sessions/`   | `docs/ai-inputs.md`                                     |

## Supporting documentation

These docs are not requirements in their own right, but they
document the artefacts the requirements produce:

- `docs/db-design.md` — `books` table schema, indexes, DDL
  reference, and the cross-DB migration story.
- `docs/api-design.md` — base path, content type, error format,
  full per-endpoint reference (request/response/examples), and the
  status code matrix.
- `docs/design.md` — the three design patterns (Repository, DTO,
  Builder) with problem / location / code / extension points.
- `docs/submission-checklist.md` — the 5-point self-check used
  before packaging the assignment.
- `docs/ai-inputs.md` — per-session log of the AI collaboration
  (satisfies requirement 5).

## Out of scope (intentionally not built)

The brief did not ask for any of these. They were considered and
rejected as scope creep:

- Authentication / authorization
- Search / filtering beyond what `Pageable` provides
- Swagger / OpenAPI documentation
- Actuator / metrics / health checks
- Caching, rate limiting, CORS
- Database migration tooling (Flyway / Liquibase)
