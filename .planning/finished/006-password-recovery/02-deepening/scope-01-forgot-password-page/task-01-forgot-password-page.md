# ⚛️ TASK 01 — Crear página `/forgot-password`

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← scope file](../scope-01-forgot-password-page.md)

---

## Objective

Crear `web/src/app/forgot-password/page.tsx` con layout de dos columnas (panel Sprout a la derecha, idéntico al de `/login` y `/register`), formulario de un campo (email) validado con RHF + Zod, llamada a `sendPasswordResetEmail` de Firebase Auth, y lógica de estados: confirmación neutral ante éxito o `user-not-found`, error de campo ante `auth/invalid-email`.

---

## Technical Design

- **Approach:** Componente `"use client"` con la misma estructura de dos columnas que `login/page.tsx`. El formulario reemplaza visualmente la pantalla de confirmación una vez enviado (`submitted` state). El mensaje de confirmación es idéntico tanto para éxito como para `auth/user-not-found`, evitando user enumeration. Sólo `auth/invalid-email` devuelve un error de campo inline (no confirmación).

- **Affected files:**
  - `web/src/app/forgot-password/page.tsx` (nuevo)

- **Schema Zod:**
  ```ts
  const forgotSchema = z.object({
    email: z.string().min(1, "Ingresa tu correo.").email("Ingresa una dirección de correo válida."),
  });
  ```

- **Estado del componente:**
  - `submitted: boolean` — cuando `true` reemplaza el formulario con el mensaje de confirmación
  - `serverError: string | null` — solo para errores que no sean campo ni confirmación
  - `isSubmitting` de RHF — loader del botón

- **Lógica de errores Firebase:**
  | Código Firebase | Comportamiento |
  |----------------|---------------|
  | *(sin error)* | `setSubmitted(true)` |
  | `auth/user-not-found` | `setSubmitted(true)` — mismo mensaje neutral |
  | `auth/invalid-email` | `setError("email", { message: "Ingresa una dirección de correo válida." })` — NO confirmación |
  | Cualquier otro | `setSubmitted(true)` — ante la duda, no revelar información |

- **Mensaje de confirmación:**
  > "Si existe una cuenta con ese correo, recibirás un enlace en los próximos minutos. Revisa también tu carpeta de spam."

- **Componentes DS usados:**
  - `FieldWithHelper` — campo email con tooltip de ayuda
  - `Input` — con icono `mail` y `error` prop
  - `Button variant="primary" block loading={isSubmitting}`
  - `AppLogo size="md"`
  - `LucideIcon` — `mail`, `graduation-cap`

- **Panel Sprout derecho:** idéntico al de `login/page.tsx` (mismo JSX, mismo `className="login-panel-right"`).

- **Navegación:** Link de vuelta a `/login` debajo del formulario/confirmación. No hay enlace hacia registro desde esta página.

---

## Implementation Steps

1. Crear `web/src/app/forgot-password/page.tsx` con:
   - Imports: `sendPasswordResetEmail` de `firebase/auth`, `auth` de `@/lib/firebase/client`, DS components, `AppLogo`, `useForm`, `zodResolver`, `z`
   - Schema Zod `forgotSchema`
   - Estados `submitted` y `serverError`
   - `useForm` con `zodResolver`
   - `onSubmit`: llama `sendPasswordResetEmail(auth, data.email)`, maneja errores según tabla
   - JSX: columna izquierda con logo + heading + formulario (o confirmación si `submitted`) + link a login; columna derecha con panel Sprout
2. Si `submitted`, renderizar panel de confirmación en lugar del formulario:
   ```tsx
   <div role="status">
     <p>Si existe una cuenta con ese correo, recibirás un enlace en los próximos minutos. Revisa también tu carpeta de spam.</p>
     <Button variant="outline" block onClick={() => router.push("/login")}>
       Volver a iniciar sesión
     </Button>
   </div>
   ```
3. Verificar `npm run build` sin errores TypeScript.

---

## Unit Tests

> Los tests se escriben en task-03. Esta tarea solo verifica comportamiento visual y build.

| # | Verificación | Cómo validar |
|---|-------------|-------------|
| 1 | Ruta `/forgot-password` renderiza sin errores | `npm run build` — ruta aparece en el listado |
| 2 | Layout dos columnas visible en dev | `npm run dev` → abrir `/forgot-password` |
| 3 | Panel Sprout oculto en móvil | Resize < 768px → panel derecho desaparece |
| 4 | `npm run build` sin errores TypeScript | Terminal |

---

## Done Criteria

- [ ] `web/src/app/forgot-password/page.tsx` existe y compila
- [ ] Layout dos columnas con panel Sprout idéntico al de `/login`
- [ ] Formulario usa `FieldWithHelper` + `Input name="email"` + `Button`
- [ ] Zod valida formato de email antes de llamar a Firebase
- [ ] Éxito y `auth/user-not-found` → `submitted = true`, muestra confirmación neutral
- [ ] `auth/invalid-email` de Firebase → error de campo inline, `submitted` permanece `false`
- [ ] Estado `isSubmitting` activa loader en el botón
- [ ] Panel de confirmación tiene `role="status"`
- [ ] Link "Volver a iniciar sesión" presente tanto en el formulario como en la confirmación
- [ ] `npm run build` sin errores TypeScript
- [ ] No scope creep: satisface `[CHECK-ATOMICITY]`

---

> [← scope file](../scope-01-forgot-password-page.md)
