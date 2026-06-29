# ⚛️ TASK 08 — Create InternalTeacherController + convert controller tests to @WebMvcTest

> **Status:** TODO
> **Workflow:** REFACTOR+IMPLEMENT
> **Depends On:** task-03, task-05, task-06
> [← story file](../story-03-teacher-bounded-context.md)

---

## Objective

Create `InternalTeacherController` in `teacher.infrastructure.adapter.in.web` with 2 endpoints (`POST /internal/teachers`, `PATCH /internal/teachers/{uid}/flags`). Convert the existing `ProvisionTeacherControllerTest` and `PilotFlagControllerTest` from `@SpringBootTest` to `@WebMvcTest`. Delete all old `internal/teacher/` files (controller, services, DTOs, tests).

---

## Technical Design

- **Approach:** `InternalTeacherController` is `@RestController @RequestMapping("/internal/teachers") @RequiredArgsConstructor`. It injects 2 use-case interfaces and a `String webBaseUrl`. Because `@RestController` is the only permitted Spring stereotype and the controller IS the annotation, no `@Configuration` wiring is needed for the controller itself — Spring's component scan picks it up. However, `webBaseUrl` must be supplied via an explicit constructor with `@Value` (since the controller is component-scanned, not `@Bean`-declared, this is the one valid exception to the "no `@Value` in class body" rule — here `@Value` goes on the constructor parameter). `POST /internal/teachers` returns `201 Created` with a JSON body containing `inviteLink`. `PATCH /internal/teachers/{uid}/flags` returns `200 OK` with the updated flag snapshot.
- **Controller code:**
  ```java
  @RestController
  @RequestMapping("/internal/teachers")
  @RequiredArgsConstructor
  public class InternalTeacherController {

      private final ProvisionTeacherUseCase provisionTeacherUseCase;
      private final UpdatePilotFlagsUseCase updatePilotFlagsUseCase;
      private final String webBaseUrl;

      public InternalTeacherController(
              ProvisionTeacherUseCase provisionTeacherUseCase,
              UpdatePilotFlagsUseCase updatePilotFlagsUseCase,
              @Value("${gradeops.web.base-url}") String webBaseUrl) {
          this.provisionTeacherUseCase = provisionTeacherUseCase;
          this.updatePilotFlagsUseCase = updatePilotFlagsUseCase;
          this.webBaseUrl = webBaseUrl;
      }
  ```
  Note: because `@RequiredArgsConstructor` would also generate a constructor and conflict with the explicit one here, use `@Autowired` is NOT an option; instead drop `@RequiredArgsConstructor` and write the explicit constructor with `@Value` on the parameter directly (Lombok cannot handle `@Value` on parameters). The class does NOT use `@RequiredArgsConstructor` — it has an explicit all-args constructor.
- **Affected files / components (new):**
  - `src/main/java/cl/gradeops/ai/api/teacher/infrastructure/adapter/in/web/InternalTeacherController.java` ← NEW
  - `src/main/java/cl/gradeops/ai/api/teacher/infrastructure/adapter/in/web/request/ProvisionTeacherRequest.java` ← NEW (record)
  - `src/main/java/cl/gradeops/ai/api/teacher/infrastructure/adapter/in/web/request/UpdatePilotFlagsRequest.java` ← NEW (record)
  - `src/main/java/cl/gradeops/ai/api/teacher/infrastructure/adapter/in/web/response/ProvisionTeacherResponse.java` ← NEW (record)
  - `src/main/java/cl/gradeops/ai/api/teacher/infrastructure/adapter/in/web/response/UpdatePilotFlagsResponse.java` ← NEW (record)
  - `src/test/java/cl/gradeops/ai/api/teacher/infrastructure/adapter/in/web/InternalTeacherControllerTest.java` ← NEW (`@WebMvcTest`)
- **Affected files / components (deleted):**
  - `src/main/java/cl/gradeops/ai/api/internal/teacher/InternalTeacherController.java`
  - `src/main/java/cl/gradeops/ai/api/internal/teacher/ProvisionTeacherService.java`
  - `src/main/java/cl/gradeops/ai/api/internal/teacher/PilotFlagService.java`
  - All DTOs under `src/main/java/cl/gradeops/ai/api/internal/teacher/` (request/response records)
  - `src/test/java/cl/gradeops/ai/api/internal/teacher/ProvisionTeacherControllerTest.java`
  - `src/test/java/cl/gradeops/ai/api/internal/teacher/PilotFlagControllerTest.java`

