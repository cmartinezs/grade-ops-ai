<a id="top"></a>

# Decisiones UI/UX

Este directorio mantiene decisiones UI/UX relevantes, su estado de validación y sus consecuencias. No reemplaza los ADR técnicos canónicos de `docs/99-decisions/`; los referencia cuando una decisión requiere cambios de arquitectura o implementación.

## Cuándo crear una decisión

Registrar una decisión cuando:

- cambie tareas, navegación, jerarquía, comportamiento o contenido crítico;
- introduzca una excepción de accesibilidad, privacidad o seguridad;
- afecte múltiples pantallas o componentes;
- requiera persistencia, nuevos estados, autorización, eventos o procesamiento asíncrono;
- existan alternativas relevantes con consecuencias distintas;
- la evidencia invalide una decisión previamente aprobada.

## Convención

- Nombre: `YYYY-MM-DD-descripcion-breve.md`.
- Plantilla: [`ADR-UIUX-template.md`](ADR-UIUX-template.md).
- Estados de decisión: `Proposed`, `Accepted`, `Superseded`, `Rejected`.
- Estados de validación: `Internally validated`, `Externally validated`, `External evidence pending`, `Invalidated`.
- Identificadores, estados y referencias técnicas se escriben en inglés.
- Contenido explicativo del proceso puede escribirse en español.

## Documentos

El orden de lectura y navegación es:

| Archivo | Resumen |
|---|---|
| [`ADR-UIUX-template.md`](ADR-UIUX-template.md) | Plantilla para registrar contexto, evidencia, alternativas, decisión, consecuencias, validación y revisión. |
| [`2026-07-27-assessment-domain-foundations.md`](2026-07-27-assessment-domain-foundations.md) | Decisión aceptada sobre ciclo evaluativo transversal, perfiles multidimensionales, plantillas versionadas, estados independientes y semántica DDD de transiciones. |
| [`data-model-impact-ledger.md`](data-model-impact-ledger.md) | Registro de capacidades UI/UX con impacto potencial en dominio, datos, contratos, seguridad, agentes o migraciones. |

## Relación con arquitectura

Una decisión UI/UX no modifica por sí sola el dominio ni el modelo de datos. Sus impactos potenciales se registran en [`data-model-impact-ledger.md`](data-model-impact-ledger.md) y, cuando se resuelvan, deben enlazar el ADR técnico, historia, contrato, migración y pruebas correspondientes.

---

[← Design System](../README.md) · [Siguiente: Plantilla ADR UI/UX →](ADR-UIUX-template.md) · [↑ Volver al inicio](#top)
