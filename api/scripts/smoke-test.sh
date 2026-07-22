#!/usr/bin/env bash
# Real-backend smoke test: brings up Postgres + a Firebase Auth Emulator via
# compose.smoke.yml, boots api/ against them (Spring profile `smoke`), and
# exercises the real assessment draft-builder endpoints end to end — the exact
# error/happy-path surface task-10/task-11/task-12 (web/) were built against.
#
# The Admin SDK talks to the Firebase Auth Emulator via the standard
# FIREBASE_AUTH_EMULATOR_HOST env var — application code never needs to know
# it isn't talking to real Firebase.
#
# Usage: ./scripts/smoke-test.sh
# Exits 0 if every check passes, 1 otherwise. Always tears down what it started.

set -uo pipefail

cd "$(dirname "$0")/.."

FIREBASE_EMULATOR_HOST="localhost:9099"
API_BASE="http://localhost:8080"
API_LOG="$(mktemp)"
FAILURES=0

api_pid=""

cleanup() {
  echo ""
  echo "=== Tearing down ==="
  if [ -n "$api_pid" ] && kill -0 "$api_pid" 2>/dev/null; then
    kill "$api_pid" 2>/dev/null
    wait "$api_pid" 2>/dev/null
  fi
  pkill -f "spring-boot:run.*profiles=smoke" 2>/dev/null || true
  docker compose -f compose.smoke.yml down >/dev/null 2>&1
  rm -f "$API_LOG"
}
trap cleanup EXIT

pass() { echo "  PASS: $1"; }
fail() { echo "  FAIL: $1"; FAILURES=$((FAILURES + 1)); }

