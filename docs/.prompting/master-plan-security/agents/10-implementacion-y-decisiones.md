# Implementación y decisiones

## Fases

1. **Fail secure:** eliminar default productivo, límites de payload/timeout, catálogo provider/model y pruebas negativas.
2. **OIDC:** Cloud Run privado, service account API, audience/caller validation y retiro del secreto productivo.
3. **Envelope/policy:** IDs, schema versions, deadline, budgets y capabilities.
4. **Prompt/output:** delimitación, schemas cerrados, semántica y redacción de telemetría.
5. **Resiliencia/economía:** concurrency, rate limits, circuit breakers y conciliación.
6. **Tools/sandbox:** solo cuando un caso funcional real lo requiera, con infraestructura aislada.

Cada fase debe incluir cambios coordinados en `api/`, `agents/`, tests y `infra/`.

## Decisiones abiertas

- Validación OIDC en aplicación versus confianza en proxy más allowlist adicional.
- Catálogo central o desplegado por versión para provider/model.
- Semántica de deduplicación por `runId/attemptId`.
- Límites por agente y clasificación de datos por proveedor.
- Retención de prompts/resultados para debugging versus privacidad.
- Tecnología e infraestructura del sandbox futuro.

## Recomendación CTO

No crear RBAC humano en `agents/`. Fortalecer primero la frontera API→Agents con OIDC y policy envelopes. Mantener el runtime determinista y tool-less durante el MVP; cada capacidad agentic nueva debe pagar explícitamente su costo de seguridad, observabilidad y aislamiento.
