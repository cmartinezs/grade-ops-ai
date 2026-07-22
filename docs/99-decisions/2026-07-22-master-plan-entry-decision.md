# ADR - Decisión de entrada al Master Plan (cierre pre-master-plan)

## Estado

Aceptada — **GO**.

## Contexto

`docs/.prompting/pre-master-plan/clean.md` definió una serie de 10 tareas para cerrar coherentemente los dos plannings abiertos que precedían al Master Plan — `.planning/active/008-assessment-creation` (root, coordinador) y `web/.planning/active/001-assessment-creation` (child) — antes de que el Master Plan pudiera tratarlos como una base cerrada en vez de trabajo pendiente. El criterio duro definido ahí: después de las 10 tareas no debía quedar ningún planning viejo "medio abierto" por incoherencia documental; lo futuro/transversal/estratégico entra como input del Master Plan, no como deuda del pre-master-plan.

Las 10 tareas se ejecutaron el 2026-07-22, con verificación real (comandos ejecutados, exit codes registrados, no asunciones) en cada paso.

## Resultado

## GO

Ambos plannings están cerrados, archivados y documentalmente coherentes. Todo defecto real encontrado durante la certificación fue corregido el mismo día. Todos los ítems que quedan abiertos están explícitamente etiquetados como input del Master Plan, sin ninguna condición bloqueante pendiente.

