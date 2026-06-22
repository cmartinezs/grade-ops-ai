"use client";

import GoogleButton from "@/components/ds/GoogleButton";

interface Props {
  label?: string;
  onSuccess: (idToken: string, displayName: string) => Promise<void>;
}

export default function GoogleSignInButton({ onSuccess }: Props) {
  return <GoogleButton onSuccess={onSuccess} />;
}
