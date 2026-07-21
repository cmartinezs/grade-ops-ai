# Inventario de user stories — GradeOps AI

> Fase 02 — Capacidades y user stories del Master Plan Ejecutivo.
> No se define aquí la secuencia final de releases (Fase 04).

## Resumen cuantitativo

- **62 historias** en 13 épicas + 5 historias `out-of-scope` (67 en total).
- **15 historias (24%)** completamente enriquecidas (Story + AC + DoD + Technical Notes + Dependencies + Complejidad): Epic 01 (12) y Epic 02 (3).
- **47 historias (76%)** esqueléticas (solo Story + Acceptance Criteria): Epics 03 a 13.
- **41 historias P0**, **17 P1**, **1 con prioridad condicional** (US-OUT-004, "P1 closed / P2 open"), **5 Out** de alcance permanente/semi-permanente.
- **Implementación real verificada** (código + planning cerrada o activa): 15/62 historias — las 12 de Epic 01 (9 completamente, más US-014 y US-015 con matices) y 2-3 de Epic 02 (US-010, US-011 implementadas; US-012 en progreso). El resto (47 historias, 76%) es backlog sin ejecución iniciada.
- **0 colisiones de ID** tras la resolución de D-03 (renumeración de Epic 01: US-010/011/012 → US-013/014/015).
- **2 archivos con corte de historias P0 del validacion MVP divergente entre sí** — ver "Recomendaciones de normalización".

## Tabla de historias existentes

Leyenda de **Estado**: `READY` (cumple Definition of Ready), `NOT READY — enriquecer` (le faltan elementos del DoR, principalmente Dependencias/Restricciones/Datos a nivel de historia individual), `READY — implementada` (ya construida y verificada).

