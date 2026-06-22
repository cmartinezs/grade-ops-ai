# ⚛️ TASK 01 — Rutas maqueta y PlaceholderPage

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** scope-02/task-03 (ShellContext), scope-02/task-04 (AppShell)
> [← scope file](../scope-05-placeholder-routes.md)

---

## Objective

Crear el componente `PlaceholderPage` (empty state genérico para rutas no implementadas) y las 4 rutas maqueta del portal docente (`/assessments`, `/bank`, `/students`, `/reports`) dentro del shell DS, junto con la redirección de la raíz `/` al dashboard.

---

## Technical Design

- **Approach:** Un único componente `PlaceholderPage` reutilizable que acepta configuración por props. Cada ruta maqueta es un archivo `page.tsx` mínimo que llama `useShellConfig` y renderiza `<PlaceholderPage>`. La raíz `/` se actualiza para redirigir a `/dashboard` si el usuario está autenticado (o simplemente redirigir siempre — AuthGuard maneja el caso no autenticado). Todos los componentes son Client Components (`"use client"`) porque usan hooks.

- **Affected files / components:**
  - `web/src/components/shell/PlaceholderPage.tsx` (nuevo)
  - `web/src/app/(protected)/assessments/page.tsx` (nuevo)
  - `web/src/app/(protected)/bank/page.tsx` (nuevo)
  - `web/src/app/(protected)/students/page.tsx` (nuevo)
  - `web/src/app/(protected)/reports/page.tsx` (nuevo)
  - `web/src/app/page.tsx` (modificar — agregar redirect)

- **Interfaces / contracts:**
  ```tsx
  interface PlaceholderPageProps {
    icon: React.ReactNode;        // SVG ícono (LucideIcon)
    title: string;
    description: string;
    badge?: string;               // e.g. "Próximamente"
    cta?: {
      label: string;
      onClick?: () => void;
    };
  }
  ```

- **Design notes (PlaceholderPage):**
  - Wrapper externo: `display:flex; align-items:center; justify-content:center; height:100%` (para centrar en el content area del shell)
  - Contenedor inner: bg `var(--surface-card)`, border `1px dashed var(--border-subtle)`, radius `var(--radius-lg)`, padding `64px 32px`, max-width `480px`, width `100%`, text-align `center`
  - Ícono: 48px, color `var(--text-subtle)`, margin-bottom 16px
  - Badge "Próximamente": `<Badge tone="neutral">Próximamente</Badge>` (si se pasa `badge`)
  - Título: font-display, font-600, text-xl, text-strong, margin-top 12px
  - Descripción: text-muted, text-md, line-height 1.6, margin-top 8px, max-width 360px, margin horizontal auto
  - CTA: `<Button variant="primary" style={{ marginTop: 24 }}>` (si se pasa `cta`)

- **Design notes (rutas):**
  
  `/assessments`:
  - Shell config: título "Evaluaciones", subtítulo "Crea y gestiona evaluaciones"
  - PlaceholderPage: icon `file-pen-line` (19px), title "Tus evaluaciones", description "Crea evaluaciones abiertas (rúbrica), cerradas y mixtas con apoyo de IA. Gestiona entregas físicas y digitales.", badge "Próximamente"

  `/bank`:
  - Shell config: título "Banco de preguntas", subtítulo "Tus preguntas personales y el banco global"
  - PlaceholderPage: icon `library`, title "Banco de preguntas", description "Reutiliza preguntas propias o accede al banco global de la comunidad docente. Genera preguntas con IA en segundos.", badge "Próximamente"

  `/students`:
  - Shell config: título "Estudiantes", subtítulo "Gestiona tus cursos y alumnos"
  - PlaceholderPage: icon `users`, title "Tus estudiantes", description "Visualiza el rendimiento de cada alumno, identifica a quienes están en riesgo y envía retroalimentación personalizada.", badge "Próximamente"

  `/reports`:
  - Shell config: título "Reportes", subtítulo "Análisis y métricas de tus cursos"
  - PlaceholderPage: icon `bar-chart-3`, title "Panel de reportes", description "Analiza el rendimiento de tus cursos a lo largo del tiempo. Exporta reportes para compartir con tu institución.", badge "Próximamente"

- **Design notes (redirect raíz):**
  `web/src/app/page.tsx` actualmente tiene `redirect("/login")` o similar. Verificar el comportamiento actual:
  - Si redirige a `/login`: mantener — AuthGuard redirige al dashboard después del login.
  - Si no existe redirect: agregar `redirect("/dashboard")` (AuthGuard redirige a login si no autenticado).

---

## Implementation Steps

1. Crear `web/src/components/shell/PlaceholderPage.tsx`:
   - Importar `Badge`, `Button`, `LucideIcon` de DS.
   - Renderizar el empty state con las props.

2. Crear `web/src/app/(protected)/assessments/page.tsx`:
   ```tsx
   "use client";
   import { useShellConfig } from "@/components/shell/ShellContext";
   import PlaceholderPage from "@/components/shell/PlaceholderPage";
   import LucideIcon from "@/components/ds/LucideIcon";

   export default function AssessmentsPage() {
     useShellConfig({ title: "Evaluaciones", subtitle: "Crea y gestiona evaluaciones" });
     return (
       <PlaceholderPage
         icon={<LucideIcon name="file-pen-line" size={40} color="var(--text-subtle)" />}
         title="Tus evaluaciones"
         description="Crea evaluaciones abiertas (rúbrica), cerradas y mixtas con apoyo de IA."
         badge="Próximamente"
       />
     );
   }
   ```

3. Crear los 3 archivos restantes (`/bank`, `/students`, `/reports`) con la misma estructura, ajustando la config de cada ruta.

4. Verificar `web/src/app/page.tsx` y agregar/mantener redirect si es necesario.

5. Confirmar que navegar a cada ruta:
   - Muestra el PlaceholderPage centrado
   - El topbar tiene el título correcto
   - El nav item correspondiente está activo en el sidebar

---

## Unit Tests

| # | Caso | Resultado esperado | Archivo |
|---|------|-------------------|---------|
| 1 | Navegar a `/assessments` | PlaceholderPage visible, nav "Evaluaciones" activo, topbar "Evaluaciones" | Manual |
| 2 | Navegar a `/bank` | PlaceholderPage "Banco de preguntas" | Manual |
| 3 | Navegar a `/students` | PlaceholderPage "Tus estudiantes" | Manual |
| 4 | Navegar a `/reports` | PlaceholderPage "Panel de reportes" | Manual |
| 5 | Badge "Próximamente" aparece en todas las rutas | Badge neutral visible | Visual |
| 6 | Usuario no autenticado accede a `/assessments` | Redirige a `/login` (AuthGuard) | Manual |

---

## Done Criteria

- [x] `PlaceholderPage.tsx` existe y acepta `icon`, `title`, `description`, `badge`, `cta`
- [x] Las 4 rutas maqueta existen: `/assessments`, `/bank`, `/students`, `/reports`
- [x] Cada ruta muestra el PlaceholderPage centrado en el content area del shell
- [x] Cada ruta tiene el nav item correcto activo en el sidebar
- [x] Badge "Próximamente" (tone neutral) aparece en todas las rutas
- [x] Textos en español (Chile), con descripción orientativa sobre cada funcionalidad
- [x] La ruta raíz `/` tiene redirect correctamente configurado
- [x] `npm run dev` sin errores de rutas; las 4 rutas responden HTTP 200
- [x] No scope creep: la tarea satisface `[CHECK-ATOMICITY]`

---

> [← scope file](../scope-05-placeholder-routes.md)
