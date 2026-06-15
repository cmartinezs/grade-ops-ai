# Alineación De `04-architecture`

> Nota raw formateada desde la conversación sobre revisión y actualización de la documentación de arquitectura.

## Prompt Original

> Continuemos con `04-architecture`.

## Resultado General

Se revisó `04-architecture` y se dejó alineado con las decisiones consolidadas en:

- `00-project`;
- `01-business`;
- `02-product`;
- `03-ai-agents`.

## Diagnóstico Inicial

La carpeta estaba correctamente orientada, pero todavía muy inicial.

`README.md` ya definía bien:

- el propósito de la carpeta;
- el principio de trazabilidad de operaciones IA.

Pero los documentos técnicos principales estaban demasiado resumidos:

- `system-architecture.md` tenía solo componentes lógicos básicos.
- `api-design.md` solo listaba recursos representativos.
- `data-model.md` aún no definía campos, estados ni relaciones.

## Cambios Aplicados En `04-architecture`

| Documento | Modificación principal |
| --- | --- |
| `README.md` | Se convirtió en índice arquitectónico completo: inputs canónicos, principios, stack MVP recomendado, límites y cut line. |
| `system-architecture.md` | Se agregó arquitectura lógica, módulos, diagramas Mermaid, flujo de datos, patrón de ejecución de agentes, decisión modular monolith + agent runtime, evidencia y criterios de aceptación. |
| `data-model.md` | Se agregó modelo de entidades completo: Organization, UserAccount, Assessment, Rubric, Submission, Artifact, GradeSuggestion, FeedbackDraft, LearningGap, RecoveryActivity, TeacherReport, AgentExecutionLog, ApprovalEvent, UsageEvent, RevenueEvent y CostEvent. |
| `api-design.md` | Se agregó diseño REST por recursos, endpoints, comandos de workflow, errores, estados requeridos, agent-runs, evidence, usage y reglas de mutación auditable. |
| `security.md` | Se agregó clasificación de datos, auth, autorización, tenant isolation, seguridad de prompts, validación de outputs, logging redacted, privacidad, secretos, archivos y controles de abuso de costo. |
| `deployment.md` | Se agregó topología Cloud Run, ambientes, servicios Google Cloud recomendados, CI/CD, configuración, migraciones, storage, observabilidad, checklist, release strategy y rollback. |

## Decisión Técnica Clave

Recomendación MVP:

> Modular monolith + agent runtime, no microservicios completos al inicio.

## Razonamiento

Esa decisión calza mejor con el objetivo de hackathon:

- construir rápido;
- auditar bien;
- desplegar en Google Cloud;
- demostrar Gemini en producción;
- evitar complejidad distribuida prematura.

## Estado De Entrega

No se modificó GitHub directamente.

Los archivos quedaron listos para:

- reemplazar;
- revisar;
- commitear;
- usar como base técnica del MVP.

## Entregable ZIP

Se generó un ZIP con los documentos modificados:

```text
gradeops-ai-04-architecture-modified-docs.zip
```
