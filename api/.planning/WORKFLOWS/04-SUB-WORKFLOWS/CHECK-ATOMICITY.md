# [CHECK-ATOMICITY]

> [← README](README.md)

Verifies that a task satisfies the atomicity requirements before it is created or executed. This is the authoritative definition of what makes a task **atomic**.

---

## Atomicity requirements

A task is atomic when **all** of the following hold:

| # | Requirement | Verification |
|---|------------|--------------|
| 1 | **Single deliverable** — produces exactly one verifiable output (a module, an endpoint, a migration, a document section) | The Objective names one output, not a list |
| 2 | **Independently executable** — can be completed without waiting on undone work, or declares explicit dependencies | `Depends On` lists task IDs or `—` |
| 3 | **Technical design included** — approach, affected files, and interfaces are decided before implementation | Technical Design section has no placeholders |
| 4 | **Concrete implementation steps** — ordered steps naming real files or components | No abstract verbs ("handle", "manage") without a target file |
| 5 | **Unit test plan** — cases, expected results, and test location are defined | Unit Tests table has at least one row |
| 6 | **Binary done criteria** — each criterion is pass/fail verifiable, no judgment calls | Every criterion can be answered yes/no |
| 7 | **Workflow reference** — execution is governed by a workflow ID from the catalog | `Workflow` value exists in `WORKFLOWS/README.md` |
| 8 | **Session-sized** — completable in one focused work session | If it needs more, it hides more than one deliverable |

---

## Steps

1. Read the task (candidate description or existing `task-NN-*.md` file).
2. Evaluate each requirement in the table above.
3. If all 8 pass: return `PASS`.
4. If requirement 1 or 8 fails: return `REJECTED — too large` (the task must be split).
5. If the deliverable cannot be verified on its own: return `REJECTED — fragment` (the task must be merged with its counterpart).
6. For any other failure: return `REJECTED — incomplete`, listing the failing requirements.

---

**Called by:** [`ATOMIZE-STORY`](../02-EXECUTION-WORKFLOWS/ATOMIZE-STORY.md), `/plan-task-validate`

---

> [← README](README.md)
