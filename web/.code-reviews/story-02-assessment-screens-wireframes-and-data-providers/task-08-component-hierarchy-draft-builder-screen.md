# Code Review: task-08-component-hierarchy-draft-builder-screen

Date: 2026-07-17
Scope: `task-08-component-hierarchy-draft-builder-screen.md`, `wireframes/draft-builder-screen-hierarchy.md`, the task-07 wireframe source, and downstream task-09/task-11/task-12 contracts that consume this hierarchy.

## Findings

### P1 - Mutation/refetch ownership is split between section hooks and the page hook

- Files:
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/wireframes/draft-builder-screen-hierarchy.md:17`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/wireframes/draft-builder-screen-hierarchy.md:19`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/wireframes/draft-builder-screen-hierarchy.md:60`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/wireframes/draft-builder-screen-hierarchy.md:61`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-11-mutations-draft-builder-screen.md:43`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-12-connect-real-api-draft-builder-screen.md:18`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-12-connect-real-api-draft-builder-screen.md:31`

The hierarchy says `useDraftEditorSection` and `useRegenerateSection` wrap the real task-11 mutations, while the page hook only owns `onSaved`/`onRegenerated` callbacks. That conflicts with task-11/task-12, which explicitly make the post-mutation version refetch a page-hook concern through `loadAssessmentDraftBuilderPage`. It also lets task-09 implement the final tree with network-owning section hooks, then task-12 has to move handlers into `useAssessmentDraftBuilderPage` despite saying the component tree should not change.

The practical failure mode is stale or duplicated page state: Section 2 can append the returned regenerated draft locally via `onRegenerated(updated)` instead of refetching the authoritative current draft + versions list; Section 1 can save through a section hook while the page hook remains responsible for `selectedVersion` and historical read-only state. That is exactly the cross-section coordination the hierarchy already says belongs above the sections.

Recommendation: make `useAssessmentDraftBuilderPage` the owner of `onSave` and `onRegenerate` for both fake and real phases. Section hooks should own form state, validation, disabled state, and local field errors, but call callbacks supplied by the page hook. After success, the page hook refetches or reloads the page view model per task-11/task-12.

### P2 - The historical-version read-only fix is not propagated into task-09's implementation/tests

- Files:
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/wireframes/draft-builder-screen-hierarchy.md:68`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/wireframes/draft-builder-screen-hierarchy.md:98`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-09-functional-mockup-draft-builder-screen.md:36`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-09-functional-mockup-draft-builder-screen.md:39`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-09-functional-mockup-draft-builder-screen.md:47`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-09-functional-mockup-draft-builder-screen.md:49`

Task-08 correctly identifies that viewing a historical version must make the editor read-only, otherwise "Guardar cambios" becomes a silent restore path. But task-09, which consumes this hierarchy, still only requires `onViewVersion` to swap the editor fields and tests only that the editor reflects the selected version. Its save test remains generic and does not require the historical preview to disable/hide saving.

That leaves the new safety rule unenforced in the first executable task. An implementer can satisfy task-09 exactly as written while still allowing the silent restore flow task-08 says it resolved.

Recommendation: update task-09's steps and verification to require a historical-version fixture where selecting a past version makes `DraftEditorSection` read-only and prevents `onSave`, plus a current-version fixture where saving is enabled.

### P2 - The current-included version-list contract conflicts with the zero-prior-versions fixture

- Files:
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/wireframes/draft-builder-screen-hierarchy.md:55`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/wireframes/draft-builder-screen-hierarchy.md:57`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-09-functional-mockup-draft-builder-screen.md:37`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-09-functional-mockup-draft-builder-screen.md:51`

The hierarchy models `versions` as the full backend list with the current version included, and derives `isViewingHistoricalVersion` by comparing `selectedVersion` to the latest entry in `versions`. That matches the verified backend endpoint. Task-09 still asks for a zero-prior-versions case as an "empty version list, only the current draft."

Those contracts cannot both be true. If the executable mockup uses `versions: []`, the hierarchy's derived latest/current logic has no latest entry and the current row cannot be marked `(actual)`. If it uses the real backend contract, zero prior versions means a one-item versions list containing the current draft, not an empty list.

Recommendation: change task-09's fixture wording to "one-item version list containing only the current draft" or explicitly add defensive behavior for an impossible empty backend versions response. The former is cleaner because it keeps the mockup aligned with task-01/task-10.

### P2 - Downstream tasks still require 409 conflict handling after task-07/task-08 rule it out

- Files:
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/wireframes/draft-builder-screen.md:111`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-11-mutations-draft-builder-screen.md:52`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-11-mutations-draft-builder-screen.md:62`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-12-connect-real-api-draft-builder-screen.md:25`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-12-connect-real-api-draft-builder-screen.md:34`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-12-connect-real-api-draft-builder-screen.md:35`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-12-connect-real-api-draft-builder-screen.md:47`

Task-07 says the Draft Builder API cannot emit a 409 conflict and explicitly leaves that as a known concurrency risk so task-08/task-11 do not assume a response that will never arrive. I re-checked the local backend: the draft controller exposes generate/regenerate/update/current/versions endpoints, and `GlobalExceptionHandler` maps 409 only for `DuplicateEmailException`, not draft update/regenerate paths.

Task-11 and task-12 still require distinct 409 handling, tests, and translated UI messages. That will push future implementation toward dead error branches and tests for a nonexistent draft-builder contract.

Recommendation: update task-11/task-12 to remove 409 from Draft Builder save/regenerate acceptance, replacing it with the documented last-write-wins concurrency risk. Keep 404/422/500 plus 502/503 agent errors where applicable.

## Validation Notes

- `git diff --check story-02-assessment-screens-wireframes-and-data-providers...HEAD` passed.
- Reviewed the task-08 diff: story status update, task-08 verification summary, and new `wireframes/draft-builder-screen-hierarchy.md`.
- Cross-checked task-08 against task-07's wireframe, task-09's functional mockup plan, task-10/task-11/task-12 data/mutation/API wiring plans, PDR-001, and the frontend component hierarchy guide.
- Verified the backend assumptions against `/home/carlos/projects/grade-ops-ai/api`: `AssessmentController.java` has no restore endpoint; `GenerateAssessmentDraftResponse` contains no timestamp/edit marker; `UpdateAssessmentDraftRequest` is partial; `GlobalExceptionHandler.java` maps draft validation/application/agent errors but no draft-specific 409.
