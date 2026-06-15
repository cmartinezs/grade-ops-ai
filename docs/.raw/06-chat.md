# Revisión Profunda De Documentación Canónica

> Nota raw formateada desde la conversación sobre revisión integral de `00-project` y generación de documentos modificados.

## Prompt Original

> Le hice una extensión y repaso a la documentación en el repo de acuerdo a las últimas conversaciones. Hazle un repaso tú, revisión profunda y dime qué debería modificar en cada documento y por qué. Haz esa modificación de acuerdo a los archivos que se encuentran en `00-project` en el repo. Te dejo la imagen de la lista completa de los documentos. Aparte de entregarme las modificaciones por el chat, entrégamelas también en un ZIP con los documentos que hayan sido modificados.

## Contexto

Se revisaron los documentos canónicos de `00-project`, contrastándolos con:

- la lectura estratégica previa de la hackathon;
- las reglas actuales de Devpost;
- la decisión canónica de posicionar el proyecto como **GradeOps AI**;
- la separación entre documentos canónicos y `raw/`.

## Diagnóstico General

El repo ya estaba bien alineado:

- `00-project-review.md` declara correctamente que el proyecto canónico es **GradeOps AI**.
- El foco es docentes de programación.
- El MVP debe tener aprobación docente, logs de agentes y costos separados de tooling personal.
- La separación entre archivos canónicos y `raw/` ya estaba clara.
- `raw/` debe conservarse como historial, no como especificación vigente.

La mejora principal fue pasar de:

> Documentos estratégicos correctos.

A:

> Documentos accionables para construir, vender y postular.

Devpost confirma que el reto exige:

- negocio real;
- clientes/revenue reales;
- operación con IA;
- uso de Google Cloud;
- Gemini API en proyectos con LLM;
- evidencia de logs/API/dashboards;
- evidencia de clientes;
- video menor a 3 minutos.

## Cambios Aplicados Por Documento

| Documento | Qué se modificó | Por qué |
| --- | --- | --- |
| `00-project-review.md` | Se convirtió en índice ejecutivo/canónico del proyecto, con decisiones, roles documentales, riesgos críticos, coherencia, validaciones pendientes y próximos documentos sugeridos. | El archivo ya resumía bien el estado del proyecto, pero faltaba que funcionara como puerta de entrada para cualquiera que llegue al repo. |
| `vision.md` | Se agregó North Star, ICP, segmentos iniciales, moat estratégico, resultados por docente/estudiante/negocio y límites explícitos. | La visión estaba correcta, pero muy sintética. Faltaba diferenciar visión de producto de ventaja competitiva. |
| `pitch.md` | Se agregó pitch de 60 segundos, variantes por comprador, objeciones/respuestas, oferta inicial y líneas de apertura/cierre para demo. | El pitch ya decía bien qué es GradeOps AI, pero necesitaba material comercial directo para vender pilotos y grabar video. |
| `problem.md` | Se agregaron Jobs To Be Done, hipótesis de validación, problema económico, límites del problema y riesgos de interpretación. | El problema estaba claro, pero faltaba formularlo como algo testeable con usuarios reales. |
| `solution.md` | Se agregó matriz de alcance MVP, coreografía de agentes, estados de aprobación, modelo mínimo de datos, seguridad/privacidad, NFR y política de confianza. | Era el documento más importante técnicamente. Ya tenía flujo y agentes, pero faltaban contratos mínimos para implementación y demo. |
| `roadmap.md` | Se agregó critical path, weekly execution plan desde el 8 de junio al 17 de agosto, criterios de kill/pivot y Definition of Done para la postulación. | El roadmap estaba bien por fases, pero necesitaba calendario operativo y criterios de salida duros. |
| `hackathon-strategy.md` | Se agregó matriz de criterios de evaluación, inventario de evidencia, guion de demo de 3 minutos y checklist de cumplimiento. | El archivo ya recogía las restricciones principales, pero faltaba mapearlas contra evidencia concreta que se debe capturar durante el desarrollo. |
| `cost-model.md` | Se actualizó el enfoque de costos, se agregaron supuestos actuales de modelos, margen bruto, esquema de ledger de ingresos/costos y separación más fuerte entre runtime y tooling personal. | El documento ya tenía buenos principios de costos y pricing. Se reforzó porque Devpost exige reportar revenue, costos, marketing spend, related-party revenue y evidencia financiera. |

## Documentos No Modificados

No se modificaron:

- `raw/00-chat.md`;
- `raw/01-chat.md`;
- `raw/02-chat.md`;
- `raw/03-chat.md`;
- `raw/04-chat.md`;
- `raw/05-chat.md`.

Motivo:

> Mantenerlos intactos es correcto porque son historial de razonamiento, no la fuente canónica del producto.

El propio repo ya declara esa intención.

## Ajuste Importante En Costos

Se actualizó `cost-model.md` para que no dependa de nombres/precios obsoletos o demasiado específicos.

La política quedó basada en clases de modelo:

- Flash-Lite;
- Flash;
- premium fallback.

La página oficial de Gemini muestra precios actuales para:

- Gemini 3.1 Flash-Lite;
- Gemini 3 Flash Preview;
- Grounding con Google Search;
- límites/costos asociados.

También se dejó recordatorio de verificar Cloud Run y Firestore antes de la postulación final:

- Cloud Run mantiene free tier por vCPU/RAM/requests según modalidad.
- Firestore mantiene free tier diario de lecturas/escrituras/deletes y 1 GiB almacenado para una base gratuita por proyecto.

## Estado De Entrega

No se hizo commit directo sobre `master`.

Los archivos quedaron listos para:

- reemplazar;
- revisar;
- commitear;
- usar como base documental del proyecto.

## Entregable ZIP

Se generó un ZIP con los 8 documentos modificados:

```text
gradeops-ai-00-project-modified-docs.zip
```
