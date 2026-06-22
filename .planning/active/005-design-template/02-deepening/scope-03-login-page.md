# 🔍 DEEPENING: Scope 03 — login-page

> **Status:** DONE
> **Depends on:** scope-01-ds-tokens-base
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)

---

## Objective

Rediseñar la página de login (`/login`) aplicando el look&feel del design system: layout dividido (izquierda: formulario; derecha: panel de marca Sprout), tipografía DS, colores de tokens, y los componentes DS de inputs y botones — manteniendo **toda la lógica de autenticación existente** (Firebase, Google SSO, error handling, redirect).

---

## Context

**Referencia visual:** `design/design-system/ui_kits/teacher/LoginScreen.jsx`  
**Archivo actual:** `web/src/app/login/page.tsx` (funcional, estilo genérico TailwindCSS)

El diseño del DS tiene:
- **Izquierda (52%):** Logo GradeOps AI, card con título "Bienvenida de vuelta", botón Google, divisor "o con tu correo", form email+password, link "¿Olvidaste tu contraseña?", pie "Portal docente".
- **Derecha (48%):** Panel verde degradado (`linear-gradient(150deg, var(--sprout-600), var(--sprout-800))`), cita de docente, ícono graduation-cap, estadísticas del producto.

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [Primitivos DS de formulario: Input y Field](scope-03-login-page/task-01-input-field.md) | GENERATE-DOCUMENT | DONE | `web/src/components/ds/Input.tsx`, `Field.tsx` |
| 2 | [Componente GoogleButton DS](scope-03-login-page/task-02-google-button.md) | GENERATE-DOCUMENT | DONE | `web/src/components/ds/GoogleButton.tsx` |
| 3 | [Redisenio de login/page.tsx con layout DS](scope-03-login-page/task-03-login-page-redesign.md) | GENERATE-DOCUMENT | DONE | `web/src/app/login/page.tsx` refactorizado |

---

## Approach Details

### Task 1 — Input y Field DS

**`Input.tsx`**
```tsx
// bg: var(--surface-card)
// border: 1px solid var(--border-default); hover: var(--border-strong); focus: var(--border-brand)
// border-radius: var(--radius-md)
// padding: 9px 12px (con icon: padding-left: 38px)
// font-family: var(--font-sans), font-size: var(--text-md) (15px)
// color: var(--text-body); placeholder: var(--text-subtle)
// focus: box-shadow var(--ring) (anillo verde 3px translúcido)
// icon: posicionado absoluto a la izquierda, color: var(--text-subtle)
// error state: border-color var(--danger-500)
// Props: id, type, placeholder, value, onChange, icon?, error?
```

**`Field.tsx`**
```tsx
// Wrapper con label + Input + mensaje de error
// label: font-size var(--text-sm), font-weight 500, color: var(--text-body), margin-bottom: 6px
// error message: font-size var(--text-sm), color: var(--danger-600), margin-top: 4px
```

### Task 2 — GoogleButton DS

El botón Google mantiene el `onSuccess` callback del `GoogleSignInButton.tsx` existente pero adopta el estilo del DS:

```tsx
// height: 46px, width: 100%
// bg: var(--surface-card), border: 1px solid var(--border-default)
// border-radius: var(--radius-md)
// hover: bg var(--surface-sunken)
// font-weight: 600, font-size: var(--text-md), color: var(--text-body)
// gap: 10px entre SVG de Google y texto
// loading: spinner + "Conectando…" + disabled
// Texto: "Continuar con Google"
```

> **Mantener la lógica de Firebase de `GoogleSignInButton.tsx`** — solo cambiar el estilo del botón. Puede ser un wrapper o un refactor visual.

### Task 3 — login/page.tsx layout DS

