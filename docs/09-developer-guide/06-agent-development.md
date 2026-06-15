# Agent Development Guide

This guide covers how to implement AI agents in the `agents/` repository. It assumes familiarity with Spring Boot and the agent execution pattern described in [`docs/04-architecture/system-architecture.md`](../04-architecture/system-architecture.md).

> **Status:** The `agents/` repository is scaffolded (the directory exists with a README). No agent code has been implemented yet. This guide documents the target implementation pattern for Epic 02 and beyond.

---

## The agent runtime

The `agents/` repo is a separate Spring Boot 4 + Java 21 application from `api/`. It is called by the `api/` service via internal HTTP. It is never exposed to the public internet.

In production (Cloud Run), the `api/` service calls `agents/` using service-to-service OIDC authentication — Cloud Run injects an identity token automatically, and the agents service verifies it. In local development, no auth token is required (plain HTTP between localhost processes).

The base Java package for all agent code is `cl.gradeops.ai.agents`.

---

## The seven-step agent pattern

Every agent in GradeOps AI follows the same execution sequence without exception. The steps are enforced by code structure, not by convention alone:

1. **Validate command** — check that all required inputs are present and internally consistent. Throw a descriptive exception early if the command is invalid.
2. **Load data** — fetch any domain data needed from the API or passed in the command (assessment brief, approved rubric, submission content, question bank context).
3. **Build envelope** — assemble the full prompt context as a structured object. Avoid PII in the envelope; reference IDs rather than names or emails where possible.
4. **Call Gemini** — use Spring AI's `ChatClient` to invoke the model. Pass the formatted prompt from the StringTemplate.
5. **Validate structured output** — parse the JSON response using `BeanOutputConverter`. Validate that required fields are non-null, numeric values are within bounds, and the schema matches expectations.
6. **Log execution** — write an `AgentExecutionLog` record. This step is mandatory for every run, including failures.
7. **Return result** — return the `{Agent}Result` DTO to the calling `api/` service.

---

## Naming conventions

| Artifact | Pattern | Example |
|----------|---------|---------|
| Command DTO | `{AgentName}Command` | `RubricCommand` |
| Result DTO | `{AgentName}Result` | `RubricResult` |
| Service class | `{AgentName}AgentService` | `RubricAgentService` |
| Internal REST controller | `{AgentName}AgentController` | `RubricAgentController` |
| Prompt template file | `{agent-name}.st` | `rubric.st` |
| Java package | `cl.gradeops.ai.agents.{agentname}` | `cl.gradeops.ai.agents.rubric` |

---

## Prompt templates

Prompts are StringTemplate (`.st`) files located in:

```
agents/src/main/resources/prompts/
```

**Never inline prompt text in Java code.** Prompts are versioned content, not code. Keeping them in resource files makes them reviewable, diffable, and changeable without recompilation.

Template variables use `$variableName$` syntax. Example template fragment for the Assessment Agent:

```
You are an expert programming educator assistant for GradeOps AI.

Generate a structured assessment draft for the following context:

Learning goal: $learningGoal$
Topic: $topic$
Target level: $targetLevel$
Programming language: $programmingLanguage$
Estimated duration: $durationMinutes$ minutes
Teacher constraints: $teacherConstraints$

$formatInstructions$
```

Template variables to use by agent:

| Agent | Key template variables |
|-------|----------------------|
| Assessment | `$learningGoal$`, `$topic$`, `$targetLevel$`, `$programmingLanguage$`, `$durationMinutes$`, `$teacherConstraints$` |
| Rubric | `$assessmentTitle$`, `$learningObjectives$`, `$expectedEvidence$`, `$programmingLanguage$`, `$targetLevel$`, `$preferredScoringScale$` |
| Grading | `$submissionContent$`, `$rubricCriteria$`, `$assessmentInstructions$`, `$programmingLanguage$` |
| Feedback | `$criteriaResults$`, `$evidenceSummaries$`, `$studentIdentifier$` |

Load templates via Spring's `ResourceLoader`:

```java
@Value("classpath:prompts/assessment.st")
private Resource assessmentPromptTemplate;
```

---

## Example agent structure (AssessmentAgent)

The following skeleton shows all required classes for one agent. The AssessmentAgent is the first agent in the open assessment pipeline.

### Command record

```java
package cl.gradeops.ai.agents.assessment;

import java.util.List;

public record AssessmentCommand(
    String requestId,
    String teacherId,
    String assessmentId,
    String learningGoal,
    String topic,
    String targetLevel,
    String programmingLanguage,
    String assessmentType,
    int durationMinutes,
    List<String> teacherConstraints,
    String courseContext,          // nullable
    List<String> excludedTopics,  // nullable
    String teacherNotes           // nullable
) {}
```

### Result record

