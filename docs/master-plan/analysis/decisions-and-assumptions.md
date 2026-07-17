# Decisiones, supuestos, riesgos y preguntas abiertas — GradeOps AI

> Fase 01 — Descubrimiento y diagnóstico del Master Plan Ejecutivo.
> Ninguna contradicción se resuelve silenciosamente aquí. Cada elemento queda explícito para que un humano decida.

## Leyenda de tipo

- **Decisión bloqueante**: impide definir con confianza el MVP o el flujo crítico si no se resuelve.
- **Decisión no bloqueante**: puede quedar como supuesto documentado y resolverse en paralelo.
- **Decisión reversible / difícilmente reversible**: coste de cambiar de opinión más adelante.
- **Supuesto**: se asume verdadero para poder avanzar; no confirmado activamente en esta fase.
- **Riesgo**: puede materializarse y afectar el plan; no es una decisión a tomar hoy sino algo a vigilar.
- **Pregunta abierta**: no hay información suficiente en `@docs/` para responderla.

---

## D-01 — Entorno de despliegue objetivo para el hackathon: `demo` (GCP) vs. `beta` (Render)

- **Tipo**: Decisión bloqueante, difícilmente reversible bajo presión de tiempo.
- **Contexto**: `CLAUDE.md` raíz, `docs/04-architecture/system-architecture.md` y todo `docs/07-hackathon/` describen únicamente el entorno `demo` (GCP Cloud Run + Cloud SQL + Vertex AI Gemini) como arquitectura objetivo. Las reglas del hackathon (`00-project/hackathon-strategy.md`) exigen explícitamente Google Cloud + Gemini API. Sin embargo, el entorno `demo` **nunca ha sido desplegado** (Terraform nunca aplicado contra GCP real, según la retrospectiva de `.planning/finished/009-groq-infra-provisioning/README.md`), mientras que el entorno `beta` (Render + Vercel + Neon + Groq), documentado únicamente en `04-architecture/beta-environment-design.md`, es el único con evidencia real de funcionamiento end-to-end (flujo brief→generate verificado 2026-07-16/17, story-04 de `008-assessment-creation`).
- **Evidencia**: `04-architecture/beta-environment-design.md`; `.planning/finished/009-groq-infra-provisioning/README.md`; `.planning/active/008-assessment-creation/02-deepening/story-04-e2e-integration-verification.md`; `00-project/hackathon-strategy.md` (requisito Google Cloud + Gemini API); `infra/terraform/environments/demo/*.tf` (definidos pero no aplicados).
- **Alternativas**: (a) desplegar `demo` en GCP antes del deadline y usarlo como plataforma de evaluación oficial; (b) usar `beta` como entorno de desarrollo/demo interno y desplegar una versión mínima de `demo` solo para cumplir el requisito formal del hackathon; (c) formalizar `beta` como el entorno real y evaluar el riesgo de incumplimiento de bases si el jurado exige evidencia GCP.
- **Recomendación**: Priorizar (b) — es la opción de menor riesgo y esfuerzo: mantener `beta` como motor de evidencia real de producto, y desplegar un `demo` mínimo (aunque sea con datos de prueba) específicamente para satisfacer el requisito de plataforma del hackathon.
- **Consecuencia si no se resuelve**: la Fase 04 (planificación de releases) no puede definir criterios de "desplegable y demostrable" sin saber contra qué entorno se evalúan; riesgo de descalificación o pérdida de puntos de elegibilidad del hackathon.
- **Estado**: Pendiente.
- **Responsable sugerido**: Carlos (founder / decisión de producto y cumplimiento de bases).
- **Fecha máxima de resolución**: 2026-07-24 (una semana desde este diagnóstico; el deadline del hackathon es 2026-08-17 y esta decisión condiciona todo el trabajo de infraestructura restante).
- **Fases afectadas**: 03, 04, 05, 06.

---

## D-02 — Alcance del modo Closed en el corte P0 del hackathon

