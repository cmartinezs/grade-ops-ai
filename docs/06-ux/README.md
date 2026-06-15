# 06 UX

This folder defines the interaction model, screen inventory, and UX design intent for GradeOps AI.

It is not a wireframe library — implementation-level mockups live in `grade-ops-ai-web`. This folder records the UX decisions and design rationale that must remain consistent across implementation iterations.

## Design Purpose

GradeOps AI should feel like an **assessment operations console**, not a quiz tool or generic education admin.

The product serves two roles with distinct needs:

- **Teacher** — controls the full assessment workflow; reviews and approves AI outputs.
- **Student (LearnerRef)** — accesses a specific assessment or result via a secure link; no account, no navigation, no ambient app.

These roles should not share visual language or screen structure. The teacher workspace is a control room. The student experience is a focused, ephemeral interaction.

## UX Principles

1. **The teacher is always in control.** Every AI output is presented as a suggestion requiring review, never as a final result. The UI must make approval and edit paths equally prominent.
2. **Agent activity must be visible.** Logs, model names, cost estimates, and approval states are first-class UI elements — not hidden in an admin panel.
3. **Progress over polish.** During the MVP, clarity and flow correctness matter more than visual refinement. The demo must work end-to-end.
4. **No ambient student UI.** Students do not browse, navigate, or have a profile. Their experience starts and ends at a single access link.
5. **Evidence must be screenshot-ready.** Key dashboards (agent logs, evidence summary, approval states) must look credible and clear at demo time.

## Folder Contents

| File | Purpose |
|------|---------|
| [`screen-inventory.md`](screen-inventory.md) | Full screen inventory per role, with status and mode |
| [`teacher-workspace-ux.md`](teacher-workspace-ux.md) | Detailed UX intent for the teacher workspace and approval flows |
| [`student-access-ux.md`](student-access-ux.md) | UX intent for student secure-link access and result delivery |

## Relationship To Other Documents

| Source | UX implication |
|--------|----------------|
| [`02-product/personas.md`](../02-product/personas.md) | Teacher goals and pain points drive workspace priorities |
| [`02-product/mvp-scope.md`](../02-product/mvp-scope.md) | MVP cut line defines which screens must ship first |
| [`02-product/workflows.md`](../02-product/workflows.md) | Assessment state machine maps directly to navigation and screen transitions |
| [`02-product/student-access.md`](../02-product/student-access.md) | Secure link model and LearnerRef identity constraints |
| [`04-architecture/repository-structure.md`](../04-architecture/repository-structure.md) | Web routing structure (`grade-ops-ai-web`) must match screen inventory |
