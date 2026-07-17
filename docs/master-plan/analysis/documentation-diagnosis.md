# Diagnóstico de documentación — GradeOps AI

> Fase 01 — Descubrimiento y diagnóstico del Master Plan Ejecutivo.
> Este documento **no** define releases, prioridades ni prompts `/release-*`. Solo diagnostica.

## Resumen ejecutivo

GradeOps AI tiene una documentación de negocio y producto extensa (~180 archivos Markdown en `docs/`, excluyendo `.raw/` y `.all-by-category/`) y un nivel de implementación real muy superior a lo que el propio repositorio declara de sí mismo. El hallazgo central de esta fase es que **existen tres pares de "verdad declarada vs. verdad operativa" sin reconciliar**:

1. **Modo de producto**: `00-project/` (capa canónica según su propio README) describe un producto de un solo modo (Open, 8 agentes); `02-product/`, `03-ai-agents/`, `CLAUDE.md` y el propio ADR `2026-06-10-closed-assessment-mode.md` describen dos modos (Open + Closed, 13 agentes). El ADR existe y debería primar, pero `00-project/` nunca se actualizó tras esa decisión — y el propio corte de historias P0 del hackathon (`02-product/user-stories.md`) excluye las 3 épicas del modo Closed pese a que `workflows.md`/`mvp-scope.md` las marcan P0.
2. **Proveedor de IA**: toda la documentación de decisión (`99-decisions/2026-06-10-technology-stack.md`, `agent-runtime-separation.md`) y `CLAUDE.md` raíz fijan Vertex AI Gemini como único proveedor. El código real (`agents/`) tiene Groq como proveedor **por defecto**, ya implementado, testeado y con dos plannings cerradas (`agents/.planning/finished/002-groq-genai-provider`, root `.planning/finished/009-groq-infra-provisioning`). No existe ADR para esta decisión ya tomada y ejecutada.
3. **Entorno de despliegue**: `CLAUDE.md` raíz, `04-architecture/system-architecture.md` y `07-hackathon/*` describen exclusivamente el entorno `demo` (GCP Cloud Run + Cloud SQL). La retrospectiva de `009-groq-infra-provisioning` confirma que **ese entorno nunca ha sido desplegado** (`terraform apply` nunca ejecutado contra GCP real). El único entorno con evidencia real de funcionamiento end-to-end (flujo brief→generate con Groq, verificado 2026-07-16/17) es `beta` (Render + Vercel + Neon), documentado solo en `04-architecture/beta-environment-design.md` y completamente ausente de `CLAUDE.md` raíz. Dado que las reglas del hackathon (`00-project/hackathon-strategy.md`) exigen explícitamente Google Cloud + Gemini API, esta es la discrepancia de mayor riesgo detectada.

Fuera de estos tres ejes, la documentación de negocio (00-project, 01-business) es madura y internamente consistente (salvo por el punto 1), el modelo de agentes (03-ai-agents) es completo y uniforme (13/13 documentados), y la guía de desarrollador (09-developer-guide) está sistemáticamente desfasada respecto al código real — repite tres veces la afirmación falsa de que `agents/` es "scaffolding sin lógica implementada".

El estado general es: **documentación de intención sólida, documentación de estado real obsoleta**. El repositorio ha ejecutado bastante más de lo que declara haber ejecutado.

## Fuentes revisadas

**Documentación (`docs/`, excluyendo `.raw/` y `.all-by-category/`):**
`00-project/*` (9), `01-business/*` (6), `02-product/*` nivel superior (10) + `02-product/user-stories/` (62 historias en 13 épicas + out-of-scope + README + template), `03-ai-agents/*` (15), `04-architecture/*` (8), `05-evidence/*` (6), `06-ux/*` (4), `07-hackathon/*` (4), `08-user-guide/*` (8), `09-developer-guide/*` (11), `10-best-practices/*` (9), `99-decisions/*` (16, incluye 13 ADRs fechados), `CLAUDE.md` (raíz y `docs/`), `README.md` (raíz y `docs/`), `.github/copilot-instructions.md`, `master-plan-specification.md`.

