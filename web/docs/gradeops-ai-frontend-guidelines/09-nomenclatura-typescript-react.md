# 09 — Nomenclatura TypeScript y React

## 1. Componentes

Componentes React en PascalCase:

- `AssessmentRow`
- `DashboardEmptyState`
- `GoogleSignInButton`
- `AppShell`

El archivo debe llamarse igual que el componente principal:

```text
AssessmentRow.tsx
DashboardEmptyState.tsx
```

## 2. Hooks

Hooks en camelCase con prefijo `use`:

- `useDashboardPage`
- `useAssessmentList`
- `useShellConfig`
- `useSignInForm`

Hook propio de componente:

```text
AssessmentRow.tsx
useAssessmentRow.ts
```

## 3. Props

Interfaces de props:

```ts
interface AssessmentRowProps {
  assessment: AssessmentSummaryDto;
  onOpen: (assessmentId: string) => void;
}
```

No usar `Props` genérico exportado desde muchos archivos. El nombre debe incluir el componente.

## 4. Tipos DTO

DTOs backend:

- Sufijo `Dto` cuando reflejan contrato API.
- Nombres alineados con backend.
- En `src/types` o feature.

Ejemplos:

- `AssessmentSummaryDto`
- `CreateAssessmentRequestDto`
- `AssessmentStatus`

## 5. View models

Tipos adaptados a UI:

- Sufijo `ViewModel`.
- No se envían al backend.

Ejemplos:

- `AssessmentRowViewModel`
- `DashboardMetricViewModel`
- `FeedbackReviewViewModel`

## 6. Estados y enums

Usar unions cuando son estados frontend:

```ts
type LoadState = "loading" | "ready" | "empty" | "error";
```

Usar enum/union alineado con backend cuando representa contrato:

```ts
export type AssessmentStatus = "DRAFT" | "OPEN" | "GRADING" | "CLOSED";
```

## 7. Callbacks

Nombrar callbacks por evento de negocio:

- `onCreateAssessment`
- `onApproveFeedback`
- `onRetry`
- `onClose`
- `onSelectStudent`

Evitar:

- `onClick` en props de alto nivel.
- `callback`
- `handler`
- `setData`

## 8. Handlers internos

Dentro del componente/hook:

- `handleSubmit`
- `handleRetry`
- `handleOpenReport`
- `handleStatusFilterChange`

## 9. Archivos de test

Tests junto al componente o ruta:

```text
AssessmentRow.tsx
__tests__/AssessmentRow.test.tsx
```

O en carpeta de ruta:

```text
src/app/login/__tests__/SignInPage.test.tsx
```

Mantener nombres orientados a comportamiento.

## 10. Constantes

Constantes de módulo en UPPER_SNAKE_CASE:

```ts
const FIREBASE_ERRORS: Record<string, string> = { ... };
const NAV_ITEMS = [ ... ] as const;
```

Constantes locales derivadas pueden usar camelCase.

## 11. Booleans

Booleanos deben leerse como pregunta:

- `isLoading`
- `isSubmitting`
- `hasError`
- `canApprove`
- `shouldShowEmptyState`

Evitar:

- `loadingFlag`
- `disabledState`
- `dataReady` si puede ser estado union.

## 12. CSS classes

Si se usan clases:

- Prefijo contextual cuando sean globales: `login-panel-right`, `ds-input`.
- Evitar nombres genéricos globales como `.container`, `.button`, `.card`.

Preferir scope por componente o tokens DS.
