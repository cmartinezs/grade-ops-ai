# Portal Docente — UI kit

Recreación interactiva del portal del profesor de GradeOps AI. Abre `index.html`.

**Flujo:** Login (Google o email) → Panel de control → (clic en una evaluación) Corrección con IA → Publicar. El botón “Nueva evaluación” abre el builder con el banco de preguntas.

**Pantallas**
- `LoginScreen.jsx` — SSO Google + email/password, panel de marca con testimonio.
- `DashboardScreen.jsx` — KPIs, evaluaciones recientes (filtros por tipo), cobertura por curso, alumnos en riesgo.
- `BuilderScreen.jsx` — detalles + configuración (fechas, atraso, penalización, alcance) y banco de preguntas (personal/global) con preview.
- `GradingScreen.jsx` — corrección de evaluación abierta: rúbrica por índices, nivel de desempeño y **motivo** por criterio, feedback editable, puntos a mejorar, recalificar con IA.
- `Shell.jsx` — sidebar + topbar. `App.jsx` — ruteo + toasts + confirmaciones. `icons.jsx` — helper Lucide.

Demuestra las tres convenciones del producto: toasts + loaders en acciones asíncronas, y `ConfirmDialog` antes de publicar.
