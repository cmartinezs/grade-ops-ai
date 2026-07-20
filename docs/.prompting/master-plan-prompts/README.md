# GradeOps AI — Prompts para generar el Master Plan

Este paquete divide la generación del Master Plan Ejecutivo en fases pequeñas, verificables y reutilizables.

## Objetivo

Construir un Master Plan basado en `@docs/` que permita entregar GradeOps AI de forma incremental, funcional, desplegable y orientada a valor, aumentando progresivamente el nivel de automatización y generando evidencia técnica, operativa y comercial.

## Estructura

```text
master-plan-prompts/
├── README.md
├── master-plan-specification.md
├── 01-discovery-and-diagnosis.md
├── 02-capabilities-and-user-stories.md
├── 03-automation-strategy.md
├── 04-release-planning.md
├── 05-generate-release-document.md
└── 06-validate-master-plan.md
```

## Orden de ejecución

1. Ejecutar `01-discovery-and-diagnosis.md`.
2. Revisar y resolver los hallazgos bloqueantes.
3. Ejecutar `02-capabilities-and-user-stories.md`.
4. Ejecutar `03-automation-strategy.md`.
5. Ejecutar `04-release-planning.md`.
6. Ejecutar `05-generate-release-document.md` una vez por cada release.
7. Ejecutar `06-validate-master-plan.md` cuando todas las releases estén documentadas.

## Regla principal

Ninguna fase debe ejecutarse si faltan sus entradas obligatorias o si la fase anterior dejó decisiones bloqueantes sin resolver.

## Resultado esperado

```text
@docs/master-plan/
├── README.md
├── master-plan-executive.md
├── validation-report.md
├── analysis/
│   ├── documentation-diagnosis.md
│   ├── decisions-and-assumptions.md
│   ├── capability-map.md
│   ├── user-story-inventory.md
│   ├── automation-inventory.md
│   └── release-strategy.md
└── releases/
    ├── release-01-<nombre-descriptivo>.md
    ├── release-02-<nombre-descriptivo>.md
    └── ...
```

## Convenciones

- Los prompts están diseñados para Claude Code o herramientas similares con acceso al repositorio.
- `@docs/` representa la documentación funcional, técnica y estratégica del proyecto.
- Si existe código implementado, debe contrastarse con la documentación.
- No se deben inventar comandos `/release-*`; deben inspeccionarse en el plugin `claude-planning-with-ai`.
- Los prompts de cada release deben vivir únicamente en el archivo de esa release.
- Las decisiones no confirmadas deben quedar identificadas como supuestos, riesgos o preguntas abiertas.

## Uso recomendado

Conserva `master-plan-specification.md` como fuente normativa. Los prompts de fase deben referenciarla, pero cada uno debe ejecutar únicamente su responsabilidad.
