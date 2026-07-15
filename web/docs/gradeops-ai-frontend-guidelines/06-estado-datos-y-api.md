# 06 — Estado, datos y API

## 1. Tipos de estado

Separar estado por naturaleza:

- UI local: modal abierto, tab activo, hover, campo expandido.
- Form state: valores, errores, touched, submitting.
- Server state: datos que vienen del backend.
- Auth state: usuario Firebase y token.
- Derived state: conteos, filtros, labels, view models.

No meter todo en un único objeto global.

## 2. Estado local

Usar `useState` para estado local simple:

- `isOpen`
- `selectedId`
- `activeTab`
- `isSubmitting`

Usar `useReducer` cuando:

- Hay muchas transiciones.
- Los estados son mutuamente excluyentes.
- Hay flujos multi-step.
- El estado depende de eventos con payload.

## 3. Server state

Los datos del backend deben entrar por funciones en `lib/api`.

Ejemplo:

```ts
export async function getAssessments(): Promise<AssessmentSummaryDto[]> {
  const response = await apiClient("/api/v1/assessments");
  if (!response.ok) {
    throw await toApiError(response);
  }
  return response.json();
}
```

No hacer `fetch` directo desde componentes si existe patrón `apiClient`.

## 4. API client

`apiClient` debe centralizar:

- Base URL.
- Token Firebase.
- Headers comunes.
- Manejo de 401.
- Redirección por sesión expirada.
- Redirección por email no verificado.
- Normalización básica de errores si se agrega.

No duplicar lógica de Authorization en cada feature.

## 5. DTOs

Los DTOs que reflejan backend deben vivir en `src/types` o en la feature si son específicos.

Reglas:

- Nombres alineados con backend cuando representen contrato real.
- No inventar campos sin documentarlos.
- Mantener enums sincronizados.
- Separar DTO de view model.

```ts
export interface AssessmentSummaryDto {
  id: string;
  title: string;
  status: AssessmentStatus;
}
```

## 6. View models

Usar view models para adaptar datos a UI:

```ts
interface AssessmentRowViewModel {
  id: string;
  title: string;
  statusLabel: string;
  statusTone: BadgeTone;
  pendingText: string;
}
```

El DTO representa contrato. El view model representa pantalla.

## 7. Page Data Loader / Screen Data Facade

Cuando una pantalla necesita varias llamadas API para renderizarse, no llamar `getA()`, `getB()`, `getC()` directamente desde la `Page` ni desde el TSX del componente.

Usar una fachada por pantalla:

```text
Page -> usePageHook -> loadPageData() -> lib/api/*
```

Nombre recomendado:

- `loadDashboardPage`
- `loadAssessmentBuilderPage`
- `loadStudentDetailPage`
- `loadReportsPage`

También se puede llamar `Screen Data Facade` cuando la función representa una pantalla compleja y no solo carga datos.

Responsabilidades del loader:

- Orquestar llamadas API.
- Ejecutar llamadas en paralelo con `Promise.all` cuando sean independientes.
- Ejecutar llamadas secuenciales cuando una depende de otra.
- Normalizar errores técnicos a errores de UI o de dominio frontend.
- Combinar DTOs en un view model de pantalla.
- Aplicar formateos y labels estables de presentación.
- Resolver defaults seguros.
- Ocultar detalles de endpoints a la página.

La página o hook de página debe recibir datos ya cocinados:

```ts
interface AssessmentBuilderPageViewModel {
  assessment: AssessmentHeaderViewModel;
  rubric: RubricEditorViewModel;
  availableQuestions: QuestionOptionViewModel[];
  permissions: AssessmentBuilderPermissionsViewModel;
}
```

Ejemplo:

```ts
export async function loadAssessmentBuilderPage(
  assessmentId: string
): Promise<AssessmentBuilderPageViewModel> {
  const [assessment, rubric, questions, permissions] = await Promise.all([
    getAssessment(assessmentId),
    getAssessmentRubric(assessmentId),
    getQuestionBankOptions(),
    getAssessmentPermissions(assessmentId),
  ]);

  return toAssessmentBuilderPageViewModel({
    assessment,
    rubric,
    questions,
    permissions,
  });
}
```

