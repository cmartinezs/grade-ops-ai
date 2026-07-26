# Principios y gobierno UI/UX

## Objetivo

Establecer la autoridad, las reglas transversales y la trazabilidad necesarias para diseñar GradeOps AI sin anticipar decisiones visuales ni prometer capacidades que la plataforma no pueda sostener.

## Principios

- Priorizar la tarea docente sobre la estética.
- Reducir carga cognitiva, pasos y decisiones innecesarias.
- Diseñar primero el flujo y la semántica; luego la interfaz visual.
- Mantener consistencia mediante tokens, patrones y componentes reutilizables.
- Considerar desde el inicio carga, vacío, error, éxito, permisos, timeout y recuperación.
- Adoptar WCAG 2.2 nivel AA como requisito transversal.
- Tratar la IA como asistencia identificable, revisable y reversible, nunca como autoridad académica.
- Proteger datos personales y académicos mediante privacidad por diseño y mínimo privilegio.
- Hacer visible el estado real de procesos asíncronos y resultados parciales.
- Validar decisiones con evidencia observable y declarar cuando la evidencia externa esté pendiente.
- Registrar el impacto potencial de toda decisión que afecte dominio, datos, API, eventos, seguridad o migraciones.

## Decisiones de gobierno aceptadas

### Autoridad y validación

Carlos ejerce inicialmente las funciones de Product Owner, responsable UX y responsable técnico. Puede solicitar feedback a docentes externos, pero su disponibilidad no bloquea etapas ni releases.

Cada decisión UX utiliza uno de estos estados:

- `Internally validated`: revisada y aprobada por Carlos.
- `Externally validated`: respaldada por evidencia de uno o más docentes.
- `External evidence pending`: habilitada para avanzar con la hipótesis y el riesgo registrados.
- `Invalidated`: la evidencia exige revisar la decisión.

Las responsabilidades y aprobaciones se detallan en [`roles-and-authority.md`](../governance/roles-and-authority.md).

### Accesibilidad

WCAG 2.2 nivel AA es el estándar general desde diseño hasta implementación. Una herramienta automática no acredita por sí sola el cumplimiento. Las excepciones deben identificar criterio incumplido, justificación, impacto, mitigación, responsable y fecha de revisión.

### Alcance responsive

GradeOps AI es responsive desde el inicio y orientado principalmente a escritorio:

| Contexto | Alcance inicial |
|---|---|
| Escritorio y notebook | Experiencia completa y máxima productividad |
| Tablet | Experiencia completa adaptada, especialmente para evaluación |
| Móvil | Consulta, seguimiento y acciones breves |
| Pantallas muy pequeñas | Información crítica y tareas de una sola intención |

La capacidad disponible depende también de complejidad, precisión, duración, riesgo y contexto de uso. No se comprimen artificialmente flujos complejos para conservar paridad funcional.

### Control humano sobre IA

Todo contenido generado por IA debe identificarse. Las sugerencias se pueden revisar, editar, regenerar y rechazar. La IA no publica notas, evaluaciones ni retroalimentación, ni ejecuta acciones académicas sensibles sin confirmación humana.

Los estados conceptuales mínimos de contenido son `generated`, `edited`, `reviewed` y `published`. Su modelo definitivo se resuelve en dominio, no en este documento.

### Acciones críticas

GradeOps AI prioriza prevención, reversibilidad y trazabilidad sin añadir confirmaciones indiscriminadas:

- borradores con estado y última actualización visibles;
- confirmación antes de publicar o enviar resultados;
- historial y recuperación cuando sean técnica y legalmente viables;
- operaciones masivas con resumen previo de alcance y consecuencias;
- idempotencia y prevención de doble ejecución;
- actor y fecha para acciones académicas relevantes.

### Privacidad y permisos

La plataforma aplica privacidad por diseño, mínimo privilegio y permisos contextuales. La UI comunica autorizaciones; la API siempre las garantiza.

