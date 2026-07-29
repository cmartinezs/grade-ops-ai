"use client";

import { useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { submitAssessmentBrief, CreateAssessmentBriefError, isIdempotencyKeyPayloadMismatch } from "@/lib/api/assessments";
import { createIdempotencyKey } from "@/lib/api/idempotencyKey";
import type { BriefFormValues } from "../schemas/briefSchema";

// The backend's @NotBlank constraint has no custom message, so its 422 body carries
// Hibernate Validator's default English text ("must not be blank") — confirmed against
// the real local api/ stack, not assumed. Never shown to the teacher (per this task's own
// "no English backend strings" rule); reuse briefSchema's own Spanish copy per field instead,
// since every one of these fields has the exact same single constraint (required, non-blank).
const FIELD_ERROR_MESSAGES: Record<keyof BriefFormValues, string> = {
  learningGoal: "Ingresa el objetivo de aprendizaje.",
  topic: "Ingresa el tema.",
  level: "Ingresa el nivel.",
  duration: "Ingresa la duración.",
  language: "Ingresa el idioma.",
};

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

const GENERIC_RETRY_MESSAGE = "Ocurrió un error inesperado. Intenta de nuevo.";
const IDEMPOTENCY_CONFLICT_MESSAGE =
  "Ocurrió un conflicto al enviar el formulario (un envío anterior con datos distintos sigue vigente). Intenta de nuevo.";

export function useIntakeAssessmentPage(): IntakeAssessmentPageViewModel {
  const router = useRouter();
  const [state, setState] = useState<SubmitState>({ status: "idle" });
  const [fieldErrors, setFieldErrors] = useState<Partial<Record<keyof BriefFormValues, string>> | null>(null);
  // Generated once per submit action, not once per render — a double-click or accidental
  // resubmit before this ref clears must reuse the same key (LOCAL-CONTRACTS.md § Idempotency
  // key lifecycle). Cleared once a terminal response (success or a definitive error) arrives,
  // so a genuinely new attempt (e.g. resubmitting after fixing a validation error) gets a
  // fresh key rather than colliding with the previous payload under the same key.
  const createIdempotencyKeyRef = useRef<string | null>(null);

  async function handleSubmit(values: BriefFormValues) {
    if (state.status === "submitting") return;
    setState({ status: "submitting" });
    setFieldErrors(null);

    if (!createIdempotencyKeyRef.current) {
      createIdempotencyKeyRef.current = createIdempotencyKey();
    }

    try {
      // submitAssessmentBrief still calls create then generate, in that order (Authoring
      // Operation Contract § "Create and generate remain two calls"), but a generate failure
      // no longer throws here — the assessment is already durably created and addressable, so
      // Web always routes to the draft page and lets its own resume-on-load check (§ Recovery
      // after refresh) resolve whatever state generation actually ended up in. This is the
      // concrete fix for Research 02 §5.6's demonstrated dead end.
      const { assessmentId } = await submitAssessmentBrief(values, {
        createIdempotencyKey: createIdempotencyKeyRef.current,
        generateIdempotencyKey: createIdempotencyKey(),
      });

      createIdempotencyKeyRef.current = null;
      setState({ status: "success" });
      router.push(`/assessments/${assessmentId}/draft`);
    } catch (err) {
      createIdempotencyKeyRef.current = null;

      if (err instanceof CreateAssessmentBriefError && Array.isArray(err.body)) {
        const mapped: Partial<Record<keyof BriefFormValues, string>> = {};
        for (const fieldError of err.body) {
          const field = fieldError.field as keyof BriefFormValues;
          mapped[field] = FIELD_ERROR_MESSAGES[field] ?? GENERIC_RETRY_MESSAGE;
        }
        setFieldErrors(mapped);
        setState({ status: "idle" });
        return;
      }

      if (isIdempotencyKeyPayloadMismatch(err)) {
        // Do not silently mint a new key and resubmit — surface the conflict explicitly.
        setState({ status: "error", message: IDEMPOTENCY_CONFLICT_MESSAGE });
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
