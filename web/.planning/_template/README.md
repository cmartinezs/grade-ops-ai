# Planning: [Planning Name]

> [← planning/README.md](../README.md)

Short working summary for this planning. Keep this file current as the planning moves through INITIAL, EXPANSION, DEEPENING, and COMPLETED.

---

## Overview

- **Planning ID:** NNN-name
- **Current status:** Initial
- **Intent:** [One-sentence intent from 00-initial.md]
- **Owner:** [human / AI agent / team]
- **Started:** YYYY-MM-DD
- **Completed:** *(not completed yet)*

Example:

- **Planning ID:** 006-password-reset
- **Current status:** Deepening
- **Intent:** Add password reset by email to API and web login flow
- **Owner:** team
- **Started:** 2026-07-12
- **Completed:** *(not completed yet)*

---

## Key Links

- [Initial context](00-initial.md)
- [Expansion plan](01-expansion.md)
- [Story details](02-deepening/)
- [Traceability](TRACEABILITY.md)
- [Retrospective raw notes](RETROSPECTIVE-RAW.md)
- Decision records: `pdr-*.md` *(optional; create with `/plan-decision` only when needed)*

---

## Current State

Summarize where this planning stands and what remains before archive.

- [ ] Initial intent is complete.
- [ ] Expansion stories are dimensioned.
- [ ] Stories are DONE or intentionally SKIPPED.
- [ ] Traceability is complete.
- [ ] Retrospective is complete.

---

## Retrospective

Complete this section before archiving.

### Outcomes

- [What shipped, changed, or was decided?]

Example:

- Password reset token lifecycle shipped for API and web flows.
- PDR-001 accepted the 15-minute token expiry policy across clients.

### Deviations

- [What changed from the original scope, and why?]

Example:

- Branded email template work was deferred because provider ownership was not selected.

### Follow-ups

- [Open improvements, deferred work, or next planning candidates.]

Example:

- Create a follow-up planning for email template branding and localization.

### Lessons

- [What should carry forward to future planning work?]

Example:

- Cross-client security policies should become PDR candidates before story implementation starts.

---

> [← planning/README.md](../README.md)
