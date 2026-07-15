# 🌱 INITIAL: [Planning Name]

> **Status:** Initial
> [← planning/README.md](../../README.md)

---

## Intent

> *What needs to be done, in one sentence.*
> Example: `Add password reset by email to the API and web login flow.`

[Describe the intent here.]

---

## Why

> *Why does this planning exist? What problem does it solve or what value does it deliver?*
> Example: `Users currently require support intervention when they forget a password. This planning reduces support load and restores account access without manual admin work.`

[Describe the motivation and context.]

---

## Approximate Scope

> *Which repositories or areas might be affected? This does not need to be exhaustive.*
> In a monorepo, if a child artifact has its own `.planning/`, list it here but keep its implementation in a child planning created inside that artifact. The parent planning coordinates only parent-scope work and synchronization.
> When git is enabled, child plannings must run from dedicated sibling worktrees such as `../<worktree-prefix>`, with branch names prefixed by the worktree name.
> Example: mark `api/` for token endpoints, `web/` for reset screens, and `.planning/` if workflow docs or smoke-test plans must change.

- [ ] `docs/` — [brief note]
- [ ] `web/` — [brief note]
- [ ] `api/` — [brief note]
- [ ] `agents/` — [brief note]
- [ ] `infra/` — [brief note]
- [ ] `.planning/` — [brief note]

---

## Initiator

- **Requested by:** [human / AI agent / automatic trigger]
- **Date:** YYYY-MM-DD
- **Related planning (if continuation):** [NNN-name or "none"]

Example:

- **Requested by:** human
- **Date:** 2026-07-12
- **Related planning (if continuation):** 004-auth-cleanup

---

## Supersedes

> *Fill only if this planning replaces or contradicts a previous planning. Leave blank otherwise. Run `SUPERSEDE-PLANNING` workflow before creating this planning if applicable.*
> Example: `Supersedes 003-password-reset-prototype because the prototype stored reset tokens in memory and this planning replaces it with persistent expiring tokens.`

*(none)*

---

## Next Step

- [ ] When dimensioned → fill `01-expansion.md` and move to `planning/active/`
- [ ] If needs clarification first → document open questions below

### Open Questions

Examples:

- [ ] Which email provider sends password reset messages in local and production environments?
- [ ] Should reset links expire after 15 minutes or 1 hour?

*None yet.*

---

> [← planning/README.md](../../README.md)
