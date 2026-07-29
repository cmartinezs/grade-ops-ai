export type AssessmentStatus = "DRAFT" | "OPEN" | "GRADING" | "CLOSED";

export interface AssessmentSummaryDto {
  id: string;
  title: string;
  status: AssessmentStatus;
  submissionCount: number;
  pendingApprovals: number;
  reportLink: string | null;
}

export interface CreateAssessmentBriefRequestDto {
  learningGoal: string;
  topic: string;
  level: string;
  duration: string;
  language: string;
}

export interface CreateAssessmentBriefResponseDto {
  assessmentId: string;
}

// origin/actorId/reason/previousRevisionId are authoritative provenance from the API —
// see docs/99-decisions/2026-07-28-authoring-operation-contract.md § 2/5/6. Web renders
// them as-is; it never derives or infers provenance locally.
export type AssessmentRevisionOrigin = "AI_GENERATED" | "HUMAN_EDITED";

export interface AssessmentDraftDto {
  draftId: string;
  title: string;
  context: string;
  instructions: string;
  objectives: string[];
  deliverables: string[];
  constraints: string[];
  versionNumber: number;
  origin: AssessmentRevisionOrigin;
  actorId: string | null;
  reason: string | null;
  previousRevisionId: string | null;
}

export interface UpdateAssessmentDraftRequestDto {
  title?: string;
  context?: string;
  instructions?: string;
  objectives?: string[];
  deliverables?: string[];
  constraints?: string[];
}

export interface FieldErrorResponse {
  field: string;
  message: string;
}

export interface ApiErrorResponse {
  error: string;
  message: string | null;
}

// Frozen shape for every 409 typed conflict introduced by the authoring operation contract
// (STALE_REVISION, IDEMPOTENCY_KEY_PAYLOAD_MISMATCH, ALREADY_GENERATED,
// NO_ACTIVE_OPERATION_TO_RETRY, OPERATION_IN_PROGRESS) — see LOCAL-CONTRACTS.md's note that
// every 409 body is `{ code, message }`, distinct from the legacy `{ error, message }` shape
// above. Not yet verified against the real API (Session A's contract is frozen but
// unimplemented at the time this packet was executed) — see WEB-HANDOFF.md "API assumptions".
export interface ApiConflictErrorResponse {
  code: string;
  message: string | null;
}

// Canonical values per LOCAL-CONTRACTS.md § Canonical status taxonomy. Use exactly these
// spellings — never invent a client-side synonym.
export type GenerationStatusValue = "NOT_STARTED" | "IN_PROGRESS" | "FAILED_RETRYABLE" | "INDETERMINATE";

export interface GenerationStatusDto {
  operationType: string;
  status: GenerationStatusValue;
  failureCode?: string;
  retryable: boolean;
  currentRevisionId?: string | null;
}

// Body shape of the durable AiOperation record returned by POST .../draft on a 202
// (generation failed/pending) — see Authoring Operation Contract § 2.
export interface AiOperationDto {
  id: string;
  status: string;
  failureCode?: string;
}
