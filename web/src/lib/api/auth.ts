export interface RegisterTeacherResponse {
  firebaseUid: string;
}

export async function registerTeacher(
  idToken: string,
  name: string
): Promise<RegisterTeacherResponse> {
  const res = await fetch("/api/auth/register", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ idToken, name }),
  });

  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.error ?? `Registration failed: ${res.status}`);
  }

  return res.json();
}

export async function signOutApi(token: string): Promise<void> {
  await fetch("/api/auth/sign-out", {
    method: "POST",
    headers: { Authorization: `Bearer ${token}` },
  });
}
