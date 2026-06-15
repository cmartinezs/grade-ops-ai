# Business Model

GradeOps AI is an AI-native assessment operations business for programming educators.

It helps teachers, tutors, bootcamps, and small academies run practical programming assessments with AI agents while preserving teacher authority over grading, feedback, and student-facing decisions.

This document defines the business model for the hackathon and early market validation. The canonical strategic foundation remains `../00-project/`.

## Canonical Alignment

| Source Decision | Business Interpretation |
| --- | --- |
| Project name | GradeOps AI |
| Initial wedge | Practical programming assessments |
| Initial market | Programming educators, tutors, bootcamps, and small academies |
| Category | Education & Human Potential |
| Core promise | Run the next programming assessment with AI agents |
| Pricing model | Bounded by assessments and graded submissions |
| Human role | Teacher reviews and approves important outputs |
| AI role | Agents execute repetitive workflow steps and produce structured evidence |
| Evidence policy | Usage, cost, revenue, testimonials, and agent logs are first-class outputs |

## Business Thesis

Programming educators do not only need help writing tests. They need assessment operations capacity.

The recurring business pain is:

> Creating practical assessments, reviewing code, applying rubrics, writing personalized feedback, identifying learning gaps, and deciding recovery actions takes too much manual time and is hard to scale consistently.

GradeOps AI converts that pain into a measurable business offer:

> We operate your next programming assessment with AI agents: activity design, rubric, grading assistance, feedback drafts, learning-gap report, teacher approval, and evidence logs.

The business is viable if customers believe the workflow saves enough time, improves feedback quality, and creates enough reporting value to justify a recurring software spend or a paid pilot.

## Ideal Customer Profile

### Primary ICP

Independent or small-team programming educators who run recurring practical assessments and do not have dedicated academic operations support.

| Attribute | Target |
| --- | --- |
| Teaching domain | Programming, algorithms, introductory software development, web/mobile/backend basics |
| Assessment type | Practical exercises, code submissions, short projects, rubric-based tasks |
| Team size | Solo educator, tutor, small academy, small bootcamp, cohort-based training team |
| Buying power | Can pay directly or approve a small pilot without institutional procurement |
| Pain intensity | High grading and feedback workload |
| Trust requirement | Teacher must remain in control |
| Adoption requirement | Workflow must work without replacing current teaching style |

### Secondary ICP

Small education providers that need standardization across multiple cohorts or instructors.

Examples:

- small coding academies;
- bootcamp operators;
- technical training teams;
- continuing education programs;
- instructors coordinating repeated sections of the same course.

## Customer Segments

| Segment | Pain | Buying Trigger | Best Offer |
| --- | --- | --- | --- |
| Independent programming teacher | Time spent grading and writing feedback | Upcoming assessment with many submissions | Pilot Pack |
| Tutor with several students | Need to give feedback faster and look professional | Growing student base | Teacher Lite / Teacher Pro |
| Small academy | Need consistent grading and reporting across cohorts | New cohort starts or quality issue appears | Cohort Pro |
| Bootcamp instructor | Fast feedback cycle and student risk visibility | Sprint project, module exam, capstone milestone | Pilot Pack / Cohort Pro |
| Program manager | Evidence of learning progress and instructor consistency | Reporting pressure or student retention risk | Cohort Pro / custom pilot |

## Jobs To Be Done

### Functional Job

When I run a programming assessment, I want to move from learning goal to reviewed feedback and teacher report with less manual effort, so I can spend more time teaching and supporting students.

### Emotional Job

I want to trust that feedback is consistent and useful without feeling that AI is replacing my professional judgment.

### Business Job

I want to deliver better assessment operations without hiring more staff or manually scaling every part of the workflow.

## Value Proposition

### For Teachers

- Save time on repetitive grading and feedback preparation.
- Generate structured rubrics faster.
- Detect repeated misconceptions earlier.
- Approve or edit AI outputs before delivery.
- Produce reports without manual consolidation.

### For Students

- Receive faster and more actionable feedback.
- Understand mistakes by rubric criterion.
- Get targeted recovery recommendations.
- Experience more consistent evaluation criteria.

### For Small Education Providers

- Operate with more academic capacity without immediately hiring.
- Standardize assessment workflow across instructors.
- Produce evidence of learning outcomes.
- Reduce feedback bottlenecks.
- Improve visibility into cohort risk.

## Productized Offer

### Initial Hackathon Offer

> **Pilot Pack — US$99 one-time**  
> We operate 3 real programming assessments with AI agents, up to 150 graded submissions, onboarding, feedback drafts, teacher report, and evidence dashboard.

This is the primary early revenue vehicle because it is easier to sell than a self-serve subscription before the product has full polish.

### Subscription Path

After pilot validation, customers can move into bounded monthly plans:

| Plan | Target Customer | Included Usage |
| --- | --- | --- |
| Teacher Lite | Tutor or solo educator testing the workflow | 3 assessments / 90 graded submissions |
| Teacher Pro | Active programming educator | 10 assessments / 300 graded submissions |
| Cohort Pro | Small academy or bootcamp | 30 assessments / 1,000 graded submissions |

