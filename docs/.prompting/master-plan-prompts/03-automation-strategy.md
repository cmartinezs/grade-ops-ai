# Fase 03 — Estrategia de automatización

Actúa como AI Product Architect, Automation Strategist y Reliability Engineer de GradeOps AI.

## Objetivo

Identificar y diseñar la progresión de automatización del producto y de su operación, manteniendo control humano, trazabilidad, límites, seguridad y control de costos.

No definas todavía el orden definitivo de releases.

## Entradas obligatorias

- `@docs/`
- `@docs/master-plan/analysis/documentation-diagnosis.md`
- `@docs/master-plan/analysis/decisions-and-assumptions.md`
- `@docs/master-plan/analysis/capability-map.md`
- `@docs/master-plan/analysis/user-story-inventory.md`
- `master-plan-specification.md`, si está disponible.

## Archivos que puede crear

- `@docs/master-plan/analysis/automation-inventory.md`

## Archivos que puede modificar

- Únicamente `automation-inventory.md`.

## Archivos que no puede modificar

- User stories originales.
- Inventario de US.
- Diagnóstico.
- Código.
- Releases.

## Trabajo requerido

Para cada proceso automatizable, documenta:

- Identificador.
- Proceso.
- Capacidad asociada.
- Actor actual.
- Problema operativo.
- Evento disparador.
- Inputs.
- Decisiones requeridas.
- Acciones.
- Agente o componente responsable.
- Herramientas e integraciones.
- Nivel de automatización actual.
- Nivel inicial recomendado.
- Nivel objetivo.
- Human in the loop.
- Acciones permitidas.
- Acciones prohibidas.
- Validaciones.
- Guardrails.
- Idempotencia.
- Reintentos.
- Manejo de fallos.
- Reversión.
- Auditoría.
- Datos sensibles.
- Tokens y costos.
- Métrica de éxito.
- Evidencia.
- Dependencias.
- Riesgos.
- Release candidata, no definitiva.

## Niveles de automatización

- Manual.
- Asistida.
- Supervisada.
- Automatizada.
- Autónoma controlada.

## Procesos a considerar

- Generación de evaluaciones.
- Validación de rúbricas.
- Análisis de entregas.
- Propuesta de calificación.
- Feedback.
- Brechas de aprendizaje.
- Reforzamiento.
- Notificaciones.
- Reportes.
- Seguimiento de pilotos.
- Registro de consumo.
- Créditos.
- Alertas de costos.
- Gestión de errores.
- Métricas.
- Evidencia de validacion MVP.
- Operaciones administrativas repetitivas.

## Reglas

- No automatizar silenciosamente decisiones pedagógicas sensibles.
- No proponer autonomía sin trazabilidad.
- No usar IA cuando una regla determinista sea suficiente.
- No generar una nueva US sin registrarla como recomendación para la Fase 02.
- Diferenciar automatización del producto y automatización operativa interna.

## Contenido de `automation-inventory.md`

- Resumen ejecutivo.
- Principios de automatización.
- Mapa de procesos.
- Inventario detallado.
- Matriz nivel actual versus objetivo.
- Human-in-the-loop.
- Guardrails.
- Observabilidad.
- Seguridad y privacidad.
- Costos.
- Riesgos.
- Dependencias.
- Métricas.
- Recomendaciones.
- Historial de cambios.

## Criterios de finalización

- Cada automatización tiene trigger, inputs, acción, límites y evidencia.
- Cada automatización tiene nivel inicial y objetivo.
- Las decisiones humanas están definidas.
- Costos, seguridad y errores están considerados.
- No se definió todavía la secuencia final de releases.
