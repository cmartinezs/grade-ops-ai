En este worktree el foco debería ser cerrar coherentemente:

  - .planning/active/008-assessment-creation
  - web/.planning/active/001-assessment-creation

  Serie concreta de tareas

  1. Inventariar plannings abiertos
      - Confirmar qué está en active/, qué stories/tasks figuran como DONE, TODO, SKIPPED o stale.
      - Salida: tabla de estado real vs estado documentado.

  2. Reconciliar Web 001
      - Actualizar índices, README, traceability y status.
      - Dejar claro que Story 02 está cerrada y que los gaps futuros pertenecen al Master Plan, no a este planning.
      - Decidir si Story 01 queda SKIPPED o documentada como reemplazada.

  3. Reconciliar parent 008
      - Alinear el planning padre con los child plannings reales.
      - Actualizar Story 03 con evidencia de Web.
      - Revisar Stories 01, 02 y 04 para marcarlas cerradas solo si tienen evidencia suficiente.
      - Story 05 debe cerrarse como DONE solo si la validación existente ya la soporta; si no, marcarla como SKIPPED/SUPERSEDED con motivo explícito.

  4. Limpiar residuos documentales activos
      - Resolver pdr-NNN-title.md.
      - Corregir headings genéricos de traceability.
      - Completar o retirar placeholders en SMOKE-TESTS.md activos.
      - No tocar templates originales del plugin.

  5. Definir política branch/CI mínima
      - Resolver si los workflows actuales deben escuchar develop o master.
      - Esto es pre-master-plan porque afecta la capacidad de certificar cierre, no una feature nueva.

  6. Certificar baseline existente
      - Ejecutar solo suites existentes: Web, API, Agents, compose config y smoke actual.
      - Registrar SHA, comandos, fecha, exit codes y fallas reales.
      - No crear suites nuevas ni meter testing strategy aquí.

  7. Revisar seguridad/configuración mínima
      - Confirmar que no hay secretos reales versionados.
      - Documentar fixtures fake permitidos.
      - Dejar evidencia de que beta/demo/local no están mezclando credenciales.

  8. Actualizar retrospectivas y DoD
      - Cada planning abierto debe tener cierre explícito: qué se hizo, qué no, qué se difiere.
      - Los pendientes diferidos deben apuntar al Master Plan como entrada futura, no como deuda abierta del planning actual.

  9. Archivar plannings cerrados
      - Mover web/.planning/active/001-assessment-creation a web/.planning/finished/....
      - Mover .planning/active/008-assessment-creation a .planning/finished/....
      - Actualizar índices active y finished.

  10. Emitir decisión de entrada

  - Crear/actualizar un documento tipo master-plan-entry-decision.
  - Resultado esperado: GO, CONDITIONAL GO o NO-GO.
  - Si queda CONDITIONAL GO, debe tener dueños y razones concretas, no “pendientes genéricos”.

  El criterio duro: después de estas tareas no debería quedar ningún planning viejo “medio abierto” por incoherencia documental. Lo que sea futuro, transversal o estratégico entra como input del Master
  Plan, no como trabajo pendiente del pre-master-plan.

---

## Tareas de ejecución (inventario real, verificado 2026-07-22)

No es planificación formal (no se crea `.planning/` nuevo para esto). Es la checklist ejecutable derivada de inspeccionar directamente `.planning/active/008-assessment-creation`, `web/.planning/active/001-assessment-creation`, los workflows de CI y el estado de secretos/config. Cada bloque corresponde al paso homónimo de la lista original.

### 1. Inventario — estado real vs documentado

**Root `008-assessment-creation`**