| ID | Título | Epic | Pri. | Actor | Objetivo / Valor | Estado | Dependencias | Reglas de negocio clave | Observaciones | Acción recomendada |
|---|---|---|---|---|---|---|---|---|---|---|
| US-001 | Teacher Login | 01 | P0 | Teacher | Acceso seguro al workspace | READY — implementada | US-006 o US-008 | Auth vía Firebase; scoping server-side | Planning `001-teacher-onboarding` (DONE) | Ninguna |
| US-002 | Assessment Dashboard | 01 | P0 | Teacher | Ver assessments y estado en un vistazo | READY — implementada | US-001 | Estados desde state machine de `api/` | Depende de contratos aún no construidos (Epics 03/05/06/08/09) para poblar status real | Ninguna a nivel de historia; su contenido completo depende de otras épicas |
| US-003 | Pilot Account Flag | 01 | P1 | Operator | Marcar cuenta piloto/free/paid para evidencia | READY — implementada | US-006 o US-008 | Flags `pilot`/`related_party` auditables | **Open point propio**: sin mecanismo de acceso definido para "Operator" | Ver US-PROPUESTA-01 |
| US-004 | Sign-Out and Session Expiry | 01 | P1 | Teacher | Cerrar sesión / expirar sesiones inactivas | READY — implementada | US-001 | Enforcement server-side | — | Ninguna |
| US-005 | Dashboard Empty State | 01 | P1 | Teacher nuevo | Dashboard útil sin assessments | READY — implementada | US-002 | Empty state desaparece al crear el primero | — | Ninguna |
| US-006 | Teacher Account Provisioning | 01 | P0 | Operator | Aprovisionar cuentas de docente para pilotos | READY — implementada | Firebase setup | Coexiste con self-registration | Mismo open point que US-003 | Ver US-PROPUESTA-01 |
| US-007 | Cross-Teacher Access Denial | 01 | P0 | Teacher | Denegar acceso a datos de otro docente | READY — implementada | US-001 | 404 en vez de 403; intentos logueados | Define patrón de ownership-scoping reutilizable | Ninguna |
| US-008 | Teacher Self-Registration | 01 | P0 | Teacher | Crear cuenta propia sin operador | READY — implementada | Firebase setup | API nunca almacena password en crudo | — | Ninguna |
| US-009 | Email Verification | 01 | P0 | Teacher | Verificar email antes de usar workspace | READY — implementada | US-008 | Enforcement server-side vía claim | — | Ninguna |
| US-013 | Postman Collection | 01 | P1 | Developer | Colección Postman para probar la API | READY | US-001, US-006, US-003, US-008 | No commitear secretos reales | Historia de tooling, no de producto; implementación no verificada en esta fase | Confirmar si ya existe `docs/postman/` antes de priorizarla |
| US-014 | Google Sign-In for Teachers | 01 | P0 | Teacher | Sign-in/registro con Google sin password | READY — implementada | US-008, US-009, infra | Pre-verificación de email; merge automático de cuentas | Planning `002-google-sign-in` (DONE) | Ninguna |
| US-015 | Password Recovery | 01 | P1 | Teacher | Restablecer password sin soporte | READY — implementada (con discrepancias) | US-008, US-001 | Mensaje neutral anti-enumeración; expiración 30 min | **2 discrepancias ⚠️ marcadas en el propio DoD** (método HTTP y códigos de error) sin resolver | Cerrar las 2 discrepancias antes de considerarla DONE |
| US-010 | Assessment Brief Intake | 02 | P0 | Teacher | Describir meta de aprendizaje para brief de IA | READY — implementada | Epic 01 | Brief persiste antes de invocar al agente | Planning `api/003-assessment-creation`, `agents/001-assessment-creation` | Ninguna |
| US-011 | Assessment Draft Generation | 02 | P0 | Teacher | Generar borrador de assessment desde el brief | READY — implementada | US-010 | Output estructurado validado antes de mostrar | Groq es el proveedor real por defecto (ver D-04); Gemini no verificado end-to-end | Ninguna funcional; documentar decisión de proveedor (D-04) |
| US-012 | Assessment Draft Regeneration | 02 | P1 | Teacher | Regenerar borrador con notas de ajuste | READY — en progreso | US-011 | Versionado, nunca sobreescribe | `web/001-assessment-creation` aún en Deepening (no atomizada) | Continuar ejecución ya iniciada |
| US-020 | Rubric Draft Generation | 03 | P0 | Teacher | Borrador de rúbrica del assessment | NOT READY — enriquecer | Epic 02 (implícita) | Total weight visible | Esquelética; sin DoD/Technical Notes/Dependencies propias | Enriquecer antes de atomizar |
| US-021 | Rubric Validation | 03 | P0 | Teacher | Revisión de ambigüedad/pesos de la rúbrica | NOT READY — enriquecer | US-020 (implícita) | Flags deben almacenarse | Esquelética | Enriquecer antes de atomizar |
| US-022 | Rubric Approval | 03 | P0 | Teacher | Aprobar rúbrica antes del grading | NOT READY — enriquecer | US-021 (implícita) | Rúbrica aprobada queda bloqueada | Esquelética | Enriquecer antes de atomizar |
| US-023 | Rubric Version History | 03 | P1 | Teacher | Ver historial de versiones de rúbrica | NOT READY — enriquecer | US-022 (implícita) | Versión aprobada debe marcarse claramente | Esquelética | Enriquecer antes de atomizar |
| US-030 | Manual Student Submission Creation | 04 | P0 | Teacher | Agregar submission manualmente | NOT READY — enriquecer | Epic 03 (implícita) | Requiere `student_identifier` | Esquelética | Enriquecer antes de atomizar |
| US-031 | File Upload Student Submission | 04 | P0 | Teacher | Subir archivos como submission | NOT READY — enriquecer | US-030 (implícita) | Error claro en tipo no soportado | Esquelética | Enriquecer antes de atomizar |
| US-032 | Bulk Submission Intake | 04 | P1 | Teacher | Importar varias submissions a la vez | NOT READY — enriquecer | US-030/031 (implícita) | Errores de importación visibles | Esquelética | Enriquecer antes de atomizar |
| US-033 | Submission Status | 04 | P0 | Teacher | Ver estado de procesamiento | NOT READY — enriquecer | US-030 (implícita) | 6 estados definidos (received…rejected) | Esquelética | Enriquecer antes de atomizar |
| US-034 | Graded Submission Usage Count | 04 | P0 | Operator | Contar submission calificada contra uso del plan | NOT READY — enriquecer | Epic 09/10 (implícita) | Uso se consume en análisis, no en creación | Esquelética; regla de negocio de billing relevante | Enriquecer antes de atomizar |
| US-040 | Rubric-Based Grading Suggestion | 05 | P0 | Teacher | Sugerencia de calificación por criterio | NOT READY — enriquecer | Epic 03, 04 (implícita) | Output siempre "sugerencia" | Esquelética; Grading Agent | Enriquecer antes de atomizar |
| US-041 | Uncertainty Flags | 05 | P0 | Teacher | Flags de incertidumbre en sugerencias | NOT READY — enriquecer | US-040 (implícita) | No aprobación bulk silenciosa de outputs inciertos | Esquelética | Enriquecer antes de atomizar |
| US-042 | Teacher Edit of Score | 05 | P0 | Teacher | Editar score sugerido por IA | NOT READY — enriquecer | US-040 (implícita) | Sugerencia original trazable | Esquelética | Enriquecer antes de atomizar |
| US-043 | Reject AI Suggestion | 05 | P0 | Teacher | Rechazar sugerencia de IA | NOT READY — enriquecer | US-040 (implícita) | Rechazo registrado como evidencia | Esquelética | Enriquecer antes de atomizar |
| US-050 | Individual Feedback Draft | 06 | P0 | Teacher | Borrador de feedback por estudiante | NOT READY — enriquecer | Epic 05 (implícita) | Feedback legible por el estudiante | Esquelética; Feedback Agent | Enriquecer antes de atomizar |
| US-051 | Feedback Approval | 06 | P0 | Teacher | Aprobar feedback antes de entrega | NOT READY — enriquecer | US-050 (implícita) | Sin `ApprovalEvent` no hay entrega | Esquelética | Enriquecer antes de atomizar |
| US-052 | Tone Adjustment | 06 | P1 | Teacher | Ajustar tono del feedback | NOT READY — enriquecer | US-050 (implícita) | Feedback final sigue editable | Esquelética | Enriquecer antes de atomizar |
| US-060 | Learning Gap Summary | 07 | P0 | Teacher | Resumen de brechas del cohorte | NOT READY — enriquecer | Epic 05 (implícita) | Vinculado a criterios de rúbrica | Esquelética; Learning Gap Agent | Enriquecer antes de atomizar |
| US-061 | Recovery Activity Suggestion | 07 | P0 | Teacher | Sugerencia de actividad de recuperación | NOT READY — enriquecer | US-060 (implícita) | ≥1 actividad por gap detectado | Esquelética; Recovery Agent | Enriquecer antes de atomizar |
| US-062 | Student-Specific Recovery Notes | 07 | P1 | Teacher | Next steps opcionales por estudiante | NOT READY — enriquecer | US-061 (implícita) | Nunca se entrega automáticamente | Esquelética | Enriquecer antes de atomizar |
| US-070 | Assessment Report | 08 | P0 | Teacher | Reporte consolidado del ciclo | NOT READY — enriquecer | Epic 01, 05, 06, 07 | Reporte logueado por Teacher Report Agent | Esquelética; el epic con más prerequisitos funcionales | Enriquecer antes de atomizar |
| US-071 | Export Report | 08 | P1 | Teacher | Exportar/compartir el reporte | NOT READY — enriquecer | US-070 (implícita, no declarada en el archivo) | No exponer prompts internos del agente | Esquelética; dependencia de US-070 no está escrita en el propio archivo | Enriquecer, incluyendo dependencia explícita |
| US-080 | Agent Execution Log | 09 | P0 | Operator | Loguear cada ejecución de agente | NOT READY — enriquecer | Epic 01; consumida por Epics 02-08/11-13 | Runs fallidos no se descartan silenciosamente | Esquelética; entidad central del sistema de evidencia | Enriquecer; considerar tratarla como infraestructura transversal (ver `capability-map.md`) |
| US-081 | Cost Estimate Per Run | 09 | P0 | Operator | Estimar costo/tokens por ejecución | NOT READY — enriquecer | US-080 (implícita) | Costo agregable por assessment y customer | Esquelética; sin fórmula de cálculo especificada | Enriquecer, incluyendo categoría de costo Groq (ver D-04/D-06) |
| US-082 | Business Evidence Dashboard | 09 | P0 | Operator | Dashboard interno de uso/costo/evidencia | NOT READY — enriquecer | US-080, US-081 (implícita) | Datos reales de BD, sin mocks | Esquelética; alcance amplio (6 AC de dashboard) — candidata a vigilar tamaño en Fase 04 | Enriquecer; validar tamaño (podría acercarse a L) |
| US-083 | Time Saved Estimate | 09 | P1 | Teacher/Operator | Estimar tiempo ahorrado | NOT READY — enriquecer | US-080 (implícita) | Debe etiquetarse como estimación | Esquelética; ambigüedad en cálculo del baseline default | Enriquecer, resolver ambigüedad de baseline |
| US-090 | Usage Limits | 10 | P0 | Operator | Trackear consumo vs. límite de plan | NOT READY — enriquecer | Epic 01, 04, 09 | Overuse se reporta, no bloquea (regla MVP) | Esquelética | Enriquecer antes de atomizar |
| US-091 | Payment Evidence Link | 10 | P1 | Operator | Vincular evidencia de pago a un customer | NOT READY — enriquecer | US-090 (implícita) | `related_party` obligatorio si el flag está activo | Esquelética; regla de negocio vive solo en el README del epic, no en la historia | Enriquecer, trasladar la regla al cuerpo de la historia |
| US-100 | Tag Question With Subject/Outcome | 11 | P0 | Teacher | Etiquetar pregunta con metadatos curriculares | NOT READY — enriquecer | Epic 01 | Pregunta sin tags no puede pasar a `active` | Esquelética; vínculo con Question Generation Agent | Enriquecer antes de atomizar |
| US-101 | Filter Question Bank by Curriculum Metadata | 11 | P0 | Teacher | Filtrar banco por metadatos | NOT READY — enriquecer | US-100 (implícita) | Filtros combinables deben ser precisos | Esquelética; incluye caso de prueba concreto | Enriquecer antes de atomizar |
| US-102 | Generate Curriculum Structure With AI | 11 | P1 | Teacher | Generar estructura curricular con IA | NOT READY — enriquecer | Epic 01 | Estructura entra en `pending_review` | **No confirma textualmente reutilizar el Question Generation Agent** — verificar contra `03-ai-agents/` | Enriquecer y confirmar agente responsable |
| US-103 | Validate Assessment Curriculum Coverage | 11 | P1 | Teacher | Alertar cobertura curricular incompleta | NOT READY — enriquecer | US-100, Epic 12 (implícita) | Alerta no bloqueante | Esquelética; vínculo directo con US-113 | Enriquecer, declarar dependencia con Epic 12 |
| US-110 | Generate Question Batch With AI | 12 | P0 | Teacher | Generar lote de preguntas objetivas | NOT READY — enriquecer | Epic 01, 11 | Output en `pending_review`; run logueado | Esquelética; Question Generation Agent explícito | Enriquecer antes de atomizar |
| US-111 | Review AI-Generated Questions | 12 | P0 | Teacher | Revisar/aprobar/editar/rechazar preguntas | NOT READY — enriquecer | US-110 (implícita) | Toda acción de curación se audita | Esquelética; Distractor Quality + Ambiguity Review solo nombrados en README del epic, no en la historia | Enriquecer, nombrar agentes en el cuerpo de la historia |
| US-112 | Question Bank | 12 | P0 | Teacher | Banco de preguntas buscable/filtrable | NOT READY — enriquecer | US-111 (implícita) | No muestra rechazadas/pending por default | Esquelética; sin agente propio (capa de persistencia) | Enriquecer antes de atomizar |
| US-113 | Compose Closed Assessment From Bank | 12 | P0 | Teacher | Componer assessment balanceado desde el banco | NOT READY — enriquecer | US-112 (implícita) | Valida answer key y aprobación antes de publicar | Esquelética; Assessment Assembly Agent explícito | Enriquecer antes de atomizar |
| US-114 | Publish Closed Assessment and Freeze Snapshot | 12 | P0 | Teacher | Publicar con snapshot inmutable | NOT READY — enriquecer | US-113 (implícita) | No editable estructuralmente tras publicar | Esquelética; sin agente propio | Enriquecer antes de atomizar |
| US-115 | Annul Question and Recalculate | 12 | P1 | Teacher | Anular pregunta post-grading y recalcular | NOT READY — enriquecer | US-114 (implícita) | Resultados originales preservados en audit trail | Esquelética; sin agente propio | Enriquecer antes de atomizar |
| US-120 | Create Learner List | 13 | P0 | Teacher | Crear/importar lista de estudiantes | NOT READY — enriquecer | Epic 01, 09, 12 | Estudiante no requiere cuenta | Esquelética; sin agente | Enriquecer antes de atomizar |
| US-121 | Send Assessment Access Links | 13 | P0 | Teacher | Enviar links firmados por estudiante | NOT READY — enriquecer | US-120 (implícita) | Token único por learner+assessment | Esquelética; sin agente | Enriquecer antes de atomizar |
| US-122 | Student Response via Link | 13 | P0 | Student | Responder assessment cerrado sin cuenta | NOT READY — enriquecer | US-121, US-114 (implícita) | Validación de token; link se marca usado | Esquelética; grading determinístico posterior no descrito en el cuerpo de la historia | Enriquecer, declarar vínculo con grading determinístico |
| US-123 | Publish Results and Student Result Access | 13 | P0 | Teacher | Publicar resultados vía link seguro | NOT READY — enriquecer | US-122 (implícita) | Aislamiento fuerte entre estudiantes | Esquelética | Enriquecer antes de atomizar |
| US-124 | Item Analytics Report | 13 | P0 | Teacher | Analítica de dificultad/acierto por pregunta | NOT READY — enriquecer | US-123 (implícita) | Docente revisa antes de compartir | Esquelética; Item Analytics Agent explícito; cierra el ciclo Closed | Enriquecer antes de atomizar |

