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

## D-01 — Roles de entorno: `beta` para evidencia funcional y `demo` como target Google Cloud

- **Tipo**: Decisión de arquitectura y entrega, reversible si se registra un nuevo ADR.
- **Contexto**: el entorno `beta` (Render + Vercel + Neon + Groq) es el camino de evidencia funcional e iteración rápida. El entorno `demo` representa el target Google Cloud con Terraform, Cloud Run, Cloud SQL, Cloud Storage, Firebase y camino Gemini-capable. La postulación externa que imponía restricciones específicas fue archivada; ya no condiciona el plan activo.
- **Evidencia**: `04-architecture/beta-environment-design.md`; `.planning/finished/009-groq-infra-provisioning/README.md`; `.planning/active/008-assessment-creation/02-deepening/story-04-e2e-integration-verification.md`; `infra/terraform/environments/demo/*.tf`; ADR `99-decisions/2026-07-20-environment-roles.md`.
- **Decisión**: `beta` puede usarse para evidencia real de producto y pilotos tempranos. `demo` se mantiene como target Google Cloud para validar despliegue productivo cuando exista evidencia real. Ningún documento activo debe afirmar que `demo` esta desplegado o que Gemini fue usado si no hay prueba.
- **Consecuencia**: la Fase 04 puede planificar releases contra valor de producto sin bloqueo de elegibilidad externa. R06 debe verificar deployment/provider evidence solo para las afirmaciones que se quieran hacer frente a clientes, evaluadores o partners.
- **Estado**: Resuelta el 2026-07-20.
- **Responsable sugerido**: Carlos.
- **Fases afectadas**: 03, 04, 05, 06.

---

## D-02 — Alcance del modo Closed en el corte P0 del MVP

- **Tipo**: Decisión bloqueante, reversible si se resuelve pronto.
- **Contexto**: `02-product/workflows.md` y `02-product/mvp-scope.md` marcan los flujos de modo Closed (generación de preguntas, ensamblaje, intake y grading cerrado, analítica de ítems, invitación de estudiante) como **P0**. Sin embargo, `02-product/user-stories.md` excluye por completo las épicas 11, 12 y 13 (curriculum structure, question bank, student invitation/access). `00-project/` tampoco cubría el modo Closed de forma consistente en todos los documentos.
- **Evidencia**: `02-product/user-stories.md` (línea del corte P0); `02-product/workflows.md`; `02-product/mvp-scope.md` (matriz de scope y "MVP Cut Line").
- **Alternativas**: (a) el modo Closed entra al corte P0 real y se agregan sus historias al MVP cut; (b) el modo Closed queda fuera del corte P0 y se corrigen `workflows.md`/`mvp-scope.md`; (c) el modo Closed se demuestra parcialmente (solo generación de preguntas, sin ciclo completo de estudiante) como término medio.
- **Recomendación**: Adoptar (a) como decisión de planificación: el modo Closed entra al corte P0 real y debe aparecer en el mapa de capacidades, inventario de historias y planificación de releases. La viabilidad de implementación completa se validará en Fase 04 al dimensionar releases y camino crítico.
- **Consecuencia si no se hubiera resuelto**: la Fase 02 no habría podido clasificar con confianza el readiness de las épicas 11-13, y la Fase 04 no habría podido secuenciar releases sin saber si el modo Closed compite por el mismo tiempo que el resto del roadmap P0.
- **Estado**: Resuelta el 2026-07-17 para efectos del Master Plan: **Closed = P0 del MVP**. Queda pendiente propagar esta decisión a los documentos de producto divergentes fuera del alcance de la Fase 02.
- **Responsable sugerido**: Carlos.
- **Fecha máxima de resolución**: 2026-07-20 (antes de iniciar formalmente la Fase 02).
- **Fases afectadas**: 02, 04, 05.

---

## D-03 — Colisión de IDs de historia entre Epic 01 y Epic 02 (US-010/011/012)

