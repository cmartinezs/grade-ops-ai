# 00 — Principios rectores

## 1. La interfaz debe proteger el flujo docente

GradeOps AI no debe organizar la UI alrededor de modas visuales, librerías o componentes aislados. Debe organizarla alrededor de tareas reales del docente:

- Crear evaluaciones.
- Configurar rúbricas.
- Revisar entregas.
- Aprobar o corregir feedback generado por IA.
- Comparar desempeño de estudiantes.
- Identificar brechas de aprendizaje.
- Preparar reportes.
- Administrar créditos y consumo.
- Revisar auditoría de decisiones AI.

La tecnología es reemplazable. El flujo docente no.

## 2. La UI no contiene reglas de negocio autoritativas

El frontend puede:

- Guiar al usuario.
- Validar formato.
- Deshabilitar acciones obviamente inválidas.
- Mostrar estados derivados para mejorar comprensión.
- Preparar comandos para el backend.
- Manejar carga, error y estados vacíos.

El frontend no debe:

- Decidir permisos reales.
- Calcular notas finales como fuente de verdad.
- Consumir créditos directamente.
- Aprobar decisiones AI sin pasar por backend.
- Reemplazar validaciones de dominio del backend.
- Ocultar errores de seguridad o autorización.

La UI mejora la experiencia; el backend conserva autoridad de negocio.

## 3. Diseñar antes de implementar

Una feature visible debe tener, como mínimo:

- Objetivo del usuario.
- Flujo principal.
- Estados alternativos.
- Wireframe textual o visual.
- Maqueta funcional si el flujo no es trivial.
- Componentes y hooks previstos.
- Contratos API involucrados.
- Criterios de aceptación verificables.

El código sin diseño previo suele producir páginas difíciles de mantener, estados incompletos y experiencia inconsistente.

## 4. La página compone; el componente presenta; el hook decide

Regla base:

- `Page`: compone la ruta y conecta contexto de pantalla.
- `Section`: agrupa bloques de negocio.
- `Component`: presenta una unidad reusable.
- `Hook`: concentra lógica, handlers, efectos y derivaciones.
- `lib/api`: habla con el backend.
- `types`: expresa contratos compartidos.

Evitar páginas con mucha lógica embebida. Una página de 300 líneas con formularios, fetch, estados, handlers y estilos mezclados debe dividirse.

## 5. La consistencia visual vale más que la creatividad local

Preferir:

- Componentes del Design System.
- Tokens semánticos: `--text-body`, `--surface-card`, `--brand`, `--ring`.
- Espaciado, tipografía y radius ya definidos.
- Iconografía compartida.
- Patrones repetibles de loading, empty, error y success.

Evitar:

- Colores raw sin semántica.
- Tailwind arbitrario mezclado con DS sin razón.
- Gradientes decorativos por feature.
- Botones con variantes inventadas.
- Cards dentro de cards sin necesidad.
- Estilos inline grandes copiados entre páginas.

## 6. Cada estado de UI es parte del contrato

Una pantalla no está lista si solo funciona en el caso feliz. Debe considerar:

- Loading inicial.
- Loading de acción.
- Empty state.
- Error recuperable.
- Error bloqueante.
- Sin permisos.
- Sesión expirada.
- Datos parciales.
- Latencia alta.
- Pantalla móvil.
- Navegación por teclado.

Los estados no son detalles visuales; son comportamiento del producto.

## 7. KISS primero, composición después

No crear frameworks internos antes de tener presión real. Crear abstracciones cuando:

- Dos pantallas comparten el mismo patrón de interacción.
- Un componente tiene variantes reales y estables.
- Una lógica se repite con el mismo contrato.
- Un flujo requiere coordinación entre varias secciones.
- Un patrón de error/loading/empty ya aparece en varias features.

Duplicar dos líneas de JSX puede ser más sano que crear un componente genérico confuso.

## 8. DRY no significa ocultar intención

No duplicar:

- Validaciones complejas.
- Mapeo de estados.
- Formateo de fechas/monedas/porcentajes.
- Contratos DTO.
- Acceso a API.
- Lógica de permisos visuales.

Se permite duplicar:

- Microcopy específico de una pantalla.
- Layout local simple.
- Variantes visuales experimentales en una maqueta funcional.

Un componente compartido debe mejorar legibilidad, no esconder el propósito de la pantalla.

## 9. Accesibilidad y responsive son requisitos, no extras

Todo cambio visible debe cuidar:

- Semántica HTML.
- Labels y `aria-label` en controles.
- Orden de foco.
- Contraste.
- Estados hover/focus/disabled.
- Navegación móvil.
- Textos que no se corten ni se superpongan.
- Áreas clicables suficientes.

Una UI que solo se puede usar con mouse y pantalla grande está incompleta.

## 10. Una feature frontend no está terminada sin pruebas

Toda feature debe incluir al menos:

- Tests de render de estados principales.
- Tests de interacción crítica.
- Tests de validación si hay formulario.
- Tests de integración con API mockeada si consume datos.
- Tests de regresión para bugs corregidos.

La prueba debe validar comportamiento observable por el usuario, no detalles internos irrelevantes.
