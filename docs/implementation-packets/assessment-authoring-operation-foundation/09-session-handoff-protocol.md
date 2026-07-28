<a id="top"></a>

# 09 — Session Handoff Protocol

**Parent:** [README](README.md) · **Status:** Ready · **Prev:** [08 — Risk and Decision Ledger](08-risk-and-decision-ledger.md)

## Branch topology

```text
develop
  └── feat/assessment-authoring-operation-foundation        (integration branch — Session D's target)
        ├── feat/assessment-authoring-operation-foundation-api      (Session A)
        ├── feat/assessment-authoring-operation-foundation-agents   (Session B)
        └── feat/assessment-authoring-operation-foundation-web      (Session C)
```

- **Origin commit for the integration branch:** `develop`, pulled fresh (`git switch develop && git pull --ff-only`) at the moment it is created. It is created **once**, by whichever of Sessions A/B/C starts first — check `git ls-remote origin feat/assessment-authoring-operation-foundation` before creating it; if it already exists, fetch and branch from it instead of recreating it.
- **Which session completes first is not fixed** — Sessions A and B may start in parallel per [05 — Execution Order](05-execution-order.md); Session C may start early against mocks. Whichever session finishes and pushes first does not block the others.
- **Who resolves conflicts between subrepo branches:** Session D, and only Session D, at integration time — see [FINAL-INTEGRATION-PROMPT.md](FINAL-INTEGRATION-PROMPT.md). Since `api/`, `agents/`, and `web/` are disjoint directory trees in this monorepo, file-level conflicts between the three subrepo branches are not expected; the real integration work is verifying the *contracts* line up (see [07 — Integration and Final Verification](07-integration-and-final-verification.md)), not resolving text conflicts.
- **When each session pushes:** immediately after its own local verification (full test suite for that workspace) is green and its `*-HANDOFF.md` is written — do not batch multiple sessions' work into one push. For API specifically, this means **four** pushes to the same `feat/assessment-authoring-operation-foundation-api` branch (one per sub-session A1–A4), not one — see [API sub-session handoffs](#api-sub-session-handoffs) below.
- **When the PR opens:** only Session D opens the one functional PR, from the integration branch to `develop`, after merging all three subrepo branches into it and completing the checks in [07 — Integration and Final Verification](07-integration-and-final-verification.md). No session opens an intermediate PR for its own subrepo branch — see [Alternative: worktrees](#alternative-git-worktrees-for-concurrent-local-sessions) if multiple sessions run on one machine concurrently and need isolated working directories without separate PRs.

## API sub-session handoffs

Session A (API) is itself four sequential sub-sessions — A1, A2, A3, A4 — all on the **same** `feat/assessment-authoring-operation-foundation-api` branch (no `-a1`/`-a2`/`-a3`/`-a4` suffixed branches). Each sub-session's handoff is a gate the next sub-session checks before starting:

```text
A1 → API-A1-HANDOFF.md → A2 → API-A2-HANDOFF.md → A3 → API-A3-HANDOFF.md → A4 → API-A4-HANDOFF.md
                                                                                  + consolidated HANDOFF.md
```

A sub-session must not proceed past its own preflight if the previous one's handoff is missing, incomplete, records a HEAD that doesn't match the branch's actual HEAD, its recorded test run wasn't actually green (re-run it, don't trust the number), or it flags a critical blocker. Each intermediate handoff (`API-A1-HANDOFF.md` through `API-A3-HANDOFF.md`) uses the same required-sections discipline as the cross-workspace `*-HANDOFF.md` format below, specialized with a `Session`/`Starting commit` pair so continuity across sub-sessions is checkable, not assumed — see each template in the [API local packet](../../../api/docs/implementation-packets/assessment-authoring-operation-foundation/README.md) for its exact fields.

**Only Session A4 produces the document Session D reads**: `HANDOFF.md`, filled in as the final, consolidated `API-HANDOFF.md` summarizing all four sub-sessions' commits, tasks, migrations, endpoints, and residual risks into one view. Session D validates this consolidated handoff's internal consistency against the four intermediate ones (continuity of commits, no gap between sub-sessions, tests green at every stage) as part of its own integration checklist — see [07 — Integration and Final Verification](07-integration-and-final-verification.md).

## This coordination packet's own branch

This documentation-only packet lives on `docs/assessment-authoring-execution-packets`, branched from `develop` (not from the integration branch above — the packets describing the split must exist and be merged to `develop` *before* any implementation branch starts, since they are Phase 0's exit criterion per [05 — Execution Order](05-execution-order.md)). It is a separate PR from the functional one Session D eventually opens.

## `*-HANDOFF.md` format

Each local packet's `HANDOFF.md` (filled in by that session as `API-HANDOFF.md`/`AGENTS-HANDOFF.md`/`WEB-HANDOFF.md` once work is done — the template lives in the packet, the filled version is what gets committed) must contain the sections below. **API is the one exception**, per [API sub-session handoffs](#api-sub-session-handoffs) above: its `HANDOFF.md`/`API-HANDOFF.md` is filled in by sub-session A4 as a *consolidation* of four prior intermediate handoffs, not by a single session describing its own work — the same required sections apply, summarized across all four sub-sessions instead of one.

```markdown
# {Workspace} Handoff — Assessment Authoring Operation Foundation

## Commits
<hash> <message>
...

## Branch
feat/assessment-authoring-operation-foundation-{workspace}, pushed to origin at <commit>

## Tasks completed
<task id> — <one-line status>

## Test results
<verification command> → <PASS/FAIL, test count>

## Contract changes
<any field/endpoint/status/failure-code that differs from what 02/03 in the root packet specified,
 with justification — empty section if none>

## Assumptions
<anything this session had to decide because the frozen contract underspecified it>

## Blockers
<anything unresolved — empty section if none>

## Integration instructions
<what Session D specifically needs to know to merge and verify this workspace's work>
```

Session D refuses to start integration if any of the three handoffs is missing a required section — an empty "Blockers"/"Contract changes" section is fine (it means none), a missing section is not.

## Alternative: git worktrees for concurrent local sessions

If Sessions A/B/C run concurrently on one machine, plain branch switching in a single working directory does not work. Use `git worktree` instead, with paths distinct from the reserved `.planning/GUIDE.md` child-planning worktree names already used elsewhere in this repository (`../gradeops-api`, `../gradeops-agents` are reserved for that separate system per the root `CLAUDE.md` — do not reuse them here):

```bash
git worktree add ../gradeops-authoring-api    feat/assessment-authoring-operation-foundation-api
git worktree add ../gradeops-authoring-agents feat/assessment-authoring-operation-foundation-agents
git worktree add ../gradeops-authoring-web    feat/assessment-authoring-operation-foundation-web
```

Each session then opens only its own worktree path and never touches the other two directories.

---

← [08 — Risk and Decision Ledger](08-risk-and-decision-ledger.md) | [↑ inicio](#top) | [↑ README](README.md)