The business should never sell unlimited AI grading. The commercial unit is assessment operations plus graded-submission volume.

## Revenue Model

| Revenue Stream | Timing | Notes |
| --- | --- | --- |
| Pilot Pack | One-time | Best for immediate hackathon validation |
| Monthly subscription | Recurring | Bounded by assessments and submissions |
| Overuse fees | Usage-based | Additional graded submissions, reports, or premium review |
| Guided onboarding | One-time or bundled | Useful for small academies and bootcamps |
| Cohort setup | One-time or premium tier | Applies when several instructors/courses are involved |

## Unit Economics

The primary unit is:

> 1 assessment = activity + rubric + grading assistance for 30 submissions + personalized feedback + teacher report.

The business must track:

- cost per agent run;
- cost per assessment;
- cost per graded submission;
- cost per active teacher;
- cost per customer;
- revenue per customer;
- gross margin per plan;
- payment fees;
- marketing/customer acquisition spend;
- related-party revenue separately from arms-length revenue.

## Business Evidence Required

Every pilot should produce business evidence, not only product feedback.

| Evidence | Why It Matters |
| --- | --- |
| Customer identity and role | Proves real market contact |
| Pain severity | Shows whether the problem is worth paying for |
| Current workflow | Reveals switching friction |
| Payment or commitment | Proves willingness to pay |
| Assessment processed | Proves product usage |
| Submissions processed | Proves volume and operational value |
| Agent logs | Proves AI-native operations |
| Time saved estimate | Supports business value |
| Testimonial | Supports pitch and category impact |
| Cost per run | Supports sustainability |

## Acquisition Strategy

GradeOps AI starts with founder-led sales.

The first customers should come from:

- existing educator networks;
- programming teachers;
- bootcamp instructors;
- small academies;
- technical communities;
- LinkedIn outreach;
- referral conversations from educators.

Do not start with large schools, universities, or procurement-heavy institutions. Their sales cycles are too slow for the hackathon window.

## Retention Strategy

Retention depends on assessment recurrence.

The product should make it easy for a teacher to reuse a successful workflow:

- clone assessment setup;
- reuse rubric patterns;
- compare cohorts;
- track recurring learning gaps;
- export reports;
- keep a history of approved feedback;
- move from one assessment to the next with less setup.

## Competitive Positioning

GradeOps AI should not compete as a generic AI quiz generator.

| Alternative | Why It Falls Short | GradeOps AI Position |
| --- | --- | --- |
| LMS | Organizes courses but leaves assessment work mostly manual | Operates the assessment workflow |
| Quiz generator | Generates questions but does not manage grading/feedback/reporting | Runs end-to-end assessment operations |
| Generic chatbot | Helpful but unstructured and hard to audit | Produces structured outputs, logs, and approval states |
| Manual grading | Trusted but slow and inconsistent under workload | AI drafts; teacher approves |
| Autograder | Useful for tests but narrow for rubric and feedback | Combines rubric-based judgment, feedback, and reporting |

## Business Risks

| Risk | Impact | Mitigation |
| --- | --- | --- |
| Teachers distrust AI grading | Low adoption | Keep teacher approval mandatory and show uncertainty flags |
| Product becomes too broad | Slow delivery and weak pitch | Stay focused on programming assessments |
| Pricing is too low | Weak business viability | Sell value and bounded usage, not token cost |
| Usage is unbounded | Margin risk | Cap assessments and submissions |
| Revenue comes only from friends | Weak hackathon evidence | Separate related-party revenue and prioritize arms-length pilots |
| Procurement cycles are long | No revenue in time | Sell to small buyers with direct decision power |
| Customer evidence is weak | Poor submission | Capture interviews, testimonials, payment evidence, and dashboard screenshots from day one |

## Validation Gates

| Gate | Pass Signal | Fail Signal |
| --- | --- | --- |
| Problem validation | 10+ educators confirm grading/feedback pain | Pain is occasional or low-value |
| Buyer validation | 3+ customers accept paid pilot or strong commitment | Users like the idea but will not pay |
| Workflow validation | Teachers complete full assessment flow | The team must manually patch every run |
| Trust validation | Teachers approve AI-assisted feedback | Teachers reject AI involvement in grading |
| Margin validation | Cost per assessment remains controlled | Long submissions or retries destroy margin |
| Repeat-use validation | At least one customer wants another assessment | Product is seen as one-time demo |

## Current Business Conclusion

GradeOps AI should be treated as a focused assessment-operations business, not a broad education platform.

The immediate business objective is not to maximize feature breadth. It is to prove:

1. programming educators have a painful recurring assessment workflow;
2. they will pay for AI-assisted assessment operations;
3. AI agents can execute meaningful parts of that workflow in production;
4. teachers can remain in control;
5. the business can track revenue, cost, usage, and value per customer.

<!-- nav -->

---

[↑ inicio](#business-model) | [README](README.md) | [Customer Discovery →](customer-discovery.md)
