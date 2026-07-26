<a id="top"></a>

# Integración y endurecimiento

## Objetivo

Adaptar el código generado al repositorio real, su arquitectura, seguridad, observabilidad y estándares de calidad.

## Actividades

1. Comparar implementación con arquitectura vigente.
2. Reutilizar componentes y contratos existentes.
3. Conectar casos de uso mediante adaptadores.
4. Aplicar autorización y manejo seguro de datos.
5. Completar estados de red y recuperación.
6. Añadir telemetría sin datos sensibles.
7. Verificar accesibilidad y rendimiento.
8. Incorporar pruebas.
9. Revisar visualmente contra Figma.

## Cobertura mínima

- pruebas unitarias de lógica de presentación;
- pruebas de componentes;
- pruebas de accesibilidad automatizadas;
- pruebas de aceptación con API simulada;
- regresión visual de componentes críticos;
- E2E del camino principal y recuperaciones esenciales.

## Revisión técnica

- límites entre presentación, aplicación y datos;
- consistencia del dominio;
- seguridad de acciones y permisos;
- hidratación y renderizado;
- tamaño de bundle;
- rendimiento de tablas y formularios;
- internacionalización;
- observabilidad;
- mantenibilidad.

## Criterio de salida

La funcionalidad cumple diseño, criterios de aceptación, arquitectura y quality gates; no depende de código provisional ni decisiones implícitas de la herramienta generadora.

---

[← Índice del workflow](README.md) · [Siguiente: Quality gates →](09-quality-gates.md) · [↑ Volver al inicio](#top)
