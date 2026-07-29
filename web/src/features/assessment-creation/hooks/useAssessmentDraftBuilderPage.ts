"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { loadAssessmentDraftBuilderPage } from "../loaders/loadAssessmentDraftBuilderPage";
import {
  createAssessmentRevision,
  regenerateAssessmentDraft,
  generateAssessmentDraft,
  retryAssessmentDraftGeneration,
  getGenerationStatus,
  isStaleRevisionConflict,
  isAlreadyGeneratedConflict,
  GetAssessmentDraftError,
  GetAssessmentDraftVersionsError,
  GetGenerationStatusError,
  CreateAssessmentRevisionError,
  RegenerateAssessmentDraftError,
  RetryAssessmentDraftGenerationError,
} from "@/lib/api/assessments";
import { createIdempotencyKey } from "@/lib/api/idempotencyKey";
import type {
  AssessmentDraftBuilderPageData,
  AssessmentDraftVersionViewModel,
  AssessmentDraftViewModel,
} from "../mappers/toAssessmentDraftBuilderPageViewModel";
import type { DraftEditableField, DraftEditableFields } from "./useDraftEditorSection";
import type { FieldErrorResponse, GenerationStatusValue } from "@/types/assessment";

// No second consumer needs this yet — promote to a shared module once another
// screen needs the same shape, per 04-hooks-y-logica-de-ui.md §7.
export type RemoteData<T> =
  | { status: "loading" }
  | { status: "ready"; data: T }
  | { status: "not-found" }
  | { status: "error"; error: string };

export interface StaleRevisionConflictViewModel {
  message: string;
  onReload: () => void;
}

export interface AssessmentDraftBuilderPageViewModel {
  draft: AssessmentDraftViewModel;
  versions: AssessmentDraftVersionViewModel[];
  selectedVersion: number;
  isViewingHistoricalVersion: boolean;
  onViewVersion: (versionNumber: number) => void;
  isSaving: boolean;
  saveFieldErrors: Partial<Record<DraftEditableField, string>> | null;
  saveServerError: string | null;
  onSave: (values: DraftEditableFields) => Promise<void>;
  isRegenerating: boolean;
  regenerateFieldError: string | null;
  regenerateAgentError: string | null;
  onRegenerate: (adjustmentNotes: string) => Promise<void>;
  staleConflict: StaleRevisionConflictViewModel | null;
}

// A generation-status-driven state (LOCAL-CONTRACTS.md § New/changed UI states) — reached
// whenever the page loads and no current revision exists yet, instead of the old bare
// "not found" dead end (Research 02 §5.6).
export interface GenerationNotStartedViewModel {
  isGenerating: boolean;
  generateError: string | null;
  onGenerate: () => void;
}

export interface GenerationFailedViewModel {
  failureCode?: string;
  isRetrying: boolean;
  retryError: string | null;
  onRetry: () => void;
}

export interface GenerationIndeterminateViewModel {
  isRetrying: boolean;
  retryError: string | null;
  onRetry: () => void;
}

export type AssessmentDraftBuilderPageState =
  | { status: "loading" }
  | { status: "not-found" }
  | { status: "error"; error: string }
  | { status: "generation-not-started"; data: GenerationNotStartedViewModel }
  | { status: "generation-in-progress" }
  | { status: "generation-failed"; data: GenerationFailedViewModel }
  | { status: "generation-indeterminate"; data: GenerationIndeterminateViewModel }
  | { status: "ready"; data: AssessmentDraftBuilderPageViewModel };

// Microcopy per docs/gradeops-ai-frontend-guidelines/15-backend-frontend-contracts.md §4 (never
// show raw backend codes/English strings) and wireframes/draft-builder-screen.md's "Estados" table.
const LOAD_ERROR_MESSAGE = "Ocurrió un error inesperado al cargar el draft. Intenta de nuevo.";
// task-07 traced GetCurrentDraftHandler: assessment-not-found and no-draft-yet both throw the
// same ResourceNotFoundException, producing an identical {error:"NOT_FOUND"} 404 body in both
// cases. The generation-status call (added by this packet) disambiguates the two: if it also
// 404s, the assessment truly doesn't exist/isn't the caller's; otherwise it reports what state
// generation is actually in — see loadPage() below.
export const NOT_FOUND_MESSAGE = "No encontramos esta evaluación.";
const NO_PRIOR_DRAFT_MESSAGE = "Aún no se ha generado un borrador para esta evaluación.";
const AGENT_REJECTED_MESSAGE =
  "No pudimos regenerar el borrador con estas notas. Ajusta el texto e intenta de nuevo.";
const AGENT_DOWN_MESSAGE = "El servicio de generación no está disponible. Intenta de nuevo en unos minutos.";
const ADJUSTMENT_NOTES_REQUIRED_MESSAGE = "Ingresa notas de ajuste antes de regenerar.";
const GENERIC_RETRY_MESSAGE = "Ocurrió un error inesperado. Intenta de nuevo.";
export const STALE_REVISION_MESSAGE =
  "La versión actual cambió mientras editabas (otra pestaña o proceso la modificó). Recarga para ver la última versión.";
