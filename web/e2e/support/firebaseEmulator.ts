import type { APIRequestContext } from "@playwright/test";

// Mirrors api/scripts/smoke-test.sh's own emulator calls and task-13's manual walkthrough —
// same emulator, same admin-bypass mechanism, kept in one place so every spec/fixture that
// needs a real authenticated teacher uses the identical, already-verified sequence.
const FIREBASE_EMULATOR_HOST = process.env.E2E_FIREBASE_EMULATOR_HOST ?? "localhost:9099";
const FIREBASE_API_KEY = process.env.NEXT_PUBLIC_FIREBASE_API_KEY ?? "fake-api-key";
const API_BASE_URL = process.env.E2E_API_BASE_URL ?? "http://localhost:8080";

export interface EmulatorTeacher {
  email: string;
  password: string;
  idToken: string;
  localId: string;
}

/**
 * Creates a Firebase Auth Emulator user, force-verifies its email via the emulator's
 * admin-only bypass (real Firebase never lets a client self-verify — this bypass exists
 * specifically for test setup), signs in fresh, and registers the resulting idToken against
 * the real api/ backend so a real Teacher row exists. Returns credentials a spec can use to
 * either drive the login UI or call api/ endpoints directly via the idToken.
 */
export async function createVerifiedTeacher(
  request: APIRequestContext,
  overrides: { firstName?: string; lastName?: string } = {}
): Promise<EmulatorTeacher> {
  const email = `e2e-${Date.now()}-${Math.floor(Math.random() * 1_000_000)}@test.com`;
  const password = "E2eTest123!";

  const signUp = await request.post(
    `http://${FIREBASE_EMULATOR_HOST}/identitytoolkit.googleapis.com/v1/accounts:signUp?key=${FIREBASE_API_KEY}`,
    { data: { email, password, returnSecureToken: true } }
  );
  if (!signUp.ok()) {
    throw new Error(`Emulator signUp failed: ${signUp.status()} ${await signUp.text()}`);
  }
  const { localId } = await signUp.json();

  await request.post(`http://${FIREBASE_EMULATOR_HOST}/identitytoolkit.googleapis.com/v1/accounts:update`, {
    headers: { Authorization: "Bearer owner" },
    data: { localId, emailVerified: true },
  });

  const signIn = await request.post(
    `http://${FIREBASE_EMULATOR_HOST}/identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${FIREBASE_API_KEY}`,
    { data: { email, password, returnSecureToken: true } }
  );
  if (!signIn.ok()) {
    throw new Error(`Emulator signIn failed: ${signIn.status()} ${await signIn.text()}`);
  }
  const { idToken } = await signIn.json();

  const registerRes = await request.post(`${API_BASE_URL}/api/v1/auth/register`, {
    data: {
      idToken,
      firstName: overrides.firstName ?? "Ada",
      lastName: overrides.lastName ?? "Lovelace",
    },
  });
  if (!registerRes.ok()) {
    throw new Error(`api/ register failed for e2e teacher: ${registerRes.status()} ${await registerRes.text()}`);
  }

  return { email, password, idToken, localId };
}

export { API_BASE_URL };
