# 🔗 Traceability: 002-google-sign-in

> [← planning/README.md](../../README.md)

Term and concept traceability for this planning. For global consolidated view, see [`TRACEABILITY-GLOBAL.md`](../../TRACEABILITY-GLOBAL.md).

---

## Repository Area Code Reference

<!-- AREAS-REF: populated by plan-init from the project's configured areas — keep in sync with GUIDE.md -->
| Code | Area |
|------|------|
| AG | Agent Runtime (`agents/`) |
| AP | Backend / Domain (`api/`) |
| DO | Documentation (`docs/`) |
| IN | Infrastructure (`infra/`) |
| WB | Frontend (`web/`) |
| W | Planning System (`.planning/`) |

**Cell values:** `✅` present/correct · `⚠️` needs review · `❌` missing · `N/A` not applicable · *(blank)* not evaluated

---

## Term Matrix

<!-- MATRIX-HEADER: plan-init adds one column per area between "Term / Concept" and "Notes" -->
| Term / Concept | AG | AP | DO | IN | WB | W | Notes |
|---------------|----|----|----|----|----|---|-------|
| `GoogleSignInButton` | N/A | N/A | N/A | N/A | ✅ | N/A | Component React — `signInWithPopup` + API upsert |
| `signInWithPopup` | N/A | N/A | N/A | N/A | ✅ | N/A | Firebase popup-based OAuth flow |
| `GoogleAuthProvider` | N/A | N/A | N/A | N/A | ✅ | N/A | Firebase provider class for Google OAuth |
| `google_identity_platform_default_supported_idp_config` | N/A | N/A | N/A | ✅ | N/A | N/A | Terraform resource para habilitar Google como IDP |
| `provider` (teacher field) | N/A | ✅ | N/A | N/A | N/A | N/A | Columna en tabla `teachers`: `EMAIL_PASSWORD` o `GOOGLE` |
| `RegisterResult` | N/A | ✅ | N/A | N/A | N/A | N/A | Enum/sealed type: distingue `CREATED` vs `FOUND` en upsert |
| AuthGuard bypass (Google) | N/A | N/A | N/A | N/A | ✅ | N/A | Google users tienen `emailVerified: true`; skip `/verify-email` |

---

## Decisions Made

| ID | Decision | Rationale | Affects | Date |
|----|----------|-----------|---------|------|
| D-01 | Google sign-in es aditivo, no reemplaza email/password | Reduce fricción sin romper el flujo existente | AP, WB | 2026-06-15 |
| D-02 | `provider` column en `teachers` para distinguir origen de auth | Permite tratar Google users diferente (skip email verify) | AP | 2026-06-15 |
| D-03 | `terraform apply` diferido a deploy real con credenciales GCP | La resource definition es suficiente para el hackathon | IN | 2026-06-15 |

---

## Residuals

| ID | Term / Issue | Blocker | Status | Target Resolution |
|----|-------------|---------|--------|------------------|
| — | *None* | — | — | — |

---

> [← planning/README.md](../../README.md)
