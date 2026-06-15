# ⚛️ TASK 02 — Firebase Admin SDK Bean in api/

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← scope file](../scope-06-teacher-account-provisioning.md)

---

## Objective

Create a Spring `@Configuration` class in `api/` that initializes a `FirebaseApp` bean using credentials from Secret Manager or the `GOOGLE_APPLICATION_CREDENTIALS` environment variable, making the Admin SDK available for injection.

---

## Technical Design

- **Approach:** Add the Firebase Admin SDK dependency to `api/pom.xml`. Create `FirebaseConfig.java` annotated `@Configuration` that calls `FirebaseApp.initializeApp(FirebaseOptions)` using `GoogleCredentials.getApplicationDefault()` (which reads `GOOGLE_APPLICATION_CREDENTIALS` or workload identity). Guard with `@ConditionalOnMissingBean` to avoid double-init in tests. Expose `FirebaseAuth.getInstance()` as a bean via `@Bean`.
- **Affected files / components:**
  - `api/pom.xml` — add `com.google.firebase:firebase-admin` dependency
  - `api/src/main/java/com/gradeops/api/config/FirebaseConfig.java` (new file)
  - `api/src/main/resources/application.yml` — no change needed (credentials via env)
  - `api/src/test/resources/application-test.yml` — set stub/mock strategy for tests
- **Interfaces / contracts:** `FirebaseAuth` bean injected into `ProvisionTeacherService` (task-03) and `FirebaseTokenFilter` (scope-01 task-01).
- **Design notes:** Use `FirebaseApp.getApps().isEmpty()` check before `initializeApp` to be idempotent. For local dev, point `GOOGLE_APPLICATION_CREDENTIALS` to a downloaded service-account JSON (not committed). For tests, either mock `FirebaseAuth` or use `@TestConfiguration` with a stub.

---

## Implementation Steps

1. Add to `api/pom.xml`:
   ```xml
   <dependency>
       <groupId>com.google.firebase</groupId>
       <artifactId>firebase-admin</artifactId>
       <version>9.3.0</version>
   </dependency>
   ```
2. Create `api/src/main/java/com/gradeops/api/config/FirebaseConfig.java`:
   - `@Configuration` class
   - `@Bean FirebaseApp firebaseApp()` — calls `FirebaseApp.initializeApp(FirebaseOptions.builder().setCredentials(GoogleCredentials.getApplicationDefault()).build())` if no apps initialized.
   - `@Bean FirebaseAuth firebaseAuth(FirebaseApp app)` — returns `FirebaseAuth.getInstance(app)`.
3. Create `api/src/test/java/com/gradeops/api/config/FirebaseTestConfig.java` with `@TestConfiguration` that provides a `@MockBean FirebaseAuth` to avoid real SDK calls in unit tests.
4. Run `./mvnw test` to verify no startup errors.

---

## Unit Tests

| # | Case | Expected result | Location |
|---|------|----------------|----------|
| 1 | Spring context loads with Firebase config | Application context starts without `BeanCreationException` | `./mvnw test -Dtest=FirebaseConfigTest` |
| 2 | `FirebaseAuth` bean is available | `@Autowired FirebaseAuth auth` is not null in a context test | `FirebaseConfigTest.java` |
| 3 | Test context uses mock | Unit tests that import `@MockBean FirebaseAuth` do not call the real SDK | Any service unit test |

---

## Done Criteria

- [ ] `firebase-admin` dependency added to `api/pom.xml`.
- [ ] `FirebaseConfig.java` exists and produces `FirebaseApp` and `FirebaseAuth` beans.
- [ ] Application context loads successfully in the test profile without real credentials.
- [ ] All unit tests listed above pass.
- [ ] No scope creep: the task still satisfies `[CHECK-ATOMICITY]`.

---

> [← scope file](../scope-06-teacher-account-provisioning.md)
