# Estrategia transversal de seguridad - GradeOps AI

> Incorporacion del paquete `docs/.prompting/master-plan-security/` al Master Plan.
> Este documento no crea una release tecnica independiente: define controles obligatorios para que cada release funcional avance seguridad junto con valor de producto.

## Principio rector

La seguridad de GradeOps AI se implementa por capas y por release funcional.

`api/` conserva la autoridad de autenticacion validada, roles humanos, permisos, ownership, reglas de dominio, auditoria y proteccion de endpoints.

`agents/` no tiene RBAC humano. Autoriza llamadas service-to-service, capacidades de ejecucion, provider/model policy, limites, contratos, prompts, outputs y herramientas.

`web/` no es frontera de autorizacion. Representa capacidades y rutas seguras, minimiza exposicion de tokens/PII y debe tolerar respuestas `401`, `403`, `404`, `409` y `422` sin filtrar informacion.

El contrato funcional de seguridad debe ser igual en `demo` y `beta`; lo que cambia es la identidad de infraestructura, el hosting, la custodia de secretos y la forma de probar aislamiento.

## Fuentes incorporadas

| Area | Fuente |
|---|---|
| API authorization | `docs/.prompting/master-plan-security/api/` |
| API multi-environment topology | `docs/.prompting/master-plan-security/api/12-topologia-seguridad-multiambiente.md` |
| Agents runtime security | `docs/.prompting/master-plan-security/agents/` |
| Agents multi-environment topology | `docs/.prompting/master-plan-security/agents/11-topologia-seguridad-multiambiente.md` |
| Web security | `docs/.prompting/master-plan-security/web/` |
| Web multi-environment topology | `docs/.prompting/master-plan-security/web/11-topologia-seguridad-multiambiente.md` |

## Modelo objetivo

La autorizacion no se resuelve con una sola anotacion o guard de UI. Cada accion sensible debe atravesar estas capas:

| Capa | Responsabilidad |
|---|---|
| Identidad | Firebase para actores humanos; OIDC service-to-service en `demo`; JWT interno firmado y rotatorio en `beta` hasta disponer de identidad administrada equivalente. |
| Cuenta/rol | PostgreSQL como fuente canonica de `TEACHER` y `OPERATOR`. |
| Permission | Authorities semanticas y `@PreAuthorize` en casos de uso/endpoints. |
| Ownership/scope | Consultas scoped y denial seguro, preservando `404` cuando corresponde. |
| Dominio | Estado del assessment, approval, publish, billing, limits e invariantes. |
| Auditoria | Actor derivado del principal autenticado, nunca del body. |
| UI | Capacidades y rutas funcionales devueltas por API, sin decision de ownership. |

## Topologia multiambiente

`demo` y `beta` no son intercambiables:

| Dimension | `demo` | `beta` |
|---|---|---|
| Web | Firebase App Hosting/GCP o hosting GCP definido | Vercel |
| API | Cloud Run | Render |
| Agents | Cloud Run privado | Render endurecido en aplicacion |
| Base de datos | Cloud SQL PostgreSQL | Neon PostgreSQL |
| Archivos | GCS | Cloudflare R2 segun perfil actual |
| Identidad humana | Firebase del proyecto `demo` | Firebase del proyecto `beta` |
| Service-to-service | IAM + OIDC de Cloud Run | JWT interno de vida corta, firmado por `api`, validado por `agents` |
| Secretos | Secret Manager + IAM | Render Secret Files/Environment con rotacion documentada |

Reglas obligatorias:

- Un build de `web` se asocia a un unico ambiente; no existe selector de backend en el navegador.
- Tokens Firebase de un ambiente no son validos ante la API del otro.
- `api` y `agents` rechazan audience, issuer, capability, environment o `kid` incorrectos.
- `INTERNAL_API_SECRET` es compatibilidad transitoria; `demo` y `beta` deben fallar startup si conserva `change-me-in-production`.
- Previews de Vercel no apuntan a datos reales de `beta` salvo autorizacion explicita, temporal y auditada.
- CORS usa allowlist exacta por ambiente; nunca `*` con `Authorization`.
- No hay secretos en imagenes, repositorio, logs, bundle web, source maps publicos o respuestas de error.
- Debe existir evidencia versionada de configuracion de `beta`: infraestructura declarativa o manifiesto de variables, secretos y controles.

## Aplicacion por release

