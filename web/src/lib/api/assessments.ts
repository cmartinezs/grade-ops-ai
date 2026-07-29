import { apiClient } from "@/lib/api/client";
import { logger } from "@/lib/logging/logger";
import { createCorrelationId } from "@/lib/logging/correlationId";
import type {
  AssessmentSummaryDto,
  ApiErrorResponse,
  ApiConflictErrorResponse,
  AssessmentDraftDto,
  CreateAssessmentBriefRequestDto,
  CreateAssessmentBriefResponseDto,
  FieldErrorResponse,
  UpdateAssessmentDraftRequestDto,
  GenerationStatusDto,
  AiOperationDto,
} from "@/types/assessment";
import type { Logger } from "pino";

export async function getAssessments(): Promise<AssessmentSummaryDto[]> {
  const res = await apiClient("/api/v1/assessments");
  if (!res.ok) throw new Error(`Failed to fetch assessments: ${res.status}`);
  return res.json();
}

type MutationErrorBody = FieldErrorResponse[] | ApiErrorResponse | ApiConflictErrorResponse;

export class CreateAssessmentBriefError extends Error {
  constructor(
    public status: number,
    public body: MutationErrorBody
  ) {
    super(`createAssessmentBrief failed with status ${status}`);
    this.name = "CreateAssessmentBriefError";
  }
}

export class GenerateAssessmentDraftError extends Error {
  constructor(
    public status: number,
    public body: ApiErrorResponse | ApiConflictErrorResponse,
    public assessmentId: string
  ) {
    super(`generateAssessmentDraft failed with status ${status} for assessment ${assessmentId}`);
    this.name = "GenerateAssessmentDraftError";
  }
}

export class GetAssessmentDraftError extends Error {
  constructor(
    public status: number,
    public body: ApiErrorResponse,
    public assessmentId: string
  ) {
    super(`getAssessmentDraft failed with status ${status} for assessment ${assessmentId}`);
    this.name = "GetAssessmentDraftError";
  }
}

export class GetAssessmentDraftVersionsError extends Error {
  constructor(
    public status: number,
    public body: ApiErrorResponse,
    public assessmentId: string
  ) {
    super(`getAssessmentDraftVersions failed with status ${status} for assessment ${assessmentId}`);
    this.name = "GetAssessmentDraftVersionsError";
  }
}

export class GetGenerationStatusError extends Error {
  constructor(
    public status: number,
    public body: ApiErrorResponse,
    public assessmentId: string
  ) {
    super(`getGenerationStatus failed with status ${status} for assessment ${assessmentId}`);
    this.name = "GetGenerationStatusError";
  }
}

export class RetryAssessmentDraftGenerationError extends Error {
  constructor(
    public status: number,
    public body: ApiErrorResponse | ApiConflictErrorResponse,
    public assessmentId: string
  ) {
    super(`retryAssessmentDraftGeneration failed with status ${status} for assessment ${assessmentId}`);
    this.name = "RetryAssessmentDraftGenerationError";
  }
}

export class CreateAssessmentRevisionError extends Error {
  constructor(
    public status: number,
    public body: MutationErrorBody,
    public assessmentId: string
  ) {
    super(`createAssessmentRevision failed with status ${status} for assessment ${assessmentId}`);
    this.name = "CreateAssessmentRevisionError";
  }
}

export class RegenerateAssessmentDraftError extends Error {
  constructor(
    public status: number,
    public body: MutationErrorBody,
    public assessmentId: string
  ) {
    super(`regenerateAssessmentDraft failed with status ${status} for assessment ${assessmentId}`);
    this.name = "RegenerateAssessmentDraftError";
  }
}

export async function createAssessmentBrief(
  brief: CreateAssessmentBriefRequestDto,
  idempotencyKey: string,
  log: Logger = logger
): Promise<CreateAssessmentBriefResponseDto> {
  const startedAt = Date.now();
  const res = await apiClient("/api/v1/assessments", {
    method: "POST",
    headers: { "Idempotency-Key": idempotencyKey },
    body: JSON.stringify(brief),
  });
  const latencyMs = Date.now() - startedAt;

  if (!res.ok) {
    const body = await res.json().catch(() => ({ error: "UNKNOWN", message: null }));
    log.error({ dependency: "api/assessments", status: res.status, latencyMs }, "createAssessmentBrief failed");
    throw new CreateAssessmentBriefError(res.status, body);
  }

  const data: CreateAssessmentBriefResponseDto = await res.json();
  log.info({ dependency: "api/assessments", status: res.status, latencyMs, assessmentId: data.assessmentId }, "createAssessmentBrief succeeded");
  return data;
}