# assert_status <description> <expected_status> <actual_status> [<actual_body> <body_substring>]
assert_status() {
  local desc="$1" expected="$2" actual="$3"
  if [ "$actual" = "$expected" ]; then
    if [ $# -ge 5 ]; then
      if echo "$4" | grep -qF "$5"; then
        pass "$desc (HTTP $actual, body matches)"
      else
        fail "$desc — HTTP $actual as expected but body did not contain '$5': $4"
      fi
    else
      pass "$desc (HTTP $actual)"
    fi
  else
    fail "$desc — expected HTTP $expected, got HTTP $actual (body: ${4:-})"
  fi
}

echo "=== 1. Starting smoke stack (Postgres + Firebase Auth Emulator) ==="
docker compose -f compose.smoke.yml up -d --build

echo "Waiting for services to be healthy..."
for i in $(seq 1 40); do
  status=$(docker compose -f compose.smoke.yml ps --format "{{.Name}}: {{.Health}}" 2>&1)
  if echo "$status" | grep -q "postgres.*healthy" && echo "$status" | grep -q "firebase-emulator.*healthy"; then
    break
  fi
  sleep 3
  if [ "$i" = 40 ]; then
    echo "Services never became healthy:"
    echo "$status"
    exit 1
  fi
done
echo "Both services healthy."

echo ""
echo "=== 2. Starting api/ (profile=smoke, FIREBASE_AUTH_EMULATOR_HOST=$FIREBASE_EMULATOR_HOST) ==="
FIREBASE_AUTH_EMULATOR_HOST="$FIREBASE_EMULATOR_HOST" ./mvnw spring-boot:run -Dspring-boot.run.profiles=smoke > "$API_LOG" 2>&1 &
api_pid=$!

echo "Waiting for the app to start..."
for i in $(seq 1 40); do
  if grep -q "Started GradeOpsApiApplication" "$API_LOG" 2>/dev/null; then
    break
  fi
  if grep -q "APPLICATION FAILED TO START" "$API_LOG" 2>/dev/null; then
    echo "App failed to start:"
    tail -60 "$API_LOG"
    exit 1
  fi
  sleep 3
  if [ "$i" = 40 ]; then
    echo "App never started within timeout:"
    tail -60 "$API_LOG"
    exit 1
  fi
done
echo "App started."

echo ""
echo "=== 3. Creating a test teacher via the emulator, verifying real API auth ==="

EMAIL="smoke-teacher-$(date +%s)@test.com"
SIGNUP=$(curl -s -X POST "http://$FIREBASE_EMULATOR_HOST/identitytoolkit.googleapis.com/v1/accounts:signUp?key=fake-api-key" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"SmokeTest123!\",\"returnSecureToken\":true}")
LOCAL_ID=$(echo "$SIGNUP" | python3 -c "import json,sys; print(json.load(sys.stdin)['localId'])")

# Real Firebase never lets a client self-verify email; the emulator's admin
# bypass (Authorization: Bearer owner) exists specifically for test setup.
curl -s -X POST "http://$FIREBASE_EMULATOR_HOST/identitytoolkit.googleapis.com/v1/accounts:update" \
  -H "Authorization: Bearer owner" -H "Content-Type: application/json" \
  -d "{\"localId\":\"$LOCAL_ID\",\"emailVerified\":true}" >/dev/null

SIGNIN=$(curl -s -X POST "http://$FIREBASE_EMULATOR_HOST/identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=fake-api-key" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"SmokeTest123!\",\"returnSecureToken\":true}")
ID_TOKEN=$(echo "$SIGNIN" | python3 -c "import json,sys; print(json.load(sys.stdin)['idToken'])")

REGISTER_STATUS=$(curl -s -o /tmp/smoke_register.json -w "%{http_code}" -X POST "$API_BASE/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"idToken\":\"$ID_TOKEN\",\"firstName\":\"Smoke\",\"lastName\":\"Teacher\"}")
assert_status "register with a real emulator-issued idToken" "200" "$REGISTER_STATUS" "$(cat /tmp/smoke_register.json)" '"created":true'

echo ""
echo "=== 4. Real assessment + draft error surface (task-10/11/12's error mapping) ==="

CREATE_STATUS=$(curl -s -o /tmp/smoke_create.json -w "%{http_code}" -X POST "$API_BASE/api/v1/assessments" \
  -H "Authorization: Bearer $ID_TOKEN" -H "Content-Type: application/json" \
  -d '{"learningGoal":"Entender recursividad","topic":"Recursion","level":"Intermedio","duration":"45 min","language":"Java"}')
ASSESSMENT_ID=$(python3 -c "import json; print(json.load(open('/tmp/smoke_create.json'))['assessmentId'])" 2>/dev/null || echo "")
assert_status "create assessment brief" "201" "$CREATE_STATUS" "$(cat /tmp/smoke_create.json)" "assessmentId"

if [ -n "$ASSESSMENT_ID" ]; then
  STATUS=$(curl -s -o /tmp/r.json -w "%{http_code}" -H "Authorization: Bearer $ID_TOKEN" "$API_BASE/api/v1/assessments/$ASSESSMENT_ID/draft")
  assert_status "GET draft before generation -> 404 NOT_FOUND" "404" "$STATUS" "$(cat /tmp/r.json)" '"error":"NOT_FOUND"'

  STATUS=$(curl -s -o /tmp/r.json -w "%{http_code}" -X PATCH -H "Authorization: Bearer $ID_TOKEN" -H "Content-Type: application/json" \
    -d '{"title":"x"}' "$API_BASE/api/v1/assessments/$ASSESSMENT_ID/draft")
  assert_status "PATCH draft with no prior draft -> 422 APPLICATION_ERROR" "422" "$STATUS" "$(cat /tmp/r.json)" '"error":"APPLICATION_ERROR"'

  STATUS=$(curl -s -o /tmp/r.json -w "%{http_code}" -X POST -H "Authorization: Bearer $ID_TOKEN" -H "Content-Type: application/json" \
    -d '{"adjustmentNotes":"x"}' "$API_BASE/api/v1/assessments/$ASSESSMENT_ID/draft/regenerate")
  assert_status "regenerate with no prior draft -> 422 APPLICATION_ERROR" "422" "$STATUS" "$(cat /tmp/r.json)" '"error":"APPLICATION_ERROR"'

  STATUS=$(curl -s -o /tmp/r.json -w "%{http_code}" -X POST -H "Authorization: Bearer $ID_TOKEN" "$API_BASE/api/v1/assessments/$ASSESSMENT_ID/draft")
  assert_status "generateDraft with agents/ unreachable -> 503 AGENT_CALL_FAILED" "503" "$STATUS" "$(cat /tmp/r.json)" '"error":"AGENT_CALL_FAILED"'

  STATUS=$(curl -s -o /tmp/r.json -w "%{http_code}" -H "Authorization: Bearer $ID_TOKEN" "$API_BASE/api/v1/assessments/00000000-0000-0000-0000-000000000000/draft")
  assert_status "GET draft for a nonexistent assessment -> 404 (same shape as 'no draft yet')" "404" "$STATUS" "$(cat /tmp/r.json)" '"error":"NOT_FOUND"'

  echo ""
  echo "=== 5. Seeding a real draft row to exercise the happy path (agents/ isn't running) ==="
  docker compose -f compose.smoke.yml exec -T postgres psql -U gradeops -d gradeops -v ON_ERROR_STOP=1 -c "
    INSERT INTO assessment_drafts (assessment_id, version_number, title, context, instructions, objectives, deliverables, constraints)
    VALUES ('$ASSESSMENT_ID', 1, 'Recursividad: Fibonacci', 'Evaluación práctica', 'Implementa una función recursiva.', '[\"Comprender recursividad\"]'::jsonb, '[\"Archivo .py\"]'::jsonb, '[\"No usar librerías externas\"]'::jsonb);
  " >/dev/null
  echo "Seeded."

  STATUS=$(curl -s -o /tmp/r.json -w "%{http_code}" -H "Authorization: Bearer $ID_TOKEN" "$API_BASE/api/v1/assessments/$ASSESSMENT_ID/draft")
  assert_status "GET draft happy path" "200" "$STATUS" "$(cat /tmp/r.json)" '"versionNumber":1'

  STATUS=$(curl -s -o /tmp/r.json -w "%{http_code}" -H "Authorization: Bearer $ID_TOKEN" "$API_BASE/api/v1/assessments/$ASSESSMENT_ID/draft/versions")
  assert_status "GET draft versions happy path" "200" "$STATUS" "$(cat /tmp/r.json)" '"versionNumber":1'

  STATUS=$(curl -s -o /tmp/r.json -w "%{http_code}" -X PATCH -H "Authorization: Bearer $ID_TOKEN" -H "Content-Type: application/json" \
    -d '{"title":"Recursividad: Fibonacci (editado en smoke test)"}' "$API_BASE/api/v1/assessments/$ASSESSMENT_ID/draft")
  assert_status "PATCH draft happy path — in-place edit, same version" "200" "$STATUS" "$(cat /tmp/r.json)" '"versionNumber":1'

  STATUS=$(curl -s -o /tmp/r.json -w "%{http_code}" -H "Authorization: Bearer $ID_TOKEN" "$API_BASE/api/v1/assessments/$ASSESSMENT_ID/draft")
  assert_status "GET draft confirms the PATCH persisted" "200" "$STATUS" "$(cat /tmp/r.json)" "editado en smoke test"

  STATUS=$(curl -s -o /tmp/r.json -w "%{http_code}" -X PATCH -H "Authorization: Bearer $ID_TOKEN" -H "Content-Type: application/json" \
    -d '{"title":""}' "$API_BASE/api/v1/assessments/$ASSESSMENT_ID/draft")
  assert_status "PATCH with blank title -> 422 array FieldErrorResponse[]" "422" "$STATUS" "$(cat /tmp/r.json)" '"field":"title"'

  STATUS=$(curl -s -o /tmp/r.json -w "%{http_code}" -X POST -H "Authorization: Bearer $ID_TOKEN" -H "Content-Type: application/json" \
    -d '{"adjustmentNotes":""}' "$API_BASE/api/v1/assessments/$ASSESSMENT_ID/draft/regenerate")
  assert_status "regenerate with blank notes -> 422 array FieldErrorResponse[]" "422" "$STATUS" "$(cat /tmp/r.json)" '"field":"adjustmentNotes"'
else
  fail "could not extract assessmentId — skipping draft endpoint checks"
fi

echo ""
if [ "$FAILURES" -eq 0 ]; then
  echo "=== ALL SMOKE CHECKS PASSED ==="
  exit 0
else
  echo "=== $FAILURES SMOKE CHECK(S) FAILED ==="
  exit 1
fi
