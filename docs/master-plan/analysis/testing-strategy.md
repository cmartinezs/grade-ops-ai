# Estrategia transversal de testing - GradeOps AI

> Incorporacion del paquete `docs/.prompting/master-plan-testing-strategy/` al Master Plan.
> Este documento no crea una release tecnica independiente: define los gates de prueba y calidad que cada release funcional debe implementar de forma incremental.

## Principio rector

Testing no es una unica suite end-to-end. GradeOps AI debe detectar cada defecto en la capa mas rapida y determinista posible, y reservar los ambientes reales para smoke, promocion y evidencia.

| Capa | Dependencias | Proposito | Gate |
|---|---|---|---|
| Unitario/componente | En memoria o mocks locales | Reglas, UI, handlers, prompts, parsers y adapters | Cada PR |
| Aceptacion aislada | Imagen real con externos simulados | Validar la frontera publica del artefacto desplegable | PR relevante |
| Contrato | Consumidor/proveedor sin stack completo | Evitar drift Web-API-Agents | PR relevante |
| Integracion Compose | Web/API/Agents reales, Firebase/GenAI simulados, PostgreSQL real | Probar comunicacion y configuracion transversal | PR transversal y nightly |
| Funcional local real | Stack local con Firebase/GenAI aislados reales | Smoke humano controlado y exploracion | Manual explicito |
| Beta | Vercel + Render + Neon | Validacion continua pre-demo | Post-deploy |
| Demo | GCP | Evidencia estable y promocion | Aprobado/post-deploy |

Las suites hermeticas nunca consumen Firebase ni GenAI reales. Las llamadas reales a modelos son smoke/evaluaciones separadas con presupuesto, credenciales y datos sinteticos exclusivos.

## Fuentes incorporadas

| Area | Fuente |
|---|---|
| Paquete transversal | `docs/.prompting/master-plan-testing-strategy/` |
| Modelo objetivo | `docs/.prompting/master-plan-testing-strategy/03-modelo-objetivo-y-piramide.md` |
| Unitarias y coverage | `docs/.prompting/master-plan-testing-strategy/04-unitarias-componentes-y-coverage.md` |
| Aceptacion aislada | `docs/.prompting/master-plan-testing-strategy/05-aceptacion-aislada-por-artefacto.md` |
| Contratos y simuladores | `docs/.prompting/master-plan-testing-strategy/06-contratos-y-simuladores.md` |
| Integracion Compose | `docs/.prompting/master-plan-testing-strategy/07-integracion-automatizada-compose.md` |
| Beta/demo y promocion | `docs/.prompting/master-plan-testing-strategy/09-beta-y-demo.md` |
| SonarQube/JMeter | `docs/.prompting/master-plan-testing-strategy/15-sonarqube-jmeter-quality-gates.md` |

## Contrato minimo

Cada release que toque `web/`, `api/`, `agents`, `.github/`, `infra/` o `testkit/` debe declarar:

- suites unit/component por artefacto afectado;
- coverage generado en formato humano y maquina: LCOV/Cobertura para Web, JaCoCo XML/HTML para Java;
- contract checks Web-API, API-Agents y JSON Schema de comandos/resultados cuando cambie un contrato;
- aceptacion black-box de la imagen desplegable cuando cambie una frontera publica;
- reachability de UI para cada ruta funcional: la prueba debe iniciar desde una accion visible de usuario, no desde la URL directa, salvo pruebas tecnicas explicitamente marcadas como deep-link/guard;
- pruebas de semantica de datos UI cuando hay formularios, filtros, tablas o dashboards: controles correctos para texto libre, texto restringido, enums, datos maestros, seleccion multiple, numeros, booleanos, fechas, read-only/provenance y outputs generados editables;
- pruebas de API I/O por pantalla: datos de lectura, escritura, catalogos, defaults, capabilities, errores y estados deben estar cubiertos por contrato Web-API o acceptance segun riesgo;
- pruebas de comunicacion sync/async: las acciones asincronas deben demostrar completion/progress por polling, SSE, WebSocket, webhook server-to-server o push/notification segun contrato;
- pruebas de i18n cuando haya texto user-facing o contenido GenAI visible: translation keys, fallback, `Accept-Language`/locale efectivo, catalog labels, safe errors, `outputLocale` y logs/telemetria no localizados;
- integracion Compose requerida por impacto: `api-agents`, `web-api` o `full-chain`;
- smoke post-deploy si la release usa `beta` o `demo` como evidencia;
- Sonar quality gate sobre codigo nuevo, calibrado desde baseline real;
- JMeter smoke/baseline cuando cambien rutas criticas, latencia, asincronia, batch o exports;
- artefactos: JUnit XML, coverage, Playwright, contract diff, Compose logs, `run-manifest.json`, `summary.json` y reportes de performance cuando apliquen.

