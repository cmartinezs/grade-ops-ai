# Alineación De `01-business` Con `00-project`

> Nota raw formateada desde la conversación sobre revisión y actualización de la documentación de negocio.

## Prompt Original

> Ok, listo, lo actualicé en el repo. Ahora deberíamos seguir con la documentación de `01-business` considerando lo ya definido en `00-project`. Hay que revisar lo que hizo Copilot inicialmente cuando se creó el repo y alinear con lo ya visto hasta ahora en las revisiones anteriores.

## Diagnóstico Inicial

Sí: `01-business` estaba demasiado inicial.

Copilot dejó una base correcta, pero muy genérica:

- `business-model.md` solo definía cliente, oferta y revenue en pocas líneas.
- `customer-discovery.md` solo dejaba preguntas base.
- `pricing.md` todavía tenía precios amplios no alineados con el modelo canónico actual.

## Criterios De Alineación

La actualización se alineó con `00-project`, especialmente con estas decisiones:

- proyecto canónico: **GradeOps AI**;
- foco en docentes de programación;
- MVP de evaluaciones prácticas;
- pricing por evaluaciones + entregas corregidas;
- control docente;
- evidencia como output obligatorio;
- separación entre tooling personal y runtime productivo;
- costos, revenue y logs como parte del negocio.

También se cruzó con la lógica oficial de la hackathon:

- negocio real;
- usuarios reales;
- revenue;
- AI agents en producción;
- Google Cloud;
- evidencia de producto;
- gastos;
- logs/API/dashboards;
- evidencia de clientes.

## Cambios Aplicados En `01-business`

| Documento | Problema detectado | Modificación aplicada |
| --- | --- | --- |
| `business-model.md` | Muy genérico; no convertía la idea en modelo vendible. | Se rehízo como modelo de negocio completo: ICP, segmentos, JTBD, propuesta de valor, oferta, revenue streams, unit economics, adquisición, retención, riesgos y gates de validación. |
| `customer-discovery.md` | Tenía preguntas útiles, pero no suficientes para validar venta real. | Se agregaron hipótesis, targets priorizados, screener, entrevista completa, scoring de dolor/intención de compra, evidencia por entrevista, síntesis por batch y reglas de privacidad/consentimiento. |
| `go-to-market.md` | Correcto pero muy superficial; faltaba ejecución concreta. | Se agregó GTM founder-led, Pilot Pack, canales, flujo de venta, mensajes de outreach, landing copy, métricas de funnel, plan semanal, objeciones y referral motion. |
| `hackathon-strategy.md` | Duplicaba de forma débil lo de `00-project`. | Se convirtió en estrategia de evidencia business: revenue, customers, agent evidence, dashboard, demo business arc, narrativa, checklist y riesgos de submission. |
| `pricing.md` | No estaba alineado con `cost-model.md`. | Se alineó con los planes canónicos: Free, Teacher Lite, Teacher Pro, Cohort Pro y Pilot Pack; se agregó CLP, overuse, margen, experimentos de precio, descuentos y reporting. |

## Decisión Importante

El foco de `01-business` debe ser distinto al de `00-project`:

| Carpeta | Pregunta Que Responde |
| --- | --- |
| `00-project` | Qué estamos construyendo y por qué. |
| `01-business` | A quién se vende, cómo se valida, cuánto se cobra, cómo se consigue evidencia y cómo se prueba negocio real. |

## Idioma

Los documentos quedaron en inglés porque sirven directamente para preparar:

- submission;
- narrativa;
- testing evidence;
- demo;
- materiales de evaluación ante la hackathon.

## Entregable ZIP

Se generó un ZIP con los 5 documentos modificados:

```text
gradeops-ai-01-business-modified-docs.zip
```

## Nota Operativa

Durante la revisión se generó por error un archivo vacío `dummy` en el repo y se eliminó inmediatamente.

No quedó archivo residual en el árbol del repo; solo quedarían esos commits mínimos en el historial si la operación fue commiteada.
