# ⚛️ TASK 04 — Final code quality assertions + traceability update

> **Status:** DONE
> **Workflow:** CLEANUP+VERIFY
> **Depends On:** task-01, task-02
> [← story file](../story-05-final-cleanup.md)

---

## Objective

Run three targeted grep assertions that verify the no-stereotypes / no-autowired / no-transactional-on-controller rules were applied consistently across the entire codebase. Then update `TRACEABILITY.md` and `TRACEABILITY-GLOBAL.md` with any terms introduced during this planning, and update `01-expansion.md` to mark all 5 stories DONE.

---

## Technical Design

- **Approach:** The four ArchUnit rules in `HexagonalArchitectureTest` (verified in task-02) cover structural dependency violations but do NOT check for `@Autowired` field injection, `@Data` on `@Entity` classes, or `@Transactional` on `@RestController` classes. These three checks are done via `grep`. Any hit is a coding convention violation introduced during refactoring that was not caught by compile or ArchUnit.
- **Scope:** `src/main/java` only — test classes may legitimately use `@Autowired` in `@SpringBootTest` configurations.

---

## Implementation Steps

### Part A — Code quality grep assertions

Run each command against `src/main/java` and confirm 0 results:

```bash
# 1. No @Autowired field injection in production code
grep -rn "@Autowired" src/main/java --include="*.java"

# 2. No @Data on any @Entity class
#    (first find all entity files, then check for @Data in them)
grep -rl "@Entity" src/main/java --include="*.java" | xargs grep -l "@Data" 2>/dev/null

# 3. No @Transactional on any @RestController class
#    (find controller files, check for class-level @Transactional)
grep -rl "@RestController" src/main/java --include="*.java" | xargs grep -ln "^@Transactional\|^    @Transactional" 2>/dev/null

# 4. No @Service, @Component, or @Repository on non-@RestController classes
grep -rn "^@Service\|^@Component\|^@Repository" src/main/java --include="*.java" \
  | grep -v "@RestController"
```

Fix any hit:
- `@Autowired` → replace with constructor injection via `@RequiredArgsConstructor` or explicit constructor; declare class as `@Bean` if not already.
- `@Data on @Entity` → replace with `@Getter @Setter @NoArgsConstructor(access = AccessLevel.PROTECTED)`.
- `@Transactional on @RestController` → move `@Transactional` to the handler/use-case method.
- `@Service/@Component/@Repository` → remove stereotype; add `@Bean` declaration in the relevant `@Configuration` class.

### Part B — TRACEABILITY.md update

Open `.planning/active/001-hexagonal-refactor/TRACEABILITY.md` (or the planning's own traceability file) and register any domain terms introduced across all 5 stories that are not yet listed:

New terms to register (if not already present):
- `AggregateRoot<ID>` — shared domain base class
- `DomainEvent` — shared domain event interface
- `DomainException` — shared domain base exception
- `PasswordResetCode` — auth aggregate root
- `TeacherIdentity` — auth domain value object
- `AuthPort` — auth outbound port (Firebase operations)
- `PasswordResetCodeRepositoryPort` — auth outbound port
- `EmailNotificationPort` — auth outbound port
- `Teacher` — teacher aggregate root
- `TeacherId` — teacher value object
- `AuthProvider` — teacher enum value object
- `TeacherRepositoryPort` — teacher outbound port
- `ProvisionTeacherUseCase` — teacher inbound port
- `UpdatePilotFlagsUseCase` — teacher inbound port
- `AssessmentStatus` — assessment domain enum
- `ListAssessmentsUseCase` — assessment inbound port
- `AssessmentRepositoryPort` — assessment outbound port (stub)
- `AssessmentSummaryResult` — assessment application result
- `HexagonalArchitectureTest` — ArchUnit test class

### Part C — TRACEABILITY-GLOBAL.md update

Open `.planning/TRACEABILITY-GLOBAL.md`. Find the row for planning `001-hexagonal-refactor` and:
- Update status to `DONE`
- Note that all 5 stories completed

### Part D — Mark stories DONE in 01-expansion.md

Open `.planning/active/001-hexagonal-refactor/01-expansion.md` Story Summary table. Update the `Status` column for all 5 stories from `TODO` → `DONE`.

---

## Unit Tests

| # | Verification | Expected result |
|---|-------------|----------------|
| 1 | `grep @Autowired src/main/java` | 0 results |
| 2 | `@Data` on `@Entity` files | 0 files match |
| 3 | `@Transactional` on `@RestController` classes | 0 results |
| 4 | `@Service/@Component/@Repository` in `src/main/java` (excluding `@RestController`) | 0 results |
| 5 | TRACEABILITY.md contains all 19 new terms | Read the file |
| 6 | TRACEABILITY-GLOBAL.md row for `001-hexagonal-refactor` marked DONE | Read the file |
| 7 | `01-expansion.md` shows all 5 stories DONE | Read the file |

---

## Done Criteria

- [ ] `grep @Autowired src/main/java --include="*.java"` → 0 results
- [ ] No `@Entity` class also has `@Data` annotation
- [ ] No `@RestController` class has class-level `@Transactional`
- [ ] No `@Service`, `@Component`, or `@Repository` outside `@RestController` in `src/main/java`
- [ ] TRACEABILITY.md updated with all new domain terms from this planning
- [ ] TRACEABILITY-GLOBAL.md updated: `001-hexagonal-refactor` marked DONE
- [ ] `01-expansion.md` Story Summary table: all 5 stories show DONE
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-05-final-cleanup.md)
