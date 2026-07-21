# Code Review: task-10-data-provider-draft-builder-screen

Date: 2026-07-20
Scope: `src/types/assessment.ts`, `src/lib/api/assessments.ts`, `src/features/assessment-creation/loaders/loadAssessmentDraftBuilderPage.ts`, mapper + hook (import cleanup), test suites.

## Veredicto

✅ **APPROVED — READY TO MERGE**

Todos los gates pasan. La arquitectura está bien ejecutada y los invariantes de la tarea son verificables objetivamente, no solo por lectura de código.

---

## Gates verificados en vivo

| Gate | Resultado |
|------|-----------|
| 29 tests (11 nuevos + 18 existentes) | ✅ 29/29 PASS — 1.628s |
| Lint (scoped a 7 archivos de la task) | ✅ CLEAN — 0 errors, 0 warnings |
| Facade enforcement | ✅ `getAssessmentDraft`/`getAssessmentDraftVersions` — único caller: `loadAssessmentDraftBuilderPage.ts` |
| `AssessmentDraftDto` unicidad | ✅ 1 sola definición en `src/types/assessment.ts` |

> **Nota sobre lint sin scope:** `npm run lint` completo reporta errores preexistentes en `login/page.tsx`, `register/page.tsx`, `reset-password/page.tsx`, `AuthGuard.test.tsx` — confirmados en el baseline de la branch antes de este PR. No es una regresión de esta task.

---

## Findings

### P3 - `createCorrelationId` está duplicado entre `assessments.ts` y `loadAssessmentDraftBuilderPage.ts`

- Files: `src/lib/api/assessments.ts:72`, `src/features/assessment-creation/loaders/loadAssessmentDraftBuilderPage.ts:6`

Ambos archivos definen `function createCorrelationId()` con exactamente el mismo cuerpo:
```ts
return `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 10)}`;
```

`assessments.ts` la usa para `submitAssessmentBrief` (task-05). El loader la usa para su propia traza. Son implementaciones idénticas con el mismo comentario de justificación (evita `crypto.randomUUID()` en jsdom).

Recommendation: extraer a `src/lib/logging/correlationId.ts` (o similar) y que ambos la importen. No bloquea este PR porque cada copia está en su propio scope y no hay riesgo de drift inmediato, pero acumula deuda si se añaden más loaders.

### P3 - El error path en `getAssessmentDraft`/`getAssessmentDraftVersions` descarta el body leído

- File: `src/lib/api/assessments.ts:119`, `:136`

En el path de error:
```ts
await res.json().catch(() => ({ error: "UNKNOWN", message: null }));
// ... body nunca se usa
throw new Error(`Failed to fetch assessment draft: ${res.status}`);
```

El body parseado se descarta silenciosamente. En `createAssessmentBrief` y `generateAssessmentDraft` (task-05) el body se propaga en el error (`new CreateAssessmentBriefError(res.status, body)`). Aquí en cambio el error solo lleva el status code.

Esto no es un bug hoy porque el caller (`loadAssessmentDraftBuilderPage`) hace `throw error` directamente y el hook de task-12 lo manejará. Pero si task-12 necesita distinguir `404 Not Found` de `403 Forbidden` para mostrar mensajes distintos al usuario, la información ya se perdió en la capa API.

Recommendation: crear una `GetAssessmentDraftError` clase análoga a `CreateAssessmentBriefError`, o al menos propagar el body en el mensaje: `throw new Error(`Failed to fetch assessment draft: ${res.status} — ${body.error}`)`. No bloquea este PR.

### P3 - El test de timing para paralelismo es correcto pero frágil en CI lento

- File: `src/features/assessment-creation/loaders/__tests__/loadAssessmentDraftBuilderPage.test.ts:60-82`

El test usa `expect(elapsedMs).toBeLessThan(50)` para probar que `Promise.all` corre en paralelo (2 × 10ms = ~10ms, no ~20ms). Con el límite de 50ms hay margen suficiente en local, pero en un CI bajo carga heavy (algunos runners llegan a tener 5-10× overhead en `setTimeout`) este test puede flakear.

Recommendation: aumentar el límite a 500ms (más que suficiente para distinguir 10ms paralelo de 20ms secuencial) o reescribir el assertion como verificación de order-of-calls en vez de timing: usar `jest.useFakeTimers()` + `jest.advanceTimersByTimeAsync()` para controlar el tiempo determinísticamente. Ninguna de las dos opciones bloquea este PR — el test es correcto en su propósito, solo el threshold es ajustable.

---

## Observaciones positivas

- **Single source of truth para `AssessmentDraftDto`**: la migración del tipo desde la copia local del mapper a `src/types/assessment.ts` es limpia. Un solo `grep` lo confirma.
- **Facade enforcement verificable**: el `grep` de calidad en `EVIDENCE.md` es un comando reproducible, no solo una afirmación. Todos los callers de las funciones API están dentro del facade — ningún componente o página los llama directamente.
- **`Promise.all` con log compartido**: el `correlationId` se crea una vez y se pasa via `logger.child()` a ambas llamadas paralelas. Ambas trazas comparten el mismo id. Correcto.
- **Logging conforme**: `DEBUG` en éxito individual, `ERROR` en fallo, `INFO` en el succeed del loader completo. No se loguea `title`, `context`, ni `instructions` — solo `assessmentId`, `versionNumber`, `versionCount`, status.
- **3 failure-mode tests**: draft falla solo, versions falla solo, ambas fallan — cubre todos los casos de `Promise.all` de forma exhaustiva.
- **`getAssessmentDraftVersions` acepta array vacío**: el test `"returns an empty array when no versions exist"` cubre el edge case de un assessment con solo la versión actual (alineado con el fix del P2 de task-08 sobre el contrato de lista de versiones).
- No hay `console.log`, `debugger`, `.skip()`, `.only()`.
- No hay breaking changes en el mapper ni en `AssessmentDraftBuilderPageViewModel`.
