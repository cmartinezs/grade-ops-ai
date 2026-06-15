# 📋 Planning System Guide

> [← planning/README.md](README.md)

---

Detailed reference for the lifecycle, structure, and naming conventions of the planning system.

---

## 🔁 Planning Lifecycle

```mermaid
flowchart LR
    A[INITIAL\nUndimensioned idea] --> B[EXPANSION\nTransversal scopes]
    B --> C[DEEPENING\nTasks per scope]
    C --> D[COMPLETED]
```

| Phase | File(s) | Description |
|-------|---------|-------------|
| **INITIAL** | `00-initial.md` | General idea without dimensioning. What needs to be achieved, why, approximate context. Clarity of intent — not exhaustiveness. |
| **EXPANSION** | `01-expansion.md` | All transversal scopes are identified and listed. Dependencies between them are mapped. Impact per SDLC phase is documented. |
| **DEEPENING** | `02-deepening/` | One `.md` file per scope. Each one details its specific tasks with assigned workflow types. |

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
│       ├── 02-deepening/
│       │   ├── scope-01-[name].md
│       │   └── scope-NN-[name].md
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

---

## 🔗 Term Traceability

Each planning has its local `TRACEABILITY.md` with its own terms mapped against the project's repository areas.

The [`TRACEABILITY-GLOBAL.md`](TRACEABILITY-GLOBAL.md) consolidates all terms from all plannings into a single view.

The **macro structures** (columns of the matrix) are the project areas configured during `/plan-init`. Each column maps to a distinct surface of your project (a directory, a service, a repo).

<!-- AREAS-TABLE: populated by plan-init — edit manually if you add or remove areas later -->
| Code | Repository / Area |
|------|------------------|
| `AG` | `agents/` — agent runtime (Spring Boot 4 + Spring AI + Vertex AI Gemini) |
| `AP` | `api/` — backend / domain (Spring Boot 4 + Java 21 + PostgreSQL) |
| `DO` | `docs/` — canonical documentation (Markdown) |
| `IN` | `infra/` — infrastructure (Terraform + GitHub Actions) |
| `WB` | `web/` — frontend (Next.js + TypeScript + Tailwind CSS) |
| `W` | `.planning/` — meta-workflow (this system) |

> This table is filled in by `/plan-init` based on your project structure. To add or change an area after initialization, update this table, `TRACEABILITY-GLOBAL.md`, `_template/TRACEABILITY.md`, and `WORKFLOWS/05-SDLC-PHASE-GUIDANCE/README.md` consistently. `W` is always reserved for the planning system itself.

---

## 📌 Naming Conventions

- File and folder names are in **English**, in **kebab-case**.
- Plannings are numbered sequentially: `001-`, `002-`, etc.
- The name describes the topic: `001-planning-system-bootstrap`
- Scopes inside `02-deepening/` follow the same pattern: `scope-01-name.md`
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
| **Meta-planning system** | `.planning/` (this folder) | Tracks HOW implementation work is managed across all repos. Controls AI agent workflow. Contains plannings, scopes, traceability matrices, PDRs. |
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