**Código y estado de ejecución real (contraste):**
- `api/`: árbol de paquetes (`cl.gradeops.ai.api`), 12 migraciones Flyway (V1–V12), 4 `@RestController`, 6 `@Entity`, 55 archivos de test.
- `agents/`: árbol de paquetes (`cl.gradeops.ai.agents`), adaptadores Gemini y Groq, prompts `.st`, 10 archivos de test.
- `web/`: rutas Next.js (`src/app`), design system, cliente Firebase.
- `infra/`: 13/14 archivos `.tf` (incluye `groq.tf`, `firebase_*`, `identity_platform.tf`).
- `.planning/` (raíz) y `.planning/` de `api/`, `agents/`, `web/`: 8 plannings cerradas + 1 activa en la raíz; plannings cerradas y activas por servicio hijo.
- Verificación en vivo reportada por planning `009-groq-infra-provisioning`: infraestructura GCP del entorno `demo` nunca aplicada; entorno `beta` en Render confirmado operativo al 2026-07-16/17.

## Estado general de la documentación

- **Negocio y estrategia (00-project, 01-business)**: madura, bien argumentada, con principios explícitos y no-negociables claros. Su debilidad no es de calidad sino de **actualización**: quedó congelada en el momento anterior a la incorporación del modo Closed (ADR del 2026-06-10) y nunca se sincronizó después.
- **Producto (02-product)**: es la capa más actualizada y la única que trata ambos modos con paridad real (mvp-scope, workflows, assessment-modes). Es también donde vive la ambigüedad de prioridad P0 del modo Closed.
- **Agentes (03-ai-agents)**: completa y homogénea — 13/13 agentes documentados con contrato de entrada/salida, reglas de calidad, control humano y logging. Es el documento de mejor calidad estructural del corpus.
- **Arquitectura (04-architecture)**: internamente fragmentada — coherente y Gemini-céntrica en la mayoría de los archivos, pero con un documento aislado (`beta-environment-design.md`) que ya reconoce el drift de Groq/Render sin que esa información se haya propagado al resto.
- **Evidencia (05-evidence)**: son plantillas vacías, correctas para esta etapa temprana, pero con un modelo de campos más pobre que el que ya usan `06-ux` y `08-user-guide` para el mismo concepto (log de agente).
- **UX (06-ux)**: consistente internamente y con `08-user-guide`; distingue correctamente P0/P1/P2 por pantalla.
- **Guía de usuario (08-user-guide)**: escrita en tono 100% "producto terminado", sin ningún disclaimer de funcionalidad futura, pese a describir features P1/P2 (Recovery Agent, Learning Gap Agent, exportación, landing/pricing) como si ya operaran.
- **Guía de desarrollador (09-developer-guide)**: la carpeta más desfasada del corpus — tres archivos distintos repiten independientemente que `agents/` no tiene lógica implementada, lo cual es falso.
- **Buenas prácticas (10-best-practices)**: genérica, transferible, no específica de GradeOps AI; irónicamente advierte sobre el mismo patrón de drift documental que se encontró en 09-developer-guide.
- **Decisiones (99-decisions)**: 13 ADRs, todos en estado "Accepted", ninguno formalmente contradicho entre sí — pero dos decisiones de facto importantes (Groq, entorno beta) nunca se registraron como ADR pese a estar ya implementadas.
- **User stories (02-product/user-stories)**: catálogo de 62 historias en 2 niveles de madurez muy distintos — 15 historias (épicas 01-02) completamente enriquecidas; 47 historias (épicas 03-13) esqueléticas (solo Story + Acceptance Criteria, sin DoD/Technical Notes/Dependencies).

## Cobertura funcional

- Cubre de punta a punta el ciclo Open (brief → draft → rúbrica → submissions → grading → feedback → gaps → recovery → report) y el ciclo Closed (generación de preguntas → curación de calidad/ambigüedad → ensamblaje → publicación con snapshot → intake de estudiante → grading determinístico → analítica de ítems).
- El flujo crítico de negocio (onboarding docente → crear evaluación → calificar → evidenciar) está bien cubierto por `02-product/workflows.md` y por la implementación real (Epic 01 y 02 son las únicas con ejecución).
- Vacío funcional real: no hay ningún documento que defina el flujo **mixto** (open+closed en un mismo assessment) más allá de mencionarlo como diferido — correctamente fuera de alcance, no es un vacío a resolver ahora.
- Vacío funcional real: el modo Closed, pese a estar diseñado con detalle funcional completo, no tiene ninguna historia de usuario en el corte P0 del hackathon declarado en `02-product/user-stories.md`.

