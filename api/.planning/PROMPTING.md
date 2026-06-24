# 🎯 Prompting Guidelines

> [← planning/README.md](README.md)

Guidelines for writing effective prompts when using AI agents (Claude Code, etc.) in the context of this planning system.

---

## Foundational Principle

> **Give context → assign scope → specify output → define constraints.**

A good prompt does not just say what to do. It tells the AI:
- **why** it's needed (context from the planning)
- **what specific thing** must be produced (file, module, endpoint, config)
- **which constraints** must be respected (repo boundaries, naming conventions, architecture rules)
- **what success looks like** (done criteria)

---

## Prompt Structure Template

```
Context: [Brief description of the planning and current story]
Task: [Exactly what needs to be produced or modified]
Constraints:
  - [Constraint 1: e.g., "Agents do not persist domain entities directly"]
  - [Constraint 2: e.g., "Follow Spring Boot module structure in docs/04-architecture/repository-structure.md"]
  - [Constraint 3: e.g., "Update cross-references in related story files"]
Done when:
  - [Criterion 1]
  - [Criterion 2]
```

---

## General Rules

### 1. Reference the planning before executing
Every prompt should name the active planning and story it belongs to.

> ✅ *"As part of planning 019-web-scaffold, story-02-assessment-pages: create the assessment creation page…"*
>
> ❌ *"Create the assessment creation page."*

---

### 2. Reference the canonical docs before generating code
Before generating any implementation, confirm which docs/ document defines the contract:

> *"Refer to `docs/04-architecture/repository-structure.md` for the module structure. Refer to `docs/04-architecture/api-design.md` for the endpoint contract. Refer to `docs/04-architecture/data-model.md` for the entity schema."*

---

### 3. Specify the target file and location
Name the exact file path, module, and class where output goes.

> ✅ *"In `api/src/main/java/ai/gradeops/api/assessment/AssessmentController.java`, add endpoint `POST /api/v1/assessments/{id}/generate-draft`…"*
>
> ❌ *"Add the generate-draft endpoint."*

---

### 4. Include the done criteria explicitly
End the prompt with a short list of what must be true for the task to be considered complete.

---

### 5. Use workflow names as triggers
When the prompt should trigger a full workflow, name it explicitly:

> *"Execute GENERATE-DOCUMENT for `AssessmentService.java` using the assessment module contract in `docs/04-architecture/repository-structure.md`."*

---

### 6. Cascade prompts for cross-repo changes
If the task affects multiple repos (e.g., a new API endpoint also requires a web client update), split into individual story prompts per repo, or use `CASCADE-CHANGE` workflow explicitly.

---

### 7. For inconsistencies, use RECORD-INCONSISTENCY
If you discover a contradiction between a `docs/` decision and the implementation:
> *"Record an inconsistency: the data model in `docs/04-architecture/data-model.md` defines field X but the Flyway migration uses Y. Mark it as residual if not immediately resolvable."*

---

## Prompt Library (Reusable)

### Advance to next story

```
Context: Planning [NNN-name], current story is [story-NN], status DONE.
Task: Execute [NEXT-STORY] sub-workflow. Verify all story done criteria are met,
      update TRACEABILITY.md, and identify the next story to execute.
```

---

### Scaffold a new API module

```
Context: Planning [NNN-name], story [NN-name].
Task: Execute GENERATE-DOCUMENT workflow for [module-name] module in [api-directory/].
      Reference [docs/your-repository-structure-doc] for module structure.
      Reference [docs/your-api-design-doc] for endpoint contracts.
      Reference [docs/your-data-model-doc] for entity definitions.
Constraints:
  - Follow the package/directory convention: [your.base.package].[module-name]
  - Module must contain: [Entity], [Entity]Repository, [Entity]Service, [Entity]Controller
  - Controller routes must match the API design doc exactly
  - ID and timestamp conventions must match the data model doc
Done when:
  - Module structure exists at the correct path
  - Endpoints compile and return correct status codes
  - Database migration added for any new tables/columns
  - TRACEABILITY.md updated with new domain terms
```

---

### Implement an agent

```
Context: Planning [NNN-name], story [NN-name]. Working on [AgentName] agent in [agents-directory/].
Task: Execute GENERATE-DOCUMENT for [AgentName] agent.
      Reference [docs/your-agent-spec-doc] for the agent's input/output contract.
      Reference [docs/your-repository-structure-doc] for the agent directory structure.
Constraints:
  - Follow the package/directory convention: [your.base.package].[agent-name]
  - Must contain: [Agent]Command, [Agent]Result, [Agent]Service, [Agent]Controller
  - Prompt template must be versioned and stored separately from code
  - Agent must log every execution (success and failure)
  - Agent must NOT persist domain entities directly — return result to the caller
Done when:
  - All required files exist at the correct path
  - Prompt template file exists in the expected location
  - Execution logging is recorded for both success and failure cases
  - Agent endpoint responds on the expected path
```

---

### Scaffold a frontend page or component

```
Context: Planning [NNN-name], story [NN-name]. Working on [page/component] in [frontend-directory/].
Task: Execute GENERATE-DOCUMENT for [component or page name].
      Reference [docs/your-ux-doc] for UX intent and interaction flows.
      Reference [docs/your-repository-structure-doc] for routing and directory structure.
Constraints:
  - Follow the routing and component conventions of [your-frontend-framework]
  - Place pages/components in the correct directory following project conventions
  - API calls must go through the typed client — no direct fetch in components
  - No business logic in page files — delegate to components and hooks
Done when:
  - File exists at the correct path
  - Component renders without errors
  - API calls use the project's typed client
```

---

### Infrastructure change

```
Context: Planning [NNN-name], story [NN-name]. Working on infrastructure in [infra-directory/].
Task: Execute GENERATE-DOCUMENT for [IaC module or CI/CD workflow].
      Reference [docs/your-deployment-doc] for topology and environment conventions.
Constraints:
  - Target the [target-environment] environment
  - All secrets must go through the project's secret management solution — never hardcoded
  - Service-to-service auth must follow the project's authentication conventions
  - Internal services must NOT be publicly reachable
Done when:
  - IaC plan runs without errors in [target-environment]
  - Resource names follow the project naming convention
```

---

### Review coherence after changes

```
Context: Planning [NNN-name]. Story [NN-name] just completed [X].
Task: Execute REVIEW-COHERENCE workflow.
      Check cross-references from [list of related files or modules].
      Verify implementation matches the relevant docs/ contract.
Done when:
  - No broken references found
  - Implementation matches the relevant docs/ contracts
  - Terminology matches the canonical definitions in docs/
```

---

### Update traceability

```
Context: Planning [NNN-name]. New term/concept "[term]" was introduced.
Task: Execute UPDATE-TRACEABILITY workflow.
      Map "[term]" across the traceability matrix in TRACEABILITY.md.
      Mark which repo area codes (DO, WB, AP, AG, IN, W) are affected.
      If globally significant, update TRACEABILITY-GLOBAL.md.
```

---

### Audit a completed planning

```
Context: Planning [NNN-name] is about to be archived.
Task: Execute AUDIT-PLANNING workflow.
      Verify all stories are DONE, all tasks have outputs, traceability is updated,
      no open inconsistencies, no pending residuals.
      If all checks pass, move to `.planning/finished/`.
```

---

> [← planning/README.md](README.md)
