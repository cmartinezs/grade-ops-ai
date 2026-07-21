# Code Review: task-11-mutations-draft-builder-screen

Date: 2026-07-21
Scope: `src/types/assessment.ts` (nuevo DTO), `src/lib/api/assessments.ts` (2 funciones + 2 clases de error + helper), `src/lib/api/__tests__/assessments.test.ts` (11 tests nuevos).

## Veredicto

✅ **APPROVED — READY TO MERGE**

Todos los gates pasan. El código es consistente con el patrón establecido en task-10. Los invariantes clave (partial-update, no 409, log levels) están verificados por tests, no solo afirmados.

---

## Gates verificados en vivo

| Gate | Resultado |
|------|-----------|
| 27 tests (11 nuevos + 16 existentes) | ✅ 27/27 PASS — 0.46s |
| Lint (scoped a 3 archivos) | ✅ CLEAN — 0 errors, 0 warnings |
| Sin datos sensibles en logs | ✅ `adjustmentNotes`, `title`, `context`, `instructions` — ninguno aparece en llamadas a `log.*` |

> **Nota sobre lint/tests sin scope:** 5 fallas preexistentes en `SignOutButton.test.tsx`/`RegisterPage.test.tsx` — confirmadas en el baseline de la story branch antes de este PR.

---

## Findings

### P3 - `isRecoverableDraftMutationStatus` no es exportada ni testeable en aislamiento

- File: `src/lib/api/assessments.ts:171`

La función `isRecoverableDraftMutationStatus` clasifica 422/502/503 como recuperables (WARN) y todo lo demás como ERROR. Es una regla de negocio con su propia lógica (`status === 422 || status === 502 || status === 503`) pero está declarada como `function` privada al módulo.

Los 4 tests de log-level la ejercen indirectamente a través de `updateAssessmentDraft` y `regenerateAssessmentDraft`, lo que da cobertura funcional. Sin embargo, si en el futuro se añade un nuevo status recuperable (ej. 504), la verificación de que la clasificación es correcta requiere ejecutar toda la función API, no solo el helper.

Recommendation: exportarla (`export function isRecoverableDraftMutationStatus`) o moverla a un archivo compartido si crece. No bloquea — la cobertura actual es suficiente para esta task.

### P3 - `updateAssessmentDraft` no acepta un `Logger` externo — inconsistente con `getAssessmentDraft`/`createAssessmentBrief`

- File: `src/lib/api/assessments.ts:175`

Las funciones de lectura (`getAssessmentDraft`, `getAssessmentDraftVersions`) y la función de submit (`createAssessmentBrief`) aceptan `log: Logger = logger` como parámetro opcional, lo que permite al caller inyectar un logger con contexto extra (ej. el `correlationId` del loader padre). Las mutaciones de task-11 (`updateAssessmentDraft`, `regenerateAssessmentDraft`) crean su propio `correlationId` internamente y no aceptan un logger externo.

Esto es una decisión de diseño válida (las mutaciones son acciones independientes, no parte de una operación batch), documentada en la EVIDENCE y justificada correctamente. Lo menciono como observación de consistencia de API, no como bug. Task-12 no necesita inyectar un logger externo en estas funciones.

No es un finding bloqueante — la decisión de diseño está bien razonada.

---

## Observaciones positivas

- **Partial-update verificado con `Object.keys()`**: el test `"sending only the caller-provided keys"` parsea el body serializado y afirma exactamente qué keys contiene — no solo que el resultado "se vea bien". Es la forma correcta de probar esta garantía.
- **409 auditado activamente**: hay un test explícito que envía un mock de status 409 y verifica que cae en el path genérico (`UpdateAssessmentDraftError`/`RegenerateAssessmentDraftError`), no en un branch especial. Esto documenta en código la decisión de task-07.
- **Log levels verificados con mocks**: los 4 tests de criticidad hacen `expect(logger.warn).toHaveBeenCalled()` y `expect(logger.error).not.toHaveBeenCalled()` (y viceversa). No es solo lectura de código.
- **`isRecoverableDraftMutationStatus`**: el comentario inline explica exactamente por qué esos tres status son recuperables (422 = validación de campos, 502/503 = agente IA caído) y por qué no hay 409. Documentación útil para el siguiente dev.
- **Patrón de error consistente**: `UpdateAssessmentDraftError` y `RegenerateAssessmentDraftError` tienen la misma firma que `GetAssessmentDraftError` de task-10 (`status`, `body`, `assessmentId`). Task-12 puede usar `instanceof` de forma uniforme.
- **Sin datos sensibles en logs**: `adjustmentNotes`, `title`, `context`, `instructions` no aparecen en ninguna llamada a `log.*`. Solo se loguea `assessmentId`, `versionNumber`, `status`, `latencyMs`.
- No hay `console.log`, `debugger`, `.skip()`, `.only()`.
- No hay breaking changes en funciones existentes.
