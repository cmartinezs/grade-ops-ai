# Code Review: task-12-connect-real-api-draft-builder-screen

Date: 2026-07-21
Scope: `useAssessmentDraftBuilderPage.ts` (rewrite), `page.tsx` (not-found state), `toAssessmentDraftBuilderPageViewModel.ts` (versionDrafts), `assessments.ts` (body type fix), `page.integration.test.tsx` (rewrite), `useAssessmentDraftBuilderPage.test.ts` (nuevo, 16 tests).

## Veredicto

✅ **APPROVED — READY TO MERGE**

Todos los gates pasan. El wiring real está correctamente implementado. Las 2 correcciones a task-10/task-11 están justificadas con evidencia de rastreo real del backend.

---

## Gates verificados en vivo

| Gate | Resultado |
|------|-----------|
| 28 tests (13 integración + 15 unitarios) | ✅ 28/28 PASS — 2.101s |
| Lint (scoped a 5 archivos) | ✅ CLEAN — 0 errors, 0 warnings |
| No fake dataset | ✅ grep devuelve exit 1 — 0 matches |
| No 409 handling | ✅ solo aparece en comentarios explicativos |
| Directorio bogus `%28protected%29` | ✅ eliminado — find devuelve exit 1 |

---

## Findings

### P3 — `translateRegenerateError` tiene un gap de cobertura en el branch 502/503

- File: `src/features/assessment-creation/hooks/useAssessmentDraftBuilderPage.ts:119`

```ts
if ((error.status === 502 || error.status === 503) && error.body.error === "AGENT_CALL_FAILED") {
  return { fieldError: null, agentError: AGENT_DOWN_MESSAGE };
}
```

Los tests unitarios cubren 502 y 503 por separado (bien). Sin embargo, el branch `if` requiere **dos condiciones**: `status === 502 || 503` **y** `body.error === "AGENT_CALL_FAILED"`. Si el backend envía 502 con un `body.error` diferente (ej. un gateway genérico con body `{"error":"BAD_GATEWAY","message":null}`), la condición falla y cae al `GENERIC_RETRY_MESSAGE` final — que es correcto en términos de UX, pero el test solo verifica la condición cuando `body.error === "AGENT_CALL_FAILED"`.

La cobertura de branch en la EVIDENCE reporta `84.61%` — esta es la rama no cubierta. No es un bug (el fallback genérico es el comportamiento correcto para un 502 sin `AGENT_CALL_FAILED`), pero sí un camino no testeado.

Recommendation: añadir un test unitario: `new RegenerateAssessmentDraftError(502, { error: "BAD_GATEWAY", message: null }, "a1")` → `{ fieldError: null, agentError: GENERIC_RETRY_MESSAGE }`. Demuestra que el fallback genérico funciona correctamente para 502s inesperados. No bloquea.

---

## Observaciones positivas

- **Fake dataset completamente eliminado**: no hay `buildFakeVersions`, `LONG_INSTRUCTIONS` ni estado estático. El hook usa `loadAssessmentDraftBuilderPage` en mount y refetch tras mutación exitosa — verificado por grep y por los tests de conteo de llamadas.
- **`versionDrafts` en el mapper**: la corrección de task-10 es limpia y aditiva — `draft`/`versions` no se tocaron, `loadAssessmentDraftBuilderPage.test.ts` sigue pasando sin cambios. El test de integración `"clicking a past version in history... without a second network call"` verifica que el conteo de llamadas no cambia al navegar entre versiones históricas.
- **Corrección del tipo `body` en task-11**: justificada con el código Java exacto (`MethodArgumentNotValidException` → `List<FieldErrorResponse>`), el mismo error que `CreateAssessmentBriefError` ya corregía correctamente desde task-04. Backward compatible — los tests de task-11 no hacían assertions sobre el tipo TS, solo sobre la shape en runtime.
- **Funciones puras exportadas**: `isDraftNotFoundError`, `translateSaveError`, `translateRegenerateError` exportadas y testeadas en aislamiento — 16 tests directos sin levantar componentes. Es la forma correcta de testear lógica de traducción de errores: no a través de un renderizado completo.
- **Refetch verificado por conteo de llamadas**: `expect(mockGetAssessmentDraft).toHaveBeenCalledTimes(2)` — no solo "el UI se ve bien", sino que el facade se llamó de nuevo.
- **`not-found` como estado de primer nivel**: `page.status === "not-found"` en `RemoteData<T>` es la forma correcta — no un flag booleano ad-hoc ni un mensaje hardcodeado en el error handler genérico. La página renderiza una pantalla completa con link de regreso a `/assessments`.
- **Fix incidental del directorio `%28protected%29`**: correcto — `git mv` sin tocar contenido, y el test sigue pasando en su ubicación real. La explicación de por qué pasó desapercibido (Jest es glob-based, no routing de Next.js) está bien documentada.
- No hay `console.log`, `debugger`, `.skip()`, `.only()`.
- No hay breaking changes en la API pública del mapper ni en los tests de tasks anteriores.
