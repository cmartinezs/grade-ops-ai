# Prompts operativos

## Google Stitch — direcciones visuales

```text
Actúa como diseñador principal de producto SaaS educativo.

Diseña tres direcciones visuales estructuralmente diferentes para el flujo:
[FLUJO].

Usuario: [USUARIO Y CONTEXTO].
Objetivo: [RESULTADO].
Tareas: [TAREAS].
Wireframes y arquitectura: [REFERENCIA].
Marca: [ATRIBUTOS].
Restricciones: [RESPONSIVE, CONTENIDO, TÉCNICAS].
Accesibilidad: WCAG 2.2 AA.

Incluye estados vacío, carga, error, éxito, permisos y revisión de resultados de IA.
Usa contenido realista en español.
No alteres el flujo ni inventes funcionalidades.

Las alternativas deben responder a estos enfoques:
1. máxima eficiencia operativa;
2. experiencia guiada;
3. equilibrio entre eficiencia y marca.

Explica jerarquía, decisiones, riesgos y trade-offs de cada alternativa.
```

## Figma Make — prototipo

```text
Construye un prototipo responsive de alta fidelidad para [FLUJO] usando la
dirección visual seleccionada [REFERENCIA].

Conserva arquitectura, terminología, pasos y reglas del flujo aprobado.
Crea componentes reutilizables con propiedades, variantes y estados.
Define tokens semánticos para color, tipografía, espaciado, borde, elevación,
movimiento y foco.

Incluye:
- camino principal y alternativos;
- vacío, carga, error, éxito y permisos;
- validación de formularios;
- navegación por teclado y foco visible;
- revisión, edición, rechazo y confirmación de contenido generado por IA;
- comportamiento desktop y móvil;
- anotaciones para desarrollo.

No inventes objetos de dominio, funcionalidades ni datos.
```

## v0 — implementación

```text
Implementa exclusivamente [FLUJO O COMPONENTE] en React/Next.js a partir de
[REFERENCIA DE FIGMA Y ESPECIFICACIÓN].

Usa los componentes y tokens existentes: [RUTAS O DOCUMENTACIÓN].
Respeta las convenciones del repositorio: [CONVENCIONES].

Requisitos:
- TypeScript estricto;
- componentes presentacionales separados de acceso a datos;
- responsive conforme a la especificación;
- WCAG 2.2 AA;
- estados vacío, carga, error, éxito y permisos;
- fixtures y mocks aislados;
- pruebas de componentes y accesibilidad;
- sin dependencias nuevas salvo justificación;
- sin endpoints, modelos o reglas inventadas.

Entrega código, pruebas, decisiones y discrepancias detectadas.
```

## Codex o Claude Code — integración

```text
Integra la implementación de [FLUJO] en el repositorio actual.

Antes de modificar:
1. inspecciona arquitectura, convenciones, componentes y pruebas;
2. identifica reutilización y conflictos;
3. presenta un plan breve con archivos afectados y riesgos.

Durante la integración:
- conserva los límites arquitectónicos;
- reutiliza el design system;
- conecta contratos reales sin inventarlos;
- aplica autorización, errores y telemetría;
- elimina duplicación y código provisional;
- implementa pruebas unitarias, de componente, aceptación y E2E pertinentes;
- verifica accesibilidad, responsive y paridad con Figma.

No amplíes el alcance funcional. Registra cualquier desviación necesaria.
Finaliza ejecutando las verificaciones del repositorio y reportando resultados.
```
