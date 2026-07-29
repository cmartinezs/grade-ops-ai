<a id="top"></a>

# Web Handoff — Assessment Authoring Operation Foundation

**Status:** Filled in. Session C complete. **Parent:** [README](README.md) · **Prev:** [TEST-PLAN](TEST-PLAN.md)

## Commits

```
52920cee1a343ea58ffe1c2dfb5da758119eff79 feat(web): migrate authoring flow to idempotent, resumable, revision-aware contract
```

(This handoff commit, `docs(web): record assessment authoring foundation handoff`, follows on top of the above.)

## Branch

`feat/assessment-authoring-operation-foundation-web`, pushed to origin at `52920cee1a343ea58ffe1c2dfb5da758119eff79`.

## Tasks completed

11 — Done. All items in [TASKS.md](TASKS.md)'s file list were updated: `src/lib/api/assessments.ts`, `src/features/assessment-creation/hooks/{useIntakeAssessmentPage,useAssessmentDraftBuilderPage,useDraftEditorSection*,useRegenerateSection,useVersionHistorySection*}.ts`, `src/features/assessment-creation/components/{DraftEditorSection,RegenerateSection,VersionHistorySection}.tsx`, `src/features/assessment-creation/loaders/loadAssessmentDraftBuilderPage.ts`*, `src/features/assessment-creation/mappers/toAssessmentDraftBuilderPageViewModel.ts` (`*` = inspected, no code change needed — see Assumptions).

## Test results

`cd web && npm run lint && npm run test && npm run build` → **PASS**.

- Lint: clean (0 warnings/errors).
- Tests: **171 passed, 171 total** (24 suites), up from the 153-test baseline (+18 new tests: Idempotency-Key/expectedRevisionId wiring, 202-durable-operation handling, resume-on-load states, stale-revision-conflict handling, provenance display, and the Research 02 §5.6 regression test).
- Build: succeeds, all 16 routes compile and prerender.

## Routes/screens changed

- `/assessments/new` (`useIntakeAssessmentPage.ts`, `BriefFormSection`/`BriefForm` unchanged) — client-generated `Idempotency-Key`, always navigates to the draft page once create succeeds regardless of generation's outcome.
- `/assessments/[id]/draft` (`page.tsx`, `useAssessmentDraftBuilderPage.ts`, `DraftEditorSection`, `RegenerateSection`, `VersionHistorySection`) — new resume-on-load states (`generation-not-started`, `generation-in-progress`, `generation-failed`, `generation-indeterminate`), stale-revision-conflict banner with a Recargar action, provenance (`origin`/`actorId`/`reason`) rendered from the API response instead of the old local `aiDisclosureLabel` state, edit calls switched to `POST .../revisions`.

No new routes. No visual redesign — new states reuse the existing DS `Button`/`Field`/`Badge` components and the same centered-message layout already used by the pre-existing not-found state.

## API assumptions

Built and tested against [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md) since the real API (API packet Task 10) does not exist yet. The following are Session C's own decisions where the frozen contract underspecified an exact shape or Web-side mechanism — **Session D must verify every one of these against the real implementation once API-A3 lands, before the functional PR merges**:

1. **409 conflict body shape** — assumed to be exactly `{ code: string, message: string | null }` for every typed 409 introduced by this cut (`IDEMPOTENCY_KEY_PAYLOAD_MISMATCH`, `ALREADY_GENERATED`, `STALE_REVISION`, `NO_ACTIVE_OPERATION_TO_RETRY`, `OPERATION_IN_PROGRESS`), per LOCAL-CONTRACTS.md's literal "`{ code, message }`" wording — distinct from the legacy `{ error, message }` shape used by non-409 errors today. Modeled as `ApiConflictErrorResponse` in `src/types/assessment.ts`. **Not yet verified against a real 409 response body.**
2. **Resume disambiguation trigger** — LOCAL-CONTRACTS.md says Web calls `GET .../generation-status` "whenever `currentRevisionId` is absent from the assessment summary," but no assessment-summary-fetch endpoint exists in this contract or in Web's current call graph. Assumed the existing `GET .../draft` 404 (today's "no draft yet" signal, per Research 02/task-07) is the trigger: on a 404 from `getAssessmentDraft`, Web now calls `getGenerationStatus` to disambiguate "generation never ran" / "in progress" / "failed" / "indeterminate" from a genuinely nonexistent/unauthorized assessment (which itself now 404s a second time, from `generation-status`). No new endpoint was invented to do this.
3. **Initial-generate 202 body shape** — `POST .../draft`'s 202 (durable pending/failed operation) response is assumed to be `AiOperationDto { id, status, failureCode? }`, matching the shape implied for the `AiOperation` record in the Authoring Operation Contract § 2. Its `status` value is *not* constrained by Web to the `generation-status` taxonomy (which excludes `FAILED_TERMINAL`) — Web reads it only for logging, never branches UI on it directly (see #4).
4. **Retry's success response is not interpreted** — after a successful `POST .../draft/retry` (202), Web does not parse or branch on the response body at all; it unconditionally re-runs the full load pipeline (`getAssessmentDraft` → `generation-status` if still no revision) to re-derive the true state. LOCAL-CONTRACTS.md's endpoint table gives retry's success only as bare `202`, with no documented body shape, so this sidesteps guessing one. Same treatment for a successful `generateAssessmentDraft` call from the new `generation-not-started` state's "Generar borrador" action.
5. **`expectedRevisionId` is `AssessmentDraftDto.draftId`** — LOCAL-CONTRACTS.md describes the draft DTO as gaining `origin`/`actorId`/`reason`/`previousRevisionId` "additively," with no new distinct "revision id" field. Assumed the existing `draftId` field *is* the revision id and is what Web sends back as `expectedRevisionId` on regenerate/human-edit.
6. **Idempotency key format** — a client-generated UUID-v4-shaped string produced via `Math.random()` (`src/lib/api/idempotencyKey.ts`), not `crypto.randomUUID()` (jsdom's test environment doesn't implement it — same constraint this codebase's existing `createCorrelationId()` already documents and avoids). Assumed the API accepts any string in that format for `Idempotency-Key`, with no stricter server-side validation.
7. **Create and generate use two independently generated keys**, not one shared key reused across both calls — since they have different idempotency scopes (`teacherUid` vs `assessmentId`, per the Idempotency and Concurrency Strategy decision) and different `operationType`s.
8. **`isRecoverableDraftMutationStatus`** (a Web-internal logging-severity helper, not a contract field) was widened to also classify 409 as WARN-not-ERROR, since `STALE_REVISION`/`ALREADY_GENERATED` are expected, user-actionable conflicts under the new contract, not incidents. This reverses task-07-era code's assumption that 409 was unreachable from any draft-mutation endpoint — that assumption is exactly what this whole cut changes.

## Contract changes

None. No endpoint, state value, failure code, payload field, `expectedRevisionId` semantics, or idempotency behavior was altered from [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md). Everything in "API assumptions" above is a Web-side implementation decision filling a gap the frozen contract left open, not a deviation from anything it explicitly specifies.

## Tests

New:
- `src/features/assessment-creation/__tests__/resumeAfterFailedGeneration.acceptance.test.tsx` — **the single most important test in this packet**: reproduces Research 02 §5.6 end-to-end (brief created → generation fails durably → reload → Retry → succeeds), asserting `POST /api/v1/assessments` fires exactly once throughout, mocked at the `apiClient` transport boundary so the real `submitAssessmentBrief`/`createAssessmentBrief`/`generateAssessmentDraft`/`getAssessmentDraft`/`getGenerationStatus`/`retryAssessmentDraftGeneration` implementations all run.
- `src/features/assessment-creation/testUtils/DraftBuilderPageTestWrapper.tsx` — shared test-only mirror of `page.tsx`'s render tree (not a test file itself; deliberately outside any `__tests__/` directory so Jest's default `testMatch` doesn't pick it up as an empty suite).

Changed (rewritten for the new contract, signatures, and error shapes): `src/lib/api/__tests__/assessments.test.ts`, `src/features/assessment-creation/hooks/__tests__/useAssessmentDraftBuilderPage.test.ts`, `src/features/assessment-creation/loaders/__tests__/loadAssessmentDraftBuilderPage.test.ts`, `src/features/assessment-creation/components/__tests__/{DraftEditorSection,RegenerateSection,VersionHistorySection}.test.tsx`, `src/app/(protected)/assessments/new/__tests__/NewAssessmentPage.test.tsx`, `src/app/(protected)/assessments/[id]/draft/page.integration.test.tsx` (now also covers: resume-on-load for all four `generation-status` values, stale-revision-conflict-blocks-and-reloads, provenance rendering).

## Legacy paths removed

Confirmed: `PATCH /api/v1/assessments/{id}/draft` is no longer called anywhere in `web/src` (`grep -rn "PATCH" src/` returns only an explanatory code comment in `assessments.ts`). `updateAssessmentDraft`/`UpdateAssessmentDraftError` were removed and fully replaced by `createAssessmentRevision`/`CreateAssessmentRevisionError` (`POST .../revisions`). The old transient `aiDisclosureLabel` local state/prop was removed everywhere; provenance now renders directly from the API's `origin`/`actorId`/`reason` fields.

## Blockers

None for further web-side work. The one open item is verification, not a blocker: this implementation has been tested only against mocks matching the frozen [LOCAL-CONTRACTS.md](LOCAL-CONTRACTS.md) — see "Integration requirements" below.

## Integration requirements

- **Not yet verified against the real API.** Every test in this packet mocks either `apiClient` (transport boundary) or the `@/lib/api/assessments` functions directly — no test has hit a real `api/` instance. This must happen before the functional PR (Session D) merges.
- When API-A3/A4 lands, re-run this packet's acceptance test (`resumeAfterFailedGeneration.acceptance.test.tsx`) as a true end-to-end check against the real API (or a contract test) to confirm the 8 assumptions in "API assumptions" above hold — items 1 (409 body shape) and 3/4 (202 body shapes) are the highest-risk ones, since they're structural response-shape guesses, not just missing-endpoint gap-filling.
- If any assumption doesn't hold, the fix is confined to `src/lib/api/assessments.ts` and `src/types/assessment.ts` (parsing/shape only) — no UI/state-machine rework should be needed unless the actual API introduces a state or failure code not in `LOCAL-CONTRACTS.md`'s canonical taxonomies, which would itself be a contract violation to flag back to Session A, not silently absorbed here.
- `web/.env.local` (Firebase placeholder values, gitignored) was created locally to unblock `npm run build`'s static prerender step, which otherwise fails with `auth/invalid-api-key` — pre-existing local-dev setup requirement, unrelated to this task, not committed.

---

← [TEST-PLAN](TEST-PLAN.md) | [↑ inicio](#top) | [↑ README](README.md)
