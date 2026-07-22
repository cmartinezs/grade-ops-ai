# ADR - Testing y quality gates incorporados por release funcional

## Estado

Aceptada.

## Contexto

El paquete `docs/.prompting/master-plan-testing-strategy/` define una estrategia de pruebas en capas para `web/`, `api/`, `agents`, Compose local, CI/CD, `beta` y `demo`.

La necesidad es transversal, pero crear una release tecnica aislada para testing produciria infraestructura de pruebas desconectada de los flujos reales. Cada release funcional debe incorporar las pruebas, contratos, simuladores, artefactos y quality gates que necesita para demostrar su propio valor.

## Decision

Testing y quality gates se incorporan por release funcional y no como una release tecnica independiente.

- Las suites de PR deben priorizar unitarias/componentes, coverage, contratos, aceptacion aislada, integracion Compose por impacto, Sonar quality gate sobre codigo nuevo y security/dependency/container scanning complementario.
- `web/`, `api/` y `agents` publican coverage en formatos maquina y humano: LCOV/Cobertura para Web, JaCoCo XML/HTML para Java.
- Los contratos Web-API, API-Agents y comandos/resultados de agents se versionan y validan con fixtures/simuladores compartidos.
- Las suites hermeticas no invocan Firebase ni GenAI reales. Las pruebas con servicios reales son smoke/eval separados, con proyectos, credenciales, datos y presupuesto exclusivos.
- Compose se organiza como testkit raiz con archivos componibles y perfiles para acceptance, `api-agents`, `web-api`, `full-chain` y funcional real.
- `beta` y `demo` requieren smoke post-deploy, proof de digest/imagen, aislamiento de ambientes y rollback/promocion documentados.
- SonarQube/SonarCloud complementa tests y revision humana; no reemplaza unitarias, SAST, contratos ni criterios de arquitectura.
- JMeter se usa para performance HTTP con perfiles, host allowlist, presupuesto, duracion, concurrencia, abortado y artefactos; no se ejecuta carga sobre `beta` o `demo` sin autorizacion explicita.

## Consecuencias

- R01 debe crear el baseline operativo de testing, contratos y Compose para el primer journey `web -> api -> agents`.
- R02-R05 amplian los gates por flujo funcional Open/Closed, manteniendo GenAI real fuera del PR y usando simuladores deterministicos.
- R06 consolida evidencias, dashboard futuro de calidad, Sonar/JMeter summaries, smoke beta/demo, retention y promotion proof.
- Toda tarea que cambie CI, secretos, ambientes, recursos o testkit debe incluir scope `.github/`, `infra/` o manifest versionado.
- Los cambios solo documentales no deben levantar contenedores.

## Fuentes

- `docs/.prompting/master-plan-testing-strategy/`
- `docs/.prompting/master-plan-testing-strategy/03-modelo-objetivo-y-piramide.md`
- `docs/.prompting/master-plan-testing-strategy/05-aceptacion-aislada-por-artefacto.md`
- `docs/.prompting/master-plan-testing-strategy/06-contratos-y-simuladores.md`
- `docs/.prompting/master-plan-testing-strategy/07-integracion-automatizada-compose.md`
- `docs/.prompting/master-plan-testing-strategy/09-beta-y-demo.md`
- `docs/.prompting/master-plan-testing-strategy/15-sonarqube-jmeter-quality-gates.md`
- `docs/master-plan/analysis/testing-strategy.md`
