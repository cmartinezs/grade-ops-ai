# Requisitos no funcionales

## Confiabilidad

- Transporte durable con entrega al menos una vez.
- Consumers idempotentes.
- Retry con backoff exponencial y jitter solo para errores transitorios.
- Dead-letter handling visible para operadores.
- Recuperación de leases expirados.
- Persistencia parcial en batch.
- Deadlines por operation, run, attempt y tool.
- Circuit breaker por provider/modelo cuando exista evidencia de necesidad.

No reintentar automáticamente errores de validación, policy, presupuesto o input.

## Rendimiento y SLO inicial

| Indicador | Objetivo inicial propuesto |
|---|---|
| Aceptación de comando asíncrono | p95 < 500 ms |
| Consulta de operación | p95 < 300 ms |
| Límite recomendado para sync GenAI | 10–15 s |
| Actualización de progreso | cada 5 s o 1–5 ítems |
| Duplicación por mismo idempotency key | 0 |
| Runs con trazabilidad completa | 100% |

Estos valores deben validarse con tráfico real del piloto antes de tratarlos como SLA contractual.

## Escalabilidad y backpressure

- Concurrencia limitada por tenant/teacher.
- Concurrencia global por provider y modelo.
- Cola por prioridad cuando exista diferencia entre interacción y batch.
- Batch particionado por `StudentSubmission` o question item.
- Límites de tamaño de payload; artefactos grandes por referencia segura.
- Listados paginados.
- No cargar entregas masivas completas en memoria.

## Seguridad

- Firebase para usuario público según arquitectura vigente.
- OIDC service-to-service e IAM Cloud Run para producción.
- Secreto compartido solo como mecanismo local transitorio.
- Validación de audience, issuer y service account.
- Ownership antes del despacho y nuevamente antes de aplicar resultados.
- Inputs internos construidos por API; no forward ciego del body de UI.
- Allowlist de tools por agente.
- Sin comandos arbitrarios.
- Redacción de PII, tokens, prompts sensibles y entregas en logs.
- Signed URLs de vida corta para artefactos.
- Sandbox aislado antes de ejecutar código no confiable.
- Defensa contra prompt injection en submissions y documentos.

## Privacidad

- Minimizar datos de estudiantes.
- Usar identificadores o pseudónimos controlados por docente.
- No incluir nombres completos si no son necesarios.
- Definir retención y eliminación de inputs/outputs.
- No almacenar chain-of-thought.
- Distinguir evidencia auditable de contenido sensible.

## Observabilidad

Contexto mínimo:

```text
requestId
correlationId
operationId
agentRunId
attemptId
agentName y version
provider y model
prompt/schema versions
latency
tokens
cost
status y errorCode
validation outcome
```

Métricas:

```text
ai_operation_total
agent_run_total
agent_run_duration_seconds
agent_run_failure_total
agent_retry_total
agent_tokens_total
agent_cost_usd
agent_queue_delay_seconds
agent_output_validation_failure_total
agent_human_rejection_total
agent_human_edit_total
```

La tasa de edición/rechazo docente mide calidad funcional real y debe complementar latencia y disponibilidad.

## Testing

- Unit tests de estados y transiciones.
- Unit tests de idempotencia y policy.
- Integration tests PostgreSQL/Testcontainers para constraints y locking.
- Contract tests API–Agents.
- Tests de timeout, rate limit y respuesta malformada.
- Tests de duplicación de Cloud Tasks.
- Tests de recuperación por lease.
- Tests de batch parcial y retry selectivo.
- Tests de cancelación.
- Tests de ownership cruzado.
- Tests de reserva/conciliación de uso.
- Smoke tests reales controlados por provider.
- Evaluation dataset por agente para calidad semántica.

