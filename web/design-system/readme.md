# GradeOps AI — Design System

> **GradeOps AI** es una plataforma para docentes que permite **generar evaluaciones con apoyo de IA** y **corregirlas de forma masiva con IA**. Cubre evaluaciones **abiertas** (rúbricas automáticas por criterios), **cerradas** (selección múltiple con clave de respuestas) y **mixtas**, con bancos de preguntas personales y globales, entregas físicas o digitales, retroalimentación automática y un panel de control académico para el profesor.

This repository is the **design system**: brand, tokens, foundations, reusable React components, and full-screen UI kits for both portals.

---

## Product context

GradeOps AI has **two separate portals**:

| Portal | Quién | Acceso | Qué hace |
|---|---|---|---|
| **Docente** | Profesores | Email + contraseña **o** Google SSO | Crear/reutilizar evaluaciones (abiertas, cerradas, mixtas), banco de preguntas, registrar alumnos por curso/asignatura, corregir con IA, panel de control y reportes por grupo. |
| **Alumno** | Estudiantes | **Magic link** enviado al correo por el docente | Responder evaluaciones en el sistema y ver resultados. El magic link de resultados reúne **todas** las evaluaciones del alumno, sin importar el profesor. |

**Conceptos de dominio clave:**
- **Tipos de evaluación:** abierta (rúbrica), cerrada (clave de respuestas), mixta.
- **Preguntas cerradas:** alternativa única, múltiple, o verdadero/falso. Desde banco **personal** o **global**.
- **Rúbricas (abiertas):** cada rúbrica tiene **índices de evaluación**; cada índice se contrasta con una **tabla de desempeño** (escala de logro). La IA decide nivel + entrega el **motivo**.
- **Entregas:** en el sistema (magic link), físicas (hoja de respuestas que la IA lee), o digitales (docx/pdf/pptx, .zip/.rar de proyectos — múltiples archivos).
- **Configuración de evaluación:** fecha inicio/fin, permitir respuestas fuera de tiempo, penalización por atraso, alcance personal/global.
- **Feedback:** completo, con motivos y puntos más bajos a mejorar.

### Sources
No codebase, Figma, or brand assets were provided. **This system is an original brand** built from the product description. The logo, color palette, type pairing, and iconography choices are proposals — see **Caveats** at the bottom and the open questions for the user.

---

## Content fundamentals

**Idioma:** Español (Chile). Es la lengua de toda la interfaz y los ejemplos. Usa convenciones locales: notas en escala **1,0–7,0** con **coma decimal** (“promedio 5,4”), cursos como “2°A”, “1°B”.

**Tono:** cercano, claro y profesional — como una colega que respeta el tiempo del docente. Nunca infantil, nunca corporativo-frío. El producto **ahorra tiempo**; el copy lo refleja (“Corrige una prueba en minutos”).

**Persona:**
- Con el **docente** se usa el trato directo y cálido: “Bienvenida de vuelta”, “¿Eliminar evaluación?”, “Tu profesora recibió tu entrega”. Tuteo (tú), no usted.
- Con el **alumno**, motivador y constructivo: se nombra primero la fortaleza, luego lo mejorable. “Buen dominio conceptual, Diego. Para subir tu nota, enfócate en…”.

**Casing:** Títulos y botones en **sentence case** (“Nueva evaluación”, “Guardar evaluación”), nunca Title Case ni MAYÚSCULAS. Las únicas mayúsculas son los **eyebrows/overlines** (`.gops-eyebrow`, 0.08em tracking).

**Verbos de acción** concretos en botones: “Crear”, “Guardar”, “Publicar y enviar enlace”, “Recalificar con IA”, “Generar preguntas con IA”. Evita “Enviar formulario” genérico.

**Estados asíncronos** se nombran en gerundio: “Guardando…”, “Corrigiendo con IA…”, “Subiendo archivos…”.

**Emoji:** **no se usan** en la interfaz. La expresividad viene del color, los íconos Lucide y la tipografía display.