La pantalla consume:

```tsx
const page = useAssessmentBuilderPage(assessmentId);
```

No consume:

```tsx
const assessment = await getAssessment(id);
const rubric = await getAssessmentRubric(id);
const questions = await getQuestionBankOptions();
const permissions = await getAssessmentPermissions(id);
```

Regla práctica:

- Una pantalla simple puede llamar un servicio API desde su hook.
- Una pantalla con dos o más fuentes remotas debe tener un loader/fachada de pantalla.
- Una pantalla con permisos, métricas, listas y datos del usuario debe retornar un view model de pantalla, no DTOs crudos.

El loader no reemplaza a `lib/api`. `lib/api` sigue siendo el adapter técnico por endpoint o recurso. El loader es la capa de composición para una pantalla concreta.

Ubicación recomendada:

```text
features/assessments/loaders/loadAssessmentBuilderPage.ts
features/assessments/mappers/toAssessmentBuilderPageViewModel.ts
features/assessments/hooks/useAssessmentBuilderPage.ts
```

Para pantallas pequeñas sin carpeta de feature:

```text
components/dashboard/useDashboardPage.ts
components/dashboard/loadDashboardPage.ts
```

No crear un loader genérico global para todas las pantallas. La fachada debe hablar el lenguaje de la pantalla que alimenta.

## 8. Loading state

No usar booleanos sueltos cuando hay más de dos estados.

Preferir:

```ts
type LoadState = "idle" | "loading" | "ready" | "empty" | "error";
```

O:

```ts
type RemoteData<T> =
  | { status: "loading" }
  | { status: "ready"; data: T }
  | { status: "empty" }
  | { status: "error"; error: UiError };
```

Esto evita combinaciones inválidas como `loading=true` y `error` presente.

## 9. Errores API

Normalizar errores en una forma útil:

```ts
interface ApiError {
  status: number;
  code?: string;
  message: string;
  traceId?: string;
}
```

La UI puede traducir:

- `401`: sesión expirada o email no verificado.
- `403`: sin permisos.
- `404`: recurso no encontrado.
- `409`: conflicto de estado.
- `422`: validación de negocio.
- `500`: error inesperado.

## 10. Mutaciones

Toda mutación debe definir:

- Estado submitting.
- Deshabilitado de acción.
- Manejo de éxito.
- Manejo de error.
- Actualización/refetch de datos.
- Mensaje al usuario si corresponde.
- Confirmación si es destructiva.

No disparar mutaciones sin feedback visual.

## 11. Optimistic UI

Usar optimistic UI solo cuando:

- La operación es reversible.
- El conflicto es improbable.
- Hay rollback claro.
- El usuario se beneficia de respuesta inmediata.

Evitar optimistic UI para:

- Aprobaciones académicas.
- Consumo de créditos.
- Publicación a estudiantes.
- Cambios irreversibles.

## 12. Datos derivados

Calcular derivados en render o `useMemo` si son costosos:

```ts
const inReviewCount = assessments.filter((a) => a.status === "GRADING").length;
```

No guardar derivados en `useState` salvo que haya razón real. Duplicar estado produce inconsistencias.

## 13. Sincronización con backend

La UI debe respetar el backend como fuente de verdad:

- Refetch después de mutaciones críticas.
- Mostrar estado devuelto por API, no asumir transición.
- Manejar conflictos 409.
- Mostrar cambios realizados por otros usuarios si aplica.

## 14. Contratos temporales

Si el backend aún no existe:

- Crear mock explícito en `features/<feature>/mocks`.
- Documentar shape esperado.
- Mantener nombres compatibles con el contrato planeado.
- Remover o aislar mocks al conectar API real.

No dejar datos hardcoded mezclados con datos reales sin señal clara.
