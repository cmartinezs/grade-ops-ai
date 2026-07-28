# Research 03 — Baseline Technical Verification

**Repository:** `cmartinezs/grade-ops-ai`
**Branch:** `design`
**Verified commit (HEAD):** `ce3d31b37d8eb63fcf0c91fa04d724ade3a96e6d` ("docs: mark master plan for redesign reconciliation")
**Verification date:** 2026-07-28
**Relationship to Research 01/02:** this is not a re-audit. Research 01/02 were static reviews that never executed a build, test, or migration. This report actually runs the repository's real verification commands against current `HEAD` and records evidence, closing the "not executed" limitation both prior reports explicitly stated (Research 01 §2.4, Research 02 §2.4).

---

## 1. Snapshot validation — are Research 01/02 still valid against current `HEAD`?

| Artifact | Path | Audited commit | Current commit | Still valid | Notes |
|---|---|---|---|---|---|
| Research 01 | `design-system/research/research-01-current-domain-data-model-alignment-audit.md` | `e6ab06de496ffb3bde7fc9774503f106ac5e36d0` | `ce3d31b37d8eb63fcf0c91fa04d724ade3a96e6d` | **Yes** | `e6ab06d` is a true ancestor of current `HEAD` (`git merge-base --is-ancestor e6ab06d HEAD` → true). |
| Research 02 | `design-system/research/research-02-workflow-lifecycle-alignment-audit.md` | `39f7c7dbbea7c7f7b77cd5a16ed0511d77d751f1` | `ce3d31b37d8eb63fcf0c91fa04d724ade3a96e6d` | **Yes, with a topology note** | `39f7c7d` is **not** an ancestor of current `HEAD` — it is the PR #94 merge commit on `origin/develop`, a sibling branch tip that diverged from the `design` line at `e6ab06d`. `git merge-base(39f7c7d, HEAD) = e6ab06d`. Research 02 itself already recorded that `39f7c7d` and `e6ab06d` are functionally identical trees ("`compare(e6ab06d, 39f7c7d)` reporta `ahead_by = 1`; `files = []`"), so this is not a divergence in evidence — both reports are effectively anchored to the same tree, `e6ab06d`. |

### What changed between `e6ab06d` and current `HEAD`

`git diff --stat e6ab06d HEAD` shows changes **exclusively under `docs/`** (business model, curriculum-structure, mvp-scope, workflows, data-model, screen-inventory, teacher-workspace-ux, 99-decisions, `docs/CLAUDE.md`, `docs/README.md`, `docs/master-plan/README.md` — 56 files, all documentation). **Zero files changed under `api/`, `agents/`, or `web/`.**

This means: every code-level finding in Research 01 and Research 02 — domain classes, persistence entities, migrations, use cases, controllers, tests, workflows, concurrency gaps — remains accurate against current `HEAD` without amendment. Spot-checked directly against current `HEAD` for this report (not re-derived from the research prose): `Assessment.java`, `AssessmentDraft.java`, `AssessmentStatus.java`, `AgentExecutionLog.java`, `DraftGenerationCoordinator.java`, `GenerateAssessmentDraftHandler.java`, `UpdateAssessmentDraftHandler.java`, `AssessmentController.java`, `AssessmentAgentOrchestrator.java`, `AssessmentCommand.java`, and Flyway `V9`–`V12` — all match Research 01/02's descriptions exactly, including the specific defects cited (in-place `applyEdit()`, `MAX(version_number)` current-draft derivation, external call before durable evidence, `provider = agentCommand.provider()` persisting client-supplied `null` rather than the resolved value).

The documentation changes are the "redesign reconciliation" work referenced by this branch's own commit history (`docs: align … with redesign` series) and are consistent with, not contradictory to, both research reports' target-state descriptions. One specific item Research 02 flagged as needing correction — the obsolete "Assessment Lifecycle (Unified)" block in `docs/02-product/workflows.md` — **has already been removed** in this reconciliation (`grep -i "unified" docs/02-product/workflows.md` returns no matches on current `HEAD`). No addendum is required for this; it is noted here as confirmation, not as new work.

**Conclusion: no findings in Research 01 or Research 02 are invalidated by code drift.** No addendum was necessary because there was no code drift to reconcile — only documentation catch-up, already completed.

---

## 2. Baseline command execution

