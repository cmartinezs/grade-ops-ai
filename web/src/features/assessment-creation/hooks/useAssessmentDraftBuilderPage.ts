"use client";

import { useCallback, useEffect, useState } from "react";
import { loadAssessmentDraftBuilderPage } from "../loaders/loadAssessmentDraftBuilderPage";
import {
  updateAssessmentDraft,
  regenerateAssessmentDraft,
  GetAssessmentDraftError,
  GetAssessmentDraftVersionsError,
  UpdateAssessmentDraftError,
  RegenerateAssessmentDraftError,
} from "@/lib/api/assessments";
import type {
  AssessmentDraftBuilderPageData,
  AssessmentDraftVersionViewModel,
  AssessmentDraftViewModel,
} from "../mappers/toAssessmentDraftBuilderPageViewModel";
import type { DraftEditableField, DraftEditableFields } from "./useDraftEditorSection";
import type { FieldErrorResponse } from "@/types/assessment";

// No second consumer needs this yet — promote to a shared module once another
// screen needs the same shape, per 04-hooks-y-logica-de-ui.md §7.
export type RemoteData<T> =
  | { status: "loading" }
  | { status: "ready"; data: T }
  | { status: "not-found" }
  | { status: "error"; error: string };

export interface AssessmentDraftBuilderPageViewModel {
  draft: AssessmentDraftViewModel;
  versions: AssessmentDraftVersionViewModel[];
  selectedVersion: number;
  isViewingHistoricalVersion: boolean;
  aiDisclosureLabel: "generado-por-ia" | "version-actual";
  onViewVersion: (versionNumber: number) => void;
  isSaving: boolean;
  saveFieldErrors: Partial<Record<DraftEditableField, string>> | null;
  saveServerError: string | null;
  onSave: (values: DraftEditableFields) => Promise<void>;
  isRegenerating: boolean;
  regenerateFieldError: string | null;
  regenerateAgentError: string | null;
  onRegenerate: (adjustmentNotes: string) => Promise<void>;
}

// Microcopy per docs/gradeops-ai-frontend-guidelines/15-backend-frontend-contracts.md §4 (never
// show raw backend codes/English strings) and wireframes/draft-builder-screen.md's "Estados" table
// (task-07's traced error surface — no 409 anywhere, see NO_PRIOR_DRAFT_MESSAGE note below).
const LOAD_ERROR_MESSAGE = "Ocurrió un error inesperado al cargar el draft. Intenta de nuevo.";
// task-07 traced GetCurrentDraftHandler: assessment-not-found and no-draft-yet both throw the
// same ResourceNotFoundException, producing an identical {error:"NOT_FOUND"} 404 body in both
// cases (verified directly against GetCurrentDraftHandler.java) — the frontend cannot
// distinguish "this assessment doesn't exist/isn't yours" from "no draft was generated yet" from
// the response alone. Collapsed into a single not-found state rather than inventing a
// distinction the API can't back, per this task's own Design notes ("404 → assessment not
// found... handle it defensively").
export const NOT_FOUND_MESSAGE = "No encontramos esta evaluación.";
// NoPriorDraftException (ApplicationException -> 422 {error:"APPLICATION_ERROR"}) on a save/
// regenerate — same defensive edge case as the not-found load state above, just surfaced as a
// section-level error since a draft was loaded successfully to get here in the first place.
const NO_PRIOR_DRAFT_MESSAGE = "Aún no se ha generado un borrador para esta evaluación.";
const AGENT_REJECTED_MESSAGE =
  "No pudimos regenerar el borrador con estas notas. Ajusta el texto e intenta de nuevo.";
const AGENT_DOWN_MESSAGE = "El servicio de generación no está disponible. Intenta de nuevo en unos minutos.";
const ADJUSTMENT_NOTES_REQUIRED_MESSAGE = "Ingresa notas de ajuste antes de regenerar.";
const GENERIC_RETRY_MESSAGE = "Ocurrió un error inesperado. Intenta de nuevo.";

// Hibernate Validator's default @Size message is untranslated English ("must not be blank if
// provided") — never shown to the teacher, same "no English backend strings" rule
// useIntakeAssessmentPage.ts already follows for the brief form.
const SAVE_FIELD_ERROR_MESSAGES: Record<DraftEditableField, string> = {
  title: "El título no puede estar vacío.",
  context: "El contexto no puede estar vacío.",
  instructions: "Las instrucciones no pueden estar vacías.",
  objectives: "Debes ingresar al menos un objetivo.",
  deliverables: "Debes ingresar al menos un entregable.",
  constraints: "Debes ingresar al menos una restricción.",
};

export function isDraftNotFoundError(error: unknown): boolean {
  return (
    (error instanceof GetAssessmentDraftError || error instanceof GetAssessmentDraftVersionsError) &&
    error.status === 404
  );
}

export function translateSaveError(
  error: unknown
): { fieldErrors: Partial<Record<DraftEditableField, string>> | null; serverError: string | null } {
  if (error instanceof UpdateAssessmentDraftError) {
    if (Array.isArray(error.body)) {
      const fieldErrors: Partial<Record<DraftEditableField, string>> = {};
      for (const fieldError of error.body as FieldErrorResponse[]) {
        const field = fieldError.field as DraftEditableField;
        fieldErrors[field] = SAVE_FIELD_ERROR_MESSAGES[field] ?? GENERIC_RETRY_MESSAGE;
      }
      return { fieldErrors, serverError: null };
    }
    if (error.status === 422 && error.body.error === "APPLICATION_ERROR") {
      return { fieldErrors: null, serverError: NO_PRIOR_DRAFT_MESSAGE };
    }
  }
  return { fieldErrors: null, serverError: GENERIC_RETRY_MESSAGE };
}

