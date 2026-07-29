import {
  createAssessmentBrief,
  generateAssessmentDraft,
  submitAssessmentBrief,
  getAssessmentDraft,
  getAssessmentDraftVersions,
  getGenerationStatus,
  retryAssessmentDraftGeneration,
  createAssessmentRevision,
  regenerateAssessmentDraft,
  isRecoverableDraftMutationStatus,
  isStaleRevisionConflict,
  isIdempotencyKeyPayloadMismatch,
  isAlreadyGeneratedConflict,
  CreateAssessmentBriefError,
  GenerateAssessmentDraftError,
  GetAssessmentDraftError,
  GetAssessmentDraftVersionsError,
  GetGenerationStatusError,
  RetryAssessmentDraftGenerationError,
  CreateAssessmentRevisionError,
  RegenerateAssessmentDraftError,
} from "../assessments";
import { apiClient } from "../client";
import type { CreateAssessmentBriefRequestDto, AssessmentDraftDto } from "@/types/assessment";

jest.mock("../client");
jest.mock("@/lib/logging/logger", () => ({
  logger: { info: jest.fn(), warn: jest.fn(), error: jest.fn(), debug: jest.fn(), child: jest.fn().mockReturnThis() },
}));

const mockApiClient = apiClient as jest.Mock;

const brief: CreateAssessmentBriefRequestDto = {
  learningGoal: "Entender recursividad",
  topic: "Recursion",
  level: "Intermedio",
  duration: "45 min",
  language: "Java",
};

describe("createAssessmentBrief", () => {
  beforeEach(() => jest.clearAllMocks());

  it("sends the Idempotency-Key header and the CreateAssessmentBriefRequestDto body to POST /api/v1/assessments", async () => {
    mockApiClient.mockResolvedValue({
      ok: true,
      status: 201,
      json: () => Promise.resolve({ assessmentId: "assess-1" }),
    });

    const result = await createAssessmentBrief(brief, "key-1");

    expect(mockApiClient).toHaveBeenCalledWith("/api/v1/assessments", {
      method: "POST",
      headers: { "Idempotency-Key": "key-1" },
      body: JSON.stringify(brief),
    });
    expect(result).toEqual({ assessmentId: "assess-1" });
  });

  it("throws CreateAssessmentBriefError carrying the List<FieldErrorResponse> body on 422", async () => {
    const fieldErrors = [{ field: "topic", message: "El tema no puede estar en blanco." }];
    mockApiClient.mockResolvedValue({
      ok: false,
      status: 422,
      json: () => Promise.resolve(fieldErrors),
    });

    await expect(createAssessmentBrief(brief, "key-1")).rejects.toMatchObject({
      status: 422,
      body: fieldErrors,
    });
    await expect(createAssessmentBrief(brief, "key-1")).rejects.toBeInstanceOf(CreateAssessmentBriefError);
  });

  it("throws CreateAssessmentBriefError with a { code, message } body on 409 IDEMPOTENCY_KEY_PAYLOAD_MISMATCH, detected by isIdempotencyKeyPayloadMismatch", async () => {
    mockApiClient.mockResolvedValue({
      ok: false,
      status: 409,
      json: () => Promise.resolve({ code: "IDEMPOTENCY_KEY_PAYLOAD_MISMATCH", message: "Same key, different payload" }),
    });

    let caught: unknown;
    try {
      await createAssessmentBrief(brief, "key-1");
    } catch (err) {
      caught = err;
    }

    expect(caught).toBeInstanceOf(CreateAssessmentBriefError);
    expect(isIdempotencyKeyPayloadMismatch(caught)).toBe(true);
  });
});

