import { test, expect } from "@playwright/test";
import { waitForPath } from "./support/waitForPath";

// Real UI, real api/, real Firebase Auth Emulator — no fixture shortcut here on purpose:
// this spec is the one place that still drives the actual /register form, independent of
// the auth fixture's programmatic setup, so a UI regression here can't hide behind the
// fixture bypassing the form.
test.describe("Teacher registration", () => {
  test("submitting the form creates a real Teacher row and redirects to /verify-email", async ({ page }) => {
    const email = `e2e-reg-${Date.now()}@test.com`;

    await page.goto("/register");
    await page.getByLabel("Nombres", { exact: true }).fill("Ada");
    await page.getByLabel("Apellidos", { exact: true }).fill("Lovelace");
    await page.getByLabel("Correo electrónico", { exact: true }).fill(email);
    await page.getByLabel("Contraseña", { exact: true }).fill("E2eTest123!");

    const registerResponsePromise = page.waitForResponse(
      (res) => res.url().includes("/api/v1/auth/register") && res.request().method() === "POST"
    );
    await page.getByRole("button", { name: /crear cuenta/i }).click();

    const registerResponse = await registerResponsePromise;
    // Regression guard for task-13's Bug 2 (EmailVerifiedFilter whitelisting the wrong,
    // bare path): a fresh registration's token is always emailVerified=false, so this
    // endpoint must accept it rather than 401ing.
    expect(registerResponse.status()).toBe(200);
    const body = await registerResponse.json();
    expect(body.created).toBe(true);

    await waitForPath(page, "/verify-email");
  });
});