// Discriminated result: the API can complete generation synchronously (201, new revision) or
// durably record a pending/failed attempt (202, AiOperation) without ever throwing — see
// Authoring Operation Contract § 2 ("returning 202 for a call that fully executed synchronously
// is intentional"). Callers must branch on `outcome`, never assume a 2xx means a revision exists.
export type GenerateAssessmentDraftOutcome =
  | { outcome: "revision-created"; revision: AssessmentDraftDto }
  | { outcome: "operation-pending"; operation: AiOperationDto };

export async function generateAssessmentDraft(
  assessmentId: string,
  idempotencyKey: string,
  log: Logger = logger
): Promise<GenerateAssessmentDraftOutcome> {
  const startedAt = Date.now();
  const res = await apiClient(`/api/v1/assessments/${assessmentId}/draft`, {
    method: "POST",
    headers: { "Idempotency-Key": idempotencyKey },
  });
  const latencyMs = Date.now() - startedAt;

  if (res.status === 202) {
    const operation: AiOperationDto = await res.json();
    // WARN, not ERROR: a durably-recorded pending/failed operation is not a crash — the
    // assessment remains addressable and resumable (see the resume-after-refresh flow).
    log.warn(
      { dependency: "api/assessments/draft", status: res.status, latencyMs, assessmentId, operationStatus: operation.status },
      "generateAssessmentDraft returned a durable pending/failed operation"
    );
    return { outcome: "operation-pending", operation };
  }

  if (!res.ok) {
    const body: ApiErrorResponse | ApiConflictErrorResponse = await res.json().catch(() => ({ error: "UNKNOWN", message: null }));
    log.warn({ dependency: "api/assessments/draft", status: res.status, latencyMs, assessmentId }, "generateAssessmentDraft failed");
    throw new GenerateAssessmentDraftError(res.status, body, assessmentId);
  }

  const revision: AssessmentDraftDto = await res.json();
  log.info({ dependency: "api/assessments/draft", status: res.status, latencyMs, assessmentId }, "generateAssessmentDraft succeeded");
  return { outcome: "revision-created", revision };
}

export interface SubmitAssessmentBriefKeys {
  createIdempotencyKey: string;
  generateIdempotencyKey: string;
}

// Create and generate remain two calls (Authoring Operation Contract § "Create and generate
// remain two calls, not one compound command"). A failure of the second call no longer fails
// this function — the assessment is already durably created and addressable via
// generation-status, so the caller always receives an assessmentId to route to a resumable
// page, instead of Web inventing recovery behavior for a call it does not own the outcome of.
export async function submitAssessmentBrief(
  brief: CreateAssessmentBriefRequestDto,
  keys: SubmitAssessmentBriefKeys
): Promise<{ assessmentId: string }> {
  const correlationId = createCorrelationId();
  const log = logger.child({ correlationId });

  log.info("submitAssessmentBrief started");
  const { assessmentId } = await createAssessmentBrief(brief, keys.createIdempotencyKey, log);

  try {
    await generateAssessmentDraft(assessmentId, keys.generateIdempotencyKey, log);
  } catch (error) {
    log.warn(
      { assessmentId, error: error instanceof Error ? error.message : String(error) },
      "generateAssessmentDraft did not complete cleanly after brief creation; assessment remains resumable"
    );
  }

  log.info({ assessmentId }, "submitAssessmentBrief completed");
  return { assessmentId };
}

