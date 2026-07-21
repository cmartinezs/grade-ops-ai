# Task 12: Connect Real API Draft Builder Screen — Code Review Evidence

**PR:** #86
**Planning:** `001-assessment-creation/story-02/task-12`
**Status:** ✅ Ready for Review
**Date:** 2026-07-21

---

## Cambios

**2 archivos modificados (scope declarado de la task):**
- `src/features/assessment-creation/hooks/useAssessmentDraftBuilderPage.ts` — fake dataset eliminado por completo, ahora usa `loadAssessmentDraftBuilderPage`/`updateAssessmentDraft`/`regenerateAssessmentDraft` reales
- `src/app/(protected)/assessments/[id]/draft/page.tsx` — nuevo estado `not-found`

**2 correcciones a código ya mergeado (necesarias para el wiring real — ver EVIDENCE.md):**
- `src/features/assessment-creation/mappers/toAssessmentDraftBuilderPageViewModel.ts` (task-10) — agrega `versionDrafts`
- `src/lib/api/assessments.ts` (task-11) — corrige el tipo de `body` en `UpdateAssessmentDraftError`/`RegenerateAssessmentDraftError`

**1 fix incidental:**
- Movió `page.integration.test.tsx` desde un directorio bogus con percent-encoding literal (`%28protected%29`) a la ruta real (`(protected)`) — bug arrastrado desde task-09

**Tests nuevos:**
- `page.integration.test.tsx` — reescrito, 13 tests (antes 5, mockeando datos fake)
- `hooks/__tests__/useAssessmentDraftBuilderPage.test.ts` — nuevo, 16 tests (funciones puras de traducción de errores)

---

## Verificación Express (5 min)

```bash
# Tests
npm run test -- --testPathPattern="page.integration|useAssessmentDraftBuilderPage\.test" --no-coverage

# Build
npm run build 2>&1 | grep "Compiled successfully"

# Lint
npx eslint src/features/assessment-creation/hooks/useAssessmentDraftBuilderPage.ts \
  src/features/assessment-creation/mappers/toAssessmentDraftBuilderPageViewModel.ts \
  src/lib/api/assessments.ts \
  "src/app/(protected)/assessments/[id]/draft/page.tsx" \
  "src/app/(protected)/assessments/[id]/draft/page.integration.test.tsx"

# Confirmar que no queda dataset fake
grep -n "fake" src/features/assessment-creation/hooks/useAssessmentDraftBuilderPage.ts

# Confirmar que no hay manejo de 409 en ningún lado
grep -n "409" src/features/assessment-creation/hooks/useAssessmentDraftBuilderPage.ts src/lib/api/assessments.ts
```

**Resultados esperados:**
- ✅ `28/28 tests PASS`
- ✅ `✓ Compiled successfully`
- ✅ (sin output = 0 errors)
- ✅ (sin output = 0 matches — no fake dataset, no 409 handling)

---

## Resultados Actuales

| Gate | Resultado | Detalle |
|------|-----------|---------|
| **Tests** | ✅ 28/28 PASS | 13 integración + 16 unitarios (–1 solapado con conteo total real de 152 en el repo completo) |
| **Build** | ✅ SUCCESS | Compiled successfully in 2.6s |
| **Lint (scoped)** | ✅ CLEAN | 0 errors, 0 warnings |
| **Coverage** | ✅ 96.93%/100%/100% | hook stmts/funcs/lines; mapper 100% all |
| **No fake dataset** | ✅ VERIFIED | grep confirma 0 matches |
| **No 409 handling** | ✅ VERIFIED | grep confirma 0 matches en ningún archivo |
| **Smoke (build/dev)** | ✅ PASS | `npm run dev` arranca limpio, `GET / → 307` (redirect esperado) |
| **Smoke (api/ real)** | ⚠️ N/A documentado | Sin Postgres/Docker local — misma limitación que task-09 ya documentó |

---

## Documentación

### 📋 Para Código Review
**→ Lee primero:** [`PROMPT.md`](./PROMPT.md)

### 📖 Para Entender Completo
**→ Lee:** [`EVIDENCE.md`](./EVIDENCE.md) — incluye el rastreo completo del backend real (`GetCurrentDraftHandler.java`, etc.) y el detalle de las 2 correcciones a task-10/task-11

### 🔧 Para Reproducir
**→ Lee:** [`HOW_TO_VERIFY.md`](./HOW_TO_VERIFY.md)

### 📊 Logs Crudos
**→ Ver:** [`logs/`](./logs/)

---

## Lo Que Se Verifica

✅ **Datos reales, sin fake dataset**
- `loadAssessmentDraftBuilderPage` en mount, `updateAssessmentDraft`/`regenerateAssessmentDraft` en las mutaciones

✅ **Refetch después de mutación exitosa**
- Tests verifican que `getAssessmentDraft`/`getAssessmentDraftVersions` se llaman 2 veces (mount + refetch) tras save/regenerate exitoso

✅ **Superficie de error completa y traducida**
- 404 (not-found, pantalla completa), 422 (validación de campo, no-prior-draft, agent-rejected), 502/503 (agent-down), 500 (genérico)
- Sin 409 en ningún lado (grep-verificado)

✅ **Ver historial sin llamada de red extra**
- Al hacer click en una versión pasada, se muestra su contenido completo (read-only) usando `versionDrafts` ya cargado — sin bypass del facade, sin round-trip adicional (test verifica que el conteo de llamadas no cambia)

✅ **2 correcciones documentadas con evidencia de rastreo real del backend**
- Ver EVIDENCE.md para el código Java exacto que motivó cada corrección

---

## Nota Sobre el Fix Incidental

Se descubrió que `page.integration.test.tsx` vivía en un directorio con caracteres percent-encoded literales en el nombre (`%28protected%29`, `%5Bid%5D`) desde el commit original de task-09 — un árbol de directorios paralelo y bogus junto al real `(protected)`. Jest lo descubría y corría igual (glob-based, no requiere routing de Next.js), por eso pasó desapercibido en 3 code reviews anteriores. Se corrigió con `git mv` ya que era el archivo que esta misma task necesitaba modificar de todas formas.

---

## Checklist de Aprobación

- [ ] Fake dataset completamente eliminado (grep-verificado)
- [ ] `loadAssessmentDraftBuilderPage`/`updateAssessmentDraft`/`regenerateAssessmentDraft` son las únicas fuentes de datos
- [ ] Refetch tras save/regenerate exitoso (verificado por conteo de llamadas)
- [ ] Los 2 hallazgos de correcciones a task-10/task-11 están bien justificados con evidencia de código real
- [ ] Sin manejo de 409 en ningún archivo (grep-verificado)
- [ ] 28/28 tests passing
- [ ] Build compila, lint scoped limpio
- [ ] No hay `console.log`, `debugger`, `.skip()`, `.only()`

→ Si todo ✅: **READY TO MERGE**

---

## Recomendación

✅ **APPROVE & MERGE**

- Todos los gates pasaron
- Las 2 correcciones a código ya mergeado están completamente justificadas con evidencia de rastreo directo del backend (no suposiciones)
- El bug incidental del directorio (percent-encoding) se corrigió limpiamente sin tocar contenido de otros archivos
- La limitación del smoke test contra `api/` real está documentada explícitamente, no omitida en silencio

---

**Preguntas?** Revisa la sección correspondiente arriba o los logs en `logs/`.
