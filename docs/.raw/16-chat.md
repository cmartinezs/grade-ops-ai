# GradeOps AI — Esquema estructurado de materias, currículo y vinculación evaluativa

## 1. Decisión de producto

GradeOps AI necesita un esquema estructurado de **materias/contenidos curriculares** para vincular preguntas cerradas, bancos de preguntas, evaluaciones cerradas, evaluaciones abiertas, rúbricas, resultados de aprendizaje, reportes de desempeño y generación AI de evaluaciones.

Este esquema no debe limitarse a una jerarquía simple `Asignatura > Unidad > Tema`, aunque esa estructura debe seguir existiendo como vista básica.

La recomendación es evolucionar a un modelo de **taxonomía curricular versionada**, capaz de representar:

```text
País / sistema educativo
  -> marco curricular
  -> nivel educativo / curso
  -> asignatura
  -> eje / unidad / módulo
  -> tema
  -> subtema
  -> objetivo de aprendizaje / resultado de aprendizaje
  -> habilidad / competencia
```

---

## 2. Problema que resuelve

Para crear evaluaciones cerradas con AI desde un banco de preguntas, el sistema no puede seleccionar preguntas arbitrarias.

Debe garantizar que las preguntas pertenezcan a un espacio curricular compatible.

Ejemplo incorrecto:

```text
Evaluación: Matemática / Aritmética / Suma
Banco contiene:
  - Suma
  - Fotosíntesis
  - Revolución Francesa
  - Verbos irregulares en inglés
```

Ejemplo correcto:

```text
Evaluación: Matemática / Aritmética / Suma

Banco elegible:
  - Preguntas vinculadas a Matemática
  - Nivel compatible
  - Unidad o eje compatible
  - Tema compatible
  - Objetivo de aprendizaje compatible
```

---

## 3. Principio de diseño

```text
Toda pregunta, rúbrica o evaluación debe saber qué materia evalúa.
```

Más específicamente:

```text
Toda pregunta o evaluación debe quedar vinculada a:
  - una asignatura;
  - uno o más temas;
  - uno o más objetivos de aprendizaje;
  - un nivel/curso cuando aplique;
  - un marco curricular cuando exista.
```

---

## 4. Modelo base inspirado en GRADE

GRADE planteaba una estructura base:

```text
Asignatura
  -> Unidad
  -> Tema
```

Ejemplo:

```text
Matemática
  -> Aritmética
  -> La suma
```

Este modelo es útil, simple y entendible para docentes. Debe mantenerse como **modo simple/manual**.

Sin embargo, para GradeOps AI se necesita una estructura más extensible.

---

## 5. Modelo recomendado para GradeOps AI

### 5.1 Vista simple

Para la UX inicial del docente:

```text
Asignatura
  -> Unidad
  -> Tema
```

Ejemplo:

```text
Matemática
  -> Aritmética
  -> Suma de números naturales
```

Esta vista sirve para docentes independientes, educación superior, cursos libres, bootcamps, capacitaciones e instituciones sin currículo formal cargado.

### 5.2 Vista curricular completa

Para contextos escolares o institucionales:

```text
Sistema educativo
  -> Marco curricular
  -> Nivel / Curso
  -> Asignatura
  -> Eje / Unidad / Módulo
  -> Tema
  -> Objetivo de aprendizaje
  -> Habilidad / competencia
```

Ejemplo Chile:

```text
Chile
  -> Currículum Nacional
  -> 1° Básico
  -> Matemática
  -> Números y operaciones
  -> Adición y sustracción hasta 20
  -> MA01 OA 09
  -> Resolver problemas / Representar / Modelar
```

---

## 6. Por qué esto es necesario

### 6.1 Para generar preguntas cerradas

Cuando la AI genera preguntas, debe recibir contexto curricular:

```text
Genera 10 preguntas de selección única para:
  - país: Chile
  - nivel: 1° básico
  - asignatura: Matemática
  - eje: Números y operaciones
  - objetivo: MA01 OA 09
  - tema: adición y sustracción hasta 20
  - dificultad: básica/intermedia
```

Sin esa estructura, la AI puede generar preguntas fuera de nivel, fuera de tema o con dificultad incorrecta.