export function translateRegenerateError(
  error: unknown
): { fieldError: string | null; agentError: string | null } {
  if (error instanceof RegenerateAssessmentDraftError) {
    if (Array.isArray(error.body)) {
      return { fieldError: ADJUSTMENT_NOTES_REQUIRED_MESSAGE, agentError: null };
    }
    if (error.status === 422 && error.body.error === "APPLICATION_ERROR") {
      return { fieldError: null, agentError: NO_PRIOR_DRAFT_MESSAGE };
    }
    if (error.status === 422 && error.body.error === "AGENT_CALL_FAILED") {
      return { fieldError: null, agentError: AGENT_REJECTED_MESSAGE };
    }
    if ((error.status === 502 || error.status === 503) && error.body.error === "AGENT_CALL_FAILED") {
      return { fieldError: null, agentError: AGENT_DOWN_MESSAGE };
    }
  }
  return { fieldError: null, agentError: GENERIC_RETRY_MESSAGE };
}

export function useAssessmentDraftBuilderPage(assessmentId: string): RemoteData<AssessmentDraftBuilderPageViewModel> {
  const [pageState, setPageState] = useState<RemoteData<AssessmentDraftBuilderPageData>>({ status: "loading" });
  const [selectedVersion, setSelectedVersion] = useState<number | null>(null);
  const [aiDisclosureLabel, setAiDisclosureLabel] = useState<"generado-por-ia" | "version-actual">("version-actual");

  const [isSaving, setIsSaving] = useState(false);
  const [saveFieldErrors, setSaveFieldErrors] = useState<Partial<Record<DraftEditableField, string>> | null>(null);
  const [saveServerError, setSaveServerError] = useState<string | null>(null);

  const [isRegenerating, setIsRegenerating] = useState(false);
  const [regenerateFieldError, setRegenerateFieldError] = useState<string | null>(null);
  const [regenerateAgentError, setRegenerateAgentError] = useState<string | null>(null);

  const loadPage = useCallback(async () => {
    try {
      const data = await loadAssessmentDraftBuilderPage(assessmentId);
      setPageState({ status: "ready", data });
      setSelectedVersion(data.draft.versionNumber);
    } catch (error) {
      if (isDraftNotFoundError(error)) {
        setPageState({ status: "not-found" });
        return;
      }
      setPageState({ status: "error", error: LOAD_ERROR_MESSAGE });
    }
  }, [assessmentId]);

  useEffect(() => {
    setPageState({ status: "loading" });
    loadPage();
  }, [loadPage]);

  async function onSave(values: DraftEditableFields) {
    if (pageState.status !== "ready") return;
    if (selectedVersion !== pageState.data.draft.versionNumber) return; // read-only while viewing history, per task-08
    setSaveFieldErrors(null);
    setSaveServerError(null);
    setIsSaving(true);
    try {
      await updateAssessmentDraft(assessmentId, values);
      await loadPage(); // refetch full page data after a successful mutation, per 06-estado-datos-y-api.md §13
      setAiDisclosureLabel("version-actual");
    } catch (error) {
      const { fieldErrors, serverError } = translateSaveError(error);
      setSaveFieldErrors(fieldErrors);
      setSaveServerError(serverError);
    } finally {
      setIsSaving(false);
    }
  }

  async function onRegenerate(adjustmentNotes: string) {
    if (pageState.status !== "ready") return;
    setRegenerateFieldError(null);
    setRegenerateAgentError(null);
    setIsRegenerating(true);
    try {
      await regenerateAssessmentDraft(assessmentId, adjustmentNotes);
      await loadPage();
      setAiDisclosureLabel("generado-por-ia");
    } catch (error) {
      const { fieldError, agentError } = translateRegenerateError(error);
      setRegenerateFieldError(fieldError);
      setRegenerateAgentError(agentError);
    } finally {
      setIsRegenerating(false);
    }
  }

  function onViewVersion(versionNumber: number) {
    setSelectedVersion(versionNumber);
  }

  if (pageState.status === "loading") return { status: "loading" };
  if (pageState.status === "not-found") return { status: "not-found" };
  if (pageState.status === "error") return { status: "error", error: pageState.error };

  const { data } = pageState;
  const currentVersionNumber = data.draft.versionNumber;
  const effectiveSelectedVersion = selectedVersion ?? currentVersionNumber;
  const selectedDraft =
    data.versionDrafts.find((v) => v.versionNumber === effectiveSelectedVersion) ?? data.draft;

  return {
    status: "ready",
    data: {
      draft: selectedDraft,
      versions: data.versions,
      selectedVersion: effectiveSelectedVersion,
      isViewingHistoricalVersion: effectiveSelectedVersion !== currentVersionNumber,
      aiDisclosureLabel,
      onViewVersion,
      isSaving,
      saveFieldErrors,
      saveServerError,
      onSave,
      isRegenerating,
      regenerateFieldError,
      regenerateAgentError,
      onRegenerate,
    },
  };
}
