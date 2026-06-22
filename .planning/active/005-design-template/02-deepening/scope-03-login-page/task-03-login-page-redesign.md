# ⚛️ TASK 03 — Rediseño de login/page.tsx con layout DS

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01, task-02
> [← scope file](../scope-03-login-page.md)

---

## Objective

Refactorizar `web/src/app/login/page.tsx` aplicando el layout de dos columnas del design system: columna izquierda con formulario (logo, título, botón Google, form email+password con componentes DS) y columna derecha con panel de marca Sprout (degradado verde, cita de docente, estadísticas), manteniendo **toda la lógica de autenticación Firebase existente** sin cambios.

---

## Technical Design

- **Approach:** Reescribir el JSX del componente `LoginForm` con el nuevo layout. La lógica de estado (`email`, `password`, `error`, `loading`, `handleSubmit`, redirect por `emailVerified`) se mantiene idéntica. Se reemplazan los elementos HTML genéricos (inputs de Tailwind) por componentes DS (`Input`, `Field`, `Button`, `GoogleButton`). Los mensajes de error se traducen al español. El layout es `display:flex; height:100vh` con dos columnas (`flex: 1 1 52%` izquierda, `flex: 1 1 48%` derecha). En mobile (< 768px): panel derecho oculto con media query.

- **Affected files / components:**
  - `web/src/app/login/page.tsx` (refactorizar — solo JSX y estilos, lógica intacta)

- **Interfaces / contracts:** Sin cambios en la API externa — el componente `LoginPage` y `LoginForm` mantienen su estructura de Suspense. Los redirects (`/dashboard`, `/verify-email`) no cambian.

- **Design notes:**
  - Columna izquierda: bg `var(--surface-page)`, centrado vertical, padding `40px 24px`
    - Logo: `<AppLogo size="md" />` + margin-bottom 22px
    - Título: "Bienvenida de vuelta" (font-display, font-700, text-3xl, text-strong)
    - Subtítulo: "Entra para crear y corregir evaluaciones con IA." (text-muted)
    - GoogleButton: componente DS (task-02)
    - Divisor: "o con tu correo" con líneas laterales (`--border-subtle`)
    - Form: `Field` + `Input` para email y contraseña con íconos `mail` y `lock`
    - Button submit: `<Button variant="primary" block loading={loading}>Iniciar sesión</Button>`
    - Link olvidé contraseña: `<a>` con color `var(--text-link)`
    - Pie: "Portal docente · ¿Eres estudiante? Usa el enlace de tu correo."
  - Columna derecha: `linear-gradient(150deg, var(--sprout-600), var(--sprout-800))`, color `#fff`
    - Ícono `graduation-cap` 40px, blanco
    - Cita del docente (font-display, font-600, text-2xl)
    - Autor + cargo
    - Estadísticas (3 métricas en fila: +8.400 docentes / 1,2M correcciones / 9 min ahorro/prueba)
  - Alerta "session expired": bg `var(--warning-50)`, border `1px solid var(--warning-200)`, color `var(--warning-700)`, radius `var(--radius-md)`, padding `12px 14px`
  - Errores de auth → texto en español:
    ```tsx
    const ERROR_MESSAGES: Record<string, string> = {
      "auth/wrong-password": "Correo o contraseña incorrectos.",
      "auth/invalid-credential": "Correo o contraseña incorrectos.",
      "auth/user-not-found": "No encontramos una cuenta con ese correo.",
      "auth/invalid-email": "Ingresa una dirección de correo válida.",
    };
    ```
  - Responsive: `@media (max-width: 768px) { .login-right { display: none; } }` — clase CSS o inline style con `window.innerWidth` check (preferir CSS media query).

---

## Implementation Steps

1. Mantener toda la lógica de estado y autenticación existente en `LoginForm`.
2. Reescribir el JSX de return con el layout de dos columnas descrito arriba.
3. Reemplazar inputs nativos Tailwind por `<Field>` + `<Input>` DS.
4. Reemplazar botón submit por `<Button variant="primary" block loading={loading}>`.
5. Reemplazar `GoogleSignInButton` por `<GoogleButton onSuccess={...}>` DS.
6. Traducir `ERROR_MESSAGES` al español.
7. Agregar media query para ocultar panel derecho en mobile (agregar clase o estilo inline con CSS-in-JS).
8. Verificar que el flujo de login completo funciona: Google SSO → `/dashboard`; email+password → `/dashboard` o `/verify-email`; credenciales incorrectas → mensaje de error en español.

---

## Unit Tests

| # | Caso | Resultado esperado | Archivo |
|---|------|-------------------|---------|
| 1 | Login exitoso con email + password verificado | Redirect a `/dashboard` | `login/page.test.tsx` (existente, adaptar) |
| 2 | Login exitoso, email no verificado | Redirect a `/verify-email` | `login/page.test.tsx` |
| 3 | Credenciales incorrectas | Mensaje de error en español visible | `login/page.test.tsx` |
| 4 | Session expired (`?reason=expired`) | Banner de advertencia DS (gold/warning) visible | `login/page.test.tsx` |
| 5 | Layout visible: panel derecho con gradiente verde | Panel visible en desktop | Inspección visual |
| 6 | Layout mobile: panel derecho oculto | Solo columna izquierda en viewport < 768px | Inspección visual responsive |

---

## Done Criteria

- [x] Layout de dos columnas funciona: formulario (52%) + panel Sprout (48%)
- [x] Logo GradeOps AI aparece con logo-mark DS y colores Sprout
- [x] Título "Bienvenida de vuelta" usa font-display (Bricolage Grotesque)
- [x] Input email e Input password usan componente DS con foco verde
- [x] Button "Iniciar sesión" usa variante primary verde
- [x] Panel derecho tiene gradiente `linear-gradient(150deg, var(--sprout-600), var(--sprout-800))`
- [x] Mensajes de error están en español
- [x] La lógica de autenticación funciona exactamente igual que antes
- [x] El test de login existente sigue pasando (adaptar si el JSX cambió)
- [x] Panel derecho se oculta en viewport < 768px
- [x] No scope creep: la tarea satisface `[CHECK-ATOMICITY]`

---

> [← scope file](../scope-03-login-page.md)
