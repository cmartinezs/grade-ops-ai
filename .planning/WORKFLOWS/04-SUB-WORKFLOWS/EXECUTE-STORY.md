# [EXECUTE-STORY]

> [← README](README.md)

Validates the done criteria of the current story before advancing.

---

## Steps

1. Open the current story file in `02-deepening/`.
1b. **Task file audit:** For each row in the `## Tasks` table, verify that its corresponding `task-NN-*.md` file exists under the story's subfolder (`02-deepening/<story-id>-*/`) and has `Status: DONE`. If any file is missing or not `DONE`, return `BLOCKED` listing which files are absent or incomplete. A story cannot pass this check on the basis of the table alone — the individual task files are the source of truth.
2. Read the `Done Criteria` section.
3. For each criterion: verify it is satisfied (file exists, section is present, link is valid, etc.).
4. If all criteria are met: return `DONE`.
5. If any criterion is not met: list the unmet criteria and return `BLOCKED`.

---

**Called by:** [`ADVANCE-PLANNING`](../01-PLANNING-WORKFLOWS/ADVANCE-PLANNING.md)

---

> [← README](README.md)
