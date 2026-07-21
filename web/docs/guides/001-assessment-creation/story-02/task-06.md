# Connect Real Api Intake Screen

**Source:** task-06 | **Area:** unknown | **Date:** 2026-07-17

## What it does
The Intake screen calls the real `api/` via `submitAssessmentBrief`, redirects to the Draft Builder screen with the real `assessmentId`, and surfaces the real error surface (validation 422, agent-rejected 422, agent-down 502/503, not-found 404, unexpected 500 — per `task-02`'s traced evidence, not the generic 422/500) to the teacher — the fake `setTimeout` submit from `task-04` is fully removed.

---

## How to use it
- Replace `useIntakeAssessmentPage`'s fake submit with a call to `submitAssessmentBrief(brief)`.
- On success, use `useRouter().push()` to navigate to `/assessments/${assessmentId}/draft` (the Draft Builder screen route from `task-08`).
- On failure, branch on the response shape/status per `task-02`'s traced evidence, not a flat 422/500 switch: `List<FieldErrorResponse>` (422 from `POST /assessments`) → map to `fieldErrors` (`Partial<Record<keyof BriefFormValues, string>>`) and return it from the hook exactly as `task-04`'s fake `trigger-field-error` case did — it flows through the already-built `BriefForm.fieldErrors` → `DynamicForm.externalErrors` path, no new UI mechanism; `ApiErrorResponse{error:"AGENT_CALL_FAILED", message:"AGENT_REJECTED"}` → business-rejection message via `serverError` (banner); `ApiErrorResponse{error:"AGENT_CALL_FAILED", message:"AGENT_ERROR"|"UNREACHABLE"}` (502/503) → service-unavailable message via `serverError`, distinct wording from the rejection case; anything else (400/404/500) → generic retry message via `serverError`. All translated to Spanish per `15-backend-frontend-contracts.md` §4, surfaced via the existing error-state UI from `task-04`.
- Extend `BriefForm.test.tsx`/add a hook test covering: successful submit navigates with the real `assessmentId`; the `List<FieldErrorResponse>` 422 shows per-field messages; the `AGENT_REJECTED` 422 shows the business message; a 502/503 shows the service-unavailable message; a 500 shows the generic retry message.
- Remove the `setTimeout` fake-submit code path entirely — no leftover dead code or feature flag.
- --

## Example
`POST /assessments`