## Cobertura técnica

- Arquitectura hexagonal descrita en `10-best-practices/02-arquitectura-y-diseno.md` se refleja fielmente en el código real de `api/` y `agents/` — es la única guía cuyo contenido coincide sin corrección con el estado actual.
- Modelo de datos (`04-architecture/data-model.md`) es rico y con 20+ entidades, pero su enum `CostCategory` no contempla Groq/proveedores OpenAI-compatible — vacío técnico directo para el futuro inventario de automatización (Fase 03).
- `agents-overview.md` (Model Routing Policy) es 100% Gemini-Flash-class-céntrico, sin ninguna previsión de ruteo hacia modelos Groq.
- Cobertura de despliegue está partida en dos documentos que no se referencian entre sí: `04-architecture/beta-environment-design.md` (Render/Vercel/Neon) y `09-developer-guide/09-deployment-guide.md` (GCP/Cloud Run manual).
- El estado real de "madurez" de cada repo (`api`/`agents`/`web`/`infra`) no está documentado en ningún lugar de forma fidedigna — `04-architecture/repository-structure.md` es puramente aspiracional (no distingue actual vs. objetivo) y `09-developer-guide` está desactualizado en sentido contrario (subestima lo ya construido).

## Cobertura de negocio

- Modelo de negocio, pricing, GTM y customer discovery están completos y coherentes entre sí (00-project/cost-model.md ↔ 01-business/pricing.md coinciden cifra a cifra).
- Vacío de negocio real: **ningún documento de 01-business/ contempla el modo Closed** — no hay ángulo de venta, persona compradora ni mensaje diferenciado para evaluación objetiva pese a que 02-product lo trata como paridad de producto.
- Contradicción de negocio real: las cifras de pricing en `07-hackathon/submission-narrative.md` (borrador de narrativa pública) no coinciden con las cifras canónicas de `cost-model.md`/`pricing.md`.
- El presupuesto de tokens/costo (`cost-model.md`) solo cubre los pasos del modo Open — no incluye los 5 agentes del modo Closed.
- Todos los artefactos de evidencia real (`05-evidence/*`, `07-hackathon/evidence-checklist.md`, `submission-narrative.md`) están vacíos o con placeholders — consistente con estar en fase de descubrimiento, pero es un riesgo de tiempo dado que quedan ~4 semanas para el deadline del hackathon (2026-08-17).

## Cobertura de automatización

- `03-ai-agents/` documenta el contrato funcional de los 13 agentes de forma completa, incluyendo nivel de automatización implícito (clase de modelo) y reglas de human-in-the-loop explícitas por agente.
- No existe todavía (ni se espera en esta fase) una clasificación formal Manual/Asistida/Supervisada/Automatizada/Autónoma controlada por proceso — eso es objeto de la Fase 03.
- Vacío detectado que sí corresponde anticipar: el modelo de costos y el routing de modelos no contemplan Groq, lo que dejará un hueco en el inventario de automatización de la Fase 03 si no se resuelve antes.

## Cobertura de seguridad y observabilidad

- `04-architecture/security.md` es agnóstico de proveedor (correcto) y cubre postura estándar (server-side model calls, tenant isolation, redacción de logs, tokens hasheados).
- `09-developer-guide/04-security-implementation.md` es, junto con `10-best-practices/02-arquitectura-y-diseno.md`, de los pocos documentos técnicos que sí están al día con el código real (filter chain, OwnershipVerifier).
- Vacío de observabilidad: el modelo de "evidencia de ejecución de agente" está fragmentado en al menos tres versiones de distinta completitud (`05-evidence/agent-logs.md` pobre; `06-ux/teacher-workspace-ux.md` y `08-user-guide/06-dashboard-and-workspace.md` ricas y casi idénticas entre sí; `CLAUDE.md`/ADR `agent-runtime-separation.md` ricas). No hay una única fuente de verdad para qué campos debe tener un `AgentExecutionLog`.
- Vacío de seguridad documental: Firebase Authentication está implementado y es load-bearing en el código real (`FirebaseAuthAdapter`, `FirebaseTokenFilter`, Identity Platform en infra), respaldado por un ADR completo, pero **ausente del diagrama de arquitectura y del texto de `CLAUDE.md` raíz**, que es el documento que debería describir la postura de seguridad vigente de extremo a extremo.

