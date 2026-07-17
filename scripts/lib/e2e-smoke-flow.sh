#!/usr/bin/env bash
#
# e2e-smoke-flow.sh — shared brief -> generate -> retrieve flow used by both
# scripts/smoke-e2e-local.sh and scripts/smoke-e2e-render-beta.sh.
#
# This file is sourced, not executed directly. The sourcing script must first:
#   - `set -uo pipefail`
#   - define a `fail()` function that prints to stderr and exits non-zero
#   - set `API_BASE_URL` to the target api/ base URL
#   - export `INTERNAL_API_SECRET` and `NEXT_PUBLIC_FIREBASE_API_KEY`
#
# Each function leaves its result in predictable variables (teacher_email,
# assessment_id, draft_id, draft_title, objectives_count, draft_body, ...) so
# the sourcing script can use them afterward (e.g. for its own summary or an
# environment-specific persistence check).

provision_test_teacher() {
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

  echo "==> Setting test teacher password..."
  reset_response="$(curl -sS -w '\n%{http_code}' -X POST "${API_BASE_URL}/api/v1/auth/reset-password" \
    -H "Content-Type: application/json" \
    -d "{\"code\":\"${reset_code}\",\"email\":\"${teacher_email}\",\"password\":\"${teacher_password}\",\"passwordRepeat\":\"${teacher_password}\"}")"
  reset_status="$(tail -n1 <<<"$reset_response")"
  if [[ "$reset_status" != "204" ]]; then
    fail "password reset returned HTTP ${reset_status}: $(sed '$d' <<<"$reset_response")"
  fi
  echo "    password set."
}

obtain_id_token() {
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
}

run_brief_intake() {
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
}

trigger_draft_generation() {
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
  draft_id="$(jq -r '.draftId' <<<"$draft_body")"
}

retrieve_draft() {
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
}

run_brief_to_draft_flow() {
  provision_test_teacher
  obtain_id_token
  run_brief_intake
  trigger_draft_generation
  retrieve_draft
}
