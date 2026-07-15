# Wireframe: Intake screen

> Follows `docs/gradeops-ai-frontend-guidelines/02-ux-wireframes-y-maquetas.md` §3's textual wireframe format.
> Route: `src/app/(protected)/assessments/new/`

---

## Pantalla: Nueva evaluación (Intake)

**Usuario:** docente autenticado.

**Objetivo:** describir una meta de aprendizaje para iniciar la generación de un draft de evaluación con IA.

**Layout:**
- `AppShell` protegido (ya existe vía `(protected)/layout.tsx` — `AuthGuard` + `ShellProvider` + `AppShell`).
- Header: título "Nueva evaluación", subtítulo "Describe el objetivo de aprendizaje" (vía `useShellConfig`).
- Section única: `BriefFormSection` — formulario de brief.

**Campos del formulario** (todos requeridos, confirmados contra `CreateAssessmentBriefRequest` en `task-01`):

| Campo | Tipo | Validación |
|-------|------|------------|
| `learningGoal` | texto libre (textarea) | requerido, no vacío |
| `topic` | texto corto | requerido, no vacío |
| `level` | texto corto (o select, TBD en `task-03`) | requerido, no vacío |
| `duration` | texto corto | requerido, no vacío |
| `language` | texto corto | requerido, no vacío |

**Acción primaria:** "Generar borrador con IA" — deshabilitada mientras el formulario no sea válido o mientras se está enviando.

**Acciones secundarias:** ninguna en esta pantalla (no hay "guardar borrador sin generar" ni "cancelar" — cancelar es simplemente navegar fuera).

---

## Estados

| Estado | Disparador | Tratamiento visual |
|--------|-----------|---------------------|
| **idle** | Carga inicial de la pantalla | Formulario vacío, botón primario deshabilitado (formulario aún no válido) |
| **validando** | El docente intenta enviar con campos vacíos | Error inline por campo (mensaje bajo cada input), foco en el primer campo inválido, botón primario permanece deshabilitado |
| **enviando** | Formulario válido, submit en curso | Botón primario deshabilitado + spinner/label "Generando…", campos deshabilitados para evitar doble envío |
| **éxito** | `submitAssessmentBrief` resuelve con `assessmentId` | Navegación inmediata a `/assessments/{assessmentId}/draft` — no hay estado de éxito visible en esta pantalla, la confirmación es la propia navegación |
| **error de negocio (422)** | La API rechaza el brief por regla de negocio | Mensaje de error traducido (no crudo) sobre el formulario, formulario vuelve a estado editable, botón primario vuelve a habilitarse si el formulario sigue siendo válido |
| **error inesperado (500)** | Falla de red o error del servidor | Mensaje genérico de reintento ("No pudimos generar el borrador. Intenta de nuevo."), formulario vuelve a estado editable |

No aplica §7 (estados de aprobación humana) en esta pantalla — todavía no existe ningún output de IA que aprobar; eso ocurre en la pantalla de Draft Builder (`task-07`/`task-08`/`task-09`).

---

## Densidad y microcopy

- Un solo formulario, sin cards ni decoración adicional — coherente con `02-ux-wireframes-y-maquetas.md` §7: "la densidad organizada suele ser mejor que una composición decorativa" para un producto operativo.
- Microcopy en español claro, orientado a acción, sin tecnicismos del backend (`08-accesibilidad-responsive-y-usabilidad.md`/`15-backend-frontend-contracts.md` §4): "Generar borrador con IA", no "Submit"; mensajes de error traducidos, no códigos HTTP crudos.

---

## Resultado esperado del diseño (checklist §10)

- **Componentes necesarios:** ver `task-03` (jerarquía de componentes).
- **Hooks necesarios:** `useIntakeAssessmentPage` (ver `task-03`).
- **DTOs requeridos:** `CreateAssessmentBriefRequestDto`/`ResponseDto` (ver `task-01`, construidos en `task-05`).
- **Estados de UI:** los 6 listados arriba.
- **Validaciones:** RHF + Zod, 5 campos requeridos, sin validación HTML nativa.
- **Eventos de usuario:** editar campo, enviar formulario.
- **Tests mínimos:** validación bloquea envío con campos vacíos; botón deshabilitado durante envío (ver `task-04`).
- **Riesgos de UX:** ninguno crítico — pantalla de un solo paso, sin flujo multi-step.
