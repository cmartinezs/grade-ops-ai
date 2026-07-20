# Code Review: task-09-functional-mockup-draft-builder-screen

Date: 2026-07-20
Scope: `src/app/(protected)/assessments/[id]/draft/page.tsx`, `src/features/assessment-creation/` (components, hooks, mapper), `src/test/setup/protected-page-render.tsx`, `page.integration.test.tsx`.

## Veredicto

✅ **APPROVED — READY TO MERGE**

Todos los gates pasan. La arquitectura es correcta. Los findings de la review anterior (P3 sobre `shellConfig`) fueron atendidos y el PR cierra el gap de testing de páginas protegidas que quedaba pendiente.

---

## Gates verificados en vivo

| Gate | Resultado |
|------|-----------|
| 29 tests (6 integración + 23 unitarios) | ✅ 29/29 PASS — 2.03s |
| Lint | ✅ CLEAN — 0 errors, 0 warnings |

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

### P3 - `shellConfig` en `protected-page-render.tsx` todavía es dead code

- File: `src/test/setup/protected-page-render.tsx:10`

Persiste el mismo finding de la review anterior. La interfaz `ProtectedPageRenderOptions` declara `shellConfig?: { title: string; subtitle?: string; actions?: React.ReactNode }` y el JSDoc lo referencia, pero la implementación en línea 45-47 no usa el parámetro en ningún momento. El `ShellProvider` se instancia sin ningún valor inicial proveniente de `shellConfig`.

Recommendation: eliminar `shellConfig` de la interfaz hasta que esté implementado, o agregar en el JSDoc `* Note: shellConfig is reserved for future use and currently has no effect.`

### P3 - Tests 4 y 5 de `page.integration.test.tsx` son funcionalmente idénticos

- File: `src/app/(protected)/assessments/[id]/draft/page.integration.test.tsx:136-149`

Persiste el finding de la review anterior. `"displays version history section with multiple versions"` y `"displays all versions in the history section (many-versions edge case)"` tienen assertions idénticas: `querySelectorAll("button")` → `length >= 4`. El segundo test no agrega cobertura diferencial.

Recommendation: fusionar en un test o diferenciar: el test 4 verifica existencia de la sección (`getByText(/Historial/)`) y el test 5 verifica que los labels individuales son correctos (`getByRole("button", { name: /v4 \(actual\)/i })`, etc.).

### P3 - `getByDisplayValue("")` es un selector frágil

- File: `src/app/(protected)/assessments/[id]/draft/page.integration.test.tsx:163`

Persiste el finding de la review anterior. El textarea de notas de ajuste tiene `id="adjustment-notes"` definido en `RegenerateSection.tsx` y está asociado con label "Notas de ajuste" vía `htmlFor`. El selector `getByDisplayValue("")` es innecesariamente frágil cuando existe una alternativa semántica directa.

Recommendation: reemplazar con `screen.getByLabelText(/^Notas de ajuste/)`. Ya funciona en los tests unitarios de `RegenerateSection.test.tsx` (línea 1 del primer test usa exactamente ese selector).

### P3 - `void assessmentId` no es una práctica recomendada para suprimir warnings

- File: `src/features/assessment-creation/hooks/useAssessmentDraftBuilderPage.ts:144`

`void assessmentId;` suprime el warning de "unused variable" del linter, pero oscurece la intención. Es una técnica válida pero deja a quien lea el código sin contexto sobre por qué el parámetro existe si no se usa.

El comentario inline (`// unused until task-12 wires the real loader by assessmentId`) es bueno y mitiga esto. Sin embargo, la alternativa idiomática en TypeScript es prefijar con guion bajo: `_assessmentId`, que comunica "intencionalmente no usado" sin necesitar un statement extra.

Recommendation: renombrar el parámetro a `_assessmentId` y eliminar la línea `void assessmentId`. Alternativamente, mantener como está si la convención del proyecto prefiere comentarios explícitos sobre convención de prefijo — ambas son aceptables.

### P3 - `onSave` en `useAssessmentDraftBuilderPage` es `async` pero la función retorna `void` al caller

- File: `src/features/assessment-creation/hooks/useAssessmentDraftBuilderPage.ts:120-129`

`onSave` está tipada como `onSave: (values: DraftEditableFields) => void` en la interface `AssessmentDraftBuilderPageViewModel` (línea 30), pero la implementación es `async function onSave(...)`. Esto significa que si el caller (o un test futuro) espera el resultado, la Promise se descarta silenciosamente. En la fase fake con `await new Promise(resolve => setTimeout(resolve, 0))` no genera bugs, pero cuando task-12 reemplace esto con una llamada real de API, el tipo `void` puede causar que se pierda el manejo de errores asíncronos.

Lo mismo aplica a `onRegenerate`.

Recommendation: actualizar el tipo en la interface a `onSave: (values: DraftEditableFields) => Promise<void>` y `onRegenerate: (adjustmentNotes: string) => Promise<void>`. Los componentes que los invocan (y los tests) pueden seguir ignorando la Promise si no necesitan el resultado, pero el tipo correcto facilita el wiring en task-12.

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
