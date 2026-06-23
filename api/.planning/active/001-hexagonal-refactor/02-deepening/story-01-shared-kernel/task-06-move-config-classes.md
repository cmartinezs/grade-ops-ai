# ⚛️ TASK 06 — Move config classes to shared.infrastructure.config

> **Status:** DONE
> **Workflow:** REFACTOR
> **Depends On:** —
> [← story file](../story-01-shared-kernel.md)

---

## Objective

Move `FirebaseConfig`, `JacksonConfig`, `GradeOpsEmailProperties`, and `GradeOpsWebProperties` from `cl.gradeops.ai.api.config` to `cl.gradeops.ai.api.shared.infrastructure.config`. Update all importing files (main and test). Delete the `config/` package.

---

## Technical Design

- **Approach:** Pure package-rename move — no logic changes. `FirebaseConfig` and `JacksonConfig` produce Spring beans; their new location is still under the `cl.gradeops.ai.api` root, so `@SpringBootApplication`'s default component scan picks them up without any `@Import` or `@ComponentScan` addition. `GradeOpsEmailProperties` uses `@ConfigurationProperties(prefix = "gradeops.email")` — the prefix is unchanged. `GradeOpsWebProperties` uses `@ConfigurationProperties(prefix = "gradeops.web")` — unchanged. Note that `FirebaseTestConfig` in `src/test/` (used by many integration tests) is NOT moved by this task — it is a test-only helper. It imports `FirebaseConfig`, so its import must be updated here.
- **Affected files / components:**
  - 4 files MOVED into `src/main/java/cl/gradeops/ai/api/shared/infrastructure/config/`:
    - `FirebaseConfig.java`
    - `JacksonConfig.java`
    - `GradeOpsEmailProperties.java`
    - `GradeOpsWebProperties.java`
  - 4 original files in `config/` ← DELETE
  - Files with `import cl.gradeops.ai.api.config.*` that must be updated (7 files):
    - `src/main/java/cl/gradeops/ai/api/email/EmailService.java` (imports `GradeOpsEmailProperties`)
    - `src/main/java/cl/gradeops/ai/api/auth/PasswordResetService.java` (imports `GradeOpsEmailProperties`, `GradeOpsWebProperties`)
    - `src/test/java/cl/gradeops/ai/api/email/EmailServiceTest.java` (imports `GradeOpsEmailProperties`)
    - `src/test/java/cl/gradeops/ai/api/auth/PasswordResetServiceTest.java` (imports `GradeOpsEmailProperties`, `GradeOpsWebProperties`)
    - `src/test/java/cl/gradeops/ai/api/config/FirebaseTestConfig.java` ← UPDATE import of `FirebaseConfig`
    - `src/test/java/cl/gradeops/ai/api/auth/AuthControllerTest.java` (imports `FirebaseTestConfig` — no direct config import, no change needed)
    - `src/test/java/cl/gradeops/ai/api/auth/PasswordResetControllerTest.java` (same — no direct config import)
- **Interfaces / contracts:** `GradeOpsEmailProperties` and `GradeOpsWebProperties` getter/setter API unchanged. `FirebaseConfig` bean names unchanged.
- **Design notes:** `FirebaseTestConfig` lives in `src/test/java/cl/gradeops/ai/api/config/` — it is NOT moved (test helpers stay in their own test package). Only its import of `FirebaseConfig` is updated to the new main-source location.

---

## Implementation Steps

1. Create directory `src/main/java/cl/gradeops/ai/api/shared/infrastructure/config/` (may already exist from tasks 04/05).
2. For each of the 4 classes, create a new file in the new package with only the `package` declaration changed to `cl.gradeops.ai.api.shared.infrastructure.config`.
3. Delete the 4 original files from `src/main/java/cl/gradeops/ai/api/config/`.
4. In `src/main/java/cl/gradeops/ai/api/email/EmailService.java`:
   - Change: `import cl.gradeops.ai.api.config.GradeOpsEmailProperties;`
   - To: `import cl.gradeops.ai.api.shared.infrastructure.config.GradeOpsEmailProperties;`
5. In `src/main/java/cl/gradeops/ai/api/auth/PasswordResetService.java`:
   - Change both imports (`GradeOpsEmailProperties`, `GradeOpsWebProperties`) to new package.
6. In `src/test/java/cl/gradeops/ai/api/email/EmailServiceTest.java`:
   - Change: `import cl.gradeops.ai.api.config.GradeOpsEmailProperties;`
   - To: `import cl.gradeops.ai.api.shared.infrastructure.config.GradeOpsEmailProperties;`
7. In `src/test/java/cl/gradeops/ai/api/auth/PasswordResetServiceTest.java`:
   - Change both imports to new package.
8. In `src/test/java/cl/gradeops/ai/api/config/FirebaseTestConfig.java`:
   - Change: `import cl.gradeops.ai.api.config.FirebaseConfig;`
   - To: `import cl.gradeops.ai.api.shared.infrastructure.config.FirebaseConfig;`
9. Run `./mvnw compile -q` to confirm clean build.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | No `.java` files remain in `config/` main source | `find src/main/java/cl/gradeops/ai/api/config -name "*.java"` returns 0 |
| 2 | No imports of old `cl.gradeops.ai.api.config.*` in main sources | `grep -rn "import cl.gradeops.ai.api.config\." src/main/` returns 0 |
| 3 | All test config imports updated | `grep -rn "import cl.gradeops.ai.api.config\.Firebase\|import cl.gradeops.ai.api.config\.GradeOps" src/test/` returns 0 |
| 4 | Spring context loads `GradeOpsEmailProperties` correctly | `./mvnw compile -q` exits 0 |

---

## Done Criteria

- [ ] `src/main/java/cl/gradeops/ai/api/shared/infrastructure/config/` contains all 4 config classes
- [ ] `src/main/java/cl/gradeops/ai/api/config/` has no `.java` files
- [ ] `grep -rn "import cl.gradeops.ai.api.config\." src/main/` returns 0 results
- [ ] `src/test/java/cl/gradeops/ai/api/config/FirebaseTestConfig.java` imports `FirebaseConfig` from new location
- [ ] `PasswordResetService.java` and `PasswordResetServiceTest.java` import `GradeOpsEmailProperties`/`GradeOpsWebProperties` from new location
- [ ] `./mvnw compile -q` exits 0
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-01-shared-kernel.md)
