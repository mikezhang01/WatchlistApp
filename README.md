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

## MVP Capabilities

- Email/password registration and JWT login
- Authenticated user profile endpoint
- Movie search/import API backed by local PostgreSQL cache and TMDB fallback hooks
- CRUD watchlists with public/private visibility
- Add, update, and remove watchlist items
- Mark items watched and add personal ratings
- Public/private movie reviews
- Basic recommendations from highly rated genres
- Legacy JSON watchlist import/export path

## Local Development

Create environment files from the examples:

```text
backend/.env.example
frontend/.env.example
```

Then run:

```bash
docker compose up --build
```

Services:

- Frontend: `http://localhost:3000`
- Backend API: `http://localhost:8080`
- OpenAPI docs: `http://localhost:8080/swagger-ui.html`

## Original Project

The original implementation lives in `legacy-watchlist-app/` and includes the Java model, Swing UI, local JSON persistence, bundled JUnit jars, image assets, and original README/UML materials.

