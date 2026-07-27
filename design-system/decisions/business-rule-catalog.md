<a id="top"></a>

# Catálogo de reglas de negocio del descubrimiento evaluativo

## Propósito

Convertir decisiones aceptadas en reglas identificables y comprobables. Este
catálogo no sustituye los ADR ni define por sí solo clases, tablas o endpoints.
Cada regla deberá enlazarse posteriormente con historias, criterios de
aceptación, componentes de dominio y pruebas automatizadas.

## Convención

```text
Regla
→ historia de usuario
→ criterio de aceptación
→ componente de dominio
→ implementación
→ prueba
→ evidencia
```

Los identificadores son estables. Una modificación semántica debe registrar la
versión o reemplazo correspondiente, no reutilizar el ID de forma ambigua.

## Plantillas y evaluación

| ID | Regla | Alcance | Verificación mínima |
|---|---|---|---|
| BR-ASM-001 | Toda evaluación posee un `AppliedAssessmentProfile` válido. | Foundation | Rechazar creación o publicación sin perfil válido. |
| BR-ASM-002 | Una evaluación derivada conserva plantilla y versión de origen. | Foundation | La procedencia aparece en consulta y auditoría. |
| BR-ASM-003 | Personalizar una evaluación no modifica su plantilla. | MVP | Comparar versión fuente antes y después. |
| BR-ASM-004 | Una versión publicada de plantilla es inmutable. | Foundation | Rechazar actualización de contenido publicado. |
| BR-ASM-005 | Archivar una plantilla no invalida evaluaciones existentes. | Foundation | Evaluaciones derivadas siguen siendo reproducibles. |
| BR-ASM-006 | Una propuesta de IA y una decisión docente son registros distintos. | Foundation | Ninguna salida de IA se publica sin aprobación aplicable. |
| BR-ASM-007 | Toda transición sensible valida permisos e invariantes y conserva auditoría. | Foundation | Probar actor no autorizado, idempotencia y evento. |

## Política y cálculo

| ID | Regla | Alcance | Verificación mínima |
|---|---|---|---|
| BR-GRD-001 | Las ponderaciones de un grupo que forma un resultado deben sumar exactamente 100%, salvo regla explícita de normalización. | MVP | Casos 99,99%, 100% y 100,01%. |
| BR-GRD-002 | Actividades formativas, bonos y examen no participan en una suma de ponderaciones salvo declaración explícita. | MVP | Validar clasificación y grafo. |
| BR-GRD-003 | Toda política publicada tiene versión inmutable. | Foundation | Rechazar mutación; permitir nueva revisión. |
| BR-GRD-004 | Todo resultado oficial referencia política e inputs exactos. | Foundation | Reconstrucción produce el mismo valor. |
| BR-GRD-005 | La explicación procede de la misma traza del cálculo. | Foundation | Comparar pasos, valores y explicación. |
| BR-GRD-006 | El orden de ajustes, conversión, topes y redondeo es explícito. | Foundation | Casos donde alterar el orden cambia el resultado. |
| BR-GRD-007 | Un cambio de política no recalcula resultados silenciosamente. | Foundation | Exigir impacto, aprobación y nuevas revisiones. |
| BR-GRD-008 | Una simulación se identifica como hipotética y no es publicable. | MVP | Bloquear publicación de ejecución simulada. |
| BR-GRD-009 | Solo se ejecutan operaciones deterministas permitidas y versionadas. | Foundation | Rechazar bloque o versión desconocidos. |
| BR-GRD-010 | La IA puede explicar o sugerir, pero no alterar ni publicar una política autónomamente. | Foundation | Exigir decisión humana identificable. |

## Periodos, secciones y matrículas

| ID | Regla | Alcance | Verificación mínima |
|---|---|---|---|
| BR-SEC-001 | Cada sección pertenece a un periodo y no cambia de periodo para reutilizarse. | Foundation | Rechazar cambio de referencia tras creación. |
| BR-SEC-002 | Repetir asignatura, código y docente crea otra identidad de sección. | Foundation | Crear dos ejecuciones sin colisión global. |
| BR-SEC-003 | Copiar una sección excluye matrículas, resultados, evidencias y excepciones. | MVP | Inspeccionar copia y auditoría de opciones. |
| BR-ENR-001 | Una matrícula conserva fecha de registro, vigencia e historial. | Foundation | Consultar estado aplicable a una fecha. |
| BR-ENR-002 | Retirar a una persona no elimina su historia académica. | Foundation | Entregas y resultados siguen consultables según permiso. |
| BR-ENR-003 | Una incorporación tardía no genera automáticamente calificaciones cero. | MVP | Evaluaciones previas quedan con resolución explícita. |
| BR-ENR-004 | Cada administración conserva elegibilidad y fuente aplicable. | Foundation | Cambiar roster actual no altera roster histórico. |
| BR-ENR-005 | Una transferencia conecta dos matrículas y exige equivalencia para trasladar resultados. | Future | Rechazar copia automática sin decisión. |
| BR-ENR-006 | Repetir una asignatura crea una matrícula independiente. | Foundation | Conservar intento anterior sin sobrescritura. |
| BR-ENR-007 | Una corrección posterior al cierre genera otra revisión. | Foundation | Mantener resultado anterior y vigente. |
| BR-ENR-008 | Fecha de registro y fecha efectiva no son intercambiables. | Foundation | Casos con vigencia retroactiva controlada. |