| Story | Título | Status documentado | Evidencia real | Brecha |
|---|---|---|---|---|
| 01 | agents-assessment-agent-coordination | IN PROGRESS | Child `agents/.planning` story-01 = DONE, checkpoint 3 (api integra) correctamente TODO | Ninguna — consistente |
| 02 | api-assessment-creation-coordination | IN PROGRESS | Child `api/.planning/finished/003-assessment-creation` story-01 = DONE, checkpoint 4 (web integra) correctamente TODO | Ninguna — consistente |
| 03 | web-assessment-creation-coordination | TODO | Child web story-01 = SKIPPED, story-02 = DONE (15/15 tasks, evidencia sólida) — pero el archivo coordinador nunca menciona story-02 | **Desactualizado**: sigue referenciando solo la story `assessment-creation-ui` (ya SKIPPED/absorbida) |
| 04 | e2e-integration-verification | DONE | `compose.yml` real, scripts de smoke ejecutables, evidencia con output real de comandos | Ninguna — la mejor evidenciada |
| 05 | automated-cross-service-test-suite | TODO | Nunca atomizada; tabla de tasks enlaza a `story-05-automated-cross-service-test-suite/task-0{1..4}-*.md`, carpeta inexistente | Enlaces muertos, cero trabajo real hecho |

**Web `001-assessment-creation`**

| Story | Título | Status documentado | Evidencia real | Brecha |
|---|---|---|---|---|
| 01 | assessment-creation-ui | SKIPPED (correcto en `01-expansion.md` y en el story file, con fecha y motivo) | PR #64 mergeado solo trae docs/tooling, cero código de producto | Consistente en la story, pero `web/.planning/active/README.md` sigue mostrando `TODO` |
| 02 | assessment-screens-wireframes-and-data-providers | DONE | 15/15 tasks DONE, cada uno con output de comando verificable (tests, lint, e2e), PRs #73–#89 reales, código real en `src/features/assessment-creation/` | DONE es correcto para el scope original; la propia story-02 abre un residual `R-POST-01` (Open) pidiendo revalidación contra gates del Master Plan posteriores al cierre |

### 2. Reconciliar Web 001 — ✅ hecho (2026-07-22)

- [x] Actualizar `web/.planning/active/README.md`: cambiar Story 01 de `TODO` a `SKIPPED`, agregar fila de Story 02 (`DONE`) — hoy no aparece en absoluto.
- [x] Actualizar `web/.planning/active/001-assessment-creation/README.md` → sección "Current State": marcar como resueltos los 3 checkboxes que hoy están sin marcar (`Story is DONE or intentionally SKIPPED`, `Traceability is complete`, `Retrospective is complete`) una vez completadas las tareas de abajo. (También se corrigió la línea de "Expansion story is dimensioned", que seguía hablando de 1 sola story sin atomizar.)
- [x] Redactar la sección "Retrospective" de ese mismo README (`### Outcomes` / `### Deviations` / `### Follow-ups` / `### Lessons`) sintetizando desde `RETROSPECTIVE-RAW.md` (que sí tiene contenido real fechado) — hoy son placeholders con corchetes sin editar.
- [x] Sincronizar `TRACEABILITY.md` → tabla "Residuals": agregar `R-POST-01` (hoy dice `*None*` pero `story-02-...md` sí lo lista como `Open`).
- [x] Agregar una frase explícita en el README de cierre (no solo en el residual de story-02) del tipo: "gaps post-cierre (i18n, UI-data-semantics, testing gates) quedan como input del Master Plan, no como deuda abierta de este planning" — agregada como línea de apertura de la sección Follow-ups.
- [x] Decidir explícitamente el estado final de Story 01: queda `SKIPPED` (ya estaba así, correcto) — el índice `active/README.md` ahora lo refleja. No se redocumentó como "reemplazada" porque su alcance fue absorbido, no reemplazado 1:1; la nota ya vivía en el propio story file y se reforzó en el Retrospective.

### 3. Reconciliar parent 008 — ✅ hecho (2026-07-22)