Los cambios solo documentales no deben levantar contenedores.

## Testkit objetivo

El testkit debe evolucionar como capacidad compartida, sin duplicar stacks por repo:

```text
testkit/compose/compose.base.yml
testkit/compose/compose.acceptance.yml
testkit/compose/compose.integration.yml
testkit/compose/compose.functional.yml
testkit/fixtures/
testkit/scenarios/
testkit/performance/
testkit/scripts/test-up
testkit/scripts/test-run
testkit/scripts/test-down
```

El Compose hermetico usa PostgreSQL real y simuladores versionados para Firebase, GenAI, Agents API, GradeOps API, email y storage. El modo funcional real usa archivos ignorados, credenciales exclusivas y guardas fail-fast para impedir cruces con `beta`, `demo` o produccion.

## Aplicacion por release

| Release | Incremento de testing requerido | No incluir aun |
|---|---|---|
| R01 | Baseline unit/component/coverage por artefacto afectado, contract checks Assessment Web-API/API-Agents, aceptacion aislada de API/Agents/Web para assessment creation, reachability desde boton de dashboard hacia `/assessments/new`, pruebas de controles correctos para `learningGoal`/`topic`/`level`/`duration`/`language`, contrato sync/async para create/generate draft y completion model si generation/regeneration es operation-backed, pruebas i18n de copy dashboard/intake/draft, locale efectivo, safe errors, catalog labels y `outputLocale`, Compose `api-agents`/`web-api`/`full-chain` minimo y primer `summary.json` | Suite E2E gigante, GenAI real como gate de PR, dashboard historico completo |
| R02 | Contratos Rubric/Grading/Feedback, pruebas de upload/submission con PostgreSQL real y externos simulados, acceptance de Web/API/Agents, Compose full-chain Open y JMeter smoke de rutas criticas | Bulk imports, performance load/soak amplio, evaluacion IA masiva |
| R03 | Tests de agregacion, estimates y reports con datos sinteticos; contratos report/gap/recovery; Compose full-chain de reporte; baseline de performance para agregaciones | BI dashboard avanzado, pruebas de carga con datos reales |
| R04 | Fixtures/golden files de question generation/review/assembly, JSON Schema de outputs, GenAI mock adversarial, Compose Closed authoring y `ai-eval` separado con dataset dorado | Scoring IA, cargas masivas contra modelos reales |
| R05 | Playwright student-link flow, tests negativos de token/replay/tamper, persistencia real de attempts/grading deterministic, Compose Closed full-chain y JMeter smoke allowlisted en rutas publicas | Student accounts, proctoring, stress sobre Demo |
| R06 | Quality dashboard/artefact ingestion, smoke beta/demo, digest promotion proof, Sonar/JMeter summaries, export leakage tests, readiness evidence y retention policy | BI generico, cargas no autorizadas sobre ambientes compartidos |

## Gate de testing por tarea

Toda tarea que cree o modifique endpoints, rutas, agentes, prompts, providers, schemas, migrations, CI, deploy, observabilidad, exports o performance debe incluir un checkpoint de testing.

El checkpoint debe verificar:

