# 🔗 Traceability: 006-password-recovery

> [← planning/README.md](../../README.md)

Term and concept traceability for this planning. For global consolidated view, see [`TRACEABILITY-GLOBAL.md`](../../TRACEABILITY-GLOBAL.md).

---

## Repository Area Code Reference

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

| Term / Concept | AG | AP | DO | IN | WB | W | Notes |
|---------------|----|----|----|----|----|---|-------|
| `sendPasswordResetEmail` (Firebase Auth SDK) | N/A | N/A | N/A | N/A | ⚠️ | N/A | Implementado en scope-01; reemplazado por 007 con `POST /api/v1/auth/forgot-password` |
| `/forgot-password` (ruta web) | N/A | N/A | ✅ | N/A | ✅ | ✅ | Ruta existente; lógica interna reemplazada por 007 |
| `/reset-password` (ruta web custom handler) | N/A | N/A | ✅ | N/A | ✅ | ✅ | Ruta existente; lógica interna reemplazada por 007 |
| `verifyPasswordResetCode` / `confirmPasswordReset` | N/A | N/A | N/A | N/A | ⚠️ | N/A | Implementados en scope-03; reemplazados por 007 |
| Mensaje neutral anti-enumeración | N/A | N/A | ✅ | N/A | ✅ | ✅ | Patrón mantenido en 007: éxito y usuario-no-encontrado muestran el mismo mensaje |
| US-012 (password recovery) | N/A | N/A | ✅ | N/A | N/A | ✅ | DoD marcado en scope-02; Technical Notes actualizadas en scope-03 |
| `FieldWithHelper` + RHF + Zod (auth forms) | N/A | N/A | N/A | N/A | ✅ | ✅ | Patrón establecido en este planning; heredado por 007 |

---

## Decisions Made

| ID | Decision | Rationale | Affects | Date |
|----|----------|-----------|---------|------|
| D01 | Mensaje neutral en `/forgot-password` para éxito y `user-not-found` | Previene user enumeration — no revelar si un email está registrado | WB, DO | 2026-06-21 |
| D02 | `auth/invalid-email` sí muestra error inline (no neutral) | Es un error de formato, no de existencia de cuenta — no revela información sensible | WB | 2026-06-21 |
| D03 | Página `/reset-password` custom en lugar de pantalla default de Firebase | Control sobre UX y look&feel del DS | WB | 2026-06-21 |
| D04 | **SUPERSEDED:** reemplazar `sendPasswordResetEmail` por email service propio en `api/` | Control sobre template del correo, eliminación de dependencia del email genérico de Firebase | AP, WB | 2026-06-22 |

---

## Residuals

| ID | Term / Issue | Blocker | Status | Target Resolution |
|----|-------------|---------|--------|------------------|
| R01 | Firebase Console Custom Action URL en entorno `demo` | Acceso a Firebase Console del proyecto | VOID | Irrelevante: 007 elimina Firebase del flujo de email |

---

> [← planning/README.md](../../README.md)
