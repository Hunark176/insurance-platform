---
applyTo: "frontend/**"
---

# Frontend-specific instructions

- Inspect the relevant route/page, feature components, API client, types, and tests before changing behavior. Reuse existing patterns and shared UI primitives.
- Keep TypeScript types explicit at API and component boundaries. Avoid `any`, unsafe casts, duplicated request logic, and adding libraries for problems already handled by the existing stack.
- Keep API paths relative and consistent with the Vite/Caddy `/api` proxy setup. Handle loading, empty, success, and error states consistently with nearby features.
- Do not put credentials, tokens, or private data in browser storage, source code, or logs. Treat public API responses and writes as demo-only; never assume client-side restrictions secure backend data.
- Preserve accessibility, responsive behavior, and the existing design system. Avoid unrelated visual redesigns.
- Add or update tests if the affected feature has test coverage or the change introduces testable behavior. Run `npm run lint` and `npm run build` from the repository root for meaningful frontend changes.
- Keep user-facing text consistent with the surrounding UI. Avoid adding untranslated or misleading text.
