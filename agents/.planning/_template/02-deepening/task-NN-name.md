# ⚛️ TASK NN — [Task Name]

> **Status:** TODO
> **Workflow:** [WORKFLOW-NAME]
> **Depends On:** — *(or `task-NN`, comma-separated)*
> [← story file](../story-NN-name.md)

---

## Objective

> *The single deliverable this task produces. One task = one verifiable output.*

[One sentence: what exists after this task that didn't exist before.]

---

## Technical Design

> *Decisions made before implementation. If this section cannot be filled, the task is not ready to execute.*

- **Approach:** [Why this solution and not the obvious alternative — argue the design decision in the context of this story. If the obvious approach is correct, say so explicitly. If alternatives were discarded, name them briefly.]
- **Affected files / components:** [Exact list of files that will be created or modified]
- **Interfaces / contracts:** [What this task exposes to the rest of the story: types, routes, tokens, schemas. "None" if fully internal.]
- **Risk:** [Main risk in this task and how the implementation reduces it. Use "Low — routine change" if appropriate.]
- **Design notes:** [Constraints, invariants, gotchas the implementer must know before touching code]

---

## Implementation Steps

> *Concrete, ordered steps naming real files or components — no abstract verbs.*

1. [Step naming a real file or component]
2. [Step naming a real file or component]

---

## Verification

> *If the task produces no code (config, assets, docs), list manual verifications instead.*

| # | Verification | How to validate |
|---|-------------|----------------|
| 1 | [What is checked] | [Exact steps to confirm it passes] |

### Software Smoke Test Check

> *Required when `.planning/config.yml` has `project.type: software`. Use `.planning/SMOKE-TESTS.md` when it exists. If it does not exist, infer the smoke plan from the repository stack signals, write the inferred commands here before running them, and keep the checks as small and non-destructive as possible.*

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Supporting services are ready | Start the services required by the smoke plan and confirm they are healthy |
| 2 | App compiles and starts | Run the normal local build/start command for the stack |
| 3 | Connectivity or schema validation succeeds | Confirm the app can reach its dependencies and that schema/bootstrap checks pass |
| 4 | Changed surface responds correctly | Run the smallest endpoint/API/CLI checks that prove the task did not break startup |
| 5 | No startup regressions are visible | Confirm logs and smoke output do not show new startup, migration, or boot failures |

### Database / ORM Consistency Check

> *Required when this task changes database structure, migrations, schema files, seed/bootstrap data, ORM models/entities, generated clients, repositories tied to ORM models, or persistence configuration. If no database or ORM is involved, write `N/A`.*

| # | Check | How to validate |
|---|-------|----------------|
| 1 | Static database-to-ORM consistency is valid | Compare migrations/schema against ORM models/entities/generated client for fields, types, nullability, defaults, enums, indexes, relationships, table/column names, and generated artifacts |
| 2 | Local runtime environment starts | Start required local services and the app/worker using the project-local command; ask the human for startup steps if they cannot be inferred |
| 3 | Persistence smoke check passes | Run a minimal non-destructive check that proves migration/bootstrap and the changed persistence path work in execution |

---

## Done Criteria

- [ ] [Deliverable exists and is verifiable: specific and binary]
- [ ] All verification checks listed above pass
- [ ] For software projects, smoke test plan passes: supporting services, app startup, connectivity or schema checks, and changed-surface smoke checks
- [ ] If database structure or ORM artifacts changed, static DB/ORM consistency validation passes and local runtime persistence smoke evidence is captured
- [ ] Human developer code review completed; requested corrections, if any, were implemented and re-reviewed
- [ ] `npm run dev` / `./mvnw test` / equivalent runs without errors
- [ ] No unintended expansion: the task satisfies `[CHECK-ATOMICITY]`

---

> [← story file](../story-NN-name.md)
