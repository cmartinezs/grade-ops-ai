# Code Review: task-09-functional-mockup-draft-builder-screen

Date: 2026-07-20
Scope: `src/app/(protected)/assessments/[id]/draft/page.tsx`, `src/features/assessment-creation/` (components, hooks, mapper), `src/test/setup/protected-page-render.tsx`, `page.integration.test.tsx`.

## Veredicto

✅ **APPROVED — READY TO MERGE**

Todos los findings cerrados. Todos los gates limpios.

---

## Gates verificados en vivo (re-review final)

| Gate | Resultado |
|------|-----------|
| 28 tests (5 integración + 23 unitarios) | ✅ 28/28 PASS — 1.328s |
| Lint | ✅ CLEAN — 0 errors, 0 warnings | ✅ Compiled successfully in 2.2s |

---

## Issues Cerrados (5 P3 + 2 Lint)

### Historial de findings — estado final

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

---

## Historial de findings — estado final

| # | Finding | Corrección aplicada |
|---|---------|--------------------|
| 1 | `shellConfig` dead code | `shellConfig` eliminado de interfaz y JSDoc |
| 2 | Tests 4 y 5 idénticos | Fusionados en 1 (28 tests totales) |
| 3 | `getByDisplayValue("")` frágil | Reemplazado con `getByLabelText(/^Notas de ajuste/)` |
| 4 | `void assessmentId` | Renombrado a `_assessmentId` + `argsIgnorePattern: "^_"` en `eslint.config.mjs` |
| 5 | `onSave`/`onRegenerate` `void` → `Promise<void>` | Actualizado en interface y firma |
| 6 | Interface vacía `no-empty-object-type` | `interface` → `type ProtectedPageRenderOptions = Omit<RenderOptions, "wrapper">` |

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
