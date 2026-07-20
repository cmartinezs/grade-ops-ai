# Fase 02 — Capacidades y user stories

Actúa como Product Manager, Business Analyst y Domain Expert de GradeOps AI.

## Objetivo

Construir el mapa de capacidades, identificar el flujo crítico y diagnosticar todas las user stories existentes y faltantes.

No diseñes todavía el orden definitivo de releases ni generes prompts `/release-*`.

## Entradas obligatorias

- `@docs/`
- `@docs/master-plan/analysis/documentation-diagnosis.md`
- `@docs/master-plan/analysis/decisions-and-assumptions.md`
- `master-plan-specification.md`, si está disponible.

## Archivos que puede crear

- `@docs/master-plan/analysis/capability-map.md`
- `@docs/master-plan/analysis/user-story-inventory.md`

## Archivos que puede modificar

- Únicamente los dos archivos anteriores.

## Archivos que no puede modificar

- User stories originales.
- Código.
- Diagnóstico de la Fase 01.
- Documentos de releases.

## Trabajo requerido

### Mapa de capacidades

Agrupa las funcionalidades reales en capacidades de negocio. Adapta la taxonomía a `@docs/`.

Considera, cuando corresponda:

- Identidad y acceso.
- Docentes.
- Cursos.
- Estudiantes.
- Evaluaciones.
- Rúbricas.
- Entregas.
- Calificación.
- Feedback.
- Revisión docente.
- Reportes.
- Automatización.
- Agentes.
- Facturación y créditos.
- Auditoría.
- Métricas.
- Evidencia de validacion MVP.

### Flujo crítico

Identifica el flujo de mayor valor inicial:

- Actor.
- Evento inicial.
- Pasos.
- Intervenciones humanas.
- Automatizaciones.
- Resultado.
- Evidencia.
- Métrica.

### Inventario de user stories

Para cada US existente:

- ID.
- Título.
- Actor.
- Objetivo.
- Valor.
- Fuente.
- Estado de definición.
- Dependencias.
- Reglas de negocio.
- Datos.
- Criterios de aceptación.
- Cumplimiento de Definition of Ready.
- Observaciones.
- Acción recomendada.

Clasificar como:

- READY.
- NOT READY.
- Demasiado grande.
- Duplicada.
- Bloqueada.
- Obsoleta.
- Requiere enriquecimiento.
- Requiere división.
- Requiere combinación.

### US faltantes

Proponer historias faltantes relacionadas con:

- Seguridad.
- Auditoría.
- Errores.
- Reintentos.
- Idempotencia.
- Costos.
- Créditos.
- Observabilidad.
- Aprobación humana.
- Privacidad.
- Eliminación o anonimización.
- Agentes.
- Métricas.
- Evidencia comercial.
- Administración.
- Configuración.
- Onboarding.
- Facturación.
- Pilotos reales.

Marcar cada una como `US PROPUESTA` y explicar su necesidad.

## Contenido de `capability-map.md`

- Resumen.
- Mapa de capacidades.
- Subcapacidades.
- Actores.
- Relación entre capacidades.
- Flujo crítico.
- Dependencias funcionales.
- Capacidades MVP.
- Capacidades validacion MVP.
- Capacidades roadmap.

## Contenido de `user-story-inventory.md`

- Resumen cuantitativo.
- Tabla de historias existentes.
- Historias NOT READY.
- Historias duplicadas.
- Historias a dividir o combinar.
- Historias faltantes propuestas.
- Recomendaciones de normalización.
- Trazabilidad hacia capacidades.
- Historial de cambios.

## Criterios de finalización

- Toda US localizada está inventariada.
- Toda US está asociada a una capacidad.
- Toda US tiene estado de readiness.
- Las historias faltantes están justificadas.
- El flujo crítico está documentado.
- No se definió aún la secuencia final de releases.
