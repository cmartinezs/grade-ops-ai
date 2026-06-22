# 🔍 DEEPENING: Scope 04 — dashboard-page

> **Status:** DONE
> **Depends on:** scope-02-shell-layout
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)

---

## Objective

Adaptar la página de dashboard (`/dashboard`) para que funcione dentro del shell DS y muestre los datos reales de evaluaciones con el look&feel del design system: stat cards, lista de evaluaciones branded, y en caso de lista vacía, un empty state DS. La lógica de carga de datos (API call a `getAssessments()`) se mantiene intacta.

---

## Context

**Referencia visual:** `design/design-system/ui_kits/teacher/DashboardScreen.jsx`  
**Archivo actual:** `web/src/app/(protected)/dashboard/page.tsx` (funcional, estilo TailwindCSS genérico)  
**Componentes existentes:** `AssessmentCard.tsx`, `EmptyDashboard.tsx`

El dashboard tiene actualmente:
- Loading spinner (TailwindCSS genérico)
- Empty state (`EmptyDashboard`)
- Lista de evaluaciones (`AssessmentCard`)
- Sin shell (sin sidebar/topbar)

El DS define para el dashboard:
- 4 stat cards en grid 4 columnas (Promedio general, Por corregir, Entregas a tiempo, En riesgo)
- Tabla/lista de evaluaciones recientes (nombre, tipo/badge, curso, conteo estudiantes, promedio, estado/badge)
- Panel derecho con cobertura por curso (ProgressBar) y estudiantes en riesgo

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [Componentes DS del dashboard: Badge, StatCard, Card](scope-04-dashboard-page/task-01-dashboard-ds-components.md) | GENERATE-DOCUMENT | DONE | `web/src/components/ds/Badge.tsx`, `StatCard.tsx`, `Card.tsx` |
| 2 | [AssessmentRow y DashboardEmptyState DS](scope-04-dashboard-page/task-02-assessment-row-empty-state.md) | GENERATE-DOCUMENT | DONE | `web/src/components/dashboard/AssessmentRow.tsx`, `DashboardEmptyState.tsx` |
| 3 | [Refactor de DashboardPage con shell context y componentes DS](scope-04-dashboard-page/task-03-dashboard-page-refactor.md) | GENERATE-DOCUMENT | DONE | `web/src/app/(protected)/dashboard/page.tsx` refactorizado |

---

## Approach Details

### Task 1 — Componentes DS adicionales

**`Badge.tsx`**
```tsx
type BadgeTone = "brand" | "gold" | "success" | "warning" | "danger" | "info" | "neutral";
interface BadgeProps {
  tone?: BadgeTone;
  dot?: boolean;
  children: React.ReactNode;
}

// Cada tone tiene colores del DS:
// brand: bg var(--sprout-50), color var(--sprout-700), border var(--sprout-200)
// gold: bg var(--gold-50), color var(--gold-700), border var(--gold-200)
// success: bg var(--success-50), color var(--success-700), border var(--success-200)
// warning: bg var(--warning-50), color var(--warning-700), border var(--warning-200)
// danger: bg var(--danger-50), color var(--danger-700), border var(--danger-200)
// info: bg var(--info-50), color var(--info-700), border var(--info-200)
// neutral: bg var(--slate-50), color var(--slate-700), border var(--slate-200)

// Estilo base:
// display: inline-flex, align-items: center, gap: 5px
// padding: 2px 8px, border-radius: 999px (píldora)
// font-size: var(--text-xs), font-weight: 500
// border: 1px solid
// dot: círculo 6px relleno con el color del tone
```

**`StatCard.tsx`**
```tsx
interface StatCardProps {
  label: string;
  value: string | number;
  unit?: string;
  delta?: string;       // e.g. "+0,3" o "-1"
  deltaDir?: "up" | "down";
  iconTone?: "default" | "gold" | "info" | "danger";
}

// Card blanca con borde, padding 20px
// label: text-xs, text-muted, uppercase, tracking
// value: font-display, font-700, text-3xl, text-strong
// unit: text-sm, text-muted, inline con value
// delta: text-sm, color verde (up) o rojo (down)
// Icono en top-right: bg circle con tone (brand=sprout-50, gold=gold-50, etc.)
// El ícono real se usa hardcoded por prop o default: trending-up
```

**`Card.tsx`**
```tsx
// Card simple con background: var(--surface-card)
// border: 1px solid var(--border-subtle)
// border-radius: var(--radius-lg) (14px)
// box-shadow: var(--shadow-sm)
// Card.Header: padding 16px 20px, border-bottom 1px var(--border-subtle)
//   display flex, justify-content space-between, align-items center
// Card.Title: font-display, font-600, text-lg, text-strong
// Card.Body: padding 16px 20px
// Card.Footer: padding 12px 20px, border-top 1px var(--border-subtle), bg var(--surface-sunken) (sutil)
```

### Task 2 — AssessmentRow

El componente `AssessmentRow` reemplaza `AssessmentCard` para el dashboard. Muestra cada evaluación como fila en una tabla/lista:

```tsx
interface AssessmentRowProps {
  assessment: AssessmentSummaryDto;
}

// Fila con hover: background var(--surface-sunken)
// Columna izquierda: nombre (text-strong, font-600) + fila meta (Badge tipo + curso + N estudiantes)
// Columna derecha: promedio (font-display, font-700, text-lg) + etiqueta "promedio" + Badge estado
// Ícono chevron-right en el extremo derecho (text-subtle)
// border-bottom: 1px solid var(--border-subtle)
// cursor: pointer (futuro: navegar al detalle)

// Mapeo de tipos de evaluación (del API) a tone de Badge:
// "CERRADA" → tone="brand", texto="Cerrada"
// "ABIERTA" → tone="gold", texto="Abierta"
// "MIXTA" → tone="info", texto="Mixta"

// Mapeo de estado a Badge:
// "DRAFT" → tone="neutral", texto="Borrador"
// "OPEN" → tone="info", dot, texto="En curso"
// "IN_REVIEW" → tone="warning", dot, texto="En revisión"
// "GRADED" → tone="success", dot, texto="Corregida"
```

> **Nota:** Los tipos exactos dependen del enum del backend. Si difieren, ajustar el mapeo.

### Task 3 — DashboardEmptyState DS

```tsx
// Reemplaza EmptyDashboard.tsx con el estilo DS
// Centrado en el contenido
// Ícono: file-pen-line (Lucide, 40px, color: var(--text-subtle))
// Título: "Aún no tienes evaluaciones" (font-display, font-600, text-xl, text-strong)
// Subtítulo: "Crea tu primera evaluación y empieza a corregir con IA." (text-muted)
// Botón: Button primary "+ Nueva evaluación"
// bg: var(--surface-card), border dashed: 1px solid var(--border-subtle), border-radius: var(--radius-lg)
// padding: 48px 24px, text-align: center
```

### Task 4 — DashboardPage actualizado

```tsx
"use client";

export default function DashboardPage() {
  // Mantener lógica actual: getAssessments(), setLoading(), etc.
  
  // Agregar shell context
  useShellConfig({
    title: "Panel de control",
    subtitle: "Resumen de tus cursos y evaluaciones",
    actions: <Button variant="primary">+ Nueva evaluación</Button>
  });

  if (loading) {
    // DS loading: Spinner centrado, usando --brand color
    return (
      <div style={{ display: "flex", alignItems: "center", justifyContent: "center", height: "100%" }}>
        <div style={{ /* spinner animado con border var(--brand) */ }} />
      </div>
    );
  }

  if (assessments.length === 0) {
    return <DashboardEmptyState />;
  }

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 22, maxWidth: "var(--content-max)" }}>
      
      {/* Stat cards — maqueta por ahora con valores hardcoded */}
      {/* Nota: los datos reales de stats (promedio, por corregir, etc.) no vienen del API actual */}
      {/* Se muestran como maqueta hasta que el API los soporte */}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: 16 }}>
        <StatCard label="Promedio general" value="—" />
        <StatCard label="Por corregir" value={assessments.filter(a => a.status === "IN_REVIEW").length} iconTone="gold" />
        <StatCard label="Entregas a tiempo" value="—" unit="%" iconTone="info" />
        <StatCard label="En riesgo" value="—" unit="alumnos" iconTone="danger" />
      </div>

      {/* Lista de evaluaciones reales */}
      <Card>
        <Card.Header>
          <Card.Title>Evaluaciones recientes</Card.Title>
        </Card.Header>
        <div>
          {assessments.map(assessment => (
            <AssessmentRow key={assessment.id} assessment={assessment} />
          ))}
        </div>
        <Card.Footer>
          <span style={{ fontSize: "var(--text-sm)", color: "var(--text-muted)" }}>
            {assessments.length} evaluación(es)
          </span>
        </Card.Footer>
      </Card>

    </div>
  );
}
```

> **Decisión de diseño:** Los 4 stat cards son maqueta en el template (datos no disponibles en el API actual). El valor real que sí viene del API es la lista de evaluaciones (`getAssessments()`). Los stats se alimentarán cuando haya endpoints correspondientes.

---

## Done Criteria

- [x] Dashboard carga dentro del shell (sidebar + topbar visibles)
- [x] El topbar muestra "Panel de control" + subtítulo + botón "+ Nueva evaluación"
- [x] Los 4 stat cards se muestran en grid de 4 columnas con estilo DS
- [x] La lista de evaluaciones muestra datos reales del API con `AssessmentRow` DS
- [x] Cada fila tiene el Badge de tipo correcto (Cerrada/Abierta/Mixta) y Badge de estado
- [x] El empty state DS se muestra cuando no hay evaluaciones
- [x] El spinner de carga usa colores DS (`--brand`)
- [x] `npm run dev` corre sin errores
- [x] TRACEABILITY.md actualizado

---

## Inconsistencies Found

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| 1 | Los stat cards (promedio, entregas a tiempo) no tienen endpoint en el API actual — se muestran como maqueta con "—" | `design/design-system/ui_kits/teacher/DashboardScreen.jsx` vs API actual | ACCEPTED | Maqueta explícita hasta que el API lo soporte |
| 2 | `AssessmentSummaryDto` puede no tener todos los campos necesarios (tipo, estado, promedio, conteo estudiantes) — verificar al implementar | `web/src/types/assessment.ts` vs DS UI kit | PENDING | Verificar tipos y ajustar mapeo |

---

## Residuals

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| 1 | Panel derecho del dashboard (cobertura por curso con ProgressBar, estudiantes en riesgo) | Planning futuro (cuando API tenga endpoints de estadísticas) | PENDING |
| 2 | Filtro de evaluaciones por tipo (tabs "Todas/Abiertas/Cerradas/Mixtas") | Planning futuro | PENDING |
| 3 | Acción "+ Nueva evaluación" — navegar al builder | Planning futuro | PENDING |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)
