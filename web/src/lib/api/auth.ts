import { apiClient } from "./client";

export interface ApiError {
  error: string;
  message?: string;
}

export class PasswordResetError extends Error {
  constructor(public readonly code: string) {
    super(code);
  }
}

export interface RegisterTeacherResponse {
  firebaseUid: string;
}

export async function registerTeacher(
  idToken: string,
  firstName: string,
  lastName: string
): Promise<RegisterTeacherResponse> {
  const res = await apiClient("/api/v1/auth/register", {
    method: "POST",
    body: JSON.stringify({ idToken, firstName, lastName }),
  });

  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.error ?? `Registration failed: ${res.status}`);
  }

  return res.json();
}

export async function signOutApi(token: string): Promise<void> {
  await apiClient("/api/v1/auth/sign-out", {
    method: "POST",
    headers: { Authorization: `Bearer ${token}` },
  });
}

export async function forgotPassword(email: string): Promise<void> {
  await apiClient("/api/v1/auth/forgot-password", {
    method: "POST",
    body: JSON.stringify({ email }),
  });
  // Always succeeds — backend returns 200 regardless of whether email is registered
}

export async function resetPassword(
  code: string,
  body: { email: string; password: string; passwordRepeat: string }
): Promise<void> {
  const res = await apiClient(
    `/api/v1/auth/reset-password?code=${encodeURIComponent(code)}`,
    {
      method: "PUT",
      body: JSON.stringify(body),
    }
  );

  if (!res.ok) {
    const data: ApiError = await res.json().catch(() => ({ error: "UNKNOWN_ERROR" }));
    throw new PasswordResetError(data.error);
  }
}
