# 🔍 DEEPENING: Story 02 — assessment-screens-wireframes-and-data-providers

**Planning:** 001-assessment-creation | **Date:** 2026-07-21 | **Area:** unknown

## What changed
- Design and build, per screen, the full pipeline defined by `docs/gradeops-ai-frontend-guidelines/02-ux-wireframes-y-maquetas.md` §2 (objetivo de usuario → flujo → estados → wireframe → jerarquía de componentes → maqueta funcional con datos fake → validar copy → conectar API real) for the two screens that make up Story 01's scope: 1.
- Wireframe textual de baja fidelidad documentado para Intake screen y Draft Builder screen.
- Maqueta funcional navegable con datos fake existe para ambas pantallas, cubriendo estados loading/empty/error y casos límite (texto largo, muchas versiones, cero versiones), antes de conectar el backend.
- DTOs de `lib/api` reflejan exactamente los tipos reales de `AssessmentController` — sin campos inventados.
- Draft Builder screen usa un Screen Data Facade (`loadAssessmentDraftBuilderPage`) porque combina 2+ fuentes remotas (draft actual + versiones).
- Intake screen orquesta sus 2 llamadas secuenciales (crear brief → generar draft) fuera del Page/TSX, con estados de mutación explícitos.

## Who is affected
End users and API consumers
