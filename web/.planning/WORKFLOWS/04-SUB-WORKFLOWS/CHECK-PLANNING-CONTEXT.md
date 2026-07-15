# [CHECK-PLANNING-CONTEXT]

> [← README](README.md)

Validates the state of the planning system before launching a new planning execution. Detects in-progress work and offers safe alternatives before any context switch.

---

## Steps

1. Scan `.planning/active/*/02-deepening/*.md` for any story file whose status line contains `IN PROGRESS`.

2. If no in-progress stories are found: return **OK** — caller continues normally.

3. If in-progress stories exist:

   a. Collect for each:
      - Planning ID and name
      - Story ID and name
      - Git story branch derived from the story filename (`story-NN-<slug>`), or `<worktree-prefix>/story-NN-<slug>` when the planning runs in a dedicated child worktree
      - Any local task branches under that story branch (`story-NN-<slug>/*` or `<worktree-prefix>/story-NN-<slug>/*`)

   b. For each affected branch, gather git state:
      ```bash
      git status --short
      git log @{u}..HEAD --oneline 2>/dev/null || echo "(no upstream configured)"
      git stash list
      ```

   c. Present a structured report to the user:

      ```
      ⚠️  Planning context conflict detected

      In-progress work:
        Planning : <NNN-slug>
        Story    : <story-NN> — <story name>
      Story branch : <branch-name>
      Task branches: <task-branch-list or none>

      Git state on <branch-name>:
        Modified / staged : N files
        Untracked         : M files
        Unpushed commits  : P
        Stashes           : Q

      (repeat block for each additional in-progress story)
      ```

   d. Present the following options and wait for the user to choose:

      ```
      Choose how to proceed:

        A) Abort          — stay on the current planning; do not switch context
        B) Inspect        — show full `git diff` and story file contents; choose again afterwards
        C) Stabilize      — commit pending work as WIP, mark stories STANDBY, return to base branch
                            (recommended when you need to safely resume later)
        D) Proceed anyway — ignore in-progress work and start the new planning
                            (requires typing CONFIRMAR PROCEDER)
      ```

4. **If A (Abort):** return **ABORT** — caller must stop immediately without making any changes.

5. **If B (Inspect):**
   - Run `git diff` on each affected branch and display the output.
   - Display the full content of each in-progress story file.
   - Return to step 3d (present options again).

6. **If C (Stabilize):** for each in-progress story branch and any related task branches, in sequence:

   a. Switch to the story branch:
      ```bash
      git checkout <branch-name>
      ```

   b. Stage tracked changes only:
      ```bash
      git add -u
      ```

   c. If there are staged changes, commit with a WIP prefix:
      ```bash
      git commit -m "WIP: <story-name> — paused for planning context switch"
      ```
      If there is nothing to commit, skip this step.

   d. Ask the user: "Push the WIP commit for `<branch-name>` to the remote? (yes/no)"
      - If yes: `git push origin <branch-name> --force-with-lease`
      - If no: skip.

   e. Set the story status to `STANDBY` in its story file (replace the `IN PROGRESS` status line).

   f. Return to the base branch:
      ```bash
      git checkout <base_branch>
      ```

   g. Repeat for each remaining in-progress story.

   After processing all stories, return **STABILIZED** — caller continues with the new planning execution.

7. **If D (Proceed anyway):**
   - Ask the user to type exactly `CONFIRMAR PROCEDER` to confirm.
   - If the user types anything else: return to step 3d (present options again).
   - If confirmed: return **PROCEED** — caller continues without touching in-progress work.

---

**Called by:** `/plan-run` (step 2b), `/plan-agent-execute` (step 0)

---

> [← README](README.md)