Environment: Java 21.0.11 (Temurin), Node v24.15.0 / npm 11.12.1, Docker 29.6.1 (daemon running). Full raw command logs were captured during this verification and are available on request; this table reports the results and relevant evidence, not full transcripts (see § 5, security note, for why raw `docker compose config` output specifically is not reproduced here).

### API (`api/`)

| Area | Command | Result | Duration | Relevant output | Blocker |
|---|---|---|---|---|---|
| Compile | `./mvnw clean compile` (no profile) | PASS | ~10s | Exit 0, no errors | none |
| Full test suite | `./mvnw test` (no profile) | PASS | 1m 18s | `Tests run: 289, Failures: 0, Errors: 0, Skipped: 0` | none |
| `AssessmentTest` | via full run | PASS | 0.011s | 9/9 | none |
| `AssessmentDraftTest` | via full run | PASS | 0.024s | 25/25 | none |
| `DraftGenerationCoordinatorTest` | via full run | PASS | 0.224s | 4/4 | none |
| `AssessmentPersistenceFkChainIntegrationTest` | via full run | PASS | 3.9s | 4/4, real Testcontainers `postgres:16-alpine` | none |
| `GenerateAssessmentDraftHandlerIntegrationTest` | via full run | PASS | 4.5s | 2/2, Testcontainers | none |
| `RegenerateAssessmentDraftHandlerIntegrationTest` | via full run | PASS | 0.35s | 1/1, Testcontainers | none |
| `UpdateAssessmentDraftHandlerIntegrationTest` | via full run | PASS | 3.5s | 2/2, Testcontainers | none |
| Flyway empty-DB migration | implicit, via every Testcontainers test | PASS | n/a | Each of the 11 `*IntegrationTest` classes independently boots a fresh `postgres:16-alpine` container and logs `Successfully applied 12 migrations to schema "public", now at version v12`. No single dedicated `FlywayMigrationTest` asserts this in isolation, but it is proven repeatedly and independently. | none |
| Static analysis / coverage | grep for Jacoco/Checkstyle/SpotBugs/PMD/Sonar in `api/pom.xml` | NOT_CONFIGURED | n/a | No such plugin declared | none |

### Agents (`agents/`)

| Area | Command | Result | Duration | Relevant output | Blocker |
|---|---|---|---|---|---|
| Compile, no profile | `./mvnw clean compile` | FAIL (by design) | 3.4s | 20 errors, all `org.springframework.ai.*`/`ChatClient`/`ChatModel` missing — confirms `CLAUDE.md`'s documented `-Pdemo`/`-Pbeta` requirement empirically, not just by reading the doc | Spring AI starters are profile-gated (intentional, documented) |
| Compile, `-Pdemo` | `./mvnw -Pdemo clean compile` | PASS | ~6s | Exit 0 | none |
| Full test suite, `-Pdemo` | `./mvnw -Pdemo test` | PASS | 11.6s | `Tests run: 32, Failures: 0, Errors: 0, Skipped: 0`, 9 test classes | none |
| Live-provider-key requirement | inspection of `application-test.yml` + adapter tests | NOT_REQUIRED | n/a | Test profile excludes all Google GenAI/OpenAI autoconfiguration; `GeminiAssessmentGenerationAdapterTest`/`GroqAssessmentGenerationAdapterTest` mock `ChatClient`. No `GRADEOPS_GROQ_API_KEY`/`GRADEOPS_GEMINI_API_KEY` present in the verification shell; suite ran fully offline. | none |

*Methodology note:* the first no-profile compile attempt used a non-`clean` `compile`, which reused stale `-Pdemo`-built `target/classes` and produced a false `PASS`. Re-run with `clean compile` reproduced the real, documented profile-gated failure. Anyone re-running this baseline should always `clean` when testing profile-gating, or Maven's incremental compiler will mask it.

### Web (`web/`)

| Area | Command | Result | Duration | Relevant output | Blocker |
|---|---|---|---|---|---|
| Install | `npm ci` | PASS | 33.5s | Clean reinstall from lockfile, exit 0 | none |
| Lint | `npm run lint` (`next lint`) | PASS | 6.0s | 0 warnings/errors (tool itself is deprecated — see § 5) | none |
| Unit/component tests | `npm run test` (Jest) | PASS | 10.5s | 23/23 suites, 153/153 tests | none |
| Typecheck + production build | `npm run build` (`next build`) | PASS | 38.0s | Type checking passed, 16/16 routes generated, no errors | none |
| E2E | `npm run test:e2e` (Playwright) | NOT_EXECUTABLE | n/a | Requires an orchestrated `api/` + PostgreSQL + Firebase Auth Emulator stack via `web/scripts/e2e-test.sh`, plus the system Chrome channel in this environment — not a bare-command target. Correctly out of scope for a static baseline pass; not attempted. | Multi-service stack not started (by design for this baseline) |

