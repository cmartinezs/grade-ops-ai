# Archive Event-Specific Constraints

- Status: Accepted
- Date: 2026-07-20
- Decision owner: Founder / Documentation Governance

## Context

GradeOps AI documentation previously contained material created for a specific 2026 event. That material included timing, evidence, pricing, deployment, and presentation constraints that were useful for that event but no longer govern the product.

Leaving those constraints in active documentation would create false requirements for implementation agents and future planning.

## Decision

Move event-specific constraints and narrative material to `docs/archive/2026-event/` and treat them as historical reference only.

Active product, business, architecture, UX, and developer documentation must not rely on event-specific deadlines, judging criteria, demo scripts, pricing variants, or deployment claims.

If archived material contains useful product insight, promote the durable part into the appropriate active folder or a new ADR before using it as a source of truth.

## Rationale

This keeps active documentation focused on durable product and implementation decisions while preserving historical context for auditability.

It also prevents headless agents from optimizing for obsolete event constraints instead of the current Master Plan and source documentation.

## Consequences

- `docs/archive/2026-event/` remains readable but non-canonical.
- Active documentation should reference archived event material only as history.
- Future validation narratives must be rebuilt from current product, business, evidence, and deployment facts.
- If a future event introduces new constraints, they need an explicit ADR or planning decision before becoming active scope.

<!-- nav -->

---

← [Environment Roles](2026-07-20-environment-roles.md) | [↑ inicio](#archive-event-specific-constraints) | [README](README.md)