### Out of scope (no requieren AC/DoD — solo justificación)

| ID | Título | Prioridad | Justificación |
|---|---|---|---|
| US-OUT-001 | Student Chatbot | Out | Fuera del modelo operado-por-docente; riesgo de moderación/seguridad |
| US-OUT-002 | Fully Autonomous Grading | Out | Restricción de producto: aprobación docente es central al modelo de confianza |
| US-OUT-003 | Full LMS | Out | GradeOps es operaciones de evaluación, no LMS |
| US-OUT-004 | Physical Paper Ingestion (OMR/OCR) | **P1 closed / P2 open** (no es exclusión pura) | Diferida a post-MVP; requiere pipeline de ingesta separado |
| US-OUT-005 | Enterprise SSO | Out | MVP apunta a docentes individuales/equipos pequeños |

## Historias NOT READY

**47 de 62 historias (Epics 03-13)** comparten el mismo patrón de incumplimiento del Definition of Ready: tienen Actor, Valor y Criterios de aceptación, pero **carecen de Dependencias explícitas, Restricciones y Datos requeridos detallados a nivel de historia individual** (esa información solo vive, parcialmente, en el README del epic). Esto no es un defecto de calidad del contenido — es el estado esperado del backlog antes de que una épica entre a ejecución (ver `A-02` en `decisions-and-assumptions.md`: Epic 01-02 se enriquecieron recién al iniciar su planning vía `/us-enrich`). Se listan explícitamente en la tabla anterior con acción recomendada "Enriquecer antes de atomizar" — es decir, deben pasar por `/us-enrich` (o equivalente) inmediatamente antes de que la Fase 04/05 las asigne a una release, no antes.

