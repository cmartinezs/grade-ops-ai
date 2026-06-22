# ⚛️ TASK 03 — Refactor de DashboardPage con shell context y componentes DS

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01, task-02, scope-02/task-03 (ShellContext)
> [← scope file](../scope-04-dashboard-page.md)

---

## Objective

Refactorizar `web/src/app/(protected)/dashboard/page.tsx` para: configurar el topbar del shell (título, subtítulo, acción), mostrar los 4 StatCards maqueta, usar `AssessmentRow` para la lista de evaluaciones reales cargada del API, y usar `DashboardEmptyState` cuando no hay evaluaciones — todo con el DS. La lógica de carga de datos (`getAssessments()`) no cambia.

---

## Technical Design

- **Approach:** Agregar `useShellConfig` al inicio del componente para configurar el topbar. Reemplazar el JSX de la lista y los estados (loading, empty, populated) con los componentes DS. Los StatCards muestran valores maqueta ("—") excepto "Por corregir" que usa datos reales del API (count de evaluaciones con status `IN_REVIEW`).

- **Affected files / components:**
  - `web/src/app/(protected)/dashboard/page.tsx` (refactorizar)

- **Interfaces / contracts:** Sin cambios en el API — `getAssessments()` sigue siendo la única fuente de datos. `useShellConfig` viene de `ShellContext` (scope-02/task-03).

- **Design notes:**
  - **Shell config:**
    ```tsx
    useShellConfig({
      title: "Panel de control",
      subtitle: "Resumen de tus cursos y evaluaciones",
      actions: <Button variant="primary">+ Nueva evaluación</Button>,
    });
    ```
  - **Loading spinner DS:**
    ```tsx
    <div style={{ display:"flex", alignItems:"center", justifyContent:"center", height:"100%" }}>
      <div style={{
        width: 32, height: 32,
        border: "3px solid var(--surface-sunken)",
        borderTopColor: "var(--brand)",
        borderRadius: "50%",
        animation: "spin 0.7s linear infinite",
      }} />
    </div>
    ```
    > El keyframe `spin` fue agregado en globals.css en scope-02/task-02.
  - **StatCards (maqueta hardcoded):**
    ```tsx
    <StatCard label="Promedio general" value="—" />
    <StatCard label="Por corregir" value={assessments.filter(a => a.status === "IN_REVIEW").length} iconTone="gold" />
    <StatCard label="Entregas a tiempo" value="—" unit="%" iconTone="info" />
    <StatCard label="En riesgo" value="—" unit="alumnos" iconTone="danger" />
    ```
    > "—" en lugar de un número indica explícitamente que el dato no está disponible.
  - **Lista de evaluaciones real:**
    ```tsx
    <Card>
      <Card.Header>
        <Card.Title>Evaluaciones recientes</Card.Title>
      </Card.Header>
      <div>
        {assessments.map(a => <AssessmentRow key={a.id} assessment={a} />)}
      </div>
      <Card.Footer>
        <span style={{ fontSize:"var(--text-sm)", color:"var(--text-muted)" }}>
          {assessments.length} evaluación{assessments.length !== 1 ? "es" : ""}
        </span>
      </Card.Footer>
    </Card>
    ```
  - **Layout general:** `display:flex; flex-direction:column; gap:22px; max-width:1200px`

---

## Implementation Steps

1. Agregar `useShellConfig` import de `ShellContext`.
2. Llamar `useShellConfig(...)` dentro del componente (ver config arriba).
3. Reemplazar el spinner genérico por el spinner DS.
4. Reemplazar `<EmptyDashboard />` por `<DashboardEmptyState />`.
5. Reemplazar el JSX con evaluaciones por el layout con StatCards + Card + AssessmentRow.
6. Verificar que `getAssessments()` sigue funcionando y las evaluaciones aparecen en la lista.
7. Verificar que el topbar muestra "Panel de control" + subtítulo + botón "Nueva evaluación".

---

## Unit Tests

| # | Caso | Resultado esperado | Archivo |
|---|------|-------------------|---------|
| 1 | Dashboard carga con evaluaciones → muestra lista + StatCards | AssessmentRows visibles | Prueba manual |
| 2 | Dashboard sin evaluaciones → DashboardEmptyState | Empty state visible, no lista | Prueba manual |
| 3 | Loading → spinner DS (borde verde) | Spinner correcto | Prueba manual |
| 4 | Topbar muestra "Panel de control" + botón | Config de shell aplicada | Prueba manual |
| 5 | StatCard "Por corregir" muestra el conteo real | Número correcto basado en API | Prueba manual |

---

## Done Criteria

- [x] `DashboardPage` llama `useShellConfig` correctamente
- [x] Topbar muestra "Panel de control", subtítulo y botón "+ Nueva evaluación"
- [x] 4 StatCards visibles en grid de 4 columnas
- [x] La lista de evaluaciones usa `AssessmentRow` y muestra datos reales del API
- [x] `DashboardEmptyState` aparece cuando no hay evaluaciones
- [x] Spinner DS (borde verde `--brand`) durante la carga
- [x] La lógica de `getAssessments()` no cambió
- [x] `npm run dev` sin errores
- [x] No scope creep: la tarea satisface `[CHECK-ATOMICITY]`

---

> [← scope file](../scope-04-dashboard-page.md)