### 6.2 Para construir evaluaciones cerradas desde banco

La evaluación debe tener una **blueprint curricular**:

```text
Evaluación:
  - Matemática
  - 1° básico
  - Números y operaciones
  - OA 07, OA 09, OA 10
  - 20 preguntas
  - 60% suma/resta
  - 20% cálculo mental
  - 20% relación inversa suma-resta
```

El sistema solo puede seleccionar preguntas que cumplan el scope:

```text
question.subject = Matemática
question.level = 1° Básico
question.curricular_nodes contiene OA definido
question.status = approved
question.type = closed
```

### 6.3 Para evaluaciones abiertas

Las evaluaciones abiertas también deben vincularse a materias, pero de otra forma:

```text
Evaluación abierta:
  - asignatura
  - unidad/tema
  - objetivo de aprendizaje
  - habilidad evaluada
  - rúbrica alineada al objetivo
```

Ejemplo:

```text
Asignatura: Programación
Unidad: Programación orientada a objetos
Tema: Herencia y polimorfismo
Resultado de aprendizaje: Diseñar clases reutilizables aplicando herencia
Evaluación: caso práctico de modelamiento
Rúbrica:
  - diseño de clases
  - uso correcto de herencia
  - encapsulamiento
  - ejecución funcional
  - claridad del código
```

---

## 7. Fuentes de creación de materias

GradeOps AI debe permitir tres fuentes:

| Fuente | Descripción | Uso |
|---|---|---|
| Manual | El docente crea asignatura, unidad y tema | Cursos propios, educación superior, bootcamps |
| AI-assisted | La AI propone estructura curricular desde un programa o descripción | Acelerar configuración inicial |
| Oficial/importada | Se carga un currículo oficial por país/nivel/asignatura | Escolar, colegios, alineación normativa |

---

## 8. Currículo oficial por país

### 8.1 Concepto

GradeOps AI puede incorporar currículos oficiales como **paquetes curriculares**.

Ejemplo:

```text
Curriculum Pack: Chile — Currículum Nacional
  -> Educación Básica
  -> 1° Básico
  -> Matemática
  -> Ejes
  -> Objetivos de Aprendizaje
  -> Habilidades
  -> Actitudes
```

Otro país podría tener su propio pack:

```text
Curriculum Pack: Colombia
Curriculum Pack: México
Curriculum Pack: España
Curriculum Pack: Argentina
Curriculum Pack: Perú
```

El producto no debe asumir que todos los países tienen la misma estructura. Por eso el modelo debe ser flexible.

### 8.2 Enfoque recomendado

No hardcodear el currículo chileno en el núcleo.

Crear una arquitectura por capas:

```text
Core GradeOps AI
  -> modelo genérico de taxonomía curricular

Country Curriculum Packs
  -> Chile
  -> otros países

Institution Custom Curriculum
  -> adaptaciones internas
  -> programas propios
```

---

## 9. Modelo conceptual recomendado

```text
CurriculumProvider
  └── CurriculumFramework
      └── CurriculumVersion
          └── EducationLevel
              └── Subject
                  └── CurriculumNode
                      └── LearningObjective
                          └── Skill / Competency
```

### 9.1 CurriculumProvider

Representa la fuente.

Ejemplos:

```text
Ministerio de Educación de Chile
Institución educativa
Docente independiente
GradeOps AI generated
```

Campos sugeridos:

| Campo | Descripción |
|---|---|
| `id` | identificador |
| `country_code` | CL, MX, CO, PE, etc. |
| `name` | nombre del proveedor |
| `provider_type` | official, institution, teacher, ai_generated |
| `source_url` | fuente si existe |
| `active` | vigente |

### 9.2 CurriculumFramework

Representa un marco curricular.

Ejemplos:

```text
Currículum Nacional Chile
Programa interno Bootcamp Java
Plan de estudios Ingeniería Informática
```

Campos:

| Campo | Descripción |
|---|---|
| `id` | identificador |
| `provider_id` | fuente |
| `name` | nombre |
| `education_system` | escolar, superior, bootcamp, corporate |
| `description` | descripción |
| `status` | draft, active, archived |

### 9.3 CurriculumVersion

El currículo cambia con el tiempo. Debe versionarse.

Campos:

