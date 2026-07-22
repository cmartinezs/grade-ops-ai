"use client";

import { useState } from "react";

interface UseRegenerateSectionParams {
  isRegenerating: boolean;
  fieldError: string | null;
  agentError: string | null;
  onRegenerate: (adjustmentNotes: string) => void;
}

const REQUIRED_MESSAGE = "Ingresa notas de ajuste antes de regenerar.";

export function useRegenerateSection({ isRegenerating, fieldError, agentError, onRegenerate }: UseRegenerateSectionParams) {
  const [adjustmentNotes, setAdjustmentNotes] = useState("");
  const [localError, setLocalError] = useState<string | null>(null);

  function handleChange(event: React.ChangeEvent<HTMLTextAreaElement>) {
    setAdjustmentNotes(event.target.value);
    if (localError) setLocalError(null);
  }

  function handleRegenerate(event: React.FormEvent) {
    event.preventDefault();
    const trimmed = adjustmentNotes.trim();
    if (trimmed.length === 0) {
      setLocalError(REQUIRED_MESSAGE);
      return;
    }
    onRegenerate(trimmed);
  }

  return {
    adjustmentNotes,
    handleChange,
    handleRegenerate,
    fieldError: fieldError ?? localError,
    agentError,
    isDisabled: isRegenerating,
    isRegenerating,
  };
}
