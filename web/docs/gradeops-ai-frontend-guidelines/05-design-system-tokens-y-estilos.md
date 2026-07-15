# 05 — Design System, tokens y estilos

## 1. Regla base

Usar primero el Design System de GradeOps AI.

Preferir:

```tsx
import { Button, Card, Badge, Input, IconButton } from "@/components/ds";
```

Antes de crear un nuevo componente visual, revisar si ya existe una pieza DS equivalente.

## 2. Tokens semánticos

En código de producto, preferir tokens semánticos:

- `--text-strong`
- `--text-body`
- `--text-muted`
- `--surface-page`
- `--surface-card`
- `--surface-sunken`
- `--border-subtle`
- `--border-default`
- `--brand`
- `--brand-hover`
- `--ring`

Evitar usar escalas crudas en componentes de feature:

```css
color: var(--sprout-700);
background: var(--slate-50);
```

Las escalas crudas pertenecen al DS o a casos donde el significado visual es explícito y compartido.

## 3. Colores

Usar color con función:

- Verde/brand: acción principal, progreso, foco.
- Gold: logro, créditos, énfasis secundario.
- Info: estado informativo.
- Warning: atención recuperable.
- Danger: error, bloqueo, acción destructiva.
- Neutrales: estructura y lectura.

No usar color solo para decorar. Todo color debe ayudar a entender jerarquía, estado o acción.

## 4. Tipografía

Reglas:

- Usar `--font-display` para headings principales.
- Usar `--font-sans` para UI y cuerpo.
- Usar `--font-mono` solo para IDs, tokens, código o datos técnicos.
- No escalar font-size con viewport.
- No usar letter spacing negativo en componentes compactos.
- Mantener jerarquía clara: título de página, section heading, label, body, helper.

Evitar hero-scale type dentro de cards, tablas o sidebars.

## 5. Espaciado

Usar spacing consistente:

- 4/8/12/16/20/24/32 como escala principal.
- Gaps explícitos en flex/grid.
- Padding estable en cards, rows y botones.
- No depender de márgenes accidentales entre componentes.

En pantallas operativas, preferir densidad organizada sobre aire excesivo.

## 6. Radius y cards

Reglas:

- Cards y paneles: radius moderado.
- Botones e inputs: radius del DS.
- Evitar cards dentro de cards.
- No convertir secciones completas de página en cards flotantes si la pantalla necesita densidad.
- Usar cards para elementos repetidos, modales o herramientas enmarcadas.

## 7. Iconografía

Usar `LucideIcon` o la librería de iconos establecida.

Reglas:

- Iconos en botones de herramienta cuando exista icono familiar.
- `aria-label` obligatorio en icon buttons.
- Icono + texto para acciones no obvias.
- Tooltips para iconos poco frecuentes si se implementa sistema de tooltips.
- No crear SVG manual si ya existe icono equivalente.

## 8. Tailwind vs tokens inline

El repo actual mezcla tokens CSS e instancias Tailwind en algunos componentes. Regla recomendada:

- DS y shell: tokens semánticos.
- Features nuevas: tokens semánticos y componentes DS.
- Tailwind: permitido en prototipos o componentes existentes, pero no introducir una segunda identidad visual.

Si una feature usa Tailwind, debe respetar colores, spacing y tipografía del DS.

## 9. Estilos inline

Los estilos inline son aceptables para:

- Componentes pequeños.
- Variantes controladas.
- Tokens CSS.
- Layout local de una pantalla simple.

Extraer a CSS/module/DS cuando:

- El bloque de estilos domina el componente.
- Se repite en varios archivos.
- Requiere media queries complejas.
- Tiene pseudo-selectores no triviales.
- Representa un patrón compartido.

## 10. Responsive

Cada layout debe definir:

- Grid que colapsa correctamente.
- Anchuras máximas.
- `minWidth: 0` en contenedores flex con texto.
- Wrap de acciones.
- Sidebar móvil si aplica.
- Tablas con alternativa responsive.
- Textos largos sin overflow.

No asumir viewport desktop.

## 11. Estados visuales

Cada componente interactivo debe tener:

- Default.
- Hover cuando aplica.
- Focus visible.
- Active/pressed si aplica.
- Disabled.
- Loading si dispara async.
- Error si recibe validación.

El focus visible no se negocia.

## 12. Skeleton, spinner y loading

Usar spinner para:

- Acciones cortas.
- Submit.
- Carga de página simple.

Usar skeleton para:

- Listas.
- Cards.
- Tablas.
- Paneles con layout conocido.

Evitar pantallas en blanco durante fetch.

## 13. Empty states

Un empty state debe decir:

- Qué está vacío.
- Por qué puede estar vacío si ayuda.
- Qué acción puede tomar el usuario.

Ejemplo:

```tsx
<EmptyState
  title="No hay evaluaciones"
  description="Crea tu primera evaluación para empezar a corregir con IA."
  action={<Button>Crear evaluación</Button>}
/>
```

## 14. Error states

Un error state debe:

- Ser visible.
- Explicar en lenguaje humano.
- Ofrecer retry si aplica.
- No mostrar stack trace.
- No culpar al usuario si es falla técnica.

## 15. Diseño para datos reales

Probar visualmente con:

- Textos largos.
- Nombres largos.
- Cursos con siglas.
- Muchos estados.
- Listas vacías.
- Cantidades grandes.
- Fechas y zonas horarias.
- Feedback IA extenso.

Si solo se ve bien con datos perfectos, la UI no está terminada.