### Repository-level

| Area | Command | Result | Duration | Relevant output | Blocker |
|---|---|---|---|---|---|
| Root Compose | `docker compose -f compose.yml config` | PASS | <1s | Parses cleanly (see § 5 — raw output not reproduced) | none |
| API Compose | `docker compose -f api/compose.yml config` | PASS | <1s | Single `postgres:16-alpine` service | none |
| API smoke Compose | `docker compose -f api/compose.smoke.yml config` | PASS | <1s | `firebase-emulator` + `postgres` | none |
| CI parity review | read `.github/workflows/*.yml` (not executed) | INFORMATIONAL | n/a | `api.yml` runs `./mvnw test` (no profile) — matches this baseline exactly. `agents.yml` runs `./mvnw test -Pdemo` — matches exactly. `deploy.yml` builds and deploys `site/` (a separate static docs/marketing project) to GitHub Pages — it is **not** `web/`. | See § 4 |

## 3. Working tree changes made during verification

**None.** Every command either passed outright or failed in an expected, already-documented way (`agents/` no-profile compile). No lockfile, `.env`, or config fix was necessary — `npm ci` reproduced cleanly, and `web/.env.local` was already present. No commit is associated with this section because there is nothing to commit.

## 4. Gaps found (informational, not blockers for this cut)

1. **No CI workflow exercises `web/`.** `.github/workflows/` contains `api.yml`, `agents.yml`, and `deploy.yml` (the last deploys `site/`, an unrelated static docs project, not `web/`). `web/`'s lint/test/build are currently exercised only by whoever runs them locally — this baseline run is, as far as this verification could determine, the only record of them passing. This is a pre-existing gap, not introduced by this effort; it is out of scope to fix here (adding CI workflows is not part of the Assessment Authoring Operation Foundation cut) but is worth a follow-up ticket.
2. **`next lint` is deprecated** (Next.js 15.5.19 warns it will be removed in Next.js 16, suggesting a codemod migration to plain ESLint). Not a failure today; flagged for whoever next touches `web/` tooling.
3. **No static analysis or coverage plugin** (Jacoco/Checkstyle/SpotBugs/PMD/Sonar) is configured in `api/` or `agents/`. Per this task's fix policy, no such tooling was invented or run — reported as `NOT_CONFIGURED`, not `FAIL`.

None of these three gaps block or are addressed by the Assessment Authoring Operation Foundation plan; they are pre-existing repository-maturity items outside this cut's scope.

## 5. Security note

Running `docker compose -f compose.yml config` against the root `compose.yml` resolves `.env` variable interpolation and prints the *values* of `GRADEOPS_GROQ_API_KEY` and `INTERNAL_API_SECRET` from the local `.env` file into command output — this is standard `docker compose config` behavior, not a defect in this repository's Compose file, but it means running that exact command against a real `.env` prints live secrets to whatever captures the output. No secret value is reproduced anywhere in this report or in this branch's history. **Recommendation: treat the `GRADEOPS_GROQ_API_KEY` in the local `.env` used during this verification as potentially exposed to this session's tooling and rotate it if there is any chance this transcript is stored or shared outside a trusted context.** This is an operational recommendation for the repository owner, not an action this documentation effort took on their behalf.

## 6. Conclusion

The baseline is green. `api/` and `agents/` (with the documented, intentional `-Pdemo` requirement) both compile and pass their full test suites, including real Testcontainers/PostgreSQL integration coverage proving Flyway migrates cleanly from an empty database. `web/` lints, tests, typechecks, and builds cleanly. No fix was required to reach this state — the repository was already reproducible. This baseline is the evidence floor the [Assessment Authoring Operation Foundation implementation plan](../../docs/implementation-plans/assessment-authoring-operation-foundation/README.md) builds on: every task in that plan is expected to keep this exact baseline green, plus add new passing coverage for the invariants the four [2026-07-28 ADRs](../../docs/99-decisions/README.md) introduce.

---

[← Research 02](research-02-workflow-lifecycle-alignment-audit.md) · [Research 01](research-01-current-domain-data-model-alignment-audit.md) · [Implementation Plan →](../../docs/implementation-plans/assessment-authoring-operation-foundation/README.md)
