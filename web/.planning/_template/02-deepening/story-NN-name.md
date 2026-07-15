# 🔍 DEEPENING: Story NN — [Story Name]

> **Status:** TODO
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)

---

## Objective

> *What specifically must be produced or changed in this story.*
> Example: `Create API support for password reset tokens: request token, validate token, update password, and expire used tokens.`

[Describe the concrete goal.]

---

## Risk

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| [e.g. Reset endpoint leaks whether an email exists] | H | M | [e.g. Always return the same public response and log details privately] |
| [e.g. Token persistence is inconsistent with ORM schema] | M | M | [e.g. Include DB/ORM validation task before marking story DONE] |

---

## Tasks

> **Each row in this table must have a corresponding `task-NN-name.md` file under `story-NN-name/` before this story can be marked `IN PROGRESS`.** Use `/plan-atomize` to generate all task files at once, or create them individually — but they must exist before execution begins. The task name in each row should link to its file once created.

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [Design reset-token persistence](story-NN-name/task-01-reset-token-persistence.md) | GENERATE-DOCUMENT | TODO | [Migration/entity/repository design captured] |
| 2 | [Implement reset endpoints](story-NN-name/task-02-reset-endpoints.md) | GENERATE-DOCUMENT | TODO | [API routes, service logic, and tests] |

---

## Test Suite

> Generated or refreshed with `/plan-test-suite <planning-id> story-NN`. The story-level file is `story-NN-name/TEST-SUITE.md`; each task-level file lives under `story-NN-name/test-suites/`.

- [ ] Story-level test suite covers unit, coverage, integration, acceptance/e2e, static analysis, style, architecture/design guide review, smoke, security, and mutation/test-strength gates as applicable.
- [ ] Task-level test suites exist for every task before execution.
- [ ] Acceptance and integration gates use isolated environments when possible: Docker Compose, Testcontainers, local emulators, sandbox profiles, or disposable fixtures.
- [ ] Acceptance dependency inventory covers every internal module, external service, database, queue, storage dependency, environment variable, port, seed dataset, readiness check, and teardown action needed to run acceptance tests.
- [ ] For Maven services with Cucumber/Gherkin, acceptance gates use profile `acceptanceTests` to boot the artifact in isolation and mock external dependencies.

---

## Done Criteria

- [ ] Reset request returns the same public response for known and unknown emails
- [ ] Used or expired tokens cannot update a password
- [ ] Automated tests cover success, expiry, reuse, and unknown-email behavior
- [ ] Code tasks include logging that supports execution tracing and correlation across calls, following `.planning/LOGGING.md`
- [ ] Story and task test suites are generated/refreshed, and applicable gates have evidence
- [ ] Acceptance dependency inventory has no unresolved dependency gaps
- [ ] TRACEABILITY.md updated with new terms from this story

---

## Inconsistencies Found

*Record any contradictions or gaps detected during this story. Use RECORD-INCONSISTENCY workflow.*

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| 1 | [e.g. API docs say reset tokens expire in 15 minutes, but support guide says 1 hour] | [docs/api/auth.md, docs/support/password-reset.md] | Open | [Resolve with RECORD-INCONSISTENCY; possible PDR if policy applies across clients] |
| — | *None yet* | — | — | — |

---

## Residuals

*Tasks or issues deferred to a future planning.*

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| 1 | [e.g. Add branded reset email template after provider is selected] | [Future email-template planning] | OPEN |
| — | *None* | — | — |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)
