# Curriculum Structure

GradeOps AI needs a curriculum taxonomy to anchor questions, assessments, and rubrics to subjects, topics, and learning objectives. This taxonomy enables AI to generate contextually relevant questions, helps teachers compose coherent assessments, and makes analytics and reports pedagogically meaningful.

## Why Curriculum Structure Matters

Without curriculum anchors, the Question Generation Agent has no reliable scope boundary. It may generate questions that are off-level, off-topic, or misaligned with declared learning objectives. Similarly, the Assessment Assembly Agent cannot guarantee coverage if it does not know which learning objectives a question addresses.

Curriculum structure enables:

- AI question generation with a defined scope.
- Question bank filtering by subject, topic, and learning objective.
- Closed assessment composition that guarantees curriculum coverage.
- Post-assessment reporting by topic and learning objective.
- Detection of over- or under-assessed curriculum areas.

## MVP Model (P0): Subject > Topic Tagging

For the MVP, curriculum anchoring is implemented as **string-based tagging** on questions and assessments. No complex relational curriculum model is required at P0.

```text
Subject (string)
  └── Topic (string)
      └── Learning Outcome (string)
```

Example:
```text
Subject: "Object-Oriented Programming"
Topic: "Inheritance"
Learning Outcome: "Design reusable class hierarchies using inheritance"
```

These fields are free-text at P0, populated by the teacher or suggested by the Question Generation Agent. Questions store them as `subject_area`, `topic_tags_json`, and `learning_outcome`. Assessments store them as `topic` and `learning_goal`.

This is sufficient for the hackathon MVP.

## Full Model (P1): Layered Curriculum Taxonomy

For scale — institutional use, Chile national curriculum, and multi-country expansion — GradeOps AI needs a structured, versionable curriculum taxonomy. This model is P1 and does not need to be built for the hackathon demo.

### Taxonomy Hierarchy

```text
CurriculumProvider
  └── CurriculumFramework
      └── CurriculumVersion
          └── EducationLevel
              └── Subject
                  └── CurriculumNode (axis, unit, module, topic, subtopic)
                      └── LearningObjective
                          └── Skill / Competency
```

### Simple vs. Full View

| Context | Recommended model |
| --- | --- |
| Independent teachers, bootcamps, corporate training | Simple: Subject > Topic > Learning Outcome (P0 string-based) |
| K-12 schools, institutional programs | Full: Provider > Framework > Level > Subject > Node > LO |
| International products | Multi-country: one CurriculumProvider per country/ministry |

### Curriculum Creation Sources

| Source | Description | When to use |
| --- | --- | --- |
| Manual | Teacher defines subject, units, and topics | Independent teachers, custom programs |
| AI-generated | AI proposes curriculum structure from a course description | Fast setup for new courses |
| Official | Imported from a national or institutional curriculum document | K-12 schools aligned to a national standard |

### AI-Generated Curriculum

When a teacher describes a course, GradeOps AI can generate a proposed curriculum structure.

Example input:
```text
Course: Object-Oriented Programming for first-year computer science students.
Duration: 12 weeks.
Topics: classes, objects, encapsulation, inheritance, polymorphism, interfaces, exceptions.
```

Example output:
```text
Subject: Object-Oriented Programming
Units:
  1. Foundations: Classes and Objects
  2. Encapsulation and Access Control
  3. Relationships: Inheritance
  4. Polymorphism and Abstract Design
  5. Exception Handling

Learning Outcomes:
  - Model entities using classes and objects.
  - Apply encapsulation in simple designs.
  - Implement inheritance for code reuse.
  - Use polymorphism to extend behavior.
  - Handle exceptions robustly.
```

AI-generated curriculum structures must be approved by the teacher before they can be used to constrain question generation or assessment composition.

## Linking Questions to Curriculum

Every question in the bank must carry curriculum metadata.

| Field | MVP (P0) | Full model (P1) |
| --- | --- | --- |
| Subject | `subject_area: string` | `subject_id: UUID` references `Subject` entity |
| Topic | `topic_tags_json: json` | `curriculum_node_ids: UUID[]` references `CurriculumNode` |
| Learning outcome | `learning_outcome: string` | `learning_objective_ids: UUID[]` references `LearningObjective` |
| Difficulty | `difficulty: enum` | unchanged |
| Level | `level` on assessment or question | `education_level_id: UUID` references `EducationLevel` |

### Rules

- A question must have at least one subject and one learning outcome to be `active` in the bank.
- A question with no curriculum metadata may be saved as `draft` but cannot be used in assessments.
- AI-generated questions include suggested subject, topic, and learning outcome; these are confirmed when the teacher approves the question.

