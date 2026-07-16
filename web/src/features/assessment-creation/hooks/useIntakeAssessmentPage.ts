"use client";

import { useState } from "react";
import type { BriefFormValues } from "../schemas/briefSchema";

type SubmitState =
  | { status: "idle" }
  | { status: "submitting" }
  | { status: "success" }
  | { status: "error"; message: string };

export interface IntakeAssessmentPageViewModel {
  isSubmitting: boolean;
  serverError: string | null;
  fieldErrors: Partial<Record<keyof BriefFormValues, string>> | null;
  handleSubmit: (values: BriefFormValues) => void;
}

export function useIntakeAssessmentPage(): IntakeAssessmentPageViewModel {
  const [state, setState] = useState<SubmitState>({ status: "idle" });
  const [fieldErrors, setFieldErrors] = useState<Partial<Record<keyof BriefFormValues, string>> | null>(null);

  function handleSubmit(values: BriefFormValues) {
    setState({ status: "submitting" });
    setFieldErrors(null);

    // Fake submit — task-06 replaces this body with submitAssessmentBrief, no contract changes.
    setTimeout(() => {
      if (values.topic === "trigger-field-error") {
        setFieldErrors({ topic: "Ya existe una evaluación con este tema" });
        setState({ status: "idle" });
        return;
      }
      setState({ status: "success" });
    }, 800);
  }

  return {
    isSubmitting: state.status === "submitting",
    serverError: state.status === "error" ? state.message : null,
    fieldErrors,
    handleSubmit,
  };
}
