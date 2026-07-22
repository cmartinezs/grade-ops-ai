# Security & Configuration Baseline — 2026-07-22

> Evidencia de la tarea 7 de `docs/.prompting/pre-master-plan/clean.md`: revisión mínima de seguridad/configuración antes de certificar el cierre de esta planning. No es un security review completo — es la verificación puntual pedida por esa tarea.

- **SHA:** `449c7b05c758e599f76cf632cca81a8b30cddf96`
- **Branch:** `planning/master-plan`
- **Fecha:** 2026-07-22

## 1. `api/smoke/fake-service-account.json` — ya documentado correctamente

**Corrección respecto al hallazgo original de la tarea 1 (inventario):** ese hallazgo decía que "nada en el repo lo declara explícitamente como fake". Verificado ahora que es **incorrecto** — `api/smoke/README.md` ya existe y ya documenta el archivo con precisión:

- Lo describe como "a **throwaway** RSA key pair with no relation to any real Google Cloud or Firebase project".
- Explica exactamente por qué es seguro commitearlo: `FirebaseConfig.firebaseApp()`'s `GoogleCredentials.fromStream(...)` solo parsea la key localmente al boot, nunca hace una llamada de red con ella.
- Explica el mecanismo real (Firebase Admin SDK redirige a `FIREBASE_AUTH_EMULATOR_HOST` cuando está seteado).
- Incluye advertencia explícita: **"Never use these files or the `smoke` profile in `demo`/`beta`/production."**

No se requirió ningún cambio. Este ítem se cierra como **verificado**, no como corregido — y deja registrado que el inventario inicial (tarea 1) tuvo un falso negativo aquí, para no repetirlo en futuras auditorías de este tipo.

## 2. `.env` real trackeado — confirmado que no existe

```
git ls-files | grep -E '\.env'
→ .env.example
→ agents/.env.example
→ web/.env.local.example
```

Ningún `.env` real está trackeado — solo los tres `.example` con placeholders. `.gitignore` líneas 4-6 excluye `.env` y `.env.*`, con excepción explícita `!.env.example`. Correcto, sin acción requerida.

## 3. Credenciales locales duplicadas en compose — aceptado como riesgo nulo, documentado

Confirmado que las mismas credenciales Postgres en texto plano se repiten en 3 archivos:

```
compose.yml:7-9            POSTGRES_DB/USER/PASSWORD: gradeops
api/compose.yml:5-7        POSTGRES_DB/USER/PASSWORD: gradeops
api/compose.smoke.yml:9-11 POSTGRES_DB/USER/PASSWORD: gradeops
```

Y que `INTERNAL_API_SECRET` cae al mismo default en 2 servicios de `compose.yml` (líneas 27 y 50, `${INTERNAL_API_SECRET:-dev-secret-change-me}`) y en `.env.example:23`.

**Decisión explícita:** esto es aceptado, no es un hallazgo a corregir. Son defaults de desarrollo local únicamente — nunca se leen desde un ambiente `beta`/`demo` real (ver punto 4, donde esos ambientes no tienen ningún default hardcodeado, solo `${ENV_VAR}` sin fallback o con fallback distinto por entorno). Duplicar un mismo valor trivial (`gradeops`/`dev-secret-change-me`) entre `compose.yml` files locales no constituye una fuga de secreto real: cualquiera con acceso al repo ya podría levantar el mismo stack local con esas mismas credenciales por diseño. **Nota para el futuro:** si algún día se agrega un `compose.override.yml` o un ambiente adicional que no sea puramente local, no reutilizar `dev-secret-change-me` ni `gradeops`/`gradeops` — son marcadores de "esto es local", no una convención a extender.

## 4. `application-beta.yml` / `application-demo.yml` — sin mezcla de credenciales entre ambientes

Confirmado en `api/` y `agents/`:

| Servicio | Archivo | Secretos | Storage/bucket | Notas |
|---|---|---|---|---|
| `api/` | `application-beta.yml` | `${DATABASE_URL}`, `${DATABASE_USER:gradeops}`, `${DATABASE_PASSWORD:}`, `${FIREBASE_ADMIN_CREDENTIALS:}`, `${R2_ACCOUNT_ID}`, `${R2_ACCESS_KEY}`, `${R2_SECRET_KEY}` | R2 bucket `${R2_BUCKET:gradeops-beta}` | Storage backend Cloudflare R2, distinto del de `demo` |
| `api/` | `application-demo.yml` | Sin secretos propios — Cloud SQL vía `DATABASE_URL`, Firebase vía ADC (service account de Cloud Run, "no extra config needed") | GCS bucket `${GCS_BUCKET:gradeops-demo}` | Storage backend Google Cloud Storage, distinto del de `beta` |
| `agents/` | `application-beta.yml` | `${GRADEOPS_GEMINI_API_KEY}`, `${GRADEOPS_GEMINI_MODEL}`, `${GRADEOPS_GROQ_API_KEY}`, `${GRADEOPS_GROQ_BASE_URL}`, `${GRADEOPS_GROQ_MODEL}` | — | Gemini vía API key de Google AI Studio (free tier) |
| `agents/` | `application-demo.yml` | `${GOOGLE_CLOUD_PROJECT}`, `${VERTEX_AI_LOCATION:us-central1}`, `${GRADEOPS_GROQ_MODEL}`, `${GRADEOPS_GROQ_API_KEY}`, `${GRADEOPS_GROQ_BASE_URL}` | — | Gemini vía Vertex AI (ADC, sin API key estática) — distinto mecanismo de auth del de `beta`, no solo distinto valor |

Ningún archivo tiene un valor hardcodeado — todo es `${ENV_VAR}` o `${ENV_VAR:default-no-secreto}` (los únicos defaults sin `:` son nombres de bucket/región, no secretos). `beta` y `demo` usan buckets distintos, y en `agents/` incluso mecanismos de autenticación distintos para Gemini (API key vs ADC/Vertex) — no hay forma de que un despliegue en un ambiente lea accidentalmente el secreto o el bucket del otro. Sin acción requerida — verificación registrada como evidencia de cierre.

## Resumen

| # | Ítem | Resultado |
|---|------|-----------|
| 1 | `fake-service-account.json` declarado como fixture fake | ✅ Ya estaba documentado (`api/smoke/README.md`) — hallazgo original de la tarea 1 era un falso negativo, corregido aquí |
| 2 | Sin `.env` real trackeado | ✅ Confirmado |
| 3 | Credenciales locales duplicadas en compose | ✅ Aceptado explícitamente como riesgo nulo (solo local), documentado |
| 4 | Sin mezcla de credenciales beta/demo | ✅ Confirmado — buckets y mecanismos de auth distintos por diseño |

Sin fallas de seguridad encontradas. Sin cambios de código requeridos.