describe("generateAssessmentDraft", () => {
  beforeEach(() => jest.clearAllMocks());

  const sampleRevision: AssessmentDraftDto = {
    draftId: "draft-1",
    title: "Fibonacci",
    context: "ctx",
    instructions: "instr",
    objectives: ["obj"],
    deliverables: ["del"],
    constraints: ["con"],
    versionNumber: 1,
    origin: "AI_GENERATED",
    actorId: null,
    reason: null,
    previousRevisionId: null,
  };

  it("posts to /api/v1/assessments/{assessmentId}/draft with the Idempotency-Key header", async () => {
    mockApiClient.mockResolvedValue({ ok: true, status: 201, json: () => Promise.resolve(sampleRevision) });

    await generateAssessmentDraft("assess-1", "gen-key-1");

    expect(mockApiClient).toHaveBeenCalledWith("/api/v1/assessments/assess-1/draft", {
      method: "POST",
      headers: { "Idempotency-Key": "gen-key-1" },
    });
  });

  it("returns { outcome: 'revision-created', revision } on 201", async () => {
    mockApiClient.mockResolvedValue({ ok: true, status: 201, json: () => Promise.resolve(sampleRevision) });

    const result = await generateAssessmentDraft("assess-1", "gen-key-1");

    expect(result).toEqual({ outcome: "revision-created", revision: sampleRevision });
  });

  it("returns { outcome: 'operation-pending', operation } on 202 without throwing — a durable AiOperation record, not a crash", async () => {
    const operation = { id: "op-1", status: "FAILED_RETRYABLE", failureCode: "AGENT_UNAVAILABLE" };
    mockApiClient.mockResolvedValue({ ok: true, status: 202, json: () => Promise.resolve(operation) });

    const result = await generateAssessmentDraft("assess-1", "gen-key-1");

    expect(result).toEqual({ outcome: "operation-pending", operation });
  });

  it("throws GenerateAssessmentDraftError carrying the body and assessmentId on a genuine non-2xx/202 failure", async () => {
    mockApiClient.mockResolvedValue({
      ok: false,
      status: 409,
      json: () => Promise.resolve({ code: "ALREADY_GENERATED", message: "A revision already exists" }),
    });

    let caught: unknown;
    try {
      await generateAssessmentDraft("assess-1", "gen-key-1");
    } catch (err) {
      caught = err;
    }

    expect(caught).toBeInstanceOf(GenerateAssessmentDraftError);
    expect((caught as GenerateAssessmentDraftError).assessmentId).toBe("assess-1");
    expect(isAlreadyGeneratedConflict(caught)).toBe(true);
  });
});

describe("submitAssessmentBrief", () => {
  beforeEach(() => jest.clearAllMocks());

  const keys = { createIdempotencyKey: "create-key", generateIdempotencyKey: "generate-key" };

  it("calls createAssessmentBrief then generateAssessmentDraft with the respective idempotency keys, and returns the assessmentId", async () => {
    mockApiClient
      .mockResolvedValueOnce({ ok: true, status: 201, json: () => Promise.resolve({ assessmentId: "assess-1" }) })
      .mockResolvedValueOnce({
        ok: true,
        status: 201,
        json: () =>
          Promise.resolve({
            draftId: "d1",
            title: "t",
            context: "c",
            instructions: "i",
            objectives: [],
            deliverables: [],
            constraints: [],
            versionNumber: 1,
            origin: "AI_GENERATED",
            actorId: null,
            reason: null,
            previousRevisionId: null,
          }),
      });

    const result = await submitAssessmentBrief(brief, keys);

    expect(result).toEqual({ assessmentId: "assess-1" });
    expect(mockApiClient).toHaveBeenNthCalledWith(
      1,
      "/api/v1/assessments",
      expect.objectContaining({ method: "POST", headers: { "Idempotency-Key": "create-key" } })
    );
    expect(mockApiClient).toHaveBeenNthCalledWith(
      2,
      "/api/v1/assessments/assess-1/draft",
      expect.objectContaining({ method: "POST", headers: { "Idempotency-Key": "generate-key" } })
    );
  });

  it("does not throw when generation fails after brief creation succeeds — the assessment remains addressable, generate errors are swallowed", async () => {
    mockApiClient
      .mockResolvedValueOnce({ ok: true, status: 201, json: () => Promise.resolve({ assessmentId: "assess-1" }) })
      .mockResolvedValueOnce({
        ok: false,
        status: 503,
        json: () => Promise.resolve({ error: "AGENT_CALL_FAILED", message: "UNREACHABLE" }),
      });

    const result = await submitAssessmentBrief(brief, keys);

    expect(result).toEqual({ assessmentId: "assess-1" });
  });

  it("surfaces a CreateAssessmentBriefError when brief creation itself fails, and never calls generate", async () => {
    mockApiClient.mockResolvedValueOnce({
      ok: false,
      status: 422,
      json: () => Promise.resolve([{ field: "topic", message: "El tema no puede estar en blanco." }]),
    });

    let caught: unknown;
    try {
      await submitAssessmentBrief(brief, keys);
    } catch (err) {
      caught = err;
    }

    expect(caught).toBeInstanceOf(CreateAssessmentBriefError);
    expect(mockApiClient).toHaveBeenCalledTimes(1);
  });
});