- **Tipo**: Decisión bloqueante, reversible si se resuelve pronto.
- **Contexto**: `02-product/workflows.md` y `02-product/mvp-scope.md` marcan los flujos de modo Closed (generación de preguntas, ensamblaje, intake y grading cerrado, analítica de ítems, invitación de estudiante) como **P0**. Sin embargo, `02-product/user-stories.md` (el corte oficial de historias P0 para el demo) **excluye por completo** las épicas 11, 12 y 13 (curriculum structure, question bank, student invitation/access), y el guion de demo (`07-hackathon/demo-script.md`) no muestra el flujo Closed en ninguna de sus 9 escenas pese a que su propio checklist de pre-grabación exige datos semilla de ese modo. `00-project/` (capa canónica de negocio) tampoco cubre el modo Closed en ningún documento.
- **Evidencia**: `02-product/user-stories.md` (línea del corte P0); `02-product/workflows.md`; `02-product/mvp-scope.md` (matriz de scope y "MVP Cut Line"); `07-hackathon/demo-script.md` (checklist vs. escenas).
- **Alternativas**: (a) el modo Closed entra al corte P0 real — hay que agregar sus historias al MVP cut y al guion de demo; (b) el modo Closed queda fuera del corte P0 del hackathon — hay que corregir `workflows.md`/`mvp-scope.md` para marcarlo P1/roadmap, y ajustar `01-business/` en consecuencia; (c) el modo Closed se demuestra parcialmente (solo generación de preguntas, sin ciclo completo de estudiante) como término medio.
- **Recomendación**: Decidir en función del tiempo real disponible (~4 semanas). Si el modo Closed ya tiene código de agente implementado y probado tan pronto como el modo Open, (a) es viable; si no, (b) es más seguro y evita dispersión de esfuerzo a 4 semanas del deadline.
- **Consecuencia si no se resuelve**: la Fase 02 no puede clasificar con confianza el readiness de las épicas 11-13, y la Fase 04 no puede secuenciar releases sin saber si el modo Closed compite por el mismo tiempo que el resto del roadmap P0.
- **Estado**: Pendiente.
- **Responsable sugerido**: Carlos.
- **Fecha máxima de resolución**: 2026-07-20 (antes de iniciar formalmente la Fase 02).
- **Fases afectadas**: 02, 04, 05.

---

## D-03 — Colisión de IDs de historia entre Epic 01 y Epic 02 (US-010/011/012)

- **Tipo**: Decisión bloqueante para la trazabilidad de la Fase 02, reversible (es un problema de numeración, no de contenido).
- **Contexto**: `US-010`, `US-011` y `US-012` existen simultáneamente y con contenido distinto en `epic-01-teacher-onboarding/` (Postman Collection, Google Sign-In, Password Recovery) y en `epic-02-assessment-creation/` (Assessment Brief Intake, Draft Generation, Draft Regeneration). El resto del catálogo (épicas 03-13) sigue un esquema de bloques de 10 sin colisiones. El índice `02-product/user-stories.md` además declara "9 historias" para Epic 01 cuando en realidad hay 12.
- **Evidencia**: `docs/02-product/user-stories/epic-01-teacher-onboarding/{10,11,12}-*.md` vs. `docs/02-product/user-stories/epic-02-assessment-creation/{01,02,03}-*.md`; `docs/02-product/user-stories/README.md`.
- **Alternativas**: (a) renumerar las historias de Epic 01 (US-010→US-013, etc., liberando el bloque 010-012 para Epic 02); (b) renumerar las de Epic 02; (c) dejarlo como está y resolver la ambigüedad solo por contexto en cada referencia futura.
- **Recomendación**: (a) — Epic 02 ya usa el bloque 010-012 de forma consistente con el esquema de bloques de 10 del resto del catálogo; Epic 01 es la excepción que rompió el patrón al crecer de 9 a 12 historias.
- **Consecuencia si no se resuelve**: la Fase 02 no puede generar un inventario de user stories trazable (regla de consistencia del `master-plan-specification.md`: "Toda US priorizada debe ser trazable"), y no puede corregirlo por sí misma porque tiene explícitamente prohibido modificar user stories originales.
- **Estado**: Pendiente. Requiere edición directa de archivos fuera del alcance de escritura de las fases del Master Plan.
- **Responsable sugerido**: Carlos (o quien mantenga `02-product/user-stories/`).
- **Fecha máxima de resolución**: antes de iniciar la Fase 02.
- **Fases afectadas**: 02, 04, 05, 06.

---

## D-04 — Formalizar ADR de Groq como proveedor de IA por defecto

