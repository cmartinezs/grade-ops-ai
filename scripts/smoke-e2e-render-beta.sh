#!/usr/bin/env bash
#
# smoke-e2e-render-beta.sh — real brief -> generate flow against the deployed `beta` Render
# environment (task-04), proving the api/<->agents/ path also works in production, not only
# locally (scripts/smoke-e2e-local.sh, task-02).
#
# Preconditions:
#   1. Render CLI installed (curl -fsSL https://raw.githubusercontent.com/render-oss/cli/main/bin/install.sh | sh)
#      and on PATH.
#   2. A root `.env` (see `.env.example`) with real values for:
#        RENDER_API_KEY               — Render Dashboard -> Account Settings -> API Keys
#        RENDER_WORKSPACE_ID          — the Render workspace/team id owning grade-ops-ai-api/agents
#                                        (see task-03's evidence for how to resolve this via
#                                        `GET https://api.render.com/v1/owners` if unknown)
#        BETA_API_BASE_URL            — the deployed api/ service's public URL. Note: Render's
#                                        auto-generated *.onrender.com hostname is a separate slug
#                                        from the service's display name and does NOT change when
#                                        the display name is renamed — confirmed the real value is
#                                        still https://gradeops-api.onrender.com even after task-03
#                                        renamed the display name to grade-ops-ai-api.
#        INTERNAL_API_SECRET          — must match beta's api/ deployment's own secret, NOT the
#                                        local compose one (these are different values in
#                                        different environments)
#        NEXT_PUBLIC_FIREBASE_API_KEY — Firebase Web API key (shared across demo/beta per
#                                        beta-environment-design.md; safe to reuse the same value
#                                        already used locally)
#
# What it does (steps 2-6 live in scripts/lib/e2e-smoke-flow.sh, shared with
# scripts/smoke-e2e-local.sh — only the pre-flight deploy check, warm-up, and target URL below
# are specific to the deployed beta environment):
#   1. Render CLI: confirm the latest deploy for grade-ops-ai-api and grade-ops-ai-agents is
#      `live` before doing anything else — fails clearly, not with a confusing HTTP error, if a
#      service is mid-deploy, crashed, or missing.
#   2. Warm-up requests to BOTH agents/ and api/'s public roots with a generous timeout and one
#      retry (Render free tier cold-starts after ~15 min idle per beta-environment-design.md, and
#      a real run confirmed agents/ takes ~50s to wake from a full cold sleep). Warming up only
#      api/ is not sufficient: a real run of this script found that when agents/ is cold, the
#      internal api/->agents/ call gets a 429 from Render's edge while agents/ is still waking,
#      which api/ surfaces as `AGENT_CALL_FAILED`/`AGENT_REJECTED` rather than a clean timeout.
#   3. Provision a fresh test teacher, obtain a real Firebase ID token, brief -> generate ->
#      retrieve, same as the local script.
#
# Scoping limitation (documented, not silently skipped): unlike smoke-e2e-local.sh, this script
# does not independently verify the persisted AgentExecutionLog row in Postgres. The local
# script can do that because the compose stack's Postgres is reachable via `docker compose exec`;
# beta's Neon database has no such access path from this script without a Neon connection string,
# which was not available when task-04 was executed. The generated draft response shape (title,
# objectives, deliverables all populated) is the evidence bar this script meets — the same bar
# story-04's own Done Criteria sets for the deployed phase, distinct from the higher bar task-02
# was held to after its P1 review finding.
#
# Exit code is non-zero and the failure reason is printed to stderr for every precondition or
# step that fails — this script never silently reports success.

set -uo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

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

: "${RENDER_API_KEY:?RENDER_API_KEY is required — set it in $ROOT_DIR/.env (Render Dashboard -> Account Settings -> API Keys)}"
: "${RENDER_WORKSPACE_ID:?RENDER_WORKSPACE_ID is required — set it in $ROOT_DIR/.env (see task-03's evidence for how to resolve it)}"
: "${BETA_API_BASE_URL:?BETA_API_BASE_URL is required — set it in $ROOT_DIR/.env (the deployed api/ service's public URL)}"
: "${INTERNAL_API_SECRET:?INTERNAL_API_SECRET is required — set it in $ROOT_DIR/.env (beta's api/ deployment's own secret, not the local one)}"
: "${NEXT_PUBLIC_FIREBASE_API_KEY:?NEXT_PUBLIC_FIREBASE_API_KEY is required — set it in $ROOT_DIR/.env}"

export RENDER_API_KEY
API_BASE_URL="$BETA_API_BASE_URL"

command -v render >/dev/null 2>&1 || fail "Render CLI not found on PATH. Install: curl -fsSL https://raw.githubusercontent.com/render-oss/cli/main/bin/install.sh | sh"
command -v jq >/dev/null 2>&1 || fail "jq is required"

