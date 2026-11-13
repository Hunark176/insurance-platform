# Copilot instructions for Insurance Platform

## Project context

- This repository is a Java 21 / Spring Boot 3.5 modular monolith with a React 19, TypeScript, and Vite frontend.
- Backend business modules include `customer`, `product`, `policy`, `claim`, `billing`, `underwriting`, and `audit`; cross-cutting code belongs in `shared` (named interfaces `shared::domain`, `shared::web`, `shared::security`).
- Read `README.md`, `docs/architecture/technical-documentation.md`, `docs/architecture/architecture-principles.md`, relevant ADRs in `docs/adr/`, and `docs/api/README.md` when they apply. Source code is authoritative if documentation is stale.
- DDD is pragmatic in this project. Clean Architecture means clear responsibilities and module boundaries, not a mandate for strict hexagonal layers. ADR 0003 explicitly keeps JPA repositories in each owning module's `domain` package.
- Keep user-facing explanations in German unless asked otherwise. Keep source identifiers and code documentation in English, following local conventions.

## Required workflow

1. Inspect the working-tree changes before editing. Preserve all unrelated user changes and untracked files; in particular, do not edit or stage generated log files.
2. Before changing code, read these repository instructions and the matching backend/frontend path-specific instructions, then inspect the relevant ADRs, current package/module layout, representative implementations, and tests.
3. Trace the existing behavior from the relevant entry point through services, module contracts, persistence/events, and tests before changing it. Search for established patterns and reuse them.
4. Keep changes scoped to the request. Avoid duplicate business logic, unnecessary abstractions/dependencies, and unrelated refactors.
5. If a requirement has materially different valid interpretations or changes a security, data, API, or deployment boundary, ask one focused question before choosing.
6. Add or update tests for behavior changes. Run the smallest relevant checks, then broader checks when the change crosses module or application boundaries. Never claim a check passed unless it ran successfully.
7. Review the final diff for correctness, unintended files, formatting, and documentation that must change with the implementation.
8. Report the approach, changed areas, commands run and their outcomes, and any remaining risks or deployment steps. Do not deploy, commit, or push unless explicitly requested.

## Architecture and data boundaries

- Preserve the modular-monolith boundaries. A module must use another module's published API or event contract; do not access another module's repository or internal implementation directly.
- Place new classes according to `docs/architecture/architecture-principles.md`. Do not add ports, adapters, layers, or services without a concrete problem and trade-off; ask before proposing changes to an accepted ADR.
- Keep HTTP concerns in controllers, use cases/orchestration in application services, business invariants in domain types, and persistence behind the owning module's repositories.
- Keep module APIs and event payloads minimal and explicit. Use events for decoupled cross-module reactions. Events are stored in Spring Modulith's persistent event publication registry (outbox, ADR 0002) and delivered at least once, so listeners must be idempotent and backed by a unique constraint. Do not introduce a message broker unless the user explicitly requests that architectural change.
- Treat database schema changes as migrations: inspect the current Flyway history and add a forward-only migration when required. Never rewrite an applied migration.
- Preserve request validation, transaction boundaries, audit-trail behavior, structured logging, and optimistic locking where relevant. Do not log secrets or unnecessary personal data.
- The hosted portfolio API is a public demo: reads are public, writes require one of the published demo logins with the matching role (`@PreAuthorize`), customers may act only for their own policies (`CustomerAccessPolicy`), requests are rate limited, and HTTP DELETE on `/api/**` is denied. It must use synthetic data only. Do not weaken these boundaries or imply the demo logins are suitable for real customer data.
- Business rules live in domain types (for example `Claim.approve`, `Policy.covers`, see ADR 0005) and raise `DomainException` (HTTP 422); `IllegalStateException` means a programming error (HTTP 500).
- Never expose credentials, tokens, private keys, or production secrets in code, logs, test fixtures, or responses.

## Validation commands

- Backend tests on Windows: `.\backend\mvnw.cmd -f backend\pom.xml test`
- Backend CI-equivalent verification on Windows: `.\backend\mvnw.cmd -f backend\pom.xml verify`
- Backend CI-equivalent verification on Linux/macOS: `bash backend/mvnw -f backend/pom.xml verify`
- Frontend checks from the repository root: `npm run lint`, `npm run format:check`, `npm run test:frontend`, and `npm run build` (includes `tsc -b`)
- The PostgreSQL integration test needs Docker; it is skipped locally without Docker but never in CI.

Run `npm ci --prefix frontend` only if dependencies are not installed or the frontend lockfile has changed. Do not install dependencies or change lockfiles just to explore the project.
