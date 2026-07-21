# ADR - Observabilidad y telemetria incorporadas por release funcional

## Estado

Aceptada.

## Contexto

El paquete `docs/.prompting/master-plan-observabilioty-and-telemetry/` documenta el baseline y el modelo objetivo para instrumentar `web/`, `api/`, `agents` e `infra/`.

La necesidad es transversal, pero crear una release tecnica aislada produciria logging y dashboards desconectados de los journeys reales. La observabilidad debe avanzar junto con las operaciones funcionales que necesita explicar.

## Decision

La observabilidad se incorpora por release funcional y no como una release tecnica independiente.

- OpenTelemetry, W3C Trace Context, JSON logs, metricas, eventos canonicos, evidencia durable y artefactos se tratan como capas separadas.
- `api/` conserva operaciones, eventos de negocio, evidencia durable y spans principales.
- `agents/` instrumenta runs, attempts, provider/model, tokens, costo, validacion, retries y errores normalizados.
- `web/` incorpora RUM minimo, Web Vitals, errores redacted y propagacion de contexto.
- `infra/` provee adaptadores por ambiente: GCP en `demo`; Vercel/Render/Neon y backend OTel acordado en `beta`.
- El dashboard futuro consume APIs y agregados autorizados, no logs crudos de plataforma.
- Cada tarea que toque endpoints, agentes, asincronia, providers, exports, dashboards o journeys criticos debe incluir un gate de observabilidad.

## Consecuencias

- R01 debe cerrar contratos, IDs, logging stdout, propagacion W3C y primera traza `web -> api -> agents -> LLM` en los ambientes aplicables.
- R02-R05 profundizan telemetria por journeys Open/Closed, calidad IA, signed links, attempts, asincronia y analytics.
- R06 consolida dashboard Operator, SLI/SLO baseline, alertas, runbooks, exports y comparacion multiambiente.
- No se capturan prompts/respuestas completos por defecto, ni se usan logs tecnicos como auditoria o fuente de producto.

## Fuentes

- `docs/.prompting/master-plan-observabilioty-and-telemetry/`
- `docs/.prompting/master-plan-observabilioty-and-telemetry/03-modelo-objetivo-observabilidad.md`
- `docs/.prompting/master-plan-observabilioty-and-telemetry/04-contratos-correlacion-contexto.md`
- `docs/.prompting/master-plan-observabilioty-and-telemetry/08-telemetria-producto-ia.md`
- `docs/.prompting/master-plan-observabilioty-and-telemetry/13-topologia-multiambiente.md`
- `docs/master-plan/analysis/observability-strategy.md`
