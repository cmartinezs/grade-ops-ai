# 08 — Accesibilidad, responsive y usabilidad

## 1. Semántica HTML

Usar HTML según propósito:

- `button` para acciones.
- `a`/`Link` para navegación.
- `form` para formularios.
- `label` para campos.
- `section` para bloques de página.
- `table` para datos tabulares reales.
- `ul`/`ol` para listas.

No usar `div` clickeable si existe elemento semántico.

## 2. Teclado

Toda interacción debe funcionar con teclado:

- Tab para navegar.
- Enter/Space para activar botones.
- Escape para cerrar modal si aplica.
- Focus vuelve a un lugar lógico.
- No hay trampas de foco fuera de modales.

Los icon buttons necesitan `aria-label`.

## 3. Foco

Reglas:

- Focus visible en todos los controles.
- No eliminar outline sin reemplazo accesible.
- Al abrir modal, foco entra al modal.
- Al cerrar modal, foco vuelve al disparador.
- Al mostrar error global, considerar focus o anuncio.

## 4. Contraste

El texto debe tener contraste suficiente. Evitar:

- Gris claro sobre blanco.
- Gold bajo contraste para texto pequeño.
- Texto sobre gradientes sin control.
- Estados disabled indistinguibles.

El color no debe ser la única señal. Usar texto, iconos o labels.

## 5. Responsive

Cada pantalla debe validarse en:

- Mobile angosto.
- Tablet.
- Desktop.
- Desktop ancho.

Reglas:

- Grids colapsan a una columna cuando corresponde.
- Acciones se envuelven sin superponerse.
- Sidebars tienen alternativa móvil.
- Tablas tienen scroll controlado o formato de cards.
- Textos largos usan wrap o ellipsis con tooltip/contexto.
- Contenedores flex usan `minWidth: 0`.

## 6. Densidad para producto operativo

GradeOps AI es una herramienta de trabajo. La UI debe ser:

- Escaneable.
- Predecible.
- Densa pero ordenada.
- Rápida para tareas repetidas.
- Sobria en decoración.

Evitar layouts tipo landing page dentro del workspace docente.

## 7. Estados vacíos accesibles

Un empty state debe:

- Tener heading.
- Explicar el vacío.
- Ofrecer acción o siguiente paso.
- No depender solo de ilustración.

## 8. Modales y drawers

Usar modal/drawer cuando:

- La tarea es secundaria.
- El usuario no debe perder contexto.
- El contenido es acotado.

Evitar modal para:

- Formularios largos.
- Flujos multi-step complejos.
- Edición profunda de evaluación.

Los modales deben tener:

- Título.
- Cierre accesible.
- Focus management.
- Acción primaria y secundaria claras.

## 9. Tablas y listas

Usar tabla cuando se comparan columnas. Usar lista/card cuando el contenido es más narrativo.

Para tablas:

- Headers claros.
- Alineación consistente.
- Estados de fila.
- Acciones al final.
- Empty/loading/error.
- Responsive definido.

## 10. Lectura de feedback AI

El feedback generado por IA puede ser largo. La UI debe:

- Dividir en bloques.
- Diferenciar original, editado y aprobado.
- Permitir expandir/colapsar si es extenso.
- Mantener contexto del estudiante/evaluación.
- Evitar columnas demasiado estrechas para texto largo.

## 11. Prevención de errores

La UI debe prevenir errores humanos:

- Deshabilitar acciones inválidas con explicación.
- Confirmar acciones irreversibles.
- Mostrar preview antes de publicar.
- Indicar cambios sin guardar.
- Mostrar destino de acciones masivas.

## 12. Performance percibida

Para latencia:

- Mostrar feedback inmediato.
- Skeleton si se conoce estructura.
- Mantener layout estable.
- Evitar saltos de contenido.
- Cargar datos críticos primero.
- Diferir detalles secundarios si aplica.
