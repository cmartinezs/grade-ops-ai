# Customer Discovery

Customer discovery for GradeOps AI exists to validate whether programming educators will pay for AI-operated assessment workflows.

The goal is not to confirm that educators find AI interesting. The goal is to prove that the assessment workflow is painful enough, frequent enough, and valuable enough to support a real business.

## Discovery Goals

| Goal | Question To Answer |
| --- | --- |
| Problem severity | Is assessment work painful enough to pay for? |
| Workflow clarity | Which steps consume the most time and produce the most frustration? |
| Trust model | What must remain under teacher control? |
| Buying trigger | What event makes a teacher or academy pay now? |
| Offer validation | Is the Pilot Pack understandable and credible? |
| Pricing validation | Which price points create real willingness to pay? |
| Evidence validation | What proof does the buyer need before adoption? |

## Canonical Hypotheses

GradeOps AI is currently built around these hypotheses:

1. Programming assessment workflows are operationally heavy.
2. Teachers spend significant recurring time creating assessments, grading code, writing feedback, and preparing reports.
3. Small education providers cannot easily hire academic operations support.
4. Teachers will consider AI support if they keep final approval.
5. Customers are more likely to buy a guided pilot than a self-serve subscription before trust is established.
6. Pricing must be bounded by assessments and graded submissions.
7. Agent logs, approval states, and cost tracking are not only technical features; they are trust and business evidence.

## Who To Interview First

Prioritize buyers or direct workflow owners, not generic education observers.

| Priority | Target | Why |
| --- | --- | --- |
| P0 | Independent programming teachers | Fastest path to real pain and direct payment |
| P0 | Tutors with recurring students | Small buyer, clear workload, direct decision |
| P0 | Bootcamp instructors | High submission volume and fast assessment cycles |
| P1 | Small academy founders | Can buy pilots and care about reporting/consistency |
| P1 | Program managers | Understand quality and cohort-level reporting needs |
| P2 | University/college instructors | Strong problem, but procurement may be slow |
| P2 | School administrators | Useful later, not ideal for first hackathon revenue |

## Interview Screening

Use a short screener before a full interview.

### Must-Have Criteria

- Teaches programming or programming-adjacent skills.
- Runs practical assessments, exercises, labs, assignments, or projects.
- Reviews student work manually at least sometimes.
- Has experienced feedback or grading workload pressure.
- Can influence adoption or payment for a small pilot.

### Good-Fit Signals

- Has 20+ submissions per assessment.
- Reuses or adapts rubrics.
- Teaches cohorts repeatedly.
- Needs to report results to students, managers, or clients.
- Has complained about grading, feedback, consistency, or student recovery.

### Poor-Fit Signals

- Only uses auto-graded multiple-choice quizzes.
- Has no recurring assessment workflow.
- Cannot pay or influence payment.
- Wants a full LMS replacement.
- Wants AI to replace teacher judgment entirely.

## Interview Structure

Keep the interview focused on current behavior, not hypothetical enthusiasm.

### 1. Context

- What do you teach?
- Who are your students?
- How often do you run programming assessments?
- What kinds of submissions do students deliver?
- How many submissions do you typically review per assessment?

### 2. Current Workflow

- Walk me through your last programming assessment from planning to final feedback.
- What tools did you use?
- Where did students submit their work?
- How did you define the rubric or criteria?
- How did you grade?
- How did you send feedback?
- Did you prepare any report or summary afterward?

### 3. Pain And Time

- Which step took the most time?
- Which step was the most annoying or repetitive?
- How many hours did the whole assessment cycle take?
- How much of that time felt low-value or administrative?
- What happens when you are overloaded?
- What quality tradeoffs do you make when time is short?

### 4. Trust And Control

- Which parts would you allow AI to draft?
- Which parts would you never allow AI to decide alone?
- What would you need to review before sending feedback to students?
- What kind of audit trail would make you trust the workflow?
- What mistakes would be unacceptable?

### 5. Buying And Adoption

- Have you paid for teaching, grading, automation, or AI tools before?
- Who decides whether a tool can be used?
- What would make you try a paid pilot?
- What would block adoption?
- What evidence would convince you after the first run?
- Would you rather pay per assessment, per month, or per student/submission?

### 6. Offer Test

Show the offer in plain language:

> We operate your next 3 programming assessments with AI agents: activity design, rubric, grading assistance, personalized feedback drafts, learning-gap report, and teacher approval. Up to 150 graded submissions.

Ask:

- Is this clear?
- Which part is most valuable?
- Which part feels unnecessary?
- What would make this feel unsafe?
- Would you pay for this as a pilot?
- At what price would it feel too cheap to trust?
- At what price would it feel expensive but possible?
- At what price would it be clearly too expensive?