---

## Implementation Steps

1. Create directory `src/main/java/cl/gradeops/ai/api/teacher/infrastructure/adapter/in/web/request/`.
2. Create `ProvisionTeacherRequest.java`:
   ```java
   package cl.gradeops.ai.api.teacher.infrastructure.adapter.in.web.request;

   public record ProvisionTeacherRequest(String firstName, String lastName, String email) {}
   ```
3. Create `UpdatePilotFlagsRequest.java`:
   ```java
   package cl.gradeops.ai.api.teacher.infrastructure.adapter.in.web.request;

   public record UpdatePilotFlagsRequest(
       String planType,
       Boolean relatedParty,
       String offerDetails,
       String evidenceLink,
       String setBy
   ) {}
   ```
4. Create directory `src/main/java/cl/gradeops/ai/api/teacher/infrastructure/adapter/in/web/response/`.
5. Create `ProvisionTeacherResponse.java`:
   ```java
   package cl.gradeops.ai.api.teacher.infrastructure.adapter.in.web.response;

   public record ProvisionTeacherResponse(String firebaseUid, String inviteLink) {}
   ```
6. Create `UpdatePilotFlagsResponse.java`:
   ```java
   package cl.gradeops.ai.api.teacher.infrastructure.adapter.in.web.response;

   public record UpdatePilotFlagsResponse(
       String firebaseUid,
       String planType,
       Boolean relatedParty,
       String flagSetAt
   ) {}
   ```
7. Create `InternalTeacherController.java`:
   ```java
   package cl.gradeops.ai.api.teacher.infrastructure.adapter.in.web;

   import cl.gradeops.ai.api.teacher.application.command.ProvisionTeacherCommand;
   import cl.gradeops.ai.api.teacher.application.command.UpdatePilotFlagsCommand;
   import cl.gradeops.ai.api.teacher.application.port.in.ProvisionTeacherUseCase;
   import cl.gradeops.ai.api.teacher.application.port.in.UpdatePilotFlagsUseCase;
   import cl.gradeops.ai.api.teacher.infrastructure.adapter.in.web.request.ProvisionTeacherRequest;
   import cl.gradeops.ai.api.teacher.infrastructure.adapter.in.web.request.UpdatePilotFlagsRequest;
   import cl.gradeops.ai.api.teacher.infrastructure.adapter.in.web.response.ProvisionTeacherResponse;
   import cl.gradeops.ai.api.teacher.infrastructure.adapter.in.web.response.UpdatePilotFlagsResponse;
   import org.springframework.beans.factory.annotation.Value;
   import org.springframework.http.HttpStatus;
   import org.springframework.web.bind.annotation.*;

   @RestController
   @RequestMapping("/internal/teachers")
   public class InternalTeacherController {

       private final ProvisionTeacherUseCase provisionTeacherUseCase;
       private final UpdatePilotFlagsUseCase updatePilotFlagsUseCase;
       private final String webBaseUrl;

       public InternalTeacherController(
               ProvisionTeacherUseCase provisionTeacherUseCase,
               UpdatePilotFlagsUseCase updatePilotFlagsUseCase,
               @Value("${gradeops.web.base-url}") String webBaseUrl) {
           this.provisionTeacherUseCase = provisionTeacherUseCase;
           this.updatePilotFlagsUseCase = updatePilotFlagsUseCase;
           this.webBaseUrl = webBaseUrl;
       }

       @PostMapping
       @ResponseStatus(HttpStatus.CREATED)
       public ProvisionTeacherResponse provision(@RequestBody ProvisionTeacherRequest request) {
           var result = provisionTeacherUseCase.execute(
               ProvisionTeacherCommand.builder()
                   .firstName(request.firstName())
                   .lastName(request.lastName())
                   .email(request.email())
                   .build()
           );
           String inviteLink = webBaseUrl + "/reset-password?code=" + result.rawCode();
           return new ProvisionTeacherResponse(result.firebaseUid(), inviteLink);
       }

       @PatchMapping("/{uid}/flags")
       public UpdatePilotFlagsResponse updateFlags(
               @PathVariable String uid,
               @RequestBody UpdatePilotFlagsRequest request) {
           var result = updatePilotFlagsUseCase.execute(
               UpdatePilotFlagsCommand.builder()
                   .uid(uid)
                   .planType(request.planType())
                   .relatedParty(request.relatedParty())
                   .offerDetails(request.offerDetails())
                   .evidenceLink(request.evidenceLink())
                   .setBy(request.setBy())
                   .build()
           );
           return new UpdatePilotFlagsResponse(
               result.firebaseUid(), result.planType(),
               result.relatedParty(), result.flagSetAt()
           );
       }
   }
   ```