> **Corrección post-publicación (misma sesión, 2026-07-22):** la primera versión de esta decisión emitió **CONDITIONAL GO** con una condición sobre el docker build roto de `web/` (`@tailwindcss/oxide` sobre `node:18-alpine`), basada en la retrospectiva de Story 04 (2026-07-15). El owner del proyecto señaló que ya estaba corregido; verificado con `git log` (commit `2c73156`, PR #91, 2026-07-21 — **antes** de que empezara este pre-master-plan) y con evidencia directa (`npm run build` compila limpio bajo Node 24, el error original de binding nativo ya no ocurre). La condición se retira; el resultado pasa de CONDITIONAL GO a GO. Detalle completo de la corrección en la sección "Condición retirada" abajo — se deja visible en vez de reescribir la historia, junto con la lección correspondiente en `.planning/finished/008-assessment-creation/README.md` → Lessons.

## Decisión

### Resumen por tarea

| # | Tarea | Resultado |
|---|-------|-----------|
| 1 | Inventariar plannings abiertos | Tabla de estado real vs documentado generada; base para las tareas 2-3 |
| 2 | Reconciliar Web 001 | Índices, README, Traceability y Retrospective reconciliados; Story 01 SKIPPED, Story 02 DONE |
| 3 | Reconciliar parent 008 | Story 03 actualizada con evidencia real de Web; hallazgo no anticipado: cierre en cascada de Stories 01 y 02 (`IN PROGRESS` → `DONE`) al verificar que sus checkpoints cruzados ya estaban satisfechos; Story 05 cerrada `SKIPPED` con motivo explícito |
| 4 | Limpiar residuos documentales | `pdr-NNN-title.md` sin editar eliminado; heading genérico de `TRACEABILITY.md` corregido; decisión explícita sobre `SMOKE-TESTS.md` (diferido a Master Plan); Retrospective de 008 redactada |
| 5 | Política branch/CI | Aclarado por el owner: `develop`→`master`→CI a GCloud es el gitflow intencional, no un error. Único gap real corregido: `api/.planning/config.yml` faltante. Gap de diseño (rama `beta` dedicada para Vercel/Render) documentado como input del Master Plan en `docs/master-plan/branching-environment/README.md` |
| 6 | Certificar baseline existente | `web`/`api`/compose config: PASS limpio. **Hallazgo crítico real:** `agents/`'s `./mvnw test` (el comando documentado en `CLAUDE.md`) no compilaba sin `-Pdemo`/`-Pbeta`; `.github/workflows/agents.yml` tenía el mismo defecto en 2 pasos — **corregido el mismo día**, re-verificado pasando (32/32) |
| 7 | Seguridad/configuración mínima | Sin hallazgos nuevos. Un falso negativo del inventario original corregido (`api/smoke/README.md` ya documentaba la fixture fake correctamente) |
| 8 | Retrospectivas y DoD | Verificado completo como efecto de las tareas 2-4 y 6-7; sin cambios nuevos necesarios |
| 9 | Archivar plannings cerrados | Ambos plannings movidos a `finished/` vía el skill `plan-archive` (auditoría `pass` en ambos), índices corregidos |
| 10 | Esta decisión | — |

Evidencia completa por tarea vive en `docs/.prompting/pre-master-plan/clean.md` (checklist ejecutado) y en los archivos de evidencia dedicados: `.planning/finished/008-assessment-creation/BASELINE-CERTIFICATION-2026-07-22.md` y `SECURITY-BASELINE-2026-07-22.md`.

### Condición retirada — `web/` docker build (ya estaba resuelta)

Propuesta originalmente como la única condición de un CONDITIONAL GO, y retirada en la misma sesión tras verificación:

- **Qué se afirmó:** el build Docker de `web/` fallaba (`Error: Cannot find native binding` de `@tailwindcss/oxide` sobre `node:18-alpine`) — bloquearía `docker compose up` con los 4 servicios juntos y el gate `full-chain` de Compose que `docs/99-decisions/2026-07-21-testing-quality-by-release.md` exige desde R01.
- **Por qué la afirmación estaba desactualizada:** se basó únicamente en la retrospectiva de Story 04 (`RETROSPECTIVE-RAW.md`, 2026-07-15) sin cruzarla contra el historial de git actual antes de publicar la decisión.
- **Qué muestra el historial real:** commit `2c73156` ("fix(web): bump Dockerfile base image to node:24-alpine"), mergeado vía PR #91 el 2026-07-21 — un día antes de que este pre-master-plan siquiera empezara. `web/Dockerfile` ya usa `node:24-alpine` en ambos stages.
- **Verificación directa hecha en esta sesión:** `npm run build` en `web/` (Node v24.15.0 local, misma versión mayor que la imagen) compila limpio (`✓ Compiled successfully`) — el error de binding nativo no ocurre. El build sí falla después, en el prerendering de `/verify-email`, por falta de `NEXT_PUBLIC_FIREBASE_API_KEY` real en este worktree — el mismo patrón de "sin secretos reales aquí" ya documentado en las tareas 6 y 7, no relacionado con el bug original.
- **Resultado:** condición retirada. No queda ninguna condición pendiente para el gate `full-chain` de R01 relacionada con este defecto.

### Ítems ya resueltos como input del Master Plan (no condiciones — no requieren acción antes de empezar)

- `R-POST-01` (revalidación i18n/UI-data-semantics/API-I-O/sync-async/reachability de `web/story-02` bajo los gates del Master Plan) — parte explícita del propio scope de R01, no una precondición para entrar.
- Story 05 original (suite Playwright cross-service durable) — ya cubierta conceptualmente por `docs/master-plan/analysis/testing-strategy.md`.
- Rama `beta` dedicada para Vercel/Render (hoy Render rastrea `develop` directamente como parche funcional) — diseño diferido, documentado en `docs/master-plan/branching-environment/README.md`.
- Formalización del `.planning/SMOKE-TESTS.md` global — diferido, la certificación de baseline de 008 ya tiene evidencia propia.
- Mismatch de wording del Residual #1 en `agents/.planning/active/001-assessment-creation` (Gemini vs. Groq) — cosmético, no bloqueante, vive en un child planning fuera de este scope.

## Consecuencias

- El Master Plan puede empezar a tratar `008-assessment-creation` y `web/001-assessment-creation` como base cerrada (`finished/`), no como trabajo pendiente, sin ninguna condición previa.
- R01 puede ejercer su gate de testing `full-chain` de Compose sin bloqueo conocido relacionado con el build de `web/`.
- `.github/workflows/agents.yml` y `CLAUDE.md` quedan corregidos y verificados; futuras ejecuciones de `./mvnw test`/`spring-boot:run` en `agents/` funcionan tal como están documentadas.
- Los ítems de Master Plan input (R-POST-01, Story 05, rama `beta`, `SMOKE-TESTS.md`) deben aparecer explícitamente en el scope de R01 o en la estrategia de branching/testing del Master Plan — no deben re-descubrirse como sorpresas.
- Lección de proceso para el propio Master Plan: antes de citar un hallazgo de una retrospectiva o evidencia anterior como estado actual, verificar contra `git log` — una nota "no corregido" puede quedar desactualizada sin que nadie actualice el documento que la cita.

## Fuentes

- `docs/.prompting/pre-master-plan/clean.md` — checklist ejecutado, tarea por tarea, con evidencia inline.
- `.planning/finished/008-assessment-creation/README.md`, `BASELINE-CERTIFICATION-2026-07-22.md`, `SECURITY-BASELINE-2026-07-22.md`.
- `web/.planning/finished/001-assessment-creation/README.md`.
- `docs/master-plan/branching-environment/README.md`.
- `docs/99-decisions/2026-07-21-testing-quality-by-release.md`.
- `docs/99-decisions/2026-07-20-environment-roles.md`.
- `git log` / commit `2c73156` ("fix(web): bump Dockerfile base image to node:24-alpine", PR #91, 2026-07-21) — evidencia de la condición retirada.
