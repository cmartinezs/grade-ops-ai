# Testing Guide

This guide covers the testing patterns, tooling, and conventions used in the `api/` and `web/` repositories.

---

## API tests (Spring Boot + JUnit 5 + Mockito)

### Three test styles

#### 1. `@SpringBootTest` + `@AutoConfigureMockMvc` — integration tests

Used for endpoint tests that need the full Spring context: security filter chain, controller, service, and repository are all wired together.

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(FirebaseTestConfig.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired FirebaseAuth firebaseAuth;
    @Autowired TeacherRepository teacherRepository;

    @BeforeEach
    void setUp() {
        teacherRepository.deleteAll();
        reset(firebaseAuth);
    }

    @Test
    void valid_token_creates_teacher_and_returns_201() throws Exception {
        FirebaseToken mockToken = mock(FirebaseToken.class);
        when(mockToken.getUid()).thenReturn("uid-abc");
        when(mockToken.getEmail()).thenReturn("teacher@school.com");
        when(mockToken.getName()).thenReturn("Grace Hopper");
        when(firebaseAuth.verifyIdToken("valid-token")).thenReturn(mockToken);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"idToken": "valid-token", "name": "Grace Hopper"}
                        """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.firebaseUid").value("uid-abc"));

        assertThat(teacherRepository.existsByEmail("teacher@school.com")).isTrue();
    }
}
```

Annotations explained:
- `@ActiveProfiles("test")` — activates `application-test.yml`. This profile disables Flyway and uses an H2 in-memory database with `ddl-auto: create-drop`.
- `@Import(FirebaseTestConfig.class)` — registers a Mockito mock for `FirebaseAuth` as the primary bean, overriding the real Firebase Admin SDK initialization that requires valid credentials.

#### 2. `@WebMvcTest` — controller slice tests

Used when you want to test a controller in isolation without starting the full Spring context. Only the web layer is loaded (controller, filter chain, message converters). All service dependencies must be provided as `@MockBean`.

```java
@WebMvcTest(AssessmentController.class)
class AssessmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssessmentService assessmentService;

    @Test
    void getAssessments_returns_200_with_empty_list() throws Exception {
        when(assessmentService.findByTeacher("uid-123")).thenReturn(List.of());

        mockMvc.perform(get("/api/assessments")
                .header("Authorization", "Bearer valid-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }
}
```

`@WebMvcTest` is faster than `@SpringBootTest` because it does not load the JPA, Flyway, or other non-web beans. Use it for testing HTTP status codes, request/response mapping, and validation errors.

#### 3. Unit tests — pure logic

Used for services, validators, and any logic that does not require Spring. No Spring context is loaded.

```java
class OwnershipVerifierTest {

    private final OwnershipVerifier verifier = new OwnershipVerifier();

    @Test
    void matching_uids_does_not_throw() {
        assertDoesNotThrow(() -> verifier.verify("uid-123", "uid-123"));
    }

    @Test
    void different_uids_throw_resource_not_found() {
        assertThrows(ResourceNotFoundException.class,
            () -> verifier.verify("uid-123", "uid-999"));
    }
}
```

Unit tests are the fastest category. Write unit tests for all business logic that can be exercised without HTTP or database.

---

### FirebaseTestConfig

`FirebaseTestConfig` is at `cl.gradeops.ai.api.config.FirebaseTestConfig`. It is a `@TestConfiguration` class that registers a Mockito mock for `FirebaseAuth` as the `@Primary` bean:

```java
@TestConfiguration
public class FirebaseTestConfig {

    @Bean
    @Primary
    public FirebaseAuth firebaseAuth() {
        return mock(FirebaseAuth.class);
    }
}
```

Because it is `@Primary`, Spring uses this mock instead of the real `FirebaseAuth` bean that would be created by the `FirebaseApp.initializeApp()` call in the main configuration. Without this config, the test context startup would fail because there are no Firebase Admin credentials in the test environment.

Import it into any test class that needs Firebase verification to be stubbable:

```java
@Import(FirebaseTestConfig.class)
class YourControllerTest { ... }
```

---

### How to stub FirebaseAuth in tests

The following pattern covers the three scenarios you will encounter:

**Stubbing `verifyIdToken` for a registration or unverified call:**

```java
FirebaseToken mockToken = mock(FirebaseToken.class);
when(mockToken.getUid()).thenReturn("uid-123");
when(mockToken.getEmail()).thenReturn("teacher@example.com");
when(mockToken.getName()).thenReturn("Ada Lovelace");
when(firebaseAuth.verifyIdToken("test-token")).thenReturn(mockToken);
```

**Stubbing `verifyIdToken` with email-verified check (sign-out and protected endpoints):**

```java
when(mockToken.isEmailVerified()).thenReturn(true);
when(firebaseAuth.verifyIdToken("test-token", true)).thenReturn(mockToken);
```

Note: `verifyIdToken(token)` and `verifyIdToken(token, checkRevoked)` are two different overloads. Stub the right one based on which path you are testing.

**Stubbing a token verification failure:**

```java
when(firebaseAuth.verifyIdToken(anyString()))
    .thenThrow(mock(FirebaseAuthException.class));
```

The test in `AuthControllerTest` verifies the `401` response:

```java
mockMvc.perform(post("/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
                {"idToken": "bad-token", "name": "Nobody"}
                """))
    .andExpect(status().isUnauthorized())
    .andExpect(jsonPath("$.error").value("INVALID_TOKEN"));
