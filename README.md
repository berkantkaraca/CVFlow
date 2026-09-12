# CVFlow

SaaS for managing CVs/resumes.

> Write your CV once. Generate every language and version automatically.

Create Master CVs with structured data, derive independently editable language versions (TR/EN),
improve wording with AI (translate / improve / rewrite via suggestion + accept/reject),
pick one of three templates and export print-ready PDFs.

## Architecture

```text
                  ┌──────────────┐
                  │ React + TS   │  frontend/ (Vite, Router, TanStack Query,
                  │ (Vite)       │  Zustand, Hook Form, Zod, Tailwind)
                  └──────┬───────┘
                         │ REST (/api/v1)
                         ↓
               ┌───────────────────┐
               │ Spring Boot       │  backend/ (modular monolith)
               │                   │  cv/ → PostgreSQL (JSONB)
               │  CV ───── AI ──── PDF
               └───────┬───┬───┬───┘
                       │   │   │
                       ↓   ↓   ↓
                  PostgreSQL Gemini Gotenberg/Chromium
```

Key product rule: **AI assists, never silently mutates.** Every AI operation creates a
`PENDING` suggestion first; content changes only when the user accepts it.

## Technology stack

Backend: Java 25, Spring Boot 4.1.x, Spring Web / Data JPA / Validation, Hibernate,
PostgreSQL 17, Flyway, JUnit 5, Mockito, Testcontainers, springdoc-openapi.
Frontend: React 19, TypeScript, Vite, React Router, TanStack Query, Zustand,
React Hook Form, Zod, Tailwind CSS 4, Vitest.
AI: Google Gemini (`gemini-3.6-flash` by default) via Spring `RestClient`.
PDF: server-rendered HTML/CSS → Chromium (Gotenberg service) → PDF bytes.

## Requirements

- JDK 25+ to build the backend (tested with JDK 26, `--release 25`)
- Maven 3.9+
- Node 24+ / npm (frontend)
- Docker + Docker Compose (PostgreSQL, Gotenberg, optional full stack)
- `GEMINI_API_KEY` for AI features (app runs without it; AI calls fail cleanly)

## Environment variables

| Variable | Required | Default | Description |
|---|---|---|---|
| `GEMINI_API_KEY` | for AI | empty | Google Gemini API key |
| `PDF_GOTENBERG_URL` | no | `http://localhost:3000` | Gotenberg/Chromium endpoint |

See `.env.example`. Never commit `.env` or keys.

## Local setup (without Docker, except DB)

```bash
# 1. Database (+ optional PDF service)
docker compose up -d postgres gotenberg

# 2. Backend (http://localhost:8080)
cd backend
mvn spring-boot:run

# 3. Frontend (http://localhost:5173, /api proxied to :8080)
cd frontend
npm install
npm run dev
```

Health: `GET /api/v1/health` and `/actuator/health`.
API docs (Swagger UI): `http://localhost:8080/swagger-ui.html`, OpenAPI JSON: `/v3/api-docs`.

## Docker setup (full stack)

```bash
docker compose up -d --build
```

- Frontend: http://localhost:8081 (nginx, `/api` proxied to backend)
- Backend: http://localhost:8080
- PostgreSQL: localhost:5432 (`cv_saas` / `postgres` / `postgres`)
- Gotenberg: http://localhost:3000

## Database

PostgreSQL with Flyway migrations (`backend/src/main/resources/db/migration`).
Hibernate `ddl-auto: validate` — schema changes only via Flyway.
Tables: `users`, `master_cvs` (JSONB `content` + `section_order`), `cv_language_versions`
(UNIQUE per master+language), `cv_ai_suggestions`.

A demo user is seeded by migration V1 (`demo@cvsaas.local`); authentication is mocked
(`CurrentUserService`) and ownership is enforced on every operation.

## API overview

All JSON responses use `{ success, data, error: { code, message, details }, timestamp }`
except the binary PDF export.

```text
GET    /api/v1/cvs                          list Master CVs
POST   /api/v1/cvs                          create (empty content + default order)
GET    /api/v1/cvs/{cvId}                   get
PUT    /api/v1/cvs/{cvId}                   update (optimistic locking via version → 409)
DELETE /api/v1/cvs/{cvId}                   delete
POST   /api/v1/cvs/{cvId}/duplicate         deep-copy CV + language versions

GET    /api/v1/cvs/{cvId}/languages                        list versions
POST   /api/v1/cvs/{cvId}/languages                        create (copies master content)
GET    /api/v1/cvs/{cvId}/languages/{languageVersionId}    get
PUT    /api/v1/cvs/{cvId}/languages/{languageVersionId}    update (version → 409)
DELETE /api/v1/cvs/{cvId}/languages/{languageVersionId}    delete

POST   /api/v1/ai/improve                   suggestion (PENDING)
POST   /api/v1/ai/rewrite                   suggestion (PENDING)
POST   /api/v1/ai/translate                 bulk translation → individual suggestions
GET    /api/v1/ai/suggestions?languageVersionId=&status=
POST   /api/v1/ai/suggestions/{id}/accept   apply + ACCEPTED (one transaction)
POST   /api/v1/ai/suggestions/{id}/reject   REJECTED (no content change)
POST   /api/v1/ai/suggestions/accept-all    apply all PENDING (one transaction)

POST   /api/v1/cvs/{cvId}/export/pdf        { languageCode } → application/pdf
```

Error codes: `VALIDATION_ERROR`, `INVALID_FIELD`, `INVALID_SECTION_ORDER`, `INVALID_JSON`,
`INVALID_REQUEST`, `CV_NOT_FOUND`, `LANGUAGE_VERSION_NOT_FOUND`,
`LANGUAGE_VERSION_ALREADY_EXISTS`, `SUGGESTION_NOT_FOUND`, `INVALID_SUGGESTION_STATE`,
`VERSION_CONFLICT`, `ACCESS_DENIED`, `AI_SERVICE_ERROR`, `AI_EMPTY_RESPONSE`,
`AI_INVALID_RESPONSE`, `PDF_GENERATION_FAILED`, `NOT_FOUND`, `INTERNAL_ERROR`.

## AI setup

Set `GEMINI_API_KEY`. Model and endpoint are configurable in `application.yml`
(`app.ai.gemini.model`, default `gemini-3.6-flash`).
Prompts forbid inventing facts; only allowlisted section fields can be modified
(`CvFieldValidator`, no reflection).

## PDF setup

Templates `MODERN` / `MINIMAL` / `PROFESSIONAL` render in the backend
(`pdf/template`) and a matching preview renders in the frontend.
Conversion needs Chromium: `docker compose up -d gotenberg`
(or set `PDF_GOTENBERG_URL` to a reachable instance).

## Testing

```bash
cd backend && mvn test     # unit + service + Testcontainers integration (87 tests)
cd frontend && npm test    # vitest
cd frontend && npm run build
```

## Project structure

```text
cvflow/
├── backend/            Spring Boot app (cv/, ai/, pdf/, common/)
│   ├── Dockerfile
│   └── src/main/resources/db/migration/
├── frontend/           React + TS + Vite app
│   ├── Dockerfile
│   └── nginx.conf
├── docker-compose.yml  postgres + gotenberg + backend + frontend
└── README.md
```
