import { test, expect } from "./fixtures/auth";
import { API_BASE_URL } from "./support/firebaseEmulator";
import { seedDraftVersion } from "./support/seedDraft";

// agents/ (Gemini) is not available in this local/CI environment — a documented ceiling
// from task-06/task-12/task-13, not a defect. Every test here creates the assessment brief
// directly via the real api/ endpoint (bypassing the intake form, which would otherwise
// block on the same unreachable agent call) and seeds a draft row via SQL, exactly like
// task-13's manual walkthrough, so the Draft Builder screen's real render/edit/save/refresh
// and regenerate-error-handling seams are still exercised against the real stack.
async function createSeededAssessment(
  request: import("@playwright/test").APIRequestContext,
  idToken: string
): Promise<string> {
  const createRes = await request.post(`${API_BASE_URL}/api/v1/assessments`, {
    headers: { Authorization: `Bearer ${idToken}` },
    data: {
      learningGoal: "Entender recursividad",
      topic: "Recursion",
      level: "Intermedio",
      duration: "45 min",
      language: "Java",
    },
  });
  if (!createRes.ok()) {
    throw new Error(`Failed to create seeded assessment: ${createRes.status()} ${await createRes.text()}`);
  }
  const { assessmentId } = await createRes.json();
  seedDraftVersion(assessmentId);
  return assessmentId;
}

test.describe("Draft Builder screen", () => {
  test("renders a seeded draft, persists an edit across a real page refresh", async ({
    request,
    teacher,
    authenticatedPage: page,
  }) => {
    const assessmentId = await createSeededAssessment(request, teacher.idToken);

    await page.goto(`/assessments/${assessmentId}/draft`);
    await expect(page.getByLabel("Título")).toHaveValue("Recursividad: Fibonacci");

    const editedTitle = "Recursividad: Fibonacci (editado en e2e)";
    await page.getByLabel("Título").fill(editedTitle);

    // Regression guard for task-13's Bug 3 (CORS allowedMethods missing PATCH, which 403'd
    // every real-browser save) and Bug 1 (the /api prefix rewrite bug).
    const saveResponsePromise = page.waitForResponse(
      (res) => res.url().includes(`/api/v1/assessments/${assessmentId}/draft`) && res.request().method() === "PATCH"
    );
    await page.getByRole("button", { name: /guardar cambios/i }).click();
    const saveResponse = await saveResponsePromise;
    expect(saveResponse.status()).toBe(200);

    await page.reload();
    await expect(page.getByLabel("Título")).toHaveValue(editedTitle);
  });

  test("regenerating with agents/ unreachable surfaces the expected error and preserves the existing draft", async ({
    request,
    teacher,
    authenticatedPage: page,
  }) => {
    const assessmentId = await createSeededAssessment(request, teacher.idToken);

    await page.goto(`/assessments/${assessmentId}/draft`);
    await page.getByLabel("Notas de ajuste").fill("Agrega más ejemplos.");
    await page.getByRole("button", { name: /regenerar con ia/i }).click();

    // Next.js's own route-announcer div also carries role="alert" (empty text), so scope
    // to the one that actually contains the expected message.
    await expect(page.getByRole("alert").filter({ hasText: "no está disponible" })).toBeVisible();
    // The pre-existing draft must survive an unreachable-agent regeneration attempt.
    await expect(page.getByLabel("Título")).toHaveValue("Recursividad: Fibonacci");
  });
});
