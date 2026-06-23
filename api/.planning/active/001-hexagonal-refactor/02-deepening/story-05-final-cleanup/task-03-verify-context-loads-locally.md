# ⚛️ TASK 03 — Verify Spring context loads cleanly with local profile

> **Status:** TODO
> **Workflow:** TEST
> **Depends On:** task-01, task-02
> [← story file](../story-05-final-cleanup.md)

---

## Objective

Start the application with `./mvnw spring-boot:run -Dspring.profiles.active=local` and confirm the Spring context loads without any `NoSuchBeanDefinitionException`, `UnsatisfiedDependencyException`, or other startup errors. This validates that all `@Configuration`/`@Bean` wiring across the 4 bounded contexts is complete and correct.

---

## Technical Design

- **Approach:** The hexagonal refactor replaced all `@Service`, `@Component`, and `@Repository` stereotypes (except `@RestController`) with explicit `@Bean` declarations in `@Configuration` classes. If any `@Bean` method is missing a dependency or references a wrong type, the context will fail to start — and `./mvnw test` alone may not catch it (since unit tests mock dependencies). Running the application locally is the authoritative check for bean wiring.
- **Config classes to verify are present and correct:**

| Bounded context | Config class | Beans declared |
|----------------|-------------|----------------|
| `shared` | `FirebaseConfig`, `JacksonConfig`, `GradeOpsEmailProperties`, `GradeOpsWebProperties`, `SecurityConfig` (security beans) | Firebase, Jackson, email props, security filters |
| `auth` | `AuthConfig` | `FirebaseAuthAdapter`, `PasswordResetCodePersistenceAdapter`, `PasswordResetCodePersistenceMapper`, `ThymeleafEmailNotificationAdapter`, `RevokeRefreshTokensHandler`, `SignOutHandler`, `RegisterHandler`, `IssuePasswordResetCodeHandler`, `SendPasswordResetEmailOrchestrator`, `ResetPasswordOrchestrator` |
| `teacher` | `TeacherConfig` | `TeacherPersistenceMapper`, `TeacherPersistenceAdapter`, `ProvisionTeacherHandler`, `UpdatePilotFlagsHandler` |
| `assessment` | `AssessmentConfig` | `StubAssessmentPersistenceAdapter`, `ListAssessmentsHandler` |

- **Common failure modes to watch for:**
  - Missing `@Bean` for a class that is `@RequiredArgsConstructor` without `@Service` — Spring can't auto-detect it
  - Circular dependency between two `@Configuration` classes
  - `@Value` property not present in `application-local.yml` (e.g., `gradeops.web.base-url`)
  - `TeacherJpaRepository` or `PasswordResetCodeJpaRepository` not found (Spring Data auto-detects package-private interfaces only if they're under the component-scan root)

---

## Implementation Steps

1. Ensure `application-local.yml` (or `application.yml` + local overrides) has all required properties:
   - `gradeops.web.base-url`
   - `gradeops.email.reset-password.ttl-minutes`
   - Firebase credentials (or `GOOGLE_APPLICATION_CREDENTIALS` env var)
   - Database URL (local PostgreSQL)
2. Run:
   ```bash
   ./mvnw spring-boot:run -Dspring.profiles.active=local
   ```
3. Watch logs until one of:
   - `Started GradeOpsApiApplication` — SUCCESS
   - `APPLICATION FAILED TO START` — FAILURE (read the exception)
4. If context fails to start:
   - `NoSuchBeanDefinitionException: No qualifying bean of type 'X'` → a `@Bean` declaration is missing in the relevant `@Configuration` class; add it.
   - `UnsatisfiedDependencyException` → a `@Bean` method has a parameter Spring can't satisfy; check the parameter type and whether the providing `@Bean` method exists.
   - `@Value` property missing → add the property to `application-local.yml`.
   - Spring Data repo not found → verify the repo interface is under the component-scan root (`cl.gradeops.ai.api`) and extends `JpaRepository`.
5. Fix and restart until `Started GradeOpsApiApplication` appears in the log.
6. Stop the application (Ctrl+C).
7. Make a quick smoke test request:
   ```bash
   curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/v1/assessments \
     -H "Authorization: Bearer invalid-token"
   ```
   Expected: `401` (security is up, returns unauthorized as expected with a bad token).

---

## Unit Tests

| # | Verification | Expected result |
|---|-------------|----------------|
| 1 | Application startup log | `Started GradeOpsApiApplication` present; no `APPLICATION FAILED TO START` |
| 2 | No `NoSuchBeanDefinitionException` in logs | 0 occurrences |
| 3 | No `UnsatisfiedDependencyException` in logs | 0 occurrences |
| 4 | Smoke test GET `/api/v1/assessments` without token | HTTP 401 |

---

## Done Criteria

- [ ] `./mvnw spring-boot:run -Dspring.profiles.active=local` starts successfully
- [ ] Log contains `Started GradeOpsApiApplication` and no bean-wiring errors
- [ ] Unauthenticated request to `/api/v1/assessments` returns 401 (security is active)
- [ ] Application stopped cleanly after verification
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-05-final-cleanup.md)
