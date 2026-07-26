# Flujo profesional de UI/UX para GradeOps AI

## Propósito

Definir un proceso trazable para diseñar, validar e implementar interfaces centradas en el trabajo docente, evitando que la exploración visual condicione prematuramente la arquitectura frontend.

## Flujo recomendado

1. Investigación y definición del problema.
2. Arquitectura de información y flujos.
3. Exploración visual en Google Stitch.
4. Prototipado y consolidación en Figma.
5. Validación UX con docentes.
6. Implementación inicial con v0.
7. Integración y endurecimiento con Codex o Claude Code.
8. Control de calidad y liberación.

## Documentos

| Archivo | Finalidad |
|---|---|
| `01-principios-y-gobierno.md` | Principios, roles, decisiones y trazabilidad |
| `02-descubrimiento-ux.md` | Investigación, usuarios, tareas y requisitos UX |
| `03-arquitectura-informacion-flujos.md` | Navegación, flujos y estados |
| `04-exploracion-visual-stitch.md` | Generación y selección de direcciones visuales |
| `05-prototipado-design-system-figma.md` | Prototipo, tokens, componentes y variantes |
| `06-validacion-con-usuarios.md` | Pruebas de usabilidad y criterios de aceptación |
| `07-implementacion-v0.md` | Traducción controlada del diseño a React/Next.js |
| `08-integracion-produccion.md` | Adaptación al repositorio y controles técnicos |
| `09-quality-gates.md` | Criterios obligatorios para avanzar y liberar |
| `10-prompts-operativos.md` | Prompts base para cada herramienta |

## Regla de uso

Ninguna etapa comienza sin cumplir el criterio de salida de la anterior. Los hallazgos que cambien tareas, navegación o jerarquía informativa regresan a UX; los ajustes exclusivamente visuales regresan al sistema de diseño.

El feedback de docentes externos es evidencia consultiva deseable, pero no un requisito para avanzar. Cuando no exista, la decisión debe registrar la validación interna, la hipótesis pendiente y el riesgo aceptado.

La etapa 02 solo puede comenzar cuando se cumpla el [`Gate 0`](09-quality-gates.md#gate-0--gobierno-preparado) y su [`Definition of Ready`](../governance/definition-of-ready.md).