const sampleDraft: AssessmentDraftDto = {
  draftId: "draft-uuid-1",
  title: "Recursividad: Fibonacci",
  context: "Evaluación práctica",
  instructions: "Implementa una función recursiva para calcular Fibonacci.",
  objectives: ["Comprender recursividad"],
  deliverables: ["Archivo .py"],
  constraints: ["No usar librerías externas"],
  versionNumber: 1,
  origin: "AI_GENERATED",
  actorId: null,
  reason: null,
  previousRevisionId: null,
};

describe("getAssessmentDraft", () => {
  beforeEach(() => jest.clearAllMocks());

  it("fetches from GET /api/v1/assessments/{assessmentId}/draft and returns AssessmentDraftDto", async () => {
    mockApiClient.mockResolvedValue({
      ok: true,
      status: 200,
      json: () => Promise.resolve(sampleDraft),
    });

    const result = await getAssessmentDraft("assess-1");

    expect(mockApiClient).toHaveBeenCalledWith("/api/v1/assessments/assess-1/draft");
    expect(result).toEqual(sampleDraft);
  });

  it("throws GetAssessmentDraftError carrying the ApiErrorResponse body, status, and assessmentId on failure", async () => {
    mockApiClient.mockResolvedValue({
      ok: false,
      status: 404,
      json: () => Promise.resolve({ error: "NOT_FOUND", message: "Draft not found" }),
    });

    await expect(getAssessmentDraft("assess-missing")).rejects.toMatchObject({
      status: 404,
      body: { error: "NOT_FOUND", message: "Draft not found" },
      assessmentId: "assess-missing",
    });
    await expect(getAssessmentDraft("assess-missing")).rejects.toBeInstanceOf(GetAssessmentDraftError);
  });
});

describe("getAssessmentDraftVersions", () => {
  beforeEach(() => jest.clearAllMocks());

  it("fetches from GET /api/v1/assessments/{assessmentId}/draft/versions and returns AssessmentDraftDto[]", async () => {
    const versions: AssessmentDraftDto[] = [
      { ...sampleDraft, versionNumber: 1 },
      { ...sampleDraft, title: "Recursividad: Fibonacci (revisado)", versionNumber: 2 },
      { ...sampleDraft, title: "Recursividad: Fibonacci con análisis", versionNumber: 3 },
    ];

    mockApiClient.mockResolvedValue({
      ok: true,
      status: 200,
      json: () => Promise.resolve(versions),
    });

    const result = await getAssessmentDraftVersions("assess-1");

    expect(mockApiClient).toHaveBeenCalledWith("/api/v1/assessments/assess-1/draft/versions");
    expect(result).toEqual(versions);
    expect(result).toHaveLength(3);
  });

  it("throws GetAssessmentDraftVersionsError carrying the ApiErrorResponse body, status, and assessmentId on failure", async () => {
    mockApiClient.mockResolvedValue({
      ok: false,
      status: 500,
      json: () => Promise.resolve({ error: "INTERNAL_ERROR", message: "Server error" }),
    });

    await expect(getAssessmentDraftVersions("assess-1")).rejects.toMatchObject({
      status: 500,
      body: { error: "INTERNAL_ERROR", message: "Server error" },
      assessmentId: "assess-1",
    });
    await expect(getAssessmentDraftVersions("assess-1")).rejects.toBeInstanceOf(GetAssessmentDraftVersionsError);
  });

  it("returns an empty array when no versions exist", async () => {
    mockApiClient.mockResolvedValue({
      ok: true,
      status: 200,
      json: () => Promise.resolve([]),
    });

    const result = await getAssessmentDraftVersions("assess-1");

    expect(result).toEqual([]);
  });
});

