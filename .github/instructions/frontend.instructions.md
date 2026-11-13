---
applyTo: "frontend/**"
---

# Frontend-specific instructions

- Inspect the relevant route/page, feature components, API client, types, and tests before changing behavior. Reuse existing patterns and shared UI primitives.
- Follow `docs/architecture/architecture-principles.md` for the actual feature/API/type structure and communication paths. Do not introduce routing, caching, or architectural layers without a concrete feature need.
- Keep TypeScript types explicit at API and component boundaries. Avoid `any`, unsafe casts, duplicated request logic, and adding libraries for problems already handled by the existing stack.
- Keep API paths relative and consistent with the Vite/Caddy `/api` proxy setup. Handle loading, empty, success, and error states consistently with nearby features.
- Do not put credentials, tokens, or private data in source code or logs. The only exception for browser storage is the deliberate demo login session in `sessionStorage` (`features/auth/authStore`); do not add further storage of credentials. Never assume client-side role checks secure backend data.
- Preserve accessibility, responsive behavior, and the existing design system. Avoid unrelated visual redesigns.
- Add or update tests if the affected feature has test coverage or the change introduces testable behavior. Tests live next to the code as `*.test.ts(x)` (Vitest, Testing Library). Run `npm run lint`, `npm run format:check`, `npm run test:frontend`, and `npm run build` from the repository root for meaningful frontend changes.
- Keep user-facing text consistent with the surrounding UI. Avoid adding untranslated or misleading text.
