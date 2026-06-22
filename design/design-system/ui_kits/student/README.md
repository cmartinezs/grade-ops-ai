# Portal Alumno — UI kit

Recreación interactiva del portal del estudiante de GradeOps AI (acceso por **magic link**). Abre `index.html`.

**Flujo:** Responder evaluación → confirmar envío → ver resultados.

**Pantallas**
- `AnswerScreen.jsx` — responder una evaluación mixta: alternativa única, múltiple, V/F y respuesta abierta. Barra de progreso + guardado automático.
- `ResultsScreen.jsx` — resultados por magic link: nota, nivel de rúbrica, feedback de IA, **puntos a mejorar**, y la lista de **todas** las evaluaciones del alumno (de cualquier profesor).
- `StudentBar.jsx` — barra superior. `App.jsx` — orquesta envío + confirmación + toasts. `icons.jsx` — helper Lucide.

El envío usa `ConfirmDialog` (no se puede modificar después) y muestra toast de confirmación, siguiendo las convenciones del producto.
