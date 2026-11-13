# API

The generated OpenAPI contract is available at `/v3/api-docs`; Swagger UI is
available at `/swagger-ui.html` when the backend is running.

Public deployment: [Swagger UI](https://insurance.hunarkarim.de/swagger-ui.html)
and [OpenAPI document](https://insurance.hunarkarim.de/v3/api-docs).

## Access

Read endpoints are public. Write endpoints require HTTP Basic authentication
with one of the published demo logins; use **Authorize** in Swagger UI.

| Login      | Password   | Role     | May                                                              |
| ---------- | ---------- | -------- | ---------------------------------------------------------------- |
| `customer` | `customer` | CUSTOMER | report claims for the policies of customer `DEMO-1001` only       |
| `clerk`    | `clerk`    | CLERK    | change claim status, issue/cancel policies, manage customers      |
| `admin`    | `admin`    | ADMIN    | everything, plus products, payouts and the audit trail           |

The full route/role matrix is in the
[technical documentation](../architecture/technical-documentation.md#62-routen-und-berechtigungen).
`DELETE /api/**` is disabled. Requests are rate limited per client IP
(30 writes and 300 reads per minute). Use only synthetic data: the logins are
public.

## Errors

Errors use `ProblemDetail` (`application/problem+json`): 400 validation,
401 not authenticated, 403 missing role or foreign policy, 404 not found,
409 concurrent update, 422 business rule, 429 rate limit (`Retry-After`).
Send `X-Requested-With: XMLHttpRequest` from browsers to avoid the native
Basic-auth dialog on 401.

## Claims

`GET /api/claims` returns a paginated overview. Pages are zero-based, default to
20 results, and are capped at 100 results per request:

```text
GET /api/claims?page=0&size=20
GET /api/claims?page=0&size=20&policyId=12345
```

The response contains `content` and the pagination metadata `page`, `size`,
`totalElements`, `totalPages`, `first`, and `last`. Results are ordered by
creation time and then ID, both descending. `GET /api/claims/{id}` returns a
single claim. Claim responses include `status`, `policyId`, `customerNumber`
and `customerName`; timestamps are UTC instants (`2026-09-24T17:00:00Z`).

```text
PATCH /api/claims/{id}/status?newStatus=IN_REVIEW     (CLERK, ADMIN)
PATCH /api/claims/{id}/status?newStatus=APPROVED      (CLERK, ADMIN)
```

Approval checks coverage on the loss date, the coverage limit per loss and,
if the policy has one, the remaining annual aggregate of the policy year.

## Policies

Policy responses include `customerId` and `productId` alongside
`customerNumber`, `customerName`, `productName`, `cancelledOn` and
`annualAggregateLimit`.

```text
POST /api/policies/{id}/cancel?effectiveOn=2026-12-31   (CLERK, ADMIN; default: today)
```

Losses up to and including the effective date stay covered and can still be
reported and approved; retroactive cancellations are rejected.