Casos con matiz adicional dentro de este grupo:
- **US-102**: no solo le falta enriquecimiento — tiene una **afirmación no verificada** (reutilización del Question Generation Agent) que debe confirmarse contra `03-ai-agents/` antes de enriquecerse.
- **US-071, US-091, US-103, US-122**: tienen una dependencia real y conocida que no quedó escrita en el cuerpo de la historia (vive solo en el README del epic o se infiere) — al enriquecerlas, esa dependencia debe trasladarse explícitamente.
- **US-015** (única historia con ejecución real fuera de Epic 01-02 "puro"): no es NOT READY, pero tiene **2 discrepancias auto-declaradas sin resolver** entre diseño y DoD — debe tratarse como deuda técnica a cerrar, no como backlog nuevo.

## Historias duplicadas

Ninguna tras la resolución de D-03 en esta sesión (renumeración de Epic 01). Antes de esa corrección, US-010/011/012 estaban duplicadas literalmente entre Epic 01 y Epic 02 — ver `decisions-and-assumptions.md`, D-03, para el registro histórico.

## Historias a dividir o combinar

No se detectaron candidatas fuertes a división o combinación con la información disponible en esta fase. Dos observaciones menores para vigilar en la Fase 04 (sizing de releases):

- **US-082 (Business Evidence Dashboard)**: 6 AC cubriendo múltiples fuentes de datos (assessments, submissions, feedback, agent runs, costo, estado de piloto/cliente) en una sola historia — candidata a evaluarse como potencial S/M/L grande o a dividirse por sección de dashboard si al enriquecerla resulta demasiado extensa.
- **US-070/US-071 (Assessment Report / Export Report)**: podrían combinarse en una sola historia ("Reporte y exportación") dado que US-071 depende enteramente de US-070 y es de solo 3 AC — o mantenerse separadas si el export se prioriza distinto (P1 vs P0). Se deja como decisión de la Fase 04, no se fuerza aquí.