export async function getAssessmentDraft(assessmentId: string, log: Logger = logger): Promise<AssessmentDraftDto> {
  const startedAt = Date.now();
  const res = await apiClient(`/api/v1/assessments/${assessmentId}/draft`);
  const latencyMs = Date.now() - startedAt;

  if (!res.ok) {
    const body: ApiErrorResponse = await res.json().catch(() => ({ error: "UNKNOWN", message: null }));
    log.error({ dependency: "api/assessments/draft", status: res.status, latencyMs, assessmentId }, "getAssessmentDraft failed");
    throw new GetAssessmentDraftError(res.status, body, assessmentId);
  }

  log.debug({ dependency: "api/assessments/draft", status: res.status, latencyMs, assessmentId }, "getAssessmentDraft succeeded");
  return res.json();
}

export async function getAssessmentDraftVersions(assessmentId: string, log: Logger = logger): Promise<AssessmentDraftDto[]> {
  const startedAt = Date.now();
  const res = await apiClient(`/api/v1/assessments/${assessmentId}/draft/versions`);
  const latencyMs = Date.now() - startedAt;

  if (!res.ok) {
    const body: ApiErrorResponse = await res.json().catch(() => ({ error: "UNKNOWN", message: null }));
    log.error({ dependency: "api/assessments/draft/versions", status: res.status, latencyMs, assessmentId }, "getAssessmentDraftVersions failed");
    throw new GetAssessmentDraftVersionsError(res.status, body, assessmentId);
  }

  log.debug({ dependency: "api/assessments/draft/versions", status: res.status, latencyMs, assessmentId }, "getAssessmentDraftVersions succeeded");
  return res.json();
}

// This is the read model the resume-after-refresh flow polls (LOCAL-CONTRACTS.md § Recovery
// after refresh): called whenever loading an assessment finds no current revision, to
// disambiguate "generation never started" / "in progress" / "failed, retryable" / "unknown"
// from a genuinely nonexistent-or-unauthorized assessment.
export async function getGenerationStatus(assessmentId: string, log: Logger = logger): Promise<GenerationStatusDto> {
  const startedAt = Date.now();
  const res = await apiClient(`/api/v1/assessments/${assessmentId}/generation-status`);
  const latencyMs = Date.now() - startedAt;

  if (!res.ok) {
    const body: ApiErrorResponse = await res.json().catch(() => ({ error: "UNKNOWN", message: null }));
    log.error({ dependency: "api/assessments/generation-status", status: res.status, latencyMs, assessmentId }, "getGenerationStatus failed");
    throw new GetGenerationStatusError(res.status, body, assessmentId);
  }

  log.debug({ dependency: "api/assessments/generation-status", status: res.status, latencyMs, assessmentId }, "getGenerationStatus succeeded");
  return res.json();
}

// No idempotency key, no body — retry targets the existing AiOperation for this assessment
// server-side (Authoring Operation Contract § 3). User-initiated only, never dispatched
// automatically by Web.
export async function retryAssessmentDraftGeneration(assessmentId: string, log: Logger = logger): Promise<AiOperationDto> {
  const startedAt = Date.now();
  const res = await apiClient(`/api/v1/assessments/${assessmentId}/draft/retry`, { method: "POST" });
  const latencyMs = Date.now() - startedAt;

  if (!res.ok) {
    const body: ApiErrorResponse | ApiConflictErrorResponse = await res.json().catch(() => ({ error: "UNKNOWN", message: null }));
    log.warn({ dependency: "api/assessments/draft/retry", status: res.status, latencyMs, assessmentId }, "retryAssessmentDraftGeneration failed");
    throw new RetryAssessmentDraftGenerationError(res.status, body, assessmentId);
  }

  log.info({ dependency: "api/assessments/draft/retry", status: res.status, latencyMs, assessmentId }, "retryAssessmentDraftGeneration dispatched");
  return res.json();
}

// WARN for recoverable, teacher-facing errors (422 field validation, 502/503 agent down);
// ERROR for everything else (500). 409s are never logged as ERROR: STALE_REVISION and
// ALREADY_GENERATED are expected, user-actionable conflicts, not failures.
export function isRecoverableDraftMutationStatus(status: number): boolean {
  return status === 422 || status === 502 || status === 503 || status === 409;
}

