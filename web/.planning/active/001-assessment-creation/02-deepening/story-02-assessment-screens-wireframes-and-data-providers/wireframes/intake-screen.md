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

## Boceto visual (estado idle)

```
┌────────────────────────────────────────────────────────────────────┐
│  AppShell (protegido)                                              │
│ ┌──────────────────────────────────────────────────────────────┐   │
│ │  Nueva evaluación                                             │   │
│ │  Describe el objetivo de aprendizaje                          │   │
│ └──────────────────────────────────────────────────────────────┘   │
│                                                                      │
│  ┌─ BriefFormSection ───────────────────────────────────────────┐  │
│  │                                                                │  │
│  │  Objetivo de aprendizaje *                                    │  │
│  │  ┌──────────────────────────────────────────────────────────┐ │  │
│  │  │ (textarea, varias líneas)                                 │ │  │
│  │  │                                                            │ │  │
│  │  └──────────────────────────────────────────────────────────┘ │  │
│  │                                                                │  │
│  │  Tema *                          Nivel *                     │  │
│  │  ┌───────────────────────┐       ┌───────────────────────┐   │  │
│  │  │                       │       │                       │   │  │
│  │  └───────────────────────┘       └───────────────────────┘   │  │
│  │                                                                │  │
│  │  Duración *                      Lenguaje *                  │  │
│  │  ┌───────────────────────┐       ┌───────────────────────┐   │  │
│  │  │                       │       │                       │   │  │
│  │  └───────────────────────┘       └───────────────────────┘   │  │
│  │                                                                │  │
│  │                                    ┌─────────────────────────┐│  │
│  │                                    │ Generar borrador con IA ││  │ ← deshabilitado
│  │                                    └─────────────────────────┘│  │    (form inválido)
│  └────────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────────┘
```

Un solo formulario de una columna con dos campos pareados por fila (Tema/Nivel, Duración/Lenguaje) — sin cards anidadas ni decoración, coherente con la sección "Densidad y microcopy" más abajo.

---

## Estados

| Estado | Disparador | Tratamiento visual |
|--------|-----------|---------------------|
| **idle** | Carga inicial de la pantalla | Formulario vacío, botón primario deshabilitado (formulario aún no válido) |
| **validando** | El docente intenta enviar con campos vacíos | Error inline por campo (mensaje bajo cada input), foco en el primer campo inválido, botón primario permanece deshabilitado |
| **enviando** | Formulario válido, submit en curso | Botón primario deshabilitado + spinner/label "Generando…", campos deshabilitados para evitar doble envío |
| **éxito** | `submitAssessmentBrief` resuelve con `assessmentId` | Navegación inmediata a `/assessments/{assessmentId}/draft` — no hay estado de éxito visible en esta pantalla, la confirmación es la propia navegación |
| **error de validación de negocio (422)** | `POST /assessments` rechaza el brief por Bean Validation (`@NotBlank`) pese a pasar la validación cliente — cuerpo `List<FieldErrorResponse>`, no `ApiErrorResponse` | Errores inline por campo tomados de la respuesta, formulario vuelve a editable |
| **error de agente rechazado (422)** | `POST /assessments/{id}/draft` — `agents/` rechazó el comando (`AgentClientException.Reason.AGENT_REJECTED`) | Mensaje traducido de negocio ("No pudimos generar un borrador con esta información. Ajusta el objetivo de aprendizaje e intenta de nuevo."), formulario vuelve a editable |
| **error de agente caído (502/503)** | `POST /assessments/{id}/draft` — `agents/` no responde (`UNREACHABLE` → 503) o falla al procesar (`AGENT_ERROR` → 502) | Mensaje de reintento distinto al de validación ("El servicio de generación no está disponible. Intenta de nuevo en unos minutos."), formulario vuelve a editable — la brief ya quedó persistida por el paso 1, no se pierde |
| **error inesperado (500)** | Fallo de infraestructura o excepción no prevista en cualquiera de los 2 pasos | Mensaje genérico de reintento ("No pudimos generar el borrador. Intenta de nuevo."), formulario vuelve a estado editable |

**Nota sobre el paso 2 (`generateDraft`):** un 404 (`ApiErrorResponse{error: "NOT_FOUND"}`) es posible si el `assessmentId` no existe o no pertenece al docente autenticado (`OwnershipVerifier` deliberadamente disfraza el rechazo de ownership como 404, no 403, para no revelar la existencia del recurso a otro docente) — pero no debería ocurrir en el flujo normal de este formulario, ya que el `assessmentId` viene directo de la respuesta del paso 1 (`createAssessmentBrief`) en la misma sesión. Se documenta por completitud, no porque el flujo feliz deba manejarlo como un estado de UI distinto — si ocurre, cae en el mismo tratamiento que "error inesperado (500)".

