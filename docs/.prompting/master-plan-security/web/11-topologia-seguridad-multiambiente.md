# Topología de seguridad multiambiente de `web/`

## Principio rector

`web/` ofrece experiencia y defensa del navegador; `api/` sigue siendo la frontera de autorización. Esta regla no cambia entre GCP y Vercel.

| Dimensión | `demo` | `beta` |
|---|---|---|
| Hosting web | Firebase App Hosting/GCP según infraestructura actual | Vercel |
| API | Cloud Run | Render |
| Identidad humana | Firebase del proyecto `demo` | Firebase del proyecto `beta` |
| Proxy | Rewrites server-side | Rewrites de Next.js en Vercel |
| Configuración pública | Firebase web config del ambiente | Firebase web config del ambiente |

## Evidencia y brechas

- `next.config.ts` reescribe `/api/:path*` usando `API_BASE_URL`, variable server-side.
- Las variables `NEXT_PUBLIC_FIREBASE_*` se incorporan al bundle y no son secretos.
- Solo está configurado explícitamente `Cross-Origin-Opener-Policy`; el set completo de headers todavía es trabajo futuro.
- No existe evidencia versionada equivalente a Terraform para la configuración Vercel de beta.

## Controles comunes

- Proyectos/tenants Firebase separados por ambiente; no reutilizar usuarios, claves Admin ni datos.
- `API_BASE_URL` debe ser server-only. Nunca exponer `DATABASE_URL`, Firebase Admin, R2, Render API keys o claves internas.
- ID tokens solo en memoria de Firebase; no `localStorage`, logs o analytics.
- Guards y menús por rol son UX, no autorización.
- CSP, HSTS, `nosniff`, Referrer Policy, Permissions Policy y límites de framing en ambos despliegues.
- Source maps privados y errores al cliente sin secretos ni PII.

## Controles específicos de beta en Vercel

- Usar el dominio canónico de producción de beta en Firebase Authorized Domains, CORS de API y redirects de autenticación.
- No autorizar automáticamente todos los dominios `*.vercel.app`.
- Los preview deployments no deben conectarse a Neon/Render beta con datos reales por defecto.
- Separar variables Production, Preview y Development. Toda variable `NEXT_PUBLIC_` debe pasar revisión de exposición.
- Mantener el proxy same-origin `/api/*` cuando sea viable; si el navegador llama a Render directamente, aplicar CORS exacto y probar preflight.
- Proteger endpoints server-side de Next.js contra SSRF: destinos de proxy fijos, no derivados de input del usuario.
- Configurar CSP considerando Firebase Auth y Google sign-in sin abrir comodines innecesarios.

## Controles específicos de demo en GCP

- Vincular el dominio canónico de demo al proyecto Firebase correcto.
- Usar identidades y secretos de runtime, no credenciales Admin horneadas en el build.
- Validar que CDN/hosting preserve los headers de seguridad de Next.js.

## Aislamiento y aceptación

- Un build se asocia a un único ambiente; no existe selector de backend en el navegador.
- Tokens de un proyecto Firebase no son válidos ante el API del otro.
- Preview de Vercel no alcanza datos reales de beta salvo autorización explícita y temporal.
- CSP y headers se verifican sobre las URLs desplegadas, no solo en tests unitarios.
- Logout, expiración y revocación se prueban en demo y beta.
- Cada pipeline ejecuta una comprobación que impide mezclar Firebase project ID, API URL y nombre de ambiente.

## Decisiones pendientes

1. Dominio canónico de beta y política de previews.
2. Si Vercel usará proxy same-origin exclusivamente o permitirá llamadas directas a Render.
3. Mecanismo automatizado para verificar headers y configuración pública después del despliegue.