## Do Not Pitch Too Early

During discovery, avoid selling immediately. The first goal is to understand the real workflow.

Bad question:

> Would you use an AI grading platform?

Better question:

> Tell me what happened the last time you had to review 30 programming submissions.

Bad question:

> Would this save you time?

Better question:

> Which step did you postpone, rush, or simplify because you did not have enough time?

## Evidence To Capture Per Interview

| Field | Description |
| --- | --- |
| Interview ID | Unique identifier |
| Date | Interview date |
| Contact | Name, email, phone if permitted |
| Role | Teacher, tutor, instructor, founder, manager |
| Segment | Independent, tutor, bootcamp, academy, etc. |
| Current workflow | Summary of tools and process |
| Assessment volume | Submissions per assessment and assessments per month |
| Time estimate | Hours per assessment cycle |
| Pain severity | 1-5 score |
| Trust requirement | What must be reviewed/approved |
| Buying authority | Can buy, influence, or only recommend |
| Price reaction | Response to Pilot Pack and subscription ideas |
| Objections | Trust, privacy, cost, workflow, accuracy, policy |
| Next step | No fit, follow-up, demo, pilot, paid pilot |
| Related-party flag | Whether customer is related to team/founder |
| Consent status | Whether testimonial/contact evidence can be used |

## Pain Severity Scoring

| Score | Meaning | Interpretation |
| --- | --- | --- |
| 1 | Mild annoyance | Not a buyer |
| 2 | Occasional inconvenience | Useful feedback, weak buyer |
| 3 | Recurring pain | Potential buyer if workflow is easy |
| 4 | Strong recurring pain | Good pilot candidate |
| 5 | Urgent operational bottleneck | Highest priority pilot candidate |

## Buying Intent Scoring

| Score | Meaning |
| --- | --- |
| 0 | No interest |
| 1 | Likes idea but no action |
| 2 | Wants demo |
| 3 | Wants to test with a real assessment |
| 4 | Willing to pay or sign pilot commitment |
| 5 | Paid, committed, or introduced another buyer |

## Discovery Targets For Hackathon

Minimum evidence target:

- 10+ educator discovery conversations;
- 5+ strong pilot candidates;
- 3+ paid pilots or payment commitments;
- 1+ testimonial with permission;
- 1+ clear arms-length customer if possible;
- explicit record of related-party revenue if any.

Stretch target:

- 20+ discovery conversations;
- 10+ demo calls;
- 5+ pilots;
- 3+ paying customers;
- 100+ submissions processed;
- repeated-use signal from at least one customer.

## Discovery Synthesis Template

After every 5 interviews, summarize:

```markdown
# Discovery Batch Summary

## Batch
- Dates:
- Number of interviews:
- Segments:

## Patterns
- Top repeated pain:
- Most time-consuming step:
- Strongest buying trigger:
- Strongest trust requirement:
- Most common objection:

## Pricing Signals
- Pilot Pack reaction:
- Subscription reaction:
- Preferred billing unit:
- Price resistance:

## Product Implications
- Must build:
- Can fake/manual-assist temporarily:
- Should not build:
- Needs clarification:

## Business Decision
- Continue:
- Pivot:
- Narrow segment:
- Change offer:
```

## Interview Output Classification

| Classification | Meaning | Next Action |
| --- | --- | --- |
| Research only | Useful insight, no sales path | Keep as evidence, no immediate follow-up |
| Demo candidate | Interested but not ready to pay | Schedule demo |
| Pilot candidate | Has real assessment coming soon | Offer Pilot Pack |
| Paid pilot | Will pay or has paid | Onboard and capture evidence |
| Referral source | Not a buyer but knows buyers | Ask for introductions |

## Privacy And Consent

Customer discovery must be evidence-ready without exposing private educational data unnecessarily.

Rules:

- Ask permission before using names or testimonials publicly.
- Keep student data out of discovery notes unless strictly necessary.
- For hackathon evidence, store customer contact information securely.
- Separate public testimonial text from private contact evidence.
- Do not publish student submissions or grades without explicit permission.
- Mark related-party relationships honestly.

## Discovery Conclusion

Customer discovery is successful only if it produces business decisions.

The expected decision after discovery is one of:

1. sell the Pilot Pack to the strongest segment;
2. narrow the ICP further;
3. adjust the offer;
4. adjust the pricing unit;
5. delay features that do not help paid pilots;
6. reject assumptions that did not survive customer evidence.

<!-- nav -->

---

← [Business Model](business-model.md) | [↑ inicio](#customer-discovery) | [README](README.md) | [Go-To-Market →](go-to-market.md)