describe("getGenerationStatus", () => {
  beforeEach(() => jest.clearAllMocks());

  it("fetches from GET /api/v1/assessments/{assessmentId}/generation-status and returns the status DTO", async () => {
    const status = { operationType: "GENERATE_INITIAL_REVISION", status: "FAILED_RETRYABLE", failureCode: "AGENT_UNAVAILABLE", retryable: true };
    mockApiClient.mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve(status) });

    const result = await getGenerationStatus("assess-1");

    expect(mockApiClient).toHaveBeenCalledWith("/api/v1/assessments/assess-1/generation-status");
    expect(result).toEqual(status);
  });

  it("throws GetGenerationStatusError on failure, carrying status and assessmentId", async () => {
    mockApiClient.mockResolvedValue({ ok: false, status: 404, json: () => Promise.resolve({ error: "NOT_FOUND", message: null }) });

    await expect(getGenerationStatus("assess-1")).rejects.toBeInstanceOf(GetGenerationStatusError);
    await expect(getGenerationStatus("assess-1")).rejects.toMatchObject({ status: 404, assessmentId: "assess-1" });
  });
});

describe("retryAssessmentDraftGeneration", () => {
  beforeEach(() => jest.clearAllMocks());

  it("POSTs to /api/v1/assessments/{assessmentId}/draft/retry with no idempotency key and no body", async () => {
    mockApiClient.mockResolvedValue({ ok: true, status: 202, json: () => Promise.resolve({ id: "op-1", status: "IN_PROGRESS" }) });

    await retryAssessmentDraftGeneration("assess-1");

    expect(mockApiClient).toHaveBeenCalledWith("/api/v1/assessments/assess-1/draft/retry", { method: "POST" });
  });

  it("throws RetryAssessmentDraftGenerationError with a { code, message } body on 409 OPERATION_IN_PROGRESS", async () => {
    mockApiClient.mockResolvedValue({
      ok: false,
      status: 409,
      json: () => Promise.resolve({ code: "OPERATION_IN_PROGRESS", message: "An attempt may still be running" }),
    });

    let caught: unknown;
    try {
      await retryAssessmentDraftGeneration("assess-1");
    } catch (err) {
      caught = err;
    }

    expect(caught).toBeInstanceOf(RetryAssessmentDraftGenerationError);
    expect((caught as RetryAssessmentDraftGenerationError).body).toEqual({ code: "OPERATION_IN_PROGRESS", message: "An attempt may still be running" });
  });
});

describe("isRecoverableDraftMutationStatus", () => {
  it("classifies 422, 502, 503, and 409 as recoverable (409 covers expected typed conflicts like STALE_REVISION)", () => {
    expect(isRecoverableDraftMutationStatus(422)).toBe(true);
    expect(isRecoverableDraftMutationStatus(502)).toBe(true);
    expect(isRecoverableDraftMutationStatus(503)).toBe(true);
    expect(isRecoverableDraftMutationStatus(409)).toBe(true);
  });

  it("classifies 500 and 404 as not recoverable", () => {
    expect(isRecoverableDraftMutationStatus(500)).toBe(false);
    expect(isRecoverableDraftMutationStatus(404)).toBe(false);
  });
});