## Contradicciones

| # | Contradicción | Documentos involucrados | Severidad aparente |
|---|---|---|---|
| C1 | Alcance de producto: 8 agentes/solo Open (00-project) vs. 13 agentes/Open+Closed (02-product, 03-ai-agents, CLAUDE.md, ADR closed-assessment-mode) | `00-project/solution.md`, `hackathon-strategy.md`, `roadmap.md`, `cost-model.md`, `pitch.md` vs. `02-product/*`, `03-ai-agents/*`, `99-decisions/2026-06-10-closed-assessment-mode.md` | Alta — 00-project es la capa canónica declarada y no refleja una decisión ya tomada |
| C2 | Proveedor de IA: Vertex AI Gemini exclusivo (ADR, CLAUDE.md) vs. Groq como proveedor real por defecto (código, 2 plannings cerradas) | `99-decisions/2026-06-10-technology-stack.md`, `agent-runtime-separation.md`, `CLAUDE.md` raíz vs. `agents/src/.../groq/`, `agents/.planning/finished/002-groq-genai-provider`, `.planning/finished/009-groq-infra-provisioning` | Alta — decisión ya ejecutada sin registro formal |
| C3 | Entorno objetivo: `demo` GCP (única descripción en CLAUDE.md/04-architecture/07-hackathon) nunca desplegado vs. `beta` Render (único con evidencia real de funcionamiento) documentado solo en un archivo aislado | `CLAUDE.md` raíz, `04-architecture/system-architecture.md`, `07-hackathon/*` vs. `04-architecture/beta-environment-design.md`, `.planning/finished/009-groq-infra-provisioning/README.md`, `.planning/active/008-assessment-creation/02-deepening/story-04-*` | **Crítica** — el hackathon exige explícitamente Google Cloud + Gemini API |
| C4 | Corte P0 de historias (`user-stories.md`) excluye épicas 11-13 (Closed) pese a que `workflows.md` y `mvp-scope.md` marcan esos flujos como P0 | `02-product/user-stories.md` vs. `02-product/workflows.md`, `mvp-scope.md` | Alta |
| C5 | Guion de demo no muestra el flujo Closed pese a que su propio checklist de pre-grabación exige datos semilla de modo Closed | `07-hackathon/demo-script.md` (checklist vs. escenas 1-9) | Media |
| C6 | Cifras de pricing distintas entre la narrativa pública de submission y los documentos canónicos de pricing | `07-hackathon/submission-narrative.md` vs. `00-project/cost-model.md`, `01-business/pricing.md` | Media |
| C7 | Stack técnico presentado como "opciones abiertas" en el documento canónico vs. ya decidido y en producción en la narrativa de submission | `00-project/solution.md` vs. `07-hackathon/submission-narrative.md`, `99-decisions/2026-06-10-technology-stack.md` | Media |
| C8 | Colisión de IDs de historia: US-010/011/012 existen simultáneamente en Epic 01 y Epic 02 con contenidos distintos | `02-product/user-stories/epic-01-teacher-onboarding/{10,11,12}-*.md` vs. `epic-02-assessment-creation/{01,02,03}-*.md` | Alta para trazabilidad de Fase 02 |
| C9 | "Firebase Authentication" es una decisión formal (ADR) e implementada en código, pero ausente del diagrama/arquitectura de `CLAUDE.md` raíz | `99-decisions/2026-06-12-firebase-authentication.md`, código real vs. `CLAUDE.md` raíz | Media |
| C10 | Tres archivos de `09-developer-guide/` afirman independientemente que `agents/` es "scaffolding sin lógica implementada" | `02-repository-map.md`, `06-agent-development.md`, `09-deployment-guide.md`, `01-local-setup.md`, `05-database-guide.md` vs. código real (adaptador Groq con tests) | Alta para riesgo de retrabajo en Fase 02 |
| C11 | Estado "Scaffolding" declarado en `CLAUDE.md` raíz para api/agents/web/infra vs. plannings cerradas con features reales en los cuatro | `CLAUDE.md` raíz vs. `.planning/finished/*` (raíz y por servicio) | Alta |

