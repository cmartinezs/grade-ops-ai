# Fase 06 — Validación integral del Master Plan

Actúa como Principal Architect, Product Auditor y Release Governance Lead de GradeOps AI.

## Objetivo

Validar la consistencia, trazabilidad, ejecutabilidad y mantenibilidad del Master Plan completo.

No reescribas automáticamente todo el plan. Primero genera un informe de hallazgos y correcciones recomendadas.

## Entradas obligatorias

- `@docs/master-plan/README.md`
- `@docs/master-plan/master-plan-executive.md`
- Todos los archivos de `@docs/master-plan/analysis/`
- Todos los archivos de `@docs/master-plan/releases/`
- `@docs/`
- Código existente, cuando corresponda.
- `master-plan-specification.md`, si está disponible.

## Archivos que puede crear

- `@docs/master-plan/validation-report.md`

## Archivos que puede modificar

- Únicamente `validation-report.md`.

## Archivos que no puede modificar

- README.
- Documento ejecutivo.
- Análisis.
- Releases.
- User stories.
- Código.

## Validaciones obligatorias

### Estructura

- Existen README y documento ejecutivo.
- Cada release listada tiene archivo.
- Cada archivo de release aparece en el README.
- Los enlaces relativos son válidos.
- No existen archivos huérfanos.

### User stories

- Toda US priorizada pertenece a una release.
- No hay US duplicadas entre releases.
- Las US cumplen Definition of Ready o están marcadas como bloqueadas.
- Las US propuestas están justificadas.
- Las modificaciones están explicadas.

### Releases

- Cada release entrega valor vertical.
- Cada release es demostrable.
- Cada release tiene criterios verificables.
- Ninguna release es XL.
- Las dependencias son coherentes.
- No hay dependencias circulares.
- El MVP no depende del roadmap posterior.
- Las exclusiones son explícitas.

### Automatización

- Cada automatización tiene trigger.
- Tiene inputs y outputs.
- Tiene límites y guardrails.
- Tiene human in the loop cuando corresponde.
- Tiene idempotencia o justificación.
- Tiene estrategia de errores y reintentos.
- Tiene trazabilidad.
- Tiene control de costos.

### Calidad y operación

- Seguridad no fue postergada.
- Observabilidad no fue postergada.
- Auditoría no fue postergada.
- Se consideran datos y migraciones.
- Se consideran despliegues.
- Existen métricas y evidencias.
- La Definition of Done es verificable.

### Prompts `/release-*`

- Son autocontenidos.
- No inventan comandos.
- No implementan releases posteriores.
- Referencian fuentes correctas.
- Tienen alcance y exclusiones.
- Incluyen criterios y riesgos.

### Contraste con código

Cuando haya código:

- Detectar diferencias con el plan.
- Identificar implementación no documentada.
- Identificar documentación obsoleta.
- Identificar deuda o retrabajo.

## Severidad de hallazgos

- BLOCKER.
- HIGH.
- MEDIUM.
- LOW.
- INFORMATIONAL.

## Contenido de `validation-report.md`

- Resumen ejecutivo.
- Resultado general: PASS, PASS WITH CONDITIONS o FAIL.
- Hallazgos por severidad.
- Evidencia.
- Archivos afectados.
- Corrección recomendada.
- Responsable sugerido.
- Orden de corrección.
- Validaciones aprobadas.
- Validaciones pendientes.
- Riesgos residuales.
- Historial de cambios.

## Criterios de finalización

- Todo hallazgo tiene evidencia.
- No se modificaron archivos del plan.
- Los blockers están claramente separados.
- Se indica el orden de corrección.
- Se confirma si la Release 01 puede comenzar.

## Cierre obligatorio

Finaliza indicando:

1. Resultado general.
2. Blockers.
3. Primera release ejecutable.
4. Prerrequisitos.
5. Archivo a revisar.
6. Comando `/release-*` recomendado.
7. Prompt exacto a utilizar.
