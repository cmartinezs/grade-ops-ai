# 🔍 DEEPENING: Scope 02 — shell-layout

> **Status:** DONE
> **Depends on:** scope-01-ds-tokens-base
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)

---

## Objective

Implementar el **shell del portal docente** como layout Next.js para el grupo `(protected)`: sidebar de 256px con navegación, topbar de 64px con título de página + acciones, y área de contenido principal con scroll. Este shell es la estructura base en la que viven el dashboard y todas las rutas futuras del docente.

---

## Context

**Referencia visual principal:** `design/design-system/ui_kits/teacher/Shell.jsx`  
**Template de referencia:** `design/design-system/templates/teacher-portal/TeacherPortal.dc.html`

El shell tiene:
- **Sidebar (256px):** logo GradeOps AI, navegación (Panel, Evaluaciones, Banco de preguntas, Estudiantes, Reportes), widget de créditos IA (gold), fila de usuario (avatar + nombre + escuela + logout).
- **Topbar (64px):** título h1 + subtítulo de la página actual, botón notificaciones (IconButton), slot de acciones (CTA contextual).
- **Content area:** `flex:1`, overflow auto, `padding: 28px`.

Actualmente `(protected)/layout.tsx` solo envuelve con `<AuthGuard>`. Hay que añadir el shell **sin tocar AuthGuard** (que debe seguir siendo la primera capa).

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [Íconos Lucide como SVGs inline](scope-02-shell-layout/task-01-lucide-icons.md) | GENERATE-DOCUMENT | DONE | `web/src/components/ds/LucideIcon.tsx` |
| 2 | [Primitivos DS del shell: Button, Avatar, IconButton](scope-02-shell-layout/task-02-ds-primitives.md) | GENERATE-DOCUMENT | DONE | `web/src/components/ds/Button.tsx`, `Avatar.tsx`, `IconButton.tsx` |
| 3 | [ShellContext: contexto de título/subtítulo/acciones del topbar](scope-02-shell-layout/task-03-shell-context.md) | GENERATE-DOCUMENT | DONE | `web/src/components/shell/ShellContext.tsx` |
| 4 | [AppShell y layout (protected) actualizado](scope-02-shell-layout/task-04-appshell-layout.md) | GENERATE-DOCUMENT | DONE | `web/src/components/shell/AppShell.tsx` + `(protected)/layout.tsx` actualizado |

---

## Approach Details

### Estructura de archivos

```
web/src/
├── components/
│   ├── ds/
│   │   ├── Button.tsx       ← variantes: primary, ghost, outline
│   │   ├── Avatar.tsx       ← iniciales + color brand-soft
│   │   ├── IconButton.tsx   ← icon-only button con tooltip label
│   │   └── Badge.tsx        ← tones: brand, gold, success, warning, danger, info, neutral
│   └── shell/
│       ├── AppShell.tsx     ← sidebar + topbar + content slot
│       └── ShellContext.tsx ← context para título/subtítulo/acciones
```

### Task 1 — Componentes DS primitivos (sólo los necesarios para el shell)

**`Button.tsx`**
```tsx
type ButtonVariant = "primary" | "ghost" | "outline";
type ButtonSize = "sm" | "md";

// primary: bg var(--brand), color white, hover var(--brand-hover) + shadow-brand
// ghost: bg transparent, color var(--text-body), hover bg var(--surface-sunken)
// outline: border var(--border-default), bg transparent
// size sm: height 32px, text-sm; md: height 40px, text-md
// loading: spinner + disabled opacity
// Radios: var(--radius-md) (10px)
// Transition: background + box-shadow 120ms
```

**`Avatar.tsx`**
```tsx
// Iniciales del nombre (1 o 2 letras), bg: var(--surface-brand-soft), color: var(--brand)
// size sm: 32px; md: 40px
// font-display, font-weight 700
```

**`IconButton.tsx`**
```tsx
// Botón cuadrado icon-only (w=h=40px sm→32px)
// bg: transparent, hover: var(--surface-sunken)
// label es aria-label + tooltip nativo (title)
// border: none, border-radius: var(--radius-md)
```

### Task 2 — AppShell

```tsx
interface ShellProps {
  children: React.ReactNode;
  pageTitle: string;
  pageSubtitle?: string;
  pageActions?: React.ReactNode;
  activeNav: "dashboard" | "assessments" | "bank" | "students" | "reports";
}
```

