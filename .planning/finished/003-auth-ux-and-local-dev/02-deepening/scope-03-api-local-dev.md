# 🔍 DEEPENING: Scope 03 — api-local-dev

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md)

---

## Objective

Enable the API to start locally with a single command: auto-start PostgreSQL via Docker Compose, load Firebase credentials from a file path, and configure CORS for the local web origin.

---

## Tasks

| # | Task | Area | Status | Output |
|---|------|------|--------|--------|
| 1 | Add `spring-boot-docker-compose` dependency to `pom.xml` | `api/` | DONE | `pom.xml` |
| 2 | Create `api/compose.yml` with PostgreSQL 16 + named volume + healthcheck | `api/` | DONE | `compose.yml` |
| 3 | Set `spring.docker.compose.enabled: false` in `application.yml` (off by default) | `api/` | DONE | `application.yml` |
| 4 | Update `FirebaseConfig` to read `firebase.credentials-path` — if set, use `FileInputStream`; otherwise fall back to `getApplicationDefault()` | `api/` | DONE | `FirebaseConfig.java` |
| 5 | Create `application-local.yml` with `firebase.credentials-path`, `app.cors.allowed-origins`, and `spring.docker.compose.enabled: true` | `api/` | DONE | `application-local.yml` (gitignored) |
| 6 | Add `app.cors.allowed-origins: ${CORS_ALLOWED_ORIGINS:https://gradeops.app}` to `application.yml` | `api/` | DONE | `application.yml` |
| 7 | Add `CorsConfigurationSource` bean and `.cors()` to `SecurityFilterChain` in `SecurityConfig` | `api/` | DONE | `SecurityConfig.java` |

---

## Done Criteria

- [x] `./mvnw spring-boot:run -Dspring.profiles.active=local` starts PostgreSQL via Docker and boots the API.
- [x] PostgreSQL data persists across restarts via named Docker volume.
- [x] API reads Firebase credentials from the path in `application-local.yml` — no env var required.
- [x] Production profile uses Application Default Credentials (blank `credentials-path`).
- [x] CORS allows `http://localhost:3000` in the local profile; Cloud Run uses `CORS_ALLOWED_ORIGINS` env var.
- [x] `application-local.yml` is gitignored and never committed.