```java
package cl.gradeops.ai.agents.assessment;

import java.util.List;

public record AssessmentResult(
    String title,
    String summary,
    String context,
    List<String> learningObjectives,
    List<String> studentInstructions,
    List<String> deliverables,
    List<String> constraints,
    List<String> allowedResources,
    int estimatedDurationMinutes,
    String difficulty,
    List<String> expectedEvidence,
    RubricSeed rubricSeed,
    List<String> warnings,
    List<UncertaintyFlag> uncertaintyFlags
) {
    public record RubricSeed(
        List<String> suggestedCriteria,
        List<String> notesForRubricAgent
    ) {}

    public record UncertaintyFlag(
        String code,
        String message
    ) {}
}
```

### Service class

```java
package cl.gradeops.ai.agents.assessment;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

@Service
public class AssessmentAgentService {

    private final ChatClient chatClient;
    private final AgentExecutionLogRepository logRepository;

    public AssessmentAgentService(ChatClient chatClient,
                                   AgentExecutionLogRepository logRepository) {
        this.chatClient = chatClient;
        this.logRepository = logRepository;
    }

    public AssessmentResult execute(AssessmentCommand command) {
        // Step 1: Validate
        validateCommand(command);

        // Step 2: Build converter and format instructions
        BeanOutputConverter<AssessmentResult> converter =
            new BeanOutputConverter<>(AssessmentResult.class);
        String formatInstructions = converter.getFormat();

        // Step 3: Build prompt from template (load from resource)
        String prompt = buildPrompt(command, formatInstructions);

        // Step 4: Call Gemini
        long startMs = System.currentTimeMillis();
        String rawResponse = chatClient.prompt()
            .user(prompt)
            .call()
            .content();
        long latencyMs = System.currentTimeMillis() - startMs;

        // Step 5: Validate structured output
        AssessmentResult result = parseAndValidate(converter, rawResponse);

        // Step 6: Log execution
        logExecution(command, result, latencyMs);

        // Step 7: Return result
        return result;
    }

    private void validateCommand(AssessmentCommand command) {
        if (command.learningGoal() == null || command.learningGoal().isBlank()) {
            throw new IllegalArgumentException("learningGoal is required");
        }
        if (command.durationMinutes() < 10 || command.durationMinutes() > 480) {
            throw new IllegalArgumentException("durationMinutes must be between 10 and 480");
        }
        // Add further validations per the assessment-agent.md spec
    }

    private AssessmentResult parseAndValidate(BeanOutputConverter<AssessmentResult> converter,
                                               String rawResponse) {
        try {
            AssessmentResult result = converter.convert(rawResponse);
            if (result == null || result.title() == null) {
                throw new AgentOutputValidationException("OUTPUT_VALIDATION_FAILED",
                    "Required field 'title' is missing from Gemini response");
            }
            return result;
        } catch (Exception e) {
            throw new AgentOutputValidationException("OUTPUT_VALIDATION_FAILED", e.getMessage());
        }
    }
}
```

### Controller (internal REST endpoint)

```java
package cl.gradeops.ai.agents.assessment;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/agents/assessment")
public class AssessmentAgentController {

    private final AssessmentAgentService service;

    public AssessmentAgentController(AssessmentAgentService service) {
        this.service = service;
    }

    @PostMapping("/generate")
    public ResponseEntity<AssessmentResult> generate(@RequestBody AssessmentCommand command) {
        return ResponseEntity.ok(service.execute(command));
    }
}
```

---

## Spring AI configuration

Configure Spring AI for Vertex AI Gemini in `application.yml`:

```yaml
spring:
  ai:
    vertex:
      ai:
        gemini:
          project-id: ${GCP_PROJECT_ID}
          location: ${GCP_REGION:us-central1}
          chat:
            options:
              model: gemini-2.0-flash
```

The `ChatClient` bean is created automatically by the Spring AI auto-configuration. Inject it into your service:

```java
@Service
public class AssessmentAgentService {
    private final ChatClient chatClient;

    public AssessmentAgentService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }
}
```

---

## Structured output with BeanOutputConverter

Spring AI's `BeanOutputConverter` generates a JSON schema from a Java record and instructs Gemini to return output that matches it. The schema instructions are injected into the prompt as part of `$formatInstructions$`.

```java
BeanOutputConverter<AssessmentResult> converter =
    new BeanOutputConverter<>(AssessmentResult.class);

// Get the format instructions to include in the prompt
String formatInstructions = converter.getFormat();

// After calling Gemini, parse the response
AssessmentResult result = converter.convert(rawResponse);
```

The Java record must use types that map cleanly to JSON: `String`, `Integer`, `Boolean`, `List<T>`, and nested records. Avoid using `Optional` — use nullable fields instead.

---

