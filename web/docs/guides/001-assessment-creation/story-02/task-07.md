# Wireframe Draft Builder Screen

**Source:** task-07 | **Area:** unknown | **Date:** 2026-07-17

## What it does
A written, low-fidelity textual wireframe for the Draft Builder screen (US-011 + US-012 combined: view/edit draft, regenerate with adjustment notes, browse version history), following `02-ux-wireframes-y-maquetas.md` §2-3, that `task-08` (hierarchy) and `task-09` (mockup) are built from.

---

## How to use it
- Write `wireframes/draft-builder-screen.md` using the guide's §3 format, covering:
- Usuario: docente autenticado, dueño de la evaluación.
- Objetivo: revisar el draft generado por IA, editarlo, regenerarlo con notas de ajuste si no es correcto, y consultar versiones previas.
- Layout: `AppShell` protegido → Header (título de la evaluación) → Section "Editor de draft" (campos editables: title, context, instructions, objectives, deliverables, constraints) → Section "Regenerar" (input de notas de ajuste + acción) → Section "Historial de versiones" (lista de versiones previas, solo lectura).
- Acción primaria: guardar cambios editados / regenerar (son dos acciones distintas, ambas con su propio loading/error).
- Estados: loading inicial (skeleton), empty (aún no existe draft — no debería ocurrir si se llega desde `task-06`'s redirect, pero documentarlo como estado defensivo), listo (draft renderizado), guardando edición, regenerando, error de conflicto 409 (alguien más modificó el draft), error 404 (assessment no existe), error 500.

## Example
Use `Write` through the public interface introduced by this task.
