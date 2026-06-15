"use client";

import { useState, useEffect, useRef } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { sendEmailVerification } from "firebase/auth";
import { auth } from "@/lib/firebase/client";

const RESEND_COOLDOWN_SECONDS = 60;

export default function VerifyEmailPage() {
  const router = useRouter();
  const searchParams = useSearchParams();

  const [cooldown, setCooldown] = useState(0);
  const [verifyError, setVerifyError] = useState<string | null>(null);
  const [checkingVerification, setCheckingVerification] = useState(false);
  const cooldownRef = useRef<ReturnType<typeof setInterval> | null>(null);

  // On mount: send verification email once (unless ?sent param is already present).
  useEffect(() => {
    const alreadySent = searchParams.get("sent") === "1";
    if (!alreadySent) {
      const currentUser = auth.currentUser;
      if (currentUser) {
        sendEmailVerification(currentUser).catch(() => {
          // Ignore — Firebase may have already sent one recently.
        });
        // Prevent re-send on refresh.
        router.replace("/verify-email?sent=1");
      }
    }
  }, []); // eslint-disable-line react-hooks/exhaustive-deps

  // Countdown ticker for the resend cooldown.
  useEffect(() => {
    if (cooldown <= 0) return;

    cooldownRef.current = setInterval(() => {
      setCooldown((prev) => {
        if (prev <= 1) {
          clearInterval(cooldownRef.current!);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => {
      if (cooldownRef.current) clearInterval(cooldownRef.current);
    };
  }, [cooldown]);

  async function handleResend() {
    const currentUser = auth.currentUser;
    if (!currentUser) return;

    await sendEmailVerification(currentUser).catch(() => {
      // Firebase server-side rate limiting will reject excessive requests gracefully.
    });
    setCooldown(RESEND_COOLDOWN_SECONDS);
  }

  async function handleCheckVerified() {
    const currentUser = auth.currentUser;
    if (!currentUser) return;

    setCheckingVerification(true);
    setVerifyError(null);

    try {
      await currentUser.reload();
      if (currentUser.emailVerified) {
        router.push("/dashboard");
      } else {
        setVerifyError("Email not verified yet. Check your inbox.");
      }
    } finally {
      setCheckingVerification(false);
    }
  }

  async function handleSignOut() {
    await auth.signOut();
    router.push("/login");
  }

  return (
    <main className="flex min-h-screen items-center justify-center bg-gray-50 px-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-sm">
        <h1 className="mb-4 text-2xl font-semibold text-gray-900">
          Verify your email
        </h1>

        <p className="mb-6 text-sm text-gray-600">
          We sent a verification link to your email address. Check your inbox
          and click the link to activate your account.
        </p>

        <div className="space-y-3">
          <button
            type="button"
            onClick={handleResend}
            disabled={cooldown > 0}
            className="w-full rounded-lg border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {cooldown > 0 ? `Resend in ${cooldown}s` : "Resend email"}
          </button>

          <button
            type="button"
            onClick={handleCheckVerified}
            disabled={checkingVerification}
            className="w-full rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
          >
            {checkingVerification ? "Checking…" : "I've verified my email"}
          </button>
        </div>

        {verifyError && (
          <p role="alert" className="mt-4 text-sm text-red-600">
            {verifyError}
          </p>
        )}

        <p className="mt-6 text-center text-sm text-gray-600">
          <button
            type="button"
            onClick={handleSignOut}
            className="text-blue-600 hover:underline"
          >
            Sign out
          </button>
        </p>
      </div>
    </main>
  );
}