## Historias faltantes propuestas

Identificadas a partir de los vacíos de la Fase 01 y del propio contenido de las historias existentes (open points citados textualmente en US-003/US-006, ausencia de observabilidad de servicio, ausencia de manejo de errores/reintentos visible al usuario, ausencia de privacidad/eliminación de datos de estudiante).

| ID propuesto | Título | Categoría | Necesidad |
|---|---|---|---|
| US-PROPUESTA-01 | Operator Authentication and Role Access | Seguridad / Administración | US-003 y US-006 citan textualmente que no existe mecanismo de acceso definido para el actor "Operator". Sin esta historia, C1 (Identidad y Acceso) queda incompleta para el único actor que aprovisiona cuentas piloto y flaggea evidencia de negocio. |
| US-PROPUESTA-02 | Student Data Deletion / Anonymization on Request | Privacidad / Eliminación | Ninguna historia cubre eliminación o anonimización de datos de un `LearnerRef` a solicitud (docente o estudiante). Relevante para cumplimiento y para la promesa de privacidad de `06-ux/student-access-ux.md`. |
| US-PROPUESTA-03 | Agent Run Retry and Failure Recovery (Teacher-Facing) | Errores / Reintentos | `AgentExecutionLog` (US-080) registra fallos, pero ninguna historia define qué ve o puede hacer el docente cuando una ejecución de agente falla (reintentar, cancelar, ver motivo). |
| US-PROPUESTA-04 | Idempotent Assessment/Question Generation Requests | Idempotencia | Ninguna historia garantiza explícitamente que un doble envío de brief o de solicitud de generación de preguntas no dispare dos ejecuciones de agente duplicadas (con doble costo). |
| US-PROPUESTA-05 | Cost/Token Budget Alerting | Costos | US-081/US-090 solo registran y reportan consumo; ninguna historia alerta proactivamente al operador cuando el costo o el uso se acerca a un umbral (relevante dado el presupuesto de reserva de caja US$500-1,000 citado en `cost-model.md`). |
| US-PROPUESTA-06 | Agent Service Health Visibility | Observabilidad | El ADR `agent-runtime-separation.md` menciona un health check del servicio de agentes, pero ninguna historia expone su estado al operador/docente en el dashboard. |
| US-PROPUESTA-07 | AI Provider Transparency and Fallback Notice | Configuración / Agentes | Dado el hallazgo de Fase 01 (Groq real por defecto, Gemini como alternativa no verificada end-to-end — D-04), ninguna historia cubre qué debe ver el docente/operador cuando el sistema usa un proveedor de fallback, ni cómo se selecciona. |
| US-PROPUESTA-08 | Pilot Kickoff and Onboarding Checklist | Pilotos reales / Evidencia comercial | `01-business/customer-discovery.md` y `00-project/roadmap.md` describen el proceso de conseguir pilotos a nivel estratégico, pero no existe una historia de producto que capture qué necesita el operador para dar de alta y monitorear un piloto real de punta a punta (vínculo con US-003, US-006, US-091). |

