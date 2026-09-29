# API

The generated OpenAPI contract is available at `/v3/api-docs`; Swagger UI is
available at `/swagger-ui.html` when the backend is running.

## Claims

`GET /api/claims` returns a paginated overview. Pages are zero-based, default to
20 results, and are capped at 100 results per request:

```text
GET /api/claims?page=0&size=20
GET /api/claims?page=0&size=20&policyId=12345
```

The response contains `content` and the pagination metadata `page`, `size`,
`totalElements`, `totalPages`, `first`, and `last`. Results are ordered by
creation time and then ID, both descending. `GET /api/claims/{id}` still returns
a single claim.