- No se exponen datos personales innecesarios en tablas, exportaciones, demos, logs o telemetría.
- La interfaz informa cuándo un contenido será procesado mediante IA.
- No se envían datos identificables a proveedores de IA sin necesidad y autorización.
- El acceso se aísla por organización, curso, sección y relación del usuario con el recurso.
- Los roles y permisos concretos se derivan posteriormente de los casos de uso.

### Procesos asíncronos

Las operaciones prolongadas muestran estado y progreso real cuando esté disponible. El usuario puede abandonar la pantalla sin perder el proceso y volver a su resultado.

Los estados conceptuales mínimos son `pending`, `processing`, `completed`, `partially_completed`, `failed` y `cancelled`. Los errores parciales identifican elementos afectados y opciones seguras de recuperación o reintento.

### Internacionalización

Todo identificador, contrato, estructura y elemento técnico se define en inglés. Todo contenido dirigido al usuario se localiza según idioma, región y contexto institucional. Los mensajes traducidos nunca controlan la lógica.

La política aplica a `web`, `api`, `agents`, correos, notificaciones, exportaciones y procesos asíncronos:

| Contenido | Responsabilidad principal |
|---|---|
| Navegación, formularios y errores de dominio | `web`, mediante `code + parameters` |
| Correos, notificaciones y exportaciones backend | `api`, según locale resuelto |
| Contenido académico generado | `agents`, según idioma solicitado |
| Estados, campos, códigos, logs y trazas | Inglés técnico |

La precedencia conceptual es: locale de operación, preferencia de usuario, configuración institucional, `Accept-Language`, `es-CL` y fallback `en`. Idioma, región, zona horaria y escala de calificación son configuraciones independientes.

Los contratos hacia `agents` distinguen, cuando aplique:

```json
{
  "interfaceLocale": "es-CL",
  "outputLanguage": "es",
  "sourceLanguage": "en",
  "timeZone": "America/Santiago"
}
```

`outputLanguage` gobierna el contenido generado; los campos, estados y códigos permanecen en inglés. El sistema conserva la evidencia original y no traduce silenciosamente respuestas estudiantiles, citas o nombres propios.

## Fuentes de verdad

| Información | Fuente de verdad |
|---|---|
| Problema, usuarios y evidencia | `design-system/research/` |
| Decisiones UX, estado de validación y excepciones | `design-system/decisions/` |
| Arquitectura y reglas de interacción | `design-system/patterns/` y `design-system/screens/` |
| Diseño visual y prototipo | Figma |
| Tokens y componentes ejecutables | `web/design-system/` y código de `web/` |
| Comportamiento funcional | Historias y criterios de aceptación |
| Contratos y reglas de dominio | `api/`, documentación canónica y pruebas |
| Implementación efectiva | Código y pruebas del artefacto correspondiente |

Figma representa el diseño aprobado, pero no sustituye el registro versionado de decisiones, reglas y justificaciones.

## Registro de decisiones e impactos

Las decisiones relevantes se documentan con la plantilla [`ADR-UIUX-template.md`](../decisions/ADR-UIUX-template.md). Toda capacidad que requiera persistencia, estados, autorización, trazabilidad o procesamiento asíncrono se agrega al [`Data Model Impact Ledger`](../decisions/data-model-impact-ledger.md).

El ledger identifica trabajo técnico potencial; no autoriza a diseñar el esquema de base de datos desde las pantallas.

## Política de herramientas generativas

Los resultados de Stitch, Figma Make, v0, Codex o Claude Code son propuestas. Ninguna herramienta aprueba alcance, UX, UI, accesibilidad, arquitectura ni excepciones.

## Criterio de salida

La etapa 01 se considera completa cuando:

- los principios y decisiones aceptadas están versionados;
- autoridad, responsabilidades y aprobaciones están definidas;
- las fuentes de verdad no son ambiguas;
- existe una plantilla para decisiones UI/UX;
- los impactos arquitectónicos conocidos están registrados sin resolverlos prematuramente;
- las excepciones tienen un proceso explícito;
- se cumple el [`Gate 0`](09-quality-gates.md#gate-0--gobierno-preparado);
- no se han anticipado decisiones visuales.

El inicio de la etapa 02 se rige por [`definition-of-ready.md`](../governance/definition-of-ready.md).
