# Master Plan — GradeOps AI

## Proposito

Este Master Plan transforma la documentacion, decisiones, user stories y estado real de GradeOps AI en una secuencia de releases incrementales, demostrables y orientadas a valor.

## Alcance

Incluye:

- diagnostico documental y tecnico;
- decisiones, supuestos y riesgos;
- mapa de capacidades;
- inventario de user stories;
- inventario de automatizacion;
- estrategia ejecutiva de releases;
- resumen ejecutivo.

No incluye todavia los archivos detallados de cada release. Esos documentos se generan en Fase 05.

## Estado general

| Area | Estado |
|---|---|
| Fase 01: diagnostico | Completa |
| Fase 02: capacidades y US | Completa |
| Fase 03: automatizacion | Completa |
| Fase 04: estrategia de releases | Completa |
| Fase 05: documentos por release | Pendiente |
| Fase 06: validacion final | Pendiente |

## Convenciones

- `P0` indica alcance MVP/hackathon.
- `P1` indica mejora posterior o no bloqueante para primer valor.
- `US-PROPUESTA-*` indica historia faltante recomendada, aun no creada como archivo en `docs/02-product/user-stories/`.
- `Asistida`, `Supervisada` y `Automatizada` siguen los niveles definidos en la especificacion maestra.
- Los documentos de analisis son fuente de contexto; los archivos de release de Fase 05 seran la fuente operativa por release.

## Orden de lectura

1. [Diagnostico de documentacion](analysis/documentation-diagnosis.md)
2. [Decisiones y supuestos](analysis/decisions-and-assumptions.md)
3. [Mapa de capacidades](analysis/capability-map.md)
4. [Inventario de user stories](analysis/user-story-inventory.md)
5. [Inventario de automatizacion](analysis/automation-inventory.md)
6. [Estrategia de releases](analysis/release-strategy.md)
7. [Master Plan Ejecutivo](master-plan-executive.md)

## Tabla de releases

| Release | Nombre | Estado | Complejidad | Archivo esperado en Fase 05 |
|---|---|---|---|---|
| R01 | Assessment Creation + Evidence Backbone | Documentada | M | [release-01-assessment-creation-evidence-backbone.md](releases/release-01-assessment-creation-evidence-backbone.md) |
| R02 | Open Graded Feedback Thin Slice | Documentada | L | [release-02-open-graded-feedback-thin-slice.md](releases/release-02-open-graded-feedback-thin-slice.md) |
| R03 | Open Cohort Report and Impact | Planificada | M | `releases/release-03-open-cohort-report-impact.md` |
| R04 | Closed Question Bank to Snapshot | Planificada | L | `releases/release-04-closed-question-bank-snapshot.md` |
| R05 | Closed Student Response and Item Analytics | Planificada | L | `releases/release-05-closed-response-item-analytics.md` |
| R06 | Business Evidence and Hackathon Compliance | Planificada | M | `releases/release-06-business-evidence-hackathon-compliance.md` |
| R07 | Open Workflow Refinements | Roadmap | M | `releases/release-07-open-workflow-refinements.md` |
| R08 | Closed and Curriculum Refinements | Roadmap | M | `releases/release-08-closed-curriculum-refinements.md` |

## Enlaces relativos

- [Estrategia de releases](analysis/release-strategy.md)
- [Resumen ejecutivo](master-plan-executive.md)
- [Especificacion maestra](../.prompting/master-plan-prompts/master-plan-specification.md)
- [Prompts de fases](../.prompting/master-plan-prompts/README.md)

## Leyenda de estados

| Estado | Significado |
|---|---|
| Completa | Artefacto generado y revisado para la fase actual. |
| Planificada | Release definida a nivel estrategico; falta archivo detallado de Fase 05. |
| Roadmap | Release posterior al corte MVP/hackathon. |
| Pendiente | Trabajo aun no ejecutado. |
| Bloqueada | Requiere decision externa antes de avanzar. |

## Ultima actualizacion

2026-07-19.

## Reglas de mantenimiento

- No modificar artefactos de fases anteriores sin registrar el cambio en su historial.
- No crear archivos de release fuera de Fase 05.
- Cada release debe conservar trazabilidad hacia capacidades, US, automatizaciones y evidencias.
- Toda nueva decision relevante debe registrarse en `analysis/decisions-and-assumptions.md`.
- Mantener D-01 visible hasta resolver entorno `demo`/`beta`.
- No mover P1 al MVP sin retirar o dividir otra carga equivalente.
