# Book Catalog Service

A small REST API for managing a book catalog, built with Spring Boot 3,
Spring Data JPA, and an H2 in-memory database.

## Requirements

- Java 17+
- Maven 3.8+

## Build & run

```bash
mvn spring-boot:run
```

The service starts on `http://localhost:8080`. The H2 console is available at
`http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:bookcatalog`).

## Tests

```bash
mvn test                                    # all tests
mvn test -Dtest=BookServiceTest             # one class
mvn test -Dtest=BookServiceTest#createPersistsBookWhenIsbnIsUnique  # one method
```

## API

All endpoints are public (no authentication).

| Method | Path                  | Description                      |
|--------|-----------------------|----------------------------------|
| GET    | `/api/v1/books`       | List books (paged)               |
| GET    | `/api/v1/books/{id}`  | Get a book by id                   |
| POST   | `/api/v1/books`       | Create a book                      |
| PUT    | `/api/v1/books/{id}`  | Update a book                      |
| DELETE | `/api/v1/books/{id}`  | Delete a book                      |

### Example

```bash
# Create a book
curl -s -X POST http://localhost:8080/api/v1/books \
  -H 'Content-Type: application/json' \
  -d '{"title":"Dune","author":"Frank Herbert","isbn":"9780441172719","publishedYear":1965}'

# List books
curl -s http://localhost:8080/api/v1/books
```

## Configuration

All settings live in `src/main/resources/application.properties`:

- `spring.datasource.*` — switch from H2 to PostgreSQL/MySQL by changing the
  driver and URL.
