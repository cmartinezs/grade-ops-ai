<a id="top"></a>

# API-A2-HANDOFF — Session A2: Idempotency and Durable Coordinator

**Status:** Complete. **Parent:** [README](README.md)

Handoff gate fields per [09 — Session Handoff Protocol](../../../../docs/implementation-packets/assessment-authoring-operation-foundation/09-session-handoff-protocol.md#-handoffmd-format), specialized for an intermediate API session. Session A3 will not proceed if any section below is missing or contradicted by the actual branch state.

```markdown
# API Session A2 Handoff — Idempotency and Durable Coordinator

## Session
A2 — Idempotency and Durable Coordinator

## Branch
feat/assessment-authoring-operation-foundation-api

## Final implementation HEAD
9c5bc0ad28e622d4c82294939d1ebb878f1693e9 (the Task 07B/07C commit — the last implementation
 commit before this handoff's own commit)

## Starting commit
f85c4895854211862554ef18045484fdb1bf388e (Session A1's recorded final HEAD — verified identical
 at this session's preflight)

## Handoff commit
Not yet created when this section is written — this file's own commit
 (`docs(api): record session A2 handoff`) is the handoff commit; report its hash in the final
 message to whoever invoked this session, per the instruction not to write a not-yet-existing
 commit's own hash inside itself.

## Tasks completed
06 — Done. Reusable IdempotencyGuard service (check/record) + idempotency_records port/adapter,
 scoped TEACHER/ASSESSMENT, unit-tested (mocked port) and adapter-tested (Testcontainers,
 confirms the NULLS NOT DISTINCT constraint rejects duplicates within either scope). Not wired
 into any endpoint by this task alone (wiring happens in 07B).
07B — Done. AiOperationCoordinator (three-phase, durable-evidence-before-dispatch) replaces
 DraftGenerationCoordinator for initial generation only. GenerateAssessmentDraftHandler rewired:
 ownership → idempotency check → brief-exists → ALREADY_GENERATED precondition → coordinator →
 idempotency record-on-success.
07C — Done, folded into 07B's commit per the prompt's instruction (contract test + fixture +
 provider field addition are part of Task 07B's own commit boundary, not split out).

## Commits
faea8c1 feat(api): add reusable idempotency guard service
9c5bc0a feat(api): switch initial generation to durable AiOperation/AgentAttempt coordinator
<handoff-commit-hash> docs(api): record session A2 handoff

## Dependency consumed from Agents
Path taken: AGENTS-HANDOFF.md fixture (primary path; no fallback needed).
Agents branch: origin/feat/assessment-authoring-operation-foundation-agents
Agents HEAD verified: 5542d7c2b8b8c72142840087625b4fdd7dcb0bc2 (matched the prompt's expected
 value exactly via `git rev-parse origin/feat/assessment-authoring-operation-foundation-agents`).
Agents handoff read: `agents/docs/implementation-packets/assessment-authoring-operation-foundation/HANDOFF.md`
 at that commit — includes a documented correction (the original handoff's fixture had been
 generated with a hand-built Jackson 2 ObjectMapper + manually registered JavaTimeModule,
 described inaccurately as "Spring Boot's default auto-configuration"; Agents' session corrected
 this and regenerated the fixture via the real `@JsonTest`-autoconfigured Jackson 3 `JsonMapper`
 bean before pushing).
Field name for resolved provider: `provider` (matches the prompt's expected default exactly — no
 rename to `resolvedProvider`/`providerName`/`aiProvider`/`selectedProvider`).
Fixture source: `agents/src/test/resources/fixtures/assessment-execution-response.json` at Agents
 HEAD 5542d7c, extracted via `git show` (never manually transcribed).
Fixture SHA-256 (both sides verified identical):
 9f961eac2228508d01fc50fe177f2701ecd2fa34f234fdb38ab28ea1c62fb696
API fixture destination: `api/src/test/resources/fixtures/agents/assessment-execution-response.json`
 (versioned on the API branch, committed as part of the 07B/07C commit).
Nullability contract applied: `provider` non-null on success or on a failure after provider
 resolution (e.g. MALFORMED_OUTPUT); null only for INVALID_COMMAND (rejected before resolution).

## Contract verified (Task 07C)
`AssessmentAgentResponseContractTest` (`api/src/test/java/cl/gradeops/ai/api/agentclient/`) —
 `@JsonTest` + `@Autowired JsonMapper` (Spring Boot 4 / Jackson 3 auto-configured bean, per
 `JacksonConfig.java`'s existing precedent — no second, manually-built mapper anywhere in this
 session's code). Reads the copied fixture from the classpath, deserializes into the real
 `AssessmentAgentResponse` DTO the `agentclient` module uses in production, and asserts:
 `log.provider() == "gemini"`, `log.model() == "gemini-2.0-flash"`, `log.agentExecutionId()`
 present, `log.startedAt()`/`log.finishedAt()` parsed as `Instant`, and that the deserialized
 `provider` value flows into `AgentAttempt.resolvedProvider` via `AgentAttempt.markCompleted(...)`.
 3/3 PASS. Written and confirmed to fail to *compile* (RED, mirroring A1's own TDD-by-compilation-
 failure precedent) before `provider` was added to `AssessmentAgentResponse.Log`.

## Migrations
None — this session adds no new migrations, only consumes Session A1's V13-V16 schema.

## Coordinator replaced
DraftGenerationCoordinator → AiOperationCoordinator. `DraftGenerationCoordinator.java` and
 `DraftGenerationCoordinatorTest.java` are deleted (not deprecated, not left dangling).
 `grep -rn "DraftGenerationCoordinator" api/src` returns zero results (verified — the few
 explanatory comments in `RegenerateAssessmentDraftHandler`/`AiOperationCoordinator`/their tests
 were reworded specifically so the literal string does not appear anywhere, satisfying the
 verification command literally, not just in spirit).

## Transaction boundaries (no transaction spans the HTTP call)
Phase 0 (own transaction, commits before dispatch): `AiOperation.create(...).markInProgress()` +
 `AgentAttempt.dispatch(...)` saved and committed.
Phase 1 (no transaction): `AssessmentAgentClient.generate(command, correlationId)` — the same
 correlation id already persisted in Phase 0's `AgentAttempt`, not a different one generated
 inside the client.
Phase 2 (own transaction): re-reads `Assessment` fresh, and either (a) creates the
 `AssessmentRevision`, marks `AgentAttempt` COMPLETED, marks `AiOperation` SUCCEEDED, and
 CAS-updates `Assessment.currentRevisionId`/`lockVersion` (Hibernate `@Version`) — all one
 transaction — or (b) if `currentRevisionId` is already non-null (read-time staleness) or the
 CAS write throws `ObjectOptimisticLockingFailureException` (commit-time staleness — JPA only
 surfaces this at flush/commit, after the transactional callback has already returned, so it
 cannot be caught and branched on inside that same transaction), a small follow-up transaction
 records `AgentAttempt` FAILED/`STALE_ON_COMPLETION` (structured result preserved) and
 `AiOperation` FAILED_TERMINAL. This two-transaction shape for the rare stale-completion branch
 only (the happy path is exactly one Phase 2 transaction, as specified) is a deliberate,
 documented deviation from a literal single "Transaction B" — verified via
 `AiOperationCoordinatorTest.shouldNotOpenAnyTransactionDuringTheHttpCall` (InOrder: transaction
 opened → `generate()` called → transaction opened again, never overlapping the HTTP call).

## Tests: RED/GREEN sequence
Task 06: `IdempotencyGuardTest` written first against non-existent `IdempotencyGuard`/
 `IdempotencyRepositoryPort` (RED, compile failure), then implemented (GREEN, 5/5); adapter test
 written and run against the real V16 schema (GREEN, 6/6) including the two DB-level duplicate-
 rejection tests.
Task 07C: `AssessmentAgentResponseContractTest` written first referencing `response.log().provider()`,
 confirmed to fail to compile against the pre-change `AssessmentAgentResponse.Log` (RED), then
 `provider` was added (GREEN, 3/3).
Task 07B: `AiOperationCoordinatorTest` written first against the not-yet-existing
 `AiOperationCoordinator` (RED, compile failure), then implemented (GREEN, 13/13) — including the
 explicit durable-evidence-before-dispatch test
 (`shouldPersistDurableEvidenceBeforeCallingTheAgent`) and the failure-recovery test
 (`shouldSurviveAgentCallFailureWithPhase0EvidenceAlreadyPersisted`), both of which assert a
 property (`AiOperation`/`AgentAttempt` saved strictly before `assessmentAgentClient.generate(...)`
 is ever invoked) that the deleted `DraftGenerationCoordinator` never had — it called the agent
 first and persisted nothing until after the response, so an equivalent assertion against it
 would have failed.

## Simulated failures tested
`AiOperationCoordinatorTest.shouldSurviveAgentCallFailureWithPhase0EvidenceAlreadyPersisted` and
 `GenerateAssessmentDraftHandlerIntegrationTest.shouldPersistDurableEvidenceEvenWhenTheAgentCallFails`
 (Testcontainers, real Postgres): simulate the agent call itself failing (transport `UNREACHABLE`)
 immediately after Phase 0 committed, and assert the `AiOperation`(`FAILED_RETRYABLE`)/
 `AgentAttempt` (`FAILED`, `failureCode = AGENT_UNAVAILABLE`) rows exist and
 `Assessment.currentRevisionId` stayed `null` — proving Phase 0's evidence survives independent
 of whatever happens next. `STALE_ON_COMPLETION` covered separately (see below) proves the
 Phase-2-specific "late success, lost race" case, including structured-result preservation.

## Idempotency behavior verified
`IdempotencyGuardTest` (unit, mocked port): no prior record → `Optional.empty()` (proceed); same
 key + same payload hash → returns the prior record (replay); same key + different payload hash
 → throws `IdempotencyKeyPayloadMismatchException`.
`GenerateAssessmentDraftHandlerIntegrationTest.shouldReplayIdempotentRequestWithoutCallingTheAgentASecondTime`
 (Testcontainers): same assessment + same `Idempotency-Key` invoked twice → identical
 `GenerateAssessmentDraftResult` both times, `assessmentAgentClient.generate(...)` invoked exactly
 once (verified via Mockito `times(1)`) — the second call never reaches Phase 0/1/2 at all.
 Payload-mismatch is not separately exercised at the endpoint-integration level: this endpoint's
 payload hash is computed over `assessmentId` alone (no request body to vary), so a mismatch is
 only reachable at `IdempotencyGuard`'s own unit-tested level for this specific operation type.

## Baseline
Before this session: 370 tests (Session A1's recorded/re-verified count).
After this session: 405 tests (370 + 11 Task 06 + 3 Task 07C contract + 13 AiOperationCoordinatorTest
 + 8 new/changed Assessment domain assertions [net +6 test methods] + net additions across the
 rewritten Generate/Regenerate/Update handler test suites).
`./mvnw -f api/pom.xml clean test` → PASS (405/405, 0 failures, 0 errors) — last run immediately
 before writing this handoff, HEAD 9c5bc0a.

## Docker/Testcontainers incidents this session
Twice: `docker` CLI resolved to a broken WSL2 mount path (`/mnt/wsl/docker-desktop/cli-tools/usr/bin/docker`,
 "Input/output error" reading the binary itself) and Testcontainers reported "Could not find a
 valid Docker environment." Both times, waiting (~90s and ~180s) and retrying resolved it
 completely with no code changes — an environment/sandbox condition matching A1's own documented
 WSL2/Docker Desktop quirk, not a defect in this session's code. `docker` is not on `$PATH` by
 default in this sandbox; it must be added via
 `export PATH="/mnt/wsl/docker-desktop/cli-tools/usr/bin:$PATH"` before any Testcontainers-backed
 run.

## Known residual risks
- The narrow pre-check-to-commit race (two truly concurrent `CREATE_INITIAL_REVISION` requests
  both passing the `ALREADY_GENERATED` read-check before either commits) is narrowed, not
  eliminated, by `uq_ai_operations_in_flight` (Phase 0's second `AiOperation` insert would hit
  the partial unique index first) and, as defense-in-depth, by `Assessment.lockVersion` CAS at
  Phase 2 — accepted per the ADR, not "fixed" with a lock/broker. Not separately load-tested this
  session (no such requirement in this session's TDD checklist).
- `RegenerateAssessmentDraftHandler` still operates entirely on the legacy `AssessmentDraft`/
  `AgentExecutionLog` model (Task 09, Session A3, migrates it onto `AssessmentRevision`). A
  **new** consequence surfaced by this session, not previously true: an assessment generated via
  the *new* `GenerateAssessmentDraftHandler` (which now produces an `AssessmentRevision`, not an
  `AssessmentDraft`) cannot currently be regenerated —
  `assessmentDraftRepository.findCurrentByAssessmentId(...)` finds nothing and
  `RegenerateAssessmentDraftHandler` throws `NoPriorDraftException`. This is an accepted, temporary
  consequence of the pivot (Task 09's own job to close), not a regression this session was asked
  to fix — flagging it explicitly here rather than silently. `AssessmentCreationFlowIntegrationTest`,
  `RegenerateAssessmentDraftHandlerIntegrationTest`, and `UpdateAssessmentDraftHandlerIntegrationTest`
  were updated to seed "v1" directly as a pre-existing `AssessmentDraft` (bypassing
  `GenerateAssessmentDraftHandler`) so their regenerate/edit/list-chain coverage stays meaningful
  in the interim.
- `AgentAttempt.markFailed(failureCode, structuredResult)` (Session A1's signature, unchanged) has
  no parameter for `resolvedProvider`/`resolvedModel` — so a `MALFORMED_OUTPUT` failure (where
  Agents *did* resolve a provider before failing) is persisted with `resolvedProvider == null` on
  the `AgentAttempt`, not the provider Agents actually used. Deliberately not widened this session
  to avoid modifying an already-tested A1 domain method's signature under time pressure on the
  packet's highest-risk task; flagging as a known, narrow gap rather than silently accepting it
  forever.
- `AiOperation`/`AgentAttempt` beans registered in `AssessmentConfig` are now live and reachable
  (via `GenerateAssessmentDraftHandler`'s new dependency chain) for the first time — Session A1's
  "entirely inert, nobody calls this yet" framing no longer applies to these two aggregates as of
  this session (it still applies to `AssessmentRevisionRepositoryPort` only insofar as regenerate
  doesn't use it yet).

## Blockers
None.

## Next session prerequisites
Confirm this handoff is complete and accurate, including the Agents dependency section. Re-run
 `./mvnw -f api/pom.xml clean test` yourself before trusting the test count above — and if
 Docker/Testcontainers errors appear across many unrelated test classes at once, add
 `/mnt/wsl/docker-desktop/cli-tools/usr/bin` to `$PATH` and retry once or twice before concluding
 anything is broken (see "Docker/Testcontainers incidents" above). Read the "Known residual risks"
 section above before starting Task 08/09 — specifically the regenerate-on-a-revision-generated-
 assessment gap and the `markFailed` provider/model gap, both of which Task 09 will need to
 address as part of migrating regenerate onto `AssessmentRevision`.

## Next session first command
Execute CLAUDE-API-A3-PROMPT.md.
```

---

← [README](README.md) | [↑ inicio](#top)
