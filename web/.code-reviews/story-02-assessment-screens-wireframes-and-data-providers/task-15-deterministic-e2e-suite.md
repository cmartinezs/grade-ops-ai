# Code Review: task-15-deterministic-e2e-suite

Date: 2026-07-21
Scope: `e2e/draft-builder.spec.ts`, `e2e/login.spec.ts`, `e2e/registration.spec.ts`, `e2e/fixtures/auth.ts`, `e2e/support/firebaseEmulator.ts`, `e2e/support/seedDraft.ts`, `e2e/support/waitForPath.ts`, `playwright.config.ts`, `scripts/e2e-test.sh`, `jest.config.ts` (exclusión de `e2e/`).

## Veredicto

🔴 **CHANGES REQUESTED — 1 error de lint bloqueante**

La suite es sólida en diseño, pero el directorio `e2e/` no está excluido del linter del proyecto y 2 errores de `react-hooks/rules-of-hooks` bloquean el merge.

---

## Gates verificados en vivo

| Gate | Resultado |
|------|-----------|
| Jest (suite completa) | ✅ 153/153 PASS — 2.73s (e2e/ correctamente excluido del testPathIgnorePatterns) |
| Lint scoped a archivos de esta task | ❌ 2 errors en `e2e/fixtures/auth.ts` — **bloqueante** |
| Jest no pica specs de Playwright | ✅ `testPathIgnorePatterns: ["e2e/"]` en `jest.config.ts` — correcto |

---

## Finding

### 🔴 P1 — `e2e/` no está excluido del ESLint del proyecto — 2 errores en `auth.ts`

- File: `e2e/fixtures/auth.ts:19`, `:28`

```
error  React Hook "use" is called in function "teacher" that is neither a React function
       component nor a custom React Hook function.  react-hooks/rules-of-hooks

error  React Hook "use" is called in function "authenticatedPage" that is neither a React
       function component nor a custom React Hook function.  react-hooks/rules-of-hooks
```

El linter ve `async ({ request }, use) => { ... await use(teacher) }` y malinterpreta el parámetro `use` de la fixture API de Playwright (`base.extend<T>`) como el hook `React.use()`. No es código React — es un false positive del rule `react-hooks/rules-of-hooks` sobre código Playwright completamente correcto.

**Causa raíz:** `eslint.config.mjs` hereda `next/core-web-vitals` que incluye `react-hooks/rules-of-hooks`, y no hay ninguna exclusión para `e2e/`. El mismo rule que excluye Jest no excluye Playwright.

**Fix — opción A (recomendada):** añadir `e2e/` y `scripts/` a los `ignores` en `eslint.config.mjs`:
```ts
const eslintConfig = [
  { ignores: ["e2e/**", "scripts/**"] },       // ← añadir antes del extend
  ...compat.extends("next/core-web-vitals", "next/typescript"),
  { rules: { ... } },
];
```

**Fix — opción B:** renombrar el parámetro `use` a algo que no colisione con el nombre del hook (ej. `provide`). No require cambios en config global pero es menos idiomático — la API de Playwright usa `use` por convención en toda su documentación.

La opción A es preferible: aísla la config del linter de Next.js del código Playwright/scripts sin contaminar la convención de nombrado.

---

## Observaciones positivas (para cuando el lint esté limpio)

- **`waitForPath` con `expect.poll()`**: la explicación del por qué `page.waitForURL()` cuelga en Next.js App Router (navegaciones vía History API + RSC fetch, sin `load` event) es exacta y está documentada en código. La solución de polling de URL es la forma correcta.
- **Fixture `authenticatedPage` ejerce el formulario real de login**: el fixture no usa storage-state shortcut — cada test que necesite autenticación también verifica implícitamente que el flujo de login funciona. Esto mantiene `login.spec.ts` como regression guard explícito y el fixture como una segunda cobertura del mismo flujo.
- **`registration.spec.ts` independiente del fixture**: el spec de registro no usa `authenticatedPage` a propósito, para que una regresión en el formulario de registro no pueda ocultarse detrás del fixture que usa setup programático.
- **`seedDraft` con `execFileSync` + `ON_ERROR_STOP=1`**: si el INSERT falla (ej. FK violation por un `assessmentId` inválido), el proceso aborta inmediatamente en vez de continuar silenciosamente. Correcto.
- **SQL injection defensivo**: `title.replace(/'/g, "''")` — escaping básico para el único campo interpolado en el SQL. Suficiente dado que es código de test con datos controlados, no producción.
- **`e2e-test.sh` con `trap cleanup EXIT`**: la limpieza del stack (docker down, kill de procesos) ocurre incluso si el script falla — no deja procesos/contenedores colgados. Correcto.
- **Env vars exportadas al shell en el script, no escritas a `.env.local`**: evita el problema de task-13 donde había que hacer backup/restore manual del archivo. Correcto.
- **`retries: 0` en `playwright.config.ts`**: los tests E2E de esta suite son deterministas por diseño (data seeded, stack limpio por run) — no necesitan reintentos. Reintentos enmascarían flakiness real.
- **`jest.config.ts` excluye `e2e/`**: sin este exclusión Jest intentaría parsear los specs de Playwright con su propio API y fallaría — el fix está presente y comentado con el motivo exacto.
- **Documentación del techo del agente**: el comentario en `draft-builder.spec.ts` y `seedDraft.ts` explica que `agents/` (Gemini) no está disponible en el entorno local/CI y que el seeding SQL es el equivalente exacto al paso §5 de `api/scripts/smoke-test.sh`. No oculta la limitación — la documenta en el lugar correcto.
