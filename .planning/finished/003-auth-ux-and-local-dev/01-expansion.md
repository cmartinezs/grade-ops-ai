# 🚀 EXPANSION: 003-auth-ux-and-local-dev

> **Status:** COMPLETED (retroactive)
> [← planning/README.md](../../README.md)

---

## Scope Summary

| # | Scope | Area(s) | Priority | Depends On | Status |
|---|-------|---------|----------|------------|--------|
| 01 | web-tailwind-and-brand | WB | P0 | — | DONE |
| 02 | web-auth-ux-polish | WB | P0 | 01 | DONE |
| 03 | api-local-dev | AP | P0 | — | DONE |

---

## Impact per Repository Area

| Code | Area | Affected? | What changes |
|------|------|----------|-------------|
| DO | `docs/` | ☑ | `09-developer-guide/01-local-setup.md` updated |
| WB | `web/` | ☑ | Tailwind, logo, favicon, COOP header, error messages |
| AP | `api/` | ☑ | Docker Compose, Firebase credentials path, CORS |
| AG | `agents/` | ☐ | — |
| IN | `infra/` | ☐ | — |
| W | `.planning/` | ☑ | This planning |

---

## Key Decisions

- **signInWithPopup over signInWithRedirect**: Redirect requires Firebase Hosting deployed on `authDomain`. Popup is the correct choice for a separate Next.js deployment. COOP warning fixed with `Cross-Origin-Opener-Policy: same-origin-allow-popups` header.
- **firebase.credentials-path over GOOGLE_APPLICATION_CREDENTIALS**: Explicit path in `application-local.yml` is simpler and avoids requiring shell env var setup. Production uses ADC (blank path → `getApplicationDefault()`).
- **Docker Compose via spring-boot-docker-compose**: Auto-starts PostgreSQL when running with the `local` profile. Other profiles (`default`, `prod`) have `spring.docker.compose.enabled: false`.
- **No NEXT_PUBLIC_API_BASE_URL**: Setting this env var causes the browser to call the API directly (cross-origin, wrong path). All calls must go through the Next.js proxy at `/api/*`.
- **GoogleSignInButton as pure auth component**: Business logic (registerTeacher, redirect) moved to page-level `onSuccess` callbacks so `/login` and `/register` can have independent post-auth flows.
