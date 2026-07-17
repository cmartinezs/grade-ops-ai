# Code Review: task-07-wireframe-draft-builder-screen

Date: 2026-07-17
Scope: `task-07-wireframe-draft-builder-screen.md`, `wireframes/draft-builder-screen.md`, and the story task table update for the Draft Builder textual wireframe.

## Findings

### P2 - State inventory is internally inconsistent before downstream tasks consume it

- Files:
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/wireframes/draft-builder-screen.md:92`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/wireframes/draft-builder-screen.md:94`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/wireframes/draft-builder-screen.md:109`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/wireframes/draft-builder-screen.md:209`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-07-wireframe-draft-builder-screen.md:81`
  - `.planning/active/001-assessment-creation/02-deepening/story-02-assessment-screens-wireframes-and-data-providers/task-07-wireframe-draft-builder-screen.md:89`

The wireframe says it documents the real error surface for "3 endpoints" while listing four endpoints, and the state totals do not add up. The table contains 12 state rows: 3 structural states, 2 in-flight states, and 7 error states (`validation-on-save`, `empty-notes`, `no-prior-draft`, `agent-rejected`, `agent-down`, `404`, `500`). The prose then says "11 estados", "8 variantes de error", and later "6 de error real"; the task verification summary repeats "11" while listing seven distinct error labels.

This matters because `task-08`, `task-09`, and `task-10` are supposed to use this artifact as their source of truth for component hierarchy, mock state coverage, and data-facade state modeling. As written, an implementer can satisfy the table or the checklist but not both, and test coverage can easily miss one of the documented states.

Recommendation: normalize the wireframe and task summary to one explicit inventory. For example, either keep the current 12-row table and update the prose/checklist to `4 endpoints`, `12 states`, `7 error states`, or intentionally merge/remove one state and make the table match the claimed `11`.

## Re-review - 2026-07-17

### Findings

No findings. The previous P2 state-inventory mismatch is fixed.

### Validation Notes

- `wireframes/draft-builder-screen.md` now says the screen uses 4 endpoints, matching the listed endpoints.
- The state table still contains 12 rows, and the prose/checklist now consistently describes those as 3 structural states, 2 in-flight states, and 7 error states.
- The task verification summary now repeats the same `12` total and `7 distinct error states` breakdown, so `task-08`, `task-09`, and `task-10` have one consistent source of truth for downstream state coverage.
- A residual search found no stale `11 estados`, `6 de error`, `3 endpoints`, or `8 variantes de error` wording in the task-07 wireframe/task files.

### Verification

- `rg -n "11 estados|11 listados|6 de error|6 distinct|3 endpoints|8 variantes de error|States documented: 11|Final count: 3 structural.*6" .../task-07-wireframe-draft-builder-screen.md .../wireframes/draft-builder-screen.md` - no matches.
- Manual re-read of `wireframes/draft-builder-screen.md` lines 92-113 and 204-213.
- Manual re-read of `task-07-wireframe-draft-builder-screen.md` lines 70-96.

## Validation Notes

- Verified the key backend assumptions against the local `grade-ops-ai/api` checkout: the controller exposes draft generate/regenerate/update/current/versions endpoints, no restore endpoint; `GlobalExceptionHandler` maps only `DuplicateEmailException` to 409; `GenerateAssessmentDraftResponse` has no `createdAt`; and `AssessmentDraft.applyEdit(...)` preserves the original id/version/log/createdAt without an edited-by-teacher marker.
- No runtime tests were needed for this review because the task branch only changes planning/wireframe Markdown.

## Verification

- `git diff --name-status story-02-assessment-screens-wireframes-and-data-providers...HEAD` - reviewed the three task-07 planning artifact changes.
- Manual cross-check against `docs/gradeops-ai-frontend-guidelines/02-ux-wireframes-y-maquetas.md` §2, §3, §9, and §10.
- Manual cross-check against `/home/carlos/projects/grade-ops-ai/api` source files for draft response fields, endpoints, exception mappings, ownership 404 behavior, no-prior-draft behavior, and edit metadata.
