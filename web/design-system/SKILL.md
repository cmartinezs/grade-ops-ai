---
name: gradeops-design
description: Use this skill to generate well-branded interfaces and assets for GradeOps AI, either for production or throwaway prototypes/mocks/etc. Contains essential design guidelines, colors, type, fonts, assets, and UI kit components for prototyping.
user-invocable: true
---

Read the `readme.md` file within this skill, and explore the other available files.

GradeOps AI is an AI-assisted assessment platform for teachers (Spanish/Chile), with two portals: **Docente** (teacher) and **Alumno** (student, via magic link). Evaluation types are abierta (rubric), cerrada (answer key) and mixta.

**Where things are:**
- `styles.css` — global entry (link this one file). Tokens under `tokens/`.
- `components/` — React primitives (core, forms, feedback, data). Namespace `GradeOpsAIDesignSystem_fcd12b`.
- `ui_kits/teacher/` and `ui_kits/student/` — full-screen interactive recreations; the best reference for layout, copy, and how primitives compose.
- `guidelines/foundations/` — color/type/spacing/brand specimen cards.

**When building artifacts** (slides, mocks, throwaway prototypes): copy assets out (`assets/`, `styles.css`, the component files you need) and create static HTML for the user to view. Icons come from Lucide via CDN — use the `icons.jsx` helper pattern from a UI kit.

**When working on production code:** copy assets and follow the rules here to design as an expert in this brand.

**Always honor the three product conventions** (see readme): async actions → toast + in-place loader; confirm before leaving with pending work; confirm before any data-mutating action.

If invoked without guidance, ask what the user wants to build, ask a few questions, and act as an expert designer who outputs HTML artifacts *or* production code as needed.
