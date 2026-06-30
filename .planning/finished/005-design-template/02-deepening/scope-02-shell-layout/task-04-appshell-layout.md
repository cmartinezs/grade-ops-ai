# ⚛️ TASK 04 — AppShell y layout (protected) actualizado

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01, task-02, task-03
> [← scope file](../scope-02-shell-layout.md)

---

## Objective

Implementar el componente `AppShell` (sidebar 256px + topbar 64px + área de contenido) usando los primitivos DS y el ShellContext, e integrar el shell en el layout `(protected)` de Next.js, manteniendo `AuthGuard` como primera capa de protección.

---

## Technical Design

- **Approach:** `AppShell` es un Client Component con layout `display:flex; height:100vh`. El sidebar es fijo (256px), el main area es `flex:1`. El nav usa `usePathname()` para determinar el item activo. El topbar lee `useShell()` para obtener título/subtítulo/acciones. `SignOutButton.tsx` existente se adapta para usar `IconButton` DS en lugar de su botón actual. El layout `(protected)/layout.tsx` se actualiza para envolver `AuthGuard` → `ShellProvider` → `AppShell`.

- **Affected files / components:**
  - `web/src/components/shell/AppShell.tsx` (nuevo)
  - `web/src/app/(protected)/layout.tsx` (modificar — integrar shell)
  - `web/src/components/auth/SignOutButton.tsx` (modificar — adaptar estilo a DS)

- **Interfaces / contracts:**
  ```tsx
  // AppShell no recibe props — usa ShellContext internamente
  export default function AppShell({ children }: { children: React.ReactNode })
  ```
  El nav determina la ruta activa comparando `pathname.startsWith(item.href)`.

- **Design notes — Sidebar:**
  - Ancho: 256px (`var(--sidebar-width)`)
  - bg: `var(--surface-card)`, border-right: `1px solid var(--border-subtle)`
  - Padding: `18px 14px`
  - **Brand:** `<AppLogo size="md" />` con padding bottom 18px
  - **Nav items:** los 5 del DS (Panel, Evaluaciones, Banco, Estudiantes, Reportes)
    - Activo: bg `var(--surface-brand-soft)`, color `var(--brand-hover)`, font-weight 600
    - Inactivo: bg transparent, color `var(--text-muted)`, hover: bg `var(--surface-sunken)`
    - Transición: `background 120ms, color 120ms`
  - **Widget créditos IA:** bg `var(--gold-50)`, border `1px solid var(--gold-200)`, ícono sparkles `var(--gold-600)`, texto "742 correcciones restantes este mes." (hardcoded)
  - **User row:** `<Avatar>` + nombre + escuela + `<SignOutButton>` DS

- **Design notes — Topbar:**
  - Height: 64px (`var(--topbar-height)`)
  - bg: `var(--surface-card)`, border-bottom: `1px solid var(--border-subtle)`
  - Padding: `0 28px`
  - Izquierda: `<h1>` con título (font-display, font-600, text-xl) + `<p>` subtítulo
  - Derecha: `<IconButton label="Notificaciones">` + slot `actions` del ShellContext

- **Design notes — Content area:**
  - `flex:1; min-width:0; overflow:auto; padding:28px`

- **Design notes — User row:**
  - Datos del usuario: `auth.currentUser?.displayName` para nombre y avatar
  - Escuela: "GradeOps AI" como placeholder (no viene de la API aún)
  - `SignOutButton` debe adaptarse: mismo `auth.signOut()` + redirect, pero usando `<IconButton label="Cerrar sesión">` DS

---

## Implementation Steps

1. Definir el array `NAV_ITEMS`:
   ```tsx
   const NAV_ITEMS = [
     { id: "dashboard", label: "Panel", href: "/dashboard", icon: "layout-dashboard" },
     { id: "assessments", label: "Evaluaciones", href: "/assessments", icon: "file-pen-line" },
     { id: "bank", label: "Banco de preguntas", href: "/bank", icon: "library" },
     { id: "students", label: "Estudiantes", href: "/students", icon: "users" },
     { id: "reports", label: "Reportes", href: "/reports", icon: "bar-chart-3" },
   ] as const;
   ```

2. Crear `web/src/components/shell/AppShell.tsx`:
   - Importar `usePathname` de `next/navigation` para el nav activo.
   - Importar `useShell` para el topbar.
   - Importar `useAuth` o `auth.currentUser` para el user row (usar `onAuthStateChanged` o `auth.currentUser` directamente).
   - Renderizar sidebar + main (topbar + content).

3. Adaptar `web/src/components/auth/SignOutButton.tsx`:
   - Mantener la lógica de `auth.signOut()` + `router.push("/login")`.
   - Cambiar el botón visual a `<IconButton label="Cerrar sesión" size="sm"><LucideIcon name="log-out" size={16} /></IconButton>`.

4. Actualizar `web/src/app/(protected)/layout.tsx`:
   ```tsx
   import AuthGuard from "@/components/auth/AuthGuard";
   import { ShellProvider } from "@/components/shell/ShellContext";
   import AppShell from "@/components/shell/AppShell";

   export default function ProtectedLayout({ children }: { children: React.ReactNode }) {
     return (
       <AuthGuard>
         <ShellProvider>
           <AppShell>{children}</AppShell>
         </ShellProvider>
       </AuthGuard>
     );
   }
   ```

5. Verificar navegación: clicar cada item del nav cambia la ruta y el item activo se resalta.

6. Verificar que el AuthGuard sigue funcionando (redirige a `/login` si no autenticado).

---

## Unit Tests

| # | Caso | Resultado esperado | Archivo |
|---|------|-------------------|---------|
| 1 | Usuario autenticado accede a `/dashboard` | Shell visible, nav "Panel" activo | Test manual / e2e |
| 2 | Nav item "Evaluaciones" clickeado | Ruta cambia a `/assessments`, item activo resaltado | Test manual |
| 3 | Usuario no autenticado accede a `/dashboard` | Redirige a `/login` (AuthGuard) | `AuthGuard.test.tsx` existente |
| 4 | Topbar muestra título de la página actual | "Panel de control" cuando está en dashboard | Integración con scope-04 |
| 5 | Widget créditos IA aparece con colores gold | bg gold-50, borde gold-200 | Inspección visual |
| 6 | SignOutButton llama `auth.signOut()` y redirige | Sesión cerrada, redirect a `/login` | `SignOutButton.test.tsx` existente |

---

## Done Criteria

- [x] `AppShell.tsx` existe y renderiza sidebar (256px) + topbar (64px) + content area
- [x] Nav item activo tiene bg `var(--surface-brand-soft)` y color `var(--brand-hover)`
- [x] Nav items inactivos tienen hover `var(--surface-sunken)`
- [x] Topbar muestra título y subtítulo de la página actual vía ShellContext
- [x] Widget créditos IA aparece con colores gold
- [x] User row muestra nombre del usuario autenticado + avatar DS
- [x] `SignOutButton` usa `IconButton` DS visualmente
- [x] `(protected)/layout.tsx` envuelve `AuthGuard → ShellProvider → AppShell`
- [x] `npm run dev` sin errores; la autenticación sigue funcionando
- [x] No scope creep: la tarea satisface `[CHECK-ATOMICITY]`

---

> [← scope file](../scope-02-shell-layout.md)
