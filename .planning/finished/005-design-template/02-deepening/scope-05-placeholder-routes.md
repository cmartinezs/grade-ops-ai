# 🔍 DEEPENING: Scope 05 — placeholder-routes

> **Status:** DONE
> **Depends on:** scope-02-shell-layout
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)

---

## Objective

Crear las rutas que aún no existen en el portal docente (`/assessments`, `/bank`, `/students`, `/reports`) como **páginas maqueta** dentro del shell DS. Cada página muestra un estado vacío branded y orientativo — la navegación en el sidebar funciona y el shell se ve completo, pero sin contenido real (placeholder explícito).

---

## Context

**Referencia visual:** `design/design-system/ui_kits/teacher/` (AssessmentsScreen, BankScreen, StudentsScreen, ReportsScreen)  
**Shell:** implementado en scope-02

Estas rutas no tienen backend implementado todavía. El objetivo es que el template quede navegable y visualmente correcto — el profesor puede ver el shell completo con las 5 secciones del nav activas, aunque solo Panel y Login funcionan con datos reales.

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [Rutas maqueta y PlaceholderPage](scope-05-placeholder-routes/task-01-placeholder-routes.md) | GENERATE-DOCUMENT | DONE | `PlaceholderPage.tsx` + 4 rutas `page.tsx` + `app/page.tsx` actualizado |

---

## Approach Details

### Task 1 — PlaceholderPage

Un componente reutilizable que muestra un empty state contextual:

```tsx
interface PlaceholderPageProps {
  icon: React.ReactNode;        // SVG Lucide inline
  title: string;                // Nombre de la sección
  description: string;          // Descripción orientativa
  badgeText?: string;           // e.g. "Próximamente"
  cta?: {
    label: string;
    onClick?: () => void;
  };
}

// Layout: centrado verticalmente en el área de contenido
// bg: var(--surface-card), border: 1px dashed var(--border-subtle)
// border-radius: var(--radius-lg), padding: 64px 32px, max-width: 480px, margin: auto
// ícono: 48px, color: var(--text-subtle)
// badge "Próximamente": Badge tone="neutral" (si aplica)
// título: font-display, font-600, text-xl, text-strong, margin-top: 16px
// descripción: text-muted, text-md, margin-top: 8px, line-height: 1.6
// cta (opcional): Button variant="primary" margin-top: 24px
```

### Task 2 — /assessments

```tsx
// useShellConfig({ title: "Evaluaciones", subtitle: "Crea y gestiona evaluaciones" })
// PlaceholderPage:
//   icon: file-pen-line (19px, color: var(--text-subtle))  
//   title: "Tus evaluaciones"
//   description: "Aquí verás todas tus evaluaciones: abiertas (rúbrica), cerradas y mixtas. Crea y gestiona evaluaciones con apoyo de IA."
//   badgeText: "Próximamente"
//   cta: { label: "+ Crear evaluación" }
```

### Task 3 — /bank

```tsx
// useShellConfig({ title: "Banco de preguntas", subtitle: "Tus preguntas personales y el banco global" })
// PlaceholderPage:
//   icon: library
//   title: "Banco de preguntas"
//   description: "Reutiliza preguntas propias o accede al banco global de la comunidad docente. Genera preguntas con IA en segundos."
//   badgeText: "Próximamente"
```

### Task 4 — /students

```tsx
// useShellConfig({ title: "Estudiantes", subtitle: "Gestiona tus cursos y alumnos" })
// PlaceholderPage:
//   icon: users
//   title: "Tus estudiantes"  
//   description: "Visualiza el rendimiento de cada alumno, identifica a quienes están en riesgo y envía retroalimentación personalizada."
//   badgeText: "Próximamente"
```

### Task 5 — /reports

```tsx
// useShellConfig({ title: "Reportes", subtitle: "Análisis y métricas de tus cursos" })
// PlaceholderPage:
//   icon: bar-chart-3
//   title: "Panel de reportes"
//   description: "Analiza el rendimiento de tus cursos y alumnos a lo largo del tiempo. Exporta reportes para compartir con tu institución."
//   badgeText: "Próximamente"
```

### Task 6 — Redirección raíz

`web/src/app/page.tsx` actualmente redirecciona a... verificar. Si no hay redirección explícita, agregar:

```tsx
import { redirect } from "next/navigation";
export default function HomePage() {
  redirect("/dashboard");
}
```

---

## Estructura de archivos resultante

```
web/src/app/(protected)/
├── layout.tsx                  ← Shell (scope-02)
├── dashboard/
│   └── page.tsx               ← REAL (scope-04)
├── assessments/
│   └── page.tsx               ← MAQUETA (scope-05)
├── bank/
│   └── page.tsx               ← MAQUETA (scope-05)
├── students/
│   └── page.tsx               ← MAQUETA (scope-05)
└── reports/
    └── page.tsx               ← MAQUETA (scope-05)
```

---

## Done Criteria

- [x] Las rutas `/assessments`, `/bank`, `/students`, `/reports` existen y son accesibles
- [x] Cada ruta muestra su empty state DS dentro del shell (sidebar + topbar correctos)
- [x] El nav item activo cambia al navegar a cada ruta
- [x] El topbar muestra el título correcto por ruta
- [x] `PlaceholderPage` está factorizado y es reutilizable
- [x] La ruta raíz `/` redirecciona correctamente al dashboard
- [x] Navegar entre todas las rutas no genera errores 404
- [x] `npm run dev` corre sin errores
- [x] TRACEABILITY.md actualizado

---

## Inconsistencies Found

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| — | *None* | — | — | — |

---

## Residuals

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| 1 | Contenido real de cada ruta (CRUD de evaluaciones, banco, etc.) | Plannings futuros por funcionalidad | PENDING |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)
