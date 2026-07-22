# Integración automatizada con Docker Compose

## Objetivo

Validar imágenes reales de GradeOps comunicándose entre sí, manteniendo Firebase y GenAI simulados y PostgreSQL real. Debe ser ejecutable localmente y en CI sin credenciales externas.

## Archivos propuestos

```text
testkit/compose/compose.base.yml
testkit/compose/compose.acceptance.yml
testkit/compose/compose.integration.yml
testkit/compose/compose.functional.yml
testkit/scripts/test-up
testkit/scripts/test-run
testkit/scripts/test-down
```

## Escenarios de integración

| Perfil | Servicios reales | Simulados | Runner |
|---|---|---|---|
| `api-agents` | API, Agents, PostgreSQL | Firebase, GenAI, email/storage | HTTP runner |
| `web-api` | Web, API, PostgreSQL | Firebase, Agents, email/storage | Playwright |
| `full-chain` | Web, API, Agents, PostgreSQL | Firebase, GenAI, email/storage | Playwright + assertions backend |

`web-api` simula Agents porque el objetivo es la frontera Web–API. `full-chain` usa Agents real para validar la cadena completa.

## Reglas de Compose

- Imágenes construidas desde los Dockerfiles reales y etiquetadas con SHA.
- Healthchecks reales; `depends_on: service_healthy` solo como apoyo, nunca como reemplazo de readiness.
- Puertos internos por defecto; publicar únicamente los necesarios para el runner.
- Red aislada y nombres DNS estables.
- Base de datos efímera por ejecución, migrada con Flyway y seeded determinísticamente.
- Volúmenes nombrados por project name y eliminados al finalizar.
- `--abort-on-container-exit`, `--exit-code-from test-runner` y timeout global.
- Recolección de logs/inspect/health antes del teardown, incluso cuando falle.
- `COMPOSE_PROJECT_NAME` único por job para permitir paralelismo.

## Flujo full-chain mínimo

1. Crear identidad Teacher simulada.
2. Abrir Web y autenticar sesión de prueba.
3. Crear assessment mediante API.
4. API llama a Agents con correlation/idempotency context.
5. Agents llama al GenAI stub y valida su salida.
6. API persiste draft y evidencia.
7. Web muestra el draft.
8. La prueba verifica DB/eventos solo como evidencia secundaria; la aserción principal se realiza en la frontera del usuario.
