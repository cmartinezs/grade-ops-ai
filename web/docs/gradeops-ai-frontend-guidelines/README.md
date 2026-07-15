# GradeOps AI — Reglas de codificación y buenas prácticas Frontend

Este paquete define el estándar base para construir la interfaz docente de GradeOps AI con Next.js, React, TypeScript y un Design System consistente.

La guía toma como punto de partida las prácticas usadas en `grade-ops-ai-web` y las alinea con la guía backend de `gradeops-ai-java-guidelines`: decisiones explícitas, separación por responsabilidades, trazabilidad, pruebas automatizadas y desarrollo asistido por IA con revisión humana.

La diferencia principal es el foco: en frontend no basta con que el código compile. Una feature debe ser comprensible, accesible, responsive, consistente visualmente y útil para el flujo real del docente.

## Audiencia

Esta documentación está escrita para:

- Desarrolladores frontend de GradeOps AI.
- Desarrolladores backend que necesitan entender el contrato con la UI.
- Agentes AI de codificación usados en el proyecto.
- Revisores técnicos de Pull Requests.
- Diseñadores o product owners que validen flujos docentes.
- Futuras personas que se incorporen al equipo.

## Estructura de archivos

| Archivo | Propósito |
|---|---|
| `00-principios-rectores.md` | Principios generales de ingeniería, producto y UX para la UI docente. |
| `01-arquitectura-next-react.md` | Organización por App Router, rutas, componentes, hooks, servicios y tipos. |
| `02-ux-wireframes-y-maquetas.md` | Flujo esperado antes de implementar: UX, wireframes y maquetas funcionales. |
| `03-jerarquia-de-componentes.md` | Jerarquía `Layout`, `Page`, `Section`, `SubSection`, `Component`, `MiniComponent`, `MicroComponent`. |
| `04-hooks-y-logica-de-ui.md` | Regla de hooks propios por componente y separación de lógica interna. |
| `05-design-system-tokens-y-estilos.md` | Uso del DS, tokens CSS, estilo visual, iconografía y reglas de composición. |
| `06-estado-datos-y-api.md` | Estado local/remoto, API client, DTOs, errores y sincronización con backend. |
| `07-formularios-validacion-y-feedback.md` | Formularios con React Hook Form/Zod, validación, errores y estados de carga. |
| `08-accesibilidad-responsive-y-usabilidad.md` | Accesibilidad, teclado, foco, responsive, densidad y ergonomía docente. |
| `09-nomenclatura-typescript-react.md` | Convenciones de nombres para archivos, componentes, hooks, tipos y tests. |
| `10-testing-calidad-y-automatizacion.md` | Pirámide de pruebas frontend, Testing Library, mocks y quality gates. |
| `11-seguridad-auth-y-privacidad.md` | Firebase Auth, tokens, datos sensibles, autorización server-side y privacidad. |
| `12-observabilidad-errores-y-telemetria.md` | Manejo de errores, logging cliente, métricas UX y trazabilidad. |
| `13-guia-ai-assisted-development.md` | Reglas para Copilot, Claude Code, Codex u otros agentes de desarrollo. |
| `14-checklists.md` | Checklist operativo para features, componentes, PRs y releases frontend. |
| `15-backend-frontend-contracts.md` | Contratos API, DTOs, estados, errores y coordinación con el backend Java. |

## Regla principal

> La página compone. El componente presenta. El hook decide la interacción. El servicio habla con el exterior. El Design System mantiene la coherencia.

## Baseline recomendado

- Next.js App Router.
- React con TypeScript estricto.
- TSX obligatorio para páginas, layouts, componentes y tests React.
- Componentes pequeños, explícitos y testeables.
- Design System con tokens semánticos.
- Hooks propios para lógica interna de componentes no triviales.
- React Hook Form + Zod para formularios.
- Jest + Testing Library para pruebas de comportamiento.
- Accesibilidad y responsive como requisito de merge.

## Glosario rápido

| Término | Definición |
|---|---|
| Layout | Estructura persistente de una zona de la app: shell, navegación, header, providers. |
| Page | Entrada de ruta del App Router; coordina composición, datos de alto nivel y shell config. |
| Section | Bloque mayor de una página con propósito de negocio visible. |
| SubSection | Bloque interno de una section que agrupa una tarea, lista, resumen o estado. |
| Component | Unidad reusable de UI con contrato claro. Puede tener hook propio. |
| MiniComponent | Pieza pequeña de presentación usada por un componente padre. |
| MicroComponent | Elemento mínimo: icon button, badge, field hint, status dot, skeleton unit. |
| Design System | Componentes, tokens y patrones visuales compartidos. |
| Token | Variable semántica de diseño: color, spacing, radius, shadow, font, ring. |
| Hook de componente | Hook local que concentra estado, derivaciones, handlers y efectos de un componente. |
| Page Data Loader | Fachada por pantalla que orquesta varias llamadas API y devuelve un view model listo para render. |
| Maqueta funcional | Implementación navegable con datos fake o simulados, útil para validar UX antes de conectar backend. |
