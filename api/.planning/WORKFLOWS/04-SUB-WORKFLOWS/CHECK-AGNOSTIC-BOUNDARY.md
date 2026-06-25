# [CHECK-AGNOSTIC-BOUNDARY]

> [← README](README.md)

Verifies that the output of a task is consistent with the canonical contracts defined in `docs/` — no invented entities, no schema drift, no undocumented state transitions.

> **Note:** This sub-workflow is a **docs-contract consistency check** — it verifies that implementation outputs match the contracts defined in `docs/`, regardless of technology stack.

---

## Steps

1. Identify the `docs/` document(s) that define the contract for what was just produced:
   - API endpoints → your API design doc
   - Entities / enums → your data model doc
   - Agent input/output → your agent spec doc
   - Module/directory structure → your architecture/structure doc
   - UX flows → your UX spec doc
2. Compare the produced output against the contract document:
   - Field names and types match?
   - Enum values match the allowed values defined in the contract?
   - Endpoint paths and HTTP methods match exactly?
   - State machine transitions are only the ones listed?
3. If divergences found: list them and flag as a contract violation.
4. For each violation: decide whether it is a documentation gap (update `docs/`) or an implementation error (fix the code). Only update `docs/` if this is a genuine intentional deviation — record it in a decision record if so.
5. Return: `OK` if consistent, `VIOLATION` + list if not.

---

**Called by:** [`GENERATE-DOCUMENT`](../02-EXECUTION-WORKFLOWS/GENERATE-DOCUMENT.md) · [`REVIEW-COHERENCE`](../02-EXECUTION-WORKFLOWS/REVIEW-COHERENCE.md) · [`EXPAND-ELEMENT`](../02-EXECUTION-WORKFLOWS/EXPAND-ELEMENT.md)

---

> [← README](README.md)
