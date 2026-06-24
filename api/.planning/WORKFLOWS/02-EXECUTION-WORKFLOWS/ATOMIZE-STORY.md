# ATOMIZE-STORY

> [← README](README.md)

Decompose a story in DEEPENING into **atomic tasks**: granular, directly implementable units, each with its own technical design, implementation steps, unit test plan, and done criteria.

---

## When to use

A story's task table describes *what* must happen but the rows are too coarse to execute directly — they hide design decisions, multiple deliverables, or untested work. Atomizing turns each unit of work into one file under `02-deepening/story-NN-name/` that can be picked up and completed in a single session.

---

## Steps

1. Read the story file completely: objective, tasks table, done criteria, repository area.
2. Read `00-initial.md`, `01-expansion.md`, and the relevant `docs/` contracts for context.
3. Derive candidate atomic tasks from the story's objective and existing task rows. Each candidate must target **exactly one verifiable deliverable**.
4. For each candidate, execute `[CHECK-ATOMICITY]`:
   - `REJECTED — too large`: split the candidate into smaller tasks.
   - `REJECTED — fragment`: merge it with the task it cannot be verified without.
   - Repeat until every candidate returns `PASS`.
5. Order the tasks so every `Depends On` reference points only to a lower-numbered task.
6. Create the folder `02-deepening/story-NN-name/` (same name as the story file, without `.md`).
7. For each task, create `task-NN-slug.md` from the template (`_template/02-deepening/task-NN-name.md`), filling **all** sections: Objective, Technical Design, Implementation Steps, Unit Tests, Done Criteria, Workflow, Depends On.
8. Rewrite the story's `## Tasks` table as an **index**: each row's task name becomes a link to its task file. Keep the `Workflow`, `Status`, and `Output` columns in sync with the task files.
9. Execute `[CHECK-TRACEABILITY]` — register any new domain terms introduced by the decomposition.

---

## Output

- One `task-NN-slug.md` file per atomic task under `02-deepening/story-NN-name/`.
- The story's `## Tasks` table converted into an index of those files.
- Story status is **not** changed by this workflow.

---

**Called by:** `/plan-atomize`

---

> [← README](README.md)
