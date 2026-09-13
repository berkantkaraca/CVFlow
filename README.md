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

- JDK 25+ to build the backend (tested with JDK 26, `--release 25`).
  Maven **must** run on JDK 25+. If `mvn -version` shows an older Java (e.g. an IDE-bundled
  JRE), set `JAVA_HOME` to a JDK 25+ installation before running Maven:
  ```powershell
  $env:JAVA_HOME = 'C:\Program Files\Java\jdk-26.0.1'
  ```
  For a permanent fix, set the `JAVA_HOME` system/user environment variable to your JDK.
- Maven 3.9+
- Node 24+ / npm (frontend)
- Docker + Docker Compose (PostgreSQL, Gotenberg, optional full stack)
- `GEMINI_API_KEY` for AI features (app runs without it; AI calls fail cleanly)

## Environment variables

| Variable | Required | Default | Description |
|---|---|---|---|
| `GEMINI_API_KEY` | for AI | empty | Google Gemini API key |
| `JWT_SECRET` | yes (prod) | dev-only default | HS256 signing secret, min 32 chars |
| `PDF_GOTENBERG_URL` | no | `http://localhost:3000` | Gotenberg/Chromium endpoint |

See `.env.example`. Never commit `.env` or keys.

## Local setup (without Docker, except DB)

All commands assume your terminal is in the repo root (`C:\Users\bkaraca\Desktop\CVFlow`):

```bash
# 0. Go to the repo root first
cd C:\Users\bkaraca\Desktop\CVFlow

# 1. Database (+ optional PDF service)
docker compose up -d postgres gotenberg

# 2. Backend (http://localhost:8080)
cd backend
mvn spring-boot:run

# 3. Frontend (http://localhost:5173, /api proxied to :8080)
cd ../frontend
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

A demo user is seeded by migration V1 (`demo@cvsaas.local`, no password — cannot log in).
Register via `POST /api/v1/auth/register`, then log in via `POST /api/v1/auth/login`.
The frontend stores the JWT in localStorage and sends it as `Authorization: Bearer`.
Every CV/language/suggestion operation checks ownership; cross-user access returns 403.

## API overview

All JSON responses use `{ success, data, error: { code, message, details }, timestamp }`
except the binary PDF export.

```text
POST   /api/v1/auth/register                register (email + password, returns JWT)
POST   /api/v1/auth/login                   login (returns JWT)

GET    /api/v1/cvs                          list Master CVs
POST   /api/v1/cvs                          create (empty content + default order)GET    /api/v1/cvs/{cvId}                   get
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
POST   /api/v1/cvs/{cvId}/export/docx       { languageCode } → Word document (.docx)
```

Error codes: `VALIDATION_ERROR`, `INVALID_FIELD`, `INVALID_SECTION_ORDER`, `INVALID_JSON`,
`INVALID_REQUEST`, `CV_NOT_FOUND`, `LANGUAGE_VERSION_NOT_FOUND`,
`LANGUAGE_VERSION_ALREADY_EXISTS`, `SUGGESTION_NOT_FOUND`, `INVALID_SUGGESTION_STATE`,
`VERSION_CONFLICT`, `ACCESS_DENIED`, `AI_SERVICE_ERROR`, `AI_EMPTY_RESPONSE`,
`AI_INVALID_RESPONSE`, `AI_UNRELIABLE_RESPONSE`, `PDF_GENERATION_FAILED`, `NOT_FOUND`, `INTERNAL_ERROR`.

## AI setup

Set `GEMINI_API_KEY`. Model and endpoint are configurable in `application.yml`
(`app.ai.gemini.model`, default `gemini-3.6-flash`).
Prompts forbid inventing facts; only allowlisted section fields can be modified
(`CvFieldValidator`, no reflection).

## PDF setup

Templates `MODERN` / `MINIMAL` / `PROFESSIONAL` / `ATS` render in the backend
(`pdf/template`) and a matching preview renders in the frontend.
`ATS` is an ultra-plain single-column layout for applicant tracking systems.
DOCX export (`POST .../export/docx`) renders a plain single-column Word document
via Apache POI — no external service needed.
Conversion needs Chromium: `docker compose up -d gotenberg`
(or set `PDF_GOTENBERG_URL` to a reachable instance).

## Testing

```bash
cd backend && mvn test     # unit + service + Testcontainers integration (88 tests)
cd frontend && npm test    # vitest
cd frontend && npm run build
```

## Troubleshooting

- `UnsupportedClassVersionError ... class file version 69.0 ... up to 65.0`:
  Maven is running on an old Java (e.g. Java 21). Point `JAVA_HOME` at a JDK 25+
  (see Requirements) and retry in a new terminal.
- `cd frontend` fails with path not found: run it from the repo root
  (`cd ..\frontend` if you are inside `backend/`).

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
