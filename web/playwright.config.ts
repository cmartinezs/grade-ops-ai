import { defineConfig, devices } from "@playwright/test";

// Real end-to-end suite: exercises the actual UI against a real local api/ + Postgres +
// Firebase Auth Emulator stack (see scripts/e2e-test.sh, which boots and tears down that
// stack, then runs this suite). Not part of `npm test` (jest, mocked) — run via
// `npm run test:e2e`, normally through the orchestration script rather than directly.
export default defineConfig({
  testDir: "./e2e",
  fullyParallel: false,
  retries: 0,
  timeout: 30_000,
  expect: { timeout: 10_000 },
  reporter: [["list"]],
  use: {
    baseURL: process.env.E2E_BASE_URL ?? "http://localhost:3000",
    // This WSL2 environment has no usable bundled Chromium — Playwright must drive the
    // real installed Chrome channel instead (see task-13's notes on this exact issue).
    channel: "chrome",
    trace: "retain-on-failure",
  },
  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"], channel: "chrome" },
    },
  ],
});