| Campo | Descripción |
|---|---|
| `id` | identificador |
| `framework_id` | marco asociado |
| `version_label` | ej. 2024, 2026, Decreto X |
| `valid_from` | inicio vigencia |
| `valid_to` | fin vigencia |
| `source_document_ref` | documento o fuente |
| `status` | draft, active, deprecated |

### 9.4 EducationLevel

Representa curso/nivel.

Ejemplos:

```text
1° Básico
8° Básico
2° Medio
Primer año de carrera
Módulo inicial
Nivel introductorio
```

Campos:

| Campo | Descripción |
|---|---|
| `id` | identificador |
| `curriculum_version_id` | versión curricular |
| `name` | nombre |
| `level_code` | código normalizado |
| `sequence_order` | orden |
| `education_stage` | básica, media, superior, etc. |

### 9.5 Subject

Representa asignatura/área.

Ejemplos:

```text
Matemática
Lenguaje y Comunicación
Programación I
Bases de Datos
Arquitectura de Software
```

Campos:

| Campo | Descripción |
|---|---|
| `id` | identificador |
| `education_level_id` | nivel |
| `name` | nombre |
| `subject_code` | código opcional |
| `description` | descripción |
| `source_type` | official, manual, ai_generated |

### 9.6 CurriculumNode

Nodo flexible para eje, unidad, módulo, tema o subtema.

Campos:

| Campo | Descripción |
|---|---|
| `id` | identificador |
| `subject_id` | asignatura |
| `parent_node_id` | permite jerarquía |
| `node_type` | eje, unidad, módulo, tema, subtema |
| `name` | nombre |
| `description` | descripción |
| `sequence_order` | orden |
| `source_type` | official, manual, ai_generated |
| `review_status` | pending_review, approved, rejected |

Ejemplo:

```text
Subject: Matemática 1° Básico
  CurriculumNode: Números y operaciones
    CurriculumNode: Adición y sustracción
      CurriculumNode: Suma hasta 20
```

### 9.7 LearningObjective

Representa OA, RA, competencia o resultado de aprendizaje.

Campos:

| Campo | Descripción |
|---|---|
| `id` | identificador |
| `curriculum_node_id` | nodo asociado |
| `external_code` | ej. MA01 OA 09 |
| `description` | texto del objetivo |
| `objective_type` | knowledge, skill, attitude, competency |
| `cognitive_level` | recordar, comprender, aplicar, analizar, etc. |
| `source_type` | official, manual, ai_generated |
| `review_status` | pending_review, approved, rejected |

---

## 10. Vinculación con preguntas cerradas

Cada pregunta cerrada debe tener metadata curricular obligatoria.

```text
Question
  ├── subject_id
  ├── curriculum_node_ids
  ├── learning_objective_ids
  ├── difficulty
  ├── cognitive_level
  ├── question_type
  ├── source_type
  ├── review_status
  └── approved_by
```

### Reglas

- Una pregunta no puede ser `active` si no tiene materia.
- Una pregunta AI-generated no puede pasar al banco activo sin aprobación.
- Una pregunta debe tener al menos un vínculo curricular.
- Una pregunta puede vincularse a más de un OA, pero debe tener uno principal.
- Una evaluación cerrada solo puede consumir preguntas aprobadas y compatibles.
- El snapshot de evaluación debe congelar también la metadata curricular.

---

## 11. Vinculación con evaluaciones cerradas

La evaluación cerrada debe tener una blueprint.

```text
ClosedAssessmentBlueprint
  ├── subject_id
  ├── curriculum_node_ids
  ├── learning_objective_ids
  ├── number_of_questions
  ├── distribution_by_topic
  ├── distribution_by_difficulty
  ├── distribution_by_question_type
  ├── scoring_policy
  └── selection_strategy
```

### Estrategias de selección

| Estrategia | Descripción |
|---|---|
| Manual | El docente selecciona preguntas |
| AI-assisted | AI propone preguntas desde banco aprobado |
| Hybrid | Docente fija algunas, AI completa faltantes |
| Gap-fill | AI genera nuevas preguntas para cubrir temas sin banco suficiente |

---

## 12. Vinculación con evaluaciones abiertas

La evaluación abierta debe vincular:

```text
OpenAssessment
  ├── subject_id
  ├── curriculum_node_ids
  ├── learning_objective_ids
  ├── skill_ids
  ├── case_context
  ├── rubric
  └── expected_evidence
```

La rúbrica debe mapear criterios a objetivos:

```text
RubricCriterion
  ├── learning_objective_id
  ├── criterion_name
  ├── description
  ├── weight
  └── performance_levels
```

Esto permite reportar:

```text
El estudiante logró bien el OA X,
pero presenta brecha en el OA Y.
```

---

## 13. Generación AI de materias

La AI puede asistir la creación de materias cuando no se cargue un currículo oficial.

### Input posible

```text
Curso: Programación Orientada a Objetos para primer año de informática.
Duración: 12 semanas.
Contenidos: clases, objetos, encapsulamiento, herencia, polimorfismo, interfaces, excepciones.
```

### Output esperado

```text
Asignatura: Programación Orientada a Objetos
Unidades:
  1. Fundamentos de clases y objetos
  2. Encapsulamiento
  3. Relaciones entre clases
  4. Herencia
  5. Polimorfismo
  6. Manejo de excepciones

Temas:
  - definición de clase
  - atributos y métodos
  - constructores
  - modificadores de acceso
  - sobreescritura
  - clases abstractas

Resultados de aprendizaje:
  - Modelar entidades usando clases y objetos.
  - Aplicar encapsulamiento en diseños simples.
  - Implementar herencia para reutilización de código.
```

### Regla

Las materias generadas por AI deben quedar en estado:

```text
ai_generated
  -> pending_teacher_review
  -> approved
```

No deben usarse automáticamente para generar evaluaciones hasta ser aprobadas.

---

## 14. Incorporación del currículo chileno

Para Chile, GradeOps AI debería permitir un pack curricular basado en el Currículum Nacional.

La estructura visible en el portal oficial permite acceder por curso/nivel y asignatura a objetivos de aprendizaje, programas, planes y orientaciones. Además, organiza el currículo por asignaturas y cursos, y en una página concreta como Matemática 1° Básico expone ejes, habilidades, objetivos de aprendizaje, actitudes y documentos curriculares.

### Ejemplo de mapeo Chile

```text
CurriculumProvider
  name: Ministerio de Educación de Chile
  country_code: CL
  provider_type: official

CurriculumFramework
  name: Currículum Nacional

CurriculumVersion
  version_label: vigente

EducationLevel
  name: 1° Básico

Subject
  name: Matemática

CurriculumNode
  node_type: eje
  name: Números y operaciones

LearningObjective
  external_code: MA01 OA 09
  description: Demostrar que comprenden la adición y la sustracción de números del 0 al 20...
```

---

## 15. UX recomendada

### 15.1 Selector de materia

En creación de evaluación:

```text
¿Desde dónde quieres generar la evaluación?

[ ] Materia creada por mí
[ ] Currículo oficial
[ ] Programa institucional
[ ] Crear materia con AI
```

Luego:

```text
País: Chile
Marco curricular: Currículum Nacional
Nivel: 1° Básico
Asignatura: Matemática
Eje/Unidad: Números y operaciones
Objetivo(s): MA01 OA 07, MA01 OA 09
Tipo: cerrada
Cantidad: 20 preguntas
```

### 15.2 Banco de preguntas con filtro curricular

Filtros mínimos:

- asignatura;
- nivel/curso;
- unidad/eje;
- tema;
- objetivo de aprendizaje;
- dificultad;
- tipo de pregunta;
- estado;
- origen: manual / AI / importada;
- uso histórico.

### 15.3 Generador AI de evaluación cerrada

El docente define:

```text
Generar evaluación cerrada para:
  - Matemática
  - 1° Básico
  - Números y operaciones
  - OA 09
  - 15 preguntas
  - dificultad progresiva
  - selección única
```

El sistema responde:

```text
Encontré 23 preguntas aprobadas compatibles.
Propongo usar 15:
  - 5 fáciles
  - 7 intermedias
  - 3 desafiantes

Faltan preguntas para cubrir OA 10.
¿Deseas generar nuevas preguntas con AI?
```

---

## 16. Validadores obligatorios

### 16.1 Validador de materia común

