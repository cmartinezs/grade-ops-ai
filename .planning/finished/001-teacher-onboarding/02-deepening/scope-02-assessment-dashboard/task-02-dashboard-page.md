# ⚛️ TASK 02 — AssessmentDashboard Page (web/)

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** task-01
> [← scope file](../scope-02-assessment-dashboard.md)

---

## Objective

Implement `web/src/app/(protected)/dashboard/page.tsx` — a dashboard that fetches `GET /assessments` and renders the assessment list with status, submission count, pending approvals, and conditional report links.

---

## Technical Design

- **Approach:** Next.js App Router page under the `(protected)` route group (wrapped by `AuthGuard` from scope-01 task-03). Fetches `GET /assessments` via the API client. Maps `AssessmentSummaryDto[]` to a list of `AssessmentCard` components. Conditionally renders a report/log link only when `reportLink !== null`. Status and pending-approval count displayed as-is from the API (no frontend state invention). The empty-state rendering (no assessments) is handled in scope-05 — this page only renders the list when data is present; it also works correctly with 0 items by rendering an empty list (no errors).
- **Affected files / components:**
  - `web/src/app/(protected)/dashboard/page.tsx` (new)
  - `web/src/components/dashboard/AssessmentCard.tsx` (new)
  - `web/src/lib/api/assessments.ts` (new — `getAssessments(): Promise<AssessmentSummaryDto[]>`)
  - `web/src/types/assessment.ts` (new — `AssessmentSummaryDto` TypeScript type mirroring the API contract)
- **Interfaces / contracts:** `AssessmentSummaryDto` type mirrors the API response exactly (types flow API → Web rule).
- **Design notes:** Uses the teacher-scoped API client that attaches `Authorization: Bearer <idToken>` automatically. No frontend-invented statuses. Report link only rendered when `reportLink !== null`.

---

## Implementation Steps

1. Create `web/src/types/assessment.ts` with `AssessmentSummaryDto` interface matching the API contract.
2. Create `web/src/lib/api/assessments.ts` exporting `getAssessments()` that calls `GET /assessments` with the auth token.
3. Create `web/src/components/dashboard/AssessmentCard.tsx`: renders one assessment (title, status badge, submission count, pending approvals badge, optional report link).
4. Create `web/src/app/(protected)/dashboard/page.tsx` (can be a Server Component if using Next.js server-side fetch with session token, or a Client Component using SWR/useEffect): fetches assessments, maps to `<AssessmentCard>` list, renders empty container (not empty state — that's scope-05) on zero items.
5. Write `DashboardPage.test.tsx` and `AssessmentCard.test.tsx`.

---

## Unit Tests

| # | Case | Expected result | Location |
|---|------|----------------|----------|
| 1 | API returns 3 assessments | Renders 3 `AssessmentCard` components | `DashboardPage.test.tsx` |
| 2 | API returns empty array | Renders 0 cards, no error thrown | `DashboardPage.test.tsx` |
| 3 | `reportLink` is null | No link rendered in the card | `AssessmentCard.test.tsx` |
| 4 | `reportLink` is present | Link rendered pointing to the URL | `AssessmentCard.test.tsx` |

---

## Done Criteria

- [x] `web/src/app/(protected)/dashboard/page.tsx` exists and renders without errors.
- [x] Assessment list displayed with status, submission count, pending approvals per card.
- [x] Report link only rendered when `reportLink !== null`.
- [x] `AssessmentSummaryDto` TypeScript type matches the API contract field-for-field.
- [x] All unit tests listed above pass.
- [x] No scope creep: the task still satisfies `[CHECK-ATOMICITY]`.

---

> [← scope file](../scope-02-assessment-dashboard.md)
