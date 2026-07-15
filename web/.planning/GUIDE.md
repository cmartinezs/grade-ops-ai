# 📋 Planning System Guide

> [← planning/README.md](README.md)

---

Detailed reference for the lifecycle, structure, and naming conventions of the planning system.

---

## 📍 Workspace Boundary

Planning commands always operate on the current working directory's `./.planning/` directory.

- Do not search parent directories for `.planning/`.
- If the current directory has no `./.planning/`, run `/plan-init` in the current directory before creating or running a planning.
- In a monorepo, the parent workspace and each child artifact workspace are separate planning systems when they each have their own `.planning/`.
- Running `/plan-new`, `/plan-from-epic`, `/plan-agent-plan`, or `/plan-expand` inside a child artifact must create or modify only that child artifact's `.planning/`.
- When a parent planning creates or coordinates child plannings under git, each child planning must run from its own sibling worktree. Create the worktree with the branch that belongs to that child planning: `git worktree add ../<worktree-prefix> <branch>` for an existing branch, or `git worktree add -b <branch> ../<worktree-prefix> <base_branch>` for a new branch.

### Layered git branch cleanup

When git execution is enabled, the planning system uses a layered branch model:

- Story branches start from `git.base_branch`.
- Task branches start from the story branch and open task PRs back into that story branch.
- Child planning branches created in a dedicated worktree preserve the worktree prefix before the story/task name: `<worktree-prefix>/story-NN-<slug>` and `<worktree-prefix>/story-NN-<slug>/task-NN-<slug>`.
- After a task PR is merged into the story branch, delete the local task branch from an updated story branch checkout with `git branch -d <task-branch>`.
- After the final story PR is merged into `git.base_branch`, delete the local story branch from an updated base branch checkout with `git branch -d <story-branch>`.
- Do not force-delete these branches. If `git branch -d` refuses, confirm the PR merge state first.
- Remote branch deletion remains a PR/repository policy action.

### Monorepo parent/child coordination

When a parent monorepo planning affects child artifacts that have their own `.planning/` workspaces:

- The parent planning owns parent-scope work: orchestration, cross-artifact decisions, integration sequencing, shared docs, releases, and synchronization.
- Each child artifact owns its implementation work in a child planning under that child's own worktree and `./.planning/`.
- The parent planning links to child plannings in `01-expansion.md → Linked Child Plannings`, including the child worktree path and prefixed branch.
- The parent must not duplicate child implementation stories or tasks. It should contain coordination stories that track child planning status and integration checkpoints.
- If a child artifact lacks `.planning/`, initialize it from that child worktree with `/plan-init` before expanding the parent planning.

---

## 🔁 Planning Lifecycle

```mermaid
flowchart LR
    A[INITIAL\nUndimensioned idea] --> B[EXPANSION\nTransversal stories]
    B --> C[DEEPENING\nTasks per story]
    C --> D[COMPLETED]
```

| Phase | File(s) | Description |
|-------|---------|-------------|
| **INITIAL** | `00-initial.md` | General idea without dimensioning. What needs to be achieved, why, approximate context. Clarity of intent — not exhaustiveness. |
| **EXPANSION** | `01-expansion.md` | All user stories are identified and listed. Dependencies between them are mapped. Impact per SDLC phase is documented. |
| **DEEPENING** | `02-deepening/` | One `.md` file per story. Each one details its specific tasks with assigned workflow types. Optionally, `/plan-atomize` decomposes a story into atomic task files under `story-NN-name/`, each with technical design, implementation steps, verification, and software local-runtime checks when applicable. |

---

## 🗂️ Folder Structure per Planning

```
planning/
├── README.md                        # General index + plannings in INITIAL state
├── TRACEABILITY-GLOBAL.md           # Global consolidated term matrix
├── GUIDE.md                         # This file
├── GLOSSARY.md                      # Operational vocabulary
├── PROMPTING.md                     # AI prompting guidelines
├── _template/                       # Template for new plannings
├── update-version/                  # Versioned migrations for older planning-system structures
│
├── NNN-planning-name/               # Plannings in INITIAL state (just created)
│   └── ...
│
├── active/                          # Plannings in EXPANSION or DEEPENING
│   ├── README.md                    # Index of in-progress plannings
│   └── NNN-planning-name/           # Moved here when INITIAL → EXPANSION
│       ├── README.md
│       ├── 00-initial.md
│       ├── 01-expansion.md
│       ├── RETROSPECTIVE-RAW.md
│       ├── 02-deepening/
│       │   ├── story-01-[name].md
│       │   ├── story-01-[name]/         # Atomic tasks (optional, via /plan-atomize)
│       │   │   ├── task-01-[name].md
│       │   │   └── task-NN-[name].md
│       │   └── story-NN-[name].md
│       └── TRACEABILITY.md
│
└── finished/                        # COMPLETED plannings (read-only / reference)
    ├── README.md                    # Index of archived plannings
    └── NNN-planning-name/           # Moved here when DEEPENING → COMPLETED
```

### Folder movement cycle