```

---

### Running API tests

```bash
cd api/

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=AuthControllerTest

# Run a single test method
./mvnw test -Dtest=AuthControllerTest#valid_token_creates_teacher_and_returns_201
```

Test method names use underscores (`snake_case`) to improve readability in the Maven output. This is a project convention — follow it for new tests.

---

## Web tests (Jest + React Testing Library)

### Test file location

Tests are co-located in a `__tests__/` directory beside the component under test:

```
src/components/auth/
  AuthGuard.tsx
  __tests__/
    AuthGuard.test.tsx
```

### Firebase module mock

All `firebase/auth` imports in test files are intercepted by the module mapper in `jest.config.ts`:

```typescript
moduleNameMapper: {
  "^firebase/(.*)$": "<rootDir>/src/test/__mocks__/firebase/$1",
}
```

The mock file at `src/test/__mocks__/firebase/auth.ts` exports Jest mock functions for every Firebase Auth function used in the codebase:

```typescript
export const createUserWithEmailAndPassword = jest.fn();
export const signInWithEmailAndPassword = jest.fn();
export const sendEmailVerification = jest.fn();
export const signOut = jest.fn();
export const onAuthStateChanged = jest.fn();
export const getAuth = jest.fn(() => ({}));
```

Every function is a `jest.fn()` with no default implementation. Tests must configure the return value or behavior they need:

```typescript
import { onAuthStateChanged } from "firebase/auth";

// Simulate a verified user
(onAuthStateChanged as jest.Mock).mockImplementation((_auth, callback) => {
  callback({ uid: "uid-123", emailVerified: true });
  return jest.fn(); // return the unsubscribe function
});

// Simulate a null user (logged out)
(onAuthStateChanged as jest.Mock).mockImplementation((_auth, callback) => {
  callback(null);
  return jest.fn();
});
```

### jest.config.ts

```typescript
import type { Config } from "jest";

const config: Config = {
  testEnvironment: "jsdom",
  transform: {
    "^.+\\.(ts|tsx)$": ["ts-jest", { tsconfig: { jsx: "react-jsx" } }],
  },
  moduleNameMapper: {
    "^@/(.*)$": "<rootDir>/src/$1",
    "^firebase/(.*)$": "<rootDir>/src/test/__mocks__/firebase/$1",
  },
  setupFilesAfterFramework: ["@testing-library/jest-dom"],
};

export default config;
```

`testEnvironment: "jsdom"` simulates a browser DOM environment. All tests run in this environment — there is no separate server-side test environment for Next.js server components.

### Running web tests

```bash
cd web/

# All tests in watch mode (interactive)
npm test

# Single run — use this in CI
npm test -- --watchAll=false

# Run a single test file
npm test -- --watchAll=false AuthGuard.test.tsx
```

### Test patterns

**Async render with `act`:**

```typescript
import { act, render, screen } from "@testing-library/react";

it("redirects to login when user is null", async () => {
  (onAuthStateChanged as jest.Mock).mockImplementation((_auth, cb) => {
    cb(null);
    return jest.fn();
  });

  await act(async () => {
    render(<AuthGuard><div>Protected</div></AuthGuard>);
  });

  expect(mockReplace).toHaveBeenCalledWith("/login");
});
```

**Query preference:** Use semantic queries over `getByTestId`. In order of preference:
1. `getByRole` — query by ARIA role (button, heading, textbox)
2. `getByLabelText` — query form inputs by their label
3. `getByText` — query by visible text content
4. `getByTestId` — last resort only, when there is no other accessible query

**Standard mock setup block** (required in most component tests):

```typescript
const mockPush = jest.fn();
const mockReplace = jest.fn();

jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: mockPush, replace: mockReplace }),
  useSearchParams: () => ({ get: jest.fn().mockReturnValue(null) }),
  usePathname: () => "/",
}));

jest.mock("@/lib/firebase/client", () => ({
  auth: { currentUser: null },
}));
```

Put this block at the top of the test file, before the `describe` or `it` blocks.

---

## Testing strategy

### What to test

| Layer | What to test |
|-------|-------------|
| API controllers | HTTP status codes, response body structure, auth rejection (401/403), validation errors (400) |
| API services | Business logic, error conditions, ownership rules, state transitions |
| API repositories | Only if you add custom queries — use `@DataJpaTest` for repository slice tests |
| React components | Rendering output, user interactions (click, type, submit), conditional rendering based on state |
| Custom hooks | State transitions, side effects, error states |

### What not to test

- Tailwind CSS class names — test behavior, not style. A button with `bg-blue-600` that turns into `bg-blue-700` on hover is not a test concern.
- Firebase SDK internals — mock them. The SDK is tested by Firebase.
- Spring Security internals — test that your endpoints return the right status, not how Spring Security's filter chain works internally.
- Trivial getters and setters — no value in testing `TeacherEntity.getEmail()` in isolation.

---

<!-- nav -->

← [07-web-development.md](07-web-development.md) | [↑ Top](#testing-guide) | [Deployment Guide →](09-deployment-guide.md)
