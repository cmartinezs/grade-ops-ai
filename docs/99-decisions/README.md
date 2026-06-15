# 99 Decisions

This folder records important decisions that should remain visible over time.

It answers:

> What did we decide, why did we decide it, and what tradeoffs does that create?

## Essence

`99-decisions` is for durable decisions, not notes.

Use this folder when a choice affects multiple documents, implementation direction, business strategy, architecture, scope, or future contributors.

## How To Use This Folder

Use [`adr-template.md`](adr-template.md) to create new decision records.

Recommended naming:

```text
YYYY-MM-DD-short-decision-title.md
```

Examples:

- [`2026-06-08-use-gradeops-ai-name.md`](2026-06-08-use-gradeops-ai-name.md);
- [`2026-06-08-price-by-graded-submissions.md`](2026-06-08-price-by-graded-submissions.md);
- [`2026-06-08-mermaid-diagram-standard.md`](2026-06-08-mermaid-diagram-standard.md).

## What Belongs Here

- Architecture decisions.
- Business model decisions.
- Product scope decisions.
- Naming decisions.
- Pricing model decisions.
- Compliance or hackathon interpretation decisions.
- Documentation conventions that affect the whole repo.

## What Does Not Belong Here

- Meeting notes.
- Raw brainstorming.
- Temporary todos.
- Implementation details that are not decision-worthy.

## Decision Quality Bar

A decision record should explain:

- context;
- decision;
- rationale;
- consequences;
- owner;
- status;
- date.

## Active Decision Records

| File | Topic | Status |
| --- | --- | --- |
| [`2026-06-08-use-gradeops-ai-name.md`](2026-06-08-use-gradeops-ai-name.md) | Product is named GradeOps AI; "Ops" signals workflow platform, not a quiz tool | Accepted |
| [`2026-06-08-price-by-graded-submissions.md`](2026-06-08-price-by-graded-submissions.md) | Primary billing unit is the graded submission; aligns cost with value delivered | Accepted |
| [`2026-06-08-mermaid-diagram-standard.md`](2026-06-08-mermaid-diagram-standard.md) | Mermaid is the default diagram format; PlantUML only when Mermaid is insufficient | Accepted |
| [`2026-06-10-closed-assessment-mode.md`](2026-06-10-closed-assessment-mode.md) | Adding closed (objective/alternatives) assessment mode alongside open mode | Accepted |
| [`2026-06-10-ai-native-question-bank.md`](2026-06-10-ai-native-question-bank.md) | Question bank is AI-generated and teacher-curated, not manually authored | Accepted |
| [`2026-06-10-assessment-snapshot-on-publish.md`](2026-06-10-assessment-snapshot-on-publish.md) | Questions, options, answer key, and scoring policy are frozen at publication | Accepted |
| [`2026-06-10-deterministic-grading-for-closed.md`](2026-06-10-deterministic-grading-for-closed.md) | Closed assessment grading is deterministic; no AI in the scoring calculation | Accepted |
| [`2026-06-10-student-access-via-secure-link.md`](2026-06-10-student-access-via-secure-link.md) | Students access assessments and results via signed email links; no account required | Accepted |
| [`2026-06-10-curriculum-taxonomy.md`](2026-06-10-curriculum-taxonomy.md) | Layered curriculum taxonomy: P0 string tagging, P1 relational Subject/CurriculumNode/LearningObjective model | Accepted |
| [`2026-06-10-technology-stack.md`](2026-06-10-technology-stack.md) | Next.js for web, Spring Boot + Java 21 for API and agents, Spring AI for the agent runtime | Accepted |
| [`2026-06-10-agent-runtime-separation.md`](2026-06-10-agent-runtime-separation.md) | `grade-ops-ai-agents` is a separate service; agents do not live inside the API | Accepted |
| [`2026-06-12-firebase-authentication.md`](2026-06-12-firebase-authentication.md) | Firebase Authentication as identity provider; ID token validation in `api/`; operator internal endpoint; pre-verified email for provisioned accounts; refresh-token revocation on sign-out | Accepted |

## Current Template

Use:

- [`adr-template.md`](adr-template.md)