| State transition | Action |
|-----------------|--------|
| New planning | Create at root `planning/NNN-name/` |
| `INITIAL` → `EXPANSION` | Move `planning/NNN-name/` → `planning/active/NNN-name/` |
| `DEEPENING` → `COMPLETED` | Move `planning/active/NNN-name/` → `planning/finished/NNN-name/` |
| Planning replaced by a new one | Move `planning/active/NNN-name/` → `planning/finished/NNN-name/` with status `SUPERSEDED` |

---

## ♻️ Superseded Plannings

A planning is **superseded** when a new planning contradicts or rebuilds what it implemented, rendering its output partially or fully obsolete. This is distinct from completion — a superseded planning was not wrong, but circumstances changed.

### When supersession applies

- The new planning targets the same functional domain.
- The new planning replaces, migrates away from, or discards something the previous planning built.
- The new planning modifies files the previous planning produced as output.

If any of these are true, execute `SUPERSEDE-PLANNING` **before** creating the new planning. See [`WORKFLOWS/03-MAINTENANCE-WORKFLOWS/SUPERSEDE-PLANNING.md`](WORKFLOWS/03-MAINTENANCE-WORKFLOWS/SUPERSEDE-PLANNING.md).

### Path A — Revert first

The previous planning left artifacts (code, dependencies, endpoints, config) that must be actively removed before the new planning executes. Leaving them would produce dead code, orphaned dependencies, or routes that must no longer exist.

**Status:** `SUPERSEDED — REVERTED`

The superseded planning's `README.md` documents every artifact that was deleted. The new planning's `00-initial.md § Supersedes` records the path and justification.

### Path B — Overwrite

The previous planning's artifacts are reused: pages stay, routes stay, component files stay — the new planning changes what happens *inside* them. No removal step is needed because the new planning replaces the internals cleanly.

**Status:** `SUPERSEDED — OVERRIDDEN`

The superseded planning's `README.md` documents which artifacts survived and which were internally replaced. The new planning inherits relevant traceability terms, marking modified ones as `⚠️` and absorbed ones as `✅`.

---

## 🔗 Term Traceability

Each planning has its local `TRACEABILITY.md` with its own terms mapped against the project's repository areas.

The [`TRACEABILITY-GLOBAL.md`](TRACEABILITY-GLOBAL.md) consolidates all terms from all plannings into a single view.

The **macro structures** (columns of the matrix) are the project areas configured during `/plan-init`. Each column maps to a distinct surface of your project (a directory, a service, a repo).

<!-- AREAS-TABLE: populated by plan-init — edit manually if you add or remove areas later -->
| Code | Repository / Area |
|------|------------------|
| `WB` | `src/` — teacher-facing web app (Next.js 15 + React 19 + Tailwind CSS 4) |
| `W` | `.planning/` — meta-workflow (this system) |

> This table is filled in by `/plan-init` based on your project structure. To add or change an area after initialization, update this table, `TRACEABILITY-GLOBAL.md`, `_template/TRACEABILITY.md`, and `WORKFLOWS/05-SDLC-PHASE-GUIDANCE/README.md` consistently. `W` is always reserved for the planning system itself.

---

## 📌 Naming Conventions

- File and folder names are in **English**, in **kebab-case**.
- Plannings are numbered sequentially: `001-`, `002-`, etc.
- The name describes the topic: `001-planning-system-bootstrap`
- Stories inside `02-deepening/` follow the same pattern: `story-01-name.md`
- Atomic tasks live in a folder named after their story file (without `.md`): `story-01-name/task-01-name.md`
- Content inside files may be in any language the team uses.

---

## 🔑 Source Hierarchy

When a conflict exists between documents, this order of authority applies (most authoritative first):

```
docs/ (canonical product/architecture documentation)
  > backend / agent implementation
    > frontend implementation
      > infrastructure configuration
```

- An active PDR overrides any content document for the element it covers.
- Within `docs/`: thematic folders are the source of truth. Historical or auto-generated directories are reference-only.

---

## 🗺️ Two Distinct Planning Domains — Do Not Confuse

This project has two separate "planning" concepts. They must never be conflated:

| Domain | Location | What it is |
|--------|----------|------------|
| **Meta-planning system** | `.planning/` (this folder) | Tracks HOW implementation work is managed across all repos. Controls AI agent workflow. Contains plannings, stories, traceability matrices, PDRs. |
| **Product documentation** | `docs/` | Canonical product strategy, architecture, and technical specifications — already produced. Read-only unless a planning explicitly targets `docs/`. |

### When you are in the meta-planning system (`.planning/`)

You are managing implementation work. Tasks here are:
- Creating and advancing plannings (ADVANCE-PLANNING, CREATE-PLANNING)
- Scaffolding code, implementing features, writing tests, configuring infra (GENERATE-DOCUMENT)
- Maintaining traceability (UPDATE-TRACEABILITY)
- Resolving inconsistencies between repos (RECORD-INCONSISTENCY)

### When you are reading `docs/`

You are reading the source of truth for product decisions, architecture, and technical specifications. Do not modify `docs/` unless the planning explicitly calls for a documentation update. Reference `docs/` to understand context — do not reinterpret it.

---

> [← planning/README.md](README.md)
