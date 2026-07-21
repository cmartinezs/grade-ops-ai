# Trazas distribuidas

## Alcance

Instrumentar inicialmente el journey:

```text
web interaction → API request → DB/outbox → agents request → LLM call → validation → persistence
```

## Spans recomendados

- `http.server` y `http.client` automáticos.
- `gradeops.assessment.generate` como span de negocio en API.
- `gradeops.agent.execute` por run.
- `gradeops.agent.validate_input`.
- `gradeops.agent.render_prompt` sin contenido del prompt.
- `gen_ai.chat` o convención semántica oficial del SDK.
- `gradeops.agent.validate_output`.
- `gradeops.evidence.persist`.

## Atributos GenAI

Registrar provider, modelo, operación, tokens, finish reason, latencia y error de acuerdo con convenciones OpenTelemetry disponibles. No almacenar prompt/completion por defecto. Si se habilita captura de contenido para diagnóstico, debe ser opt-in, redacted, temporal y con acceso restringido.

## Sampling

- Development: 100%.
- Demo y beta: alto mientras el volumen y costo sean pequeños, con presupuesto explícito.
- Ambientes de mayor tráfico: head sampling base más conservación de errores y trazas lentas mediante la estrategia soportada.
- Los eventos durables de negocio nunca dependen del sampling de trazas.

## Async

Outbox y el mecanismo de entrega asíncrona de cada ambiente deben persistir contexto de traza enlazable. Una tarea reintentada crea un nuevo attempt/span, pero conserva `operationId` y `agentRunId`. Cloud Tasks no debe aparecer como dependencia obligatoria de `beta` sin un diseño explícito de interoperabilidad o un adaptador equivalente.

## Pruebas

- Un request conserva un solo `trace_id` entre API y Agents.
- La llamada LLM es hija del run correcto.
- Errores agregan status/error sin contenido sensible.
- Los spans no incluyen IDs de alta cardinalidad como métricas.
- La instrumentación no altera contratos funcionales.
