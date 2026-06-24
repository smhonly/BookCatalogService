# API design

REST over HTTP, JSON in / JSON out, all endpoints public (no auth
layer — see `docs/spec.md` "Out of scope").

## Conventions

- **Base path:** `/api/v1`
- **Content type:** `application/json; charset=utf-8` for every
  request and response
- **Character encoding:** UTF-8
- **Pagination:** standard Spring Data `Pageable` query params —
  `page` (default 0), `size` (default 20), `sort` (e.g.
  `title,asc` or `publishedYear,desc`)
- **Validation:** request bodies are validated by bean-validation
  annotations on `BookRequest`. A failure produces a `400` with a
  detailed message — see the status code matrix below.

## Error response

Every 4xx and 5xx response uses the same body shape, produced by
`GlobalExceptionHandler`:

```json
{
  "timestamp": "2026-06-23T17:00:00Z",
  "status":    404,
  "error":     "Not Found",
  "message":   "Book 99 not found"
}
```

`timestamp` is ISO-8601 UTC. `error` is the standard HTTP reason
phrase. `message` is the exception's own message (or a joined
`field: detail` string for validation errors).

## Endpoints

### `GET /api/v1/books` — list books

| Aspect    | Value                                                           |
| --------- | --------------------------------------------------------------- |
| Query     | `page` (int, default 0), `size` (int, default 20), `sort` (csv) |
| Request   | (none)                                                          |
| Response  | `200 OK` with a Spring Data `Page<BookResponse>` body           |
| Errors    | (none)                                                          |

Response body shape:
```json
{
  "content": [
    {
      "id": 1,
      "title": "Dune",
      "author": "Frank Herbert",
      "isbn": "9780441172719",
      "publishedYear": 1965,
      "createdAt": "2026-06-23T17:00:00Z"
    }
  ],
  "pageable": { "pageNumber": 0, "pageSize": 20, "sort": { ... } },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "size": 20,
  "number": 0
}
```

### `GET /api/v1/books/{id}` — get one

| Aspect   | Value                              |
| -------- | ---------------------------------- |
| Path var | `id` (long)                        |
| Response | `200 OK` with a single `BookResponse` |
| Errors   | `404` if no book with that id        |

### `POST /api/v1/books` — create

| Aspect    | Value                                                                |
| --------- | -------------------------------------------------------------------- |
| Request   | `BookRequest` body (see below)                                       |
| Response  | `201 Created` with `Location: /api/v1/books/{id}` header and the created `BookResponse` in the body |
| Errors    | `400` validation failure, `409` ISBN collision                       |

Request body (`BookRequest`):
```json
{
  "title": "Dune",
  "author": "Frank Herbert",
  "isbn": "9780441172719",
  "publishedYear": 1965
}
```

- `title` and `author` — required, non-blank, ≤ 200 / 120 chars
- `isbn` — optional, ≤ 20 chars, must be unique across all books
- `publishedYear` — optional integer, must be ≥ 0

### `PUT /api/v1/books/{id}` — update

| Aspect   | Value                                                                |
| -------- | -------------------------------------------------------------------- |
| Path var | `id` (long)                                                          |
| Request  | `BookRequest` body (same shape as create)                            |
| Response | `200 OK` with the updated `BookResponse`                             |
| Errors   | `400` validation failure, `404` book missing, `409` ISBN collision     |

The id in the path is the row to update; the body's fields replace
the existing values. Supplying the same ISBN the row already has is
not a conflict — the service treats it as a no-op for the
uniqueness check.

### `DELETE /api/v1/books/{id}` — soft-delete

Sets a `deleted` flag on the row. The record is preserved in the
database but excluded from all subsequent queries (`GET`, `LIST`,
`PUT`). A soft-deleted row's ISBN becomes reusable.

| Aspect   | Value                          |
| -------- | ------------------------------ |
| Path var | `id` (long)                    |
| Response | `204 No Content` (empty body)  |
| Errors   | `404` if no book with that id    |

## Status code matrix

| Code | When                                                                       |
| ---- | -------------------------------------------------------------------------- |
| 200  | Successful read (`GET`) or update (`PUT`).                                 |
| 201  | Successful create (`POST`). Body is the created resource; `Location` header points to it. |
| 204  | Successful delete (`DELETE`). No body.                                     |
| 400  | Request body fails bean validation on `BookRequest` (`@NotBlank`, `@Size`, `@Min`). The `message` lists the offending fields. |
| 404  | Book id does not exist (single get / update / delete).                     |
| 409  | The supplied `isbn` is already used by a different book (create / update). |

## Example session

```bash
# Create
curl -s -X POST http://localhost:8080/api/v1/books \
  -H 'Content-Type: application/json' \
  -d '{"title":"Dune","author":"Frank Herbert","isbn":"9780441172719","publishedYear":1965}'
# → 201 Created
# → Location: /api/v1/books/1
# → {"id":1,"title":"Dune",...}

# List, paged and sorted
curl -s 'http://localhost:8080/api/v1/books?page=0&size=10&sort=title,asc'

# Update
curl -s -X PUT http://localhost:8080/api/v1/books/1 \
  -H 'Content-Type: application/json' \
  -d '{"title":"Dune (1965)","author":"Frank Herbert","isbn":"9780441172719","publishedYear":1965}'
# → 200 OK

# Delete
curl -s -X DELETE http://localhost:8080/api/v1/books/1
# → 204 No Content
```
