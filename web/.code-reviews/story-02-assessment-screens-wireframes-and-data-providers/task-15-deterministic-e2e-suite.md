# Code Review: task-15-deterministic-e2e-suite

Date: 2026-07-21
Scope: `e2e/draft-builder.spec.ts`, `e2e/login.spec.ts`, `e2e/registration.spec.ts`, `e2e/fixtures/auth.ts`, `e2e/support/firebaseEmulator.ts`, `e2e/support/seedDraft.ts`, `e2e/support/waitForPath.ts`, `playwright.config.ts`, `scripts/e2e-test.sh`, `jest.config.ts`, `eslint.config.mjs`.

## Veredicto

✅ **APPROVED — READY TO MERGE**

Todos los findings cerrados. Todos los gates limpios.

---

## Gates verificados en vivo (re-review final)

| Gate | Resultado |
|------|-----------|
| Jest (suite completa) | ✅ 153/153 PASS — 4.834s |
| `npm run lint` (repo completo) | ✅ No ESLint warnings or errors |
| `e2e/` excluido del linter | ✅ `{ ignores: ["e2e/**", "scripts/**"] }` en `eslint.config.mjs` |

---

## Historial de findings — estado final

| # | Finding | Corrección aplicada |
|---|---------|-------------------|
| 1 | `e2e/` no excluido del ESLint — 2 errores `react-hooks/rules-of-hooks` en `auth.ts` | `{ ignores: ["e2e/**", "scripts/**"] }` añadido como primer entry en `eslint.config.mjs`, con comentario explicando el false positive del parámetro `use` de Playwright vs `React.use()` |

---

## Observaciones positivas

- **`waitForPath` con `expect.poll()`**: solución correcta al problema de `waitForURL` en Next.js App Router (navegaciones vía History API + RSC fetch, sin `load` event). Documentado en el código con la causa exacta.
- **Fixture `authenticatedPage` ejerce el formulario real de login**: no usa storage-state shortcut — cada test autenticado también verifica implícitamente que el flujo de login funciona end-to-end.
- **`registration.spec.ts` independiente del fixture**: el spec de registro usa el formulario directamente, no el setup programático del fixture — una regresión en el formulario no puede ocultarse detrás del fixture.
- **`seedDraft` con `ON_ERROR_STOP=1`**: si el INSERT falla (ej. FK violation), el proceso aborta inmediatamente. No hay fallo silencioso.
- **`trap cleanup EXIT` en `e2e-test.sh`**: la limpieza del stack ocurre incluso si el script falla — Docker y procesos siempre se limpian.
- **Env vars al shell, no escritas a `.env.local`**: evita el problema de task-13 donde había que hacer backup/restore manual del archivo de entorno del developer.
- **`retries: 0`**: suite determinista por diseño — los reintentos enmascararían flakiness real.
- **`jest.config.ts` excluye `e2e/`**: sin este exclusión Jest intentaría parsear los specs de Playwright con su propio test runner y fallaría — correcto y comentado.
- **Techo del agente documentado en código**: el comentario en `draft-builder.spec.ts` y `seedDraft.ts` explica que `agents/` (Gemini) no está disponible y que el seeding SQL es el equivalente exacto al paso §5 de `api/scripts/smoke-test.sh`. La limitación está documentada en el lugar correcto, no omitida.
- **Regression guards explícitos**: los comments en `draft-builder.spec.ts` referencian los bugs específicos de task-13 (Bug 1: `/api` prefix rewrite, Bug 3: CORS `PATCH` faltante) que los tests comprueban. Trazabilidad correcta entre tests y defectos conocidos.