## Duplicidades

- `00-project/hackathon-strategy.md` ↔ `01-business/hackathon-strategy.md`: duplicidad **intencional y declarada** (estrategia vs. ejecución de evidencia de negocio). No requiere acción.
- `00-project/cost-model.md` ↔ `01-business/pricing.md`: contenido de pricing duplicado casi palabra por palabra, sin contradicción de cifras hoy, pero es el mecanismo que ya produjo la contradicción C6 en un tercer documento derivado. Riesgo de divergencia futura si no se convierte en una única fuente con referencia cruzada.
- `09-developer-guide/00-00-manual-steps.md` ↔ `00-gcp-project-setup.md`: duplicado casi idéntico (texto plano vs. Markdown formateado) — parece artefacto de generación no depurado.
- Modelo de "log de agente" duplicado con distinta completitud en `05-evidence/agent-logs.md`, `06-ux/teacher-workspace-ux.md` y `08-user-guide/06-dashboard-and-workspace.md`.
- `00-project/hackathon-strategy.md` (outline de demo) ↔ `07-hackathon/demo-script.md` (guion ejecutable): duplicidad intencional de nivel estratégico vs. ejecución, con timecodes ligeramente distintos entre ambos.

## Vacíos

- Modo Closed ausente de todo `01-business/` (sin ángulo de venta, sin persona compradora, sin mensaje diferenciado).
- Modo Closed ausente del presupuesto de costos/tokens de `00-project/cost-model.md`.
- `data-model.md`: `CostCategory` sin categoría para Groq/proveedores OpenAI-compatible.
- `agents-overview.md`: política de ruteo de modelos sin previsión de Groq.
- Ningún ADR formal para la decisión de usar Groq como proveedor por defecto, pese a estar implementada y en producción beta.
- Ningún ADR ni actualización de `CLAUDE.md` que reconcilie la existencia y estado real del entorno `beta` (Render) frente al `demo` (GCP) nunca desplegado.
- `99-decisions/README.md` no lista los dos ADRs más recientes (`2026-06-21-form-validation-react-hook-form-zod.md`, `2026-06-21-web-design-system.md`).
- `roadmap.md` no tiene ningún mecanismo de registro de avance real contra el plan semanal — no es posible, solo con documentación, saber en qué semana/fase se encuentra el proyecto.
- `02-product/user-stories.md` (README raíz de historias) declara 9 historias para Epic 01 cuando en realidad hay 12 — índice desactualizado.
- 47 de 62 historias de usuario (épicas 03-13) carecen de Definition of Done, Technical Notes y Dependencies a nivel de historia individual — esperable en esta etapa, pero es un vacío real que Fase 02 deberá tratar explícitamente.
- Sin epic ni agente propio para 3 de los 13 agentes (Ops Evidence, Distractor Quality, Ambiguity Review) — quedan implícitos en el alcance de otra historia.

## Diferencias entre código y documentación

Ver tabla de contradicciones C2, C3, C9, C10, C11 arriba — son, en esencia, todas diferencias código-vs-documentación. Resumen adicional:

- `api/`: 12 migraciones Flyway reales (hasta `agent_execution_log`), 4 controladores REST, patrón hexagonal completo con ArchUnit — muy por delante de lo que `09-developer-guide/05-database-guide.md` describe ("una sola tabla: teacher").
- `agents/`: solo el Assessment Agent tiene código real (de los 13 documentados) — coherente con que solo existe una planning de agente (`001-assessment-creation`), pero ningún documento de `03-ai-agents/` distingue qué agentes ya tienen implementación de cuáles son aún especificación pura.
- `infra/`: 13 archivos `.tf` reales, pero la infraestructura de `demo` (Cloud Run, Cloud SQL) nunca se aplicó contra GCP — solo Secret Manager/Cloud Run binding para Groq se gestionó vía planning de infra.
- `web/`: rutas reales para auth (login/register/forgot-password/reset-password/verify-email) y placeholders para dashboard/assessments/bank/students/reports — consistente con que solo Epic 01 y parte de Epic 02 tienen ejecución real.
- 65 archivos de test reales (55 en `api`, 10 en `agents`) más 12 en `web` — no son placeholders, incluyen tests de arquitectura (ArchUnit), integración con Testcontainers, y tests diferenciales Gemini-vs-Groq.

