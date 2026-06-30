# ⚛️ TASK 10 — Smoke test: arranque local y verificación end-to-end

> **Status:** DONE
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

## Smoke Test Evidence

> Ejecutado el 2026-06-30. Perfil `local`, PostgreSQL 16 vía Docker Compose.

**Entorno:** Docker Compose levantado manualmente (`docker compose up -d`) antes del arranque; `spring.docker.compose.enabled=false` pasado como flag para evitar ciclo de gestión de lifecycle en el segundo intento.

**1. Flyway — migraciones**

Primera ejecución (V8 pendiente):
```
INFO  FlywayExecutor: Database: jdbc:postgresql://localhost:5432/gradeops (PostgreSQL 16.14)
INFO  DbMigrate: Successfully applied 1 migration to schema "public", now at version v8 (execution time 00:00.019s)
```
Segunda ejecución (todas en su lugar): Flyway no reportó migraciones pendientes; la app arrancó sin error de schema. V1–V8 confirmadas en la DB.

**2. Hibernate — sin errores de mapeo**

Solo warnings informacionales esperados al inicio:
```
INFO  HHH008540: Processing PersistenceUnitInfo [name: default]
INFO  HHH000001: Hibernate ORM core version 7.4.1.Final
INFO  HHH10001005: Database info: (pool info)
INFO  HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
```
Sin líneas `ERROR` de Hibernate.

**3. Bean del job y scheduler activo**

Cron cambiado temporalmente a `0/10 * * * * *` para verificación (revertido al finalizar). El job disparó en el thread `scheduling-1`:
```
INFO  [scheduling-1] PasswordResetCodeCleanupJob : PasswordResetCode cleanup started
INFO  [scheduling-1] PasswordResetCodeCleanupJob : PasswordResetCode cleanup finished — deleted=0, durationMs=40
```
`deleted=0` es el resultado esperado: no hay registros elegibles en la DB local.

**4. Arranque completo**

```
INFO  TomcatWebServer: Tomcat started on port 8080 (http) with context path '/'
INFO  GradeOpsApiApplication: Started GradeOpsApiApplication in 3.303 seconds (process running for 3.594)
```

**5. Endpoints**

| Endpoint | Request | HTTP | Body / Nota |
|----------|---------|------|-------------|
| `POST /api/v1/auth/forgot-password` | `{"email":"teacher@example.com"}` | **204** | No Content — correcto (task spec decía 200; ambos son éxito) |
| `POST /api/v1/auth/reset-password` | email + code inválido + password | **422** | `{"error":"INVALID_RESET_CODE"}` — sin 500 |

> **Nota sobre discrepancias con el task spec:**
> - El task spec decía `PUT /reset-password`; el endpoint real es `POST /reset-password`. Pre-existente, no es regresión.
> - El task spec esperaba 404/410 para código inválido; la API devuelve 422 `INVALID_RESET_CODE`. Pre-existente, no es regresión.
> - No se observó ningún 500 en ningún endpoint probado. La condición principal (sin 500) se cumple.

---

## Done Criteria

- [x] Flyway aplica las 8 migraciones sin error en entorno local
- [x] La aplicación arranca sin errores de JPA o Hibernate
- [x] `POST /forgot-password` y `POST /reset-password` responden correctamente
- [x] El scheduler está activo y el job aparece registrado en el contexto
- [x] No hay regresiones observables en el comportamiento de los endpoints existentes

---

> [← story file](../story-01-cleanup-job.md)
