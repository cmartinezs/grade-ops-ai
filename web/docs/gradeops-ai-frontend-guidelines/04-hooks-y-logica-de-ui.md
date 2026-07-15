# 04 — Hooks y lógica de UI

## 1. Regla base

Cada componente no trivial debe tener un hook propio con su lógica interna.

El hook debe concentrar:

- Estado local.
- Efectos.
- Handlers.
- Derivaciones.
- Normalización de props.
- Coordinación de loading/error.
- Reglas de presentación.

El componente debe concentrar:

- Estructura semántica.
- Composición visual.
- Uso del Design System.
- Binding de props y handlers.

## 2. Forma recomendada

```text
components/assessments/
  AssessmentListSection.tsx
  useAssessmentListSection.ts
```

```tsx
export function AssessmentListSection() {
  const view = useAssessmentListSection();

  if (view.isLoading) return <AssessmentListSkeleton />;
  if (view.error) return <AssessmentListError onRetry={view.retry} />;
  if (view.isEmpty) return <AssessmentListEmpty onCreate={view.createAssessment} />;

  return <AssessmentList assessments={view.assessments} onOpen={view.openAssessment} />;
}
```

El hook retorna un modelo de vista estable y nombrado según intención.

## 3. Qué debe vivir en un hook

Debe vivir en hook:

- `useState` y `useEffect`.
- Fetching de datos de la pantalla.
- Submit de formularios si no está encapsulado por React Hook Form.
- Transformaciones de DTO a view model.
- Filtros/sort/paginación.
- Cálculos derivados.
- Handlers con navegación.
- Manejo de errores.
- Estado de modales.
- Debounce/throttle.

## 4. Qué no debe vivir en un hook

No meter en hook:

- JSX.
- Componentes React.
- Estilos.
- Microcopy largo.
- Tokens visuales.
- Decisiones de layout.

El hook decide qué mostrar; el componente decide cómo se muestra.

## 5. View model

Cuando el DTO backend no calza con la UI, crear view model:

```ts
interface AssessmentRowViewModel {
  id: string;
  title: string;
  statusLabel: string;
  statusTone: "neutral" | "info" | "warning" | "success" | "danger";
  submissionsText: string;
  canOpenReport: boolean;
}
```

El view model evita llenar JSX con ternarios y formateos repetidos.

## 6. Hooks de data fetching

Para una pantalla simple:

```tsx
function useDashboardPage() {
  const [assessments, setAssessments] = useState<AssessmentSummaryDto[]>([]);
  const [status, setStatus] = useState<"loading" | "ready" | "error">("loading");

  useEffect(() => {
    let active = true;

    getAssessments()
      .then((items) => {
        if (!active) return;
        setAssessments(items);
        setStatus("ready");
      })
      .catch(() => {
        if (!active) return;
        setStatus("error");
      });

    return () => {
      active = false;
    };
  }, []);

  return { assessments, status };
}
```

Si el proyecto adopta una librería de server state como TanStack Query, esta regla cambia: el hook de feature debe envolver esa librería y exponer un contrato propio.

## 7. Hooks de componente vs hooks compartidos

Hook de componente:

- Vive junto al componente.
- Conoce el flujo específico.
- Retorna view model de esa UI.

Hook compartido:

- Vive en `src/hooks` o `features/<feature>/hooks`.
- Tiene contrato genérico.
- No conoce JSX ni microcopy específico.

Ejemplos compartidos:

- `useDebouncedValue`
- `useMediaQuery`
- `useDisclosure`
- `usePrevious`

No promover a compartido hasta tener uso real en más de un lugar.

## 8. Efectos

Un `useEffect` debe tener propósito claro:

- Suscripción.
- Fetch.
- Sincronización con API externa.
- Actualización de título/shell.
- Limpieza de recurso.

Evitar efectos para derivar estado que puede calcularse durante render:

```tsx
const inReviewCount = assessments.filter((a) => a.status === "GRADING").length;
```

No convertir todo en `useEffect + useState`.

## 9. Handlers

Handlers deben nombrarse por intención:

- `handleCreateAssessment`
- `handleApproveFeedback`
- `handleRetryLoad`
- `handleFilterChange`

Evitar handlers anónimos largos dentro del JSX. Si el handler tiene más de una línea relevante, moverlo al hook.

## 10. Errores

El hook debe normalizar errores a algo útil para la UI:

```ts
interface UiError {
  title: string;
  message: string;
  retryable: boolean;
}
```

No mostrar directamente `error.message` si viene de infraestructura, Firebase o backend sin normalización.

## 11. Cancelación y desmontaje

Todo fetch o async effect debe considerar desmontaje:

- `AbortController` si la función lo permite.
- Flag `active` para ignorar resultado tardío.
- Limpieza de timers.
- Limpieza de suscripciones Firebase.

No llamar `setState` después de desmontar.

## 12. Tests de hooks

No todos los hooks requieren tests directos. Testear directo cuando:

- Tiene lógica compleja.
- Tiene transiciones de estado.
- Normaliza errores.
- Implementa filtros/sort/paginación.
- Encapsula una interacción crítica.

Para hooks simples, basta testear el componente que los usa.
