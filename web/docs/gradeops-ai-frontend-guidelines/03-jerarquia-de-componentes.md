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

## 8.1 Inputs base parametrizables vs. inputs especializados derivados

Un `Input` (u otro campo base como `Textarea`) es un `MicroComponent` cuando queda **parametrizable** vía atributos HTML nativos u opciones explícitas: `required`, `minLength`, `maxLength`, `pattern`. No hace falta un componente nuevo por cada formulario si el campo es genérico y solo necesita estas reglas — el Design System ya resuelve esto pasando props nativas al elemento (ver `src/components/ds/Input.tsx`, que extiende `React.InputHTMLAttributes` y reenvía `...props`).

Cuando una validación específica se repite en más de un formulario — un email, un teléfono, un código con formato fijo — no repetir esa regex/copy inline en cada pantalla. Extraer un `MiniComponent` derivado que envuelva el input base fijando su `type`/`pattern`/mensaje de error, siguiendo el mismo criterio de extracción del §7 (se usa en más de un lugar, tiene props relevantes, su validación merece test propio):

```tsx
// EmailInput.tsx — MiniComponent derivado de Input (MicroComponent base)
interface EmailInputProps extends Omit<InputProps, "type"> {}

export default function EmailInput(props: EmailInputProps) {
  return <Input type="email" inputMode="email" {...props} />;
}
```

Ejemplo real detectado en este repo (2026-07-15): `src/app/login/page.tsx` y `src/app/register/page.tsx` repiten el mismo par `type="email"` + `z.string().email(...)` de forma inline, sin componente compartido — candidato a extraer como `EmailInput` la próxima vez que se toque alguno de esos dos formularios, en vez de copiar la validación una tercera vez.

No crear el `MiniComponent` especializado por anticipado si un campo nuevo es genérico (solo `required`, sin formato fijo) — en ese caso el `Input`/`Textarea` base ya alcanza y agregar un wrapper sería una abstracción prematura.

> **Alcance de este §8.1:** decide si un *campo individual* necesita un `MiniComponent` derivado. La decisión de arquitectura más amplia — que todo formulario del proyecto reutilice primitivas únicas del Design System (`Form`, `Field`, `Input`, `Textarea`, `Select`, `Checkbox`, `DynamicForm`) en vez de recomponer campos por pantalla — es un `PDR` (`001-assessment-creation/pdr-001-design-system-form-primitives.md`), no parte de esta guía.

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