Estas 8 propuestas quedan marcadas `US PROPUESTA` — no se han creado como archivos en `02-product/user-stories/`; su creación real es una decisión de producto para después de este Master Plan, no una acción de esta fase.

## Recomendaciones de normalización

1. **Reconciliar los dos "cortes P0 de validacion MVP" divergentes**: `docs/02-product/user-stories.md` y `docs/02-product/user-stories/README.md` son archivos distintos, ambos con una sección "MVP Story Cut" — el primero lista 23 IDs, el segundo 27 IDs (agrega US-006, US-007, US-008, US-009 al principio). Ninguno de los dos refleja todavía la decisión D-02 (Closed = P0): ninguno incluye historias de Epics 11-13. Recomendación: unificar en un solo archivo fuente (sugerido: `user-stories/README.md`, que ya es más completo) y actualizar el corte P0 para incluir el camino crítico Closed confirmado en D-02, referenciando `capability-map.md` § "Capacidades validacion MVP" para la lista completa.
2. **Formato de Acceptance Criteria inconsistente**: Epic 01-02 usan checkboxes `- [ ]`; Epic 03-13 usan bullets simples. Homologar al formato de `_template-user-story.md` al enriquecer cada historia.
3. **Trasladar reglas de negocio del README del epic al cuerpo de la historia individual** cuando esa regla aplica a una sola historia (casos detectados: US-091 con `related_party`; US-111 con nombres de agente; US-122 con el vínculo a grading determinístico).
4. **US-013 (Postman Collection)**: verificar si `docs/postman/` ya existe antes de incluirla en cualquier release — es la única historia de las 62 sin ninguna señal de estado real conocida en esta fase.
5. **Cerrar las 2 discrepancias ⚠️ de US-015** como parte de la deuda técnica de Epic 01, no como trabajo nuevo de Epic 03+.

