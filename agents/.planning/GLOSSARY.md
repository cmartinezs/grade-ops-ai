# 📚 Planning System — Glossary

> [← planning/README.md](README.md)

Operational vocabulary used in this planning system. For domain-specific DDD / Hexagonal Architecture terminology, see `01-templates/02-requirements/TEMPLATE-glossary.md`.

---

## Terms

### Planning
A self-contained unit of work inside the planning system. Each planning has a lifecycle (`INITIAL → EXPANSION → DEEPENING → COMPLETED`), its own folder, and its own term traceability matrix. It is **not** the same as an SDLC phase.

---

### User Story (Story)
A transversal work unit within a planning. Defined during the EXPANSION phase and detailed in a file under `02-deepening/`. Each user story targets a specific area of the repository (a phase template, a guide, a workflow file, etc.) and contains individual tasks with assigned workflows.

---

### Work Item / Deliverable
The general-project equivalent of a user story. In software projects, the planning item is usually a user story or technical story. In research, documentation, operations, or business projects, the same planning layer can represent a deliverable, work package, decision, experiment, or action group.

---

### Atomic Task
The smallest executable unit of work, produced by decomposing a story with `/plan-atomize` (the `ATOMIZE-STORY` workflow). Lives as one file per task under `02-deepening/story-NN-name/`, and the story's task table becomes its index. Every atomic task must satisfy the atomicity requirements verified by `[CHECK-ATOMICITY]`: a single verifiable deliverable, independent executability (or explicit dependencies), a technical design or execution approach, concrete implementation steps, verification evidence, binary done criteria, a workflow reference from the catalog, and a size that fits one work session.

---

### Workflow
A defined sequence of steps for executing a specific type of task. Every task in a story must specify which workflow governs its execution. See [`WORKFLOWS/README.md`](WORKFLOWS/README.md) for the full catalog.

---

### Sub-workflow
A reusable step sequence used inside multiple workflows. Called by embedding `[SUB-WORKFLOW-NAME]` within a workflow step. See [`WORKFLOWS/04-SUB-WORKFLOWS/`](WORKFLOWS/04-SUB-WORKFLOWS/README.md) for the full catalog.

---

### INITIAL
The first phase of a planning lifecycle. The planning exists as a general idea: what needs to be done, why, and approximate scope. No detailed dimensioning.

---

### EXPANSION
The second phase. All user stories are identified, dependencies between them are mapped, and impact per SDLC phase is documented. The planning moves to `active/`.

---

### DEEPENING
The third phase. One file per story under `02-deepening/`. Each file specifies detailed tasks with workflow types. This is where actual execution occurs.

---

### COMPLETED
The final state of a planning. All stories are done, all traceability is updated, and the planning is archived in `finished/`. Documents are not modified after this point.

---

### Story Statuses
Valid story statuses are:

| Status | Meaning | Closable? |
|--------|---------|-----------|
| `TODO` | Story is defined but not started. | No |
| `IN PROGRESS` | Story execution has started and is actively being worked. | No |
| `DONE` | Story done criteria were verified and the story is complete. | Yes |
| `BLOCKED` | Story cannot proceed because of an unresolved dependency, missing information, failing validation, or external blocker. | No |
| `SKIPPED` | Story is intentionally no longer applicable. Must include a skipped reason. | Yes |
| `STANDBY` | Story was intentionally paused to allow context switching while preserving progress. | No |

`SUPERSEDED` applies to a planning, not to an individual story. Use it when a planning is replaced by a newer planning via `SUPERSEDE-PLANNING`.

---

### Project Modes
The planning system defaults to `software`, but `.planning/config.yml` can adapt behavior:

| Mode | Use when | Verification expectation |
|------|----------|--------------------------|
| `software` | Source code, services, apps, infrastructure, automation | Tests, build checks, linting, stack-specific smoke tests, connectivity or schema checks, and human developer review |
| `general` | General project delivery | Evidence checklist, manual validation, stakeholder confirmation |
| `documentation` | Docs, guides, content, knowledge bases | Review, links, rendered output, publication checks |
| `research` | Discovery, analysis, experiments | Notes, sources, decisions, reproducible evidence |
| `operations` | Process, support, rollout, coordination | Runbooks, approvals, checklists, handoff evidence |

---

### Traceability
The process of registering how a term, decision, or concept affects each SDLC phase of the repository. Maintained in `TRACEABILITY.md` per planning and consolidated in `TRACEABILITY-GLOBAL.md`.

---

### PDR (Project Decision Record)
A record of a significant decision affecting multiple phases or the framework level (e.g., a naming convention change, a new glossary term). Stored in `planning/NNN-name/` folder. Different from an **ADR** (Architecture Decision Record), which is stored at `01-templates/06-development/` and documents technical implementation decisions.

---

### ADR (Architecture Decision Record)
A record of a technical decision affecting the architecture of a specific project being documented. Stored in `01-templates/06-development/`. Different from a **PDR**, which affects the template framework itself.

---

### Done Criteria
The set of conditions that must be met for a task or story to be marked as completed. Each story file includes its own Done Criteria section. Used by the `[EXECUTE-STORY]` sub-workflow.

---

### Inconsistency
A detected contradiction, ambiguity, or gap between two or more documents in the repository. Managed via the `RECORD-INCONSISTENCY` workflow and tracked as a residual if not immediately resolvable.

---

### Residual
A task or inconsistency that cannot be resolved in the current story but is documented and deferred to a later planning or story. Not the same as "blocked" — a residual is acknowledged and intentionally deferred.

---

### STANDBY
A story status meaning execution was intentionally interrupted to allow a context switch. The story retains all its progress and can be resumed by re-running `/plan-story`. Different from `BLOCKED` (which means the story cannot proceed due to an unresolved external dependency) and from `IN PROGRESS` (which means execution is actively happening). Set by `[CHECK-PLANNING-CONTEXT]` and `[CHECK-STORY-CONTEXT]` when the user chooses to stabilize the current work before switching context.

---

### Bypass
A mechanism to skip the Fundamental Rule check and execute without a planning entry. Uses `--no-plan` (asks for confirmation) or `--no-plan-force` (executes directly). Should be used sparingly.

---

### Document Status
Each major document has a status badge (`Draft`, `In Review`, `Final`, `Obsolete`) to reflect its maturity. Format: `> **Status:** Draft`.

---

### SDLC Phase
One of the 12 documentation phases in this repository's framework (Discovery → Feedback). Represented by single-letter codes (`D`, `R`, `S`, `M`, `P`, `V`, `T`, `B`, `O`, `N`, `F`) in traceability matrices.

---

> [← planning/README.md](README.md)