- [x] Actualizar `02-deepening/story-03-web-assessment-creation.md`: cambiar `Status: TODO` reflejando la realidad (child story-01 SKIPPED, story-02 DONE), marcar Sync Checkpoint #3 y los Done Criteria correspondientes, y reemplazar toda referencia a la story ya inexistente `assessment-creation-ui` por `story-02-assessment-screens-wireframes-and-data-providers`. Story 03 quedó `DONE`.
- [x] Actualizar `01-expansion.md` línea 81 (tabla "Linked Child Plannings"): reemplazado con el estado real (Story 01 SKIPPED, Story 02 DONE). También se corrigió la fila de `api/`, que seguía afirmando que la reachability cross-service "no está probada", contradiciendo a Story 04 (ya DONE en la misma tabla).
- [x] Story 05 (`automated-cross-service-test-suite`): marcada **SKIPPED** con motivo explícito (nunca se atomizó, enlaces a 4 archivos inexistentes) — no se cerró como DONE. Se limpiaron los enlaces muertos de la tabla de tasks (quedaron como texto plano) y se agregó una sección Residuals apuntando el trabajo diferido al Master Plan (`testing-strategy.md`).
- [x] **Hallazgo no anticipado en el punto 4 original:** cerrar Story 03 cierra en cascada el checkpoint 3 de Story 01 (endpoint de agente alcanzable desde `api/` — ya probado por Story 04, real, sin mocks) y el checkpoint 4 de Story 02 (integración `web/`→`api/` — ya probada por web/story-02, real, sin mocks). Ambas coordination stories cumplían su propio criterio ("status DONE solo tras confirmación del child") una vez reconciliado el estado real. Se cambiaron **Story 01 y Story 02 de `IN PROGRESS` a `DONE`**, con evidencia citada en cada checkpoint. También se actualizó el Risk Register de `01-expansion.md`: R-04 → Mitigated (Story 04 DONE); R-05/R-06 → Largely mitigated con seguimiento en `R-POST-01`; R-07 (i18n) se dejó explícitamente `Open` porque no está probado, solo se referenció `R-POST-01`.

### 4. Limpiar residuos documentales activos — ✅ hecho (2026-07-22)

- [x] `pdr-NNN-title.md` en `.planning/active/008-assessment-creation/`: era copia sin editar del template, sin contenido de decisión real — no surgió ninguna decisión formal digna de PDR al reconciliar Story 03/05 (las decisiones ya viven en `TRACEABILITY.md` → Decisions Made D-01…D-05). **Eliminado.**
  - Nota aparte (fuera de alcance, no tocada): el mismo residuo sin editar sigue existiendo en `.planning/finished/001-teacher-onboarding/`, `004-subpage-identity/`, `005-design-template/`, `006-password-recovery/` — candidato a limpieza futura, no parte de este cierre.
- [x] `TRACEABILITY.md` de 008: heading corregido a `# 🔗 Traceability: 008-assessment-creation`.
- [x] `SMOKE-TESTS.md`: decisión tomada = **(b)**. Se dejó constancia explícita en el README de 008 (sección Follow-ups) de que la certificación de baseline de esta planning se hizo manualmente vía `scripts/smoke-e2e-local.sh` / `scripts/smoke-e2e-render-beta.sh` (evidencia real en los task files de Story 04), y que formalizar el `SMOKE-TESTS.md` global queda como input del Master Plan. El archivo global permanece sin llenar intencionalmente — no es deuda de este planning.
- [x] README de 008 → Retrospective redactada completa (Outcomes/Deviations/Follow-ups/Lessons) desde `RETROSPECTIVE-RAW.md` y la evidencia de las tareas 1-3. Los 3 checkboxes pendientes de "Current State" quedaron marcados (Stories DONE/SKIPPED, Traceability completa, Retrospective completa); también se corrigió la línea de dimensionamiento, que seguía hablando de solo 3 stories cuando hoy son 5.
- [x] No se tocó `.planning/_template/pdr-NNN-title.md` ni ningún otro archivo bajo `_template/`.

### 5. Política branch/CI mínima — ✅ hecho (2026-07-22)

Hallazgo original: los 3 workflows existentes (`.github/workflows/api.yml`, `agents.yml`, `deploy.yml`) disparan sobre `master`, mientras `develop` es el branch por defecto de GitHub (356 commits adelante) y el planning system ya asumía `base_branch: develop`. Se planteó como posible inconsistencia a corregir.

