# Roadmap incremental

## Fase 0 — Baseline y gobierno

- Inventariar suites y medir duración/cobertura/flakiness.
- Adoptar taxonomía, naming y comandos canónicos.
- Definir ownership de suites y política de datos/secretos.
- Alinear estrategia de ramas (`develop`/`master`).

**Salida:** baseline publicado y ninguna suite existente perdida.

## Fase 1 — Calidad por artefacto

- Jest coverage para Web.
- JaCoCo + Surefire/Failsafe para API/Agents.
- PostgreSQL/Flyway en integración API.
- JUnit XML y summaries uniformes.
- SonarQube local reproducible e importación de LCOV/JaCoCo.
- Quality gate inicial sobre código nuevo, calibrado desde el baseline real.

**Gate:** unit/component + coverage sin regresión.

## Fase 2 — Contratos y testkit

- OpenAPI/JSON Schema versionados.
- Fixtures y simuladores compartidos.
- Compatibility checks consumidor/proveedor.
- Perfiles de test con fail-fast fuera de CI/test.

**Gate:** ningún breaking change no declarado.

## Fase 3 — Aceptación aislada

- Web + API mock + Firebase controlado.
- API + PostgreSQL + mocks Firebase/Agents/externos.
- Agents + GenAI mock.
- Imágenes reales, healthchecks y artifacts.

**Gate:** cada artefacto desplegable prueba su frontera pública.

## Fase 4 — Integración Compose

- `api-agents`.
- `web-api`.
- `full-chain`.
- Orquestación, teardown y diagnóstico de fallos.

**Gate:** journeys transversales deterministas en PR/nightly.

## Fase 5 — Funcional local real

- Firebase y GenAI aislados.
- Guardas anti-cruce y límites de costo.
- Smoke real y guía operativa.

**Gate:** ejecución manual reproducible, nunca requisito de PR.

## Fase 6 — Beta/demo y promoción

- Smoke post-deploy por plataforma.
- Imágenes inmutables y promoción por digest.
- Rollback, soak y evidencia de release.
- Dataset dorado y `ai-eval` separado.

## Fase 7 — Tendencias/dashboard

- `run-manifest` y `summary` versionados.
- Ingestión histórica.
- Dashboard de calidad, flakiness, coverage y promoción.
- Ingestión de snapshots Sonar y resultados JMeter normalizados.

## Fase 8 — Rendimiento y capacidad

- Planes JMeter versionados para API, Agents y `full-chain`.
- Smoke breve en CI; baseline nocturno sobre Compose.
- Carga controlada en Beta con datos sintéticos y límites de costo.
- Smoke/soak excepcional en Demo, con autorización y abortado automático.
- Lighthouse/Playwright para rendimiento percibido de Web.

**Gate:** no existen regresiones relevantes frente al baseline aprobado y cada release conoce su capacidad operativa medida.

Cada fase que modifique CI, secretos o recursos debe incluir tareas correspondientes en `.github/` e `infra/`.
