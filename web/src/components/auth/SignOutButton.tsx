"use client";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { auth } from "@/lib/firebase/client";
import { signOut as firebaseSignOut } from "firebase/auth";

export default function SignOutButton() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);

  async function handleSignOut() {
    setLoading(true);
    try {
      const token = await auth.currentUser?.getIdToken();
      // Best-effort server-side revocation (3s timeout)
      await Promise.race([
        fetch("/api/auth/sign-out", {
          method: "POST",
          headers: token ? { Authorization: `Bearer ${token}` } : {},
        }),
        new Promise((_, reject) => setTimeout(() => reject(new Error("timeout")), 3000)),
      ]).catch(() => {}); // swallow — always sign out client-side
    } finally {
      await firebaseSignOut(auth);
      router.replace("/login");
    }
  }

  return (
    <button
      onClick={handleSignOut}
      disabled={loading}
      className="text-sm text-gray-600 hover:text-gray-900 disabled:opacity-50"
    >
      {loading ? "Signing out…" : "Sign out"}
    </button>
  );
}
