# Insurance Platform Frontend

React-, TypeScript- und Vite-Frontend der Insurance Platform: Claim- und
Policenübersicht, Login mit Demo-Zugängen und rollenabhängige
Claim-Statuswechsel.

## Entwicklung

Aus dem Repository-Stamm oder in diesem Verzeichnis:

```powershell
npm run dev
```

Vite leitet API-Aufrufe unter `/api` an das Spring-Boot-Backend auf
`http://localhost:8080` weiter. In Produktion liefert Caddy das gebaute
Frontend aus (`frontend/Dockerfile`).

## Skripte

| Skript                       | Zweck                                      |
| ---------------------------- | ------------------------------------------ |
| `npm run dev`                | Entwicklungsserver auf Port 5173           |
| `npm test`                   | Vitest einmal ausführen                    |
| `npm run test:watch`         | Vitest im Watch-Modus                      |
| `npm run typecheck`          | Typprüfung (`tsc -b`)                      |
| `npm run build`              | Typprüfung und Produktions-Build           |
| `npm run lint`               | ESLint                                     |
| `npm run format:check`       | Prettier-Prüfung (`npm run format` behebt) |
| `npm run generate:api-types` | API-Typen aus `openapi.json` erzeugen      |

## Aufbau

| Pfad                     | Inhalt                                                                          |
| ------------------------ | ------------------------------------------------------------------------------- |
| `src/api/`               | `apiClient` (Request-ID, Basic-Auth, `X-Requested-With`), Services, Fehlertexte |
| `src/features/auth/`     | Login-Dialog, Benutzermenü, Sitzungsspeicher (`sessionStorage`), `useAuth`      |
| `src/features/claims/`   | Claim-Übersicht, Statusbadges, rollenabhängige Aktionen                         |
| `src/features/policies/` | Policenübersicht                                                                |
| `src/types/`             | Transporttypen der API                                                          |

Die Anmeldung gilt nur für den Browser-Tab. Das Frontend blendet Aktionen nach
Rolle aus; die verbindliche Prüfung macht immer das Backend. Tests liegen als
`*.test.ts(x)` neben dem getesteten Code.
