# 🔍 DEEPENING: Scope 05 — contact-open-door

> **Status:** DONE
> [← 01-expansion.md](../../01-expansion.md) | [← planning/README.md](../../../README.md)

---

## Objective

Rediseñar la página `/contact` (ES + EN) con identidad "Open Door": fondo con textura de ruido SVG sobre azul medianoche, hover 3D suave en tarjetas de perfil, y contenido enriquecido con la visión del producto y descripción más profunda de cada perfil buscado.

---

## Contexto de diseño

**Identidad:** Cálida, humana, colaborativa — invitación genuina.
**Fondo:** Textura de ruido SVG inline como `background-image`. Base: `#050f1e` (azul medianoche). SVG noise: `<filter id='n'><feTurbulence type='fractalNoise' baseFrequency='0.65' numOctaves='3' stitchTiles='stitch'/></filter><rect width='100%' height='100%' filter='url(#n)' opacity='0.04'/>`. Combinado con `radial-gradient(ellipse 60% 40% at 50% 0%, rgba(0,212,200,0.08) 0%, transparent 60%)`.
**Color dominante:** Teal `var(--teal)` con blancos cálidos.
**Animación hover 3D:** Las 3 tarjetas de perfil (Educator, Developer, Researcher) tienen `transition: transform 300ms ease`. On hover: `transform: perspective(600px) rotateY(6deg) translateY(-4px)`. Se deshabilita con `prefers-reduced-motion: reduce`.

---

## Contenido nuevo a agregar

### Párrafo de visión / por qué colaborar
Fuente: `docs/02-product/personas.md` — sección "Persona Conclusion" y success moments

Texto a mostrar como quote destacado antes del formulario:

> "El problema que resolvemos es para la persona que tiene que convertir 30 entregas de programación en calificaciones justas, feedback útil y una acción de enseñanza clara."

Seguido de 1 párrafo corto: GradeOps AI es un proyecto open-source en desarrollo activo. Buscamos colaboradores que quieran dar forma a cómo la IA opera ciclos de evaluación con control humano real.

### Sección "¿A quién buscamos?"
Ampliar las 3 tarjetas de perfil con contenido más específico:

**Educator / Pilot**
- Perfil: Docente o tutor de programación que quiere probar GradeOps con sus estudiantes.
- Qué necesitamos de ti: Correr un ciclo real de evaluación y darnos feedback directo sobre el workflow.
- Qué obtienes: Acceso gratuito durante el piloto, feedback loop directo con el equipo.

**Developer**
- Perfil: Desarrollador interesado en Spring Boot 4, AI agents, o Astro/Next.js.
- Qué necesitamos: Contribuciones en el pipeline de agentes, el API de dominio, o el workspace del docente.
- Qué obtienes: Participación en un proyecto AI-native real con arquitectura documentada.

**Researcher**
- Perfil: Investigador de educación, evaluación asistida por IA, o HCI.
- Qué necesitamos: Colaboración en el diseño del flujo de aprobación docente y detección de gaps.
- Qué obtienes: Acceso a datos de ejecución de agentes (anonimizados) y co-autoría en outputs.

---

## Tasks

| # | Task | Status | Output |
|---|------|--------|--------|
| 1 | Leer `docs/02-product/personas.md` (sección Persona Conclusion y success moments de P0 personas) para extraer la quote y el párrafo de visión | PENDING | Datos inline en el scope |
| 2 | Modificar `site/src/pages/contact.astro` (ES): agregar fondo noise SVG + radial, hover 3D en tarjetas, quote de visión, ampliar tarjetas de perfil con contenido nuevo | PENDING | `site/src/pages/contact.astro` |
| 3 | Modificar `site/src/pages/en/contact.astro` (EN): mismo diseño, contenido en inglés | PENDING | `site/src/pages/en/contact.astro` |

---

## Done Criteria

- [x] El fondo de ruido SVG + radial teal es visible y da un tono cálido diferente al resto del site
- [x] Las 3 tarjetas de perfil tienen hover 3D suave (`perspective rotateY`) desactivable con `prefers-reduced-motion`
- [x] La quote de visión aparece como bloque destacado antes del formulario
- [x] Cada tarjeta de perfil tiene: perfil, qué necesitamos, qué obtienes
- [x] El formulario de contacto existente se preserva sin cambios funcionales
- [x] Ambas versiones (ES y EN) actualizadas
- [x] TRACEABILITY.md actualizado con nuevos términos de este scope

---

## Inconsistencies Found

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| — | *None yet* | — | — | — |

---

## Residuals

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| — | *None* | — | — |

---

> [← 01-expansion.md](../../01-expansion.md) | [← planning/README.md](../../../README.md)
