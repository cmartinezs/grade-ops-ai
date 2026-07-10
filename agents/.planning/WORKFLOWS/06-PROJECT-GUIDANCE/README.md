# Project Guidance

> [← WORKFLOWS](../README.md)

Use these notes when the planning is not primarily software delivery. They complement the execution workflows; they do not replace story/task tracking.

---

## Project Type Guidance

| `project.type` | Primary output | Verification style | Suggested terminology |
|----------------|----------------|--------------------|-----------------------|
| `software` | Code, infrastructure, services, automations | Tests, build, stack-specific smoke tests, connectivity or schema checks, human code review | story / task |
| `documentation` | Guides, references, changelogs, ADRs | Link checks, freshness, peer review | deliverable / section |
| `research` | Findings, experiments, recommendations | Evidence, citations, reproducibility | experiment / finding |
| `operations` | Process changes, rollout plans, support runbooks | Dry runs, signoff, operational metrics | workstream / action |
| `general` | Mixed project outputs | Manual acceptance criteria | initiative / deliverable |

---

## How to Adapt a Planning

1. Set `.planning/config.yml`:

```yaml
project:
  type: general

terminology:
  planning_item: deliverable
  verification_label: Acceptance checks

execution:
  requires_git: false
  requires_tests: false
```

2. Keep the same file lifecycle:
   - `00-initial.md` captures intent and boundaries.
   - `01-expansion.md` lists deliverables, risks, dependencies, and external issue IDs.
   - `02-deepening/` contains one file per deliverable and optional task files.

3. Replace technical verification with concrete acceptance checks:
   - expected document exists
   - stakeholder reviewed it
   - decision logged
   - rollout checklist completed
   - evidence attached or linked

4. Use `/plan-status` to inspect the current state and `/plan-audit-docs` when documentation is the main artifact.

---

## Examples

### Documentation Project

- `project.type: documentation`
- stories become major docs or doc groups
- tasks become sections, diagrams, examples, or reference updates
- verification checks links, stale references, and glossary consistency

### Research Project

- `project.type: research`
- stories become experiments or analysis tracks
- tasks become data gathering, evaluation, synthesis, and review
- verification checks source quality, reproducibility, and stated limitations

### Operations Project

- `project.type: operations`
- stories become workstreams
- tasks become rollout steps, runbook updates, or stakeholder actions
- verification checks approvals, dry runs, communication, and rollback plans

---

> [← WORKFLOWS](../README.md)