const GENERATE_ERROR_MESSAGE = "No pudimos generar el borrador. Intenta de nuevo.";
const RETRY_ERROR_MESSAGE = "No pudimos reintentar la generación. Intenta de nuevo.";

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
  if (error instanceof CreateAssessmentRevisionError) {
    if (Array.isArray(error.body)) {
      const fieldErrors: Partial<Record<DraftEditableField, string>> = {};
      for (const fieldError of error.body as FieldErrorResponse[]) {
        const field = fieldError.field as DraftEditableField;
        fieldErrors[field] = SAVE_FIELD_ERROR_MESSAGES[field] ?? GENERIC_RETRY_MESSAGE;
      }
      return { fieldErrors, serverError: null };
    }
    if (error.status === 422 && "error" in error.body && error.body.error === "APPLICATION_ERROR") {
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
    if (error.status === 422 && "error" in error.body && error.body.error === "APPLICATION_ERROR") {
      return { fieldError: null, agentError: NO_PRIOR_DRAFT_MESSAGE };
    }
    if (error.status === 422 && "error" in error.body && error.body.error === "AGENT_CALL_FAILED") {
      return { fieldError: null, agentError: AGENT_REJECTED_MESSAGE };
    }
    if ((error.status === 502 || error.status === 503) && "error" in error.body && error.body.error === "AGENT_CALL_FAILED") {
      return { fieldError: null, agentError: AGENT_DOWN_MESSAGE };
    }
  }
  return { fieldError: null, agentError: GENERIC_RETRY_MESSAGE };
}

type InternalPageState =
  | { status: "loading" }
  | { status: "not-found" }
  | { status: "error"; error: string }
  | { status: "generation-not-started" }
  | { status: "generation-in-progress" }
  | { status: "generation-failed"; failureCode?: string }
  | { status: "generation-indeterminate" }
  | { status: "ready"; data: AssessmentDraftBuilderPageData };

