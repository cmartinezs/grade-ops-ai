# 📋 Planning — agents

Central directory for all project plannings.

> **Project**: `grade-ops-agents` · **Initialized**: 2026-07-09

> For detailed structure, lifecycle, and naming conventions, see [`GUIDE.md`](GUIDE.md).
> For step-by-step guides, see [`TUTORIAL/`](TUTORIAL/README.md).
> For major-version workspace migrations, see [`update-version/`](update-version/README.md) and run `/plan-update-version <from> <to>`.

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

- [001-assessment-creation](active/001-assessment-creation/README.md) — Assessment Agent (contract, prompt, pipeline) for the assessment-creation flow (child planning of `008-assessment-creation`)

### ✅ Completed → see [`finished/README.md`](finished/README.md)

- [002-groq-genai-provider](finished/002-groq-genai-provider/README.md) — Groq adapter as an alternative LLM provider alongside Gemini, on-demand per-request provider/model selection, `.env`-based local config and Cloud Run env vars instead of full Spring YAML paths (COMPLETED 2026-07-12)

### 🆕 Initial

*(none yet)*
