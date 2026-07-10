# Retrospective Raw Notes: [Planning Name]

> [← README](README.md)

Working log for events that were unexpected, corrective, risky, or useful for the final retrospective.

Use `/plan-edge-case <planning-id> -- <what happened>` to add manual entries. Commands may also append entries when they encounter blockers, corrections, skipped work, recovery actions, validation failures, or other non-linear events.

---

## How To Use This File

Capture facts while they are fresh. Do not polish entries here. The final retrospective belongs in `README.md`.

Each entry should answer as many of these as possible:

- What happened?
- What was expected instead?
- How was it resolved or contained?
- What should be carried forward?

---

## Log

<!-- Add newest entries at the top. -->

### 2026-07-10 01:49 - AssessmentResult scope narrower than docs/03-ai-agents/assessment-agent.md output contract

- **Source:** `/plan-task` — CHECK-AGNOSTIC-BOUNDARY
- **Related story/task:** story-01-assessment-agent, task-01-contracts
- **What happened:** `docs/03-ai-agents/assessment-agent.md`'s "Output Contract" JSON example describes a much richer shape (`summary`, `learning_objectives`, `student_instructions`, `allowed_resources`, `estimated_duration_minutes`, `difficulty`, `programming_language`, `expected_evidence`, `rubric_seed`, `warnings`, `uncertainty_flags`, `requires_teacher_approval`, `next_action`, `schema_version`) than the `AssessmentResult` record just implemented (`title`, `context`, `instructions`, `objectives`, `deliverables`, `constraints`), and uses different names for two overlapping fields (`learning_objectives`→`objectives`, `student_instructions`→`instructions`).
- **Expected instead:** the task's own Technical Design deliberately scoped `AssessmentResult` to exactly the fields listed in US-011's acceptance criteria ("title, context, instructions, objectives, expected deliverables, and constraints") and the story's Done Criteria — a narrower MVP contract than the aspirational one in `docs/03-ai-agents/assessment-agent.md`.
- **Resolution:** treated as a documentation gap, not an implementation error — the task file and source user stories (US-010/011/012) are the authoritative scope for this story, and `AssessmentResult` was kept as designed. Not fixed in this task; flagged here and in the story's `Inconsistencies Found` table for a deliberate decision on whether `docs/03-ai-agents/assessment-agent.md` should be narrowed to the MVP contract or the richer fields deferred/tracked as residuals.
- **Retrospective signal:** `docs/03-ai-agents/[agent-name].md` files may describe a longer-term/aspirational contract rather than the MVP slice actually atomized into tasks — worth reconciling before the next agent's contract task, or explicitly noting in each agent doc which fields are MVP vs. future.

### 2026-07-10 01:49 - Task branch name collided with story branch ref (git ref prefix conflict)

- **Source:** `/plan-task` — git branch setup (step 3c)
- **Related story/task:** story-01-assessment-agent, task-01-contracts
- **What happened:** `git checkout -b gradeops-agents/story-01-assessment-agent/task-01-contracts` failed with `cannot lock ref ... 'refs/heads/gradeops-agents/story-01-assessment-agent' exists; cannot create '.../task-01-contracts'` — git refs can't have a branch that is simultaneously a leaf and a directory prefix, and the story branch `gradeops-agents/story-01-assessment-agent` already occupies that path.
- **Expected instead:** the documented task-branch convention `<story-branch>/<task-NN-slug>` assumes the story branch name never needs to also be a path segment of a longer branch name.
- **Resolution:** asked the user; adopted `<story-branch>--task-NN-slug` (double-dash separator instead of a further `/`) for all task branches in this story, e.g. `gradeops-agents/story-01-assessment-agent--task-01-contracts`.
- **Retrospective signal:** this collision will recur for every task in every story under the `<worktree-prefix>/story-NN-<slug>` branch naming scheme — worth fixing the convention itself (e.g. always using `--task-NN` instead of `/task-NN`) in the planning template rather than re-deciding it per story.

---

> [← README](README.md)
