# GradeOps AI

**One-line pitch:** AI-native assessment operations for programming educators.

**Core promise:** Run your next programming assessment with AI agents.

GradeOps AI helps programming educators, tutors, bootcamps, and small academies operate assessment workflows — both practical code tasks and objective knowledge checks — with AI agents while preserving teacher control.

It is not a generic quiz generator, chatbot, or LMS. It is an AI-operated workflow that supports two assessment modes:

- **Open assessments** — practical code/text tasks with rubric-based AI grading suggestions, teacher approval, and personalized feedback.
- **Closed assessments** — objective questions with alternatives (TF, single choice, multiple choice), an AI-native question bank, and deterministic grading against a frozen answer key.

Both modes share the same teacher workspace, evidence layer, and approval infrastructure.

## Strategic position

GradeOps AI is being built as a focused hackathon MVP and a real business experiment.

The goal is not to build a large academic platform. The goal is to prove that a small education provider can run assessment operations faster, with more consistency, and with auditable AI support.

The initial wedge is programming education because practical coding assessments are frequent, time-consuming to grade, feedback-heavy, and easy to validate through real workflows.

## Problem

Programming assessment workflows are operationally heavy.

Educators do not only create tests. They operate a full assessment cycle:

- design activities;
- define rubrics;
- collect submissions;
- grade consistently;
- write feedback;
- detect learning gaps;
- decide what to reinforce next;
- prepare reports.

For small academies, tutors, bootcamps, and independent educators, this work is usually manual, fragmented, and difficult to scale.

Existing tools fall short:

- LMS platforms organize content but do not operate the full assessment workflow.
- Quiz generators are too narrow for authentic coding assessment.
- Generic AI tools are not structured around teacher review, traceability, evidence capture, or business operations.

## Solution

GradeOps AI helps educators move from a learning goal to reviewed results through a controlled agent workflow.

**Open assessment flow:**

1. Teacher defines what they want to evaluate.
2. Assessment Agent generates the activity draft.
3. Rubric Agent creates and validates criteria.
4. Students submit answers, code, or files via secure link.
5. Grading Agent analyzes submissions against the rubric.
6. Feedback Agent drafts personalized feedback.
7. Learning Gap Agent identifies recurring cohort issues.
8. Recovery Agent suggests reinforcement activities.
9. Teacher reviews and approves all AI outputs.
10. Teacher Report Agent prepares the final report.
11. Ops Evidence Agent records usage, costs, outcomes, and agent logs.

**Closed assessment flow:**

1. Teacher defines the evaluative intent and learning outcomes.
2. Question Generation Agent produces objective questions with alternatives, answer key, and difficulty.
3. Distractor Quality Agent and Ambiguity Review Agent flag quality issues.
4. Teacher curates and approves questions into the question bank.
5. Assessment Assembly Agent composes a balanced assessment from approved questions.
6. Teacher approves composition and scoring policy; system freezes a snapshot.
7. Students receive secure access links and respond without creating an account.
8. Deterministic engine grades responses against the frozen answer key.
9. Teacher reviews exceptions; Item Analytics Agent analyzes item performance.
10. Teacher publishes results; students view grades via result link.

The teacher is the final pedagogical authority in both modes. AI agents generate, suggest, and analyze — they never finalize grades or deliver feedback without teacher approval.

## Target users

- Programming instructors in bootcamps.
- Tutors running cohort-based coding programs.
- Small academies with limited instructional operations staff.
- Independent educators managing recurring coding assessments.
- Training teams that need faster and more consistent assessment cycles.

## MVP scope

The MVP covers two assessment modes for programming educators.

Included:

- Teacher workspace for both open and closed assessments.
- Open assessment creation from a learning goal; AI-generated rubric.
- Closed assessment creation with AI-native question bank; AI generation, teacher curation, deterministic grading.
- Student access via secure email links; no student account required.
- Grading: AI rubric-based suggestions (open) and deterministic engine against frozen answer key (closed).
- Personalized feedback drafts (open) and item analytics reports (closed).
- Learning gap summaries and recovery activity suggestions.
- Teacher-facing report for both modes.
- Agent execution logs and cost tracking.
- Evidence capture for users, revenue, customer learning, and hackathon submission.
- Basic Google Cloud deployment and Gemini API integration.

Not prioritized for the MVP:

- Full LMS functionality.
- Complex institutional administration.
- Mobile app.
- Physical paper intake with OCR/OMR (P1, not P0).
- Broad multi-subject or multi-country curriculum packs (P1).
- Institutional shared question banks or marketplace.
- Student account with password, social features, or appeal workflow.

## AI-native operations