- **Tipo**: Decisión no bloqueante, reversible (la implementación ya soporta ambos proveedores vía adaptador/selector).
- **Contexto**: `agents/` tiene un `AssessmentGenerationPortSelector` con adaptadores Gemini y Groq como pares completos; el proveedor por defecto real es Groq (`default-provider: groq` en `application.yml`/`application-beta.yml`), decidido y ejecutado en la planning cerrada `agents/.planning/finished/002-groq-genai-provider`. Ningún ADR en `99-decisions/` documenta esta decisión; `2026-06-10-technology-stack.md` sigue afirmando Vertex AI Gemini como único proveedor.
- **Evidencia**: `agents/src/main/java/.../adapter/out/groq/GroqAssessmentGenerationAdapter.java`; `agents/.planning/finished/002-groq-genai-provider/README.md`; `99-decisions/2026-06-10-technology-stack.md`.
- **Alternativas**: (a) redactar un nuevo ADR que documente Groq como proveedor por defecto y Gemini como alternativa/fallback; (b) revertir el default a Gemini para alinear código con el ADR existente; (c) dejarlo sin documentar.
- **Recomendación**: (a) — la decisión ya está tomada, probada y en producción beta; el ADR debe reflejar la realidad, no al revés.
- **Consecuencia si no se resuelve**: la Fase 03 (estrategia de automatización) construirá el inventario de costos/modelos sobre información desactualizada (Gemini-only), y `data-model.md`/`agents-overview.md` seguirán sin categoría de costo ni ruteo para Groq.
- **Estado**: Pendiente.
- **Responsable sugerido**: tech lead de `agents/` (Carlos).
- **Fecha máxima de resolución**: antes de la Fase 03.
- **Fases afectadas**: 03, 04, 05.

---

## D-05 — Actualizar `CLAUDE.md` raíz y arquitectura para reflejar Firebase, Groq, beta y el estado real de madurez

- **Tipo**: Decisión no bloqueante, reversible.
- **Contexto**: `CLAUDE.md` raíz describe api/agents/web/infra como "Scaffolding", no menciona Firebase Authentication (implementado y respaldado por ADR `2026-06-12-firebase-authentication.md`), no menciona Groq, y no menciona el entorno beta. Tres archivos de `09-developer-guide/` repiten independientemente que `agents/` no tiene lógica implementada.
- **Evidencia**: `CLAUDE.md` raíz; `99-decisions/2026-06-12-firebase-authentication.md`; `api/src/.../firebase/FirebaseAuthAdapter.java`; `09-developer-guide/{02-repository-map,06-agent-development,09-deployment-guide,01-local-setup,05-database-guide}.md`.
- **Alternativas**: (a) actualizar `CLAUDE.md` raíz y los 5 archivos de developer-guide afectados en un solo barrido; (b) dejarlo para después del Master Plan y aceptar el riesgo de que futuras fases hereden la misma desactualización.
- **Recomendación**: (a), idealmente antes de que la Fase 02 use estos documentos como referencia de "qué ya existe".
- **Consecuencia si no se resuelve**: riesgo de retrabajo — cualquier colaborador (humano o agente) que confíe en `09-developer-guide/` subestimará sistemáticamente lo ya construido.
- **Estado**: Pendiente.
- **Responsable sugerido**: Carlos.
- **Fecha máxima de resolución**: antes de la Fase 02 (recomendado, no estrictamente bloqueante).
- **Fases afectadas**: 02, 04.

---

## D-06 — Unificar el modelo de campos del log de ejecución de agente

- **Tipo**: Decisión no bloqueante, reversible.
- **Contexto**: existen al menos tres versiones del "log de agente" con distinta completitud: `05-evidence/agent-logs.md` (pobre, ~5-6 campos), `06-ux/teacher-workspace-ux.md` y `08-user-guide/06-dashboard-and-workspace.md` (ricas, casi idénticas entre sí, cercanas a los 14 campos exigidos por `master-plan-specification.md`), y el modelo implícito en `CLAUDE.md`/ADR `agent-runtime-separation.md`.
- **Evidencia**: `05-evidence/agent-logs.md`; `06-ux/teacher-workspace-ux.md` § Agent Log Viewer; `08-user-guide/06-dashboard-and-workspace.md` § Agent Run Logs; `master-plan-specification.md` (lista de 14 campos de evidencia).
- **Alternativas**: (a) adoptar la versión rica de 06-ux/08-user-guide como fuente única y corregir `05-evidence/agent-logs.md` para que la referencie; (b) definir un nuevo modelo desde cero.
- **Recomendación**: (a) — ya existe consenso implícito entre dos documentos independientes, solo falta que `05-evidence/` se actualice.
- **Consecuencia si no se resuelve**: la Fase 03 heredará un modelo de evidencia incompleto para diseñar el inventario de automatización.
- **Estado**: Pendiente.
- **Responsable sugerido**: Carlos.
- **Fecha máxima de resolución**: antes de la Fase 03.
- **Fases afectadas**: 03, 05.

---