**Aclaración del owner del proyecto (2026-07-22):** no es una inconsistencia — es el gitflow intencional del repo. `develop` es la rama de integración donde nace todo el trabajo (feature/story branches); `develop` se promueve a `master` cuando hay una versión estable, y por eso el CI de despliegue a Google Cloud dispara sobre `master`. Falta una rama `beta` dedicada para Vercel/Render (hoy el ambiente beta de Render rastrea `develop` directamente como parche post-incidente), pero definir esa rama es alcance del Master Plan, no de este cierre.

- [x] Decidir explícitamente la política: **confirmada** — `develop` = integración/story branches (ya reflejado en `.planning/config.yml`, `web/`, `agents/`), `master` = estable/productiva y target de CI a GCloud. No se trata de un error a corregir.
- [x] Alinear `.github/workflows/api.yml`, `agents.yml`: **sin cambios** — ya disparan correctamente sobre `master`, consistente con la política confirmada.
- [x] Revisar `deploy.yml`: **sin cambios** — target `master` es intencional y consistente con el mismo criterio (solo lo estable se publica).
- [x] Crear `api/.planning/config.yml` con `base_branch: develop` — era el único de los cuatro workspaces de planning sin este archivo; creado, consistente con root/web/agents.
- [x] Documentar el gap real detectado (falta de rama `beta` dedicada) como input del Master Plan: creado `docs/master-plan/branching-environment/README.md` (stub simple) con la política confirmada, el gap de la rama `beta`, y referencias al incidente de Render y a `beta-environment-design.md`. Referenciar este documento al redactar la decisión de entrada al Master Plan (paso 10).

### 6. Certificar baseline existente — ✅ hecho (2026-07-22)

- [x] Ejecutado y registrado (SHA, comando, fecha, exit code) en [`.planning/active/008-assessment-creation/BASELINE-CERTIFICATION-2026-07-22.md`](../../../.planning/active/008-assessment-creation/BASELINE-CERTIFICATION-2026-07-22.md): `web` (`npm run test` PASS 153/153, `npm run lint` PASS), `api` (`./mvnw test` PASS 289/289), `agents` (`./mvnw test` **FAIL real** — ver hallazgo abajo), `docker compose config` (los 3 archivos, sintaxis válida), `scripts/smoke-e2e-local.sh` (no ejecutado, motivo documentado).
- [x] No se crearon suites nuevas ni testing strategy — solo se ejecutaron y documentaron los comandos ya existentes en `CLAUDE.md`.
- [x] Evidencia adjunta como archivo dedicado dentro de 008 (no solo en la conversación), enlazado desde `README.md` → Key Links y mencionado en Follow-ups.
- [x] **Hallazgo crítico no anticipado — corregido el mismo día a pedido del owner:** `./mvnw test` en `agents/` (el comando literal documentado en `CLAUDE.md`) **no compilaba** — los starters de Spring AI solo estaban declarados en los perfiles Maven `demo`/`beta` de `agents/pom.xml`, pero el código los importa incondicionalmente. `.github/workflows/agents.yml` tenía el mismo defecto en 2 pasos: "Run tests" (línea 35) y "Build container image" (línea 52, `spring-boot:build-image` también dispara compilación). `agents/Dockerfile` sí usaba `-P${MAVEN_PROFILE}` correctamente — el defecto estaba aislado al CI workflow y a `CLAUDE.md`. **Fix aplicado:** `-Pdemo` agregado a ambos pasos de `agents.yml` (elegido porque este workflow despliega a Cloud Run = rol `demo` per `docs/99-decisions/2026-07-20-environment-roles.md`) y a los 3 comandos documentados en `CLAUDE.md` (con nota explicando por qué el perfil es obligatorio). Re-verificado tras el fix: `./mvnw test -Pdemo` → 32/32, BUILD SUCCESS.
- [x] `scripts/smoke-e2e-local.sh` no ejecutado: sin `.env`/`agents/.env` con secretos reales en este worktree, y puertos 5432/8080/8081/3000 ya ocupados por un stack Docker corriendo desde el checkout hermano `/home/carlos/projects/grade-ops-ai` (no se tocó). No invalida la certificación ya hecha por Story 04 con su propia evidencia.