- **Tipo**: Decisión bloqueante para la trazabilidad de la Fase 02, reversible (es un problema de numeración, no de contenido).
- **Contexto**: `US-010`, `US-011` y `US-012` existen simultáneamente y con contenido distinto en `epic-01-teacher-onboarding/` (Postman Collection, Google Sign-In, Password Recovery) y en `epic-02-assessment-creation/` (Assessment Brief Intake, Draft Generation, Draft Regeneration). El resto del catálogo (épicas 03-13) sigue un esquema de bloques de 10 sin colisiones. El índice `02-product/user-stories.md` además declara "9 historias" para Epic 01 cuando en realidad hay 12.
- **Evidencia**: `docs/02-product/user-stories/epic-01-teacher-onboarding/{10,11,12}-*.md` vs. `docs/02-product/user-stories/epic-02-assessment-creation/{01,02,03}-*.md`; `docs/02-product/user-stories/README.md`.
- **Alternativas**: (a) renumerar las historias de Epic 01 (US-010→US-013, etc., liberando el bloque 010-012 para Epic 02); (b) renumerar las de Epic 02; (c) dejarlo como está y resolver la ambigüedad solo por contexto en cada referencia futura.
- **Recomendación**: (a) — Epic 02 ya usa el bloque 010-012 de forma consistente con el esquema de bloques de 10 del resto del catálogo; Epic 01 era la excepción que rompía el patrón al crecer de 9 a 12 historias.
- **Consecuencia si no se hubiera resuelto**: la Fase 02 no habría podido generar un inventario de user stories trazable (regla de consistencia del `master-plan-specification.md`: "Toda US priorizada debe ser trazable").
- **Estado**: Resuelta el 2026-07-17. Epic 01 fue renumerada de US-010/011/012 a US-013/014/015 en el commit `e07a68a` (`docs(user-stories): renumber Epic 01 US-010/011/012 to free block for Epic 02`).
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

## D-07 — Reconciliar cifras de pricing entre documentos canónicos y materiales archivados

- **Tipo**: Decisión no bloqueante, reversible.
- **Contexto**: los materiales archivados de narrativa externa presentan cifras de pricing distintas (límites de submissions por plan, ausencia del plan Free) respecto a `00-project/cost-model.md` y `01-business/pricing.md`, que coinciden entre sí.
- **Evidencia**: ver contradicción C6 en `documentation-diagnosis.md`.
- **Alternativas**: (a) conservar los materiales archivados como históricos y mantener `cost-model.md`/`pricing.md` como fuente activa; (b) si las cifras archivadas reflejan un cambio de pricing más reciente, actualizar `cost-model.md`/`pricing.md` y registrar el cambio como ADR.
- **Recomendación**: (a), salvo que exista una razón de negocio no documentada para el cambio — en cuyo caso corresponde (b) + ADR.
- **Consecuencia si no se resuelve**: riesgo de publicar cifras inconsistentes en materiales comerciales futuros.
- **Estado**: Pendiente.
- **Responsable sugerido**: Carlos.
- **Fecha máxima de resolución**: antes de publicar nueva narrativa comercial (no bloquea las fases 02-04 del Master Plan).
- **Fases afectadas**: 04 (evidencia de negocio), 05 (release con evidencia de pricing).

---

## D-UI — UI Design System y semantica de datos por release funcional

- **Tipo**: Decisión de producto/arquitectura, reversible mediante ADR nuevo.
- **Contexto**: las pantallas web pueden seguir un proceso correcto de wireframes y mockups, pero aun asi degradar el dominio si convierten enums, numeros, estados o datos maestros en `input text` libres. El caso critico actual es `/assessments/new`, donde el DTO de intake usa strings para `topic`, `level`, `duration` y `language`, mientras el modelo/UX ya sugieren enum, numero, catalogo o dato maestro.
- **Evidencia**: ADR `99-decisions/2026-07-21-ui-design-data-semantics.md`; `docs/master-plan/analysis/ui-design-data-strategy.md`; `docs/99-decisions/2026-06-21-web-design-system.md`; `docs/04-architecture/data-model.md`; `docs/06-ux/teacher-workspace-ux.md`.
- **Decisión**: toda implementacion de UI debe partir desde el Design System y una matriz de campos antes de wireframe/mockup/codigo. Cada campo debe declarar naturaleza del dato, fuente de verdad, restricciones, cardinalidad y control. Todo dato de pantalla, tanto lectura como escritura, debe estar respaldado por `api/`. Datos maestros, enums, numeros, fechas, estados y valores restringidos no se implementan como texto libre salvo decision temporal explicita con residual. Toda accion debe declarar si la comunicacion es sync o async; si es async, debe definir como `web/` detecta completion/progress/failure.
- **Consecuencia**: cada release funcional que toque `web/` debe incluir gate UI Design/Data Semantics, pruebas unitarias/acceptance/e2e de controles/valores validos-invalidos, contrato Web-API para datos I/O, mecanismo de finalizacion async cuando aplique, y scope `api`/DB/infra si se requieren endpoints, catalogos o tablas maestras.
- **Estado**: Resuelta el 2026-07-21.
- **Responsable sugerido**: Product/Web owner con API owner para fuentes de verdad.
- **Fases afectadas**: 04, 05, 06.

---

## D-I18N — i18n por release funcional

