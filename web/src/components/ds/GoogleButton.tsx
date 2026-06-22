"use client";

import { useState } from "react";
import { GoogleAuthProvider, signInWithPopup } from "firebase/auth";
import { auth } from "@/lib/firebase/client";

interface GoogleButtonProps {
  onSuccess: (idToken: string, displayName: string) => Promise<void>;
  loading?: boolean;
  disabled?: boolean;
}

function Spinner() {
  return (
    <span
      style={{
        width: 14,
        height: 14,
        border: "2px solid rgba(0,0,0,0.15)",
        borderTopColor: "var(--text-body)",
        borderRadius: "50%",
        display: "inline-block",
        animation: "spin 0.7s linear infinite",
        flexShrink: 0,
      }}
    />
  );
}

const GoogleLogoSVG = (
  <svg
    aria-hidden="true"
    xmlns="http://www.w3.org/2000/svg"
    viewBox="0 0 48 48"
    style={{ width: 18, height: 18, flexShrink: 0 }}
  >
    <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z" />
    <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z" />
    <path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z" />
    <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z" />
    <path fill="none" d="M0 0h48v48H0z" />
  </svg>
);

export default function GoogleButton({ onSuccess, loading: externalLoading, disabled }: GoogleButtonProps) {
  const [internalLoading, setInternalLoading] = useState(false);
  const [hovered, setHovered] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const isLoading = internalLoading || externalLoading;
  const isDisabled = isLoading || disabled;

  async function handleClick() {
    setError(null);
    setHovered(false);
    setInternalLoading(true);
    try {
      const result = await signInWithPopup(auth, new GoogleAuthProvider());
      const idToken = await result.user.getIdToken();
      const displayName = result.user.displayName ?? result.user.email ?? "";
      await onSuccess(idToken, displayName);
    } catch (err: unknown) {
      const code = err && typeof err === "object" && "code" in err ? (err as { code: string }).code : "";
      if (code === "auth/popup-closed-by-user" || code === "auth/cancelled-popup-request") {
        return;
      }
      console.error(err);
      setError("No pudimos completar el inicio de sesión. Intenta de nuevo.");
    } finally {
      setInternalLoading(false);
    }
  }

  return (
    <div style={{ width: "100%" }}>
      <button
        type="button"
        onClick={handleClick}
        disabled={isDisabled}
        onMouseEnter={() => setHovered(true)}
        onMouseLeave={() => setHovered(false)}
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          gap: 10,
          width: "100%",
          height: 46,
          background: hovered && !isDisabled ? "var(--surface-sunken)" : "var(--surface-card)",
          border: `1px solid ${error ? "var(--danger-200)" : "var(--border-default)"}`,
          borderRadius: "var(--radius-md)",
          fontFamily: "var(--font-sans)",
          fontWeight: 600,
          fontSize: "var(--text-md)",
          color: "var(--text-body)",
          cursor: isDisabled ? "not-allowed" : "pointer",
          opacity: isDisabled ? 0.6 : 1,
          transition: "background 120ms",
          boxSizing: "border-box",
        }}
      >
        {isLoading ? <Spinner /> : GoogleLogoSVG}
        <span>{isLoading ? "Conectando…" : "Continuar con Google"}</span>
      </button>
      {error && (
        <p
          role="alert"
          style={{
            margin: "6px 0 0",
            fontSize: "var(--text-sm)",
            color: "var(--danger-600)",
            textAlign: "center",
          }}
        >
          {error}
        </p>
      )}
    </div>
  );
}
