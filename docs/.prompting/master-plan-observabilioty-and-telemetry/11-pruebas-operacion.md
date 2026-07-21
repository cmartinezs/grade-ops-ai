# Pruebas y operación

## Pruebas automáticas

- Correlation/request IDs válidos se propagan y los inválidos se reemplazan.
- MDC y contextos se limpian aun con excepciones.
- Tokens, emails, prompts y submissions se redacted.
- Métricas usan route templates, no URLs con IDs.
- Spans mantienen jerarquía entre servicios.
- Eventos canónicos validan schemaVersion e idempotencia.
- Fallos del exporter no rompen el flujo de negocio.
- Operaciones fallidas conservan evidencia mínima.

## Pruebas de integración

- Journey real `web → api → agents` consultable por trace/correlation ID.
- Timeout y 429 del proveedor producen error normalizado, métrica y span.
- Retry crea otro attempt sin duplicar el evento de negocio.
- Run estancado dispara alerta.
- Error de frontend se correlaciona con request de API cuando corresponde.
- El mismo journey puede reconstruirse en `demo` y `beta` con idéntica semántica e IDs.
- La caída del exportador OTel en un ambiente no detiene el negocio ni contamina al otro.

## Runbooks mínimos

- API con tasa elevada de 5xx.
- Agents lento o inaccesible.
- Provider LLM degradado/cuota agotada.
- Operaciones estancadas.
- Costo/tokens anómalos.
- Exportador de telemetría caído.
- Posible fuga de PII en logs.

Cada alerta debe enlazar un runbook, indicar owner, severidad, señales de confirmación, mitigación y criterio de cierre.

## Release observability

Toda señal debe incluir versión/revisión del servicio. Los dashboards deben permitir comparar antes/después de un despliegue y detectar regresiones.

## Definition of Done transversal

- Señales definidas y probadas.
- Sin datos prohibidos.
- Dashboard técnico/consulta mínima disponible.
- Alertas accionables, no ruidosas.
- Terraform de GCP y configuración versionada/automatizada de Vercel, Render y Neon actualizados según corresponda.
- Runbook y owner definidos.
- Coste estimado y retención documentados.