Antes de armar evaluación cerrada:

```text
Todas las preguntas seleccionadas deben compartir:
  - asignatura compatible;
  - nivel compatible;
  - nodo curricular compatible;
  - objetivo de aprendizaje declarado o aceptado.
```

### 16.2 Validador de cobertura

El sistema debe advertir:

```text
La evaluación declara cubrir OA 07, OA 09 y OA 10,
pero no contiene preguntas para OA 10.
```

### 16.3 Validador de mezcla indebida

El sistema debe bloquear o alertar:

```text
Esta evaluación es de Matemática 1° Básico,
pero contiene una pregunta de Matemática 3° Básico.
```

### 16.4 Validador de banco insuficiente

El sistema debe avisar:

```text
No hay suficientes preguntas aprobadas para este objetivo.
Opciones:
  - reducir cantidad;
  - ampliar alcance;
  - generar nuevas preguntas con AI;
  - seleccionar manualmente;
  - crear evaluación abierta.
```

---

## 17. Reportes habilitados por esta estructura

Con metadata curricular, GradeOps AI puede reportar:

- desempeño por asignatura;
- desempeño por unidad;
- desempeño por tema;
- desempeño por objetivo de aprendizaje;
- preguntas más difíciles por OA;
- brechas por curso/sección;
- objetivos con baja cobertura;
- temas sobreevaluados;
- temas subevaluados;
- recomendaciones de reforzamiento.

Ejemplo:

```text
En Matemática / Números y operaciones / MA01 OA 09:
  - promedio del curso: 58%
  - pregunta con mayor error: ítem 7
  - distractor más elegido: B
  - recomendación: reforzar representación simbólica de la sustracción
```

---

## 18. Priorización

### P0

- Modelo simple Asignatura > Unidad > Tema.
- Vincular pregunta cerrada a materia.
- Vincular evaluación abierta/cerrada a materia.
- Generar materias con AI y aprobación docente.
- Filtrar banco por materia.
- Armar evaluación cerrada solo desde preguntas compatibles.
- Registrar objetivo/resultado de aprendizaje como metadata.
- Reporte básico por materia/tema.

### P1

- Curriculum packs por país.
- Pack Chile inicial.
- Importación/asistencia desde Currículum Nacional.
- Mapeo a OA oficiales.
- Blueprint curricular de evaluación.
- Cobertura por objetivo de aprendizaje.
- Generación AI de preguntas desde OA.
- Reporte por OA.

### P2

- Multi-país robusto.
- Versionado curricular oficial.
- Adaptaciones institucionales.
- Comparación entre currículos.
- Marketplace de paquetes curriculares.
- Sincronización con fuentes oficiales.
- Recomendaciones de progresión longitudinal.

---

## 19. Decisión estratégica

La estructura `Asignatura > Unidad > Tema` debe mantenerse como base UX porque es simple y conocida.

Pero internamente GradeOps AI debe modelar una taxonomía curricular más fuerte:

```text
Materia simple para empezar.
Currículo versionado para escalar.
Objetivos de aprendizaje para generar y medir.
AI para crear, mapear y cubrir.
Docente para aprobar.
```

Esta estructura permite que GradeOps AI funcione tanto para docentes independientes, colegios chilenos, educación superior, bootcamps, capacitación corporativa y futuros mercados internacionales.

---

## 20. Conclusión

El módulo de materias no debe tratarse como configuración secundaria.

Debe ser uno de los pilares de GradeOps AI, porque habilita:

- generación AI más precisa;
- bancos de preguntas reutilizables;
- evaluaciones cerradas coherentes;
- evaluaciones abiertas alineadas;
- reportes pedagógicos útiles;
- trazabilidad curricular;
- adaptación a currículos oficiales como el chileno.

La definición final debería ser:

```text
GradeOps AI organiza todo contenido evaluativo en una taxonomía curricular.
Las preguntas, rúbricas y evaluaciones se vinculan a materias, temas y objetivos.
La AI puede crear o mapear esta estructura, pero el docente o institución la aprueba.
Las evaluaciones cerradas solo se arman desde preguntas curricularmente compatibles.
Las evaluaciones abiertas usan la misma estructura para alinear caso, rúbrica y feedback.
```
