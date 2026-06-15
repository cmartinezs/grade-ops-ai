# Submission Narrative

**Format:** Written description for the Devpost submission form.
**Target length:** 500–1000 words in English.
**Audience:** Hackathon judges evaluating business viability, AI-native operations, and category impact.

---

## Draft

> This is a working draft. Revise before submission with real evidence numbers, pilot names (if consented), and actual deployment URLs.

---

### GradeOps AI — AI-Native Assessment Operations for Programming Educators

Programming assessment is operationally heavy.

Every cycle — creating activities, defining rubrics, grading submissions, writing personalized feedback, detecting learning gaps, preparing class reports — takes hours. For small academies, bootcamps, tutors, and independent programming instructors, this work is largely manual, inconsistent, and difficult to scale.

Existing tools do not solve this. LMS platforms manage content, not operations. Generic AI tools generate text, but do not run structured workflows, maintain teacher control, or produce auditable evidence.

**GradeOps AI runs the full assessment workflow with AI agents, while keeping teachers in control of every decision that matters.**

---

#### How It Works

A teacher opens GradeOps AI and writes one sentence: *what they want to evaluate, for whom, and at what level.*

From that input, the Assessment Agent generates a complete activity brief. The Rubric Agent creates and validates grading criteria. Once the teacher approves both, the workflow is open for student submissions.

When submissions arrive, the Grading Agent analyzes each one against the approved rubric and produces a structured grading suggestion with evidence and uncertainty flags. The Feedback Agent drafts personalized feedback for each student. The teacher reviews and approves — or edits — every AI output before it becomes final.

The Learning Gap Agent then identifies recurring issues across the cohort. The Recovery Agent suggests reinforcement activities. The Teacher Report Agent compiles everything into a final class summary. Every step produces structured evidence: agent name, model used, tokens, cost estimate, and approval state.

GradeOps AI also supports closed assessments — objective questions with alternatives, true/false, single choice, and multiple choice. The Question Generation Agent produces questions with answer keys and difficulty tags. The Distractor Quality Agent flags weak or biased incorrect alternatives. The Ambiguity Review Agent catches interpretation problems. Teachers curate and approve questions into a question bank, assemble assessments, and publish them to students via secure email links. Grading is deterministic — no AI scores closed assessments. The Item Analytics Agent analyzes post-assessment performance.

**The teacher is the final pedagogical authority. AI agents operate the workflow. Teachers approve the outcomes.**

---

#### Business Model

GradeOps AI is offered as a subscription product priced per educator, with usage bounded by the number of graded student submissions per cycle.

| Plan | Price | Included |
|------|------:|---------|
| Teacher Lite | US$12/month | 150 graded submissions/month |
| Teacher Pro | US$29/month | 500 graded submissions/month |
| Cohort Pro | US$79/month | 2,000 graded submissions/month |
| Pilot Pack | US$99 one-time | One cohort, onboarded with support |

The Pilot Pack is the entry point for customer validation: a low-risk, fixed-price offer that gets real educators using the product and generating payment evidence.

*[Insert: actual revenue earned, number of paying pilots, months of operation.]*

---

#### Real Operations, Real Evidence

GradeOps AI is not a prototype or a demo environment. It is a deployed product running on Google Cloud.

*[Insert: deployment URL, Google Cloud project reference.]*

Evidence captured at submission:

- *[Insert: number of educator interviews conducted.]*
- *[Insert: number of pilot users.]*
- *[Insert: number of real assessments processed.]*
- *[Insert: number of student submissions graded.]*
- *[Insert: number of feedback outputs generated.]*
- *[Insert: number of agent execution log events.]*
- *[Insert: total Gemini API spend.]*
- *[Insert: one or two educator quotes/testimonials.]*

Every agent execution is logged in the product with timestamp, model, token usage, cost estimate, and teacher approval outcome. The evidence dashboard is part of the product — not a separate report.

---

#### Google Cloud and Gemini

GradeOps AI uses:

- **Gemini API / Vertex AI Gemini** — all 13 agent implementations use Gemini models via Spring AI's Vertex AI integration. Model routing favors Gemini Flash for high-volume steps (grading, distractor quality) and Gemini Flash for quality-critical steps (assessment design, feedback, teacher report).
- **Cloud Run** — web frontend, backend API, and agent runtime deployed as three separate Cloud Run services.
- **Cloud SQL (PostgreSQL)** — primary data store for assessments, rubrics, submissions, grading results, feedback, and agent execution logs.
- **Cloud Storage** — student submission files, exported reports, and evidence artifacts.
- **Secret Manager** — API keys, database credentials, JWT secret.
- **Cloud Logging** — technical runtime observability for all services.

---

#### Category Impact

GradeOps AI is built for a specific underserved segment: **small programming education providers** — bootcamps, tutors, and independent instructors who run real assessment cycles but do not have academic operations staff.

For this segment, the product delivers:

- **Faster feedback cycles** — grading and feedback drafts generated in minutes, not hours.
- **More consistent assessment design** — rubric validation and distractor quality checks reduce variance in what gets graded and how.
- **Earlier detection of learning gaps** — cohort-level patterns identified after every assessment, not at end-of-term.
- **Operational capacity** — a single educator can run assessment operations at the consistency level previously requiring a team.

*[Insert: specific time savings reported by pilot educators if available.]*

---

#### Status at Submission

*[Complete with current status before submitting.]*

- Deployment: *[URL]*
- Paying pilots: *[count]*
- Revenue: *[amount]*
- Assessments processed: *[count]*
- Student submissions graded: *[count]*
- Agent log events: *[count]*
- Educator testimonials: *[count]*

GradeOps AI is building toward a future where small education providers can operate assessment workflows with the consistency and efficiency of a larger academic team — one assessment cycle at a time.
