<a id="top"></a>

# GradeOps AI Design System

Esta carpeta es el espacio raíz de contenido para el Design System de GradeOps AI.

Úsala para definir lenguaje visual, patrones UX, reglas de contenido, decisiones de diseño y flujo profesional antes de traducirlos a artefactos implementables dentro de `web/`.

## Propósito

El Design System mantiene consistencia entre el workspace docente, los flujos de alumno por enlace seguro, los dashboards de evidencia y las operaciones de evaluación asistidas por IA.

Antes de implementar una pantalla, este espacio debe ayudar a responder:

1. Qué patrón visual y de interacción corresponde.
2. Qué componente, token o regla del Design System gobierna el comportamiento.
3. Qué semántica de datos tiene cada control: texto libre, enum, catálogo, número, booleano, fecha, contenido generado editable o evidencia de solo lectura.
4. Qué estados de evidencia, aprobación, carga, error, vacío, timeout y reintento deben quedar visibles.

## Contenido Actual

| Ruta | Propósito |
|------|-----------|
| [`workflow/`](workflow/) | Flujo profesional UI/UX: descubrimiento, arquitectura, Stitch, Figma, validación, v0, integración y quality gates |
| [`governance/`](governance/) | Autoridad, responsabilidades y condiciones para iniciar una etapa |
| [`decisions/`](decisions/) | Decisiones UI/UX y registro de impactos potenciales en la plataforma |

## Relación Con `web/design-system`

Ya existe un Design System orientado a implementación en [`../web/design-system`](../web/design-system).

Usa esta carpeta raíz para contenido fuente y guía de producto:

- principios de diseño;
- decisiones UX y semántica de datos;
- notas por pantalla;
- especificaciones de comportamiento de componentes;
- reglas de copy, tono y mensajes;
- checklists de revisión;
- flujo profesional de diseño y validación.

Usa `web/design-system` para artefactos consumibles por la app:

- tokens;
- CSS;
- logos y assets visuales;
- referencias de componentes;
- UI kits;
- templates listos para implementación.

Cuando una decisión de diseño se vuelva ejecutable en la app web, actualiza ambos lugares de forma deliberada: la justificación y regla de producto vive aquí; el artefacto técnico vive en `web/design-system`.

## Estructura Recomendada

| Carpeta | Propósito |
|---------|-----------|
| `workflow/` | Proceso trazable de diseño, validación e implementación |
| `governance/` | Autoridad, quality gates y condiciones de avance |
| `decisions/` | ADR UI/UX e impactos sobre dominio, datos, API y seguridad |
| `foundations/` | Marca, color, tipografía, espaciado, movimiento, iconografía y accesibilidad |
| `patterns/` | Patrones de interacción reutilizables y estados de workflow |
| `components/` | Especificaciones de comportamiento antes de implementación TSX |
| `screens/` | Notas por pantalla, rationale de wireframes y semántica UX/data por página |
| `content/` | Reglas de copy en español, tono, labels, estados vacíos, errores y confirmaciones |
| `reviews/` | Checklists de revisión y hallazgos por release |

Crea carpetas solo cuando haya contenido real que agregar. No agregues archivos placeholder vacíos.

## Reglas De Trabajo

- Aplicar las reglas transversales de documentación y navegación definidas en [`../RULES.md`](../RULES.md).
- Mantener la interfaz en español para docentes de Chile.
- Mantener identificadores, contratos, estructuras y elementos técnicos en inglés.
- Localizar todo contenido dirigido al usuario según idioma, región y contexto institucional.
- Tratar al docente como operador en control de un workflow de evaluación, no como receptor pasivo de salidas de IA.
- Diseñar primero el flujo y la semántica de datos; luego la interfaz visual.
- Hacer visibles aprobación, evidencia, costo, modelo, ejecución, error y reintento cuando la IA afecte notas, feedback o contenido visible para estudiantes.
- No inventar tipos locales de frontend. Los controles UI deben reflejar contratos de API y semántica de dominio.
- La corrección de evaluaciones cerradas sigue siendo determinística; la IA puede asistir generación y revisión, no puntuar contra una clave congelada.
- Los flujos de alumno son experiencias por enlace seguro, no portales con login.
- Acciones destructivas, publicación, envío, calificación y contenido visible para estudiantes requieren confirmación explícita.
- Trabajo asíncrono debe exponer progreso, falla, timeout y comportamiento de reintento o cancelación.

## Fuentes Relacionadas

- [`../docs/06-ux`](../docs/06-ux)
- [`../docs/99-decisions/2026-06-21-web-design-system.md`](../docs/99-decisions/2026-06-21-web-design-system.md)
- [`../docs/99-decisions/2026-07-21-ui-design-data-semantics.md`](../docs/99-decisions/2026-07-21-ui-design-data-semantics.md)
- [`../web/design-system`](../web/design-system)

---

[← README del repositorio](../README.md) · [Siguiente: Gobierno UI/UX →](governance/README.md) · [↑ Volver al inicio](#top)
