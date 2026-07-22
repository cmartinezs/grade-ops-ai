# R01 Release-to-Planning Bridge

> Source analysis: planning-plugin fit review report `claude-planning-with-ai:docs/plugin-review/08-gradeops-master-plan-fit.md`.
> Scope: apply the planning-plugin fit review inside GradeOps workspace artifacts only. Do not modify plugin templates or child implementation workspaces from this parent planning.

## Verdict Applied

R01 can start implementation from the current Master Plan, but the release file is not itself an executable planning. The missing translation layer is now captured here:

- root coordination planning: `.planning/active/008-assessment-creation`;
- release source: `docs/master-plan/releases/release-01-assessment-creation-evidence-backbone.md`;
- child implementation workspaces: `api/.planning`, `agents/.planning`, `web/.planning`;
- infra/docs evidence scope: root docs plus `infra/terraform/environments/demo/` and beta environment manifests when applicable.

## Current Child Planning Inventory

| Area | Existing planning | Current interpretation for R01 |
|---|---|---|
| `api/` | `api/.planning/finished/003-assessment-creation` | Finished baseline for brief/draft persistence and agentclient calls. New R01 hardening should not reopen it silently; create a new child planning if `AiOperation`, provider/model policy, idempotency, cost fields or rich log schema exceed residual fixes. |
| `agents/` | `agents/.planning/active/001-assessment-creation`; `agents/.planning/finished/002-groq-genai-provider` | Assessment Agent contract exists and Groq provider work is finished. New R01 hardening should focus on provider/model allowlist, normalized errors, token/cost metadata, trace propagation and multi-environment service identity. |
| `web/` | `web/.planning/active/001-assessment-creation` | Owns assessment creation UI closure. It must consume functional `api/` routes only and apply API-Agent, security and observability gates already added to endpoint-facing tasks. |
| `infra/` | `infra/terraform/environments/demo/` plus beta docs/config | No child `.planning` workspace was found. Infra work for R01 should be represented as a root/infra scope or manifest task when it affects demo/beta service identity, secrets, telemetry, headers, provider credentials or deploy proof. |

## R01 Readiness Gates

| Gate | Required before atomizing new implementation tasks | Status |
|---|---|---|
| D-01 environment roles reconciled | `beta` is product-evidence environment; `demo` is Google Cloud target. R01 does not block on deployed demo, but claims must match evidence. | Applied in Master Plan docs by this bridge pass. |
| D-04 provider/model policy | Provider/model/cost policy must be explicit enough that Groq/Gemini are not hardcoded inconsistently. | Pending R01 child planning/ADR work. |
| D-06 rich `AgentExecutionLog` | US-080 is now materialized and should drive API/Agents/Web/observability tasks. | Source story enriched; implementation pending. |
| US-081 cost estimate | US-081 is now materialized and should drive provider/model and aggregation tasks. | Source story enriched; implementation pending. |
| D-TEST testing gates | R01 planning must include unit/component/coverage baseline, Assessment Web-API/API-Agents contracts, isolated acceptance and minimum Compose `api-agents`/`web-api`/`full-chain` coverage. | Testing strategy incorporated; implementation/testkit pending. |
| D-UI UI Design/Data Semantics | R01 web planning must start from Design System, classify intake fields, avoid all-text-input implementation, and record API/catalog/master-data gaps for `topic`, `level`, `duration` and `language`. | Strategy incorporated; implementation/API catalog decisions pending. |
| D-APIIO screen data and sync/async contract | Every screen read/write datum must map to `api/`; missing endpoints/read models/catalogs/mutations/operation states require API scope or blocking residual. Every action must declare sync vs async and async completion/progress mechanism. | Strategy incorporated; child tasks must verify before implementation Done. |
| D-I18N locale and user-facing language | R01 must keep source code/contracts/logs/telemetry in English while UI copy, safe errors, catalog labels and generated drafts use effective locale/`outputLocale`. | Strategy incorporated; child tasks must verify before implementation Done. |
| Existing child planning audit | Check active/finished child planning state before creating new work. | Inventory captured above; refresh before execution. |
| Plugin release file | `.releases/` should be created only when version, target period and date are known. | Not created here to avoid inventing release metadata. |

## Child Planning Briefs

### `api/` Brief

Create a new child planning only if the work is beyond a small residual patch. Candidate scope:

- `AiOperation`, `AgentRun`, `AgentAttempt` model or compatibility mapping;
- rich `AgentExecutionLog` schema aligned to US-080;
- provider/model policy and price-version lookup aligned to US-081/D-04;
- `Idempotency-Key` for GenAI mutating commands;
- normalized errors with request/correlation/trace IDs;
- contract tests for API-Web and API-Agents;
- query/read model for internal log/cost inspection if included in R01;
- master-data/catalog or enum support if the intake field matrix requires API-backed values for `topic`, `level`, `duration` presets or `language`;
- screen read models, mutation responses, capability flags and operation-status endpoints required by `web/` for both Intake and Draft Builder;
- sync/async decision per R01 command, including operation polling/SSE/WebSocket/webhook/push contract when generation/regeneration is not returned synchronously;
- locale resolver/fallback, localized catalog labels or translatable codes, safe error response strategy, and persistence of `contentLocale`/`outputLocale` for generated draft artifacts/operations;
- DB/ORM/Flyway consistency checks and negative auth/ownership tests.

