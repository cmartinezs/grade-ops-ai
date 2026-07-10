# RECORD-EDGE-CASE

> [← README](README.md)

Records unexpected, corrective, risky, or non-linear events while a planning is being executed. These raw notes feed the final retrospective.

---

```mermaid
flowchart TD
    A[Unexpected event detected] --> B[Locate planning RETROSPECTIVE-RAW.md]
    B --> C{File exists?}
    C -- No --> D[Create from template structure]
    C -- Yes --> E[Read existing log]
    D --> F[Append raw entry]
    E --> F
    F --> G[Continue original command or stop with blocker]
```

---

## When To Record

Record an edge case when any command observes one of these:

- A story, task, validation, smoke test, git operation, or archive check is blocked.
- The user requests corrections after review.
- Scope changes, a story is skipped, split, merged, rolled back, or retried.
- A command creates recovery work that was not part of the original path.
- A warning or failure should inform future planning decisions.

Do not record normal progress, successful routine checks, or duplicate entries.

---

## Entry Format

Append a new entry under `## Log`, newest first:

```md
### YYYY-MM-DD HH:MM - <short title>

- **Source:** <command name or manual>
- **Related story/task:** <story-NN, task-NN, or none>
- **What happened:** <fact-based description>
- **Expected instead:** <what the plan or command expected>
- **Resolution:** <how it was fixed, deferred, or contained>
- **Retrospective signal:** <lesson, risk, follow-up, or decision candidate>
```

If some fields are unknown, write `unknown` rather than inventing details.

---

**Called by:** execution, validation, recovery, and archive commands when non-linear events occur.

---

> [← README](README.md)