El sidebar usa `useRouter` + `usePathname` para determinar la ruta activa. Cada nav item lleva su `href` y al hacer click navega con `router.push()`.

**Nav items:**
```
Panel         → /dashboard         icon: layout-dashboard (SVG Lucide inline)
Evaluaciones  → /assessments       icon: file-pen-line
Banco         → /bank              icon: library
Estudiantes   → /students          icon: users
Reportes      → /reports           icon: bar-chart-3
```

Los íconos Lucide se implementan como SVGs inline hardcoded (sin dependencia npm, sin CDN) — copiar solo los paths SVG de Lucide necesarios.

**Widget créditos IA:**
```
bg: var(--gold-50), border: 1px solid var(--gold-200), radius: var(--radius-md)
ícono: sparkles (SVG Lucide inline, stroke: var(--gold-600))
texto: "Créditos IA" + "742 correcciones restantes este mes."
```
> Este widget es maqueta en el template — el número es hardcoded.

**User row (bottom of sidebar):**
```
Avatar nombre del usuario (desde useAuth/Firebase)
Nombre + escuela (datos del usuario o fallback "GradeOps AI")
IconButton logout → SignOutButton existente
```
> Usar `auth.currentUser.displayName` para el avatar y nombre. La escuela es placeholder en el template.

### Task 3 — ShellContext

```tsx
// ShellContext permite que las páginas hijas setteen el título/subtítulo/acciones del topbar
// Evita prop drilling: la página llama useShellConfig({ title, subtitle, actions })
// El AppShell lee el context y renderiza el topbar correctamente

interface ShellConfig {
  title: string;
  subtitle?: string;
  actions?: React.ReactNode;
}
```

### Task 4 — layout.tsx actualizado

```tsx
// (protected)/layout.tsx
// Envuelve con AuthGuard (mantener) + ShellProvider + AppShell
// AuthGuard → ShellProvider → AppShell → {children}
```

> **Importante:** `activeNav` se deriva del `pathname` para no tener que pasarlo manualmente.

### Task 5 — Dashboard usa ShellContext

El dashboard agrega a su `useEffect` o componente la llamada:
```tsx
useShellConfig({
  title: "Panel de control",
  subtitle: "Resumen de tus cursos y evaluaciones",
  actions: <Button variant="primary">+ Nueva evaluación</Button>
});
```

---

## Íconos Lucide (SVG inline)

Se crea `web/src/components/ds/icons/` con archivos mínimos de ícono (o un único `LucideIcon.tsx` con los paths hardcoded):

```
layout-dashboard, file-pen-line, library, users, bar-chart-3,
bell, sparkles, log-out, chevron-right, trending-up, alert-triangle,
calendar-clock, clipboard-check
```

Cada ícono es el SVG de Lucide con `stroke="currentColor"`, `strokeWidth={2}`, `strokeLinecap="round"`, `strokeLinejoin="round"`, `fill="none"`, `viewBox="0 0 24 24"`.

---

## Done Criteria

- [x] `AppShell.tsx` renderiza sidebar (256px) + topbar (64px) + content area correctamente
- [x] El nav item activo tiene `background: var(--surface-brand-soft)`, `color: var(--brand-hover)`, `fontWeight: 600`
- [x] El nav item inactivo tiene hover `background: var(--surface-sunken)`
- [x] El widget de créditos IA aparece con colores gold
- [x] La fila de usuario muestra avatar + nombre del usuario autenticado + botón logout
- [x] El topbar muestra el título/subtítulo de la página actual
- [x] El `ShellContext` permite que las páginas hijas setteen su configuración
- [x] Navegando entre rutas del shell, el layout no parpadea (Next.js layout compartido)
- [x] `npm run dev` corre sin errores
- [x] TRACEABILITY.md actualizado

---

## Inconsistencies Found

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| 1 | El UI kit tiene `SignOutButton` como `<IconButton>` Lucide; el componente existente `SignOutButton.tsx` tiene su propia implementación con `auth.signOut()` — se mantiene la lógica, se adapta el estilo | `ui_kits/teacher/Shell.jsx` vs `web/src/components/auth/SignOutButton.tsx` | RESOLVED | Adaptar `SignOutButton.tsx` para usar `IconButton` DS |

---

## Residuals

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| — | *None* | — | — |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)