## Riesgos iniciales

1. **Riesgo crítico de elegibilidad del hackathon**: si la evaluación de jurado requiere evidencia de despliegue en Google Cloud (`demo`), y el único sistema con evidencia real funciona en Render (`beta`) con Groq, existe riesgo de incumplimiento de bases. Debe investigarse y resolverse con la mayor urgencia posible dentro de esta planificación.
2. **Riesgo de scope creep no reconocido**: el modo Closed se trata como P0 en documentos de flujo pero está excluido del corte P0 real y del guion de demo — si se decide incluirlo tarde, compite por el mismo tiempo limitado (~4 semanas) que otras prioridades del roadmap.
3. **Riesgo temporal de negocio**: `roadmap.md` fija un criterio de kill/pivot para "mediados de julio" (ya estamos en esa ventana, 2026-07-17) sin ningún registro de si las condiciones se cumplieron. Esto no es resoluble solo con documentación — requiere una decisión humana explícita antes de que el Master Plan defina releases.
4. **Riesgo de retrabajo por documentación desactualizada**: si Fase 02 (capacidades y user stories) o cualquier futuro colaborador se guía por `09-developer-guide/` para estimar qué falta construir, subestimará sistemáticamente el trabajo ya hecho en `agents/` e `infra/`.
5. **Riesgo de evidencia insuficiente a tiempo**: todos los artefactos de evidencia de negocio están vacíos a ~4 semanas del deadline; el volumen de trabajo de captura de evidencia (entrevistas, pilotos, revenue) no tiene todavía ningún dato real.
6. **Riesgo de trazabilidad rota**: la colisión de IDs US-010/011/012 entre Epic 01 y Epic 02 puede propagarse a la Fase 02 (que no puede modificar historias originales) si no se resuelve antes.

## Recomendaciones

1. Resolver primero, y con más urgencia que cualquier otra cosa en este diagnóstico, la pregunta del entorno de despliegue objetivo para el hackathon (`demo`/GCP vs. `beta`/Render) — ver decisión bloqueante D-01 en `decisions-and-assumptions.md`.
2. Antes de iniciar la Fase 02 formalmente, renumerar la colisión de IDs entre Epic 01 y Epic 02 (fuera del alcance de escritura de las fases del Master Plan — requiere edición directa de las historias originales).
3. Redactar los dos ADRs faltantes (proveedor Groq por defecto; adopción del entorno beta) para que `99-decisions/` refleje decisiones ya tomadas y ejecutadas.
4. Actualizar `CLAUDE.md` raíz para reflejar el estado real de madurez de cada repo (ya no "Scaffolding"), incluir Firebase en el diagrama de arquitectura, y mencionar la existencia del entorno beta.
5. Decidir explícitamente si el modo Closed entra al corte P0 del hackathon; propagar esa decisión de forma consistente a `user-stories.md`, `demo-script.md` y `01-business/`.
6. Unificar el modelo de campos de "log de agente" en una sola fuente de verdad (recomendado: la versión rica de `06-ux`/`08-user-guide`, alineada a los 14 campos de `master-plan-specification.md`) y usarla para corregir `05-evidence/agent-logs.md`.
7. Corregir las tres/cinco menciones de "scaffolding" en `09-developer-guide/` y el índice de `99-decisions/README.md`.
8. Reconciliar las cifras de pricing de `submission-narrative.md` contra `cost-model.md`/`pricing.md` antes de cualquier publicación externa.

## Historial de cambios

| Fecha | Cambio | Motivo | Elementos afectados | Decisión asociada |
|---|---|---|---|---|
| 2026-07-17 | Creación inicial del diagnóstico | Ejecución de la Fase 01 del Master Plan Ejecutivo | Todo el documento | — |
