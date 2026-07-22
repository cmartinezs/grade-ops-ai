# Branching & Environment Strategy — stub

> Estado: **placeholder**, creado durante la limpieza pre-master-plan (`docs/.prompting/pre-master-plan/clean.md`, tarea 5, 2026-07-22). No es un documento de estrategia completo — es un punto de partida para que el Master Plan lo profundice cuando corresponda.

## Política confirmada (2026-07-22)

Gitflow del repo, confirmado por el owner del proyecto:

- **`develop`** es la rama por defecto y de integración. Todo el trabajo (features, stories de planning) nace desde acá — coherente con `base_branch: develop` ya declarado en `.planning/config.yml`, `web/.planning/config.yml`, `agents/.planning/config.yml` y (desde esta misma tarea) `api/.planning/config.yml`.
- **`master`** es la rama estable/productiva. `develop` se promueve a `master` cuando corresponde declarar una versión estable. Por eso el CI de despliegue a Google Cloud (`.github/workflows/api.yml`, `.github/workflows/agents.yml`) dispara sobre `master`, no sobre `develop` — es intencional, no una inconsistencia a corregir.
- `.github/workflows/deploy.yml` (GitHub Pages, despliega `site/`) también dispara sobre `master`, consistente con el mismo criterio: solo lo estable/productivo se publica.

**Conclusión de la tarea 5 del pre-master-plan:** no se modificaron los workflows de CI. Lo único corregido fue la inconsistencia real detectada — `api/.planning/` era el único de los cuatro workspaces de planning sin `config.yml`/`base_branch` declarado.

## Gap identificado — pendiente de diseño en el Master Plan

Hoy no existe una rama `beta` dedicada. El ambiente `beta` (Render) se despliega actualmente rastreando `develop` directamente (corregido manualmente el 2026-07-15 tras un incidente donde el servicio de `agents/` en Render quedó apuntando a `master` y sirvió código de ~1 mes de antigüedad — ver `.planning/finished/008-assessment-creation/02-deepening/story-04-e2e-integration-verification/task-03-verify-render-beta-live.md`).

Rastrear `develop` directamente funciona como parche, pero no es el diseño deseado a largo plazo: cada commit a `develop` dispara el ambiente beta, sin ningún punto de corte intermedio. El owner del proyecto indicó que debería existir una rama `beta` propia para los despliegues de Vercel/Render, separada de `develop`.

Preguntas a resolver cuando el Master Plan aborde esto (no se resuelven en este pre-master-plan):

- ¿Cuándo y cómo se corta `beta` desde `develop`? ¿Manual, por release candidate, por cadencia fija?
- ¿Cómo se promueve `beta` → `master`? ¿Mismo criterio que `develop` → `master`, o uno propio?
- ¿Vercel/Render deben quedar configurados para auto-deploy sobre `beta` en vez de `develop`, una vez que la rama exista?
- Reconciliar esta definición con `docs/04-architecture/beta-environment-design.md` y `docs/99-decisions/2026-07-20-environment-roles.md`, que documentan el ambiente `beta` pero no la rama que lo alimenta.

## Referencias

- `docs/.prompting/pre-master-plan/clean.md` — tarea 5, hallazgos e investigación original.
- `docs/04-architecture/beta-environment-design.md` — diseño del ambiente beta (Render).
- `docs/99-decisions/2026-07-20-environment-roles.md` — roles de `demo` vs `beta`.
- `.planning/finished/008-assessment-creation/02-deepening/story-04-e2e-integration-verification/` — evidencia del incidente de branch tracking en Render y su corrección manual.