**Mensajes de confirmación** explican la consecuencia, no solo preguntan: “Se eliminarán 32 entregas y sus correcciones. Esta acción no se puede deshacer.”

---

## Visual foundations

**Idea de marca:** *crecimiento + logro*. Un verde-teal (“Sprout”) representa el aprendizaje y conduce las acciones primarias; un dorado cálido (“Gold”) marca el logro, los destacados y lo compartido globalmente. Neutros cálidos (“Slate”) mantienen la calma en sesiones largas de corrección.

**Color**
- **Primario — Sprout** (`--brand` = `--sprout-600` `#0B9268`): botones primarios, navegación activa, foco, progreso.
- **Acento — Gold** (`--accent` = `--gold-400` `#F2A524`): CTAs celebratorios, badges “Global”, créditos IA, puntos a mejorar.
- **Neutros — Slate** cálidos (tinte verde-oliva sutil), desde `--slate-0` blanco hasta `--slate-950`. Texto, superficies, bordes.
- **Semánticos:** success (verde), warning (ámbar), danger (rojo cálido), info (azul). Cada uno con par `-50` (fondo) y `-700` (texto).
- **Escala de rúbrica** (5 pasos): Logrado destacado → Logrado → En desarrollo → Inicial → No logrado (`--perf-*`). Es el sistema de color más distintivo del producto.
- **Fondo de página:** `--slate-50` (`#F8F8F5`), un papel cálido — no blanco puro — para reducir fatiga visual.

**Tipografía**
- **Display/Headings:** Bricolage Grotesque (700/600). Carácter estructurado y amistoso. Tracking ajustado (`-0.02em`).
- **Cuerpo/UI:** Hanken Grotesque (400/500/600). Muy legible a tamaños pequeños de tabla.
- **Mono:** JetBrains Mono. Códigos, magic-link tokens, IDs, pesos de rúbrica.
- Base de lectura 15px; tablas densas hasta 13px; nunca < 11px.

**Espaciado y layout:** grilla base 8px (con medios pasos para tablas). Sidebar 256px, topbar 64px, ancho de contenido máx. 1200px.

**Radios:** redondeados y amistosos sin abusar de la píldora. Controles `10px` (`--radius-md`), tarjetas `14px` (`--radius-lg`), paneles/modales `20px`. Píldora solo en badges y switches.

**Tarjetas:** fondo blanco, borde `1px --border-subtle`, sombra `--shadow-sm`, radio `lg`. Variante `accent` con barra superior verde de 3px. Variante `interactive` con hover-lift (`translateY(-2px)` + `--shadow-lg`).

**Sombras:** suaves, de tinte cálido, baja difusión (`rgba(26,26,22,…)`). De `xs` a `xl`, más una `--shadow-brand` verde para botones primarios en hover. Nada de sombras duras ni neón.

**Bordes:** `1px` por defecto; `1.5px` en controles de selección (checkbox/radio). Color `--border-subtle`/`--border-default`/`--border-strong`.

**Movimiento:** rápido y suave. `--dur-fast 120ms` (hover/press), `--dur-base 200ms` (entradas), `--dur-slow 320ms` (progreso). Easing por defecto `--ease-out`. El `--ease-spring` (rebote leve) se reserva para toasts y micro-confirmaciones (check de checkbox, thumb del switch) — nunca en navegación.

**Hover:** superficies → `--surface-sunken`; botón primario → tono más oscuro + `--shadow-brand`; tarjeta interactiva → lift. **Press:** `translateY(1px)` (sin shrink).

**Foco:** anillo `--ring` (3px verde translúcido) vía `box-shadow`, nunca outline duro.

**Imágenes/ilustración:** el sistema es UI-first; no usa fotografía. Los acentos visuales son superficies de marca (gradientes verdes en login/resultados) y los íconos Lucide. Gradientes **solo** en superficies de marca grandes (panel de login, tarjeta de puntaje del alumno) — nunca en fondos de página ni tarjetas de contenido.