describe("createAssessmentRevision", () => {
  beforeEach(() => jest.clearAllMocks());

  it("POSTs to /api/v1/assessments/{assessmentId}/revisions with the changes and expectedRevisionId, no Idempotency-Key header", async () => {
    mockApiClient.mockResolvedValue({
      ok: true,
      status: 201,
      json: () => Promise.resolve({ ...sampleDraft, title: "Nuevo título", origin: "HUMAN_EDITED", actorId: "teacher-1" }),
    });

    await createAssessmentRevision("assess-1", { title: "Nuevo título" }, "rev-1");

    const [path, options] = mockApiClient.mock.calls[0];
    expect(path).toBe("/api/v1/assessments/assess-1/revisions");
    expect(options.method).toBe("POST");
    expect(options.headers).toBeUndefined();
    expect(JSON.parse(options.body)).toEqual({ title: "Nuevo título", expectedRevisionId: "rev-1" });
  });

  it("returns the new AssessmentDraftDto (origin HUMAN_EDITED) on success", async () => {
    const updated = { ...sampleDraft, title: "Nuevo título", origin: "HUMAN_EDITED" as const, actorId: "teacher-1" };
    mockApiClient.mockResolvedValue({ ok: true, status: 201, json: () => Promise.resolve(updated) });

    const result = await createAssessmentRevision("assess-1", { title: "Nuevo título" }, "rev-1");

    expect(result).toEqual(updated);
  });

  it("throws CreateAssessmentRevisionError with a { code, message } body on 409 STALE_REVISION, detected by isStaleRevisionConflict", async () => {
    mockApiClient.mockResolvedValue({
      ok: false,
      status: 409,
      json: () => Promise.resolve({ code: "STALE_REVISION", message: "expectedRevisionId no longer current" }),
    });

    let caught: unknown;
    try {
      await createAssessmentRevision("assess-1", { title: "x" }, "stale-rev");
    } catch (err) {
      caught = err;
    }

    expect(caught).toBeInstanceOf(CreateAssessmentRevisionError);
    expect(isStaleRevisionConflict(caught)).toBe(true);
  });

  it("throws CreateAssessmentRevisionError carrying field errors on 422", async () => {
    mockApiClient.mockResolvedValue({
      ok: false,
      status: 422,
      json: () => Promise.resolve([{ field: "title", message: "must not be blank if provided" }]),
    });

    await expect(createAssessmentRevision("assess-1", { title: "" }, "rev-1")).rejects.toBeInstanceOf(CreateAssessmentRevisionError);
  });
});

describe("regenerateAssessmentDraft", () => {
  beforeEach(() => jest.clearAllMocks());

  it("POSTs to /api/v1/assessments/{assessmentId}/draft/regenerate with the Idempotency-Key header and { adjustmentNotes, expectedRevisionId } body", async () => {
    const regenerated = { ...sampleDraft, versionNumber: 2 };
    mockApiClient.mockResolvedValue({ ok: true, status: 201, json: () => Promise.resolve(regenerated) });

    await regenerateAssessmentDraft("assess-1", "Hazlo más simple", "rev-1", "regen-key-1");

    expect(mockApiClient).toHaveBeenCalledWith("/api/v1/assessments/assess-1/draft/regenerate", {
      method: "POST",
      headers: { "Idempotency-Key": "regen-key-1" },
      body: JSON.stringify({ adjustmentNotes: "Hazlo más simple", expectedRevisionId: "rev-1" }),
    });
  });

  it("returns the new AssessmentDraftDto with an incremented versionNumber", async () => {
    const regenerated = { ...sampleDraft, versionNumber: 2 };
    mockApiClient.mockResolvedValue({ ok: true, status: 201, json: () => Promise.resolve(regenerated) });

    const result = await regenerateAssessmentDraft("assess-1", "Hazlo más simple", "rev-1", "regen-key-1");

    expect(result.versionNumber).toBe(2);
    expect(result).toEqual(regenerated);
  });

  it("throws RegenerateAssessmentDraftError with a { code, message } body on 409 STALE_REVISION, detected by isStaleRevisionConflict", async () => {
    mockApiClient.mockResolvedValue({
      ok: false,
      status: 409,
      json: () => Promise.resolve({ code: "STALE_REVISION", message: "expectedRevisionId no longer current" }),
    });

    let caught: unknown;
    try {
      await regenerateAssessmentDraft("assess-1", "notes", "stale-rev", "regen-key-1");
    } catch (err) {
      caught = err;
    }

    expect(caught).toBeInstanceOf(RegenerateAssessmentDraftError);
    expect(isStaleRevisionConflict(caught)).toBe(true);
  });

  it("throws RegenerateAssessmentDraftError carrying the ApiErrorResponse body, status, and assessmentId on 422 (empty/agent-rejected notes)", async () => {
    mockApiClient.mockResolvedValue({
      ok: false,
      status: 422,
      json: () => Promise.resolve({ error: "VALIDATION_FAILED", message: "adjustmentNotes must not be blank" }),
    });

    await expect(regenerateAssessmentDraft("assess-1", "", "rev-1", "regen-key-1")).rejects.toMatchObject({
      status: 422,
      body: { error: "VALIDATION_FAILED", message: "adjustmentNotes must not be blank" },
      assessmentId: "assess-1",
    });
    await expect(regenerateAssessmentDraft("assess-1", "", "rev-1", "regen-key-1")).rejects.toBeInstanceOf(RegenerateAssessmentDraftError);
  });

  it("throws RegenerateAssessmentDraftError on 502 (agent down)", async () => {
    mockApiClient.mockResolvedValue({
      ok: false,
      status: 502,
      json: () => Promise.resolve({ error: "AGENT_CALL_FAILED", message: "AGENT_ERROR" }),
    });

    await expect(regenerateAssessmentDraft("assess-1", "notes", "rev-1", "regen-key-1")).rejects.toBeInstanceOf(RegenerateAssessmentDraftError);
  });
});