GradeOps AI must show agents executing meaningful operational steps, not only generating text.

**Open assessment agents:**

- Assessment Agent — generates activity draft from learning goal.
- Rubric Agent — creates and validates scoring rubric.
- Grading Agent — analyzes submissions against approved rubric.
- Feedback Agent — drafts student-facing feedback.
- Learning Gap Agent — identifies cohort-level recurring issues.
- Recovery Agent — suggests reinforcement activities.
- Teacher Report Agent — prepares the final class report.
- Ops Evidence Agent — records usage, costs, outcomes, and agent logs.

**Closed assessment agents:**

- Question Generation Agent — generates TF/SC/MC questions with alternatives and answer key.
- Distractor Quality Agent — evaluates and flags weak or biased alternatives.
- Ambiguity Review Agent — detects interpretation problems in question stems.
- Assessment Assembly Agent — composes a balanced assessment from approved bank questions.
- Item Analytics Agent — analyzes post-assessment item performance and learning outcome coverage.

Each agent execution should produce structured evidence:

- timestamp;
- user;
- assessment;
- agent name;
- input summary;
- output summary;
- model used;
- status;
- teacher approval state;
- estimated time saved;
- operational evidence generated.

## Evidence-first strategy

The project is designed to collect proof from day one.

Target evidence:

- educator interviews;
- pilot commitments;
- paid pilots or payment intent;
- real or semi-real assessment runs;
- submissions processed;
- feedback outputs generated;
- teacher approval activity;
- agent logs;
- usage events;
- cost tracking;
- time saved;
- testimonials;
- demo-ready dashboards.

For the hackathon, success is measured by evidence, not feature volume.

## Repository map

- [`/00-project/`](00-project/README.md) — vision, pitch, problem, solution, roadmap, and hackathon strategy.
- [`/01-business/`](01-business/README.md) — business model, pricing, go-to-market, discovery, and revenue evidence.
- [`/02-product/`](02-product/README.md) — personas, MVP scope, stories, workflows, and product metrics.
- [`/03-ai-agents/`](03-ai-agents/README.md) — agent roles, responsibilities, boundaries, prompts, and logs.
- [`/04-architecture/`](04-architecture/README.md) — system design, data model, API, security, deployment, and Google Cloud assumptions.
- [`/05-evidence/`](05-evidence/README.md) — templates for proof of demand, usage, revenue, outcomes, and demo evidence.
- [`/06-ux/`](06-ux/README.md) — screen inventory, interaction model, and UX design intent for teacher workspace and student access.
- [`/07-hackathon/`](07-hackathon/README.md) — demo script, evidence checklist, and submission narrative for the XPRIZE AI hackathon.
- [`/99-decisions/`](99-decisions/README.md) — architecture, product, business, and scope decision records.
- [`/.github/copilot-instructions.md`](.github/copilot-instructions.md) — repository guidance for contributors and AI coding agents.

## Documentation conventions

- Diagrams must be written in Mermaid by default.
- Use PlantUML only when Mermaid cannot express the diagram clearly.
- Use ASCII diagrams only as a last fallback.
- Plain text blocks are acceptable for formulas, message templates, command examples, file trees, and literal snippets.

## Current status

- Documentation repository created for the XPRIZE AI hackathon.
- Product positioning defined: AI-native assessment operations for programming educators.
- Two assessment modes fully documented: open (practical/code) and closed (objective/alternatives).
- 13 AI agents specified with input/output contracts and human control checkpoints.
- Student access model defined: LearnerRef + secure email links; no student account required.
- Response intake documented: digital P0, physical paper P1 (OCR/OMR + QR).
- Curriculum taxonomy defined: P0 string tagging, P1 structured Subject/CurriculumNode/LearningObjective model.
- Data model, security posture, deployment architecture, and API design documented.
- Six key architecture/product decisions recorded in `99-decisions/`.
- MVP scope, user stories (Epics 1–13), and workflows defined.
- Evidence-first strategy active: product designed to capture usage, cost, revenue, and operational evidence from day one.

## Strategic principles

- Start with programming educators as the market wedge.
- Build a small, sellable, demonstrable MVP.
- Treat GradeOps AI as an operating business, not only a software demo.
- Keep teachers in control of judgments, standards, and approvals.
- Use AI agents for repetitive assessment operations.
- Capture evidence from day one.
- Prioritize real usage, real feedback, and revenue signals over feature volume.
- Avoid building a full LMS during the hackathon.
- Make the demo prove the operation, not only the interface.

## Final positioning

GradeOps AI helps programming educators operate assessments with AI agents.

It gives small education teams the operational capacity of a larger academic team while keeping teachers in control of judgment, feedback, and student outcomes.
