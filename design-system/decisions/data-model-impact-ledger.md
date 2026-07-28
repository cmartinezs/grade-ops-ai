<a id="top"></a>

# Data Model Impact Ledger

## Propósito

Registrar capacidades originadas o expuestas por decisiones UI/UX que podrían afectar dominio, datos, API, eventos, seguridad, agentes o migraciones. El ledger no define el esquema ni autoriza implementación.

## Estados

- `Open`: impacto identificado, pendiente de análisis técnico.
- `In analysis`: alternativas e invariantes en evaluación.
- `Resolved`: decisión técnica documentada y enlazada.
- `Deferred`: aceptado fuera del alcance actual con condición de revisión.
- `Rejected`: la capacidad no se implementará.

## Certeza y horizonte

La certeza distingue una decisión aceptada de una hipótesis pendiente:

- `Confirmed`: principio o necesidad aceptada internamente.
- `Hypothesis`: propuesta que requiere evidencia adicional.
- `Open question`: decisión todavía no tomada.

El horizonte evita confundir preparación arquitectónica con alcance inmediato:

- `Foundation`: debe resolverse antes de implementar la capacidad afectada.
- `MVP`: debe formar parte del primer producto validable.
- `Future`: se prevé compatibilidad, pero no se implementa ahora.

## Registro

