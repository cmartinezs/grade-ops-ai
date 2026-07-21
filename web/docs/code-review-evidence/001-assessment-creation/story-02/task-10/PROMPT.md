# Code Review Prompt for PR 84

## Cambios
Screen Data Facade para el draft builder page — carga draft actual + versiones en paralelo:
- `AssessmentDraftDto` en `src/types/assessment.ts` (matches task-01's `GenerateAssessmentDraftResponse`)
- `getAssessmentDraft()` / `getAssessmentDraftVersions()` en `src/lib/api/assessments.ts`
- `loadAssessmentDraftBuilderPage()` — nuevo, en `loaders/` — usa `Promise.all` + mapper de task-09

## Por Qué
task-12 necesita una única función que cargue todo lo que la página requiere, sin que el componente llame los `getX()` directamente (regla de Screen Data Facade, `06-estado-datos-y-api.md` §7).

## Verificación Rápida (5 min)
```bash
# Tests
npm run test -- --testPathPattern="assessments|loadAssessmentDraftBuilderPage" --no-coverage
# Expected: 29/29 PASS

# Build
npm run build 2>&1 | grep "Compiled successfully"
# Expected: ✓ Compiled successfully

# Lint (scoped)
npx eslint src/types/assessment.ts src/lib/api/assessments.ts src/lib/api/__tests__/assessments.test.ts \
  src/features/assessment-creation/loaders/loadAssessmentDraftBuilderPage.ts \
  src/features/assessment-creation/loaders/__tests__/loadAssessmentDraftBuilderPage.test.ts \
  src/features/assessment-creation/mappers/toAssessmentDraftBuilderPageViewModel.ts \
  src/features/assessment-creation/hooks/useAssessmentDraftBuilderPage.ts
# Expected: exit 0, sin output

# Confirmar que el facade es el único caller
grep -rn "getAssessmentDraft\b\|getAssessmentDraftVersions\b" src/app src/features --include="*.tsx" --include="*.ts" | grep -v "__tests__\|\.test\."
# Expected: solo matches dentro de loadAssessmentDraftBuilderPage.ts
```

## Resultados
✅ **29 tests passing** (6 nuevos en loader + 5 nuevos en API funcs + 18 existentes)
✅ **Build limpio** (Compiled successfully in 2.1s)
✅ **Lint limpio** (scoped a los 7 archivos de esta task)
✅ **Facade enforcement verificado** (grep confirma único caller)
✅ **Paralelismo verificado** (test de timing, no solo lectura del código)

## Qué Se Testea
- ✅ `getAssessmentDraft`/`getAssessmentDraftVersions` llaman los paths correctos y parsean el DTO confirmado
- ✅ `loadAssessmentDraftBuilderPage` ejecuta ambas llamadas en paralelo (`Promise.all`, no secuencial)
- ✅ Rechaza si falla una llamada, la otra, o ambas — sin datos parciales silenciosos
- ✅ View model compuesto correctamente (labels de preview, flag `isCurrent`, orden por versión)

## Nota Sobre Lint Sin Scope
El repo completo tiene errores de lint preexistentes en archivos no relacionados (`login/page.tsx`, `register/page.tsx`, `reset-password/page.tsx`, `AuthGuard.test.tsx`). Confirmado que ya existían en la story branch antes de este PR — fuera de scope de task-10.

## Evidencia Disponible
- `EVIDENCE.md` — Verificación detallada + checklist
- `HOW_TO_VERIFY.md` — Guía paso-a-paso para reviewers
- `test-results.log` — Output de tests
- `build-results.log` — Output de build

## Recomendación
✅ **READY TO MERGE** — Todas las gates pasaron, facade enforcement y paralelismo verificados con evidencia, no solo por inspección.
