# Wireframe: Draft Builder screen

> Follows `docs/gradeops-ai-frontend-guidelines/02-ux-wireframes-y-maquetas.md` §3's textual wireframe format.
> Route: `src/app/(protected)/assessments/[id]/draft/` (target of `task-06`'s post-intake redirect).
> Covers US-011 (draft generation/view) + US-012 (regeneration) + version history — one screen, per `story-01`'s own Done Criteria that regeneration and version history happen "from the draft view."

---

## Pantalla: Draft Builder

**Usuario:** docente autenticado, dueño de la evaluación.

**Objetivo:** revisar el draft generado por IA, editarlo, regenerarlo con notas de ajuste si no es correcto, y consultar versiones previas.

**Layout:**
- `AppShell` protegido (ya existe vía `(protected)/layout.tsx`).
- Header: título de la evaluación (vía `useShellConfig`).
- Section 1 — **Editor de draft**: campos editables `title`, `context`, `instructions`, `objectives[]`, `deliverables[]`, `constraints[]` (confirmados contra `GenerateAssessmentDraftResponse` en `task-01`), con acción "Guardar cambios".
- Section 2 — **Regenerar**: input de notas de ajuste (`adjustmentNotes`, requerido) + acción "Regenerar con IA".
- Section 3 — **Historial de versiones**: lista de versiones previas, solo lectura.

**Acciones primarias (dos, independientes — cada una con su propio loading/error, per este task's Technical Design):**
- "Guardar cambios" → `PATCH /assessments/{id}/draft`.
- "Regenerar con IA" → `POST /assessments/{id}/draft/regenerate`.

**Acciones secundarias:** ninguna en el historial de versiones — **no existe acción "restaurar esta versión"** (confirmado en `task-01`: `AssessmentController.java` no tiene un 8º mapping para rollback). El historial es de solo lectura/consulta.

---

## Boceto visual (estado listo)

```
┌──────────────────────────────────────────────────────────────────────┐
│  AppShell (protegido)                                                │
│ ┌────────────────────────────────────────────────────────────────┐   │
│ │  {título de la evaluación}                                     │   │
│ └────────────────────────────────────────────────────────────────┘   │
│                                                                        │
│  ┌─ Editor de draft ────────────────────────────────────────────┐   │
│  │  ⚙ Generado por IA · v2                                       │   │
│  │                                                                 │   │
│  │  Título                                                        │   │
│  │  ┌───────────────────────────────────────────────────────────┐│   │
│  │  │                                                            ││   │
│  │  └───────────────────────────────────────────────────────────┘│   │
│  │                                                                 │   │
│  │  Contexto                                                      │   │
│  │  ┌───────────────────────────────────────────────────────────┐│   │
│  │  │ (textarea)                                                ││   │
│  │  └───────────────────────────────────────────────────────────┘│   │
│  │                                                                 │   │
│  │  Instrucciones                                                 │   │
│  │  ┌───────────────────────────────────────────────────────────┐│   │
│  │  │ (textarea)                                                ││   │
│  │  └───────────────────────────────────────────────────────────┘│   │
│  │                                                                 │   │
│  │  Objetivos (lista)      Entregables (lista)   Restricciones   │   │
│  │  ┌────────────────┐     ┌────────────────┐    ┌─────────────┐│   │
│  │  │ • ...          │     │ • ...          │    │ • ...       ││   │
│  │  └────────────────┘     └────────────────┘    └─────────────┘│   │
│  │                                                                 │   │
│  │                                        ┌──────────────────────┐│   │
│  │                                        │  Guardar cambios     ││   │
│  │                                        └──────────────────────┘│   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                        │
│  ┌─ Regenerar ───────────────────────────────────────────────────┐   │
│  │  Notas de ajuste *                                             │   │
│  │  ┌───────────────────────────────────────────────────────────┐│   │
│  │  │ (textarea)                                                ││   │
│  │  └───────────────────────────────────────────────────────────┘│   │
│  │                                        ┌──────────────────────┐│   │
│  │                                        │  Regenerar con IA    ││   │
│  │                                        └──────────────────────┘│   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                        │
│  ┌─ Historial de versiones (solo lectura) ──────────────────────┐   │
│  │  v2 (actual) — vista previa del título/contexto               │   │
│  │  v1 — vista previa del título/contexto                        │   │
│  └─────────────────────────────────────────────────────────────────┘   │
└────────────────────────────────────────────────────────────────────────┘
```

Tres secciones apiladas verticalmente, no tabs ni rutas separadas (per Technical Design: "un screen, no tres"). El editor va primero porque es la acción más frecuente (editar/aprobar contenido ya generado); regenerar es la segunda acción más común; el historial es de consulta ocasional y va al final.

---

## Estados

**Nota de trazabilidad:** el plan original de este task (Implementation Steps #1) asumía un estado "error de conflicto 409 (alguien más modificó el draft)". Se rastreó el código real antes de escribir esta tabla — igual que `task-02` hizo con el flujo de Intake — en vez de asumir la taxonomía genérica. **Ese estado 409 no existe:** ni `UpdateAssessmentDraftHandler` ni `RegenerateAssessmentDraftHandler` implementan bloqueo optimista (no hay chequeo de versión/ETag en `UpdateAssessmentDraftRequest` ni en `RegenerateAssessmentDraftRequest`), y `GlobalExceptionHandler.java` no mapea ningún 409 salvo `DuplicateEmailException` (dominio de auth, no de drafts). Una edición concurrente simplemente sobrescribe la fila silenciosamente (last-write-wins) — no hay señal de conflicto que la UI pueda mostrar. Se documenta como riesgo real más abajo, no se inventa un estado que la API no puede producir.

En su lugar, la tabla documenta el error real de cada uno de los 4 endpoints de este screen (`GET .../draft`, `PATCH .../draft`, `POST .../draft/regenerate`, `GET .../draft/versions`), verificado directamente contra `AssessmentController.java`, `GlobalExceptionHandler.java`, `{GetCurrentDraftHandler,UpdateAssessmentDraftHandler,RegenerateAssessmentDraftHandler,ListDraftVersionsHandler,DraftGenerationCoordinator}.java`, `NoPriorDraftException.java`, y `OwnershipVerifier.java` (2026-07-16).

| Estado | Disparador | Tratamiento visual |
|--------|-----------|---------------------|
| **loading inicial** | Carga de pantalla — `GET .../draft` + `GET .../draft/versions` en paralelo (Screen Data Facade `loadAssessmentDraftBuilderPage`, per story context) | Skeleton en las 3 secciones |
| **empty (defensivo)** | `GET .../draft` → 404 `ApiErrorResponse{error:"NOT_FOUND"}` porque aún no existe draft — no debería ocurrir llegando desde el redirect de `task-06` (que solo redirige tras un `generateDraft` exitoso), pero se documenta como estado defensivo (ej. link directo obsoleto) | Mensaje "Aún no se ha generado un borrador para esta evaluación." + acción para volver a Intake |
| **listo** | Ambas cargas resuelven con datos | Draft renderizado y editable, historial poblado (ver boceto arriba) |
| **guardando edición** | `PATCH .../draft` en curso | Botón "Guardar cambios" deshabilitado + spinner, campos del editor deshabilitados (evita doble envío, mismo patrón que `DynamicForm`'s `disabled` prop de `task-04`) |
| **regenerando** | `POST .../draft/regenerate` en curso | Botón "Regenerar con IA" deshabilitado + spinner/label "Regenerando…"; el editor permanece visible con el contenido *anterior* hasta que la respuesta reemplace el estado (no se limpia optimistamente) |
| **error de validación al guardar (422 — `List<FieldErrorResponse>`)** | `PATCH .../draft` rechaza un campo no vacío-pero-inválido (`@Size(min=1)` en `UpdateAssessmentDraftRequest` — solo aplica a campos enviados, `null` significa "sin cambio") | Error inline bajo el campo correspondiente, editor vuelve a editable |
| **error de notas vacías al regenerar (422 — `List<FieldErrorResponse>`)** | `POST .../draft/regenerate` — `adjustmentNotes` es `@NotBlank` | Error inline bajo el textarea de notas, sección "Regenerar" vuelve a editable |
| **error "no hay draft previo" (422 — `ApiErrorResponse{error:"APPLICATION_ERROR"}`)** | `NoPriorDraftException` en `PATCH` o `POST regenerate` — precondición defensiva, mismo caso límite que el estado "empty" de arriba | Mismo tratamiento que "empty": no debería ocurrir en el flujo normal, se documenta por completitud |
| **error de agente rechazado al regenerar (422 — `AgentClientException.AGENT_REJECTED`)** | `agents/` rechazó las notas de ajuste | Banner sobre la sección "Regenerar": "No pudimos regenerar el borrador con estas notas. Ajusta el texto e intenta de nuevo." — el draft actual **no se pierde**, sigue visible en el editor |
| **error de agente caído al regenerar (502/503 — `AGENT_ERROR`/`UNREACHABLE`)** | `agents/` no responde o falla al procesar | Banner distinto: "El servicio de generación no está disponible. Intenta de nuevo en unos minutos." — draft actual intacto |
| **error 404 — assessment no existe u ownership** | Cualquiera de los 4 endpoints — `ResourceNotFoundException`, incluyendo el disfraz de `OwnershipVerifier` (per `task-02`'s hallazgo, no revela existencia a otro docente) | Pantalla completa de error (no banner de sección): "No encontramos esta evaluación." + acción para volver al listado |
| **error inesperado (500)** | Fallo de infraestructura o excepción no prevista en cualquiera de los 4 endpoints | Banner genérico de reintento en la sección donde ocurrió la acción (guardar/regenerar), o error de pantalla completa si ocurrió en la carga inicial |

Eso son **12 estados reales** documentados (no 8) — el original agrupaba todo error de escritura bajo un solo "409 de conflicto" inexistente; la tabla real subdivide en 7 estados de error propios de 4 endpoints distintos, más 3 estados estructurales (loading/empty/listo) y 2 de "en curso" (guardando/regenerando) — 3 + 2 + 7 = 12.

**Riesgo real de concurrencia (reemplaza el estado 409 asumido):** dado que no hay bloqueo optimista, si el mismo docente tiene dos pestañas abiertas (o dos sesiones), la última escritura gana silenciosamente — no hay forma de que la UI detecte o comunique esto con los datos que la API expone hoy. Fuera de alcance de este wireframe corregir el backend; se deja registrado aquí para que `task-08`/`task-11` no asuman una respuesta 409 que nunca llegará.

No aplica un estado "conflicto" adicional a los 12 de arriba — queda cubierto por la nota de riesgo, no por un estado de UI ficticio.

---

## Bocetos de variantes de estado

Solo se muestra lo que cambia respecto al boceto "listo" de arriba.

**loading inicial:**

```
│  ┌─ Editor de draft ─────────────────────────────────────────────┐│
│  │  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓ (skeleton)                                  ││
│  │  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓                                ││
│  └──────────────────────────────────────────────────────────────┘│
│  ┌─ Historial de versiones ──────────────────────────────────────┐│
│  │  ▓▓▓▓▓▓▓▓▓▓  ▓▓▓▓▓▓▓▓▓▓                                       ││
│  └──────────────────────────────────────────────────────────────┘│
```

**empty (defensivo):**

```
│  ┌────────────────────────────────────────────────────────────┐  │
│  │  Aún no se ha generado un borrador para esta evaluación.    │  │
│  │  [ Volver a Intake ]                                        │  │
│  └────────────────────────────────────────────────────────────┘  │
```

**guardando edición:**

```
│                                        ┌──────────────────────┐│
│                                        │  ⏳ Guardando…       ││ ← deshabilitado
│                                        └──────────────────────┘│
```

**regenerando:**

```
│                                        ┌──────────────────────┐│
│                                        │  ⏳ Regenerando…     ││ ← deshabilitado
│                                        └──────────────────────┘│
```

**error de agente rechazado / caído al regenerar** (banner sobre la sección "Regenerar", no sobre todo el screen — el editor de arriba no se ve afectado):

```
│  ┌─ Regenerar ──────────────────────────────────────────────────┐│
│  │  ⚠ No pudimos regenerar el borrador con estas notas.         ││
│  │     Ajusta el texto e intenta de nuevo.                       ││
│  │                                                                ││
│  │  Notas de ajuste *                                            ││
│  │  ┌────────────────────────────────────────────────────────┐  ││
```

**error 404 (assessment no existe / ownership):**

```
┌──────────────────────────────────────────────────────────────────┐
│  AppShell (protegido)                                             │
│  ┌──────────────────────────────────────────────────────────────┐│
│  │  ⚠ No encontramos esta evaluación.                           ││
│  │     [ Volver al listado ]                                    ││
│  └──────────────────────────────────────────────────────────────┘│
└────────────────────────────────────────────────────────────────────┘
```

Reemplaza las 3 secciones completas — no tiene sentido mostrar un editor vacío si el recurso no existe.

---

## Nota sobre disclosure de IA (§9)

`02-ux-wireframes-y-maquetas.md` §9 exige que la UI distinga "qué fue generado por IA" de "qué fue editado por el docente." Se verificó `AssessmentDraft.java` (dominio) para diseñar esto contra datos reales, no contra un campo asumido:

- **Hallazgo:** `applyEdit(...)` reutiliza `restore(...)` preservando `agentExecutionLogId`, `versionNumber`, `id` y `createdAt` originales — **no existe ningún flag `editedByTeacher` ni timestamp de edición** persistido. Una vez guardada una edición, el registro es indistinguible de un draft recién generado por IA con los mismos valores.
- **Consecuencia para el wireframe:** el indicador "⚙ Generado por IA · v{n}" del boceto "listo" solo puede mostrarse con certeza **inmediatamente después de un `generateDraft`/`regenerateDraft` exitoso, dentro de la misma sesión de cliente** (estado en memoria, no persistido). Tras un recargo de página (`GET .../draft` fresco), el cliente no tiene forma de saber si el contenido actual es el original de IA o fue editado después — la API no expone esa distinción.
- **Decisión de diseño:** el indicador se muestra como "Generado por IA" justo tras generar/regenerar, y cambia a un rótulo neutro ("Versión actual") tras cualquier guardado de edición o recarga de página, en vez de afirmar falsamente "editado por el docente" o "generado por IA" cuando no se puede verificar. Esto es una limitación real del contrato de datos, no una omisión del wireframe — se deja registrado para que `task-08`(jerarquía)/`task-09`(mockup) no intenten fabricar un estado "editado" que el backend no puede respaldar.
- El historial de versiones (Section 3) tampoco tiene `createdAt` en `GenerateAssessmentDraftResponse` (confirmado en `task-01`) — cada fila del historial solo puede mostrar `versionNumber` + vista previa de contenido, no una fecha/hora. No inventar un timestamp en el mockup.

---

## Densidad y microcopy

- Tres secciones apiladas, sin tabs ni modales — coherente con `02-ux-wireframes-y-maquetas.md` §7: agrupación por tarea (editar, regenerar, consultar), no por tipo técnico.
- Microcopy en español claro, sin códigos HTTP ni nombres de excepciones Java expuestos (`15-backend-frontend-contracts.md` §4) — mismos principios aplicados en `task-06`'s corrección de mensajes en inglés del backend.
- "Guardar cambios" y "Regenerar con IA" como verbos de acción distintos — no un botón genérico "Enviar" que ambigüe cuál de las dos operaciones dispara.

---

## Resultado esperado del diseño (checklist §10)

- **Componentes necesarios:** ver `task-08` (jerarquía de componentes).
- **Hooks necesarios:** hook de página para Draft Builder (nombre TBD en `task-08`), análogo a `useIntakeAssessmentPage`.
- **DTOs requeridos:** `GenerateAssessmentDraftResponseDto`, `UpdateAssessmentDraftRequestDto`, `RegenerateAssessmentDraftRequestDto` (ver `task-01`; construidos en `task-10`/`task-11`).
- **Estados de UI:** los 12 listados arriba (3 estructurales, 2 "en curso", 7 de error real — no 8 asumidos; el 409 asumido no existe, ver nota de trazabilidad).
- **Validaciones:** `adjustmentNotes` requerido antes de habilitar "Regenerar con IA"; campos del editor con `@Size(min=1)` si se envían (validación servidor, ya que son todos opcionales/parciales por diseño de `UpdateAssessmentDraftRequest`).
- **Eventos de usuario:** editar campo, guardar cambios, escribir notas de ajuste, regenerar, expandir/consultar una versión del historial.
- **Tests mínimos:** guardar deshabilita el editor durante el request; regenerar deshabilita su sección sin afectar el editor; error de agente rechazado/caído no borra el draft actual (ver `task-09`/`task-12`).
- **Riesgos de UX:** el riesgo de concurrencia sin bloqueo optimista (ver nota arriba) — dos sesiones del mismo docente pueden sobrescribirse silenciosamente; ningún estado de UI puede mitigarlo con el contrato actual, queda como limitación conocida a comunicar al producto, no a resolver en este wireframe.