## Output validation rules

After parsing the structured output, apply these checks before proceeding to the log step:

| Check | Action on failure |
|-------|------------------|
| Required fields are non-null | Throw `AgentOutputValidationException` with code `OUTPUT_VALIDATION_FAILED` |
| Numeric bounds (scores 0–100, duration > 0) | Throw `AgentOutputValidationException` |
| List fields have at least the minimum expected items | Throw `AgentOutputValidationException` |
| JSON schema match (handled by `BeanOutputConverter`) | `converter.convert()` throws on mismatch |

When validation fails:
- Set `AgentRunStatus = FAILED` in the execution log
- Include the error message in the log's `error_message` field (keep it brief — no raw prompt content)
- Throw the exception so the `api/` caller receives an error response and can retry or surface it to the teacher

---

## AgentExecutionLog — what to log

Write a log record on every agent run, including runs that fail. The log is the primary evidence layer.

| Field | Value to provide |
|-------|----------------|
| `id` | Generated UUID |
| `organization_id` | From the command's teacher context |
| `assessment_id` | From the command, if available |
| `student_submission_id` | From the command for grading runs; null otherwise |
| `agent_name` | Enum constant, e.g., `AgentName.ASSESSMENT` |
| `operation` | Enum constant, e.g., `AgentOperation.GENERATE_ASSESSMENT` |
| `model` | The Gemini model string, e.g., `gemini-2.0-flash` |
| `status` | `SUCCEEDED` or `FAILED` |
| `input_summary` | Brief human-readable description of the inputs — no raw submission content |
| `output_summary` | Brief description of the output — e.g., "Assessment draft with 3 objectives and 2 tasks" |
| `input_tokens` | Token count from the model response metadata if available |
| `output_tokens` | Token count from the model response metadata if available |
| `estimated_cost_usd` | Calculated from token counts using known Gemini pricing |
| `latency_ms` | Wall-clock time for the Gemini call |
| `uncertainty_flags_json` | JSON array of any `UncertaintyFlag` values in the result |
| `error_message` | Redacted error message if status is `FAILED`; null otherwise |
| `approval_state` | `PENDING` for all high-impact outputs (grading, feedback, rubric, report) |
| `created_at` | Current UTC timestamp |

---

## Security rules for agents

- **Never log raw submission content.** Log the `submissionId` reference. If a brief content summary is needed for debugging, keep it to 50–100 characters and redact student identifiers.
- **Never log teacher PII** beyond what is in the assessment brief (e.g., no email addresses, no names in the log text fields).
- **Always log token cost and latency.** These fields are required for billing evidence and unit economics tracking.
- **No secrets in prompts.** Never include API keys, database credentials, or internal system URLs in prompt templates.
- In Cloud Run production, the service account attached to the agents service provides Vertex AI access via ADC (Application Default Credentials). There is no need to set `GOOGLE_APPLICATION_CREDENTIALS` explicitly on Cloud Run.

---

## Local development with the Gemini API key

For local development, Vertex AI requires GCP authentication which can be complex to configure. Use the Gemini API key (simpler HTTP-based auth) instead.

Add this to `agents/src/main/resources/application-local.yml`:

```yaml
spring:
  ai:
    google:
      api-key: ${GEMINI_API_KEY}
      gemini:
        chat:
          options:
            model: gemini-2.0-flash
```

Store the key in `application-local.yml`. This file is gitignored and must never be committed.

To start the agents service locally with this profile:

```bash
cd agents/
./mvnw spring-boot:run -Dspring.profiles.active=local
```

---

## Thirteen agents reference

The complete agent set across both assessment modes:

**Open assessment (8 agents):**
1. `assessment` — generates assessment draft from learning goal
2. `rubric` — creates and validates scoring rubric
3. `grading` — analyzes submissions against approved rubric
4. `feedback` — drafts student-facing feedback
5. `learning_gap` — detects cohort gaps from graded submissions
6. `recovery` — suggests remedial activities per gap
7. `teacher_report` — summarizes the assessment cycle
8. `ops_evidence` — records usage, cost, and business evidence

**Closed assessment (5 agents):**
9. `question_generation` — generates TF/SC/MC questions with alternatives and answer key
10. `distractor_quality` — evaluates and flags weak or biased distractors
11. `ambiguity_review` — detects double-valid answers and interpretation problems
12. `assessment_assembly` — composes a balanced assessment from approved bank questions
13. `item_analytics` — analyzes post-assessment item performance

For detailed input/output contracts and quality rules for each agent, see the corresponding files in [`docs/03-ai-agents/`](../03-ai-agents/).

---

<!-- nav -->

← [05-database-guide.md](05-database-guide.md) | [↑ Top](#agent-development-guide) | [Web Development →](07-web-development.md)
