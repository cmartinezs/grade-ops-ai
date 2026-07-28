<a id="top"></a>

# TASKS — Web: Assessment Authoring Operation Foundation

**Status:** Ready. **Parent:** [README](README.md) · **Prev:** [CLAUDE-IMPLEMENTATION-PROMPT](CLAUDE-IMPLEMENTATION-PROMPT.md) · **Next:** [LOCAL-CONTRACTS →](LOCAL-CONTRACTS.md)

One task. Corresponds to Task 11 of the plan.

---

### Task 11 — Migrate to the new authoring contract

- **Objective:** Update `web/` per [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md) — idempotency-key generation, `expectedRevisionId` tracking, resume/retry UI, conflict handling.
- **Files (verified against the current tree — see [README.md](README.md)'s note on path drift from the plan document):**
  - `web/src/lib/api/assessments.ts` — API client functions; add `Idempotency-Key` header generation/sending, `expectedRevisionId` in regenerate/human-edit payloads, switch the edit call from `PATCH .../draft` to `POST .../revisions`, add calls for the new `POST .../draft/retry` and `GET .../generation-status` endpoints.
  - `web/src/features/assessment-creation/hooks/useIntakeAssessmentPage.ts` — create+generate orchestration; add idempotency-key generation per submit, remove the "no recovery" behavior, retain the assessment id on generation failure so the page can route to a resumable state instead of a dead end.
  - `web/src/features/assessment-creation/hooks/useAssessmentDraftBuilderPage.ts` — main draft/revision view; add the resume-on-load check (`currentRevisionId` absent → call `generation-status`), render `origin`/`actorId`/`reason` from the API response instead of the current transient local label.
  - `web/src/features/assessment-creation/hooks/useDraftEditorSection.ts`, `useRegenerateSection.ts` — track `expectedRevisionId` from the last-fetched revision; send it on regenerate/human-edit; handle `409 STALE_REVISION` explicitly.
  - `web/src/features/assessment-creation/hooks/useVersionHistorySection.ts` — render the richer revision list (origin/actor/reason/previousRevisionId) the API now returns additively.
  - `web/src/features/assessment-creation/components/DraftEditorSection.tsx`, `RegenerateSection.tsx`, `VersionHistorySection.tsx` — new states per [LOCAL-CONTRACTS.md § New/changed UI states](LOCAL-CONTRACTS.md#newchanged-ui-states); no visual redesign, wire existing components to new state values.
  - `web/src/features/assessment-creation/loaders/loadAssessmentDraftBuilderPage.ts`, `web/src/features/assessment-creation/mappers/toAssessmentDraftBuilderPageViewModel.ts` — extend the loaded/mapped shape for the new response fields (`origin`, `actorId`, `reason`, `previousRevisionId`, `currentRevisionId`).
- **Dependencies:** The API contract ([LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md)) is frozen and sufficient to start now, against a mock. Final, non-mocked verification needs the real API (API packet's Task 10) — see [CLAUDE-IMPLEMENTATION-PROMPT.md § You may start before the API is fully implemented](CLAUDE-IMPLEMENTATION-PROMPT.md#you-may-start-before-the-api-is-fully-implemented).
- **Steps:** Per [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md). Generate a UUID `Idempotency-Key` client-side per submit action (not per render). Track `expectedRevisionId` from the last-fetched revision. Implement the on-load resume check exactly as specified in [LOCAL-CONTRACTS.md § Recovery after refresh](LOCAL-CONTRACTS.md#recovery-after-refresh).
- **Tests to write first:** Jest/RTL tests for each new state in [LOCAL-CONTRACTS.md § New/changed UI states](LOCAL-CONTRACTS.md#newchanged-ui-states), written against the target behavior before wiring the component/hook. The single most important one: reproducing Research 02 §5.6 end-to-end from the client side (brief created, generation fails, reload, retry succeeds, exactly one `Assessment`).
- **Verification command:** `cd web && npm run lint && npm run test && npm run build`
- **Acceptance criteria:** Criterion #15 from [10 — Acceptance Criteria](../../../../docs/implementation-plans/assessment-authoring-operation-foundation/10-acceptance-criteria.md) — Web can resume a failed generation after refresh, demonstrated by a passing test.
- **Migration considerations:** None — Web has no persisted state to migrate.
- **Risks:** Low-medium. Mostly mechanical once the API contract is stable; the real risk is scope creep into visual redesign, explicitly out of bounds.
- **Commit boundary:** `feat(web): migrate authoring flow to idempotent, resumable, revision-aware contract` (may be split into reviewable sub-commits if the diff is large, per [CLAUDE-IMPLEMENTATION-PROMPT.md § Commits](CLAUDE-IMPLEMENTATION-PROMPT.md#commits) — each sub-commit must leave the app building and testing green).

---

**Not owned by this packet:** every other task in the plan. See [04 — Task Distribution](../../../../docs/implementation-packets/assessment-authoring-operation-foundation/04-task-distribution.md) for the full map.

---

← [CLAUDE-IMPLEMENTATION-PROMPT](CLAUDE-IMPLEMENTATION-PROMPT.md) | [↑ inicio](#top) | [Siguiente: LOCAL-CONTRACTS →](LOCAL-CONTRACTS.md)
