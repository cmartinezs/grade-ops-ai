# ⚛️ TASK 01 — Verify no legacy source files or stale imports remain

> **Status:** TODO
> **Workflow:** CLEANUP+VERIFY
> **Depends On:** (none — run after Stories 01–04 are all DONE)
> [← story file](../story-05-final-cleanup.md)

---

## Objective

Confirm that all 9 legacy package directories are empty of `.java` files and that no new-package source files still import old class FQNs. Fix any stragglers found.

---

## Technical Design

- **Approach:** Run a series of `find` and `grep` commands. Any hit means a previous story missed a deletion or import update — identify which story owns the fix, apply it, and rerun the check. This task produces no new files; its only output is a clean codebase.
- **Legacy packages and their expected fate:**

| Package | Emptied by story | New location |
|---------|-----------------|-------------|
| `common/` | Story 01 | `shared.domain.exception` + `shared.infrastructure.adapter.in.web` |
| `config/` | Story 01 | `shared.infrastructure.config` |
| `email/` | Story 01 | `shared.infrastructure.adapter.out.email.JavaMailEmailService` |
| `security/` | Story 01 | `shared.infrastructure.config.security` |
| `port/` | Story 01 + 02 | `shared.infrastructure.adapter.out.storage` + `auth.application.port.out` + `auth.domain.model` |
| `adapter/auth/` | Story 02 | `auth.infrastructure.adapter.out.firebase` |
| `adapter/storage/` | Story 01 | `shared.infrastructure.adapter.out.storage` |
| `domain/teacher/` | Story 03 | `teacher.infrastructure.adapter.out.persistence` (renamed) |
| `internal/` | Story 03 | `teacher.infrastructure.adapter.in.web` (renamed) |
| `auth/` (flat) | Story 02 | `auth.infrastructure.adapter.in.web` + handlers/orchestrators |
| `assessment/` (flat root files) | Story 04 | `assessment.*` hexagonal subdirs |

---

## Implementation Steps

### Part A — File existence checks

Run each command and confirm 0 results:

```bash
# Legacy shared infrastructure packages
find src/main/java -path "*/api/common/*.java"
find src/main/java -path "*/api/config/*.java"
find src/main/java -path "*/api/email/*.java"
find src/main/java -path "*/api/security/*.java"
find src/main/java -path "*/api/port/*.java"
find src/main/java -path "*/api/adapter/auth/*.java"
find src/main/java -path "*/api/adapter/storage/*.java"

# Legacy teacher packages
find src/main/java -path "*/api/domain/teacher/*.java"
find src/main/java -path "*/api/internal/*.java"

# Legacy auth flat package
find src/main/java -path "*/api/auth/AuthService.java" \
  -o -path "*/api/auth/PasswordResetService.java" \
  -o -path "*/api/auth/AuthController.java" \
  -o -path "*/api/auth/PasswordResetCodeEntity.java" \
  -o -path "*/api/auth/PasswordResetCodeRepository.java" \
  -o -path "*/api/auth/RegisterRequest.java" \
  -o -path "*/api/auth/ForgotPasswordRequest.java" \
  -o -path "*/api/auth/ResetPasswordRequest.java" \
  -o -path "*/api/auth/RegisterResponse.java" \
  -o -path "*/api/auth/RegisterResult.java" \
  -o -path "*/api/auth/InvalidTokenException.java"

# Legacy assessment flat package (root-level files — subdirs are fine)
find src/main/java -path "*/api/assessment/AssessmentController.java" \
  -o -path "*/api/assessment/AssessmentService.java" \
  -o -path "*/api/assessment/AssessmentSummaryDto.java" \
  -o -path "*/api/assessment/AssessmentStatus.java"
```

### Part B — Stale import reference checks

```bash
# Old common/config/email/security FQNs
grep -rn "cl\.gradeops\.ai\.api\.common\." src/ --include="*.java"
grep -rn "cl\.gradeops\.ai\.api\.config\." src/ --include="*.java"
grep -rn "cl\.gradeops\.ai\.api\.email\." src/ --include="*.java"
grep -rn "cl\.gradeops\.ai\.api\.security\." src/ --include="*.java"

# Old port / adapter FQNs
grep -rn "cl\.gradeops\.ai\.api\.port\." src/ --include="*.java"
grep -rn "cl\.gradeops\.ai\.api\.adapter\.auth\." src/ --include="*.java"
grep -rn "cl\.gradeops\.ai\.api\.adapter\.storage\." src/ --include="*.java"

# Old teacher / internal FQNs
grep -rn "cl\.gradeops\.ai\.api\.domain\.teacher\." src/ --include="*.java"
grep -rn "cl\.gradeops\.ai\.api\.internal\." src/ --include="*.java"

# Old auth flat FQNs
grep -rn "cl\.gradeops\.ai\.api\.auth\.AuthService\|cl\.gradeops\.ai\.api\.auth\.PasswordResetService" src/ --include="*.java"

# Old assessment flat FQNs
grep -rn "cl\.gradeops\.ai\.api\.assessment\.AssessmentService\|cl\.gradeops\.ai\.api\.assessment\.AssessmentSummaryDto" src/ --include="*.java"
```

### Part C — Fix any hits

For each file found or stale import detected:
1. Identify which story was responsible for its deletion/migration (use the table in Technical Design).
2. Apply the missing deletion or import update.
3. Rerun the affected commands until they return 0 results.

---

## Unit Tests

| # | Verification | Expected result |
|---|-------------|----------------|
| 1 | `find` for all legacy package files | 0 results each |
| 2 | `grep` for all legacy FQN imports | 0 results each |
| 3 | `./mvnw compile -q` after any fixes applied | Exits 0 |

---

## Done Criteria

- [ ] All 9 `find` commands return 0 results
- [ ] All `grep` commands for legacy FQN imports return 0 results
- [ ] `./mvnw compile -q` exits 0 after any fixes
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-05-final-cleanup.md)
