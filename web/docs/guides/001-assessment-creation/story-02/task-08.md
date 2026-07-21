# Component Hierarchy Draft Builder Screen

**Source:** task-08 | **Area:** unknown | **Date:** 2026-07-17

## What it does
A written Page → Sections → Components breakdown for the Draft Builder screen, following `03-jerarquia-de-componentes.md`, naming every file `task-09` (mockup) and `task-12` (real API wiring) will create.

---

## How to use it
- Write `wireframes/draft-builder-screen-hierarchy.md` listing every file above with its responsibility and Server/Client designation (all Client Components — interactive state + Firebase-authenticated fetch, same reasoning as `task-03`).
- Name the page-level view model shape returned by `useAssessmentDraftBuilderPage`: `{ draft: AssessmentDraftViewModel, versions: AssessmentDraftVersionViewModel[], selectedVersion: number }`.
- Confirm each Section's callback names follow the `onX` action-naming convention (`onSave`, `onRegenerate`, `onViewVersion`) per `03-jerarquia-de-componentes.md` §11 — no bare boolean props for variant control.
- --

## Example
Use `Write` through the public interface introduced by this task.