RENDER_API_SERVICE_NAME="${RENDER_API_SERVICE_NAME:-grade-ops-ai-api}"
RENDER_AGENTS_SERVICE_NAME="${RENDER_AGENTS_SERVICE_NAME:-grade-ops-ai-agents}"

# --- 1. Render CLI: confirm both services' latest deploy is live --------------------------
echo "==> Setting Render CLI workspace..."
render workspace set "$RENDER_WORKSPACE_ID" --confirm -o json >/dev/null 2>&1 \
  || fail "could not set Render workspace '${RENDER_WORKSPACE_ID}' — check RENDER_API_KEY/RENDER_WORKSPACE_ID"

echo "==> Checking Render service status (grade-ops-ai-api, grade-ops-ai-agents)..."
services_json="$(render services -o json 2>&1)" || fail "render services failed: ${services_json}"

agents_public_url=""
for svc_name in "$RENDER_API_SERVICE_NAME" "$RENDER_AGENTS_SERVICE_NAME"; do
  svc_id="$(jq -r --arg name "$svc_name" '.[].service | select(.name == $name) | .id' <<<"$services_json")"
  if [[ -z "$svc_id" ]]; then
    fail "Render service '${svc_name}' not found in workspace ${RENDER_WORKSPACE_ID}"
  fi

  deploys_json="$(render deploys list "$svc_id" -o json 2>&1)" || fail "render deploys list ${svc_id} failed: ${deploys_json}"
  latest_status="$(jq -r '.[0].status' <<<"$deploys_json")"
  latest_commit="$(jq -r '.[0].commit.id[0:12]' <<<"$deploys_json")"

  if [[ "$latest_status" != "live" ]]; then
    fail "Render service '${svc_name}' (${svc_id}) latest deploy status is '${latest_status}', expected 'live' — not proceeding with the HTTP smoke flow against a service that isn't confirmed live."
  fi
  echo "    ${svc_name} (${svc_id}): latest deploy live at commit ${latest_commit}"

  if [[ "$svc_name" == "$RENDER_AGENTS_SERVICE_NAME" ]]; then
    agents_public_url="$(jq -r --arg name "$svc_name" '.[].service | select(.name == $name) | .serviceDetails.url' <<<"$services_json")"
  fi
done
if [[ -z "$agents_public_url" || "$agents_public_url" == "null" ]]; then
  fail "could not resolve ${RENDER_AGENTS_SERVICE_NAME}'s public URL from the services listing"
fi

# --- 2. Warm-up requests (Render free tier cold-starts after ~15 min idle, confirmed to take
#        ~50s from a full sleep; a single failed first attempt is a known transient behavior,
#        also observed locally in task-02 on the very first request after `docker compose up` —
#        retried once before treating it as a real failure).
#
#        Both api/ AND agents/ are warmed up directly, not just api/: a real run of this script
#        found that warming up only api/ isn't enough — when agents/ is fully cold, the internal
#        api/->agents/ call (AssessmentAgentClient) hits Render's edge in front of the still-
#        waking agents/ instance and gets back a 429, which api/ correctly surfaces as
#        `AGENT_CALL_FAILED`/`AGENT_REJECTED` rather than a plain timeout. Warming agents/ up
#        directly first avoids exercising that path cold. ------------------------------------
warm_up() {
  local name="$1" url="$2" max_time="$3" status
  echo "==> Warm-up request to ${name} (${url}), up to ${max_time}s..."
  status="$(curl -sS -o /dev/null -w '%{http_code}' --max-time "$max_time" "$url" 2>/dev/null)"
  if [[ -z "$status" || "$status" == "000" ]]; then
    echo "    first attempt got no response — retrying once (matches the transient first-request behavior task-02 also observed locally)..."
    status="$(curl -sS -o /dev/null -w '%{http_code}' --max-time "$max_time" "$url" 2>/dev/null)"
  fi
  if [[ -z "$status" || "$status" == "000" ]]; then
    fail "${name} did not respond after two warm-up attempts (${max_time}s each) — service may be down, not just cold-starting"
  fi
  echo "    ${name} responded: HTTP ${status} (any HTTP response confirms the service answered, not a 200 requirement)"
}

warm_up "agents/" "$agents_public_url" 90
warm_up "api/" "${API_BASE_URL}/" 90

# --- 3-7. Shared brief -> generate -> retrieve flow (scripts/lib/e2e-smoke-flow.sh) ---------
run_brief_to_draft_flow

# --- Summary -----------------------------------------------------------------------------------
echo
echo "PASS: real brief -> generate -> retrieve flow completed against the deployed beta"
echo "environment on Render (${API_BASE_URL}), confirming grade-ops-ai-api reached"
echo "grade-ops-ai-agents over the real deployed network path."
echo "  teacher email     : ${teacher_email}"
echo "  assessmentId      : ${assessment_id}"
echo "  draft title       : ${draft_title}"
echo "  draft objectives  : ${objectives_count}"
echo "  full draft payload:"
jq '.' <<<"$draft_body"
