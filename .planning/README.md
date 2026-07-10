# 📋 Planning — grade-ops-ai

Central directory for all project plannings.

> **Project**: `grade-ops-ai` · **Initialized**: 2026-06-12

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

### 🆕 Initial

_None._

### 🚧 In Progress → see [`active/README.md`](active/README.md)

- [008-assessment-creation](active/008-assessment-creation/01-expansion.md) — Enable the first step of the open-assessment pipeline: turning a teacher's learning intent into a structured, AI-generated assessment draft that is editable and fully logged.

### ✅ Completed → see [`finished/README.md`](finished/README.md)

- [001-teacher-onboarding](finished/001-teacher-onboarding/README.md) — Teacher Onboarding and Workspace (11 stories, COMPLETED 2026-06-13)
- [002-google-sign-in](finished/002-google-sign-in/README.md) — Google OAuth sign-in para teachers (3 stories, COMPLETED 2026-06-15)
- [003-auth-ux-and-local-dev](finished/003-auth-ux-and-local-dev/README.md) — Auth UX polish and local dev stack (3 stories, COMPLETED 2026-06-14)
- [004-subpage-identity](finished/004-subpage-identity/README.md) — Identidad visual por subpágina (5 stories, COMPLETED 2026-06-15)
- [005-design-template](finished/005-design-template/README.md) — Aplicación del Design System al portal docente (5 stories, COMPLETED 2026-06-21)
- [006-password-recovery](finished/006-password-recovery/README.md) — Flujo `/forgot-password` + `/reset-password` vía Firebase Auth, SUPERSEDED por 007 (3 stories, COMPLETED 2026-06-21)
- [007-password-recovery-custom-email](finished/007-password-recovery-custom-email/README.md) — Reemplazar Firebase email con servicio propio en `api/` (JavaMail + Thymeleaf) (3 stories, COMPLETED 2026-06-30)
