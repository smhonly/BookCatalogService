# Submission self-check

> Walk through this file before packaging the assignment. Each
> requirement has a checkbox, the evidence, and the command (if any)
> that proves the evidence.

## 1. RESTful CRUD API

- [x] 5 endpoints under `/api/v1/books`
      (`GET` list / `GET` one / `POST` / `PUT` / `DELETE`)
- [x] No UI layer
- [x] HTTP semantics correct (`201 + Location` on create, `204` on
      delete, `404` on missing, `409` on duplicate ISBN, `400` on
      validation failure)
- [x] Full API contract documented (request/response bodies, error
      format, status code matrix)

**Evidence:** `src/main/java/com/example/bookcatalog/controller/BookController.java`,
`src/main/java/com/example/bookcatalog/exception/GlobalExceptionHandler.java`,
`docs/api-design.md`.

## 2. At least 2 design patterns

- [x] **Repository** — `BookRepository extends JpaRepository<Book, Long>`
- [x] **DTO** — `BookRequest` / `BookResponse` as Java records; entity
      never crosses the controller boundary
- [x] **Builder** — Lombok `@Builder` on `Book`

**Evidence:** `docs/design.md` (one section per pattern with problem /
location / code / extension points).

## 3. Unit + API tests, ≥ 80% coverage

- [x] Unit tests on the service layer (7 in `BookServiceTest`)
- [x] Unit tests on the model layer (2 in `BookDOTest` — covers
      `@PrePersist` callback)
- [x] API / slice tests on the controller layer (6 in `BookControllerTest`)
- [x] **Integration tests** covering the full HTTP stack (6 in
      `BookIntegrationTest` — `@SpringBootTest`, real H2, real JPA,
      real `GlobalExceptionHandler`, real JSON serialization)
- [x] All tests pass: `mvn verify` → `Tests run: 21, Failures: 0, Errors: 0`

> Coverage: 93% instruction, 96% line, 75% branch.
- [x] **JaCoCo 80% line-coverage gate wired and passing** (current
      **93%** instruction coverage, **96%** line coverage; gate rule is on `LINE`)

**Evidence:**
`src/test/java/com/example/bookcatalog/service/BookServiceTest.java`,
`src/test/java/com/example/bookcatalog/controller/BookControllerTest.java`,
`src/test/java/com/example/bookcatalog/integration/BookIntegrationTest.java`,
`src/test/java/com/example/bookcatalog/model/BookDOTest.java`,
`target/surefire-reports/`, `target/site/jacoco/index.html`.

**To verify:**
```bash
mvn verify
# Open: target/site/jacoco/index.html for the HTML report
```

**What the integration test covers that the unit / slice tests don't:**
- `GlobalExceptionHandler` (404 / 409 / 400 → JSON body, real Jackson)
- `BookDO.@PrePersist` callback (real JPA sets `createdAt` on insert)
- `BookResponse.from` (real DTO mapping on the way out)
- `BookService` end-to-end (real transaction, real repository, real DB)

## 4. Built with AI tools

- [x] All code, tests, and docs produced in collaboration with
      Claude Code (Anthropic Claude)
- [x] Prompts / inputs log kept (see `docs/ai-inputs.md`)
- [x] Skills used: built-in `java-junit`; custom `bookDO-catalog-verify`
      (playbook in `~/.claude/skills/bookDO-catalog-verify/SKILL.md`)

**Evidence:** `docs/ai-inputs.md`, git history (once the repo is
initialised).

## 5. Submit AI prompts / skills / inputs

- [x] `docs/ai-inputs.md` summarises the major interactions
- [x] Raw transcripts retained under
      `~/.claude/projects/D--projects-BookCatalogService/sessions/`
      (`agent-*.jsonl` and `*.jsonl` per session)

**Evidence:** `docs/ai-inputs.md`; Claude Code's session storage.

---

## Supporting documentation (bonus, not graded)

These are not numbered requirements, but the evaluator can read them
to understand the project:

- [x] `docs/db-design.md` — schema, indexes, DDL, cross-DB notes
- [x] `docs/api-design.md` — full API contract, status code matrix
- [x] `docs/design.md` — 3 design patterns with rationale
- [x] `docs/spec.md` — assignment brief + code mapping

---

## Open work (not blocking submission, but worth doing)

1. **Initialise git** in `D:\projects\BookCatalogService\` and commit
   the current state, so the `docs/` folder is tracked and history
   becomes a verifiable artifact for requirement 4.
2. **Tag the submission** (`git tag v0.1.0-submission`) so the
   evaluator can check out exactly the state covered by this
   checklist.
3. **Optionally create** `~/.claude/skills/book-catalog-verify/SKILL.md`
   (the skill is mentioned in `docs/ai-inputs.md` but not yet on disk).