export function isStaleRevisionConflict(
  error: unknown
): error is CreateAssessmentRevisionError | RegenerateAssessmentDraftError {
  return (
    (error instanceof CreateAssessmentRevisionError || error instanceof RegenerateAssessmentDraftError) &&
    error.status === 409 &&
    !Array.isArray(error.body) &&
    "code" in error.body &&
    error.body.code === "STALE_REVISION"
  );
}

export function isIdempotencyKeyPayloadMismatch(error: unknown): error is CreateAssessmentBriefError {
  return (
    error instanceof CreateAssessmentBriefError &&
    error.status === 409 &&
    !Array.isArray(error.body) &&
    "code" in error.body &&
    error.body.code === "IDEMPOTENCY_KEY_PAYLOAD_MISMATCH"
  );
}

export function isAlreadyGeneratedConflict(error: unknown): error is GenerateAssessmentDraftError {
  return (
    error instanceof GenerateAssessmentDraftError &&
    error.status === 409 &&
    "code" in error.body &&
    error.body.code === "ALREADY_GENERATED"
  );
}

// Replaces PATCH /api/v1/assessments/{id}/draft, retired in this cut (Authoring Operation
// Contract § 6) — creating a new resource (a revision) via POST to a sub-collection, not
// PATCHing the assessment as if it were one resource being partially updated. No idempotency
// key: CAS via expectedRevisionId is this endpoint's only concurrency guard.
export async function createAssessmentRevision(
  assessmentId: string,
  changes: UpdateAssessmentDraftRequestDto,
  expectedRevisionId: string
): Promise<AssessmentDraftDto> {
  const correlationId = createCorrelationId();
  const log = logger.child({ correlationId, assessmentId });
  const startedAt = Date.now();
  const res = await apiClient(`/api/v1/assessments/${assessmentId}/revisions`, {
    method: "POST",
    body: JSON.stringify({ ...changes, expectedRevisionId }),
  });
  const latencyMs = Date.now() - startedAt;

  if (!res.ok) {
    const body: MutationErrorBody = await res.json().catch(() => ({ error: "UNKNOWN", message: null }));
    const context = { dependency: "api/assessments/revisions", method: "POST", status: res.status, latencyMs, assessmentId };
    if (isRecoverableDraftMutationStatus(res.status)) {
      log.warn(context, "createAssessmentRevision failed");
    } else {
      log.error(context, "createAssessmentRevision failed");
    }
    throw new CreateAssessmentRevisionError(res.status, body, assessmentId);
  }

  const data: AssessmentDraftDto = await res.json();
  log.info(
    { dependency: "api/assessments/revisions", method: "POST", status: res.status, latencyMs, assessmentId, versionNumber: data.versionNumber },
    "createAssessmentRevision succeeded"
  );
  return data;
}

export async function regenerateAssessmentDraft(
  assessmentId: string,
  adjustmentNotes: string,
  expectedRevisionId: string,
  idempotencyKey: string
): Promise<AssessmentDraftDto> {
  const correlationId = createCorrelationId();
  const log = logger.child({ correlationId, assessmentId });
  const startedAt = Date.now();
  const res = await apiClient(`/api/v1/assessments/${assessmentId}/draft/regenerate`, {
    method: "POST",
    headers: { "Idempotency-Key": idempotencyKey },
    body: JSON.stringify({ adjustmentNotes, expectedRevisionId }),
  });
  const latencyMs = Date.now() - startedAt;

  if (!res.ok) {
    const body: MutationErrorBody = await res.json().catch(() => ({ error: "UNKNOWN", message: null }));
    const context = { dependency: "api/assessments/draft/regenerate", status: res.status, latencyMs, assessmentId };
    if (isRecoverableDraftMutationStatus(res.status)) {
      log.warn(context, "regenerateAssessmentDraft failed");
    } else {
      log.error(context, "regenerateAssessmentDraft failed");
    }
    throw new RegenerateAssessmentDraftError(res.status, body, assessmentId);
  }

  const data: AssessmentDraftDto = await res.json();
  log.info(
    { dependency: "api/assessments/draft/regenerate", status: res.status, latencyMs, assessmentId, versionNumber: data.versionNumber },
    "regenerateAssessmentDraft succeeded"
  );
  return data;
}
