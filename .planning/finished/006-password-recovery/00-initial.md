# 🌱 INITIAL: 006-password-recovery

> **Status:** Initial
> [← planning/README.md](../../README.md)

---

## Intent

Implementar el flujo de recuperación de contraseña (`/forgot-password`) para docentes que se registraron con correo y contraseña, usando `sendPasswordResetEmail` de Firebase Auth.

---

## Why

Un docente que olvida su contraseña no tiene forma de recuperar acceso sin contactar soporte. US-012 cierra este hueco: es P1 de la Epic 01 y el enlace "¿Olvidaste tu contraseña?" en `/login` ya existe pero apunta a `#`. Sin esta pantalla el flujo de onboarding queda incompleto.

---

## Approximate Scope

- [x] `web/` — nueva ruta `/forgot-password`, formulario con RHF + Zod, DS `FieldWithHelper` + `Input` + `Button`, enlace desde `/login` actualizado
- [x] `docs/` — marcar US-012 como implementada (Definition of Done)
- [ ] `api/` — sin cambios
- [ ] `agents/` — sin cambios
- [ ] `infra/` — sin cambios

> `api/`, `agents/`, `infra/` no requieren cambios. Firebase Auth SDK envía el correo directamente desde el cliente.

---

## Initiator

- **Requested by:** human
- **Date:** 2026-06-21
- **Related planning (if continuation):** 001-teacher-onboarding

---

## Next Step

- [ ] Cuando dimensionado → llenar `01-expansion.md` y mover a `planning/active/`

### Open Questions

- ¿Usar `ActionCodeSettings.url` para apuntar a `/reset-password` custom en lugar de la pantalla default de Firebase? → Descartado para MVP.

---

> [← planning/README.md](../../README.md)