describe("createAssessmentRevision / regenerateAssessmentDraft — logging level by criticality", () => {
  beforeEach(() => jest.clearAllMocks());

  it("logs WARN (not ERROR) for a 409 STALE_REVISION on createAssessmentRevision — expected, user-actionable conflict, not a failure", async () => {
    const { logger } = jest.requireMock("@/lib/logging/logger") as { logger: { warn: jest.Mock; error: jest.Mock } };

    mockApiClient.mockResolvedValue({
      ok: false,
      status: 409,
      json: () => Promise.resolve({ code: "STALE_REVISION", message: "stale" }),
    });

    await expect(createAssessmentRevision("assess-1", { title: "x" }, "rev-1")).rejects.toBeInstanceOf(CreateAssessmentRevisionError);

    expect(logger.warn).toHaveBeenCalled();
    expect(logger.error).not.toHaveBeenCalled();
  });

  it("logs ERROR (not WARN) for a generic 500 on createAssessmentRevision", async () => {
    const { logger } = jest.requireMock("@/lib/logging/logger") as { logger: { warn: jest.Mock; error: jest.Mock } };

    mockApiClient.mockResolvedValue({
      ok: false,
      status: 500,
      json: () => Promise.resolve({ error: "INTERNAL_ERROR", message: "Server error" }),
    });

    await expect(createAssessmentRevision("assess-1", { title: "x" }, "rev-1")).rejects.toBeInstanceOf(CreateAssessmentRevisionError);

    expect(logger.error).toHaveBeenCalled();
    expect(logger.warn).not.toHaveBeenCalled();
  });

  it("logs WARN (not ERROR) for 422/502/503 on regenerateAssessmentDraft", async () => {
    const { logger } = jest.requireMock("@/lib/logging/logger") as { logger: { warn: jest.Mock; error: jest.Mock } };

    mockApiClient.mockResolvedValue({
      ok: false,
      status: 502,
      json: () => Promise.resolve({ error: "AGENT_CALL_FAILED", message: "AGENT_ERROR" }),
    });

    await expect(regenerateAssessmentDraft("assess-1", "notes", "rev-1", "regen-key-1")).rejects.toBeInstanceOf(RegenerateAssessmentDraftError);

    expect(logger.warn).toHaveBeenCalled();
    expect(logger.error).not.toHaveBeenCalled();
  });

  it("logs ERROR (not WARN) for a generic 500 on regenerateAssessmentDraft", async () => {
    const { logger } = jest.requireMock("@/lib/logging/logger") as { logger: { warn: jest.Mock; error: jest.Mock } };

    mockApiClient.mockResolvedValue({
      ok: false,
      status: 500,
      json: () => Promise.resolve({ error: "INTERNAL_ERROR", message: "Server error" }),
    });

    await expect(regenerateAssessmentDraft("assess-1", "notes", "rev-1", "regen-key-1")).rejects.toBeInstanceOf(RegenerateAssessmentDraftError);

    expect(logger.error).toHaveBeenCalled();
    expect(logger.warn).not.toHaveBeenCalled();
  });
});