8. Create test file `src/test/java/cl/gradeops/ai/api/teacher/infrastructure/adapter/in/web/InternalTeacherControllerTest.java`:
   - `@WebMvcTest(InternalTeacherController.class)`.
   - `@MockBean ProvisionTeacherUseCase provisionTeacherUseCase`.
   - `@MockBean UpdatePilotFlagsUseCase updatePilotFlagsUseCase`.
   - `@Value("${gradeops.web.base-url:http://localhost:3000}") String webBaseUrl` — or set via `@TestPropertySource`.
   - `@Autowired MockMvc mockMvc`.
   - **Test 1 — provision happy path**: POST `/internal/teachers` with `{"firstName":"Ana","lastName":"Soto","email":"a@x.com"}` → stub `provisionTeacherUseCase.execute(any())` returns `ProvisionTeacherResult.builder().firebaseUid("uid-1").rawCode("code-abc").build()`; assert `201`, `body.firebaseUid = "uid-1"`, `body.inviteLink` contains `"/reset-password?code=code-abc"`.
   - **Test 2 — provision duplicate email**: stub throws `DuplicateEmailException("a@x.com")` → assert `409` (or whatever `GlobalExceptionHandler` maps it to; verify with existing test).
   - **Test 3 — updateFlags happy path**: PATCH `/internal/teachers/uid-1/flags` with `{"planType":"pilot","relatedParty":true,"setBy":"admin"}` → stub returns `UpdatePilotFlagsResult`; assert `200` and `body.planType = "pilot"`.
   - **Test 4 — updateFlags not found**: stub throws `TeacherNotFoundException("uid-x")` → assert `404`.
9. Delete old files (implementation + tests):
   - `src/main/java/cl/gradeops/ai/api/internal/teacher/` (entire directory)
   - `src/test/java/cl/gradeops/ai/api/internal/teacher/` (entire directory)
10. Run `./mvnw test -Dtest=InternalTeacherControllerTest -q`.

---

## Unit Tests

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | `POST /internal/teachers` → 201 + `inviteLink` with rawCode | `mockMvc.perform(post(...))` asserts status + body |
| 2 | `POST` duplicate email → 409 | Stub throws `DuplicateEmailException`; assert status |
| 3 | `PATCH /internal/teachers/{uid}/flags` → 200 + updated fields | Assert `planType`, `relatedParty` in response |
| 4 | `PATCH` unknown UID → 404 | Stub throws `TeacherNotFoundException`; assert status |
| 5 | Old `internal/teacher/` package gone | `find src -path "*/internal/teacher*" -type f` → 0 results |
| 6 | `inviteLink = webBaseUrl + /reset-password?code= + rawCode` | Assert string pattern in test 1 |

---

## Done Criteria

- [ ] `InternalTeacherController` exists in `teacher/infrastructure/adapter/in/web/`; annotated `@RestController` only
- [ ] Controller has explicit constructor with `@Value("${gradeops.web.base-url}")` on the `webBaseUrl` parameter
- [ ] `POST /internal/teachers` returns `201`; `PATCH /internal/teachers/{uid}/flags` returns `200`
- [ ] `inviteLink` is built from `webBaseUrl + "/reset-password?code=" + result.rawCode()`
- [ ] All 4 DTO records exist in `request/` and `response/` sub-packages
- [ ] `InternalTeacherControllerTest` uses `@WebMvcTest` — no `@SpringBootTest`, no database
- [ ] All 4 test cases pass
- [ ] All old `internal/teacher/` source and test files deleted
- [ ] `./mvnw test -Dtest=InternalTeacherControllerTest -q` exits 0
- [ ] No scope creep: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-03-teacher-bounded-context.md)