### 7. Revisar seguridad/configuración mínima — ✅ hecho (2026-07-22)

Evidencia completa en [`.planning/active/008-assessment-creation/SECURITY-BASELINE-2026-07-22.md`](../../../.planning/active/008-assessment-creation/SECURITY-BASELINE-2026-07-22.md).

- [x] `api/smoke/fake-service-account.json`: **el hallazgo original era un falso negativo** — `api/smoke/README.md` ya existía y ya documentaba el archivo con precisión (throwaway key, por qué es seguro commitearlo, mecanismo del emulador, advertencia explícita "Never use in demo/beta/production"). No se requirió ningún cambio; se dejó registrado que la tarea 1 (inventario) no lo detectó, para no repetir el error.
- [x] Confirmado que no hay `.env` real trackeado — solo `.env.example`, `agents/.env.example`, `web/.env.local.example`, y `.gitignore` excluye correctamente `.env`/`.env.*`.
- [x] Documentado como aceptado (riesgo nulo, solo local) que `compose.yml`/`api/compose.yml`/`api/compose.smoke.yml` repiten `POSTGRES_DB/USER/PASSWORD: gradeops` y que `INTERNAL_API_SECRET` cae a `dev-secret-change-me` en 2 servicios + `.env.example` — con nota explícita de que esto no debe extenderse a `beta`/`demo`.
- [x] Confirmado que `application-beta.yml`/`application-demo.yml` en `api/` y `agents/` no tienen valores hardcodeados (solo `${ENV_VAR}`), usan buckets distintos (R2 `gradeops-beta` vs GCS `gradeops-demo`), y en `agents/` incluso mecanismos de auth distintos para Gemini (API key vs Vertex AI/ADC) — sin mezcla posible entre ambientes.

### 8. Actualizar retrospectivas y DoD — ✅ hecho (2026-07-22, verificado — sin cambios nuevos necesarios)

Esta tarea ya quedó satisfecha como efecto de ejecutar bien las tareas 2, 3, 4, 6 y 7. Se releyeron ambos README completos el 2026-07-22 para confirmarlo explícitamente, en vez de asumirlo:

- [x] 008: README "Retrospective" completa (Outcomes/Deviations/Follow-ups/Lessons) y Current State con los 5 checkboxes marcados — hecho en la tarea 4, reconfirmado aquí.
- [x] web 001: README "Retrospective" completa, Current State con los 5 checkboxes marcados, y frase explícita sobre pertenencia al Master Plan como apertura de Follow-ups — hecho en la tarea 2, reconfirmado aquí.
- [x] Todos los pendientes diferidos verificados como explícitamente etiquetados "input del Master Plan" en el Retrospective correspondiente: `R-POST-01` (ambos README, vía Story 03 en 008), Story 05 (008), `SMOKE-TESTS.md` global (008), docker build roto de `web/` (008), mismatch de Residual #1 en `agents/` (008). El hallazgo de CI de `agents.yml`/`CLAUDE.md` (tarea 6) aparece tachado con nota "Fixed same day", no como pendiente — correcto, ya no es deuda.
- [x] Confirmado que no existe una sección "DoD" separada en el template del plugin (`.planning/_template/`) — el "DoD" del título de esta tarea se refiere a los Done Criteria por story, ya corregidos en la tarea 3.
- [x] Confirmado deliberadamente que el hallazgo de la rama `beta` (tarea 5) **no** se agregó a ninguna de las dos retrospectivas — no pertenece al scope de 008 ni de web/001, ya vive en `docs/master-plan/branching-environment/README.md`.

### 9. Archivar plannings cerrados — ✅ hecho (2026-07-22)

Ejecutado con el skill `claude-planning-with-ai:plan-archive` (script determinista `planning-mutate.mjs archive`), no a mano — dry-run auditado y aprobado antes de aplicar en ambos casos.

