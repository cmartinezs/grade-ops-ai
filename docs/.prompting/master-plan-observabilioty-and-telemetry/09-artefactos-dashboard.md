# Artefactos y dashboard futuro

## Qué significa “artefacto”

Un artefacto es un objeto durable y gobernado, no un archivo arbitrario dentro del contenedor.

| Artefacto | Almacenamiento recomendado | Retención |
|---|---|---|
| Evento de negocio/IA | PostgreSQL append-only | según obligación de evidencia |
| Estado de operación/run | PostgreSQL | vida del producto + política |
| Logs técnicos | GCP en `demo`; Vercel/Render o log drain en `beta` | corta/media según ambiente y plan |
| Métricas | Cloud Monitoring en `demo`; backend OTel acordado en `beta` | agregada |
| Trazas | Cloud Trace en `demo`; backend OTel acordado en `beta` | diagnóstico, muestreada |
| Evidence export/bundle | Object Storage por ambiente | lifecycle explícito |
| Payload diagnóstico redacted | Object Storage restringido | muy corto y excepcional |

Neon almacena eventos canónicos, estado, agregados y evidencia relacional de `beta`. No debe almacenar stack traces masivos, blobs, logs de cada request ni payloads completos de prompts/respuestas.

## API de consulta futura

El dashboard debe consumir endpoints como:

```text
GET /api/v1/operator/observability/overview
GET /api/v1/operator/observability/ai-operations
GET /api/v1/operator/observability/agents
GET /api/v1/operator/observability/costs
GET /api/v1/operator/observability/quality
GET /api/v1/operator/observability/incidents
```

Debe aplicar roles, minimización y filtros temporales. No exponer una API que ejecute consultas libres sobre logs.

## Vistas/agregados iniciales

- Volumen, éxito y latencia por capability/agente.
- Tokens/costo por provider y modelo.
- Costo por operación y resultado aprobado.
- Errores por taxonomía y dependencia.
- Runs estancados/reintentados.
- Aprobación, edición y rechazo docente.
- Web Vitals y errores por release/entorno.

## Diseño posterior

Primero estabilizar contratos y recoger beta data. Luego diseñar el dashboard con preguntas operativas concretas. Construir gráficos antes de validar semántica produce métricas bonitas pero engañosas.
