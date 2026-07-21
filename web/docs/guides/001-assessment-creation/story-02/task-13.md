# End To End Connection

**Source:** task-13 | **Area:** unknown | **Date:** 2026-07-21

## What it does
The full Intake → Draft Builder flow works end-to-end against a real local `api/` instance with real ids, no fake/mock data remains anywhere in `src/features/assessment-creation/`, and story-01's Done Criteria that this story's scope covers are demonstrably satisfiable on top of it.

---

## How to use it
- `grep -r` for leftover fake-data markers (`setTimeout`, hardcoded fixture objects, a stray `mocks/` directory) under `src/features/assessment-creation/`; remove or justify each hit.
- With `api/` running locally, manually walk the full flow: submit a real brief on `/assessments/new` → confirm redirect to `/assessments/{realId}/draft` → confirm the generated draft renders → edit a field and save → confirm the edit persists after a page refresh → regenerate with adjustment notes → confirm a new version appears and the version history shows the prior one.
- Confirm story-01's Done Criteria that fall within this story's scope hold against the real flow just walked (not the mockups): required-field validation blocks submission; brief persists before the agent call; draft is fully editable and edits persist via the API; regeneration works with adjustment notes; previous versions remain accessible; draft and versions survive a page refresh.
- Run the full test suite and lint once more across everything this story touched.
- --

## Example
Use `With` through the public interface introduced by this task.
