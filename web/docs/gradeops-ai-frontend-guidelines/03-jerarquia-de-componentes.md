# 03 — Jerarquía de componentes

## 1. Jerarquía estándar

GradeOps AI usa esta jerarquía conceptual:

```text
Layout
  Page
    Section
      SubSection
        Component
          MiniComponent
            MicroComponent
```

La jerarquía no obliga a crear carpetas para cada nivel. Sirve para decidir responsabilidades.

## 2. Layout

Un `Layout` define estructura persistente:

- Providers.
- App shell.
- Navegación.
- Zona de contenido.
- Header persistente.
- Guardias de autenticación.

Ejemplos:

- `src/app/layout.tsx`
- `src/app/(protected)/layout.tsx`
- `src/components/shell/AppShell.tsx`

Un layout no debe saber detalles internos de una feature específica.

## 3. Page

Una `Page` es la entrada de ruta:

```tsx
export default function DashboardPage() {
  useShellConfig({
    title: "Panel de control",
    subtitle: "Resumen de tus cursos y evaluaciones",
    actions: <Button variant="primary">Nueva evaluación</Button>,
  });

  return <DashboardView />;
}
```

Debe:

- Componer la pantalla.
- Configurar layout/shell.
- Invocar hook principal de la feature si aplica.
- Elegir estados top-level.

No debe convertirse en el lugar donde vive toda la feature.

## 4. Section

Una `Section` representa un bloque mayor de negocio:

- Resumen de métricas.
- Lista de evaluaciones.
- Editor de rúbrica.
- Panel de feedback.
- Historial de actividad.

Convención:

```tsx
function AssessmentSummarySection(props: AssessmentSummarySectionProps) {
  return <section aria-labelledby="assessment-summary-title">...</section>;
}
```

Cada section debe tener propósito claro y, si corresponde, encabezado semántico.

## 5. SubSection

Una `SubSection` agrupa una parte interna:

- Filtros de una lista.
- Toolbar.
- Tabla.
- Empty state local.
- Grupo de campos.
- Resumen lateral.

No todas las sections necesitan subsections. Se usan cuando mejoran lectura o testabilidad.

## 6. Component

Un `Component` es una unidad reusable con contrato explícito:

```tsx
interface AssessmentRowProps {
  assessment: AssessmentSummaryDto;
  onOpen?: (id: string) => void;
}

export default function AssessmentRow({ assessment, onOpen }: AssessmentRowProps) {
  const model = useAssessmentRow(assessment, onOpen);
  return (...);
}
```

Debe:

- Recibir props claras.
- Renderizar UI.
- Delegar lógica no trivial a su hook.
- Ser testeable por comportamiento.

## 7. MiniComponent

Un `MiniComponent` es una pieza pequeña, normalmente privada al archivo o feature:

- `StatusLabel`
- `MetricValue`
- `FilterChip`
- `InlineError`
- `UserAvatarText`

Puede vivir en el mismo archivo si es corto y no se reutiliza fuera.

Extraerlo a archivo propio cuando:

- Tiene props relevantes.
- Tiene tests propios.
- Se usa en más de un componente.
- Su markup TSX distrae del componente principal.

## 8. MicroComponent

Un `MicroComponent` es una unidad mínima y repetible:

- `Badge`
- `IconButton`
- `Input`
- `Avatar`
- `StatusDot`
- `Spinner`
- `SkeletonLine`

Los microcomponentes compartidos deben vivir en el Design System cuando su uso sea transversal.

## 9. Regla de componente con hook propio

Todo componente no trivial debe tener hook propio.

No trivial significa que cumple una o más condiciones:

- Tiene estado local.
- Tiene efectos.
- Tiene handlers con lógica.
- Deriva datos para mostrar.
- Coordina loading/error.
- Maneja permisos visuales.
- Normaliza DTOs.
- Tiene más de una acción del usuario.

Ejemplo:

```text
AssessmentListSection.tsx
useAssessmentListSection.ts
```

Para componentes triviales de presentación, no hace falta hook.

## 10. Anti-pattern: componente dios

Evitar componentes que:

- Fetching + formulario + modal + tabla + estilos + mapeos.
- Más de 250 líneas sin separación.
- Props booleanas múltiples para controlar variantes ambiguas.
- Importan API, Firebase y muchos componentes DS al mismo tiempo.
- Tienen handlers largos dentro del TSX.

Cuando aparezca este patrón, dividir por jerarquía y hooks.

## 11. Props

Reglas:

- Props con nombres de negocio.
- Evitar `data`, `item`, `value` cuando el dominio es claro.
- Evitar booleanos que se contradicen.
- Preferir unions para variantes.
- Callbacks nombrados por acción: `onApprove`, `onRetry`, `onOpen`.

Ejemplo:

```ts
type ReviewStatus = "pending" | "approved" | "rejected";

interface FeedbackReviewCardProps {
  feedbackId: string;
  status: ReviewStatus;
  generatedText: string;
  onApprove: (feedbackId: string) => void;
  onEdit: (feedbackId: string) => void;
}
```

## 12. Composición

Preferir composición explícita:

```tsx
<Card>
  <Card.Header>
    <Card.Title>Evaluaciones recientes</Card.Title>
  </Card.Header>
  <AssessmentList assessments={assessments} />
  <Card.Footer>{summaryText}</Card.Footer>
</Card>
```

Evitar componentes con demasiadas props de slots:

```tsx
<MegaPanel title="..." footer="..." showIcon showActions compact elevated />
```

La composición hace que la jerarquía visual sea visible en el código.
