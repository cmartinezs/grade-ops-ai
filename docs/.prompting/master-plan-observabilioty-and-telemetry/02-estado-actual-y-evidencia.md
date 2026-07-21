# Estado actual y evidencia

## `api/`

- `pom.xml` incluye `logstash-logback-encoder`.
- `logback-spring.xml` emite consola legible y un archivo JSON rotativo `logs/grade-ops-api.json`.
- El archivo rota por fecha/tamaño, conserva 30 días y limita el total a 1 GB.
- No se verificaron dependencias de Actuator, Micrometer u OpenTelemetry.
- `AssessmentAgentClient` crea un UUID por llamada, lo envía como `X-Correlation-Id`, registra finalización/rechazo y recupera el header de respuesta.
- Ese ID se genera al llamar a `agents/`; no representa necesariamente el request original de `web/`.
- `AgentExecutionLog` se persiste en PostgreSQL con datos de ejecución, pero el modelo actual no representa completamente una operación viva, intentos, colas, reintentos o aprobación humana.

## `agents/`

- Usa la misma estrategia Logback: consola legible más archivo JSON rotativo.
- `CorrelationIdFilter` reutiliza o genera `X-Correlation-Id`, lo devuelve y lo inserta en MDC.
- El MDC se limpia en `finally`, lo cual evita contaminación entre requests.
- El filtro acepta cualquier valor no vacío; falta validar formato, tamaño y caracteres para evitar abuso de cardinalidad o log injection.
- `AgentExecutionLogPayload` entrega hashes, provider/model relacionados, tokens estimados, costo estimado, estado, error y timestamps según el flujo actual.
- No se verificaron métricas exportadas, spans, instrumentación de Spring AI/HTTP ni medidas separadas de tiempo de cola, prompt, proveedor y validación.

## `web/`

- `package.json` no incluye SDK de observabilidad, error reporting o analytics.
- `apiClient` adjunta Firebase ID Token y procesa `401`, pero no propaga `traceparent`, request ID o correlation ID.
- No se verificaron `instrumentation.ts`, middleware de observabilidad, Web Vitals persistidos, captura central de errores o eventos de producto.
- La consola del navegador no puede considerarse un artefacto operacional durable.

## `infra/`

- Terraform aprovisiona Cloud Run para `api/` y `agents/`.
- No se verificaron recursos para dashboards, alert policies, uptime checks, log sinks, métricas basadas en logs o retención/exportación.
- `AGENTS.md` declara Cloud Logging como destino, pero la infraestructura de observabilidad no está codificada todavía.

## Topología de ambientes confirmada

| Ambiente | Web | API/Agents | Datos durables | Consecuencia |
|---|---|---|---|---|
| `demo` | GCP | GCP/Cloud Run | PostgreSQL y servicios GCP definidos por infraestructura | Integración nativa con Cloud Operations es posible |
| `beta` | Vercel | Render | Neon PostgreSQL | La telemetría queda fragmentada si solo se usan las consolas nativas |

El baseline original no modelaba esta segunda topología. Deben verificarse todavía en la configuración real los servicios y planes exactos habilitados en Vercel, Render y Neon, sus límites de retención y sus opciones de exportación. El plan no debe inventarlos ni asumir capacidades de planes pagados.

## Brecha crítica de runtimes administrados

Los archivos JSON dentro del filesystem efímero del contenedor no son una estrategia durable. Cloud Run y Render deben recibir JSON estructurado por `stdout/stderr`; Vercel requiere instrumentación compatible con su runtime. El archivo local puede conservarse solo para desarrollo cuando exista una necesidad explícita.

## Lo que no debe confundirse

- `AgentExecutionLog` no reemplaza logs técnicos ni trazas.
- Ningún backend de logs de plataforma reemplaza evidencia de negocio durable.
- Correlation ID no es equivalente a trace ID.
- Health check no es equivalente a monitoreo de experiencia real.