- capa primaria de prueba correcta: unit/component/acceptance/contract/integration/smoke/performance;
- no usar dependencias externas reales en suites hermeticas;
- fixtures versionados y validados contra contratos;
- pruebas unitarias/componentes cubren botones, links, menus y callbacks de navegacion; acceptance/e2e cubren el camino desde la accion visible hasta la pantalla destino;
- las pruebas de flujo funcional no arrancan en URLs internas como unico happy path; un deep-link puede probar guards, 404 o reload, pero no reemplaza la prueba de acceso desde UI;
- unit/component tests verifican que enums/catalogos se rendericen como select/radio/tags/badges, numeros como controles numericos con unidad/rango y textos libres como `Textarea`/`Input` con limites; acceptance/e2e cubren valores validos, valores invalidos y ausencia/fallo de catalogos;
- contract/acceptance tests verifican que toda pantalla lea/escriba datos desde `api/`; si el API requerido no existe, la prueba o checklist debe fallar y derivar a tarea `api/`, no a mock permanente;
- para acciones asincronas, tests cubren estado inicial, queued/running, completion, failure/timeout, retry/cancel cuando aplique, y el mecanismo acordado de polling/SSE/WebSocket/webhook/push;
- tests i18n cubren que no haya copy user-facing hardcodeado, que los errores seguros/catalogos respeten locale/fallback, que `outputLocale` llegue a comandos API-Agents cuando hay salida visible, y que logs/traces/metrics sigan en ingles;
- PostgreSQL/Flyway real cuando la semantica de persistencia importe;
- comandos reproducibles con timeout, healthcheck, teardown y recoleccion de artefactos;
- owner, flakiness policy y criterio de retry no silencioso;
- coverage/quality gate sobre codigo nuevo o baseline medido sin regresion;
- Sonar no sustituye tests, SAST ni revision humana;
- JMeter declara perfil, host allowlist, presupuesto, concurrencia, duracion, abortado y artefactos;
- secretos, tokens, prompts completos y PII no aparecen en logs, screenshots, videos, traces, JTL, coverage ni summaries;
- tarea `.github/`, `infra/` o testkit cuando cambien CI, secretos, ambientes, recursos o promocion.

## Criterio de salida

Una release no queda lista solo porque paso el happy path manual. Debe demostrar:

- comandos de test reproducibles para cada artefacto afectado;
- frontera publica probada por black-box cuando cambia el deployable;
- cada funcionalidad accesible desde una accion de UI documentada y testeada;
- campos UI probados segun semantica real de datos y fuente de verdad, sin convertir datos maestros/restringidos en texto libre no gobernado;
- todos los datos I/O de pantalla alineados con `api/`, con contrato probado o tarea/residual explicito si falta;
- cada flujo async probado con mecanismo de finalizacion/progreso definido, sin timers locales ni spinners indefinidos;
- i18n probado para superficies user-facing modificadas: translation keys completas, fallback, locale efectivo, contratos Web-API/API-Agents y observabilidad no localizada;
- contratos versionados y consumidores/proveedores compatibles;
- journeys transversales deterministas en Compose antes de usar `beta`/`demo` como prueba;
- smoke post-deploy con imagen/digest, ambiente y resultado documentados;
- reportes y artefactos normalizados, sin secretos ni PII;
- performance smoke/baseline cuando el flujo introduce rutas criticas, batch, asincronia, dashboards o exports.

## Historial de cambios

| Fecha | Cambio | Motivo | Elementos afectados | Decision asociada |
|---|---|---|---|---|
| 2026-07-21 | Incorporacion de i18n testing | Probar locale, translation keys, safe errors, catalog labels, output GenAI y logs/telemetria en ingles | Testing strategy, R01, Web-API/API-Agents contracts | D-I18N-01..D-I18N-10 |
| 2026-07-21 | Incorporacion de API I/O y sync/async testing | Probar que datos de pantalla se sostienen en `api/` y que flujos asincronos tienen completion model verificable | Testing strategy, API-Web contracts, R01 | D-TEST-01..D-TEST-09, D-API-01..D-API-10 |
| 2026-07-21 | Incorporacion de UI Design/Data Semantics | Exigir pruebas para controles, datos maestros, enums, restricciones y valores invalidos en UI | Testing strategy, R01, tasks UI | D-UI-01..D-UI-08 |
| 2026-07-21 | Incorporacion de reachability por accion de UI | Hacer que unit/component, acceptance y e2e prueben la navegacion desde acciones visibles, no solo URLs directas | Testing strategy, R01, tasks UI | D-TEST-01..D-TEST-09 |
| 2026-07-21 | Creacion inicial | Convertir `master-plan-testing-strategy` en gates de testing y calidad por release funcional sin crear una release tecnica transversal | Master Plan, R01-R06, tareas futuras de web/api/agents/testkit/.github/infra | D-TEST-01..D-TEST-09 |
