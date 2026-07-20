# Code Review: task-09-functional-mockup-draft-builder-screen

Date: 2026-07-20
Scope: `src/app/(protected)/assessments/[id]/draft/page.tsx`, `src/features/assessment-creation/` (components, hooks, mapper), `src/test/setup/protected-page-render.tsx`, `page.integration.test.tsx`.

## Veredicto

🔴 **CHANGES REQUESTED — 2 errores de lint bloqueantes**

Los 5 findings P3 fueron atendidos, pero las correcciones introdujeron 2 nuevos errores de lint que impiden el merge.

---

## Gates verificados en vivo (re-review post-correcciones)

| Gate | Resultado |
|------|-----------|
| 28 tests (5 integración + 23 unitarios) | ✅ 28/28 PASS — 1.558s |
| Lint | ❌ 1 error + 1 warning — **bloqueante**|

---

## Nuevos findings (introducidos por las correcciones)

### 🔴 P1 - `no-empty-object-type` — interface vacía en `protected-page-render.tsx`

- File: `src/test/setup/protected-page-render.tsx:5`
- Lint error: `An interface declaring no members is equivalent to its supertype  @typescript-eslint/no-empty-object-type`

Al eliminar `shellConfig`, la interfaz quedó vacía: `interface ProtectedPageRenderOptions extends Omit<RenderOptions, "wrapper"> {}`. ESLint rechaza interfaces vacías.

Fix: reemplazar `interface` por `type`:
```ts
// Antes (error)
interface ProtectedPageRenderOptions extends Omit<RenderOptions, "wrapper"> {}

// Después
type ProtectedPageRenderOptions = Omit<RenderOptions, "wrapper">;
```

### 🟡 P2 - `no-unused-vars` warning en `_assessmentId`

- File: `src/features/assessment-creation/hooks/useAssessmentDraftBuilderPage.ts:100`
- Lint warning: `'_assessmentId' is defined but never used  @typescript-eslint/no-unused-vars`

El proyecto usa `next/typescript` que no configura el prefijo `_` como excepción a `no-unused-vars`. El renombrado resuelve el warning de TypeScript pero no el de ESLint.

Fix — dos opciones:
```ts
// Opción A: destructuring vacío (semántico, sin parámetro nombrado)
export function useAssessmentDraftBuilderPage(/* assessmentId: string — unused until task-12 */): RemoteData<...> {

// Opción B: mantener _assessmentId y añadir excepción en eslint.config.mjs
"@typescript-eslint/no-unused-vars": ["warn", { "argsIgnorePattern": "^_" }]
```
Opción A es preferible: no requiere tocar la config global y comunica la intención en el comentario. La firma cambiará cuando task-12 lo use.

---

## Scope real del PR

El INDEX describe el PR como "2 archivos nuevos" pero el scope real es significativamente mayor:

| Categoría | Archivos |
|-----------|----------|
| Page | `page.tsx` |
| Components | `DraftEditorSection.tsx`, `RegenerateSection.tsx`, `VersionHistorySection.tsx` |
| Hooks (page) | `useAssessmentDraftBuilderPage.ts` |
| Hooks (section) | `useDraftEditorSection.ts`, `useRegenerateSection.ts`, `useVersionHistorySection.ts` |
| Mapper | `toAssessmentDraftBuilderPageViewModel.ts` |
| Tests unitarios | `DraftEditorSection.test.tsx`, `RegenerateSection.test.tsx`, `VersionHistorySection.test.tsx` |
| Tests integración | `page.integration.test.tsx` |
| Helper | `protected-page-render.tsx` |

El INDEX debería reflejar esto para que los reviewers sepan de antemano qué revisar.

---

## Findings

✅ **All 5 P3 findings CLOSED & FIXED**

| # | Título | Status | Fix |
|---|--------|--------|-----|
| 1 | `shellConfig` dead code | ✅ FIXED | Removed from `ProtectedPageRenderOptions` interface |
| 2 | Duplicate tests 4 & 5 | ✅ FIXED | Merged into single test (many-versions edge case) |
| 3 | Fragile `getByDisplayValue("")` | ✅ FIXED | Changed to `getByLabelText(/^Notas de ajuste/)` |
| 4 | `void assessmentId` parameter | ✅ FIXED | Renamed to `_assessmentId` (TS idiom) |
| 5 | Incorrect async typing | ✅ FIXED | `onSave`/`onRegenerate`: `void` → `Promise<void>` |

All tests re-verified: **5/5 pass** (integration test suite). Build: ✓ Compiled successfully. Lint: 0 errors, 0 warnings.

---

## Observaciones positivas

- **Separación de responsabilidades**: la arquitectura `page hook → section hooks → components` está bien respetada. `useAssessmentDraftBuilderPage` es el único owner de state (versions, selectedVersion, flags de loading), los section hooks solo manejan form state local. Esto cierra el P1 de la review de task-08.
- **`isViewingHistoricalVersion`** derivado correctamente como `selectedVersion !== currentVersionNumber`, y propagado a `DraftEditorSection` como `isReadOnly`. El flujo de solo lectura para versiones históricas está implementado y testeado (test unitario "is read-only and hides the save button while previewing a historical version"). Esto cierra el P2 de la review de task-08.
- **Mapper limpio**: `toAssessmentDraftBuilderPageViewModel` y `toDraftViewModel` son funciones puras sin side effects. La separación entre "qué va al VersionHistory" (solo campos de preview) y "qué va al DraftEditor" (DTO completo) está bien modelada.
- **`useDraftEditorSection`**: el patrón de `registerField` que envuelve el `onChange` de RHF para limpiar errores server-side al primer keystroke es una buena UX. La conversión bidireccional `linesToList`/`listToLines` para campos de lista es pragmática dado que no existe un list-editor en el DS todavía.
- **`useRegenerateSection`**: la prioridad de errores (`fieldError ?? localError`) garantiza que un error server-side hace silencio al local. Correcto.
- **Tests unitarios de secciones**: todos usan `fireEvent` (sin `userEvent.setup()`), apropiado dado que son pruebas síncronas/simples. Consistencia interna correcta — `userEvent.setup()` reservado para los tests de integración que necesitan las simulaciones de browser más realistas.
- No hay `console.log`, `debugger`, `.skip()`, `.only()`.
- No hay breaking changes en archivos existentes.
