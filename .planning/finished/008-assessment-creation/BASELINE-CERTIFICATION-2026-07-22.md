# Baseline Certification — 2026-07-22

> Evidencia de la tarea 6 de `docs/.prompting/pre-master-plan/clean.md`: ejecutar y registrar las suites ya existentes por servicio, sin crear suites nuevas ni introducir testing strategy. Todos los comandos abajo son los ya documentados en `CLAUDE.md`.

- **SHA:** `449c7b05c758e599f76cf632cca81a8b30cddf96`
- **Branch:** `planning/master-plan`
- **Fecha:** 2026-07-22
- **Worktree:** `/home/carlos/projects/gradeops-plan`

## Resumen

| # | Comando | Directorio | Exit code | Resultado |
|---|---------|------------|-----------|-----------|
| 1 | `npm run test` | `web/` | 0 | PASS — 23 test suites, 153 tests |
| 2 | `npm run lint` | `web/` | 0 | PASS — sin warnings/errores |
| 3 | `docker compose -f api/compose.yml config --quiet` | raíz | 0 | PASS — sintaxis válida |
| 4 | `docker compose -f api/compose.smoke.yml config --quiet` | raíz | 0 | PASS — sintaxis válida |
| 5 | `docker compose -f compose.yml config --quiet` | raíz | 1 | **FAIL** (esperado) — ver nota |
| 6 | `docker compose --env-file .env.example -f compose.yml config --quiet` | raíz | 0 | PASS — sintaxis válida (solo estructura, ver nota) |
| 7 | `./mvnw test` | `api/` | 0 | PASS — 289 tests, BUILD SUCCESS |
| 8 | `./mvnw test` | `agents/` | **1** | **FAIL real — ver hallazgo crítico abajo** |
| 9 | `./mvnw test -Pdemo` | `agents/` | 0 | PASS — 32 tests, BUILD SUCCESS |
| 10 | `scripts/smoke-e2e-local.sh` | raíz | — | **NO EJECUTADO — ver nota** |

## Detalle

### 1-2. `web/`

```
npm run test   → 23 passed, 23 total; 153 passed, 153 total; Time: 5.987s
npm run lint   → ✔ No ESLint warnings or errors
```

### 3-6. `docker compose config`

- `api/compose.yml` y `api/compose.smoke.yml` no requieren variables externas para validar sintaxis — ambos pasan directo.
- El `compose.yml` raíz **falla sin `.env`** (`FIREBASE_ADMIN_KEY_PATH` requerido) — esto es el comportamiento esperado en un checkout limpio, no un defecto: `.env` está gitignored intencionalmente y solo `.env.example` está trackeado.
- Para certificar que la sintaxis del YAML es válida sin crear un `.env` real en el worktree, se usó `docker compose --env-file .env.example -f compose.yml config --quiet` (exit 0). Esto valida estructura, no un boot real de servicios.

### 7. `api/` — `./mvnw test`

`Tests run: 289, Failures: 0, Errors: 0, Skipped: 0` — `BUILD SUCCESS`, 56s. Sin hallazgos.

### 8-9. `agents/` — hallazgo crítico

**`./mvnw test` (el comando documentado tal cual en `CLAUDE.md`) falla en la fase de compilación**, no en tests:

```
[ERROR] .../GeminiAssessmentGenerationAdapter.java:[7,42] package org.springframework.ai.chat.client does not exist
[ERROR] .../GroqAssessmentGenerationAdapter.java:[7,42] package org.springframework.ai.chat.client does not exist
[ERROR] .../AssessmentConfig.java:[10,42] package org.springframework.ai.chat.client does not exist
... (14 errores de compilación en total)
[ERROR] BUILD FAILURE
```

**Causa raíz confirmada:** `agents/pom.xml` declara los starters de Spring AI (`spring-ai-starter-model-google-genai`, `spring-ai-starter-model-openai`) únicamente dentro de los perfiles Maven `demo` y `beta` (líneas 65-95), no en las dependencias por defecto. El código fuente (`GeminiAssessmentGenerationAdapter.java`, `GroqAssessmentGenerationAdapter.java`, `AssessmentConfig.java`) importa esas clases incondicionalmente. Por lo tanto **cualquier invocación de `mvn`/`./mvnw` sin `-Pdemo` o `-Pbeta` no compila**, tests incluidos.

