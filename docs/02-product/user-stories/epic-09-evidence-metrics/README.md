# Epic 09 — Evidence and Metrics

## Narrative

**As an** operator running a hackathon MVP,  
**I want** every agent execution logged, token costs tracked, and a dashboard of usage and business activity,  
**so that** we can prove AI-native operations, track unit economics from day one, and produce credible hackathon submission evidence.

## Goal

Make the system's AI operations fully observable and auditable. Evidence is a first-class product requirement, not a side-effect. Every agent run, approval, usage event, and cost estimate must be captured and queryable.

## Stories

| ID | Title | Priority | File |
|----|-------|----------|------|
| US-080 | Agent Execution Log | P0 | [01-agent-execution-log.md](01-agent-execution-log.md) |
| US-081 | Cost Estimate Per Run | P0 | [02-cost-estimate-per-run.md](02-cost-estimate-per-run.md) |
| US-082 | Business Evidence Dashboard | P0 | [03-business-evidence-dashboard.md](03-business-evidence-dashboard.md) |
| US-083 | Time Saved Estimate | P1 | [04-time-saved-estimate.md](04-time-saved-estimate.md) |

## Scope

**In scope**
- `AgentExecutionLog` entity: timestamp, agent name, model, input/output summary, status, cost estimate, approval state, assessment/customer association
- Failed and retried runs captured in the log
- Per-run token and cost estimates stored and aggregatable by assessment and customer
- Internal dashboard: assessments processed, submissions processed, feedback outputs, agent runs, estimated AI cost, pilot/customer status
- Time-saved estimate per assessment, surfaced in report and dashboard (P1)

**Out of scope**
- Real-time streaming dashboards or live metric updates
- External billing or invoice generation (handled by Epic 10 plan limits)
- Public-facing cost transparency for students

## Epic Acceptance Criteria

- Every agent run (all 13 agents) produces an `AgentExecutionLog` record with: timestamp, agent, model, input summary, output summary, status, and approval state.
- Failed or retried runs are captured, not silently dropped.
- Model name, input/output token estimate, and cost estimate are stored per run.
- Costs are aggregatable by assessment and by customer/organization.
- Internal dashboard shows: assessments processed, submissions processed, feedback outputs, agent runs, estimated AI cost, and pilot/customer links.
- (P1) Teacher or operator can view a time-saved estimate per assessment; estimate is clearly labeled as estimated.

## Dependencies

| Epic | Reason |
|------|--------|
| Epic 01 — Teacher Onboarding | Customer/pilot account model is required for attribution |
| Epics 02–08, 11–13 | Agent execution logs are produced by every agent across all epics |

## Definition of Done

- All P0 stories pass their acceptance criteria.
- `AgentExecutionLog` is a first-class persisted entity, not a log file.
- No agent call in the codebase skips log creation.
- Dashboard data is sourced from the database, not log files or hardcoded values.
- Cost aggregation query is correct and tested against multiple assessments.
