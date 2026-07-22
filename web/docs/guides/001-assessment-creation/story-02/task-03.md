# Component Hierarchy Intake Screen

**Source:** task-03 | **Area:** unknown | **Date:** 2026-07-16

## What it does
A written Page → Section → Component breakdown for the Intake screen, following `docs/gradeops-ai-frontend-guidelines/03-jerarquia-de-componentes.md`, naming every file `task-04` (mockup) and `task-06` (real API wiring) will create.

---

## How to use it
- Write `wireframes/intake-screen-hierarchy.md` listing the Page/Section/Component/hook/schema files above, each with a one-line responsibility.
- For each file, state whether it's a Server or Client Component per `01-arquitectura-next-react.md` §7 — the Page and its children are all Client Components here (`"use client"`) since the form has interactive state and Firebase-authenticated fetch calls, which cannot run as Server Components in this project's client-driven auth model.
- Confirm the route path `src/app/(protected)/assessments/new/` doesn't collide with the existing `src/app/(protected)/assessments/page.tsx` placeholder (it doesn't — Next.js route groups treat `new/` as a distinct segment).
- --

## Example
Use `Write` through the public interface introduced by this task.
