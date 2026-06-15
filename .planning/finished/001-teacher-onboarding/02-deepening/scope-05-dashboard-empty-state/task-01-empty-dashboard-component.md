# ⚛️ TASK 01 — EmptyDashboard Component (web/)

> **Status:** DONE
> **Workflow:** GENERATE-DOCUMENT
> **Depends On:** —
> [← scope file](../scope-05-dashboard-empty-state.md)

---

## Objective

Implement `web/src/components/dashboard/EmptyDashboard.tsx` and integrate it into `DashboardPage` so that an empty assessment list shows a clear call-to-action to create the first assessment, with no errors or broken counters.

---

## Technical Design

- **Approach:** `EmptyDashboard` is a presentational component that renders an empty-state illustration (or simple text), a heading ("No assessments yet"), and a CTA button/link to `/assessments/new` (the assessment creation entry point, Epic 02). In `DashboardPage`, the conditional render is: if `assessments.length === 0` render `<EmptyDashboard />`; else render the list. The empty-state disappears automatically once `getAssessments()` returns a non-empty array.
- **Affected files / components:**
  - `web/src/components/dashboard/EmptyDashboard.tsx` (new)
  - `web/src/app/(protected)/dashboard/page.tsx` (update — add conditional render; scope-02 task-02 created this page)
- **Interfaces / contracts:** `EmptyDashboard` takes no props (or optional `createUrl` prop, defaulting to `/assessments/new`). No API calls.
- **Design notes:** The CTA link target (`/assessments/new`) may not exist yet (Epic 02). Use a `<Link>` — a 404 on the target is acceptable for now. Aligned with `docs/06-ux/` copy intent: direct and action-oriented.

---

## Implementation Steps

1. Create `web/src/components/dashboard/EmptyDashboard.tsx`:
   - Heading: "No assessments yet".
   - Subtext: "Create your first assessment to start a grading cycle."
   - CTA: `<Link href="/assessments/new">Create assessment</Link>` styled as a primary button.
2. Update `web/src/app/(protected)/dashboard/page.tsx`:
   - After fetching assessments: `if (assessments.length === 0) return <EmptyDashboard />;`
   - Otherwise render the list.
3. Write `EmptyDashboard.test.tsx` and update `DashboardPage.test.tsx`.

---

## Unit Tests

| # | Case | Expected result | Location |
|---|------|----------------|----------|
| 1 | `getAssessments` returns `[]` | `<EmptyDashboard>` rendered, no list | `DashboardPage.test.tsx` |
| 2 | `getAssessments` returns 1+ items | List rendered, no `<EmptyDashboard>` | `DashboardPage.test.tsx` |
| 3 | `EmptyDashboard` renders CTA | Link to `/assessments/new` present | `EmptyDashboard.test.tsx` |

---

## Done Criteria

- [x] `EmptyDashboard.tsx` exists and renders heading + CTA link.
- [x] Dashboard with 0 assessments renders `EmptyDashboard`, not a broken list.
- [x] Dashboard with ≥1 assessments renders the list, not `EmptyDashboard`.
- [x] No errors or broken counters in zero-data state.
- [x] All unit tests listed above pass.
- [x] No scope creep: the task still satisfies `[CHECK-ATOMICITY]`.

---

> [← scope file](../scope-05-dashboard-empty-state.md)