Verificado directamente contra `api/src/main/java/cl/gradeops/ai/api/shared/infrastructure/adapter/in/web/GlobalExceptionHandler.java`, `.../assessment/application/usecase/{CreateAssessmentBriefHandler,GenerateAssessmentDraftHandler,DraftGenerationCoordinator}.java`, `.../shared/application/security/OwnershipVerifier.java`, y `.../agentclient/AgentClientException.java` (2026-07-15) — no asumido de la taxonomía genérica de `06-estado-datos-y-api.md` §9. Esa taxonomía genérica (401/403/404/409/422/500) no cubre 502/503 ni el hecho de que 422 tiene **dos formas de cuerpo distintas** (`List<FieldErrorResponse>` para validación Bean Validation vs. `ApiErrorResponse{error, message}` para todo lo demás) — ambas relevantes para `task-06` (conectar API real).

No aplica §7 (estados de aprobación humana) en esta pantalla — todavía no existe ningún output de IA que aprobar; eso ocurre en la pantalla de Draft Builder (`task-07`/`task-08`/`task-09`).

---

## Bocetos de variantes de estado

Solo se muestra lo que cambia respecto al boceto idle de arriba — layout y campos restantes se mantienen iguales.

**validando** (campo vacío al intentar enviar):

```
│  Objetivo de aprendizaje *                                    │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │                                                            │ │
│  └──────────────────────────────────────────────────────────┘ │
│  ⚠ Este campo es requerido                                    │
```

**enviando** (formulario válido, submit en curso — todos los campos deshabilitados):

```
│                                    ┌─────────────────────────┐│
│                                    │  ⏳ Generando…          ││ ← deshabilitado
│                                    └─────────────────────────┘│
```

**error de validación de negocio (422 — `List<FieldErrorResponse>`)** — igual a "validando" pero el error viene de la respuesta del servidor, no del cliente:

```
│  Tema *                                                        │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ Física                                                     │ │
│  └──────────────────────────────────────────────────────────┘ │
│  ⚠ Debe tener al menos 3 caracteres                            │
```

**error de agente rechazado / agente caído / inesperado** — banner sobre el formulario, no inline por campo (el error no es de un campo específico):

```
│  ┌─ ⚠ No pudimos generar un borrador con esta información.  ─┐│
│  │   Ajusta el objetivo de aprendizaje e intenta de nuevo.    ││
│  └─────────────────────────────────────────────────────────────┘│
│                                                                  │
│  Objetivo de aprendizaje *                                      │
│  ┌──────────────────────────────────────────────────────────┐  │
```

(El texto del banner cambia según el caso — ver tabla de Estados arriba para el mensaje exacto de cada uno — pero la posición y forma del banner es la misma para los tres.)

---

## Densidad y microcopy

- Un solo formulario, sin cards ni decoración adicional — coherente con `02-ux-wireframes-y-maquetas.md` §7: "la densidad organizada suele ser mejor que una composición decorativa" para un producto operativo.
- Microcopy en español claro, orientado a acción, sin tecnicismos del backend (`08-accesibilidad-responsive-y-usabilidad.md`/`15-backend-frontend-contracts.md` §4): "Generar borrador con IA", no "Submit"; mensajes de error traducidos, no códigos HTTP crudos.

---

## Resultado esperado del diseño (checklist §10)

- **Componentes necesarios:** ver `task-03` (jerarquía de componentes).
- **Hooks necesarios:** `useIntakeAssessmentPage` (ver `task-03`).
- **DTOs requeridos:** `CreateAssessmentBriefRequestDto`/`ResponseDto` (ver `task-01`, construidos en `task-05`).
- **Estados de UI:** los 8 listados arriba (idle, validando, enviando, éxito, y 4 variantes de error).
- **Validaciones:** RHF + Zod, 5 campos requeridos, sin validación HTML nativa.
- **Eventos de usuario:** editar campo, enviar formulario.
- **Tests mínimos:** validación bloquea envío con campos vacíos; botón deshabilitado durante envío (ver `task-04`).
- **Riesgos de UX:** ninguno crítico — pantalla de un solo paso, sin flujo multi-step.
