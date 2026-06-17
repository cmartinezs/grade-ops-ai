import { auth } from "@/lib/firebase/client";
import { onAuthStateChanged, signOut } from "firebase/auth";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL ?? "";

function getToken(): Promise<string | undefined> {
  return new Promise((resolve) => {
    if (auth.currentUser) {
      auth.currentUser.getIdToken().then(resolve).catch(() => resolve(undefined));
      return;
    }
    const unsubscribe = onAuthStateChanged(auth, (user) => {
      unsubscribe();
      if (user) {
        user.getIdToken().then(resolve).catch(() => resolve(undefined));
      } else {
        resolve(undefined);
      }
    });
  });
}

export async function apiClient(
  path: string,
  options: RequestInit = {}
): Promise<Response> {
  const token = await getToken();

  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  });

  if (res.status === 401) {
    const body = await res.clone().json().catch(() => ({}));
    if (body?.error === "EMAIL_NOT_VERIFIED") {
      if (typeof window !== "undefined") {
        window.location.replace("/verify-email");
      }
    } else {
      await signOut(auth).catch(() => {});
      if (typeof window !== "undefined") {
        window.location.replace("/login?reason=expired");
      }
    }
  }

  return res;
}
