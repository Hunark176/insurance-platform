# Insurance Platform

Monorepo für eine Versicherungsplattform mit Spring-Boot-Backend und
React/Vite-Frontend.

## Voraussetzungen

- Java 21
- Node.js 22 oder neuer
- Docker (optional, für PostgreSQL)

## Lokal starten

Backend starten:

```powershell
backend\mvnw.cmd -f backend\pom.xml spring-boot:run
```

Frontend in einem zweiten Terminal starten:

```powershell
npm run dev
```

Die Anwendung ist anschließend unter `http://localhost:5173` erreichbar. Das
Frontend leitet `/api` an das Backend unter `http://localhost:8080` weiter.
Die lokale Entwicklungsdatenbank ist eine eingebettete H2-Datenbank.

Für PostgreSQL kann optional gestartet werden:

```powershell
docker compose up -d postgres
```

## Qualitätssicherung

```powershell
npm run lint
npm run build
npm run test:backend
```

Die API-Dokumentation ist unter
`http://localhost:8080/swagger-ui.html` verfügbar.

## Projektstruktur

- `backend/` - Java 21, Spring Boot, JPA und REST-API
- `frontend/` - React, TypeScript und Vite
- `docs/` - Architektur- und API-Dokumentation
- `infra/` - Infrastruktur-Dokumentation