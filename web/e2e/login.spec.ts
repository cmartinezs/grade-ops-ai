import { test, expect } from "@playwright/test";
import { createVerifiedTeacher } from "./support/firebaseEmulator";
import { waitForPath } from "./support/waitForPath";

test.describe("Teacher login", () => {
  test("signing in with a verified account reaches /dashboard and can call the real api/", async ({
    page,
    request,
  }) => {
    const teacher = await createVerifiedTeacher(request);

    await page.goto("/login");
    await page.getByLabel("Correo electrónico", { exact: true }).fill(teacher.email);
    await page.getByLabel("Contraseña", { exact: true }).fill(teacher.password);

    // The dashboard fetches GET /api/v1/assessments on load — the exact call that surfaced
    // task-13's Bug 1 (next.config.ts's rewrite stripping the /api prefix, turning every
    // browser-originated call into a 500). Assert it resolves cleanly, not just that the
    // page navigated.
    const assessmentsResponsePromise = page.waitForResponse(
      (res) => res.url().includes("/api/v1/assessments") && res.request().method() === "GET"
    );
    await page.getByRole("button", { name: /iniciar sesión/i }).click();

    await waitForPath(page, "/dashboard");
    const assessmentsResponse = await assessmentsResponsePromise;
    expect(assessmentsResponse.status()).toBe(200);
  });
});
