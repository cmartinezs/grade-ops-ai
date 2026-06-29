# 📐 Planning: 001-hexagonal-refactor

> **Status:** DONE  
> [← planning/README.md](../README.md)

---

## Files in this directory

| File | Purpose |
|------|---------|
| [`00-initial.md`](00-initial.md) | INITIAL phase — intent, target architecture, migration map |
| [`01-expansion.md`](01-expansion.md) | EXPANSION phase — story summary and dependency map |
| [`02-deepening/`](02-deepening/) | DEEPENING phase — one file per story, one subfolder per atomized story |
| [`TRACEABILITY.md`](TRACEABILITY.md) | Term traceability matrix for this planning |

---

## Retrospective

### What went well

**Coexistence strategy held.** Each story created the new package structure and deleted the old code it superseded in the same PR. The build stayed green after every merge — zero integration breaks across 5 stories and 5 PRs.

**Post-implementation code review loop was high-value.** Story 02 surfaced 9 real issues after the first implementation pass: a security filter registered twice via `@Component` + `SecurityConfig @Bean`, missing domain exceptions leaking Spring's `ResponseStatusException` into the application layer, a lost `pullDomainEvents()` call, and a `@WebMvcTest` / Spring Boot 4 incompatibility with `MockMvcSecurityAutoConfiguration`. Story 03 added 9 more fixes, the most impactful being `@Valid` + Bean Validation on controller requests (previously absent), security coverage for `InternalAuthFilter`, and an `ArgumentCaptor`-based persistence adapter test replacing a shallow delegation check.

**ArchUnit paid off immediately.** Adding `HexagonalArchitectureTest` in Story 01 (rather than Story 05) meant each subsequent story ran against live architectural rules. The broadened application-layer isolation rule (extended from `..usecase..` to `..application..`) was caught and fixed during Story 02's review — not discovered later.

**`TeacherRepositoryPort` pull-forward was the right call.** Moving it from Story 03 to Story 02 during the post-review audit eliminated a cross-context Spring Data import in the application layer before it could propagate to Story 03.

---

### Decisions that diverged from the initial plan

| Decision | Original plan | What actually happened | Rationale |
|----------|--------------|----------------------|-----------|
| `ResetPasswordOrchestrator @Transactional` | Orchestrators are NOT transactional (guideline) | Made transactional | Owns password-update + code-save unit of work; partial failure leaves data inconsistent |
| `TeacherRepositoryPort` location | Story 03 | Pulled into Story 02 | Application layer imported Spring Data interface directly; caught in Story 02 review |
| Sign-out test pattern | `SecurityMockMvcRequestPostProcessors` | `SecurityContextHolder.setContext()` directly | Spring Boot 4 `@WebMvcTest` has no `MockMvcSecurityAutoConfiguration`; `FilterChainProxy` absent from MockMvc |
| `HttpStatus.UNPROCESSABLE_ENTITY` | Used throughout | Replaced with `UNPROCESSABLE_CONTENT` | Deprecated in Spring Framework 7.0 (Spring Boot 4 baseline) |
| V7 migration | Not planned (no schema changes declared) | Added during Story 05 | Column `password_reset_codes.code` conflicted with reserved word in PostgreSQL; renamed to `raw_code` |

---

### What to improve next time

- **Done criteria checkboxes** should be ticked incrementally as each task completes, not left for the story's final status update. Story 04's checkboxes were all left unchecked despite DONE status.
- **Schema assumption** ("no Flyway changes") was stated in the initial plan but a V7 migration was needed by Story 05. Domain-model-to-schema mapping changes (even renames) should be audited per story, not assumed clean until the end.
- **`InternalAuthFilter` security tests** were missing from the initial test plan in `00-initial.md`. Controller security coverage (missing token, wrong key) should be an explicit checklist item in the testing strategy section of the initial plan.

---

### Metrics

| Dimension | Value |
|-----------|-------|
| Stories completed | 5 / 5 |
| Total tasks | 38 |
| PRs merged | 5 (stories 01–04) + 1 pending (story 05) |
| ArchUnit rules enforced | 5 |
| Post-review fixes applied | 18 (9 in story 02, 9 in story 03) |
| Open residuals at close | 1 (R-02: reset-code HTTP status alignment, deferred to US-012) |
| Flyway migrations added | 1 (V7 — rename `code` → `raw_code`) |

---

> [← planning/README.md](../README.md)