## Trazabilidad hacia capacidades

Ver `capability-map.md` para el mapeo completo epic↔capacidad↔agente. Resumen de trazabilidad por capacidad:

| Capacidad | Historias |
|---|---|
| C1 | US-001 a US-009, US-013, US-014, US-015 |
| C2 | US-120, US-121, US-122, US-123 |
| C3 | US-010, US-011, US-012 |
| C4 | US-020, US-021, US-022, US-023 |
| C5 | US-100, US-101, US-102, US-103 |
| C6 | US-110, US-111, US-112, US-113, US-114, US-115 |
| C7 | US-030, US-031, US-032, US-033, US-034 |
| C8 | US-040, US-041, US-042, US-043 |
| C9 | US-050, US-051, US-052 |
| C10 | US-060, US-061, US-062 |
| C11 | US-070, US-071 |
| C12 | US-124 |
| C13 | US-080, US-081, US-083 |
| C14 | US-082 |
| C15 | US-090, US-091 |

Toda historia priorizada (P0/P1) queda asociada a exactamente una capacidad — no se detectaron historias huérfanas ni historias asociadas a más de una capacidad primaria.

## Historial de cambios

| Fecha | Cambio | Motivo | Elementos afectados | Decisión asociada |
|---|---|---|---|---|
| 2026-07-17 | Creación inicial | Ejecución de la Fase 02 del Master Plan Ejecutivo, sobre el catálogo ya renumerado (D-03) y con D-02 resuelta (Closed = P0) | Todo el documento | D-02, D-03 |
