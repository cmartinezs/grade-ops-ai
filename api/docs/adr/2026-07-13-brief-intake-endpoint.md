# ADR: Brief intake endpoint — validation status code

**Date:** 2026-07-13
**Status:** Accepted
**Planning:** 003-assessment-creation / story-01-assessment-creation-persistence / task-06-brief-intake-endpoint

## Context

The task's original design specified that blank/invalid fields on `POST /api/v1/assessments` should return `400`. Before implementing, a reality check against the existing shared `GlobalExceptionHandler` (`shared/infrastructure/adapter/in/web/GlobalExceptionHandler.java`) showed it already maps every `MethodArgumentNotValidException` — exactly what a failed `@Valid` on a request body produces — to `422 UNPROCESSABLE_CONTENT`, applied uniformly to every validated endpoint in the app.

## Decision

This task follows that existing convention rather than adding a one-off `400` handler for just this endpoint, which would make validation-error status codes inconsistent across the API. Validation (`@NotBlank` etc.) happens at the request-DTO level via `spring-boot-starter-validation`, rejected before the handler runs, with the response code left to the shared, app-wide exception handler.

## Consequences

`CreateAssessmentBriefRequest` validation failures return `422`, not `400`. The task's own Verification table and Done Criteria (originally written against `400`) were corrected in place to state `422` and explain why, rather than silently diverging from what was actually built. Any future endpoint added to this codebase inherits the same `422`-for-validation-failure behavior automatically, with no extra work required.

## Alternatives Considered

A dedicated `400` exception handler scoped to this endpoint's `MethodArgumentNotValidException` was considered (to match the task's original literal spec) but discarded, since it would only apply to this one endpoint and make the API's validation-error status code inconsistent depending on which endpoint a client called.
