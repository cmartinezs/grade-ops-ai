import { apiClient } from "./client";

export interface RegisterTeacherResponse {
  firebaseUid: string;
}

export async function registerTeacher(
  idToken: string,
  name: string
): Promise<RegisterTeacherResponse> {
  const res = await apiClient("/api/v1/auth/register", {
    method: "POST",
    body: JSON.stringify({ idToken, name }),
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
