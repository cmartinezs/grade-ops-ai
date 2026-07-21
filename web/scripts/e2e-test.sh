#!/usr/bin/env bash
# Deterministic, non-interactive replacement for task-13's manual Playwright-MCP walkthrough
# (see task-15). Boots the real stack — Postgres + Firebase Auth Emulator (via
# ../api/compose.smoke.yml) + api/ (profile=smoke) + web/'s own dev server — waits for
# readiness, runs the real-browser Playwright suite in e2e/, and always tears down.
#
# Prerequisites (not automatable from inside this script — fails fast with a clear message
# if missing rather than hanging):
#   - Docker Desktop's WSL2 integration must be running (`docker info` must succeed).
#   - A real Chrome/Chromium binary at the "chrome" channel Playwright expects — bundled
#     Chromium alone is not enough in this environment (see task-13's notes); install via
#     `google-chrome-stable` if missing.
#
# Usage: ./scripts/e2e-test.sh
# Exits 0 if the full suite passes, 1 otherwise. Always tears down what it started.

set -uo pipefail

cd "$(dirname "$0")/.."
API_DIR="../api"

FIREBASE_EMULATOR_HOST="localhost:9099"
API_BASE="http://localhost:8080"
WEB_BASE="http://localhost:3000"
API_LOG="$(mktemp)"
WEB_LOG="$(mktemp)"

api_pid=""
web_pid=""

cleanup() {
  echo ""
  echo "=== Tearing down ==="
  if [ -n "$web_pid" ] && kill -0 "$web_pid" 2>/dev/null; then
    kill "$web_pid" 2>/dev/null
    wait "$web_pid" 2>/dev/null
  fi
  pkill -f "next dev" 2>/dev/null || true
  if [ -n "$api_pid" ] && kill -0 "$api_pid" 2>/dev/null; then
    kill "$api_pid" 2>/dev/null
    wait "$api_pid" 2>/dev/null
  fi
  pkill -f "spring-boot:run.*profiles=smoke" 2>/dev/null || true
  (cd "$API_DIR" && docker compose -f compose.smoke.yml down >/dev/null 2>&1)
  rm -f "$API_LOG" "$WEB_LOG"
}
trap cleanup EXIT

echo "=== 0. Checking prerequisites ==="
if ! docker info >/dev/null 2>&1; then
  echo "FAIL: Docker is not reachable (docker info failed)."
  echo "      If using Docker Desktop on WSL2, restart it and retry."
  exit 1
fi
if [ ! -x /opt/google/chrome/chrome ] && ! command -v google-chrome-stable >/dev/null 2>&1; then
  echo "FAIL: No real Chrome binary found (playwright.config.ts uses channel: \"chrome\")."
  echo "      Install google-chrome-stable, then retry."
  exit 1
fi
echo "OK."

echo ""
echo "=== 1. Starting smoke stack (Postgres + Firebase Auth Emulator) ==="
(cd "$API_DIR" && docker compose -f compose.smoke.yml up -d --build)

echo "Waiting for services to be healthy..."
for i in $(seq 1 40); do
  status=$(cd "$API_DIR" && docker compose -f compose.smoke.yml ps --format "{{.Name}}: {{.Health}}" 2>&1)
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
(cd "$API_DIR" && FIREBASE_AUTH_EMULATOR_HOST="$FIREBASE_EMULATOR_HOST" ./mvnw spring-boot:run -Dspring-boot.run.profiles=smoke > "$API_LOG" 2>&1) &
api_pid=$!

echo "Waiting for api/ to start..."
for i in $(seq 1 40); do
  if grep -q "Started GradeOpsApiApplication" "$API_LOG" 2>/dev/null; then
    break
  fi
  if grep -q "APPLICATION FAILED TO START" "$API_LOG" 2>/dev/null; then
    echo "api/ failed to start:"
    tail -60 "$API_LOG"
    exit 1
  fi
  sleep 3
  if [ "$i" = 40 ]; then
    echo "api/ never started within timeout:"
    tail -60 "$API_LOG"
    exit 1
  fi
done
echo "api/ started."

echo ""
echo "=== 3. Starting web/ (next dev, pointed at the Firebase Auth Emulator) ==="
# Exported directly to the shell rather than written to .env.local — Next.js inlines
# NEXT_PUBLIC_* vars from process.env at dev-server-start time either way, and this avoids
# ever touching the developer's own .env.local (task-13 needed a manual backup/restore
# dance because it edited that file directly; this script never does).
export NEXT_PUBLIC_FIREBASE_API_KEY="fake-api-key"
export NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN="demo-gradeops-smoke.firebaseapp.com"
export NEXT_PUBLIC_FIREBASE_PROJECT_ID="demo-gradeops-smoke"
export NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET="demo-gradeops-smoke.firebasestorage.app"
export NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID="000000000000"
export NEXT_PUBLIC_FIREBASE_APP_ID="1:000000000000:web:0000000000000000000000"
export NEXT_PUBLIC_FIREBASE_MEASUREMENT_ID="G-XXXXXXXXXX"
export NEXT_PUBLIC_FIREBASE_AUTH_EMULATOR_HOST="$FIREBASE_EMULATOR_HOST"

npm run dev > "$WEB_LOG" 2>&1 &
web_pid=$!

echo "Waiting for web/ to respond..."
for i in $(seq 1 40); do
  if curl -s -o /dev/null -w "%{http_code}" "$WEB_BASE/login" 2>/dev/null | grep -q "200"; then
    break
  fi
  sleep 2
  if [ "$i" = 40 ]; then
    echo "web/ never became reachable:"
    tail -60 "$WEB_LOG"
    exit 1
  fi
done
echo "web/ is reachable."

echo ""
echo "=== 4. Running the Playwright suite (e2e/) ==="
npx playwright test
suite_status=$?

echo ""
if [ "$suite_status" -eq 0 ]; then
  echo "=== E2E SUITE PASSED ==="
else
  echo "=== E2E SUITE FAILED (exit $suite_status) ==="
  echo "--- api/ log tail ---"
  tail -40 "$API_LOG"
  echo "--- web/ log tail ---"
  tail -40 "$WEB_LOG"
fi
exit "$suite_status"