## D-07 — Reconciliar cifras de pricing entre `submission-narrative.md` y los documentos canónicos

- **Tipo**: Decisión no bloqueante, reversible.
- **Contexto**: `07-hackathon/submission-narrative.md` presenta cifras de pricing distintas (límites de submissions por plan, ausencia del plan Free) respecto a `00-project/cost-model.md` y `01-business/pricing.md`, que coinciden entre sí.
- **Evidencia**: ver contradicción C6 en `documentation-diagnosis.md`.
- **Alternativas**: (a) corregir `submission-narrative.md` para que cite las cifras canónicas; (b) si las cifras de `submission-narrative.md` reflejan un cambio de pricing más reciente, actualizar `cost-model.md`/`pricing.md` y registrar el cambio como ADR.
- **Recomendación**: (a), salvo que exista una razón de negocio no documentada para el cambio — en cuyo caso corresponde (b) + ADR.
- **Consecuencia si no se resuelve**: riesgo de publicar cifras inconsistentes en la submission final del hackathon.
- **Estado**: Pendiente.
- **Responsable sugerido**: Carlos.
- **Fecha máxima de resolución**: antes de publicar la narrativa de submission final (no bloquea las fases 02-04 del Master Plan).
- **Fases afectadas**: 04 (evidencia de negocio), 05 (release con evidencia de pricing).

---

## A-01 — Regla de gobernanza documental de `00-project/` sigue vigente

- **Tipo**: Supuesto.
- **Contexto**: `00-project/README.md` declara que esa carpeta es la fuente canónica salvo que un decision record en `99-decisions/` la sustituya explícitamente.
- **Evidencia**: `00-project/README.md`.
- **Alternativas**: n/a — se asume como regla de gobernanza para resolver C1 y otras contradicciones en fases siguientes.
- **Recomendación**: mantener la regla; usarla para justificar que `02-product/`+ADR `closed-assessment-mode` prevalecen sobre `00-project/solution.md` en lo referente a alcance de modos.
- **Consecuencia si resulta falsa**: las prioridades derivadas de esta regla en fases posteriores tendrían que revisarse.
- **Estado**: Aceptado como supuesto de trabajo.
- **Responsable sugerido**: n/a.
- **Fecha máxima de resolución**: n/a.
- **Fases afectadas**: 02, 04.

---

## A-02 — Las 15 historias enriquecidas de Epic 01-02 reflejan fielmente lo implementado

- **Tipo**: Supuesto.
- **Contexto**: Epic 01 y Epic 02 son las únicas con historias enriquecidas (DoD, Technical Notes, Dependencies, Complexity) y son las únicas con ejecución real vía plannings cerradas/activas.
- **Evidencia**: `.planning/active/008-assessment-creation/01-expansion.md` (menciona enriquecimiento vía `/us-enrich` antes de la expansión).
- **Alternativas**: n/a.
- **Recomendación**: la Fase 02 puede tratar estas 15 historias con mayor confianza de readiness que las 47 restantes, sin necesidad de re-diagnóstico profundo más allá de contrastar contra el código ya verificado en esta fase.
- **Consecuencia si resulta falsa**: la Fase 02 subestimaría trabajo pendiente en Epic 01-02.
- **Estado**: Aceptado como supuesto de trabajo.
- **Responsable sugerido**: n/a.
- **Fecha máxima de resolución**: n/a.
- **Fases afectadas**: 02.

---

## A-03 — El deadline del hackathon (2026-08-17, 13:00 PDT) sigue vigente sin cambios

- **Tipo**: Supuesto.
- **Contexto**: las reglas citadas en `00-project/hackathon-strategy.md` están marcadas "as of June 8, 2026" con instrucción explícita de re-verificar antes de la submission final; no hay evidencia de revalidación posterior en la documentación.
- **Evidencia**: `00-project/hackathon-strategy.md`; `07-hackathon/README.md`.
- **Alternativas**: n/a.
- **Recomendación**: re-verificar contra la página oficial de Devpost antes de la Fase 04 (planificación de releases), ya que toda la secuencia de hitos depende de esta fecha.
- **Consecuencia si resulta falsa**: toda la secuencia de releases y el camino crítico de la Fase 04 quedarían mal calibrados.
- **Estado**: Aceptado como supuesto de trabajo, con recomendación de revalidación.
- **Responsable sugerido**: Carlos.
- **Fecha máxima de resolución**: antes de la Fase 04.
- **Fases afectadas**: 04.

---

## R-01 — Riesgo de elegibilidad del hackathon por entorno de despliegue no conforme

