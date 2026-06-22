"use client";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { auth } from "@/lib/firebase/client";
import { signOut as firebaseSignOut } from "firebase/auth";
import IconButton from "@/components/ds/IconButton";
import LucideIcon from "@/components/ds/LucideIcon";

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
    <IconButton
      label="Cerrar sesión"
      size="sm"
      onClick={handleSignOut}
      disabled={loading}
      style={{ opacity: loading ? 0.5 : 1 }}
    >
      <LucideIcon name="log-out" size={16} />
    </IconButton>
  );
}
