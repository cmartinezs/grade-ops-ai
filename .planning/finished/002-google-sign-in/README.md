# ✅ 002-google-sign-in — COMPLETED

> [← planning/README.md](../../README.md)

**Completed:** 2026-06-15
**Intent:** Google OAuth sign-in for teachers — additive alongside email/password, covering `infra/`, `api/`, and `web/`.

---

## Scopes (3 / 3 DONE)

| # | Scope | Area | Status |
|---|-------|------|--------|
| 01 | infra-google-provider | IN | ✅ DONE |
| 02 | api-teacher-upsert | AP | ✅ DONE |
| 03 | web-google-sign-in | WB | ✅ DONE |

---

## Key Outputs

| File | Scope | Description |
|------|-------|-------------|
| `infra/terraform/environments/demo/identity_platform.tf` | 01 | `google_identity_platform_default_supported_idp_config` for `google.com` |
| `api/src/main/resources/db/migration/V3__add_provider_column.sql` | 02 | `provider` column (`EMAIL_PASSWORD` / `GOOGLE`) |
| `api/.../AuthService.java`, `AuthController.java` | 02 | Upsert-by-UID, idempotent registration |
| `web/src/components/auth/GoogleSignInButton.tsx` | 03 | `signInWithPopup` + API upsert call |
| `web/src/components/auth/AuthGuard.tsx` | 03 | Bypass `/verify-email` for `providerId === 'google.com'` |

---

## Retrospective

**What went well:**
- Dependency chain (infra → api → web) was clean and sequential — no blocking issues between scopes.
- Reusing the existing Firebase Admin SDK token verification path meant zero changes to the auth middleware.
- `emailVerified: true` by default for Google users made the AuthGuard fix trivial.

**Deferred:**
- `terraform plan` / `apply` require live GCP credentials. Resource definition is complete; apply is deferred to real deployment.

**Decisions:**
- Google sign-in is additive (does not replace email/password).
- `provider` column added to `teachers` table to distinguish auth origin.
- `auth/operation-not-allowed` error code explicitly handled in the Google error map.

---

> [← planning/README.md](../../README.md)
