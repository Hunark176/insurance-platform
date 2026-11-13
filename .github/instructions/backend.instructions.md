---
applyTo: "backend/**"
---

# Backend-specific instructions

- Read the affected module's public API, application service, domain types, controller/DTOs, repository, and tests as applicable before editing. Also check `docs/architecture/technical-documentation.md`, `docs/architecture/architecture-principles.md`, and the relevant ADRs for boundary decisions.
- Use DDD concepts to express actual business language and invariants, not as a reason to create ceremony. Clean Architecture means clear responsibility and module boundaries here; this is not strict hexagonal architecture.
- Follow the existing package layout and Spring patterns. Keep controllers thin; put orchestration in application services and enforce domain invariants in domain objects.
- Cross-module collaboration must go through the owning module's published API or an event. Do not inject or call another module's repository or internal service.
- Keep state changes transactional and preserve audit records, domain events, and optimistic locking when the use case requires them. Check event listeners for duplicate-delivery behavior before modifying event flows.
- Validate external input at the HTTP boundary and enforce essential invariants again in the domain where appropriate. Preserve the existing error-response conventions; do not swallow exceptions or return success-shaped fallbacks.
- For persistence changes, inspect Flyway migrations and tests first. Add a new forward-only migration for schema changes; do not edit migrations that may already have run.
- Follow existing Java/Javadoc language conventions. Document public contracts and non-obvious business rules, not trivial getters or obvious implementation details.
- Add focused tests for changed behavior, including authorization/validation/error cases when those are affected. Check module-boundary tests when changing module dependencies.
- Run the narrow relevant test(s), then `.\backend\mvnw.cmd -f backend\pom.xml test` for cross-cutting or multi-module changes. Use `verify` for CI-level validation when practical.
- Reads are public; every write endpoint declares its roles with `@PreAuthorize` and needs an `ApiAuthorizationTest`-style test for 401/403/200. Object-level checks use `CustomerAccessPolicy`. DELETE on `/api/**` stays denied. Do not treat the demo HTTP Basic accounts as production identity management.
- Rules that span several rows (such as the annual aggregate limit) must exclude concurrent changes, e.g. via `PolicyApi.lockForClaimApproval`, and need a concurrency test. Load lists across modules with batch methods (`findByIds`), not per-item calls.
- Use the injected `Clock` for time rules and `Instant` for technical timestamps.
