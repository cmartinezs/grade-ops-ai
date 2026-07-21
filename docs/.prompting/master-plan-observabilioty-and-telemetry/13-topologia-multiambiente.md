# Topología multiambiente y adaptadores

## Objetivo

GradeOps AI opera dos ambientes funcionales con infraestructuras diferentes. El diseño debe evitar dos errores: forzar GCP sobre `beta` o implementar dos sistemas de observabilidad incompatibles.

## Matriz objetivo

| Capacidad | `demo` — GCP | `beta` — Vercel + Render + Neon | Contrato común |
|---|---|---|---|
| Web | Hosting/runtime GCP definido por infraestructura | Vercel | W3C Trace Context, release, environment, Web Vitals y errores redacted |
| API/Agents | Cloud Run | Render | JSON estructurado, OTel, health/readiness y mismos nombres semánticos |
| Logs | Cloud Logging | Logs/drains de Vercel y Render; destino central opcional | Esquema común y correlation IDs |
| Métricas | Cloud Monitoring | Backend compatible con OTLP por decidir | Métricas y dimensiones idénticas |
| Trazas | Cloud Trace | Backend compatible con OTLP por decidir | `traceparent` extremo a extremo |
| Evidencia durable | PostgreSQL del ambiente | Neon PostgreSQL | Schemas/versiones de eventos comunes |
| Artefactos pesados | Cloud Storage | Object storage por decidir | Manifest, checksum, clasificación y lifecycle |
| Alertas | Cloud Monitoring | Alertas nativas y/o backend OTel | Mismos SLI, severidades, owners y runbooks |

## Patrón de arquitectura

El código de dominio y aplicación emite señales mediante estándares y puertos comunes. La configuración de despliegue selecciona exportadores y credenciales:

```text
instrumentación común
  ├── demo: adaptadores GCP
  └── beta: adaptadores Vercel / Render / Neon / backend OTel
```

No deben aparecer SDK propietarios dentro de casos de uso, agentes o entidades de dominio. Una integración específica puede vivir en infraestructura, inicialización o configuración del runtime.

## Decisiones necesarias para `beta`

Antes de cerrar el backlog técnico se debe comprobar, con los planes contratados y la configuración vigente:

1. Retención y búsqueda disponible en logs de Vercel y Render.
2. Soporte de log drains y exportación OTLP en cada runtime.
3. Restricciones de instrumentación serverless/edge en Vercel.
4. Backend OTel común: administrado, self-hosted o integración de proveedor.
5. Destino de artefactos pesados y exportaciones; Neon no reemplaza object storage.
6. Costos de ingestión, cardinalidad, retención, egress y alertas.
7. Separación de credenciales, proyectos y datasets entre `demo` y `beta`.

## Criterios de aceptación

- El journey `web → api → agents → LLM` conserva `traceparent`, correlation ID y IDs de negocio en ambos ambientes.
- Los nombres de eventos, métricas, estados y errores no cambian según el proveedor.
- Cada señal contiene ambiente, servicio, versión y plataforma.
- Los fallos de exportación no afectan el flujo funcional.
- Neon contiene evidencia durable y agregados, no volcados ilimitados de telemetría técnica.
- El dashboard futuro consulta una API común y puede filtrar/comparar `demo` y `beta`.
- Cada ambiente tiene alertas, runbooks, owner, presupuesto y retención documentados.

## Recomendación

Para el MVP, evitar construir una plataforma observability propia. Usar las capacidades nativas para logs inmediatos, OpenTelemetry para portabilidad y PostgreSQL/Neon para evidencia canónica. Seleccionar un backend compartido de trazas/métricas para `beta` solo después de verificar soporte y costo reales; el contrato debe quedar definido antes que el proveedor.
