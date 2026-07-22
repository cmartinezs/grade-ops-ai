# Code Review: task-10-data-provider-draft-builder-screen

Date: 2026-07-20
Scope: `src/types/assessment.ts`, `src/lib/api/assessments.ts`, `src/lib/logging/correlationId.ts` (nuevo), `src/features/assessment-creation/loaders/loadAssessmentDraftBuilderPage.ts`, mapper + hook (import cleanup), test suites.

## Veredicto

✅ **APPROVED — READY TO MERGE**

Todos los findings cerrados. Todos los gates limpios.

---

## Gates verificados en vivo (re-review final)

| Gate | Resultado |
|------|-----------|
| 29 tests (11 nuevos + 18 existentes) | ✅ 29/29 PASS — 1.195s |
| Lint (scoped a 8 archivos, incluye `correlationId.ts`) | ✅ CLEAN — 0 errors, 0 warnings |
| Facade enforcement | ✅ único caller verificado |
| `AssessmentDraftDto` unicidad | ✅ 1 sola definición en `src/types/assessment.ts` |

> **Nota sobre lint sin scope:** errores preexistentes en `login/page.tsx`, `register/page.tsx`, `reset-password/page.tsx`, `AuthGuard.test.tsx` — confirmados en el baseline de la branch antes de este PR, no son regresión.

---

## Historial de findings — estado final

| # | Finding | Corrección aplicada |
|---|---------|-------------------|
| 1 | `createCorrelationId` duplicada en 2 archivos | Extraída a `src/lib/logging/correlationId.ts`, ambos la importan |
| 2 | Error body descartado en `getAssessmentDraft`/`getAssessmentDraftVersions` | `GetAssessmentDraftError` y `GetAssessmentDraftVersionsError` creadas, propagan `status`, `body` y `assessmentId`. Tests actualizados para verificar el tipo y el body completo |
| 3 | Test de timing frágil (`< 50ms`) | Threshold subido a `< 500ms` |

---

## Observaciones positivas

- **`createCorrelationId` en módulo propio**: el comentario del por qué (evitar `crypto.randomUUID()` en jsdom) se preservó en el nuevo módulo — documentación correcta para el siguiente dev.
- **`GetAssessmentDraftError` / `GetAssessmentDraftVersionsError`**: consistentes con el patrón de `CreateAssessmentBriefError` y `GenerateAssessmentDraftError`. Task-12 ahora puede hacer `instanceof GetAssessmentDraftError` para distinguir 404 vs 500 sin depender solo del message string.
- **Tests de error actualizados**: `"throws GetAssessmentDraftError carrying the ApiErrorResponse body, status, and assessmentId on failure"` — verifica el tipo de error y que el body se propaga. Correcto.
- **Single source of truth para `AssessmentDraftDto`**: una sola definición, confirmada por grep.
- **Facade enforcement**: único caller de `getAssessmentDraft`/`getAssessmentDraftVersions` es el loader, verificado por grep reproducible.
- **`correlationId` compartido** entre ambas llamadas paralelas via `logger.child()`. Trazabilidad correcta.
- **Logging sin datos sensibles**: no se loguea `title`, `context`, ni `instructions`.
- No hay `console.log`, `debugger`, `.skip()`, `.only()`.
- No hay breaking changes.
