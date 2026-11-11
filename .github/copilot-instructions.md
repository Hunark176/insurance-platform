# Copilot instructions for Insurance Platform

## Project context

- This repository is a Java 21 / Spring Boot 3.5 modular monolith with a React 19, TypeScript, and Vite frontend.
- Backend business modules include `customer`, `product`, `policy`, `claim`, `billing`, and `underwriting`; cross-cutting code belongs in `shared`.
- Read `README.md`, `docs/architecture/technical-documentation.md`, relevant ADRs in `docs/adr/`, and `docs/api/README.md` when they apply. Source code is authoritative if documentation is stale.
- Keep user-facing explanations in German unless asked otherwise. Keep source identifiers and code documentation in English, following local conventions.

## Required workflow

1. Inspect the working-tree changes before editing. Preserve all unrelated user changes and untracked files; in particular, do not edit or stage generated log files.
2. Trace the existing behavior from the relevant entry point through services, module contracts, persistence/events, and tests before changing it. Search for established patterns and reuse them.
3. Keep changes scoped to the request. Avoid duplicate business logic, unnecessary abstractions/dependencies, and unrelated refactors.
4. If a requirement has materially different valid interpretations or changes a security, data, API, or deployment boundary, ask one focused question before choosing.
5. Add or update tests for behavior changes. Run the smallest relevant checks, then broader checks when the change crosses module or application boundaries. Never claim a check passed unless it ran successfully.
6. Review the final diff for correctness, unintended files, formatting, and documentation that must change with the implementation.
7. Report the approach, changed areas, commands run and their outcomes, and any remaining risks or deployment steps. Do not deploy, commit, or push unless explicitly requested.

## Architecture and data boundaries

- Preserve the modular-monolith boundaries. A module must use another module's published API or event contract; do not access another module's repository or internal implementation directly.
- Keep HTTP concerns in controllers, use cases/orchestration in application services, business invariants in domain types, and persistence behind the owning module's repositories.
- Keep module APIs and event payloads minimal and explicit. Use events for decoupled cross-module reactions; do not introduce a broker or persistent outbox unless the user explicitly requests that architectural change.
- Treat database schema changes as migrations: inspect the current Flyway history and add a forward-only migration when required. Never rewrite an applied migration.
- Preserve request validation, transaction boundaries, audit-trail behavior, structured logging, and optimistic locking where relevant. Do not log secrets or unnecessary personal data.
- The hosted portfolio API is intentionally public for demonstration and permits writes, while HTTP DELETE on `/api/**` is denied. It must use synthetic data only. Do not weaken this boundary or imply it is suitable for real customer data.
- Never expose credentials, tokens, private keys, or production secrets in code, logs, test fixtures, or responses.

## Validation commands

- Backend tests on Windows: `.\backend\mvnw.cmd -f backend\pom.xml test`
- Backend CI-equivalent verification on Windows: `.\backend\mvnw.cmd -f backend\pom.xml verify`
- Backend CI-equivalent verification on Linux/macOS: `bash backend/mvnw -f backend/pom.xml verify`
- Frontend checks from the repository root: `npm run lint` and `npm run build`
- Frontend formatting check when relevant: `npm run format:check`

Run `npm ci --prefix frontend` only if dependencies are not installed or the frontend lockfile has changed. Do not install dependencies or change lockfiles just to explore the project.
