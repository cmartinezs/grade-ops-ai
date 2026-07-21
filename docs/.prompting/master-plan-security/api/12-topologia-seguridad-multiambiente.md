# Topología de seguridad multiambiente de `api/`

## Objetivo

La autorización funcional debe comportarse igual en ambos ambientes, pero la identidad de infraestructura y la gestión de secretos no son equivalentes.

| Dimensión | `demo` | `beta` |
|---|---|---|
| Hosting API | Cloud Run | Render |
| Base de datos | Cloud SQL PostgreSQL | Neon PostgreSQL |
| Firebase Admin | ADC y service account | `FIREBASE_ADMIN_CREDENTIALS` |
| Archivos | GCS | Cloudflare R2 según perfil actual |
| Identidad servicio a servicio objetivo | IAM + OIDC de Cloud Run | Token de servicio firmado y rotatorio, hasta disponer de identidad administrada equivalente |
| Secretos | Secret Manager + IAM | Secret Files/Environment de Render, con rotación externa documentada |
| Entrada desde web | Firebase/App Hosting o hosting GCP | Vercel |

## Evidencia actual

- `application-demo.yml` declara GCS y documenta Firebase mediante ADC.
- `application-beta.yml` consume `DATABASE_URL`, credenciales Firebase serializadas y credenciales de R2.
- `application.yml` conserva `INTERNAL_API_SECRET`, con un valor por defecto inseguro para producción.
- La infraestructura Terraform versionada cubre `infra/terraform/environments/demo`; no existe un equivalente versionado para Render, Vercel y Neon.

## Contrato común obligatorio

En ambos ambientes deben ser idénticos:

1. Verificación de Firebase ID token, revocación, issuer, audience y correo verificado.
2. Roles y permissions canónicos en PostgreSQL.
3. `@PreAuthorize`, ownership por consulta y reglas de dominio.
4. Separación entre endpoints humanos y `/internal/**`.
5. Auditoría derivada del principal, nunca del body.
6. Respuestas `401`, `403` y `404` sin filtración de existencia.
7. Correlation ID, rate limits, redacción de PII y pruebas negativas.

## `demo`: controles GCP

- Un service account diferente para `api/` y `agents/`.
- Cloud Run privado para `agents/`; solo `api` recibe `roles/run.invoker`.
- ID token OIDC con `aud` igual a la URL estable del servicio y validación estricta de issuer, audience y subject.
- Secret Manager para secretos no reemplazables por identidad de workload.
- Cloud SQL y GCS con mínimo privilegio; sin claves JSON persistentes.
- El endpoint público de `api` no debe conceder acceso a `/internal/**` solo porque el request alcanzó Cloud Run.

## `beta`: controles Render–Neon

Render no debe tratarse como si expusiera IAM de Cloud Run. Mientras `api` y `agents` sean servicios públicamente alcanzables:

- `api` firma un JWT interno de vida corta (recomendado: 2–5 minutos) con `iss`, `aud`, `sub`, `iat`, `exp`, `jti` y capabilities.
- `agents` valida firma, audience, expiración, algoritmo permitido y capability solicitada.
- Usar claves asimétricas: la privada queda solo en `api`; `agents` recibe únicamente la clave pública. Evitar perpetuar un secreto simétrico compartido.
- Soportar rotación mediante `kid` y dos claves válidas durante una ventana controlada.
- Aplicar rate limit y tamaño máximo aun después de autenticar al servicio.
- No confiar en IP, `Host`, `X-Forwarded-For` ni headers inyectables como identidad.
- Neon exige TLS, usuario por ambiente, privilegios mínimos, pool administrado y separación total respecto de `demo`.
- Las credenciales Firebase Admin, Neon y R2 deben estar separadas, rotadas y nunca usar prefijo `NEXT_PUBLIC_`.

El `INTERNAL_API_SECRET` actual queda como compatibilidad transitoria. Debe fallar el startup en `demo` y `beta` si conserva `change-me-in-production`.

## CORS y fronteras públicas

- Allowlist exacta por ambiente; no `*` con Authorization.
- Incluir el dominio canónico de Vercel para `beta`, no previews arbitrarios.
- Si se habilitan previews, usar un backend aislado o una allowlist automatizada con expiración; nunca apuntar previews no confiables a datos reales de beta.
- `/internal/**` no participa de CORS porque no debe invocarse desde navegador.

## Criterios de aceptación

- Un token Firebase de `demo` no es aceptado en `beta`, ni viceversa.
- Un JWT interno con audience, capability o ambiente incorrecto es rechazado.
- Rotar una clave interna no produce una interrupción ni prolonga indefinidamente la anterior.
- Teacher/Operator y ownership producen el mismo resultado en ambos ambientes.
- No hay secretos en imágenes, repositorio, logs, bundle web o respuestas de error.
- Existe una prueba E2E de autorización por ambiente y una prueba explícita de aislamiento cruzado.

## Decisiones pendientes

1. Confirmar si Render permite red privada entre los servicios del plan contratado; usarla como defensa adicional, no como autenticación.
2. Elegir mecanismo de custodia y rotación de la clave privada de `api` en beta.
3. Definir dominios canónicos y política de preview deployments de Vercel.
4. Incorporar configuración declarativa reproducible para beta o, como mínimo, un manifiesto versionado de variables, secretos y controles.