## Excepciones, intentos y pendientes

| ID | Regla | Alcance | Verificación mínima |
|---|---|---|---|
| BR-EXC-001 | Una excepción individual no modifica la política general. | Foundation | Política conserva versión y alcance. |
| BR-EXC-002 | Cada excepción conserva actor, motivo, fecha, autorización y referencia de evidencia. | Foundation | Rechazar decisión incompleta. |
| BR-ATT-001 | Un intento nuevo no sobrescribe intentos anteriores. | Foundation | Todos los candidatos permanecen auditables. |
| BR-ATT-002 | El resultado efectivo identifica candidatos, regla, topes y ajustes. | Foundation | Reconstruir selección y valor. |
| BR-REC-001 | Una recuperativa declara elegibilidad, alcance, intentos y selección. | MVP | Rechazar configuración incompleta. |
| BR-REC-002 | Recuperar un componente no recalcula componentes no afectados. | MVP | Comparar revisiones antes y después. |
| BR-REC-003 | Todo tope de recuperativa se publica antes del intento. | MVP | Bloquear aplicación retroactiva silenciosa. |
| BR-ABS-001 | Una ausencia es un estado de participación, no una nota. | Foundation | Persistencia y API no exigen valor numérico. |
| BR-ABS-002 | Una ausencia pendiente no se convierte automáticamente en cero. | MVP | Cálculo oficial queda bloqueado o incompleto. |
| BR-LAT-001 | El atraso se calcula contra la fecha aplicable a la persona o equipo. | MVP | Considerar extensión y periodo de gracia. |
| BR-LAT-002 | Toda penalización declara la etapa matemática donde se aplica. | Foundation | Traza muestra base, ajuste y conversión. |
| BR-PEN-001 | Un resultado pendiente posee causa y no participa silenciosamente en cálculos oficiales. | Foundation | Rechazar cálculo oficial incompleto. |
| BR-COR-001 | Recalificación, reapertura, recuperación y rectificación son operaciones distintas. | Foundation | Contratos y transiciones no se intercambian. |
| BR-COR-002 | Una rectificación publicada crea otra revisión y notifica a los afectados. | MVP | Conservar publicaciones anterior y vigente. |
| BR-INC-001 | Cada caso incompleto tiene responsable, plazo y plan de resolución. | MVP | Rechazar cierre individual indefinido. |
| BR-INC-002 | Resolver un caso individual no recalcula participantes no afectados. | Foundation | Comparar conjunto de resultados modificados. |
| BR-AI-001 | La IA no autoriza ni publica excepciones académicas. | Foundation | Toda decisión final identifica autoridad humana. |

## Gobierno

- El Product Owner acepta o reemplaza reglas de producto.
- Arquitectura resuelve contratos, consistencia, persistencia y eventos.
- Cada release que implemente una regla debe enlazar criterios y pruebas.
- Las reglas `Future` no autorizan implementación dentro del MVP.
- La evidencia externa puede invalidar una regla; el cambio se registra en el
  ADR y en este catálogo.

## Referencias

- [`2026-07-27-assessment-domain-foundations.md`](2026-07-27-assessment-domain-foundations.md)
- [`2026-07-27-assessment-template-lifecycle-governance.md`](2026-07-27-assessment-template-lifecycle-governance.md)
- [`2026-07-27-transparent-grading-policy.md`](2026-07-27-transparent-grading-policy.md)
- [`2026-07-27-academic-period-section-enrollment.md`](2026-07-27-academic-period-section-enrollment.md)
- [`2026-07-27-assessment-exceptions-effective-results.md`](2026-07-27-assessment-exceptions-effective-results.md)

---

[← Índice de decisiones](README.md) · [Siguiente: Data Model Impact Ledger →](data-model-impact-ledger.md) · [↑ Volver al inicio](#top)
