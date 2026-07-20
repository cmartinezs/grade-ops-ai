# Code Review: task-09-functional-mockup-draft-builder-screen

Date: 2026-07-20  
Scope: `src/test/setup/protected-page-render.tsx` (nuevo), `src/app/(protected)/assessments/[id]/draft/page.integration.test.tsx` (nuevo).

## Veredicto

✅ **APPROVED — READY TO MERGE**

Todos los gates pasan. No hay breaking changes. El patrón es correcto y escalable.

---

## Evidencia de Verificación

Para revisar completo, ver: **`docs/code-review-evidence/001-assessment-creation/story-02/task-09/`**

- 📋 **INDEX.md** — Entrada para reviewers (5 min overview)
- 📄 **PROMPT.md** — Resumen conciso
- 📖 **EVIDENCE.md** — Verificación detallada
- 🔧 **HOW_TO_VERIFY.md** — Guía step-by-step
- 📊 **logs/** — Raw test/build output

---

## Gates verificados en vivo

| Gate | Resultado |
|------|-----------|
| 6 integration tests nuevos | ✅ 6/6 PASS — 1.103s |
| 23 unit tests existentes | ✅ 23/23 PASS — sin regresiones |
| Lint | ✅ CLEAN — 0 errors, 0 warnings |

---

## Findings

### P3 - `shellConfig` declarado en la interfaz pero nunca usado en la implementación

- File: `src/test/setup/protected-page-render.tsx:10`

La interfaz `ProtectedPageRenderOptions` declara `shellConfig?: { title: string; subtitle?: string; actions?: React.ReactNode }` y el JSDoc lo documenta como `"Default: { title: "Test Page" }"`, pero la implementación en línea 45-47 ignora el parámetro completamente: solo envuelve en `<ShellProvider>` sin pasar ningún config inicial.

El resultado es que un consumidor puede escribir `renderProtectedPage(<MyPage />, { shellConfig: { title: "Custom" } })` esperando que ese título se aplique al shell, y no sucede nada. El parámetro es dead code a nivel runtime.

Recommendation: eliminar `shellConfig` de la interfaz y del JSDoc hasta que esté implementado, o añadir en el JSDoc una nota explícita `* Note: shellConfig is reserved for future use and has no effect currently.`

### P3 - Tests 4 y 5 son funcionalmente idénticos

- File: `src/app/(protected)/assessments/[id]/draft/page.integration.test.tsx:136-149`

`"displays version history section with multiple versions"` y `"displays all versions in the history section (many-versions edge case)"` hacen exactamente los mismos assertions: buscan el heading, obtienen `querySelectorAll("button")`, y verifican `length >= 4`. El segundo test no agrega cobertura adicional.

Recommendation: fusionar en un solo test, o diferenciar: el test 4 puede verificar la existencia de la sección y el test 5 puede verificar los labels de cada botón (ej. que contienen "v1", "v2", "v3", "v4").

### P3 - `getByDisplayValue("")` es un selector frágil

- File: `src/app/(protected)/assessments/[id]/draft/page.integration.test.tsx:163`

En el test `"displays regenerate section with functional controls"`, el textarea de notas se obtiene con `screen.getByDisplayValue("")`. Este selector matchea el _primer_ input o textarea con valor vacío que encuentre en el DOM — incluyendo el campo de título si en algún momento queda vacío por otro test, o si se agrega otro campo vacío a `DraftEditorSection`.

Recommendation: usar un selector más específico: `screen.getByRole("textbox", { name: /notas de ajuste/i })` si el label existe, o `screen.getByPlaceholderText(...)` si el campo tiene placeholder, o `document.getElementById("adjustment-notes")` si ese ID está presente en el markup de `RegenerateSection`.

---

## Observaciones sin finding (positivas)

- `DraftBuilderPageTestWrapper` replica fielmente el cuerpo de `page.tsx` producción. La decisión de no importar directamente la page (evitando problemas con `use(params)` y `"use client"` en JSDOM) es correcta.
- Mocks al tope del archivo, antes de cualquier `describe` — correcto.
- `userEvent.setup()` (API v14) — correcto, evita el deprecated `userEvent.type()` directo.
- `waitFor` usado solo en el test de edición donde hay una actualización de estado asíncrona — sin over-use.
- No hay `console.log`, `debugger`, `.skip()` ni `.only()`.
- El helper `renderProtectedPage` tiene JSDoc completo con `@param` y `@example`. Fácil de adoptar.

---

## Cómo Abordar los Findings

### Addressing P3 #1: `shellConfig` unused

**Option A (recommended):** Remover de la interfaz ahora, agregar en futuro cuando se necesite.

```typescript
// Cambiar:
interface ProtectedPageRenderOptions extends Omit<RenderOptions, "wrapper"> {}

// De:
shellConfig?: { title: string; subtitle?: string; actions?: React.ReactNode };
```

Task-12 puede re-introducirlo cuando se wirea API real y sea necesario test-driven config.

**Option B:** Mantener con nota explícita en JSDoc:
```typescript
/**
 * Note: shellConfig is reserved for future use. Currently has no effect.
 * Task-12 will implement when API integration requires configurable shell state.
 */
shellConfig?: { title: string; ... };
```

### Addressing P3 #2: Duplicate tests 4 & 5

**Recommendation:** Fusionar en un solo test o diferenciar.

Versión fusionada (1 test):
```typescript
it("displays version history with multiple versions", () => {
  renderPage();
  const versionHistorySection = screen.getByText(/Historial de versiones/i).closest("section");
  const versionButtons = versionHistorySection?.querySelectorAll("button") ?? [];
  
  // Both assertions in one test:
  expect(versionHistorySection).toBeInTheDocument();           // Section exists
  expect(versionButtons.length).toBeGreaterThanOrEqual(4);     // Many versions
});
```

O diferenciados:
- Test 4: Section + headings present
- Test 5: Each version button has correct label ("v1", "v2", etc)

### Addressing P3 #3: `getByDisplayValue("")` is fragile

**Recommendation:** Usar selector específico:

```typescript
// Cambiar:
const adjustmentNotesInput = screen.getByDisplayValue("") as HTMLTextAreaElement;

// A:
const adjustmentNotesInput = document.getElementById("adjustment-notes") as HTMLTextAreaElement;

// O si el label existe:
const adjustmentNotesInput = screen.getByRole("textbox", { name: /notas de ajuste/i });

// O si existe placeholder:
const adjustmentNotesInput = screen.getByPlaceholderText(/notas de ajuste/i);
```

Check `RegenerateSection.tsx` para ver cuál es más robusto (id, label, placeholder).

---

## Resumen de Hallazgos

| Finding | Severity | Status | Recommendation |
|---------|----------|--------|-----------------|
| `shellConfig` unused | P3 | Ready | Remove or add explicit JSDoc note |
| Tests 4 & 5 duplicate | P3 | Ready | Merge into single test or differentiate assertions |
| Fragile selector | P3 | Ready | Use `getElementById` or `getByRole` instead |

Ninguno de estos hallazgos bloquea merge. Son mejoras para próximas iteraciones.

---

## Dónde Encontrar Evidencia

- **Estructura de testing:** `docs/code-review-evidence/001-assessment-creation/story-02/task-09/INDEX.md`
- **Tests crudos:** `docs/code-review-evidence/001-assessment-creation/story-02/task-09/logs/test-results.log`
- **Build crudos:** `docs/code-review-evidence/001-assessment-creation/story-02/task-09/logs/build-results.log`
- **Implementación:** `src/test/setup/protected-page-render.tsx` + `src/app/(protected)/assessments/[id]/draft/page.integration.test.tsx`
