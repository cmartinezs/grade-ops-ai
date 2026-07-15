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
# fresh test teacher with a timestamp-unique email, so re-running never collides):
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

# --- 2. Provision a fresh test teacher ------------------------------------------------------
run_id="$(date +%s)"
teacher_email="smoke-e2e-${run_id}@gradeops.test"
teacher_password="Sm0ke-e2e-${run_id}!"

echo "==> Provisioning test teacher ${teacher_email}..."
provision_response="$(curl -sS -w '\n%{http_code}' -X POST "${API_BASE_URL}/internal/teachers" \
  -H "X-Internal-Key: ${INTERNAL_API_SECRET}" \
  -H "Content-Type: application/json" \
  -d "{\"firstName\":\"Smoke\",\"lastName\":\"Tester\",\"email\":\"${teacher_email}\"}")"
provision_status="$(tail -n1 <<<"$provision_response")"
provision_body="$(sed '$d' <<<"$provision_response")"

if [[ "$provision_status" != "201" ]]; then
  fail "teacher provisioning returned HTTP ${provision_status}: ${provision_body}"
fi

invite_link="$(jq -r '.inviteLink' <<<"$provision_body")"
reset_code="$(sed -n 's/.*[?&]code=\([^&]*\).*/\1/p' <<<"$invite_link")"
if [[ -z "$reset_code" ]]; then
  fail "could not extract reset code from inviteLink: ${invite_link}"
fi
echo "    provisioned firebaseUid=$(jq -r '.firebaseUid' <<<"$provision_body")"

# --- 3. Set the account's real password (GradeOps' own reset-code flow) --------------------
echo "==> Setting test teacher password..."
reset_response="$(curl -sS -w '\n%{http_code}' -X POST "${API_BASE_URL}/api/v1/auth/reset-password" \
  -H "Content-Type: application/json" \
  -d "{\"code\":\"${reset_code}\",\"email\":\"${teacher_email}\",\"password\":\"${teacher_password}\",\"passwordRepeat\":\"${teacher_password}\"}")"
reset_status="$(tail -n1 <<<"$reset_response")"
if [[ "$reset_status" != "204" ]]; then
  fail "password reset returned HTTP ${reset_status}: $(sed '$d' <<<"$reset_response")"
fi
echo "    password set."

# --- 4. Real Firebase ID token via the Identity Toolkit REST API ---------------------------
echo "==> Obtaining a real Firebase ID token..."
token_response="$(curl -sS -w '\n%{http_code}' -X POST \
  "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${NEXT_PUBLIC_FIREBASE_API_KEY}" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${teacher_email}\",\"password\":\"${teacher_password}\",\"returnSecureToken\":true}")"
token_status="$(tail -n1 <<<"$token_response")"
token_body="$(sed '$d' <<<"$token_response")"
if [[ "$token_status" != "200" ]]; then
  fail "Firebase signInWithPassword returned HTTP ${token_status}: ${token_body}"
fi
id_token="$(jq -r '.idToken' <<<"$token_body")"
if [[ -z "$id_token" || "$id_token" == "null" ]]; then
  fail "no idToken in Firebase response: ${token_body}"
fi
echo "    idToken obtained (${#id_token} chars)."

auth_header="Authorization: Bearer ${id_token}"

# --- 5. Brief intake -------------------------------------------------------------------------
echo "==> POST /api/v1/assessments (brief intake)..."
brief_response="$(curl -sS -w '\n%{http_code}' -X POST "${API_BASE_URL}/api/v1/assessments" \
  -H "$auth_header" -H "Content-Type: application/json" \
  -d '{"learningGoal":"Understand recursion","topic":"Recursive algorithms","level":"intermediate","duration":"60 minutes","language":"Python"}')"
brief_status="$(tail -n1 <<<"$brief_response")"
brief_body="$(sed '$d' <<<"$brief_response")"
if [[ "$brief_status" != "201" ]]; then
  fail "brief intake returned HTTP ${brief_status}: ${brief_body}"
fi
assessment_id="$(jq -r '.assessmentId' <<<"$brief_body")"
if [[ -z "$assessment_id" || "$assessment_id" == "null" ]]; then
  fail "no assessmentId in brief response: ${brief_body}"
fi
echo "    assessmentId=${assessment_id}"

# --- 6. Draft generation (real agents/ call over the network) --------------------------------
echo "==> POST /api/v1/assessments/${assessment_id}/draft (triggers agents/ over the real network)..."
draft_response="$(curl -sS -w '\n%{http_code}' -X POST "${API_BASE_URL}/api/v1/assessments/${assessment_id}/draft" \
  -H "$auth_header")"
draft_status="$(tail -n1 <<<"$draft_response")"
draft_body="$(sed '$d' <<<"$draft_response")"
if [[ "$draft_status" != "201" ]]; then
  fail "draft generation returned HTTP ${draft_status}: ${draft_body}"
fi
draft_title="$(jq -r '.title' <<<"$draft_body")"
objectives_count="$(jq -r '.objectives | length' <<<"$draft_body")"
if [[ -z "$draft_title" || "$draft_title" == "null" || "$objectives_count" == "0" ]]; then
  fail "draft response is missing a real generated shape (title/objectives empty) — got: ${draft_body}"
fi
echo "    draft generated: title=\"${draft_title}\", objectives=${objectives_count}"

# --- 7. Retrieval confirms persistence --------------------------------------------------------
echo "==> GET /api/v1/assessments/${assessment_id}/draft (confirms persistence)..."
get_response="$(curl -sS -w '\n%{http_code}' "${API_BASE_URL}/api/v1/assessments/${assessment_id}/draft" \
  -H "$auth_header")"
get_status="$(tail -n1 <<<"$get_response")"
get_body="$(sed '$d' <<<"$get_response")"
if [[ "$get_status" != "200" ]]; then
  fail "draft retrieval returned HTTP ${get_status}: ${get_body}"
fi
get_title="$(jq -r '.title' <<<"$get_body")"
if [[ "$get_title" != "$draft_title" ]]; then
  fail "retrieved draft title (\"${get_title}\") does not match generated draft title (\"${draft_title}\")"
fi
echo "    retrieval matches generated draft."

# --- Summary -----------------------------------------------------------------------------------
echo
echo "PASS: real brief -> generate -> retrieve flow completed against the local compose stack."
echo "  teacher email     : ${teacher_email}"
echo "  assessmentId      : ${assessment_id}"
echo "  draft title       : ${draft_title}"
echo "  draft objectives  : ${objectives_count}"
echo "  full draft payload:"
jq '.' <<<"$draft_body"
echo
echo "NOTE: api/'s GenerateAssessmentDraftResponse does not expose model/costEstimate fields (checked"
echo "against the real API response above) — those are only in the persisted AgentExecutionLog, which"
echo "has no query endpoint today. A real, non-empty structured draft (this script's actual assertion)"
echo "is the strongest available proof the real Gemini/Groq pipeline ran, since agents/'s schema"
echo "validation would reject any hand-crafted fake shape it didn't itself produce."