export function useAssessmentDraftBuilderPage(assessmentId: string): AssessmentDraftBuilderPageState {
  const [pageState, setPageState] = useState<InternalPageState>({ status: "loading" });
  const [selectedVersion, setSelectedVersion] = useState<number | null>(null);

  const [isSaving, setIsSaving] = useState(false);
  const [saveFieldErrors, setSaveFieldErrors] = useState<Partial<Record<DraftEditableField, string>> | null>(null);
  const [saveServerError, setSaveServerError] = useState<string | null>(null);

  const [isRegenerating, setIsRegenerating] = useState(false);
  const [regenerateFieldError, setRegenerateFieldError] = useState<string | null>(null);
  const [regenerateAgentError, setRegenerateAgentError] = useState<string | null>(null);
  // Generated once per regenerate click, not once per render — a double-click reuses the
  // same key, satisfying the non-negotiable idempotency-key lifecycle (see
  // LOCAL-CONTRACTS.md § Idempotency key lifecycle). Cleared on any terminal response so the
  // next distinct click gets a fresh key.
  const regenerateIdempotencyKeyRef = useRef<string | null>(null);

  const [isGenerating, setIsGenerating] = useState(false);
  const [generateError, setGenerateError] = useState<string | null>(null);
  const generateIdempotencyKeyRef = useRef<string | null>(null);

  const [isRetrying, setIsRetrying] = useState(false);
  const [retryError, setRetryError] = useState<string | null>(null);

  const [staleConflict, setStaleConflict] = useState<string | null>(null);

  const loadPage = useCallback(async () => {
    try {
      const data = await loadAssessmentDraftBuilderPage(assessmentId);
      setPageState({ status: "ready", data });
      setSelectedVersion(data.draft.versionNumber);
      setStaleConflict(null);
      return;
    } catch (error) {
      if (!isDraftNotFoundError(error)) {
        setPageState({ status: "error", error: LOAD_ERROR_MESSAGE });
        return;
      }
    }

    // No current revision — resolve via generation-status (LOCAL-CONTRACTS.md § Recovery
    // after refresh), the read model that turns "reload after a failed generation" into a
    // recoverable path instead of the demonstrated dead end (Research 02 §5.6).
    try {
      const generationStatus = await getGenerationStatus(assessmentId);

      if (generationStatus.currentRevisionId) {
        // Race: a revision completed between the draft fetch above and this call.
        const data = await loadAssessmentDraftBuilderPage(assessmentId);
        setPageState({ status: "ready", data });
        setSelectedVersion(data.draft.versionNumber);
        setStaleConflict(null);
        return;
      }

      const status: GenerationStatusValue = generationStatus.status;
      if (status === "NOT_STARTED") {
        setPageState({ status: "generation-not-started" });
      } else if (status === "IN_PROGRESS") {
        setPageState({ status: "generation-in-progress" });
      } else if (status === "FAILED_RETRYABLE") {
        setPageState({ status: "generation-failed", failureCode: generationStatus.failureCode });
      } else if (status === "INDETERMINATE") {
        setPageState({ status: "generation-indeterminate" });
      } else {
        setPageState({ status: "error", error: LOAD_ERROR_MESSAGE });
      }
    } catch (statusError) {
      if (statusError instanceof GetGenerationStatusError && statusError.status === 404) {
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
      // expectedRevisionId is always the id of the revision currently displayed, tracked from
      // the last successful fetch — never a value invented or defaulted here.
      await createAssessmentRevision(assessmentId, values, pageState.data.draft.draftId);
      await loadPage();
    } catch (error) {
      if (isStaleRevisionConflict(error)) {
        setStaleConflict(STALE_REVISION_MESSAGE);
        return;
      }
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
    if (!regenerateIdempotencyKeyRef.current) {
      regenerateIdempotencyKeyRef.current = createIdempotencyKey();
    }
    const idempotencyKey = regenerateIdempotencyKeyRef.current;
    try {
      await regenerateAssessmentDraft(assessmentId, adjustmentNotes, pageState.data.draft.draftId, idempotencyKey);
      regenerateIdempotencyKeyRef.current = null;
      await loadPage();
    } catch (error) {
      regenerateIdempotencyKeyRef.current = null;
      if (isStaleRevisionConflict(error)) {
        setStaleConflict(STALE_REVISION_MESSAGE);
        return;
      }
      const { fieldError, agentError } = translateRegenerateError(error);
      setRegenerateFieldError(fieldError);
      setRegenerateAgentError(agentError);
    } finally {
      setIsRegenerating(false);
    }
  }

  async function onReloadAfterConflict() {
    setStaleConflict(null);
    setPageState({ status: "loading" });
    await loadPage();
  }

  async function onGenerate() {
    if (isGenerating) return;
    setGenerateError(null);
    setIsGenerating(true);
    if (!generateIdempotencyKeyRef.current) {
      generateIdempotencyKeyRef.current = createIdempotencyKey();
    }
    const idempotencyKey = generateIdempotencyKeyRef.current;
    try {
      await generateAssessmentDraft(assessmentId, idempotencyKey);
      generateIdempotencyKeyRef.current = null;
      await loadPage();
    } catch (error) {
      generateIdempotencyKeyRef.current = null;
      if (isAlreadyGeneratedConflict(error)) {
        await loadPage();
        return;
      }
      setGenerateError(GENERATE_ERROR_MESSAGE);
    } finally {
      setIsGenerating(false);
    }
  }

  async function onRetry() {
    if (isRetrying) return;
    setRetryError(null);
    setIsRetrying(true);
    try {
      // Retry needs no idempotency key (Authoring Operation Contract § 3) — it targets the
      // existing AiOperation server-side. The response does not need to be interpreted here:
      // loadPage() re-derives the true state from generation-status regardless of outcome.
      await retryAssessmentDraftGeneration(assessmentId);
      await loadPage();
    } catch (error) {
      if (error instanceof RetryAssessmentDraftGenerationError && error.status === 409) {
        const code = !Array.isArray(error.body) && "code" in error.body ? error.body.code : undefined;
        if (code === "OPERATION_IN_PROGRESS") {
          await loadPage();
          return;
        }
        // NO_ACTIVE_OPERATION_TO_RETRY: "should not be reachable from the UI if state tracking
        // is correct" (LOCAL-CONTRACTS.md) — falls through to the generic message below.
      }
      setRetryError(RETRY_ERROR_MESSAGE);
    } finally {
      setIsRetrying(false);
    }
  }

  function onViewVersion(versionNumber: number) {
    setSelectedVersion(versionNumber);
  }

  if (pageState.status === "loading") return { status: "loading" };
  if (pageState.status === "not-found") return { status: "not-found" };
  if (pageState.status === "error") return { status: "error", error: pageState.error };
  if (pageState.status === "generation-not-started") {
    return { status: "generation-not-started", data: { isGenerating, generateError, onGenerate } };
  }
  if (pageState.status === "generation-in-progress") {
    return { status: "generation-in-progress" };
  }
  if (pageState.status === "generation-failed") {
    return {
      status: "generation-failed",
      data: { failureCode: pageState.failureCode, isRetrying, retryError, onRetry },
    };
  }
  if (pageState.status === "generation-indeterminate") {
    return { status: "generation-indeterminate", data: { isRetrying, retryError, onRetry } };
  }

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
      onViewVersion,
      isSaving,
      saveFieldErrors,
      saveServerError,
      onSave,
      isRegenerating,
      regenerateFieldError,
      regenerateAgentError,
      onRegenerate,
      staleConflict: staleConflict ? { message: staleConflict, onReload: onReloadAfterConflict } : null,
    },
  };
}