**Estructura del layout:**
```tsx
<main style={{ display: "flex", height: "100vh", fontFamily: "var(--font-sans)", background: "var(--surface-page)" }}>
  
  {/* LEFT — Formulario */}
  <div style={{ flex: "1 1 52%", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", padding: "40px 24px" }}>
    
    {/* Logo */}
    <div style={{ display: "flex", alignItems: "center", gap: 11, marginBottom: 22 }}>
      <img src="/brand/logo-mark.svg" alt="" style={{ width: 40, height: 40 }} />
      <span style={{ fontFamily: "var(--font-display)", fontWeight: 700, fontSize: 23, color: "var(--text-strong)", letterSpacing: "-0.02em" }}>
        GradeOps<span style={{ color: "var(--brand)" }}>AI</span>
      </span>
    </div>

    {/* Form card */}
    <div style={{ width: "100%", maxWidth: 380 }}>
      <h1 style={{ fontFamily: "var(--font-display)", fontWeight: 700, fontSize: "var(--text-3xl)", color: "var(--text-strong)", margin: "0 0 6px", letterSpacing: "-0.02em" }}>
        Bienvenida de vuelta
      </h1>
      <p style={{ margin: "0 0 24px", color: "var(--text-muted)", fontSize: "var(--text-md)" }}>
        Entra para crear y corregir evaluaciones con IA.
      </p>

      {/* Google button */}
      <GoogleButton onSuccess={...} loading={loading} />

      {/* Divider */}
      <div style={{ display: "flex", alignItems: "center", color: "var(--text-subtle)", fontSize: "var(--text-xs)", margin: "20px 0" }}>
        <span style={{ flex: 1, height: 1, background: "var(--border-subtle)" }} />
        <span style={{ padding: "0 12px" }}>o con tu correo</span>
        <span style={{ flex: 1, height: 1, background: "var(--border-subtle)" }} />
      </div>

      {/* Form */}
      <form onSubmit={handleSubmit}>
        <Field label="Correo institucional" htmlFor="email">
          <Input id="email" type="email" placeholder="correo@escuela.cl" icon={<MailIcon />} value={email} onChange={...} error={emailError} />
        </Field>
        <Field label="Contraseña" htmlFor="password" style={{ marginTop: 14 }}>
          <Input id="password" type="password" placeholder="••••••••" icon={<LockIcon />} value={password} onChange={...} error={passwordError} />
        </Field>
        
        {/* Error general */}
        {error && <p role="alert" style={{ fontSize: "var(--text-sm)", color: "var(--danger-600)", margin: "8px 0" }}>{error}</p>}
        
        <Button block loading={loading} style={{ marginTop: 16 }}>Iniciar sesión</Button>
        <a href="#" style={{ display: "block", textAlign: "center", fontSize: "var(--text-sm)", color: "var(--text-link)", marginTop: 10 }}>
          ¿Olvidaste tu contraseña?
        </a>
      </form>
    </div>

    <p style={{ marginTop: 28, fontSize: "var(--text-xs)", color: "var(--text-subtle)", textAlign: "center" }}>
      Portal docente · ¿Eres estudiante? Usa el enlace de tu correo.
    </p>
  </div>

  {/* RIGHT — Panel de marca */}
  <div style={{ flex: "1 1 48%", background: "linear-gradient(150deg, var(--sprout-600), var(--sprout-800))", display: "flex", alignItems: "center", justifyContent: "center", padding: 40 }}>
    <div style={{ maxWidth: 420, color: "#fff" }}>
      {/* Graduation cap icon (Lucide SVG inline) */}
      <GraduationCapIcon size={40} color="rgba(255,255,255,0.9)" />
      <p style={{ fontFamily: "var(--font-display)", fontWeight: 600, fontSize: "var(--text-2xl)", lineHeight: 1.3, margin: "22px 0 20px", letterSpacing: "-0.01em" }}>
        "Corrijo una prueba de 32 alumnos en lo que antes me tomaba una tarde entera."
      </p>
      <div style={{ fontSize: "var(--text-md)", lineHeight: 1.5 }}>
        <div style={{ fontWeight: 700 }}>Rodrigo Salinas</div>
        <div style={{ opacity: 0.8 }}>Profesor de Historia · Colegio del Valle</div>
      </div>
      <div style={{ display: "flex", gap: 28, marginTop: 40, paddingTop: 24, borderTop: "1px solid rgba(255,255,255,0.2)" }}>
        <div><div style={{ fontFamily: "var(--font-display)", fontWeight: 700, fontSize: "var(--text-2xl)" }}>+8.400</div><div style={{ fontSize: "var(--text-xs)", opacity: 0.8, marginTop: 2 }}>docentes</div></div>
        <div><div style={{ fontFamily: "var(--font-display)", fontWeight: 700, fontSize: "var(--text-2xl)" }}>1,2M</div><div style={{ fontSize: "var(--text-xs)", opacity: 0.8, marginTop: 2 }}>correcciones</div></div>
        <div><div style={{ fontFamily: "var(--font-display)", fontWeight: 700, fontSize: "var(--text-2xl)" }}>9 min</div><div style={{ fontSize: "var(--text-xs)", opacity: 0.8, marginTop: 2 }}>ahorro/prueba</div></div>
      </div>
    </div>
  </div>
</main>
```

### Errores y alertas — estilo DS

```tsx
// Session expired notice → mantener pero con estilo DS
// bg: var(--warning-50), border: 1px solid var(--warning-200), color: var(--warning-700)
// border-radius: var(--radius-md), padding: 12px 14px, font-size: var(--text-sm)
```

### Textos adaptados

El formulario está en **español (Chile)** en la UI del DS. Sin embargo, la lógica de errores puede quedar en español también para ser consistente con el idioma del producto. Los mensajes de error del `ERROR_MESSAGES` map se traducen al español.

---

## Done Criteria

- [x] Layout de dos columnas: formulario izquierda + panel verde derecha
- [x] Logo GradeOps AI con logo-mark SVG del DS y colores `--text-strong`/`--brand`
- [x] Input email e Input password usan el componente DS (`Field` + `Input`) con foco verde (anillo `--ring`)
- [x] Botón "Iniciar sesión" usa el `Button` DS en variante primary (verde `--brand`)
- [x] Botón Google usa el estilo DS con borde sutil
- [x] El panel derecho tiene el degradado `linear-gradient(150deg, var(--sprout-600), var(--sprout-800))`
- [x] Todos los errores de auth se muestran en español
- [x] La lógica de autenticación Firebase y redirección funciona igual que antes
- [x] Responsive: en pantallas < 768px, el panel derecho se oculta (solo columna de formulario)
- [x] TRACEABILITY.md actualizado

---

## Inconsistencies Found

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| 1 | Login actual muestra "Sign in to your account" en inglés y mensajes de error en inglés — el DS espera español (Chile) | `login/page.tsx` vs `design/design-system/readme.md` (idioma) | RESOLVED | Traducir al español en el refactor |
| 2 | `AppLogo.tsx` actual tiene gradiente indigo/blue — el DS usa Sprout verde para el logo-mark | `brand/AppLogo.tsx` vs `design/design-system/assets/logo-mark.svg` | RESOLVED | Reemplazar en scope-01 con logo-mark del DS |

---

## Residuals

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| 1 | Link "¿Olvidaste tu contraseña?" — funcionalidad Firebase password reset | Planning futuro | PENDING |
| 2 | Link "Crear cuenta" — ruta `/register` no está en el scope de este planning | Planning futuro | PENDING |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)