Con el perfil activado, compila y pasa limpio: `./mvnw test -Pdemo` → `Tests run: 32, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`, 8.9s.

**Esto no es solo un problema de documentación — el CI real está roto de la misma forma:**

- `.github/workflows/agents.yml` línea 35: `run: ./mvnw test` — **sin perfil**. Reproducido localmente: este paso fallaría en GitHub Actions exactamente como falló aquí (no hay `.mvn/maven.config` ni variable de entorno que inyecte un perfil por defecto).
- `agents/Dockerfile` líneas 6 y 8, en cambio, sí usa `-P${MAVEN_PROFILE}` correctamente — el defecto está aislado al paso de test del workflow, no al build de imagen.
- `CLAUDE.md` (raíz del repo, sección "Agents") documenta el comando como `./mvnw test`, sin mencionar el perfil requerido.

**Corregido el mismo día (2026-07-22), a pedido explícito del owner tras reportar este hallazgo:**

- `.github/workflows/agents.yml`: `-Pdemo` agregado tanto al paso "Run tests" (línea 35) como a "Build container image" (línea 52, `./mvnw spring-boot:build-image` — mismo defecto, ya que ese goal también dispara compilación vía `package`). `demo` se eligió porque este workflow despliega a Cloud Run, que es exactamente el rol de `demo` per `docs/99-decisions/2026-07-20-environment-roles.md` ("Google Cloud target").
- `CLAUDE.md` (raíz): comando documentado actualizado a `./mvnw test -Pdemo` (y `spring-boot:run -Pdemo`), con una nota explicando por qué el perfil es obligatorio.
- Verificado de nuevo tras el fix: `./mvnw test -Pdemo` en `agents/` → `Tests run: 32, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS` (re-ejecutado, mismo resultado que la primera corrida).

### 10. `scripts/smoke-e2e-local.sh` — no ejecutado

No se pudo ejecutar en esta sesión, por dos razones reales y verificadas, no por omisión:

1. **Sin secretos reales en este worktree:** el script requiere un `.env` raíz con `INTERNAL_API_SECRET` y `NEXT_PUBLIC_FIREBASE_API_KEY` reales, y `agents/.env` con un `GRADEOPS_GROQ_API_KEY` real. Este worktree (`gradeops-plan`) no tiene ninguno de los dos — solo existen los `.env.example` con placeholders.
2. **Conflicto de puertos con un stack ya corriendo:** el script requiere `docker compose up -d db api agents` corriendo en este mismo directorio. Ya hay un stack Docker activo (proyecto `grade-ops-ai`, iniciado desde `/home/carlos/projects/grade-ops-ai`, el checkout hermano del que este worktree cuelga) ocupando los puertos `5432`, `8080`, `8081` y `3000` — los mismos que `compose.yml` mapea sin parametrizar. Levantar un segundo stack desde este worktree fallaría por bind conflict, y detener el stack ajeno para liberar puertos no se hizo por no ser parte de lo solicitado y por no saber si el usuario lo está usando activamente.

Esto no invalida la certificación de Story 04: ese story ya documentó, con evidencia propia (SHA/fecha distintos, ver `02-deepening/story-04-e2e-integration-verification/`), una corrida real y exitosa de este mismo flujo contra un stack levantado específicamente para esa tarea. Esta sesión no repite esa corrida — sería redundante y, en este entorno concreto, no ejecutable sin afectar el stack de otro checkout.

## Fallas reales encontradas (resumen)

1. **Crítico — corregido el mismo día:** `.github/workflows/agents.yml` (líneas 35 y 52) y `CLAUDE.md` documentaban/ejecutaban `./mvnw test`/`spring-boot:run`/`spring-boot:build-image` para `agents/` sin perfil Maven — no compilaba. Requiere `-Pdemo` o `-Pbeta`. Reproducido de forma determinística (falla sin perfil, pasa con perfil), corregido con `-Pdemo` en ambos archivos, y re-verificado tras el fix.
2. Sin fallas nuevas en `web/`, `api/`, ni en la sintaxis de los tres `compose.yml`.
