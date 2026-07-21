import { test as base, type Page } from "@playwright/test";
import { createVerifiedTeacher, type EmulatorTeacher } from "../support/firebaseEmulator";
import { waitForPath } from "../support/waitForPath";

interface AuthFixtures {
  /** A real, already-registered teacher (Firebase Auth Emulator + real api/ Teacher row). */
  teacher: EmulatorTeacher;
  /** A page already logged in as `teacher`, via the real /login form — not a storage-state
   *  shortcut — so every consumer also gets a live regression check that login itself works. */
  authenticatedPage: Page;
}

// Reusable across any future authenticated-screen spec — the whole point of extracting this
// out of task-13's one-off manual walkthrough (see task-15's Objective). Import `test`/`expect`
// from this module instead of "@playwright/test" directly in any spec needing a logged-in page.
export const test = base.extend<AuthFixtures>({
  teacher: async ({ request }, use) => {
    const teacher = await createVerifiedTeacher(request);
    await use(teacher);
  },

  authenticatedPage: async ({ page, teacher }, use) => {
    await page.goto("/login");
    await page.getByLabel("Correo electrónico", { exact: true }).fill(teacher.email);
    await page.getByLabel("Contraseña", { exact: true }).fill(teacher.password);
    await page.getByRole("button", { name: /iniciar sesión/i }).click();
    await waitForPath(page, "/dashboard");
    await use(page);
  },
});

export { expect } from "@playwright/test";