- [x] `web/.planning/active/001-assessment-creation/` → `web/.planning/finished/001-assessment-creation/`. Auditoría `pass`; advertencias de TRACEABILITY.md revisadas y descartadas como falso positivo del heurístico del script (detecta los brackets de links markdown `[← planning/README.md]` como si fueran placeholders `[fill in]`; verificado sin `TODO`/`[fill` real en el archivo — las "celdas vacías" son la columna `W` intencionalmente en blanco según la leyenda del propio documento).
- [x] `web/.planning/README.md` actualizado automáticamente por el script: 001 movido de "🚧 In Progress" a "✅ Completed"; `web/.planning/finished/README.md` poblado. Se corrigió a mano un residuo cosmético que dejó el script en `web/.planning/active/README.md` (párrafo huérfano + tabla vacía sin planning) → reemplazado por `*(none yet)*`.
- [x] `.planning/active/008-assessment-creation/` → `.planning/finished/008-assessment-creation/`. Auditoría `pass`; advertencias revisadas: mismo falso positivo de TRACEABILITY.md, y "story-01-agents-assessment-agent.md has open inconsistencies" — es el ítem #3 ya conocido (mismatch Gemini/Groq en el child de `agents/`), ya documentado como "Not blocking" en su propia Resolution Path y ya listado como Follow-up desde la tarea 4; no bloqueante, no editable desde este workspace.
- [x] `.planning/README.md` actualizado: 008 movido a "✅ Completed" con el formato `(N stories, COMPLETED <fecha>)` usado por 001–007/009, insertado en orden numérico entre 007 y 009 (el script lo había appendeado al final, fuera de orden — corregido a mano). `.planning/finished/README.md`: fila de 008 agregada dentro de la tabla `Planning | Completed | Intent | Stories` (el script la había dejado como bullet suelto después del footer de navegación — corregido a mano, con `4 DONE + 1 SKIPPED (5 total)` en la columna Stories, reflejando que Story 05 quedó SKIPPED, no DONE).
- [x] Ambas carpetas verificadas movidas (`active/` solo con `README.md`, `finished/` con la carpeta completa) y con `Current status: Completed` / `Completed: 2026-07-22` seteados en sus README.

### 10. Emitir decisión de entrada — ✅ hecho (2026-07-22, con una corrección en el camino)

- [x] Creado `docs/99-decisions/2026-07-22-master-plan-entry-decision.md`, siguiendo el estilo actual de los ADR más recientes de la carpeta (Estado/Contexto/Resultado/Decisión/Consecuencias/Fuentes — el `adr-template.md` original quedó superado por esa convención más reciente, usada por los 5 ADR del 2026-07-21). Agregada la fila correspondiente en `docs/99-decisions/README.md` → Active Decision Records.
- [x] Primera versión: **CONDITIONAL GO** con una condición sobre el docker build roto de `web/` (`@tailwindcss/oxide` sobre `node:18-alpine`), basada en la retrospectiva de Story 04 (2026-07-15) sin cruzarla contra `git log` actual.
- [x] **Corregido tras aviso del owner:** ese fix ya estaba mergeado — commit `2c73156` ("fix(web): bump Dockerfile base image to node:24-alpine"), PR #91, 2026-07-21, un día antes de que empezara este pre-master-plan. Verificado con evidencia directa: `npm run build` en `web/` compila limpio bajo Node 24 (el error de binding nativo ya no ocurre; el build solo falla después en el prerendering de `/verify-email` por falta de credenciales Firebase reales en este worktree, mismo patrón ya documentado en las tareas 6-7, no relacionado).
- [x] **Resultado final: GO**, sin condiciones bloqueantes. La condición retirada quedó documentada de forma transparente (no reescrita en silencio) en la propia decisión, en `.planning/finished/008-assessment-creation/README.md` (Deviations, Follow-ups y una nueva Lesson sobre verificar contra `git log` antes de citar una retrospectiva vieja como estado actual), y en `docs/99-decisions/README.md`.