# US-080: Agent Execution Log

- **Epic:** 09 — Evidence and Metrics
- **Priority:** P0
- **ID:** US-080

## Story

As an operator, I want every agent execution logged so we can prove AI-native operations and debug the workflow.

## Acceptance Criteria

- Every agent run has timestamp, agent, model, input summary, output summary, status, cost estimate, and approval state.
- Logs are associated with assessment/customer.
- Logs are visible in internal dashboard.
- Failed/retried runs are captured.
