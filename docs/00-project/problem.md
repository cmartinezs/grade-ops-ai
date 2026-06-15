# Problem

Programming assessment workflows are operationally heavy.

Programming educators do not only create tests. They operate a full assessment cycle: design activities, define rubrics, collect submissions, grade consistently, write feedback, detect learning gaps, and decide what to reinforce next.

For small academies, tutors, bootcamps, and independent educators, this work is usually manual, fragmented, and difficult to scale.

## Strategic Problem Statement

Programming educators need an AI-native operating layer for practical assessments: one that reduces repetitive work, preserves teacher control, improves feedback speed, and creates evidence of learning and business value.

## Current Pain Points

- Assessment design is repeated from scratch.
- Rubrics are inconsistent across instructors or cohorts.
- Submission handling is fragmented across tools.
- Grading takes too much instructor time.
- Feedback quality varies with workload.
- Learning gaps are spotted too late.
- Recovery actions are often improvised.
- Small schools lack dedicated assessment operations staff.
- Evidence of grading, feedback, and learning progress is hard to consolidate.
- Business evidence around value, usage, and willingness to pay is usually absent.

## Jobs To Be Done

| Actor | Job |
| --- | --- |
| Teacher | “Help me run my next assessment faster without losing control of grading quality.” |
| Student | “Help me understand what I did wrong and what to practice next.” |
| Bootcamp instructor | “Help me process many similar submissions and identify cohort-wide gaps.” |
| Small academy owner | “Help me standardize assessment quality without hiring more academic staff.” |
| Hackathon judge | “Show me a real AI-operated business with customers, revenue, costs, logs, and impact.” |

## Why Programming Assessment Is Different

Programming education is not well served by simple multiple-choice tooling.

Practical programming assessment often requires:

- code analysis;
- reasoning review;
- partial credit;
- rubric-based judgment;
- feedback by type of mistake;
- detection of repeated misconceptions;
- recovery activities tied to specific learning gaps.

This is why the initial wedge matters. Programming creates a clear, repeated, high-friction assessment workflow where AI agents can provide visible operational leverage.

## Why Existing Tools Fall Short

### LMS Platforms

LMS tools organize content, classes, and submissions, but they do not operate the assessment workflow end to end.

They usually leave the hardest work to the teacher:

- designing the assessment;
- defining grading criteria;
- reviewing individual answers;
- writing useful feedback;
- identifying patterns;
- preparing recovery actions.

### Quiz Generators

Quiz generators are useful for simple objective questions, but they are too narrow for authentic programming assessment.

They typically produce questions, not operations. They do not reliably manage rubric validation, grading assistance, feedback quality, learning-gap detection, teacher approval, and evidence capture.

### Generic AI Tools

Generic AI tools can help draft content, but they are not structured around assessment operations.

They usually lack:

- teacher approval workflows;
- traceability;
- consistent rubric enforcement;
- agent-level logs;
- evidence capture;
- usage metrics;
- business evidence;
- operational dashboards;
- bounded usage and cost controls.

## Business Problem

Small education providers need to deliver better feedback without hiring more academic operations staff.

The buyer does not only need an AI writing assistant. The buyer needs a way to run assessments faster, with more consistency, better visibility, and measurable value.

The economic problem is not only the API cost. It is the manual time spent on repetitive grading, feedback, reporting, and recovery planning.

## Validation Hypotheses

These assumptions must be tested quickly:

| Hypothesis | Validation Signal |
| --- | --- |
| Teachers feel acute pain around programming grading and feedback | 10+ interviews with repeated pain patterns |
| Teachers will trust AI if they approve final outputs | Positive reaction to human-in-the-loop demo |
| A paid pilot is easier to sell than a subscription at the start | 3+ paid pilots or signed commitments |
| The assessment workflow is more valuable than isolated test generation | Customers ask for grading, feedback, and report, not only questions |
| Usage limits are acceptable | Buyers understand limits by assessment and submission volume |
| Programming is a strong enough initial wedge | First pilots come from programming educators without broad-subject expansion |

## Risk Of Misinterpretation

GradeOps AI should not be interpreted as:

- a fully automated grading authority;
- a student-facing tutoring chatbot;
- a full school administration system;
- a generic question generator;
- a compliance-heavy institutional LMS;
- a promise that AI output is always correct;
- a substitute for teacher review.

The correct interpretation is narrower and stronger:

> GradeOps AI is an AI-native operating layer for practical programming assessments, where agents run repetitive workflow steps and teachers approve the educational decisions.

## Problem Boundaries

GradeOps AI solves the assessment-operation problem first. It does not initially solve:

- full curriculum planning;
- institutional accreditation;
- attendance tracking;
- student enrollment management;
- full LMS content delivery;
- live classroom management;
- OCR-heavy paper correction;
- plagiarism investigation as a primary product.

Academic integrity can be supported through logs, rubric evidence, and uncertainty flags, but it should not become the MVP’s central promise.

<!-- nav -->

---

← [Pitch](pitch.md) | [↑ inicio](#problem) | [README](README.md) | [Solution →](solution.md)
