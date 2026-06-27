# CineStack

CineStack is a full-stack movie tracking and recommendation SaaS built from the original Java watchlist course project.

The old Swing/local JSON application has been preserved in `legacy-watchlist-app/`. The new project is organized as a modern monorepo with a Spring Boot API, PostgreSQL persistence, a Next.js frontend, Docker-based local development, and AWS-oriented deployment notes.

## Stack

- Frontend: Next.js, TypeScript, Tailwind CSS
- Backend: Spring Boot 3, Java 21, Spring Security, Spring Data JPA
- Database: PostgreSQL with Flyway migrations
- Movie metadata: TMDB API integration point
- Local dev: Docker Compose
- CI: GitHub Actions
- Deployment target: AWS Amplify, App Runner, RDS PostgreSQL

## Repository Layout

```text
legacy-watchlist-app/  Original Java/Swing app
backend/               Spring Boot REST API
frontend/              Next.js web app
infra/                 Deployment notes and future IaC
docker-compose.yml     Local full-stack runtime
```

## Current Capabilities

- Email/password registration and JWT login
- Authenticated user profile endpoint
- TMDB-backed movie title search and metadata import when `TMDB_API_KEY` is configured
- TMDB request pacing, outage fallback, and cache retention enforcement
- CRUD watchlists with public/private visibility
- Add, update, and remove watchlist items
- Mark items watched and add personal ratings
- Public/private movie reviews
- Basic local recommendations that exclude movies already in the user's watchlists
- Legacy JSON watchlist import/export API

Filtering by genre/year/rating, genre-weighted recommendations, and a polished import/export UI are not implemented yet.

## Local Development

### Docker Compose Setup

Copy the root environment template:

```powershell
Copy-Item .env.example .env
```

Open the new root `.env` file and replace these values:

```env
JWT_SECRET=use-a-long-random-development-secret-at-least-32-bytes
TMDB_API_KEY=your-real-tmdb-api-key
```

The root `.env` is ignored by Git and is automatically read by Docker Compose. Do not commit API keys or production secrets.

Start the complete local stack:

```powershell
docker compose up --build
```

If you change `NEXT_PUBLIC_API_BASE_URL`, rebuild the frontend because `NEXT_PUBLIC_*` variables are embedded during the Next.js build:

```powershell
docker compose up --build frontend
```

### Running Without Docker

Next.js automatically reads `frontend/.env.local`. Create it from the frontend example:

```powershell
Copy-Item frontend/.env.example frontend/.env.local
```

Spring Boot does not automatically read `backend/.env`. When running the backend through Maven or an IDE, configure the variables from `backend/.env.example` in the shell or IDE run configuration. Use `localhost` in `DATABASE_URL` when Spring Boot runs outside Docker.

The environment templates serve different purposes:

```text
.env.example             Docker Compose configuration
backend/.env.example     Manual Spring Boot/IDE configuration
frontend/.env.example    Manual Next.js configuration
```

Services:

- Frontend: `http://localhost:3000`
- Backend API: `http://localhost:8080`
- OpenAPI docs: `http://localhost:8080/swagger-ui/index.html`

Without a TMDB key, authentication and watchlist APIs still work, but movie search can only return movies already stored in the local database. A new database therefore produces an empty search result until TMDB is configured.

## TMDB Attribution and Cache Compliance

CineStack includes a persistent TMDB attribution notice and an About page at `/about`. Before enabling TMDB in a public deployment, download an official logo from the [TMDB logos and attribution page](https://www.themoviedb.org/about/logos-attribution) and save it as:

```text
frontend/public/tmdb-logo.svg
```

Do not generate, redraw, or recolor the TMDB logo. The About page displays the official image automatically once the file is present.

TMDB metadata is tracked separately from user-created watchlists, ratings, notes, and reviews:

- Imported metadata receives a `tmdb_synced_at` timestamp.
- Metadata is refreshed after 30 days when accessed or during scheduled maintenance.
- Scheduled maintenance processes a limited batch to avoid excessive API traffic.
- TMDB requests are paced locally and `429`/outage responses are handled without breaking local search.
- Content that cannot be refreshed is purged after 175 days, before the six-month cache limit.
- Purging removes the TMDB identifier, metadata, image paths, ratings, and genre associations while preserving the user's watchlist/review relationship as an unavailable movie record.

If TMDB access is terminated, set this once and restart the backend:

```env
TMDB_PURGE_ON_STARTUP=true
```

After startup reports the purge, stop the backend, set it back to `false`, and restart. This is an emergency cleanup switch and should not remain enabled.

## Original Project

The original implementation lives in `legacy-watchlist-app/` and includes the Java model, Swing UI, local JSON persistence, bundled JUnit jars, image assets, and original README/UML materials.
