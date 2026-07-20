# Brechas, riesgos y deuda observada

## Brechas confirmadas

| ID | Brecha | Evidencia | Impacto |
|---|---|---|---|
| B-01 | Log solo terminal | `finished_at NOT NULL` en V12 | No representa queued/running/progreso |
| B-02 | Sin idempotencia | Controllers/client/coordinator sin key | Doble costo y artefactos duplicados |
| B-03 | Error enriquecido descartado | Agents devuelve log; client solo usa status HTTP | Evidencia incompleta de fallos |
| B-04 | Sin API de operations/runs | Código público solo devuelve draft | UI sin estado/progreso/retry |
| B-05 | Timeout síncrono fijo | Read timeout 60 s | No escala a batch/tool loops |
| B-06 | Strings libres | status/agent/provider como VARCHAR/String | Estados inválidos y drift |
| B-07 | Contrato duplicado manualmente | `AssessmentAgentResponse` espejo | Riesgo de incompatibilidad silenciosa |
| B-08 | Auth interna inconsistente | OIDC docs vs secret README vs key código | Riesgo operativo y de seguridad |
| B-09 | Fallo genérico fabricado en API | `persistFailure` usa timestamps locales y nulls | Métricas/costo/correlación incorrectos |
| B-10 | `assessment_id` obligatorio | Tabla de logs actual | Dificulta agentes no ligados directamente a assessment |

## Riesgos de diseño

### Convertir API en proxy

Si el controller solo reenvía requests, la UI terminará decidiendo agentes, secuencia y retries. Esto fragmenta el workflow y debilita seguridad y auditoría.

### Construir un runtime universal antes de tiempo

Agent registry, multiagente, memoria, RAG, tools y sandbox simultáneos retrasarían valor y crearían abstracciones sin consumidores. Mantener evolución por releases funcionales.

### Reintentar LLM de forma ciega

Un timeout no prueba que el provider no haya procesado. Reintentos sin idempotencia pueden duplicar costo y producir resultados distintos.

### Persistencia dual prematura

Guardar estado canónico en API y también en Agents sin reconciliación crea split brain. Mantener Agents stateless hasta que checkpoints reanudables sean necesarios.

### Usar `@Async` en Cloud Run

El trabajo puede perderse tras responder al usuario. Debe existir transporte durable.

### Mezclar éxito técnico y aprobación

Una ejecución exitosa no autoriza calificación, feedback o publicación. Mantener estados separados.

## Deuda tolerable temporalmente

- Endpoint específico del Assessment Agent.
- Polling en vez de SSE.
- Un solo provider por run con fallback controlado.
- Estimaciones de costo cuando el provider no entregue uso exacto.
- Secreto interno en local.

## Deuda no aceptable antes de batch grading

- Ausencia de idempotencia.
- Ausencia de operaciones durables.
- Falta de resultados parciales.
- Reintento de lote completo.
- Falta de ownership en ejecución diferida.
- Falta de reserva/conciliación de uso.
- Falta de trazabilidad run/attempt.

