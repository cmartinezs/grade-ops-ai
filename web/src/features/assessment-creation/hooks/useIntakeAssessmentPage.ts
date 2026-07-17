"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import {
  submitAssessmentBrief,
  CreateAssessmentBriefError,
  GenerateAssessmentDraftError,
} from "@/lib/api/assessments";
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

const AGENT_REJECTED_MESSAGE =
  "No pudimos generar un borrador con esta información. Ajusta el objetivo de aprendizaje o el tema e intenta de nuevo.";
const SERVICE_UNAVAILABLE_MESSAGE =
  "El servicio de generación de IA no está disponible en este momento. Tu evaluación quedó guardada; intenta generar el borrador más tarde.";
const GENERIC_RETRY_MESSAGE = "Ocurrió un error inesperado. Intenta de nuevo.";

export function useIntakeAssessmentPage(): IntakeAssessmentPageViewModel {
  const router = useRouter();
  const [state, setState] = useState<SubmitState>({ status: "idle" });
  const [fieldErrors, setFieldErrors] = useState<Partial<Record<keyof BriefFormValues, string>> | null>(null);

  async function handleSubmit(values: BriefFormValues) {
    setState({ status: "submitting" });
    setFieldErrors(null);

    try {
      const { assessmentId } = await submitAssessmentBrief(values);
      setState({ status: "success" });
      router.push(`/assessments/${assessmentId}/draft`);
    } catch (err) {
      if (err instanceof CreateAssessmentBriefError && Array.isArray(err.body)) {
        const mapped: Partial<Record<keyof BriefFormValues, string>> = {};
        for (const fieldError of err.body) {
          mapped[fieldError.field as keyof BriefFormValues] = fieldError.message;
        }
        setFieldErrors(mapped);
        setState({ status: "idle" });
        return;
      }

      if (err instanceof GenerateAssessmentDraftError && err.body.message === "AGENT_REJECTED") {
        setState({ status: "error", message: AGENT_REJECTED_MESSAGE });
        return;
      }

      if (
        err instanceof GenerateAssessmentDraftError &&
        (err.body.message === "AGENT_ERROR" || err.body.message === "UNREACHABLE")
      ) {
        setState({ status: "error", message: SERVICE_UNAVAILABLE_MESSAGE });
        return;
      }

      setState({ status: "error", message: GENERIC_RETRY_MESSAGE });
    }
  }

  return {
    isSubmitting: state.status === "submitting",
    serverError: state.status === "error" ? state.message : null,
    fieldErrors,
    handleSubmit,
  };
}
