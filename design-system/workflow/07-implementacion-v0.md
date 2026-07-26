<a id="top"></a>

# Implementación inicial con v0

## Objetivo

Traducir el diseño aprobado a componentes React/Next.js sin delegar a la herramienta decisiones de producto, dominio o arquitectura.

## Entradas

- flujos y criterios de aceptación;
- pantallas aprobadas;
- tokens;
- inventario de componentes;
- reglas responsive;
- estados;
- contenido;
- convenciones del repositorio.

## Alcance recomendado

Generar por flujo vertical pequeño, no la aplicación completa. Priorizar:

1. estructura de página;
2. componentes reutilizables;
3. estados;
4. comportamiento responsive;
5. accesibilidad;
6. pruebas de interfaz.

## Restricciones

- No inventar endpoints ni modelos de dominio.
- No introducir dependencias sin justificación.
- No duplicar componentes existentes.
- No codificar colores o espaciados fuera de tokens.
- No acoplar componentes de presentación a acceso de datos.
- No aceptar lógica de IA en el cliente.
- No usar datos falsos fuera de fixtures o mocks.

## Entregable

- componentes;
- stories o catálogo equivalente;
- fixtures;
- pruebas;
- documentación de decisiones;
- lista explícita de discrepancias con Figma.

## Criterio de salida

La implementación reproduce comportamiento y jerarquía, reutiliza el design system y puede integrarse sin reescribir su estructura.

---

[← Índice del workflow](README.md) · [Siguiente: Integración y endurecimiento →](08-integracion-produccion.md) · [↑ Volver al inicio](#top)