- **Tipo**: Riesgo (ligado a D-01).
- **Contexto**: ver D-01. Si el jurado exige evidencia de despliegue en Google Cloud y solo existe evidencia real en Render, hay riesgo de pérdida de puntos o descalificación parcial.
- **Evidencia**: ver D-01.
- **Recomendación**: investigar tempranamente si las bases del hackathon exigen despliegue efectivo en GCP o solo uso de Gemini API (que podría satisfacerse incluso con el entorno beta si se reincorpora Gemini como proveedor activo).
- **Estado**: Abierto.
- **Responsable sugerido**: Carlos.
- **Fases afectadas**: 04, 06.

---

## R-02 — Ventana de decisión kill/pivot de `roadmap.md` sin resolución registrada

- **Tipo**: Riesgo.
- **Contexto**: `roadmap.md` fija un criterio de kill/pivot "si a mediados de julio" no se cumplen ciertas señales (comprensión de la oferta por parte de docentes, acuerdo de prueba, fiabilidad de agent logs, costo estimable por assessment, no derivar hacia features de LMS). Hoy es 2026-07-17, dentro de esa ventana, y no hay ningún documento que confirme si las condiciones se cumplieron.
- **Evidencia**: `00-project/roadmap.md`.
- **Recomendación**: resolver esta pregunta con Carlos antes de que la Fase 04 fije la secuencia de releases, ya que condiciona si el roadmap debe ajustarse agresivamente.
- **Estado**: Abierto — no resoluble solo con documentación.
- **Responsable sugerido**: Carlos.
- **Fases afectadas**: 04.

---

## R-03 — Evidencia de negocio insuficiente a ~4 semanas del deadline

- **Tipo**: Riesgo.
- **Contexto**: `05-evidence/*`, `07-hackathon/evidence-checklist.md` y `submission-narrative.md` están vacíos o con placeholders. Los targets numéricos (10+ entrevistas, 5+ pilotos, 3+ pilotos pagados, 100+ submissions, etc.) no tienen avance registrado.
- **Evidencia**: ver sección "Vacíos" de `documentation-diagnosis.md`.
- **Recomendación**: la Fase 04 debe priorizar explícitamente actividades que generen evidencia real cuanto antes, no solo funcionalidad.
- **Estado**: Abierto.
- **Responsable sugerido**: Carlos.
- **Fases afectadas**: 04, 05.

---

## Q-01 — ¿Cuál es el avance real frente al plan semanal de `roadmap.md`?

- **Tipo**: Pregunta abierta.
- **Contexto**: no existe en `@docs/` ningún registro de estado/avance contra el plan semanal (semana 1 a 10, Jun 8–Aug 17).
- **Recomendación**: Carlos debe aportar esta información directamente; no es derivable de la documentación ni del código.
- **Estado**: Abierta.
- **Fases afectadas**: 04.

## Q-02 — ¿Los nombres y precios de modelos Gemini citados en `cost-model.md` siguen vigentes?

- **Tipo**: Pregunta abierta.
- **Contexto**: el propio documento marca "Gemini 3.1 Flash-Lite", "Gemini 3 Flash Preview", "Gemini 3.1 Pro Preview" como sujetos a verificación antes del despliegue.
- **Recomendación**: reverificar contra pricing oficial vigente antes de que el presupuesto de costos se use en la Fase 03/04.
- **Estado**: Abierta.
- **Fases afectadas**: 03, 04.

## Q-03 — ¿`web/` se despliega vía Cloud Run o vía Firebase App Hosting?

- **Tipo**: Pregunta abierta.
- **Contexto**: `infra/terraform/environments/demo/cloud_run.tf` define servicios Cloud Run solo para `api` y `agents`; `web/` no tiene Cloud Run definido, y existe `firebase_app_hosting.tf`, lo que sugiere que `web/` se despliega vía Firebase App Hosting, no Cloud Run como implica el diagrama de `CLAUDE.md` raíz (Browser → Web → API → Agents, todo "on Cloud Run").
- **Recomendación**: confirmar y corregir el diagrama de arquitectura de `CLAUDE.md` raíz en consecuencia (parte de D-05).
- **Estado**: Abierta.
- **Fases afectadas**: 02, 04.

---

## Historial de cambios

| Fecha | Cambio | Motivo | Elementos afectados | Decisión asociada |
|---|---|---|---|---|
| 2026-07-17 | Creación inicial | Ejecución de la Fase 01 del Master Plan Ejecutivo | Todo el documento | — |
