# Applied Version Migrations

## 2026-07-09 — 1.4.0 -> 3.5.0

- Migration chain:
  - `.planning/update-version/1-2.md`
  - `.planning/update-version/2-3.md`
- Mode: applied
- Summary:
  - Renamed 59 planning-unit files/folders (`scope-NN-name.md` / `scope-NN-name/` → `story-NN-name.md` / `story-NN-name/`, `EXECUTE-SCOPE.md`/`NEXT-SCOPE.md` → `EXECUTE-STORY.md`/`NEXT-STORY.md`) across `_template/`, `active/008-assessment-creation/`, and all 7 `finished/` plannings, preserving git history via `git mv`.
  - Bulk-replaced planning-unit terminology (`scope`/`Scope`/`scopes`/`Scopes` → `story`/`Story`/`stories`/`Stories`, `plan-scope*`/`doc-scope` → `plan-story*`/`doc-story`) across `.planning/`, excluding `update-version/` (kept as historical migration documentation).
  - Reverted 3 false-positive collisions the bulk substitution introduced, where "scope" carries its ordinary business meaning rather than the planning-unit sense: `## Approximate Scope` heading (12 occurrences across `GLOSSARY.md`, `CREATE-PLANNING.md`, `TUTORIAL/flow-01-epic.md`, `_template/00-initial.md`, and every planning's `00-initial.md`), "scope creep" idiom in `MILESTONE-FEEDBACK.md`, and "cascade scope" in `CASCADE-CHANGE.md`.
  - Brought the workspace to the 3.5.0 baseline: added net-new files (`config.yml`, `SMOKE-TESTS.md`, `TUTORIAL/flow-05-autonomous.md`, `flow-06-smoke-config.md`, `WORKFLOWS/02-EXECUTION-WORKFLOWS/ATOMIZE-STORY.md`, `WORKFLOWS/03-MAINTENANCE-WORKFLOWS/RECORD-EDGE-CASE.md`/`SUPERSEDE-PLANNING.md`, `WORKFLOWS/04-SUB-WORKFLOWS/CHECK-ATOMICITY.md`/`CHECK-PLANNING-CONTEXT.md`/`CHECK-STORY-CONTEXT.md`, `WORKFLOWS/06-PROJECT-GUIDANCE/`, `_template/02-deepening/task-NN-name.md`, `_template/RETROSPECTIVE-RAW.md`); wholesale-replaced pure generic framework/reference files with zero project customization (`PROMPTING.md`, `GLOSSARY.md`, `CREATE-PLANNING.md`, several `WORKFLOWS/*/README.md`, `EXECUTE-STORY.md`, `NEXT-STORY.md`, all of `_template/` except `TRACEABILITY.md`/`pdr-NNN-title.md`, all of `TUTORIAL/`); merged `GUIDE.md` (adopted new sections — workspace boundary, layered git branch cleanup, monorepo coordination, Superseded Plannings — while re-inserting this project's `AG/AP/DO/IN/WB/W` area table); merged `WORKFLOWS/04-SUB-WORKFLOWS/README.md` (added the 3 new sub-workflow rows while preserving this project's 3 pre-existing custom rows: `CHECK-PHASE5-CHAIN`, `CHECK-DEVWORKFLOW-CONSISTENCY`, `CHECK-VERSIONING-ALIGNMENT`); added the `update-version/` cross-reference line to root `README.md`.
  - Set `config.yml → git.base_branch` to `master` (this repo's actual main branch), overriding the template's `main` default.
  - Fixed `active/008-assessment-creation/README.md`, which had been created (this session, pre-migration) from the old `_template/README.md` — that file's purpose changed in 3.5.0 from "meta-doc describing the template folder" to "the actual per-planning README template" (Overview/Key Links/Current State/Retrospective). Rewrote it with real content for this planning and added the missing `RETROSPECTIVE-RAW.md`.
  - Updated `active/008-assessment-creation/01-expansion.md` and its 3 story files to the 3.5.0 story/task status vocabulary (`PENDING` → `TODO`), and added `Risk`, `Linked Child Plannings`, and `External Issue Mapping` sections matching the new templates. `Linked Child Plannings` is marked N/A — this monorepo's sub-repos have no independent `.planning/` workspaces.
- Verification:
  - Re-ran both migrations' residual `rg` search patterns — clean (no remaining `scope`-as-planning-unit or missing-3.5.0-command references outside `update-version/`).
  - Confirmed zero remaining `scope-*` files/folders under any `02-deepening/`.
  - Confirmed zero broken internal links to old `scope-*` paths.
  - Spot-checked that `finished/001-teacher-onboarding/01-expansion.md`'s stale `PENDING` entries in its Story Summary table (a pre-existing quirk — the table was never updated to `DONE` even though the planning completed) were deliberately left untouched, since finished plannings are archived/read-only historical record, not something this migration should rewrite.
- Residual follow-up:
  - `SMOKE-TESTS.md` was added as the generic unfilled template (`[fill in]` placeholders) — run `/plan-smoke-config` to generate the actual stack-specific smoke-test plan for this monorepo (web/api/agents).
  - Three sub-workflow files exist locally with no plugin-template counterpart (`CHECK-PHASE5-CHAIN.md`, `CHECK-DEVWORKFLOW-CONSISTENCY.md`, `CHECK-VERSIONING-ALIGNMENT.md`). Left in place — no evidence they're obsolete, and the migration does not instruct removing anything not explicitly superseded.
  - `finished/` plannings' internal `PENDING` status mentions (13 occurrences, all in already-completed/archived stories) were left as historical record and not renamed to `TODO`.
