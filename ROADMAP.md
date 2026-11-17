# CineStack roadmap

Living plan for getting CineStack to a working, deployed MVP and then building on it.
Update the checkboxes and the status section as work lands.

## Status

_Last updated: 2026-11-16_

- Branch `feature/tv-support-and-auth-state` holds the June work as two commits (TV show
  support; login-state navigation). Not pushed. `main` is one commit ahead of GitHub.
- Auth, creating/renaming/deleting a watchlist, updating/removing an item, fetching one
  title, search and posting a review work.
- 11 of 24 API calls return a 500 (see Phase 1). Adding a title to a list and viewing
  lists are among them, so the core loop does not work end to end yet.
- Tests: one "app starts" test on H2 with Flyway off, one entity test. The migrations
  have never run in a test.

## How we work

- Claude writes, Mike reviews. Each item is a small, reviewable commit with a short note
  on the Spring/JPA idea behind it.
- Commits are authored by Mike only. No co-author trailers or tool attribution.
- Tests cover what matters: the core user flows, ownership and visibility rules, and the
  migrations. No coverage targets, no tests for getters or trivial mapping.
- One branch per phase (or per larger item), merged to `main` when green.

## MVP definition

A person can sign up, search movies and TV shows, build watchlists, track status and
rating for each title, and see posters. It is deployed at a public URL.

Out of the first cut, hidden in the UI until they work: reviews, public profiles,
recommendations, legacy import/export.

---

## Phase 1: Make the core work (backend correctness)

Needs Docker Desktop running for the Postgres-backed tests.

### 1.1 Test harness first

- [ ] Integration test base using Testcontainers Postgres with Flyway on, so V1 to V3
      run for real. Replace the H2 `contextLoads` test with it.
- [ ] `AuthFlowTest`: register, login, `/me`, bad password, duplicate email, no token.
- [ ] `WatchlistFlowTest`: create list, add title, list mine, get one, update item,
      remove item, delete list. This is the test that is red today.
- [ ] `WatchlistAccessTest`: private list hidden from others; another user cannot
      rename, add to, update or delete from my list; the cross-user delete below.
- [ ] `MovieImportTest`: import a movie and a TV show with TMDB stubbed; importing the
      same title twice returns the same row.

### 1.2 Lazy loading outside a transaction (9 endpoints)

Symptom: `LazyInitializationException` on list/get watchlists, public watchlists,
`/api/movies/popular`, `/api/recommendations`, the three review lists, `/api/me/export`.

Idea: a JPA collection like `Watchlist.items` or `Movie.genres` is loaded on first
access, and that needs an open database session. With `open-in-view: false` the session
closes when the transaction ends, so reads must happen inside one.

- [ ] Make read methods transactional: `@Transactional(readOnly = true)` on the service
      classes, with write methods keeping their own `@Transactional`.
- [ ] Move the query logic out of `RecommendationController` and `MeController` into
      services, so controllers never touch entities.
- [ ] Load a watchlist with its items, titles and genres in one query (`@EntityGraph`
      or a fetch join) so a list with 50 titles is not 100+ queries. Check with SQL
      logging on.

### 1.3 Adding a title fails (2 endpoints)

Symptom: `NonUniqueObjectException` on `POST /api/watchlists/{id}/items` and on legacy
import.

Idea: entities set their own UUID in the constructor. Spring Data decides "new or
existing?" by whether the id is null, so it treats every new entity as existing and
calls `merge`, which makes a second copy. `addItem` also adds the item to the
watchlist's collection, which saves the original on commit. Two objects, one id.

- [ ] Save the item one way only in `addItem`.
- [ ] Fix the cause for all five entities so `save()` inserts new rows directly.
      Options: let Hibernate generate the UUID (`@GeneratedValue(strategy = UUID)`),
      or add a `@Version` column. Recommended: generated UUIDs, no schema change.

### 1.4 Security and privacy

- [ ] Cross-user delete: `deleteItem` checks that you own the list in the URL, then
      deletes any item id. Look the item up by id and list together; 404 otherwise.
- [ ] Email leak: `GET /api/users/{id}` is public and returns the user's email, and
      every public watchlist exposes its owner's id. Return a public profile shape
      (id, display name) there; keep email for `/api/auth/me` only.
- [ ] Registration with a missing password returns a 500 (`@Size` allows null). Add
      `@NotBlank`.

### 1.5 Error responses

- [ ] Unexpected exceptions return the same JSON error shape as everything else, and
      are logged with a stack trace.
