# ⚛️ TASK 10 — Smoke test: arranque local y verificación end-to-end

> **Status:** TODO
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-07, task-08, task-09
> [← story file](../story-01-cleanup-job.md)

---

## Objective

Verificar manualmente que la aplicación arranca con el perfil `local`, las migraciones V1–V8 aplican sin error, los endpoints de password reset responden correctamente, y el job de cleanup aparece registrado en el log de inicio de Spring.

---

## Technical Design

- **Approach:** Smoke test manual ejecutado en el entorno local. No automatizable completamente por depender del entorno Docker/PG local y del scheduler de Spring. Sigue el patrón: arrancar → observar logs → ejercitar endpoints → confirmar.
- **Affected files:** Ninguno. Es verificación, no implementación.
- **Interfaces / contracts:** Endpoints existentes bajo prueba: `POST /api/v1/auth/forgot-password`, `PUT /api/v1/auth/reset-password`.
- **Design notes:**
  - Requiere una instancia PostgreSQL local con las variables de entorno del perfil `local` configuradas.
  - El job no se disparará a las 02:00 UTC durante el smoke test; se verifica su registro en el contexto de Spring (log de startup mostrando que el bean fue inicializado y el scheduler está activo).
  - Para forzar una ejecución del job durante el smoke test, se puede cambiar temporalmente el cron a `0/30 * * * * *` (cada 30 segundos) y observar el log, luego revertir.

---

## Implementation Steps

1. Asegurar que la DB local está corriendo y configurada según `application-local.yml`.
2. Ejecutar `./mvnw spring-boot:run -Dspring.profiles.active=local`.
3. Observar los logs de arranque y verificar:
   - [ ] Flyway aplica migraciones V1–V8 sin error (`Successfully applied N migrations`)
   - [ ] No hay errores de Hibernate (`HHH` warnings aceptables, errores no)
   - [ ] El bean `PasswordResetCodeCleanupJob` aparece registrado en el contexto
   - [ ] El scheduler está activo (`TaskScheduler` o `ScheduledAnnotationBeanPostProcessor` en logs)
4. Ejercitar `POST /api/v1/auth/forgot-password` con un email válido → respuesta 200.
5. Ejercitar `PUT /api/v1/auth/reset-password` con un código inválido → respuesta 404 o 410 (sin 500).
6. (Opcional) Cambiar cron a `0/30 * * * * *`, reiniciar, observar log de cleanup con `deleted=0` (tabla vacía o sin elegibles), revertir.

---

## Unit Tests

| # | Verificación | Cómo validar |
|---|-------------|--------------|
| 1 | Flyway aplica V1–V8 sin error | Log: `Successfully applied 8 migrations to schema "public"` |
| 2 | No errores de mapeo JPA al arrancar | Ausencia de `ERROR` en logs de Hibernate al inicio |
| 3 | Bean `PasswordResetCodeCleanupJob` inicializado | Log de Spring context muestra el bean |
| 4 | `POST /forgot-password` retorna 200 | `curl -X POST ... -d '{"email":"..."}' → HTTP 200` |
| 5 | `PUT /reset-password` con código inválido retorna 404/410, no 500 | `curl -X PUT ... → HTTP 404 o 410` |

---

## Done Criteria

- [ ] Flyway aplica las 8 migraciones sin error en entorno local
- [ ] La aplicación arranca sin errores de JPA o Hibernate
- [ ] `POST /forgot-password` y `PUT /reset-password` responden correctamente
- [ ] El scheduler está activo y el job aparece registrado en el contexto
- [ ] No hay regresiones observables en el comportamiento de los endpoints existentes

---

> [← story file](../story-01-cleanup-job.md)
