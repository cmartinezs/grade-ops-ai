# Resumen ejecutivo

GradeOps AI posee piezas valiosas, pero no una plataforma de observabilidad completa. `api/` y `agents/` configuran Logback y JSON en archivos; `agents/` implementa `X-Correlation-Id` y MDC; `api/` genera un correlation ID nuevo para cada llamada a `agents/`; y `AgentExecutionLog` captura evidencia parcial de ejecuciones de IA. `web/` no tiene instrumentación explícita. No se verificaron Actuator/Micrometer, OpenTelemetry, métricas exportadas, trazas distribuidas, alertas, dashboards ni recursos Terraform de observabilidad.

La arquitectura recomendada distingue:

| Señal | Finalidad | Fuente de verdad |
|---|---|---|
| Logs | Diagnóstico discreto de eventos técnicos | Backend operativo del ambiente |
| Métricas | Tendencias, SLI y alertas | Backend de métricas compatible con OTel |
| Trazas | Latencia y causalidad entre componentes | Backend de trazas compatible con OTel |
| Eventos de producto | Uso, conversión, calidad y resultados | PostgreSQL |
| Evidencia de IA | Modelo, tokens, costo, validación y decisión humana | PostgreSQL |
| Artefactos pesados | Exportaciones, muestras redacted y bundles | Object Storage seleccionado por ambiente |

Principio central:

> El dashboard futuro no debe leer archivos de log ni depender del formato propietario de GCP, Vercel o Render como modelo de producto. Debe consumir APIs de consulta sobre eventos canónicos, agregados y artefactos autorizados.

La instrumentación debe usar OpenTelemetry como estándar de emisión y separar el contrato de los adaptadores. `demo` se despliega en GCP; `beta` se distribuye entre Vercel, Render y Neon. Ambos deben producir la misma semántica, IDs y eventos aunque sus backends operacionales sean diferentes.

Neon será la fuente durable de eventos de producto y evidencia de IA en `beta`, no un repositorio de logs técnicos sin límite. Los logs nativos de Vercel y Render sirven para operación inmediata; las trazas y métricas transversales requieren un destino OTel común o compatible que deberá resolverse mediante ADR.

## Resultado esperado

- Un request puede seguirse desde el navegador hasta el proveedor LLM.
- Una operación de IA puede explicar duración, errores, reintentos, tokens, costo y aprobación humana.
- Los journeys críticos tienen SLI, SLO y alertas accionables.
- Los datos sensibles no aparecen en logs, spans ni etiquetas de métricas.
- Los artefactos durables pueden consultarse posteriormente mediante un dashboard propio.
