# Applied Migrations / Template Syncs

## 2026-07-17 — plugin 3.10.3 -> 3.10.10 script sync

- Trigger: `/plan-update-version 3.10.3 3.10.10`. `node .planning/scripts/update-version.mjs 3.10.3 3.10.10` refused with "Patch and minor updates do not require an update-version migration" (same major version, no migration file exists or is needed) — so this was a direct script sync from `/home/carlos/projects/claude-planning-with-ai/planning-template/scripts/`, not a markdown migration. Scope: `.planning/scripts/` only, per the user's specific request (fixing known `planning-task.mjs` bugs reported to the plugin author in a prior session) — broader template docs (`GUIDE.md`, `WORKFLOWS/*`, etc.) were **not** diffed or synced this round.
- Mode: applied
- Summary:
  - Diffed all 17 files in `planning-template/scripts/` against `.planning/scripts/`; 6 differed and were copied over as-is: `generate-test-suite.sh`, `planning-atomize.mjs`, `planning-check.mjs`, `planning-done.mjs`, `planning-story.mjs`, `planning-task.mjs`.
  - Confirms 2 bugs reported upstream this session are now fixed in `planning-task.mjs`: `gitSetupCommands()` no longer force-resets an existing story/task branch from `baseBranch` via `checkout -B` — it now checks `branchRefs()` first and only creates-from-base when the branch genuinely doesn't exist yet; `extractField()` no longer truncates a multi-line "Affected files / components" bullet list to just its first line — it now walks subsequent lines until a real boundary (blank line + non-indented content, a heading, or a same/shallower `- **field:**` bullet).
  - Found and fixed a **third**, previously unreported bug surfaced by this exact sync: `affectedFiles()`'s post-filter excluded any candidate containing a literal `[`, intended to strip leftover unclosed markdown-link syntax, but this also silently dropped legitimate file paths containing Next.js dynamic-route segments (e.g. `src/app/(protected)/assessments/[id]/draft/page.tsx`), which this planning's own `story-02` tasks 09/10/12 use. Fixed locally in `.planning/scripts/planning-task.mjs` by narrowing the exclusion to `](` (an actual leftover-link marker that never appears in a real path) instead of any `[`. **Not yet reported upstream** — should be added to the next handoff to the plugin author, alongside the two already-fixed bugs for reference.
- Verification:
  - `node --check` on all 5 synced `.mjs` files, `bash -n` on the synced `.sh` file — all pass.
  - `node .planning/scripts/planning-task.mjs inspect 001-assessment-creation story-02 task-09` — before the local `affectedFiles()` fix, silently dropped `src/app/(protected)/assessments/[id]/draft/page.tsx` from the 5-file list; after the fix, all 5 files are reported correctly.
  - `node .planning/scripts/planning-check.mjs validate 001-assessment-creation` — ran for informational residual-check purposes (see below); does not fail on anything related to the 3 script fixes above.
- Residual follow-up:
  - `planning-check.mjs validate` (itself one of the synced files) now enforces several new structural requirements that didn't exist when this planning's tasks were written under an older template version: a `## Frontend Design Plan` / `## Backend/API Design Plan` section per task, and a `### Test Execution Evidence` section per task. Every task in `story-02` (task-01 through task-14, most already `DONE` and merged) fails these new checks. **Deliberately not retroactively rewritten** — that would mean editing 14 already-completed, already-reviewed, already-merged task files well beyond the scope of this sync (which was specifically "fix the known script bugs," not "backfill the whole story to the newest template shape"). Left as a known, informational gap for the user to decide on separately.
  - `planning-check.mjs validate` also flags `task-04 depends on task-14, which is not an earlier task` and the same for `task-09` — a validator false-positive against a deliberate, explicitly documented design decision (`task-14` was inserted out of numeric order on purpose — see the `story-02` task table's own annotation: "Inserted out of numeric order — must run before task-04/task-09"). Not fixed here (out of scope for a script-only sync); worth mentioning to the plugin author if this rule doesn't already account for explicitly-annotated out-of-order insertions.

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
