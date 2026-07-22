# SonarQube, JMeter y quality gates

## Responsabilidades distintas

| Capacidad | Herramienta | Qué valida | Qué no sustituye |
|---|---|---|---|
| Calidad estática | SonarQube/SonarCloud | bugs, vulnerabilidades, hotspots, smells, duplicación y cobertura importada | tests, SAST especializado, revisión y arquitectura |
| Rendimiento HTTP | JMeter | latencia, throughput, errores, concurrencia, carga y soak | navegación real y Core Web Vitals |
| Rendimiento Web | Lighthouse + Playwright | carga, renderizado, recursos y experiencia de navegador | capacidad interna de API/Agents |

## SonarQube local

Agregar un perfil opcional al testkit, no al arranque funcional por defecto:

```text
testkit/compose/compose.quality.yml
testkit/sonar/quality-gate.md
testkit/scripts/quality-up
testkit/scripts/quality-scan
testkit/scripts/quality-down
```

`compose.quality.yml` debe levantar SonarQube y su PostgreSQL dedicado, con versiones fijadas, healthcheck, volumen nombrado y límites de recursos. No debe reutilizar la base GradeOps. Las credenciales locales son desechables y nunca se promueven a ambientes.

Los scanners deben importar:

- `web/`: LCOV y resultados de tests; exclusiones solo para código generado, build y fixtures.
- `api/`: JaCoCo XML y resultados Surefire/Failsafe.
- `agents/`: JaCoCo XML y resultados Surefire/Failsafe.

Cada artefacto debe tener `sonar-project.properties` o configuración equivalente, `projectKey` estable y rutas explícitas. El análisis monorepo puede orquestarse desde raíz, pero debe conservar métricas y ownership por artefacto.

### Quality gate recomendado

Aplicar Clean as You Code sobre código nuevo:

- cero bugs y vulnerabilidades nuevas;
- security hotspots nuevos revisados;
- duplicación nueva bajo el umbral acordado;
- coverage de código nuevo con baseline inicial medido y objetivo incremental;
- rating de mantenibilidad/confiabilidad/seguridad acordado;
- análisis fallido o incompleto bloquea el PR.

No imponer inmediatamente un porcentaje global alto al legado. Primero medir baseline, impedir regresión y elevar el estándar por etapas. La cobertura sigue siendo generada por Jest/JaCoCo; Sonar la consume, no la calcula como sustituto.

En CI debe decidirse entre SonarQube autogestionado y SonarCloud. El contrato del pipeline debe ser portable: scanner, reportes, estado del quality gate y snapshot normalizado. No desplegar SonarQube dentro de `beta` o `demo`; es infraestructura de ingeniería separada.

## JMeter y modelo de rendimiento

Estructura propuesta:

```text
testkit/performance/plans/api.jmx
testkit/performance/plans/agents.jmx
testkit/performance/plans/full-chain.jmx
testkit/performance/data/
testkit/performance/profiles/smoke.properties
testkit/performance/profiles/baseline.properties
testkit/performance/profiles/load.properties
testkit/performance/profiles/soak.properties
testkit/scripts/performance-run
```

Los `.jmx` deben parametrizar `baseUrl`, concurrencia, ramp-up, duración, timeouts, dataset y correlation IDs. Nunca versionar tokens o secretos. Ejecutar JMeter siempre en modo non-GUI y generar:

- JTL;
- dashboard HTML;
- JUnit compatible para gates;
- `performance-summary.json` con p50/p95/p99, throughput, errores y metadata del ambiente.

### Escenarios

| Suite | Destino | Externos | Uso |
|---|---|---|---|
| `api` | API real en Compose | Firebase y Agents simulados; PostgreSQL real | capacidad de endpoints y persistencia |
| `agents` | Agents real en Compose | GenAI simulado con latencia/error configurables | pipeline, validación y concurrencia |
| `full-chain` | API → Agents reales | Firebase/GenAI simulados | presupuesto de latencia extremo a extremo |
| `beta-smoke/load` | Vercel/Render/Neon Beta | integraciones controladas; GenAI mock salvo prueba autorizada | validar despliegue y capacidad serverless |
| `demo-smoke` | GCP Demo | reales solo cuando el caso lo exige | comprobación breve post-release |

JMeter no debe usarse como prueba UI de `web/`. Puede medir endpoints servidos por Web o rutas server-side, pero la UX se valida con navegador real.

## Perfiles y gates

- `smoke`: 1–5 usuarios, pocos minutos, apto para PR según impacto y post-deploy.
- `baseline`: carga estable y repetible en Compose, nightly; detecta regresiones.
- `load`: aproxima concurrencia objetivo; manual/programada en Beta.
- `soak`: varias horas para fugas y degradación; fuera del PR y con autorización.
- `stress`: encuentra el límite; solo en entorno efímero dedicado, nunca sobre Demo compartido.

Los umbrales deben nacer de SLO y mediciones, no de números inventados. Como guardas iniciales:

- error rate sin errores funcionales inesperados;
- p95/p99 sin regresión significativa respecto del baseline del mismo escenario y capacidad;
- throughput mínimo explícito;
- abortado ante error rate, latencia, consumo o costo máximos;
- resultados comparables solo con misma versión, perfil, dataset y topología.

## Seguridad y costos

- Allowlist exacta de hosts por perfil; rechazar dominios Productivos/no declarados.
- Secretos inyectados por CI y redactados de JTL/logs.
- Datos sintéticos, idempotentes y eliminables.
- Header identificador de carga y correlation ID por ejecución.
- Límites de autoscaling, requests, tokens y gasto.
- GenAI simulado para carga; llamadas reales solo en smoke separado, con API key y presupuesto exclusivos.
- Ventana y autorización para Beta/Demo; coordinación con observabilidad y rollback.

## Dashboard futuro

El agregador debe capturar por commit, digest, ambiente, suite y perfil:

- estado del quality gate y métricas Sonar relevantes;
- deuda y findings nuevos/resueltos;
- p50/p95/p99, throughput y error rate JMeter;
- comparación contra baseline y release anterior;
- referencia a reportes completos, sin ingerir HTML como fuente canónica.

SonarQube y JMeter generan evidencia, pero `summary.json` continúa siendo el contrato estable para el dashboard transversal.
