# [CHECK-STORY-CONTEXT]

> [← README](README.md)

Validates the current git and story state before switching to a new story branch. Detects in-progress work on the current story branch or task branch and offers safe alternatives before any context switch.

---

## Steps

1. Detect the current branch:
   ```bash
   git branch --show-current
   ```

2. If the current branch does **not** match `story-NN-*`, `story-NN-*/*`, `<worktree-prefix>/story-NN-*`, or `<worktree-prefix>/story-NN-*/*`: return **OK** — no story context to protect; caller continues normally.

3. If the current branch is a story branch or a task branch under a story branch:

   a. Derive the protected story branch:
      - For `story-NN-slug`, protected story branch is the current branch.
      - For `story-NN-slug/task-NN-task-slug`, protected story branch is the segment before `/`.
      - For `<worktree-prefix>/story-NN-slug`, protected story branch is the current branch.
      - For `<worktree-prefix>/story-NN-slug/task-NN-task-slug`, protected story branch is the first two path segments.

   b. Derive the story filename stem from the protected story branch. If the branch has a worktree prefix, strip it first: `<worktree-prefix>/story-NN-slug` → `story-NN-slug`.

   c. Find the corresponding story file under `.planning/active/*/02-deepening/<story-filename-stem>.md`.
      If no matching story file is found, return **OK** — branch exists but is not tracked by the planning system.

   d. Read the story file status. If the status is not `IN PROGRESS`: return **OK** — nothing to protect.

   e. Gather git state:
      ```bash
      git status --short
      git log @{u}..HEAD --oneline 2>/dev/null || echo "(no upstream configured)"
      git stash list
      ```
      Count separately:
      - **Staged files** (lines starting with a letter in the first column)
      - **Modified tracked files** (lines starting with a letter in the second column)
      - **Untracked files** (lines starting with `??`)
      - **Unpushed commits** (lines from `git log @{u}..HEAD`)

   f. Present a structured report to the user:

      ```
      ⚠️  Story context conflict detected

      Current story : <story-NN> — <story name>
      Branch        : <branch-name>
      Story branch  : <protected-story-branch>
      Status        : IN PROGRESS

      Git state:
        Staged              : N files
        Modified (tracked)  : M files
        Untracked           : U files
        Unpushed commits    : P
        Existing stashes    : Q
      ```

   g. Present the following options and wait for the user to choose:

      ```
      Choose how to proceed:

        A) Abort              — stay on this story/task branch; do not start the new story
        B) Stash              — stash pending changes, then switch to the new story branch
        C) Commit WIP         — commit tracked changes as WIP, then switch
                                (untracked files are left in place)
        D) Commit + Push WIP  — commit and push to remote, then switch
        E) Commit + Push + STANDBY — D plus mark this story as STANDBY
                                     (recommended if you do not plan to resume soon)
      ```

4. **If A (Abort):** return **ABORT** — caller must stop without making any changes.

5. **If B (Stash):**

   a. Ask the user: "Include untracked files in the stash? (yes/no)"
      - If yes: `git stash push --include-untracked -m "WIP: <story-name>"`
      - If no:  `git stash push -m "WIP: <story-name>"`
        Note: untracked files remain in the working tree and will carry over to the new branch. Warn the user if there are untracked files and they chose not to include them.

   b. Return **STASHED** — caller continues with the new story branch checkout.

6. **If C (Commit WIP):**

   a. Stage tracked changes:
      ```bash
      git add -u
      ```

   b. If there is nothing staged, inform the user and return to step 3g (present options again).

   c. Commit:
      ```bash
      git commit -m "WIP: <story-name>"
      ```

   d. If there are untracked files, warn: "N untracked file(s) were left in place and will carry over to the new branch."

   e. Return **COMMITTED** — caller continues with the new story branch checkout.

7. **If D (Commit + Push WIP):**

   a. Execute steps 6a–6c (stage and commit).

   b. Push:
      ```bash
      git push origin <branch-name> --force-with-lease
      ```
      If push fails (e.g. no upstream), report the error and ask the user to choose A or E.

   c. Return **COMMITTED_PUSHED** — caller continues with the new story branch checkout.

8. **If E (Commit + Push + STANDBY):**

   a. Execute steps 7a–7b (stage, commit, push).

   b. Set the story status to `STANDBY` in the story file (replace the `IN PROGRESS` status line).

   c. Return **STANDBY** — caller continues with the new story branch checkout.

---

**Called by:** `/plan-story` (Git pre-flight, step 0)

---

> [← README](README.md)