Required gates: API-Agent Orchestration, Richardson REST, Security & Authorization, Observability & Telemetry, Testing & Quality Gates, DB/ORM consistency, smoke.

### `agents/` Brief

Candidate scope:

- provider/model allowlist and effective provider/model reporting;
- normalized error model;
- token/cost metadata and missing-data warnings;
- trace propagation from `api` into provider call spans;
- `outputLocale`/`contentLocale` support in Assessment Agent commands and locale mismatch warnings for generated user-facing drafts;
- fail-secure defaults for malformed policy/provider/model;
- service identity per environment: OIDC/IAM in `demo`, signed internal JWT in `beta` if implemented in R01;
- no domain persistence and no human RBAC in `agents`.

Required gates: API-Agent internal contract, Security & Authorization, Observability & Telemetry, Testing & Quality Gates, provider/model policy, prompt/output redaction.

### `web/` Brief

Continue `web/.planning/active/001-assessment-creation`; do not create duplicate root implementation tasks. Candidate closure:

- intake and draft routes consume real `api/` DTOs;
- every visible/editable datum is backed by `api/`: reads, writes, catalogs/defaults/capabilities, mutations, errors and operation status;
- missing API support becomes a child `api/` scope or blocking residual before UI Done, not a permanent fixture, local DTO or text-only workaround;
- `/dashboard` exposes a working new-assessment action that navigates to `/assessments/new`; the intake screen cannot be considered delivered if it is only reachable by typing the URL;
- intake UI starts from `web/design-system/`, produces a field matrix and uses semantic controls; it cannot close as five unrestricted text inputs;
- `learningGoal` is long free text; `level` is enum/difficulty; `duration` is numeric/preset; `language` is enum/catalog; `topic` is candidate curriculum master-data/tag/custom-controlled according to API/domain decision;
- regenerate/versioning UX reflects supported API states only;
- visible error/retry state without fake operation polling;
- sync/async communication is explicit for brief creation, draft generation, save and regeneration; async completion/progress/failure uses the agreed API-backed mechanism only;
- UI copy, labels, validation/safe errors and catalog labels use i18n; `language` remains programming language, while generated draft/regeneration uses `outputLocale`;
- trace/correlation IDs surfaced only as safe support references;
- no `agents` URLs, provider names or prompts in frontend logic unless returned safely by `api`;
- internal log/cost inspection only if API exposes a supported route/capability in R01.

Required gates: API-Agent Web Contract Gate, API I/O + Sync/Async Contract, i18n, UI Action Reachability, UI Design/Data Semantics, Security & Authorization, Observability & Telemetry, Testing & Quality Gates, TSX/design-system conventions, route smoke.

### `infra` / `docs` Brief

Candidate scope:

- D-04 ADR/provider-model policy completion;
- D-06 log schema decision completion;
- UI Design/Data Semantics ADR/strategy synchronization and residual tracking for missing master-data/catalog/API I/O support;
- API I/O + sync/async contract synchronization across release docs, child planning briefs and test suites;
- i18n ADR/strategy synchronization across release docs, child planning briefs and test suites;
- demo/beta claim alignment in documentation;
- beta manifest for provider credentials, secrets, telemetry and deploy proof if R01 uses beta evidence;
- Terraform/demo verification only for claims actually made by R01;
- observability backend/OTel ADR if implementation starts in R01.
- CI/testkit or manifest scope for R01 testing gates, including Compose profiles and artifact publication when implemented.

Required gates: environment-specific evidence, secrets not exposed, telemetry retention/cost, Testing & Quality Gates, release documentation updated.

## Next Execution Sequence

1. Refresh child planning states in `api/`, `agents/` and `web`.
2. Decide whether D-04/D-06 are ADR-only, implementation tasks, or both.
3. Decide R01 sync/async behavior for brief creation, generation, draft load, save and regeneration; if async, choose completion/progress mechanism and API ownership before web implementation.
4. Decide R01 i18n behavior for dashboard/intake/draft: locale source/fallback, translation ownership, catalog labels/safe errors and `outputLocale` propagation.
5. Create child planning briefs in the relevant child workspaces from this file; include testing/CI/testkit ownership and do not implement child-owned work in the root planning.
6. Create `.releases/<version>.md` only after choosing version, target period and estimated date.
7. Atomize only the child planning that owns the changed files.

## History

| Date | Change | Reason |
|---|---|---|
| 2026-07-21 | Added i18n gate | Ensure R01 user-facing copy and generated drafts respect locale while technical code/logs stay in English. |
| 2026-07-21 | Added API I/O + sync/async contract gate | Ensure R01 UI data is backed by `api/` and async work has an explicit completion model. |
| 2026-07-21 | Added UI Design/Data Semantics gate | Ensure R01 intake starts from DS and preserves field/source-of-truth semantics instead of shipping as all-text-input UI. |
| 2026-07-21 | Created bridge | Apply plugin fit review by converting R01 release docs into parent/child planning inputs without modifying plugin templates. |
