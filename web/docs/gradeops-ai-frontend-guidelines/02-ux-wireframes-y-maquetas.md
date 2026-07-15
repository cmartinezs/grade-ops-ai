# 02 — UX, wireframes y maquetas funcionales

## 1. Antes del código: entender la tarea

Toda pantalla debe comenzar con una respuesta clara:

- ¿Quién usa esto?
- ¿Qué quiere lograr?
- ¿Qué información necesita para decidir?
- ¿Qué acción principal debe tomar?
- ¿Qué riesgo existe si se equivoca?
- ¿Qué datos vienen del backend?
- ¿Qué estados alternativos existen?

Para GradeOps AI, el usuario principal es un docente con tiempo limitado. La UI debe reducir carga cognitiva, no impresionar visualmente.

## 2. Flujo mínimo de diseño

Para una feature visible, seguir este orden:

1. Definir objetivo de usuario.
2. Escribir flujo principal en pasos.
3. Identificar estados secundarios.
4. Hacer wireframe de baja fidelidad.
5. Definir jerarquía de componentes.
6. Crear maqueta funcional con datos fake si el flujo es nuevo.
7. Validar copy, densidad y acciones.
8. Conectar API real.
9. Agregar pruebas.

El wireframe puede ser texto, Markdown, Figma, captura o HTML simple. Lo importante es decidir antes de codificar.

## 3. Wireframe textual recomendado

Formato simple:

```md
## Pantalla: Evaluaciones

Usuario: docente autenticado.
Objetivo: encontrar evaluaciones abiertas y revisar pendientes.

Layout:
- AppShell protegido.
- Header: título, subtítulo, acción primaria.
- Section: resumen de métricas.
- Section: filtros y búsqueda.
- Section: tabla/lista de evaluaciones.

Acción primaria:
- Nueva evaluación.

Estados:
- Loading: skeleton de métricas y lista.
- Empty: CTA para crear primera evaluación.
- Error: retry + mensaje claro.
- Sin permisos: explicación + contacto/admin.
```

## 4. Maqueta funcional

Una maqueta funcional es una implementación navegable que permite validar:

- Jerarquía visual.
- Densidad de información.
- Flujo de clicks.
- Estados loading/empty/error.
- Responsive.
- Microcopy.
- Composición de componentes.

Puede usar datos fake y no necesita backend real. Pero debe ser suficientemente cercana al producto final para detectar problemas de UX.

## 5. Cuándo exigir maqueta funcional

Debe existir maqueta funcional cuando:

- Es una pantalla nueva.
- Cambia un flujo crítico.
- Tiene formularios multi-step.
- Contiene revisión/aprobación humana.
- Muestra resultados generados por IA.
- Combina filtros, listas y acciones masivas.
- Tiene visualización de datos.
- El backend aún no está listo.

Puede omitirse cuando:

- Es una corrección visual menor.
- Es una pantalla placeholder.
- Es un cambio interno sin impacto UI.
- Es una variante ya cubierta por un patrón existente.

## 6. Datos fake con intención

Los datos fake deben cubrir casos reales:

- Títulos largos.
- Nombres con acentos.
- Estados distintos.
- Fechas vencidas y futuras.
- Cero elementos.
- Muchos elementos.
- Errores parciales.
- Texto generado por IA largo.
- Permisos restringidos.

Evitar mocks perfectos con tres tarjetas simétricas. Esos mocks no prueban la UI.

## 7. Jerarquía visual

Cada pantalla debe tener:

- Un objetivo principal reconocible.
- Una acción primaria clara.
- Acciones secundarias con menor peso.
- Información crítica visible sin scroll excesivo.
- Estados y etiquetas comprensibles.
- Agrupaciones por tarea, no por tipo técnico.

No todas las pantallas necesitan hero, ilustraciones o grandes cards. En un producto operativo, la densidad organizada suele ser mejor que una composición decorativa.

## 8. Microcopy

El texto de UI debe ser:

- Concreto.
- Breve.
- Orientado a acción.
- Consistente en tono.
- En español claro para docentes.

Preferir:

- "Crear evaluación"
- "No hay entregas por revisar"
- "Tu sesión expiró. Inicia sesión de nuevo."

Evitar:

- "Submit"
- "Oops"
- "Algo salió mal" como único detalle.
- Mensajes técnicos del backend sin traducción.

## 9. Estados de aprobación humana

En flujos con IA, la UI debe hacer explícito:

- Qué fue generado por IA.
- Qué está pendiente de revisión humana.
- Qué fue aprobado.
- Qué fue editado por el docente.
- Qué se publicará al estudiante.
- Qué no se puede deshacer.

El humano conserva autoridad pedagógica. La interfaz debe reflejarlo.

## 10. Resultado esperado del diseño

Antes de implementar contra API real, debe existir una respuesta para:

- Componentes necesarios.
- Hooks necesarios.
- DTOs requeridos.
- Estados de UI.
- Validaciones.
- Eventos de usuario.
- Tests mínimos.
- Riesgos de UX.
