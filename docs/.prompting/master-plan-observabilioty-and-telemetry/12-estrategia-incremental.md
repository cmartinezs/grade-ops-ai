# Estrategia incremental y decisiones

## Fase 0 — Contratos y ADR

- ADR de backends y OpenTelemetry.
- ADR de topología por ambiente y destino OTel de `beta`.
- Taxonomía de IDs, eventos y errores.
- Política de datos, redacción, retención y cardinalidad.
- SLI/SLO iniciales.

## Fase 1 — Backbone técnico

- JSON a stdout en `api/` y `agents/` para Cloud Run y Render.
- Actuator/Micrometer y endpoints protegidos.
- OpenTelemetry HTTP/JVM y W3C propagation.
- Correlation/request filter común en API y continuidad hacia Agents.
- `instrumentation.ts` y captura mínima en Web.
- Infraestructura: Terraform para GCP y configuración automatizable/versionada para Vercel, Render y Neon.

## Fase 2 — Journey IA y evidencia

- Instrumentar `AiOperation → AgentRun → AgentAttempt`.
- Métricas GenAI: latencia, tokens, costo, retry y validación.
- Eventos canónicos de producto/calidad.
- Conciliación entre costo estimado y real.
- Primer dashboard técnico y alertas.

## Fase 3 — Operación asíncrona

- Propagación por outbox y adaptador asíncrono por ambiente; Cloud Tasks solo donde corresponda.
- Queue delay, heartbeat, lease y estancamiento.
- Alertas de burn rate y runbooks.
- Synthetics/uptime para journeys públicos seguros.

## Fase 4 — Dashboard GradeOps

- Read models/agregados.
- Endpoints Operator con RBAC y minimización.
- Paneles de salud, costo, calidad y adopción.
- Exportaciones de evidencia y lifecycle.

## No hacer todavía

- Introducir Grafana/Prometheus/ELK solo por familiaridad.
- Construir un data warehouse antes de volumen/consultas reales.
- Capturar prompts y respuestas completos de forma general.
- Crear un dashboard custom que lea directamente Cloud Logging, logs de Vercel o logs de Render.
- Fijar SLO contractuales sin baseline de beta.

## Decisiones abiertas

1. Exportación OTel de `demo` directa a Google Cloud versus Collector.
2. Destino OTel común para `beta` y viabilidad técnica/económica de exportar desde Vercel y Render.
3. Backend de errores frontend compartido entre ambientes versus adaptadores específicos.
4. Analytics de producto first-party versus proveedor externo y requisitos de consentimiento.
5. Retención exacta por señal, proveedor, plan y entorno.
6. Object storage de `beta` para bundles/exportaciones; Neon no cubre ese rol.
7. Qué artefactos forman evidencia de hackathon versus operación normal.
8. Método verificable para estimar tiempo ahorrado.

## Prioridad recomendada

La primera entrega debe demostrar una traza completa de generación de assessment en `demo` y `beta`, logs JSON consultables, métricas básicas y un `AgentExecutionLog` durable enriquecido. La equivalencia semántica entre ambientes es un criterio de aceptación, no una mejora posterior.