- [ ] Unique-constraint races (same title added twice at once, same email registered
      twice) return 409, not 500.

**Done when:** all Phase 1 tests are green against Postgres and the endpoint sweep
shows no 500s.

---

## Phase 2: MVP frontend

### 2.1 Broken pages

- [ ] `/watchlists/[id]`, `/movies/[id]`, `/profile/[userId]` read `params.id`
      directly. In Next 16 `params` is a Promise, so the id is `undefined`. Switch to
      `useParams()`.
- [ ] One place for "401 means logged out": handle it inside `api()` (clear auth, go to
      login) instead of per page.
- [ ] Loading, empty and error states on every page. Several pages have no error
      handling at all today.

### 2.2 Core loop polish

- [ ] Search: show the result of "Add" (added, already in list, failed). Today nothing
      happens on screen.
- [ ] Watchlist page: poster, year, Movie/TV badge, remove title, filter by status,
      rename and delete list.
- [ ] Title page: poster, genres, runtime, "add to list". Link to it from search results
      and list items (nothing links to it today).
- [ ] Dashboard: useful empty state for a new account; say "titles" where it says
      "movies".
- [ ] Logged-out home: `/` goes to the dashboard, which bounces to login. Send
      logged-out visitors to search or a simple landing page.
- [ ] Hide Reviews and Picks from the nav until Phase 5 and 6.

**Done when:** a new user can go from sign-up to a list with three titles, one marked
watched and rated, without seeing an error or a dead end.

---

## Phase 3: Hardening before it is public

- [ ] Refuse to start in production with the default `JWT_SECRET`.
- [ ] Session length: tokens last 120 minutes with no refresh, so users are logged out
      mid-session. Decide: longer-lived token, or refresh tokens.
- [ ] Rate-limit login and register.
- [ ] `GET /api/movies/search` is public and every call goes to TMDB through one shared
      4-requests-per-second gate, so one visitor can starve everyone. Require login, or
      limit per IP, and cache recent searches briefly.
- [ ] A malformed date from TMDB throws and returns a 500. Parse defensively.
- [ ] Turn off Swagger UI in production, or put it behind auth.
- [ ] CI: use `npm ci` with the lockfile; make sure the Testcontainers tests run in
      GitHub Actions.

---

## Phase 4: Deploy

- [ ] Pick hosting. `infra/README.md` assumes AWS (Amplify, App Runner, RDS), which has
      a monthly cost even when idle. Compare with a cheaper setup (for example a small
      container host plus managed Postgres plus static/Next hosting) before committing.
- [ ] Production config: profile, secrets, CORS origin, Flyway on deploy, health check.
- [ ] Build and deploy from CI on merge to `main`.
- [ ] TMDB attribution visible in production (logo and notice are already in place).
- [ ] README: screenshots, live link, how to run.

**MVP is done here.**

---

## Phase 5: Features held back from the MVP

- [ ] Reviews: finish the UI (edit, delete, show on the title page), then unhide.
- [ ] Public profiles and shareable list links.
- [ ] Legacy import/export UI. Import currently creates a new row per title; match
      against TMDB instead so imports do not duplicate.
- [ ] Filter and sort by genre, year, rating.
- [ ] Pagination on list endpoints.

## Phase 6: Recommendations and ML

Today "recommendations" is the 20 most popular titles already in the database minus
what you have added, so a fresh database shows nothing.

- [ ] Seed a catalogue: import TMDB popular and top-rated lists on a schedule, within
      the existing cache rules.
- [ ] Step 1, no ML: weight genres by the user's ratings and watched history.
- [ ] Step 2, content-based: embed title overviews, store in pgvector, "more like
      this" and "because you liked X". Reuse the retrieval approach from GtHired.
- [ ] Step 3, collaborative filtering: train on MovieLens (maps to TMDB ids), since one
      user's data is not enough. Evaluate offline before shipping.

---

## Open decisions

| # | Decision | Needed by |
|---|---|---|
| 1 | Generated UUIDs vs `@Version` for the entity id fix | 1.3 |
| 2 | Longer token vs refresh tokens | Phase 3 |
| 3 | Search: require login vs per-IP limit | Phase 3 |
| 4 | Hosting provider | Phase 4 |

## Log

- 2026-11-16: June work committed on `feature/tv-support-and-auth-state` as two
  commits. Endpoint sweep run (11 of 24 calls fail). Roadmap written.