**Transparencia/blur:** overlays de modal con `backdrop-filter: blur(2px)` y `--slate-950` al 45%. `LoadingOverlay` usa blur leve sobre la superficie.

---

## Iconography

- **Sistema:** [**Lucide**](https://lucide.dev) vía CDN (`unpkg.com/lucide`). Trazo **2px redondeado**, grilla 24px — coincide con la voz amistosa-pero-estructurada de la marca.
- **SUSTITUCIÓN:** no se entregó un set de íconos propio, así que Lucide es la elección base. Si existe un set de marca, reemplazar y documentar aquí.
- **Uso en código:** los UI kits incluyen `icons.jsx`, un helper `<Icon name="file-pen-line" size={20} />` que construye el SVG desde los datos de Lucide (idempotente entre renders). Color vía `currentColor`.
- **Logo de marca:** SVG propios en `assets/` (`logo-mark.svg`, `logo-wordmark.svg`, `logo-wordmark-light.svg`). El mark es un check dentro de un cuadrado redondeado verde con un punto dorado — “corregido + logro”.
- **Emoji / unicode como íconos:** no se usan.

---

## Index / manifest

**Raíz**
- `styles.css` — punto de entrada global (solo `@import`s). Los consumidores enlazan **este** archivo.
- `tokens/` — `colors.css`, `typography.css`, `spacing.css` (radius/shadow/motion/z), `fonts.css` (Google Fonts), `base.css` (reset + utilidades).
- `assets/` — logos SVG.
- `readme.md` — este documento.
- `SKILL.md` — manifiesto de Agent Skill.

**Componentes** (`components/`, namespace global `GradeOpsAIDesignSystem_fcd12b`)
- `core/` — Button, IconButton, Badge, Card, Avatar, **Tag**, **Tooltip**
- `forms/` — Field, Input, Textarea, Select, Checkbox, Radio, Switch
- `feedback/` — Spinner, LoadingOverlay, ProgressBar, Toast, ToastViewport, Dialog, ConfirmDialog
- `data/` — Tabs, StatCard, RubricLevel

**Templates** (`templates/`) — puntos de partida listos para proyectos consumidores:
- `teacher-portal/` — `TeacherPortal.dc.html`: shell completo del Portal Docente (sidebar, topbar, dashboard con stat cards y lista de evaluaciones).
- `student-portal/` — `StudentPortal.dc.html`: página de respuesta de evaluación por magic link (topbar, progreso, preguntas, submit).

**UI kits** (`ui_kits/`) — recreaciones interactivas de alta fidelidad para referencia visual:
- `teacher/` — Portal Docente: login (Google + email), panel de control, builder + banco de preguntas, corrección con IA. `index.html` es interactivo.
- `student/` — Portal Alumno: responder evaluación por magic link, resultados con feedback + rúbrica + puntos a mejorar.

**Foundations** (`guidelines/foundations/`) — tarjetas de especimen para la pestaña Design System:
- **Colors** (5 cards): Sprout, Gold, Slate neutrals, Semantic status, Rubric performance ladder.
- **Type** (4 cards): Display (Bricolage Grotesque), Body (Hanken Grotesque), Mono (JetBrains Mono), Type scale.
- **Spacing** (5 cards): Spacing scale, Radius, Shadows, **Motion** (timing + easing).
- **Brand** (3 cards): Logo lockups, Iconography (Lucide).

---

## Conventions worth knowing

These three product rules from the brief are baked into components — keep them:
1. **Toda acción asíncrona** se indica con **toast** + **loader en el componente involucrado** (`Spinner` / `LoadingOverlay` / `Button loading` / `ProgressBar striped`). No repetir loaders globales.
2. **Antes de salir** con guardado/proceso pendiente: validar y confirmar (`ConfirmDialog`).
3. **Toda acción que modifica datos** (eliminar, publicar, compartir) se **confirma previamente** (`ConfirmDialog`, `tone="danger"` si es destructiva).
