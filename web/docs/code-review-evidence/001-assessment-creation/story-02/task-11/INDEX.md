# Task 11: Mutations Draft Builder Screen — Code Review Evidence

**PR:** #85
**Planning:** `001-assessment-creation/story-02/task-11`
**Status:** ✅ APPROVED — 1 actionable P3 finding fixed
**Date:** 2026-07-21

---

## Code Review Findings

Code review approved with 2 P3 findings:

| # | Finding | Status | Fix |
|---|---------|--------|-----|
| 1 | `isRecoverableDraftMutationStatus` not exported/testable in isolation | ✅ Fixed | Exported + dedicated isolation test block (2 tests) |
| 2 | Mutations don't accept an external `Logger` (consistency observation) | No action | Reviewer explicitly noted this as a well-reasoned design decision, not a recommendation to change |

Re-verified: 29/29 tests pass (2 new), build compiles, scoped lint clean.

---

## Cambios

**1 archivo modificado (types):**
- `src/types/assessment.ts` — agrega `UpdateAssessmentDraftRequestDto` (todos los campos opcionales)

**1 archivo modificado (API):**
- `src/lib/api/assessments.ts` — agrega `updateAssessmentDraft()` (PATCH), `regenerateAssessmentDraft()` (POST), `UpdateAssessmentDraftError`, `RegenerateAssessmentDraftError`

**1 archivo modificado (tests):**
- `src/lib/api/__tests__/assessments.test.ts` — 11 tests nuevos

---

## Verificación Express (5 min)

```bash
# Tests
npm run test -- --testPathPattern="assessments\.test" --no-coverage

# Build
npm run build 2>&1 | grep "Compiled successfully"

# Lint (scoped)
npx eslint src/types/assessment.ts src/lib/api/assessments.ts src/lib/api/__tests__/assessments.test.ts

# Confirmar que updateAssessmentDraft solo envía las keys provistas (no todos los campos)
npm run test -- --testPathPattern="assessments\.test" -t "sending only the caller-provided keys" --no-coverage
```

**Resultados esperados:**
- ✅ `27/27 tests PASS`
- ✅ `✓ Compiled successfully`
- ✅ (sin output = 0 errors)
- ✅ Test específico pasa (verifica `Object.keys(sentBody)`, no solo lectura de código)

---

## Resultados Actuales

| Gate | Resultado | Detalle |
|------|-----------|---------|
| **Tests** | ✅ 27/27 PASS | 11 nuevos + 16 existentes |
| **Build** | ✅ SUCCESS | Compiled successfully in 2.0s |
| **Lint (scoped)** | ✅ CLEAN | 0 errors, 0 warnings |
| **Partial update contract** | ✅ VERIFIED | `Object.keys(sentBody)` asserted, no defaults filtrados |
| **No 409 handling** | ✅ VERIFIED | Test explícito con mock 409 confirma que cae en el path genérico |
| **Log level por criticidad** | ✅ VERIFIED | Aserciones sobre `logger.warn`/`logger.error`, no solo lectura de código |

---

## Documentación

### 📋 Para Código Review
**→ Lee primero:** [`PROMPT.md`](./PROMPT.md)

### 📖 Para Entender Completo
**→ Lee:** [`EVIDENCE.md`](./EVIDENCE.md)

### 🔧 Para Reproducir
**→ Lee:** [`HOW_TO_VERIFY.md`](./HOW_TO_VERIFY.md)

### 📊 Logs Crudos
**→ Ver:** [`logs/`](./logs/)

---

## Lo Que Se Verifica

✅ **`updateAssessmentDraft` respeta el contrato de partial update**
- Solo serializa las keys que el caller pasó explícitamente
- Test verifica `Object.keys(sentBody)` — no solo que el resultado "se vea bien"

✅ **`regenerateAssessmentDraft` produce nueva versión**
- Envía `{ adjustmentNotes }`, retorna `AssessmentDraftDto` con `versionNumber` incrementado

✅ **Superficie de error correcta — sin 409**
- `UpdateAssessmentDraftError`/`RegenerateAssessmentDraftError` con `status`/`body`/`assessmentId`
- 422/502/503 → WARN; 500 → ERROR (verificado con mocks de logger, no asumido)
- Test explícito con status 409 confirma que no existe branch especial (alineado con hallazgo de task-07: ningún endpoint de draft implementa optimistic locking)

✅ **Logging conforme a `.planning/LOGGING.md`**
- Cada mutación crea su propio `correlationId` (acciones independientes, no un solo page-load)
- Nunca loguea texto del draft ni las adjustment notes — solo `assessmentId`, `versionNumber`, status

---

## Nota Sobre Tests/Lint Sin Scope

`npm run test` completo tiene 5 fallas preexistentes en `SignOutButton.test.tsx`/`RegisterPage.test.tsx` — confirmado con `git stash` que existen en el baseline de la story branch antes de este PR. Fuera de scope de task-11.

---

## Checklist de Aprobación

- [ ] `UpdateAssessmentDraftRequestDto` con todos los campos opcionales, matching task-01
- [ ] `updateAssessmentDraft` usa PATCH, solo envía keys provistas
- [ ] `regenerateAssessmentDraft` usa POST, retorna nueva versión
- [ ] 27/27 tests passing
- [ ] Build compila sin errores
- [ ] Lint scoped limpio
- [ ] No existe branch de manejo de 409 en ninguna función
- [ ] Logging: correlationId por mutación, niveles por criticidad, sin datos sensibles
- [ ] No hay `console.log`, `debugger`, `.skip()`, `.only()`

→ Si todo ✅: **READY TO MERGE**

---

## Recomendación

✅ **APPROVE & MERGE**

- Todos los gates pasaron
- Contrato de partial-update verificado con test específico, no asumido
- Ausencia de manejo de 409 verificada activamente (test con mock 409), no solo por omisión
- Logging cumple la política establecida en task-05, niveles verificados con mocks

---

**Preguntas?** Revisa la sección correspondiente arriba o los logs en `logs/`.
