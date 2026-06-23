# Design patterns

This project applies **three** design patterns, each justified by a
concrete problem in the assignment scope. The brief required "at least
2" — we picked three because they are all visible in the code and
defensible on their own merits, not as pattern-padding.

> For each pattern, we document: **problem** → **where it lives** →
> **current code** → **how it would extend** if the requirements grew.

---

## 1. Repository pattern

**Problem.** The service layer needs to read and write `Book` rows
without knowing anything about SQL, JPA `EntityManager`, transactions,
or connection handling. If the service talked to JPA directly, swapping
H2 for PostgreSQL (or replacing persistence with a REST call to
another service) would require changes everywhere.

**Where it lives.**
`src/main/java/com/example/bookcatalog/repository/BookRepository.java`

**Current code.**
```java
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByIsbn(String isbn);
}
```
The interface exposes a domain-language API (`findByIsbn`,
`findAll(pageable)`, `save`, `existsById`, `deleteById`); Spring Data
JPA provides the implementation at runtime.

**Extension points.**

- Add a derived query (e.g. `List<Book> findByAuthor(String author)`) —
  no implementation needed.
- Add a custom query with `@Query` JPQL or native SQL — declare a
  method, no service-side change.
- Swap JPA for jOOQ / MyBatis / a remote API — only the repository
  class changes; `BookService` is untouched.

---

## 2. Data Transfer Object (DTO)

**Problem.** The JPA entity `Book` is **an implementation detail** of
the persistence layer: it has JPA annotations, an `id` set by the
database, and a `createdAt` that JPA fills in. Exposing it directly
over the wire would (a) leak JPA-managed state, (b) couple the public
contract to the database schema, (c) make input validation awkward
because you can't `record`-ify a `@Entity` without losing JPA.

**Where it lives.**
- `src/main/java/com/example/bookcatalog/dto/BookRequest.java` (input)
- `src/main/java/com/example/bookcatalog/dto/BookResponse.java` (output)

**Current code.**
```java
// Input — immutable record with bean-validation annotations
public record BookRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 120) String author,
        @Size(max = 20) String isbn,
        @Min(0) Integer publishedYear
) {}

// Output — projection that drops fields the client should not see
public record BookResponse(Long id, String title, String author,
                           String isbn, Integer publishedYear,
                           Instant createdAt) {
    public static BookResponse from(Book bookDO) { ... }
}
```
`BookController` accepts `BookRequest` and returns `Page<BookResponse>`
— the entity is constructed inside `BookService.toEntity(request)`
and never crosses the HTTP boundary.

**Extension points.**

- Rename a database column — only `Book` and the repository change; the
  public contract (`BookRequest` / `BookResponse`) is unaffected.
- Add a field that should never reach the client (e.g. `internalNotes`)
  — keep it on `Book`, omit it from `BookResponse`.
- API versioning — add a `BookResponseV2` record next to the old one,
  no entity change.

---

## 3. Builder

**Problem.** `Book` has five required fields plus `createdAt`. A
4-arg or 5-arg constructor is unreadable at the call site and
fragile under reordering; setters lose atomicity. Without a builder,
the call site would either be a wall of positional args or a sequence
of `setX(...)` calls that briefly leaves the entity in an invalid
state.

**Where it lives.**
`src/main/java/com/example/bookcatalog/model/Book.java` — Lombok
`@Builder` annotation.

**Current code.**
```java
@Entity
@Table(name = "books")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Book {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank @Size(max = 200) @Column(nullable = false)
    private String title;
    // ... other fields ...
}
```
And the call site in `BookService.toEntity`:
```java
return Book.builder()
        .title(request.title())
        .author(request.author())
        .isbn(request.isbn())
        .publishedYear(request.publishedYear())
        .build();
```
The call site reads as a labelled struct construction, not a sequence
of mystery positional arguments.

**Extension points.**

- Add a new field to `Book` — the builder gets one method
  automatically, all existing call sites keep compiling.
- Add a `toBuilder = true` if we ever need a partial-copy-with-changes
  pattern (Lombok supports it via `@Builder(toBuilder = true)`).

---

## Patterns considered and rejected

- **Strategy / Specification** for search — would have been appropriate
  if the brief required a search endpoint. The brief did not, so
  introducing it would be pattern padding.
- **Singleton** — every Spring bean is a singleton by default, so
  calling it out as a "pattern" adds no information.
- **Front Controller** — Spring's `DispatcherServlet` already
  implements it. Same reason: not informative to enumerate.
- **Dependency Injection** — used pervasively but considered too
  fundamental to count as a "pattern applied here"; it's part of the
  framework, not a choice this project made.