## Linking Assessments to Curriculum

### Closed Assessments

A closed assessment must declare its curriculum scope before composition. This scope defines which questions from the bank are eligible.

```text
Assessment blueprint:
  subject: "Object-Oriented Programming"
  topics: ["Inheritance", "Polymorphism"]
  learning_outcomes: ["LO-04", "LO-05"]
  question_count: 15
  difficulty_distribution: { easy: 30%, medium: 50%, hard: 20% }
```

The Assessment Assembly Agent selects only questions that match the declared subject, topics, and learning outcomes. Questions from other subjects cannot be included unless the teacher explicitly overrides with a confirmation.

### Open Assessments

Open assessments are linked to curriculum less rigidly: the teacher declares the subject and intended learning outcomes, and these inform the assessment context, case, and rubric criteria.

```text
Assessment:
  subject: "Object-Oriented Programming"
  topic: "Inheritance"
  learning_outcomes: ["Design reusable class hierarchies using inheritance"]
  rubric: criteria mapped to each declared learning outcome
```

This enables post-assessment reporting by learning outcome even for open assessments.

## Curriculum Validators

Before composing or publishing a closed assessment, the system should validate curriculum coherence.

| Validator | Check | Behavior |
| --- | --- | --- |
| Subject consistency | All selected questions belong to the declared subject | Block or alert |
| Coverage completeness | All declared learning outcomes have at least one question | Alert with option to generate more |
| Level consistency | Questions match the declared student level | Alert on mismatch |
| Cross-subject contamination | A question from a different subject is included | Block or require confirmation |
| Insufficient bank | Bank has fewer approved questions than requested for the declared scope | Alert; suggest generating more or reducing count |

## Reports Enabled by Curriculum Structure

Curriculum metadata unlocks pedagogically meaningful analytics after an assessment runs.

| Report | Requires |
| --- | --- |
| Performance by subject | `subject_area` on question |
| Performance by topic | `topic_tags_json` on question |
| Performance by learning outcome | `learning_outcome` on question |
| Questions with lowest correct rate per outcome | Question + item analytics |
| Outcomes with low coverage in the assessment | Assessment blueprint + bank |
| Reinforcement suggestions by topic | Item analytics + curriculum anchors |

Example interpretation:
```text
In "Object-Oriented Programming / Inheritance":
  - cohort average: 54%
  - question with most errors: item 9 (correct rate: 21%)
  - most-chosen wrong option: B (chosen by 58% of students)
  - recommendation: reinforce use of super() and constructor chaining
```

## Chile Curriculum Pack (P1 Reference)

As a concrete P1 example, GradeOps AI could incorporate the Chilean national curriculum as an official `CurriculumProvider`.

```text
CurriculumProvider
  name: Ministerio de Educación de Chile
  country_code: CL
  provider_type: official

CurriculumFramework
  name: Currículum Nacional

EducationLevel
  name: 1° Básico

Subject
  name: Matemática

CurriculumNode (axis)
  name: Números y Operaciones

LearningObjective
  external_code: MA01 OA 09
  description: Demostrar que comprenden la adición y la sustracción de números del 0 al 20...
```

The architecture must support multiple countries, each as its own `CurriculumProvider`, without hardcoding Chilean structures into the core model.

## Scope

### P0

- String-based `subject_area`, `topic_tags_json`, and `learning_outcome` fields on questions and assessments.
- AI suggests subject/topic/outcome when generating questions; teacher confirms at curation.
- Question bank filterable by subject, topic, and learning outcome strings.
- Assessment assembly uses declared subject and outcome scope to filter eligible bank questions.
- Basic curriculum consistency warning (subject mismatch alert).

### P1

- Structured `Subject`, `CurriculumNode`, and `LearningObjective` entities in the data model.
- AI-generated curriculum structure from course description, with teacher approval.
- Closed assessment blueprint entity with coverage validation.
- Insufficient bank alert with option to generate more questions.
- Reports by learning objective with item analytics.
- Chile national curriculum pack as the first official `CurriculumProvider`.
- Multi-country framework architecture.

### P2

- Curriculum pack marketplace.
- Version-controlled official curriculum updates.
- Longitudinal reporting across curriculum versions.
- Institutional curriculum customization on top of official packs.
- Comparison between aligned and non-aligned questions.

<!-- nav -->

---

← [Response Intake](response-intake.md) | [↑ inicio](#curriculum-structure) | [README](README.md) | [Metrics →](metrics.md)
