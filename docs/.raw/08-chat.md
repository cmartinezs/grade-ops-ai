# Alineación De `02-product`

> Nota raw formateada desde la conversación sobre revisión y actualización de la documentación de producto.

## Prompt Original

> Ahora toca `02-product`.

## Resultado General

Se revisó `02-product` y se dejó alineado con lo ya consolidado en:

- `00-project`;
- `01-business`.

## Diagnóstico Inicial

Copilot dejó buenos encabezados, pero los documentos todavía estaban en modo "nota inicial".

Ejemplos:

- `mvp-scope.md` definía solo in/out scope en pocas líneas.
- `personas.md` tenía 3 perfiles muy resumidos.
- `user-stories.md` traía 6 historias sin prioridad ni criterios de aceptación.

Para esta etapa, el producto necesitaba quedar mucho más ejecutable.

## Cambios Aplicados En `02-product`

| Documento | Modificación principal |
| --- | --- |
| `mvp-scope.md` | Se convirtió en una especificación de alcance MVP: objetivo, experiencia, matriz Must/Should/Could/Do Not Build, estados del producto, roles, NFR, criterios de aceptación y cut line. |
| `personas.md` | Se ampliaron las personas: Lead Instructor, Tutor, Bootcamp Instructor, Academy Operator, Program Manager, Student, Reviewer y anti-personas. |
| `user-stories.md` | Se estructuró por épicas, prioridades P0/P1/P2/Out y criterios de aceptación. Quedó como backlog base para implementación. |
| `workflows.md` | Pasó de un flujo general a workflows detallados: onboarding, creación de evaluación, rúbrica, submissions, grading, feedback, learning gaps, reportes, logs y evidencia. |
| `metrics.md` | Se rehízo como sistema de métricas de producto, negocio, AI-native operations, confianza docente, unit economics y evidencia de hackathon. |

## Decisión De Enfoque

`02-product` ahora responde a:

> ¿Qué debe hacer exactamente el producto MVP para probar el negocio?

No intenta resolver arquitectura interna todavía. Eso debe quedar en una carpeta técnica posterior.

El foco de esta carpeta queda en:

- producto;
- alcance;
- usuarios;
- historias;
- flujos;
- estados;
- métricas;
- criterios de aceptación.

## Estado De Entrega

No se modificó GitHub directamente.

Los archivos quedaron listos para:

- reemplazar;
- revisar;
- commitear;
- usar como base de implementación del MVP.

## Entregable ZIP

Se generó un ZIP con los documentos modificados:

```text
gradeops-ai-02-product-modified-docs.zip
```