| ID | Decisión UI/UX | Capacidad requerida | Impacto potencial | Certeza | Horizonte | Estado | Resolución técnica |
|---|---|---|---|---|---|---|---|
| DMI-001 | Control humano sobre IA | Origen, revisión y publicación diferenciados | Estados o dimensiones separadas; actor; timestamps; versión; `ApprovalEvent`; contratos Web/API/Agents | Confirmed | Foundation | Open | Pendiente |
| DMI-002 | Acciones críticas | Borradores, autoguardado, historial, recuperación y operaciones masivas seguras | Versionado; optimistic locking; idempotency key; batch status; audit trail; eventos compensatorios | Confirmed | Foundation | Open | Pendiente |
| DMI-003 | Privacidad y permisos | Autorización contextual y aislamiento de recursos | Organization, course, section, membership, ownership, permissions, retention y auditoría | Confirmed | Foundation | Open | Pendiente |
| DMI-004 | Procesos asíncronos | Progreso real, resultados parciales, reintento y cancelación | Job, attempts, item results, error codes, idempotencia, polling/SSE y recuperación | Confirmed | Foundation | Open | Pendiente |
| DMI-005 | Internacionalización | Locale, idioma de salida, zona horaria y configuración académica independientes | Preferencias de usuario e institución; resolución de locale; contratos `code + parameters`; contenido multilingüe | Confirmed | Foundation | Open | Pendiente |
| DMI-006 | Responsive diferenciado | Capacidades variables por contexto de uso | Capabilities/read models; autorización no dependiente del cliente; continuidad entre dispositivos | Hypothesis | MVP | Open | Pendiente |
| DMI-007 | WCAG 2.2 AA | Preferencias y alternativas accesibles cuando corresponda | Semántica de componentes, contenido alternativo, announcements y evidencia de pruebas; persistencia solo si se justifica | Confirmed | Foundation | Open | Pendiente |
| DMI-008 | Perfil evaluativo transversal | Representar evaluaciones mixtas mediante dimensiones combinables y extensiones disciplinares con contrato | `Assessment`; `AppliedAssessmentProfile`; evidence requirements; catálogos gobernados; esquema y versión de extensiones; compatibilidad; localización | Confirmed | Foundation | In analysis | [Fundamentos del dominio evaluativo](2026-07-27-assessment-domain-foundations.md); diseño técnico pendiente |
| DMI-009 | Plantillas reutilizables | Crear desde arquetipos de sistema o docente, personalizar, clonar y conservar origen sin mutaciones retroactivas | `AssessmentTemplate`; ciclos separados de identidad y versión; publicación atómica; ownership; scope; visibility; usage policy; lineage; snapshots; deprecation; idempotencia; auditoría | Confirmed | MVP | In analysis | [Fundamentos del dominio evaluativo](2026-07-27-assessment-domain-foundations.md) y [ciclo y gobierno de plantillas](2026-07-27-assessment-template-lifecycle-governance.md); persistencia y contratos técnicos pendientes |
| DMI-010 | Ciclos de vida independientes | Separar plantilla, preparación, aplicación, revisión y publicación de resultados; permitir correcciones trazables | Máquinas de estado; revisiones; audit trail; excepciones individuales; impacto y reproceso; optimistic locking; permisos | Confirmed | Foundation | In analysis | [Fundamentos del dominio evaluativo](2026-07-27-assessment-domain-foundations.md); catálogo de transiciones pendiente |
| DMI-011 | Transiciones explícitas DDD | Asociar cada transición con comando o trigger, guardas, acción, evento, idempotencia y autorización | Aggregates; application ports; domain events; outbox o mecanismo equivalente; process managers; observabilidad y pruebas de transición | Confirmed | Foundation | In analysis | [Fundamentos del dominio evaluativo](2026-07-27-assessment-domain-foundations.md); [catálogo conductual de plantillas](2026-07-27-assessment-template-lifecycle-governance.md) definido; restantes ciclos y límites técnicos pendientes |
| DMI-012 | Gobernanza y compartición de plantillas | Separar publicación, propiedad, alcance, visibilidad, derecho de uso, clonación y aprobación institucional | Capacidades granulares; policy evaluation; `TemplatePublicationRequest`; revisión exacta o hash; process manager; índices de descubrimiento; invalidación de caché; linaje; aislamiento institucional | Confirmed | Foundation | In analysis | [Ciclo y gobierno de plantillas](2026-07-27-assessment-template-lifecycle-governance.md); MVP personal/plataforma confirmado y workflow institucional diferido |
| DMI-013 | Política de calificación transparente | Componer, validar, publicar, simular, ejecutar y explicar reglas de cálculo deterministas | `GradingPolicy`; versiones inmutables; grafo tipado; catálogo de operaciones; `CalculationExecution`; revisiones de inputs y resultados; análisis de impacto; autorización; pruebas doradas | Confirmed | Foundation | In analysis | [Política de calificación transparente](2026-07-27-transparent-grading-policy.md); lenguaje físico, persistencia y contratos pendientes |
| DMI-014 | Temporalidad académica | Separar periodo, ejecución de sección, matrícula temporal, elegibilidad histórica y cierre | `AcademicPeriod`; `TeachingSection`; `SectionEnrollment`; vigencia; revisiones; participantes por administración; copia segura; transferencia y corrección; consultas temporales | Confirmed | Foundation | In analysis | [Periodos, secciones y matrículas](2026-07-27-academic-period-section-enrollment.md); límites de aggregates e integración institucional pendientes |
| DMI-015 | Excepciones y resultados efectivos | Conservar hechos, autorizaciones, intentos y revisiones; seleccionar el valor que participa en el cálculo | `AcademicExceptionCase`; `AssessmentAttempt`; `EffectiveResultDecision`; participación; atrasos; pendientes; reapertura; rectificación; evidencia sensible; autorización y notificaciones | Confirmed | Foundation | In analysis | [Excepciones, intentos y resultados efectivos](2026-07-27-assessment-exceptions-effective-results.md); workflows físicos y contratos pendientes |

## Reglas de actualización

Cada entrada debe evolucionar con:

- certeza, horizonte y evidencia de origen;
- entidad, agregado o módulo potencialmente afectado;
- estados, transiciones e invariantes;
- datos persistidos y política de retención;
- contrato, evento o mecanismo asíncrono;
- autorización, privacidad y auditoría;
- migración y compatibilidad;
- ADR técnico y pruebas que resuelven el impacto.

No consolidar elaboración, revisión, publicación, origen IA y estado de job en un único `status` sin demostrar que representan una sola máquina de estados.

## Gate de resolución

Antes de que una release implemente una capacidad registrada:

1. definir invariantes y fuente de verdad;
2. resolver el impacto técnico en el planning correspondiente;
3. crear o actualizar el ADR técnico cuando aplique;
4. enlazar contratos, migraciones, eventos y pruebas;
5. cambiar la entrada a `Resolved` o documentar un `Deferred` explícito.

---

[← Índice de decisiones](README.md) · [Siguiente: Volver al índice →](README.md) · [↑ Volver al inicio](#top)
