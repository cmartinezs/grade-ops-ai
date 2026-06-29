# 🔍 DEEPENING: Story 05 — Legacy Package Cleanup + Final Verification

> **Status:** DONE
> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)

---

## Objective

Ensure the codebase is fully clean after Stories 01–04: no legacy packages remain, all 14 original tests (migrated) plus all new unit tests pass, and all four ArchUnit constraints hold green. Also verifies there are no stray Spring component scans or broken bean wiring introduced during the refactoring.

Depends on **Stories 02, 03, and 04** all being DONE.

---

## Tasks

| # | Task | Workflow | Status | Output |
|---|------|----------|--------|--------|
| 1 | [Verify no legacy source files or stale imports remain](story-05-final-cleanup/task-01-verify-no-legacy-artifacts.md) | CLEANUP+VERIFY | DONE | All 9 legacy package `find` commands → 0 results; all legacy FQN `grep` commands → 0 results |
| 2 | [Run full test suite + verify all 4 ArchUnit constraints](story-05-final-cleanup/task-02-run-full-test-suite.md) | TEST | DONE | `./mvnw test` exits 0; `HexagonalArchitectureTest` runs 5 tests, all green |
| 3 | [Verify Spring context loads cleanly with local profile](story-05-final-cleanup/task-03-verify-context-loads-locally.md) | TEST | DONE | `./mvnw spring-boot:run -Dspring.profiles.active=local` starts without bean-wiring errors; 401 on unauthenticated smoke test. Fixed: added V7 migration renaming `code` → `raw_code` in `password_reset_codes` |
| 4 | [Final code quality assertions + traceability update](story-05-final-cleanup/task-04-code-quality-and-traceability.md) | CLEANUP+VERIFY | DONE | `@Autowired`, `@Data on @Entity`, `@Transactional on controller` all → 0 hits; TRACEABILITY.md + TRACEABILITY-GLOBAL.md updated; `01-expansion.md` all 5 stories DONE |

---

## Done Criteria

- [x] Zero Java files remain in `common/`, `config/`, `email/`, `security/`, `port/`, `adapter/auth/`, `adapter/storage/`, `domain/teacher/`, `internal/`
- [x] Zero imports of old class FQNs (e.g., `cl.gradeops.ai.api.auth.AuthService`) in `src/`
- [x] `./mvnw test` passes — all original migrated tests + all new tests pass with 0 failures
- [x] `HexagonalArchitectureTest` reports 0 violations for all 4 rules
- [x] Application context loads cleanly with `spring.profiles.active=local`
- [x] No `@Autowired` field injection anywhere in `src/main/`
- [x] No `@Data` on any `@Entity` class
- [x] No `@Transactional` on any controller class
- [x] TRACEABILITY.md and TRACEABILITY-GLOBAL.md updated
- [x] `01-expansion.md` story summary table updated to reflect all 5 stories DONE

---

## Inconsistencies Found

| # | Description | Docs Involved | Status | Resolution Path |
|---|-------------|--------------|--------|----------------|
| — | *None yet* | — | — | — |

---

## Residuals

| # | Description | Deferred To | Status |
|---|-------------|------------|--------|
| — | *None* | — | — |

---

> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)
