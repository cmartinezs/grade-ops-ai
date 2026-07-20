# Resumen ejecutivo

## Conclusión principal

`api/` no debe ser un proxy HTTP transparente hacia `agents/`. Debe ser el orquestador durable del workflow académico y la única interfaz consumida por la UI. `agents/` debe permanecer como motor especializado de ejecución GenAI, sin autoridad para persistir o finalizar efectos de dominio.

La implementación actual constituye una vertical slice síncrona válida del Assessment Agent. Resuelve correctamente la separación de la llamada externa y la transacción de persistencia, pero todavía no soporta el ciclo operativo requerido por GradeOps AI: ejecución larga o masiva, estados consultables, progreso parcial, reintentos seguros, idempotencia, cancelación, recuperación tras reinicios y múltiples intentos o agentes.

## Decisión arquitectónica recomendada

Adoptar una arquitectura híbrida:

- síncrona para operaciones pequeñas, acotadas y previsibles;
- asíncrona durable para batch, tool loops, sandbox, multiagente o latencia incierta;
- un modelo común de `AiOperation`, `AgentRun` y `AgentAttempt`;
- comandos REST funcionales, no un endpoint público genérico de ejecución de agentes;
- PostgreSQL como fuente de verdad y Outbox + Cloud Tasks como mecanismo inicial de despacho durable;
- polling como primera solución de progreso, con SSE como evolución compatible;
- OIDC service-to-service en Cloud Run y secreto compartido solo como conveniencia local transitoria.

## Lo que ya está bien resuelto

- La UI no llama directamente a `agents/`.
- La API valida identidad y ownership.
- La llamada al LLM ocurre fuera de la transacción de base de datos.
- Resultado y log exitoso se persisten juntos.
- Existen correlation IDs entre servicios.
- Los contratos del Assessment Agent son estructurados.
- Existen providers Gemini y Groq.
- El runtime valida salida antes de devolverla.
- Se capturan modelo, prompt, hashes, tokens, costo y timestamps en ejecuciones exitosas.

## Brechas prioritarias

1. `AgentExecutionLog` solo representa ejecuciones terminales.
2. No existe estado transversal consultable por la UI.
3. No existe idempotencia.
4. Se descarta el payload de error enriquecido que devuelve `agents/`.
5. No existen runs, attempts, progreso, cancelación ni retry explícito.
6. No hay transporte durable para procesos asíncronos.
7. El contrato interno se duplica manualmente sin contract tests automatizados.
8. Hay drift entre OIDC, `X-Internal-Secret` y `X-Internal-Key`.
9. Los estados y nombres se almacenan como strings libres.
10. No existe reserva y conciliación de créditos/costo antes y después del despacho.

## Norte para la planificación

La primera evolución debe consolidar el Assessment Agent sin romperlo. La primera necesidad asíncrona fuerte debe introducirse con grading por `StudentSubmission`, no como infraestructura especulativa. Cada release funcional debe aportar solo las capacidades del runtime que consume.

