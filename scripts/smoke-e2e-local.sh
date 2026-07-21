#!/usr/bin/env bash
#
# smoke-e2e-local.sh — real brief -> generate flow against the local docker-compose stack.
#
# Preconditions (documented here so a future developer isn't blocked figuring them out):
#   1. `docker compose up -d db api agents` is already running (this script does not start it —
#      task-01's compose.yml is the source of truth for how the stack starts).
#   2. A root `.env` (see `.env.example`) with real values for:
#        INTERNAL_API_SECRET          — must match what the `api` container was started with
#        NEXT_PUBLIC_FIREBASE_API_KEY — Firebase Web API key (used for the real Identity Toolkit
#                                        REST call; this is a public key, safe to reuse here)
#      and `agents/`'s own `.env` (see `agents/.env.example`) populated with a real
#      GRADEOPS_GROQ_API_KEY, so the draft-generation call hits a real Groq model.
#
# What it does (no manual state from a previous run required — every run provisions its own
# fresh test teacher with a timestamp-unique email, so re-running never collides). Steps 2-6
# (provision teacher -> auth -> brief -> draft -> retrieve) live in scripts/lib/e2e-smoke-flow.sh,
# shared with scripts/smoke-e2e-render-beta.sh (task-04) — only the preconditions, target URL, and
# the AgentExecutionLog Postgres check below are specific to the local compose stack:
#   1. Provisions a fresh test teacher via POST /internal/teachers (X-Internal-Key).
#   2. Completes the account's password via POST /api/v1/auth/reset-password, using the `code`
#      from the provisioning response's invite link (GradeOps' own reset-code flow — not
#      Firebase's built-in email reset, per docs/09-developer-guide/01-local-setup.md).
#   3. Obtains a real Firebase ID token via the Identity Toolkit REST API.
#   4. POST /api/v1/assessments (brief intake).
#   5. POST /api/v1/assessments/{id}/draft (draft generation — this is the call that internally
#      invokes agentclient -> agents/, over the real network path task-01 wired up).
#   6. GET /api/v1/assessments/{id}/draft (retrieval, confirms persistence).
#
# Exit code is non-zero and the failure reason is printed to stderr for every precondition or
# step that fails — this script never silently reports success.

set -uo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
API_BASE_URL="${API_BASE_URL:-http://localhost:8080}"

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

# shellcheck disable=SC1091
source "$ROOT_DIR/scripts/lib/e2e-smoke-flow.sh"

# --- 0. Load .env (never hardcode secrets) -----------------------------------------------
if [[ -f "$ROOT_DIR/.env" ]]; then
  set -a
  # shellcheck disable=SC1091
  source "$ROOT_DIR/.env"
  set +a
fi

: "${INTERNAL_API_SECRET:?INTERNAL_API_SECRET is required — set it in $ROOT_DIR/.env (see .env.example)}"
: "${NEXT_PUBLIC_FIREBASE_API_KEY:?NEXT_PUBLIC_FIREBASE_API_KEY is required — set it in $ROOT_DIR/.env (see .env.example)}"

# --- 1. Supporting services must already be running ---------------------------------------
echo "==> Checking docker compose services (db, api, agents)..."
running_services="$(cd "$ROOT_DIR" && docker compose ps --status running --format '{{.Service}}' 2>/dev/null || true)"
for svc in db api agents; do
  if ! grep -qx "$svc" <<<"$running_services"; then
    fail "service '$svc' is not running. Run 'docker compose up -d db api agents' first (this script does not start the stack)."
  fi
done
echo "    db, api, agents are running."

# --- 2-6. Shared brief -> generate -> retrieve flow (scripts/lib/e2e-smoke-flow.sh) ---------
run_brief_to_draft_flow

# --- 6b. Persisted AgentExecutionLog (the story/task's actual required evidence, not just the
#         draft response — DraftGenerationCoordinator persists the log, then back-fills draft_id
#         in a second save, so this also confirms that backfill happened for real). Local-only:
#         the compose stack's Postgres is reachable via `docker compose exec`; beta's Neon
#         database is not, so scripts/smoke-e2e-render-beta.sh does not repeat this check
#         (documented there as a scoping limitation). ------------------------------------------
echo "==> Verifying persisted AgentExecutionLog in Postgres..."
log_row="$(cd "$ROOT_DIR" && docker compose exec -T db psql -U gradeops -d gradeops -t -A -F'|' -c \
  "SELECT status, model, agent_execution_id, draft_id FROM agent_execution_logs WHERE assessment_id = '${assessment_id}' ORDER BY started_at DESC LIMIT 1;" 2>&1)"
if [[ -z "$log_row" ]]; then
  fail "no agent_execution_logs row found for assessment_id=${assessment_id}"
fi
log_status="$(cut -d'|' -f1 <<<"$log_row")"
log_model="$(cut -d'|' -f2 <<<"$log_row")"
log_agent_execution_id="$(cut -d'|' -f3 <<<"$log_row")"
log_draft_id="$(cut -d'|' -f4 <<<"$log_row")"
if [[ "$log_status" != "COMPLETED" ]]; then
  fail "agent_execution_logs.status = '${log_status}', expected 'COMPLETED' (row: ${log_row})"
fi
if [[ -z "$log_model" ]]; then
  fail "agent_execution_logs.model is empty — not a real model response (row: ${log_row})"
fi
if [[ -z "$log_agent_execution_id" ]]; then
  fail "agent_execution_logs.agent_execution_id is empty (row: ${log_row})"
fi
if [[ "$log_draft_id" != "$draft_id" ]]; then
  fail "agent_execution_logs.draft_id ('${log_draft_id}') does not match the generated draftId ('${draft_id}') — the log->draft backfill did not happen"
fi
echo "    persisted log confirmed: status=${log_status}, model=${log_model}, agent_execution_id=${log_agent_execution_id}, draft_id backfilled correctly"

# --- Summary -----------------------------------------------------------------------------------
echo
echo "PASS: real brief -> generate -> retrieve flow completed against the local compose stack,"
echo "with a persisted AgentExecutionLog confirmed directly in Postgres (not inferred from shape)."
echo "  teacher email        : ${teacher_email}"
echo "  assessmentId         : ${assessment_id}"
echo "  draft title          : ${draft_title}"
echo "  draft objectives     : ${objectives_count}"
echo "  log status           : ${log_status}"
echo "  log model            : ${log_model}"
echo "  log agent_execution_id: ${log_agent_execution_id}"
echo "  full draft payload:"
jq '.' <<<"$draft_body"
