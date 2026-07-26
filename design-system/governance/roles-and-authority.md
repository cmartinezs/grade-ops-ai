<a id="top"></a>

# Roles y autoridad UI/UX

## Objetivo

Definir responsabilidades y autoridad de aprobación durante la etapa inicial de GradeOps AI. Los roles representan funciones; una misma persona puede ejercer varias.

## Responsabilidades

| Función | Responsable inicial | Responsabilidad | Autoridad |
|---|---|---|---|
| Product Owner | Carlos | Problema, alcance, prioridad, valor y riesgo de producto | Aprueba o rechaza |
| Responsable UX | Carlos | Investigación, arquitectura de información, flujos y usabilidad | Aprueba o rechaza |
| Responsable técnico | Carlos | Viabilidad, arquitectura, seguridad, accesibilidad y deuda aceptada | Aprueba o rechaza |
| Docente validador | Colegas disponibles | Evidencia sobre utilidad, comprensión y eficiencia | Recomienda; no bloquea |
| IA generativa | Herramienta utilizada | Propone, analiza, implementa y documenta dentro del alcance autorizado | No aprueba |

## Matriz de aprobación

| Decisión | Aprobación requerida |
|---|---|
| Problema, alcance o prioridad | Product Owner |
| Flujo, arquitectura de información o copy crítico | Responsable UX |
| Dirección visual | Product Owner y responsable UX |
| Contrato, persistencia o arquitectura | Responsable técnico |
| Excepción WCAG, seguridad o privacidad | Responsable técnico con riesgo documentado |
| Publicación de una acción académica sensible | Confirmación humana autorizada |
| Cierre de etapa | Responsables aplicables según el gate |

Cuando Carlos ejerza varias funciones, debe revisar explícitamente cada dimensión afectada; una aprobación única no elimina los criterios técnicos, de producto o UX.

## Validación externa

El feedback de docentes externos es consultivo y opcional. Su ausencia:

- no bloquea etapas ni releases;
- obliga a registrar `External evidence pending`;
- se mitiga con evaluación heurística, casos docentes realistas y revisión interna;
- se reconsidera cuando exista acceso razonable a usuarios.

No se utiliza la expresión “validado por docentes” sin evidencia externa registrada.

## Quality gate

Ninguna decisión generada o implementada por IA se considera aprobada sin revisión humana. Las excepciones registran:

- criterio afectado;
- justificación;
- impacto y usuarios expuestos;
- mitigación;
- responsable;
- fecha límite o condición de revisión.

---

[← Índice de gobierno](README.md) · [Siguiente: Definition of Ready →](definition-of-ready.md) · [↑ Volver al inicio](#top)
