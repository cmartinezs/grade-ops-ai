# Applied Migrations / Template Syncs

## 2026-07-15 — plugin 3.6.0 -> 3.10.0 template sync

- Trigger: `claude plugin update claude-planning-with-ai@cmartinezs` (3.6.0 -> 3.10.0), followed by `/plan-update-version` (no `<from> <to>` version bump existed — this workspace was already on template major version 3, confirmed via `2-3.md`'s discovery search finding zero legacy `scope`-era references) and a manual diff against `~/.claude/plugins/cache/cmartinezs/claude-planning-with-ai/3.10.0/planning-template/`.
- Mode: applied
- Summary:
  - Added `.planning/scripts/planning-atomize.mjs` (previously missing entirely — powers `/plan-atomize`).
  - Refreshed `.planning/scripts/planning-init.mjs` and `.planning/scripts/planning-story.mjs` (the latter gained the `execute-inspect|start|done|finalize` subcommands used by `/plan-story`).
  - Refreshed `.planning/update-version/2-3.md` and `update-version/README.md` (previously targeted a stale 3.6.0 baseline; now cover the full v3.7-v3.10 change set).
  - Refreshed `.planning/GLOSSARY.md`, `_template/*.md`, `TUTORIAL/{README,reference}.md`, and the affected `WORKFLOWS/*.md` files with the `/plan-decision`/PDR, logging-policy, and test-suite additions from v3.7.0-v3.10.0.
  - Merged (not overwritten) `.planning/README.md` (added LOGGING.md/generate-test-suite.sh pointers), `GUIDE.md` (added LOGGING.md/TEST-SUITE.md tree entries and PDR note, kept the `WB` area row), `config.yml` (added `test_suite_generator`/`logging_file` keys, kept `base_branch: develop`), `WORKFLOWS/05-SDLC-PHASE-GUIDANCE/README.md` and `_template/TRACEABILITY.md` (kept the `WB` area row/column).
  - Removed stray leftover `_template/pdr-NNN-title.md` (superseded by root `PDR-TEMPLATE.md` in the current template layout).
- Verification:
  - `node --check` on every file under `.planning/scripts/*.mjs` — all pass.
  - `node .planning/scripts/planning-story.mjs planning-inspect 001-assessment-creation` — both stories still resolve correctly after the script refresh.
  - Re-ran the `2-3.md` discovery `rg` search — no legacy `scope`/`doc-scope` references found.
- Residual follow-up:
  - `.planning/SMOKE-TESTS.md` still has generic `[fill in the command...]` placeholders rather than this project's actual Next.js/npm commands — left untouched since filling it in is `/plan-smoke-config`'s job, not a template-sync concern.

## 2026-07-15 — plugin 3.10.0 -> 3.10.2 script fixes

- Trigger: two more `claude plugin update` runs (3.10.0 -> 3.10.1 -> 3.10.2) shipping fixes for bugs reported during the sync above.
- Mode: applied
- Summary:
  - 3.10.1 fixed the `row.depends-on` invalid-JS crash (`ReferenceError: on is not defined`) in both `planning-check.mjs` and `planning-report.mjs` — changed to `row['depends-on']`. Synced both files.
  - 3.10.2 fixed a second bug the crash had been masking: `storyIdFromValue(row.story)` could never match the Story Summary table's own `| # | Story | ... |` format, because the `Story` column holds a plain slug (e.g. `assessment-creation-ui`) with no `story-\d+` substring — every story row false-failed `/plan-validate` with "story file has no row in Story Summary". Fixed via a new `storyIdFromSummaryRow(row)` that reads the numeric `#` column first (`row['']`, then `row.cells[0]`) before falling back to pattern-matching. Synced `planning-check.mjs` and `planning-report.mjs`.
- Verification:
  - `node --check` on both files — pass.
  - `node .planning/scripts/planning-check.mjs validate 001-assessment-creation` — now reports `PASS: no structural issues found` (previously crashed, then false-failed).
  - `node .planning/scripts/planning-report.mjs status 001-assessment-creation` — renders correctly.
- Residual follow-up:
  - `planning-report.mjs status`'s Story Detail table shows `Area: -` for both stories instead of `WB` — not investigated further, low priority, not blocking. Fixed in 3.10.3, see below.

## 2026-07-15 — plugin 3.10.2 -> 3.10.3 script fix

- Trigger: another `claude plugin update` shipping the Area-column fix flagged above.
- Mode: applied
- Summary: `planning-report.mjs` gained `areaFromSummaryRow(row)`, which reads `row.area`/`repository-area`/`sdlc-phase-s`/`phase`/`cells[2]` (with a `meaningfulValue()` guard against `-`/`—`/`none`/`n/a`) instead of the bare `row.area || ''` that never matched our table's `sdlc-phase-s` column. Also now backfills `row.area` from the story file's own `## Area` section when the summary row lacks it. Synced `planning-report.mjs`.
- Verification:
  - `node --check` — pass.
  - `node .planning/scripts/planning-report.mjs status 001-assessment-creation` — Story Detail table now shows `Area: WB` for both stories.
  - `node .planning/scripts/planning-check.mjs validate 001-assessment-creation` — still `PASS: no structural issues found`.
- Residual follow-up: none.