- **Tipo**: Decision de producto/arquitectura, reversible mediante ADR nuevo.
- **Contexto**: i18n no afecta solo labels de UI. Tambien impacta errores seguros, catalogos, emails, reports, exports, contenido generado por IA, preferencias de usuario, contratos Web-API y comandos API-Agents. Al mismo tiempo, el codigo fuente, los contratos tecnicos, logs y telemetria deben permanecer estables en ingles.
- **Evidencia**: ADR `99-decisions/2026-07-21-i18n-by-release.md`; `docs/master-plan/analysis/i18n-strategy.md`; `docs/source-docs-refresh/audit-report.md` y `validation-report.md` registran politica de idioma canonico pendiente; `docs/09-developer-guide/07-web-development.md` declara UI teacher-facing en espanol como baseline actual.
- **Decision**: i18n se implementa dentro de cada release funcional, no como release tecnica transversal. Source code, field names, enum/error/event/metric/span codes, logs y telemetria quedan en ingles. Todo texto user-facing y contenido GenAI visible debe tener locale explicito, fallback definido y pruebas. `web` indica locale efectivo, `api` resuelve/preferencias/fallback y persiste locale cuando afecta contenido durable, `agents` recibe `outputLocale`/`contentLocale` en comandos que generan texto visible.
- **Consecuencia**: las tareas de `web/`, `api/` y `agents/` deben incorporar gate i18n cuando modifiquen pantallas, errores seguros, catalogos, emails, reports, exports o outputs GenAI. Observabilidad debe registrar locale como atributo, pero no traducir logs, metrics, traces ni event names.
- **Estado**: Resuelta el 2026-07-21.
- **Responsable sugerido**: Product/Web owner con API/Agents owners para contratos y outputs generados.
- **Fases afectadas**: 04, 05, 06.

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

## A-02 — Las historias enriquecidas de Epic 01-02 reflejan fielmente lo implementado

- **Tipo**: Supuesto.
- **Contexto**: Epic 01 y Epic 02 son las únicas con historias enriquecidas y ejecución real vía plannings cerradas/activas. US-080/US-081 tambien estan materializadas documentalmente para R01, pero todavia no tienen implementacion verificada.
- **Evidencia**: `.planning/active/008-assessment-creation/01-expansion.md` (menciona enriquecimiento vía `/us-enrich` antes de la expansión).
- **Alternativas**: n/a.
- **Recomendación**: la Fase 02 puede tratar las 15 historias de Epic 01-02 con mayor confianza de readiness implementada que las restantes, y tratar US-080/US-081 como listas para atomizacion documental dentro de R01. Las otras 45 historias requieren enriquecimiento antes de atomizar.
- **Consecuencia si resulta falsa**: la Fase 02 subestimaría trabajo pendiente en Epic 01-02.
- **Estado**: Aceptado como supuesto de trabajo.
- **Responsable sugerido**: n/a.
- **Fecha máxima de resolución**: n/a.
- **Fases afectadas**: 02.

---

## A-03 — Materiales de evento archivados y sin vigencia

- **Tipo**: Supuesto.
- **Contexto**: los materiales de evento fueron archivados en `docs/archive/2026-event/` y no imponen restricciones, fechas, formato de demo, proveedor, despliegue ni evidencias sobre el plan activo.
- **Evidencia**: `docs/archive/2026-event/README.md`.
- **Alternativas**: n/a.
- **Recomendación**: no usar esos documentos como fuente activa. Si un futuro evento o partner impone requisitos nuevos, registrarlos como decisión nueva.
- **Consecuencia si resulta falsa**: el plan activo podría volver a incorporar restricciones externas sin trazabilidad.
- **Estado**: Aceptado como supuesto de trabajo.
- **Responsable sugerido**: Carlos.
- **Fecha máxima de resolución**: antes de la Fase 04.
- **Fases afectadas**: 04.

---

## R-01 — Riesgo de claims de despliegue o proveedor sin evidencia

- **Tipo**: Riesgo (ligado a D-01).
- **Contexto**: ver D-01. Si la documentacion, demos o ventas afirman Google Cloud, Gemini o produccion sin evidencia real, se debilita la confianza en el producto.
- **Evidencia**: ver D-01.
- **Recomendación**: mantener claims por entorno. `beta` prueba funcionalidad y pilotos; `demo` prueba target Google Cloud solo cuando exista deployment/API evidence.
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

## R-03 — Evidencia de negocio insuficiente para validar el MVP

- **Tipo**: Riesgo.
- **Contexto**: `05-evidence/*` tiene plantillas o evidencia parcial. Los targets numéricos (10+ entrevistas, 5+ pilotos, 3+ pilotos pagados, 100+ submissions, etc.) no tienen avance registrado.
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
| 2026-07-19 | Actualización de estado de D-02 y D-03 | Reconciliación posterior a la Fase 02 antes de iniciar la Fase 03 | D-02, D-03 | D-02, D-03 |
| 2026-07-17 | Creación inicial | Ejecución de la Fase 01 del Master Plan Ejecutivo | Todo el documento | — |
