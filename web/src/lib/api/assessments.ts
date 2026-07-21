import { apiClient } from "@/lib/api/client";
import { logger } from "@/lib/logging/logger";
import { createCorrelationId } from "@/lib/logging/correlationId";
import type {
  AssessmentSummaryDto,
  ApiErrorResponse,
  AssessmentDraftDto,
  CreateAssessmentBriefRequestDto,
  CreateAssessmentBriefResponseDto,
  FieldErrorResponse,
  UpdateAssessmentDraftRequestDto,
} from "@/types/assessment";
import type { Logger } from "pino";

export async function getAssessments(): Promise<AssessmentSummaryDto[]> {
  const res = await apiClient("/api/v1/assessments");
  if (!res.ok) throw new Error(`Failed to fetch assessments: ${res.status}`);
  return res.json();
}

export class CreateAssessmentBriefError extends Error {
  constructor(
    public status: number,
    public body: FieldErrorResponse[] | ApiErrorResponse
  ) {
    super(`createAssessmentBrief failed with status ${status}`);
    this.name = "CreateAssessmentBriefError";
  }
}

export class GenerateAssessmentDraftError extends Error {
  constructor(
    public status: number,
    public body: ApiErrorResponse,
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

export class UpdateAssessmentDraftError extends Error {
  constructor(
    public status: number,
    public body: ApiErrorResponse,
    public assessmentId: string
  ) {
    super(`updateAssessmentDraft failed with status ${status} for assessment ${assessmentId}`);
    this.name = "UpdateAssessmentDraftError";
  }
}

export class RegenerateAssessmentDraftError extends Error {
  constructor(
    public status: number,
    public body: ApiErrorResponse,
    public assessmentId: string
  ) {
    super(`regenerateAssessmentDraft failed with status ${status} for assessment ${assessmentId}`);
    this.name = "RegenerateAssessmentDraftError";
  }
}

export async function createAssessmentBrief(
  brief: CreateAssessmentBriefRequestDto,
  log: Logger = logger
): Promise<CreateAssessmentBriefResponseDto> {
  const startedAt = Date.now();
  const res = await apiClient("/api/v1/assessments", {
    method: "POST",
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

export async function generateAssessmentDraft(assessmentId: string, log: Logger = logger): Promise<void> {
  const startedAt = Date.now();
  const res = await apiClient(`/api/v1/assessments/${assessmentId}/draft`, { method: "POST" });
  const latencyMs = Date.now() - startedAt;

  if (!res.ok) {
    const body: ApiErrorResponse = await res.json().catch(() => ({ error: "UNKNOWN", message: null }));
    // WARN, not ERROR: by the time this is called via submitAssessmentBrief the brief already
    // exists — this failure is recoverable, not a hard stop, per .planning/LOGGING.md's criticality mapping.
    log.warn({ dependency: "api/assessments/draft", status: res.status, latencyMs, assessmentId }, "generateAssessmentDraft failed");
    throw new GenerateAssessmentDraftError(res.status, body, assessmentId);
  }

  log.info({ dependency: "api/assessments/draft", status: res.status, latencyMs, assessmentId }, "generateAssessmentDraft succeeded");
}

export async function submitAssessmentBrief(
  brief: CreateAssessmentBriefRequestDto
): Promise<{ assessmentId: string }> {
  const correlationId = createCorrelationId();
  const log = logger.child({ correlationId });

  log.info("submitAssessmentBrief started");
  const { assessmentId } = await createAssessmentBrief(brief, log);
  await generateAssessmentDraft(assessmentId, log);

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

// WARN for recoverable, teacher-facing errors (422 field validation, 502/503 agent down);
// ERROR for everything else (500). No 409 handling — task-07 traced neither draft mutation
// handler implements optimistic locking, so no draft endpoint can ever return one.
export function isRecoverableDraftMutationStatus(status: number): boolean {
  return status === 422 || status === 502 || status === 503;
}

export async function updateAssessmentDraft(
  assessmentId: string,
  changes: UpdateAssessmentDraftRequestDto
): Promise<AssessmentDraftDto> {
  const correlationId = createCorrelationId();
  const log = logger.child({ correlationId, assessmentId });
  const startedAt = Date.now();
  const res = await apiClient(`/api/v1/assessments/${assessmentId}/draft`, {
    method: "PATCH",
    body: JSON.stringify(changes),
  });
  const latencyMs = Date.now() - startedAt;

  if (!res.ok) {
    const body: ApiErrorResponse = await res.json().catch(() => ({ error: "UNKNOWN", message: null }));
    const context = { dependency: "api/assessments/draft", method: "PATCH", status: res.status, latencyMs, assessmentId };
    if (isRecoverableDraftMutationStatus(res.status)) {
      log.warn(context, "updateAssessmentDraft failed");
    } else {
      log.error(context, "updateAssessmentDraft failed");
    }
    throw new UpdateAssessmentDraftError(res.status, body, assessmentId);
  }

  const data: AssessmentDraftDto = await res.json();
  log.info(
    { dependency: "api/assessments/draft", method: "PATCH", status: res.status, latencyMs, assessmentId, versionNumber: data.versionNumber },
    "updateAssessmentDraft succeeded"
  );
  return data;
}

export async function regenerateAssessmentDraft(
  assessmentId: string,
  adjustmentNotes: string
): Promise<AssessmentDraftDto> {
  const correlationId = createCorrelationId();
  const log = logger.child({ correlationId, assessmentId });
  const startedAt = Date.now();
  const res = await apiClient(`/api/v1/assessments/${assessmentId}/draft/regenerate`, {
    method: "POST",
    body: JSON.stringify({ adjustmentNotes }),
  });
  const latencyMs = Date.now() - startedAt;

  if (!res.ok) {
    const body: ApiErrorResponse = await res.json().catch(() => ({ error: "UNKNOWN", message: null }));
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
