# Retrospective Raw Notes: [Planning Name]

> [← README](README.md)

Working log for events that were unexpected, corrective, risky, or useful for the final retrospective.

Use `/plan-edge-case <planning-id> -- <what happened>` to add manual entries. Commands may also append entries when they encounter blockers, corrections, skipped work, recovery actions, validation failures, or other non-linear events.

---

## How To Use This File

Capture facts while they are fresh. Do not polish entries here. The final retrospective belongs in `README.md`.

Each entry should answer as many of these as possible:

- What happened?
- What was expected instead?
- How was it resolved or contained?
- What should be carried forward?

Example entry:

```md
### 2026-07-12 16:30 - Reset-token expiry changed during review

- **Source:** plan-done
- **Related story/task:** story-01 / task-02
- **What happened:** Reviewer rejected the original 1-hour token expiry because the support runbook already promised 15 minutes.
- **Expected instead:** Expiry policy should have been checked against docs before implementation.
- **Resolution:** API config, tests, and docs were aligned to 15 minutes.
- **Retrospective signal:** decision candidate; create a PDR if the 15-minute policy applies across clients.
```

---

## Log

<!-- Add newest entries at the top. -->

*(No unexpected events recorded yet.)*

---

> [← README](README.md)
