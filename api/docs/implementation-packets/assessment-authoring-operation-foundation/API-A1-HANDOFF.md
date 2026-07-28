<a id="top"></a>

# API-A1-HANDOFF — Session A1: Schema and Inert Domain

**Status:** Complete. **Parent:** [README](README.md)

Handoff gate fields per [09 — Session Handoff Protocol](../../../../docs/implementation-packets/assessment-authoring-operation-foundation/09-session-handoff-protocol.md#-handoffmd-format), specialized for an intermediate API session. Session A2 will not proceed if any section below is missing or contradicted by the actual branch state — write "None." where genuinely empty, never delete a section.

```markdown
# API Session A1 Handoff — Schema and Inert Domain

## Session
A1 — Schema and Inert Domain

## Branch
feat/assessment-authoring-operation-foundation-api

## HEAD
2d6d408c99253fbf230214a02d48cf0955932834

## Starting commit
f8cddc1ad5b64d2fa04f1841ea7b6d4a93809fa6 (tip of develop/the integration branch at session start;
 branch feat/assessment-authoring-operation-foundation-api already existed and was already
 checked out and up to date with origin at session start — this session did not create it)

## Tasks completed
01 — Done. V13 migration (ai_operations, agent_attempts + uq_ai_operations_in_flight).
02 — Done. V14/V15 migrations (assessment_revisions + assessments.current_revision_id/lock_version).
03 — Done. V16 migration (idempotency_records).
04 — Done. AssessmentRevision aggregate + persistence, test-first (TDD).
05 — Done. AiOperation/AgentAttempt aggregates + persistence, test-first (TDD).

## Commits
1352d3a feat(api): add durable ai_operations/agent_attempts schema
a9e316b feat(api): add assessment_revisions schema and current-revision pointer
490bed3 feat(api): add idempotency_records schema
1b4db20 feat(api): add AssessmentRevision aggregate and persistence
2d6d408 feat(api): add AiOperation and AgentAttempt aggregates and persistence

## Migrations
V13__add_agent_attempts_and_ai_operations.sql — Applied cleanly alongside V1-V12 (Testcontainers/
 PostgreSQL 16). uq_ai_operations_in_flight verified twice: a schema-level direct-SQL test
 (AssessmentAuthoringSchemaMigrationTest) and an adapter-level test through the domain/persistence
 layer (AiOperationPersistenceAdapterTest) both prove a second PENDING/IN_PROGRESS row for the same
 (assessment_id, operation_type) is rejected, and that a second row is allowed once the first is
 terminal (SUCCEEDED) or has a different operation_type.
V14__add_assessment_revisions.sql — Applied cleanly after V13. UNIQUE(assessment_id, version_number)
 and the self-referential previous_revision_id FK both verified by direct-SQL tests. Also adds the
 ai_operations.expected_revision_id/result_revision_id → assessment_revisions(id) FKs that V13 could
 not add yet (assessment_revisions did not exist at V13) — see "Decisions applied".
V15__add_assessment_current_revision.sql — Applied cleanly after V14. Verified existing assessments
 rows get lock_version = 0 and current_revision_id = NULL.
V16__add_idempotency_records.sql — Applied cleanly after V15. Unique constraint verified — see
 "Decisions applied" for a required correction to the constraint as literally specified in
 LOCAL-CONTRACTS.md.

## Classes introduced
Domain (cl.gradeops.ai.api.assessment.domain.model):
- AssessmentRevision.java, RevisionOrigin.java
- AiOperation.java, AiOperationType.java, AiOperationStatus.java
- AgentAttempt.java, AgentAttemptStatus.java

Application ports (cl.gradeops.ai.api.assessment.application.port.out):
- AssessmentRevisionRepositoryPort.java
- AiOperationRepositoryPort.java
- AgentAttemptRepositoryPort.java

Infrastructure — persistence (cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence):
- AssessmentRevisionJpaEntity.java, AssessmentRevisionJpaRepository.java,
  AssessmentRevisionPersistenceMapper.java, AssessmentRevisionPersistenceAdapter.java
- AiOperationJpaEntity.java, AiOperationJpaRepository.java,
  AiOperationPersistenceMapper.java, AiOperationPersistenceAdapter.java
- AgentAttemptJpaEntity.java, AgentAttemptJpaRepository.java,
  AgentAttemptPersistenceMapper.java, AgentAttemptPersistenceAdapter.java

Migrations (api/src/main/resources/db/migration):
- V13__add_agent_attempts_and_ai_operations.sql
- V14__add_assessment_revisions.sql
- V15__add_assessment_current_revision.sql
- V16__add_idempotency_records.sql

None of the above is registered as a Spring `@Bean` in `AssessmentConfig.java` or any other
 configuration class — see "Decisions applied".

## Contracts changed
1. `idempotency_records`'s unique constraint uses `UNIQUE NULLS NOT DISTINCT (scope_type,
   teacher_uid, assessment_id, operation_type, idempotency_key)` (PostgreSQL 15+), not the plain
   `UNIQUE (...)` shown in LOCAL-CONTRACTS.md § Persistence changes. Column list, table shape, and
   semantics are unchanged — only the null-handling modifier differs. Required because
   `teacher_uid` is always null for `ASSESSMENT`-scoped rows and `assessment_id` is always null for
   `TEACHER`-scoped rows; standard SQL `UNIQUE` treats NULL as distinct from NULL, so a plain
   `UNIQUE` constraint would silently accept unlimited duplicate idempotency keys within either
   scope — verified by a failing test before applying this fix. See Task 03's commit for detail.
2. `ai_operations.expected_revision_id`/`result_revision_id` do not get their `REFERENCES
   assessment_revisions(id)` FK constraints in V13 (as LOCAL-CONTRACTS.md's single combined SQL
   block shows) because `assessment_revisions` does not exist until V14. V14 adds these two FKs via
   `ALTER TABLE ai_operations ADD CONSTRAINT ...` immediately after creating `assessment_revisions`,
   mirroring the exact pattern already used by `V12__add_agent_execution_logs.sql` for
   `assessment_drafts.agent_execution_log_id`. No field, table, or semantic differs from the
   contract — only the migration-file placement of two FK constraints.

No other field, table, taxonomy, or contract value differs from LOCAL-CONTRACTS.md.

## Tests
- AssessmentAuthoringSchemaMigrationTest — 12/12 PASS (Testcontainers; covers V13, V14, V15, V16
  schema/constraint behavior incrementally, per task)
- AssessmentRevisionTest — 25/25 PASS (unit; chain-integrity invariants, immutability, all four
  factories, LEGACY_UNKNOWN restore-only rule)
- AssessmentRevisionPersistenceAdapterTest — 4/4 PASS (Testcontainers; round-trip fidelity incl.
  JSON list fields and full 3-revision chain)
- AiOperationTest — 18/18 PASS (unit; valid/invalid state transitions, restore, validation)
- AgentAttemptTest — 13/13 PASS (unit; valid/invalid state transitions, restore, validation)
- AiOperationPersistenceAdapterTest — 4/4 PASS (Testcontainers; round-trip fidelity,
  uq_ai_operations_in_flight rejected at the DB level through the adapter)
- AgentAttemptPersistenceAdapterTest — 5/5 PASS (Testcontainers; round-trip fidelity incl.
  NUMERIC cost_estimate and JSONB structured_result)

All were written before their corresponding production class (TDD RED confirmed by compilation
 failure for AssessmentRevisionTest/AiOperationTest/AgentAttemptTest before the domain classes
 existed), then made to pass (GREEN), per task.

## Baseline
Before this session: 289 tests (verified against the starting commit before any change in this
 session, with Docker access — see "Known residual risks" for the sandbox's Docker quirk).
After this session: 370 tests (289 + 81 new: 12 + 25 + 4 + 18 + 13 + 4 + 5).
./mvnw clean test → PASS (370/370, 0 failures, 0 errors) — last run against HEAD
 2d6d408c99253fbf230214a02d48cf0955932834, immediately before writing this handoff.
`grep -rn "AssessmentRevision\|AiOperation\|AgentAttempt" api/src/main/java --include="*.java" -l`
 shows only the 21 new files listed above under "Classes introduced" — zero references in
 `application/usecase/` or `infrastructure/adapter/in/web/`, confirmed by an explicit grep scoped
 to those two directories returning no matches. The new code is verifiably unreachable from any
 productive request/response path.

## Decisions applied
1. `idempotency_records` unique constraint uses `NULLS NOT DISTINCT` — see "Contracts changed" #1.
2. `ai_operations`'s two revision-id FKs are added in V14, not V13 — see "Contracts changed" #2.
3. Task 02 touches only the two migration files, not `Assessment.java` or `AssessmentJpaEntity`.
   `Assessment.currentRevisionId`/`lockVersion` exist in the schema after this session but are not
   yet mapped into the domain class or JPA entity — CLAUDE-API-A1-PROMPT.md's Task 02 "Files" list
   names only the SQL migrations, and 02-domain-and-data-changes.md's `withCurrentRevision(UUID)`
   method belongs to the use case that actually performs the CAS update (Task 07B, Session A2), not
   to schema-and-inert-domain scope. Confirm this reading before Session A2 begins wiring the
   coordinator, since `Assessment` will need those two fields added at that point.
4. None of the new ports/adapters are registered as Spring `@Bean`s in `AssessmentConfig.java`.
   `AssessmentDraftPersistenceAdapter` and siblings are registered as beans in the existing code
   even though this cut's precedent exists, but doing the same here would mean Spring instantiates
   and holds live references to adapters nothing calls yet — a form of passive wiring the session's
   "entirely inert... nobody calls this new code yet" framing argues against. The Testcontainers
   tests instantiate every adapter directly (`new XyzPersistenceAdapter(autowiredJpaRepo, new
   XyzMapper())`), exactly mirroring how `AssessmentDraftPersistenceAdapterIntegrationTest` already
   tests `AssessmentDraftPersistenceAdapter` without going through the Spring-registered bean.
   Session A2 will need to add `@Bean` methods for whichever of these its coordinator/handlers
   inject.
5. `AssessmentRevisionPersistenceAdapterTest`'s Testcontainers round-trip test is named exactly as
   CLAUDE-API-A1-PROMPT.md's Task 04 acceptance criteria states it
   (`AssessmentRevisionPersistenceAdapterTest`, no "Integration" suffix) rather than following the
   repo's existing `*PersistenceAdapterTest` (Mockito) / `*PersistenceAdapterIntegrationTest`
   (Testcontainers) split used by `AssessmentDraft`. No separate Mockito-mock adapter test was
   added for any of the three new adapters — mocking a `JpaRepository` would not exercise the JSONB
   mapping or DB constraints that are the actual point of these tests, and TEST-PLAN.md's own
   "Repository/adapter" row specifies Testcontainers, not H2/mocks, for exactly this reason.
6. `AiOperation`/`AgentAttempt` state-transition methods (`markInProgress`/`markSucceeded`/etc.)
   return a new instance rather than mutating in place, matching this codebase's existing immutable
   -value-object style for `Assessment`/`AssessmentDraft` (`restore`, no setters on the domain
   class) rather than introducing a new "mutable aggregate" pattern into the codebase.
7. `AssessmentRevision.restore()` enforces "actorId is null iff origin == LEGACY_UNKNOWN" as a
   thrown `DomainInvariantViolationException`, not just descriptive documentation. This is not one
   of LOCAL-CONTRACTS.md's seven numbered "Invariants," but it is a direct, literal implementation
   of the field table's own stated constraint ("actorId: String // teacherUid; null only for
   LEGACY_UNKNOWN").

## Known residual risks
Carried forward from the ADRs, unchanged by this session (not yet exercised — no use case calls
 this code yet):
- Orphaned in-flight `AgentAttempt`/`AiOperation` records after a crash between Phase 0 commit and
  Phase 2 are an accepted, documented outcome (Durable AI Operation Model ADR), not a bug — Session
  A2's coordinator is what will actually produce this state.
- No cleanup job exists for expired `idempotency_records` rows (24h retention is a data column, not
  an enforced deletion) — explicitly deferred past this cut per the Idempotency and Concurrency
  Strategy ADR.

New, surfaced by this session's implementation (informational, not blocking):
- Hibernate 7's `@JdbcTypeCode(SqlTypes.JSON)` mapping for a `String`-typed Java field (used for
  `AgentAttempt.structuredResult`, and already relied upon for the `List<String>` JSONB columns
  this packet mirrors) round-trips through parse-then-re-serialize, not a raw byte-for-byte
  passthrough — it normalizes whitespace (e.g. `{"a":1}` becomes `{"a": 1}` after a round trip).
  Semantically identical JSON, not byte-identical. Anyone writing an exact-string assertion against
  a `structuredResult` value should use whitespace-tolerant or JSON-aware comparison, not
  `isEqualTo` on the raw string (this session's own tests were corrected to do this after first
  hitting the mismatch).
- This specific sandbox's Docker access (WSL2 + Docker Desktop CLI integration, mounted at
  `/mnt/wsl/docker-desktop/...`) was intermittently unavailable during this session — twice a full
  `mvn test` run produced dozens of unrelated Docker-dependent test errors ("Previous attempts to
  find a Docker environment failed. Will not retry" — a Testcontainers-internal cache of the first
  failure in a JVM run cascades to every subsequent Docker-dependent test class in that same run),
  and once the `docker` CLI itself briefly returned "Input/output error" / "command not found." Each
  time, waiting ~15-20 seconds and retrying resolved it completely, with the full suite going green
  immediately after. This is an environment/sandbox condition, not a code defect — every reported
  test count and PASS/FAIL status in this handoff reflects a clean run with Docker fully responsive.
  If Session A2 sees a wall of unrelated Docker/Testcontainers errors, retry before investigating
  further.

## Blockers
None.

## Next session prerequisites
Confirm this handoff is complete and accurate. Re-run `./mvnw clean test` yourself before trusting
 the test count above — and if Docker/Testcontainers errors appear across many unrelated test
 classes at once, retry once before concluding anything is broken (see "Known residual risks").
 Also read "Decisions applied" #3 and #4 before starting Task 06/07B: `Assessment` does not yet
 have `currentRevisionId`/`lockVersion` mapped into its domain class or JPA entity, and none of this
 session's adapters are yet Spring beans — both will need to be added as part of wiring the
 durable coordinator.

## Next session first command
Give agents/docs/implementation-packets/assessment-authoring-operation-foundation/HANDOFF.md's
 completion status a check (Agents Task 07A), then execute CLAUDE-API-A2-PROMPT.md.
```

---

← [README](README.md) | [↑ inicio](#top)
