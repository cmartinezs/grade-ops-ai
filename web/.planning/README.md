# 📋 Planning — web

Central directory for all project plannings.

> **Project**: `grade-ops-web` · **Initialized**: 2026-07-14

> For detailed structure, lifecycle, and naming conventions, see [`GUIDE.md`](GUIDE.md).
> For step-by-step guides, see [`TUTORIAL/`](TUTORIAL/README.md).
> For major-version workspace migrations, see [`update-version/`](update-version/README.md) and run `/plan-update-version <from> <to>`.
> For software logging policy, see [`LOGGING.md`](LOGGING.md).
> For deterministic quality gates, run `/plan-test-suite <planning-id> [story-NN] [task-NN] [--all]`; it uses [`scripts/generate-test-suite.sh`](scripts/generate-test-suite.sh).

---

## 🚨 Fundamental Rule

> **Nothing is executed without being inside a planning.**

### Bypass Parameters

| Parameter | Behavior |
|-----------|----------|
| `--no-plan` | Ask for confirmation before executing without a planning entry |
| `--no-plan-force` | Execute directly without asking |

---

## 📂 Plannings

> **In progress** (EXPANSION / DEEPENING): [`active/`](active/README.md) · **Completed**: [`finished/`](finished/README.md)

### 🚧 In Progress → see [`active/README.md`](active/README.md)


### ✅ Completed → see [`finished/README.md`](finished/README.md)

- [001-assessment-creation](finished/001-assessment-creation/README.md) — Build the intake form, editable draft view, regeneration flow, and version history for assessment creation, integrating against `api/003-assessment-creation`'s already-`DONE` endpoints. (COMPLETED 2026-07-22)

### 🆕 Initial

*(none yet)*
