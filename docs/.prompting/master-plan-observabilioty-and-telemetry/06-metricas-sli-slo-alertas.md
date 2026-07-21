# Métricas, SLI, SLO y alertas

## Métricas técnicas mínimas

```text
http.server.request.duration
http.client.request.duration
db.client.operation.duration
process.cpu.utilization
jvm.memory.used
gradeops.auth.failure.total
gradeops.ai.operation.total
gradeops.ai.run.total
gradeops.ai.run.duration
gradeops.ai.queue.delay
gradeops.ai.retry.total
gradeops.ai.token.usage
gradeops.ai.cost.usd
gradeops.ai.output_validation.failure.total
gradeops.ai.human.rejection.total
```

Los nombres finales deben seguir las convenciones del SDK adoptado; esta lista define semántica, no una API literal.

## Dimensiones permitidas

`service`, `environment`, `route_template`, `http_method`, `status_class`, `agent_name`, `agent_version`, `provider`, `model_family`, `result`, `error_code`.

No usar IDs de usuario, assessment, operation, run o submission como labels.

## SLI iniciales

| Capacidad | SLI |
|---|---|
| API pública | proporción de requests válidos exitosos y latencia p95 |
| Generación IA | runs completados / runs aceptados; p95 por agente/modelo |
| Cola | tiempo p95 hasta inicio y operaciones estancadas |
| Web | LCP, INP, CLS y errores de cliente por sesión |
| Calidad IA | output válido, edición/rechazo docente y retry rate |

## SLO iniciales propuestos

Son objetivos a validar con la beta, no compromisos definitivos:

- API de estado: 99.5% mensual; p95 < 300 ms.
- Aceptación asíncrona: 99.5%; p95 < 500 ms.
- Runs sin fallo técnico atribuible a GradeOps: ≥ 98%.
- Cero operaciones `RUNNING` sin heartbeat más allá del umbral definido.

## Alertas

Alertar por impacto y burn rate, no por cada excepción:

- Error budget burn rápido/lento.
- Aumento sostenido de 5xx.
- Dependencia LLM degradada por provider/modelo.
- Cola o runs estancados.
- Coste/tokens anómalos respecto del presupuesto.
- Cero tráfico en un servicio que debería estar activo.
- Fallo de exportación de telemetría.
