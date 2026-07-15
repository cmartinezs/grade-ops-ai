# PDR-NNN: [Decision Title]

> **Status:** Proposed | Accepted | Rejected | Superseded by [PDR-NNN]
> [← planning/README.md](README.md)

---

## Context

> *What situation, problem, or need prompted this decision?*
> Example: `During password reset planning, API docs, web copy, and support docs disagreed on token expiry. Because the policy affects API validation, user-facing messages, and support runbooks, it needs a durable planning decision.`

[Describe the context in 2-5 sentences.]

---

## Decision

> *What was decided, stated clearly.*
> Example: `Password reset tokens expire after 15 minutes, can be used only once, and public responses must not reveal whether an email address exists.`

[State the decision.]

---

## Rationale

> *Why was this the chosen option?*
> Example: `A short expiry reduces account-takeover risk while still giving users enough time to complete the flow. Single-use tokens prevent replay, and uniform public responses prevent user enumeration.`

[Explain the reasoning.]

---

## Alternatives Considered

Replace example rows with the real alternatives considered for this decision.

| Option | Why Rejected |
|--------|-------------|
| 1-hour expiry | Longer exposure window for leaked email links |
| Different expiry per client | Creates inconsistent support behavior and harder validation |
| [Option A] | [Reason] |

---

## Consequences

### Positive

- API, web, docs, and support use one policy.
- Security-sensitive behavior is documented before implementation.
- [What improves or becomes easier]

### Negative / Trade-offs

- Some users may need to request a second link if they miss the 15-minute window.
- [What becomes harder or requires additional work]

---

## Affected Areas

Replace example rows with the actual project area codes from `.planning/GUIDE.md`.

| Area Code | Impact |
|-----------|--------|
| AP | API validation must reject expired or used reset tokens |
| WB | Web copy must explain the 15-minute expiry |
| DO | Support docs and runbooks must use the same expiry policy |

---

## Related

- **Planning:** [NNN-name]
- **Story / Task:** [story-NN / task-NN or "none"]
- **ADR (if technical follow-up):** [ADR link or "none"]
- **Supersedes:** [PDR-NNN or "none"]
- **Superseded by:** [PDR-NNN or "none"]

Example:

- **Planning:** 006-password-reset
- **Story / Task:** story-01 / task-02
- **ADR (if technical follow-up):** docs/adr/0007-reset-token-storage.md
- **Supersedes:** none
- **Superseded by:** none

---

> [← planning/README.md](README.md)
