<a id="top"></a>

# TEST-PLAN — Web: Assessment Authoring Operation Foundation

**Status:** Ready. **Parent:** [README](README.md) · **Prev:** [LOCAL-CONTRACTS](LOCAL-CONTRACTS.md) · **Next:** [HANDOFF →](HANDOFF.md)

## Scope

Jest + React Testing Library, the existing pattern already in this codebase (see `useAssessmentDraftBuilderPage.test.ts`, `BriefForm.test.tsx`, `DraftEditorSection.test.tsx`, `RegenerateSection.test.tsx`, `VersionHistorySection.test.tsx` under `web/src/features/assessment-creation/**/__tests__/`). No new test framework.

## Test types

| Type | Used for |
|---|---|
| Web unit/component | New states from [LOCAL-CONTRACTS.md § New/changed UI states](LOCAL-CONTRACTS.md#newchanged-ui-states): failed-retryable render, in-progress render, stale-conflict render, resume-after-reload render |
| Web acceptance | The exact Research 02 §5.6 regression scenario: brief created, generation fails, reload, retry succeeds, exactly one `Assessment` exists |

## The single most important test in this packet

A test reproducing Research 02 §5.6 end-to-end from the client side — this is the literal defect (no recoverable path after a failed generation call) that motivated the whole cut. Write it against the target behavior before implementing the resume flow.

## What is not re-tested

Existing, already-passing coverage for behavior this packet does not change — form validation on `BriefForm` (React Hook Form + Zod, unchanged), unrelated components — is left as-is.

## Verification

```bash
cd web && npm ci && npm run lint && npm run test && npm run build
```

All must pass. **No CI workflow currently runs `web/`** (pre-existing gap, out of scope) — run this manually before every commit; nothing else will catch a regression here.

---

← [LOCAL-CONTRACTS](LOCAL-CONTRACTS.md) | [↑ inicio](#top) | [Siguiente: HANDOFF →](HANDOFF.md)
