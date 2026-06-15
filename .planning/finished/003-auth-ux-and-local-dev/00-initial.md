# 🆕 INITIAL: 003-auth-ux-and-local-dev

> **Status:** COMPLETED (retroactive)
> **Date:** 2026-06-14
> [← planning/README.md](../../README.md)

---

## Intent

Complete the web authentication experience (styling, branding, UX edge cases) and establish a working local development stack for the API (Docker Compose, Firebase credentials, CORS).

## Why

After executing `002-google-sign-in` (scope 03), several gaps were discovered during manual testing:
- Tailwind CSS was not configured — no styles applied at all
- The app had no visual identity (logo, favicon)
- Google sign-in popup triggered a COOP browser warning
- Auth flows shared business logic that should be independent
- The API could not start locally (no Firebase credentials path, no database)
- CORS blocked browser calls when `NEXT_PUBLIC_API_BASE_URL` bypassed the Next.js proxy

## Approximate Scope

| Area | Work |
|------|------|
| `web/` | Tailwind CSS setup, AppLogo component, favicon, COOP header, auth flow independence, error message polish |
| `api/` | Docker Compose for PostgreSQL, firebase.credentials-path config, CORS in SecurityConfig |
| `docs/` | Update local-setup guide to reflect actual setup |

## Requested By

human (discovered during manual testing of 002-google-sign-in)
