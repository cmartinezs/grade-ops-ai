# Estado actual y evidencia

## Autenticación

`src/lib/firebase/client.ts` inicializa Firebase con variables `NEXT_PUBLIC_FIREBASE_*` y exporta `getAuth(app)`. Estas claves identifican el proyecto Firebase y necesariamente llegan al navegador; no deben confundirse con secretos de servidor.

`login/page.tsx` usa `signInWithEmailAndPassword`. `register/page.tsx` crea primero el usuario Firebase, obtiene un ID token y llama a `POST /api/v1/auth/register`. Google sign-in entrega igualmente un ID token a `registerTeacher`.

## Navegación protegida

`src/app/(protected)/layout.tsx` envuelve las páginas con `AuthGuard`. Este usa `onAuthStateChanged`, redirige usuarios nulos a `/login` y usuarios email/password no verificados a `/verify-email`. Considera verificado al proveedor Google por `providerData`.

**Brecha:** es un componente `"use client"`; no existe middleware ni sesión de servidor que impida servir la ruta. Tampoco carga roles o permisos.

## Cliente HTTP

`src/lib/api/client.ts` obtiene `auth.currentUser.getIdToken()`, o espera un evento de auth, y agrega `Authorization: Bearer`. Ante `401 EMAIL_NOT_VERIFIED` redirige a verificación; para otros `401` cierra sesión y redirige a login.

**Brechas comprobadas:**

- No hay tratamiento central de `403`, `404`, `409` o `429`.
- No existe timeout/abort, correlation ID, idempotency key ni política de reintentos.
- Se fuerza `Content-Type: application/json` incluso si más adelante se envía `FormData`.
- El token vive en memoria administrada por Firebase, lo que reduce persistencia explícita, pero cualquier XSS ejecutado en el origen puede invocar APIs como el usuario.

## Rutas y actores

Solo hay superficie Teacher: dashboard, assessments, bank, students y reports. `AppShell` presenta todos los ítems sin consultar capacidades. No existen rutas Operator ni Student implementadas.

## Configuración HTTP

`next.config.ts` usa salida standalone, un rewrite `/api/:path*` hacia `API_BASE_URL` y únicamente agrega `Cross-Origin-Opener-Policy: same-origin-allow-popups`. No se observan CSP, HSTS, nosniff, Referrer-Policy ni Permissions-Policy.

## Pruebas actuales

Existen Jest tests de páginas de autenticación, `AuthGuard` y `apiClient`. Son una base útil, pero no constituyen una suite de seguridad por actor ni prueban manipulación de rutas/capacidades.