| Release | Incremento de seguridad requerido | No incluir aun |
|---|---|---|
| R01 | Fundacion de cuenta/roles, `AuthenticatedAccount`, permisos base Teacher, ownership en assessment, errores seguros, actor auditado desde principal, fail-secure minimo en `agents`, tokens no persistidos en `web`, contrato multiambiente `demo`/`beta` definido | Operator completo, BFF/cookies HttpOnly, red privada Render como requisito, preview deployments con datos reales |
| R02 | Authorities para rubric/submission/grading/feedback, validacion de archivos, minimizacion PII de learner refs, idempotencia segura, provider/model allowlist y limites por request | Ejecucion de codigo, plagiarism accusation, batch durable si no hay volumen |
| R03 | Proteccion de reportes/gaps/recovery con agregacion segura, ocultamiento de detalles student-level, reportes student-safe sin costos/evidencia interna, herramientas read-only scoping | Publicacion automatica o perfiles high-stakes |
| R04 | Seguridad de question bank/snapshot, ownership de banco, snapshot inmutable por API/modelo de datos, policy engine basico para generation/review/assembly, prompts/output cerrados | Datos estudiantiles, scoring IA, compartir banco sin permiso explicito |
| R05 | Invitation context sin login, tokens hasheados/expirables/revocables, anti-enumeracion, result access scoped, replay/tamper tests, rate limiting en rutas publicas | Student accounts, proctoring, OCR/OMR, integridad remota avanzada |
| R06 | Operator auth, `/api/v1/operator/**`, access minimo a dashboards/exports, allowlist public-safe, evidence links revocables, health sin secretos, audit/readiness no editable desde UI | Consola admin generica, export privado sin canal controlado, calculos de negocio por LLM |

## Gate de seguridad por tarea

Toda tarea que cree o modifique endpoints, rutas funcionales, agentes, prompts, providers, secrets, uploads, links publicos, exports o datos sensibles debe incluir un checkpoint de seguridad.

El checkpoint debe verificar:

- Actor autenticado y fuente de identidad.
- Permission requerida y rol minimo.
- Ownership/scope server-side.
- Estado de dominio requerido antes de ejecutar la accion.
- Semantica de denial (`401`, `403`, `404`, `409`, `422`) sin filtracion.
- Auditoria con actor derivado del principal, correlation ID y resultado.
- Datos sensibles minimizados en responses, logs, analytics y exports.
- Idempotencia/replay protection cuando haya comandos mutantes, links o ejecuciones GenAI.
- Secrets y provider keys server-side only.
- Ambiente objetivo declarado (`demo`, `beta` o ambos) y controles especificos documentados.
- Aislamiento cruzado probado: Firebase project, API URL, DB, storage, service identity y secrets no se mezclan entre ambientes.
- Service-to-service acorde al ambiente: OIDC/IAM en `demo`; JWT interno firmado, rotatorio y con `kid` en `beta`.
- CORS, redirects, authorized domains, headers y previews validados sobre URLs desplegadas cuando haya despliegue real.
- Pruebas negativas para rol incorrecto, ownership cruzado, token expirado/revocado, tampering y estado invalido.
- Tarea `infra/` cuando cambien hosting, headers, IAM, Secret Manager, Cloud Run privado, egress o service accounts.

Si una release decide postergar un control, debe registrar el residual como deuda explicita en la release y no marcarlo como implementado.

## Criterio de salida

Una release no queda lista solo porque el happy path funcione. Debe demostrar:

- UI manipulada no puede saltarse autorizacion de `api/`.
- Teacher no accede recursos de otro Teacher.
- Operator no ve PII academica por defecto.
- Student links no enumeran, no filtran y respetan expiracion/revocacion.
- `agents` solo acepta llamados internos autorizados y comandos con policy.
- `demo` y `beta` tienen aislamiento probado y evidencia de configuracion reproducible o manifiesto versionado.
- Logs y exports no contienen secretos, tokens, prompts completos ni PII innecesaria.
- Pruebas negativas y smoke de seguridad quedan documentados junto con los criterios funcionales.

## Historial de cambios

| Fecha | Cambio | Motivo | Elementos afectados | Decision asociada |
|---|---|---|---|---|
| 2026-07-21 | Incorporacion de topologia multiambiente | Separar controles de `demo` GCP y `beta` Vercel-Render-Neon sin cambiar el contrato funcional de autorizacion | Modelo objetivo, gate de seguridad, R01-R06 | D-SEC-01..D-SEC-08 |
| 2026-07-21 | Creacion inicial | Convertir `master-plan-security` en reglas operativas por release sin crear una release tecnica transversal | Master Plan, R01-R06, tareas futuras de api/agents/web/infra | D-SEC-01..D-SEC-08 |
