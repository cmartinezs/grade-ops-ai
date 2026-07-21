import {
  createAssessmentBrief,
  generateAssessmentDraft,
  submitAssessmentBrief,
  getAssessmentDraft,
  getAssessmentDraftVersions,
  updateAssessmentDraft,
  regenerateAssessmentDraft,
  isRecoverableDraftMutationStatus,
  CreateAssessmentBriefError,
  GenerateAssessmentDraftError,
  GetAssessmentDraftError,
  GetAssessmentDraftVersionsError,
  UpdateAssessmentDraftError,
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

  it("sends the exact CreateAssessmentBriefRequestDto shape to POST /api/v1/assessments and parses {assessmentId}", async () => {
    mockApiClient.mockResolvedValue({
      ok: true,
      status: 201,
      json: () => Promise.resolve({ assessmentId: "assess-1" }),
    });

    const result = await createAssessmentBrief(brief);

    expect(mockApiClient).toHaveBeenCalledWith("/api/v1/assessments", {
      method: "POST",
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

    await expect(createAssessmentBrief(brief)).rejects.toMatchObject({
      status: 422,
      body: fieldErrors,
    });
    await expect(createAssessmentBrief(brief)).rejects.toBeInstanceOf(CreateAssessmentBriefError);
  });
});

describe("generateAssessmentDraft", () => {
  beforeEach(() => jest.clearAllMocks());

  it("posts to /api/v1/assessments/{assessmentId}/draft with the given assessmentId", async () => {
    mockApiClient.mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve({}) });

    await generateAssessmentDraft("assess-1");

    expect(mockApiClient).toHaveBeenCalledWith("/api/v1/assessments/assess-1/draft", { method: "POST" });
  });

  it("throws GenerateAssessmentDraftError carrying the ApiErrorResponse body and assessmentId on failure", async () => {
    mockApiClient.mockResolvedValue({
      ok: false,
      status: 502,
      json: () => Promise.resolve({ error: "AGENT_CALL_FAILED", message: "AGENT_ERROR" }),
    });

    await expect(generateAssessmentDraft("assess-1")).rejects.toMatchObject({
      status: 502,
      body: { error: "AGENT_CALL_FAILED", message: "AGENT_ERROR" },
      assessmentId: "assess-1",
    });
    await expect(generateAssessmentDraft("assess-1")).rejects.toBeInstanceOf(GenerateAssessmentDraftError);
  });
});

describe("submitAssessmentBrief", () => {
  beforeEach(() => jest.clearAllMocks());

  it("calls createAssessmentBrief then generateAssessmentDraft with the returned assessmentId, and returns it", async () => {
    mockApiClient
      .mockResolvedValueOnce({ ok: true, status: 201, json: () => Promise.resolve({ assessmentId: "assess-1" }) })
      .mockResolvedValueOnce({ ok: true, status: 200, json: () => Promise.resolve({}) });

    const result = await submitAssessmentBrief(brief);

    expect(result).toEqual({ assessmentId: "assess-1" });
    expect(mockApiClient).toHaveBeenNthCalledWith(1, "/api/v1/assessments", expect.objectContaining({ method: "POST" }));
    expect(mockApiClient).toHaveBeenNthCalledWith(2, "/api/v1/assessments/assess-1/draft", { method: "POST" });
  });

  it("surfaces a distinguishable GenerateAssessmentDraftError (carrying assessmentId) when draft generation fails after brief creation succeeds", async () => {
    mockApiClient
      .mockResolvedValueOnce({ ok: true, status: 201, json: () => Promise.resolve({ assessmentId: "assess-1" }) })
      .mockResolvedValueOnce({
        ok: false,
        status: 503,
        json: () => Promise.resolve({ error: "AGENT_CALL_FAILED", message: "UNREACHABLE" }),
      });

    let caught: unknown;
    try {
      await submitAssessmentBrief(brief);
    } catch (err) {
      caught = err;
    }

    expect(caught).toBeInstanceOf(GenerateAssessmentDraftError);
    expect(caught).not.toBeInstanceOf(CreateAssessmentBriefError);
    expect((caught as GenerateAssessmentDraftError).assessmentId).toBe("assess-1");
  });

  it("surfaces a CreateAssessmentBriefError (not GenerateAssessmentDraftError) when brief creation itself fails", async () => {
    mockApiClient.mockResolvedValueOnce({
      ok: false,
      status: 422,
      json: () => Promise.resolve([{ field: "topic", message: "El tema no puede estar en blanco." }]),
    });

    let caught: unknown;
    try {
      await submitAssessmentBrief(brief);
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

describe("isRecoverableDraftMutationStatus", () => {
  it("classifies 422, 502, and 503 as recoverable", () => {
    expect(isRecoverableDraftMutationStatus(422)).toBe(true);
    expect(isRecoverableDraftMutationStatus(502)).toBe(true);
    expect(isRecoverableDraftMutationStatus(503)).toBe(true);
  });

  it("classifies 500, 409, and 404 as not recoverable", () => {
    expect(isRecoverableDraftMutationStatus(500)).toBe(false);
    expect(isRecoverableDraftMutationStatus(409)).toBe(false);
    expect(isRecoverableDraftMutationStatus(404)).toBe(false);
  });
});

describe("updateAssessmentDraft", () => {
  beforeEach(() => jest.clearAllMocks());

  it("PATCHes /api/v1/assessments/{assessmentId}/draft sending only the caller-provided keys", async () => {
    mockApiClient.mockResolvedValue({
      ok: true,
      status: 200,
      json: () => Promise.resolve({ ...sampleDraft, title: "Nuevo título", versionNumber: 1 }),
    });

    await updateAssessmentDraft("assess-1", { title: "Nuevo título" });

    expect(mockApiClient).toHaveBeenCalledWith("/api/v1/assessments/assess-1/draft", {
      method: "PATCH",
      body: JSON.stringify({ title: "Nuevo título" }),
    });
    // The serialized body must not contain unspecified fields at all — not even as
    // empty strings or nulls — since the backend treats absence as "don't change this field".
    const [, options] = mockApiClient.mock.calls[0];
    const sentBody = JSON.parse(options.body);
    expect(Object.keys(sentBody)).toEqual(["title"]);
  });

  it("sends multiple changed keys together without including unspecified ones", async () => {
    mockApiClient.mockResolvedValue({
      ok: true,
      status: 200,
      json: () => Promise.resolve(sampleDraft),
    });

    await updateAssessmentDraft("assess-1", { context: "Nuevo contexto", objectives: ["Nuevo objetivo"] });

    const [, options] = mockApiClient.mock.calls[0];
    const sentBody = JSON.parse(options.body);
    expect(sentBody).toEqual({ context: "Nuevo contexto", objectives: ["Nuevo objetivo"] });
  });

  it("returns the updated AssessmentDraftDto on success", async () => {
    const updated = { ...sampleDraft, title: "Nuevo título" };
    mockApiClient.mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve(updated) });

    const result = await updateAssessmentDraft("assess-1", { title: "Nuevo título" });

    expect(result).toEqual(updated);
  });

  it("throws UpdateAssessmentDraftError carrying the ApiErrorResponse body, status, and assessmentId on 422", async () => {
    mockApiClient.mockResolvedValue({
      ok: false,
      status: 422,
      json: () => Promise.resolve({ error: "VALIDATION_FAILED", message: "El título no puede estar en blanco." }),
    });

    await expect(updateAssessmentDraft("assess-1", { title: "" })).rejects.toMatchObject({
      status: 422,
      body: { error: "VALIDATION_FAILED", message: "El título no puede estar en blanco." },
      assessmentId: "assess-1",
    });
    await expect(updateAssessmentDraft("assess-1", { title: "" })).rejects.toBeInstanceOf(UpdateAssessmentDraftError);
  });

  it("throws UpdateAssessmentDraftError on 502/503 (agent down) distinctly logged from 500", async () => {
    mockApiClient.mockResolvedValue({
      ok: false,
      status: 503,
      json: () => Promise.resolve({ error: "AGENT_CALL_FAILED", message: "AGENT_ERROR" }),
    });

    await expect(updateAssessmentDraft("assess-1", { title: "x" })).rejects.toBeInstanceOf(UpdateAssessmentDraftError);
  });

  it("does not throw a 409 error — no draft endpoint returns one (task-07)", async () => {
    // Confirms the error surface has no 409 branch: a 409 falls through the same
    // generic error path as any other non-recoverable status.
    mockApiClient.mockResolvedValue({
      ok: false,
      status: 409,
      json: () => Promise.resolve({ error: "CONFLICT", message: "should never happen" }),
    });

    await expect(updateAssessmentDraft("assess-1", { title: "x" })).rejects.toMatchObject({ status: 409 });
    await expect(updateAssessmentDraft("assess-1", { title: "x" })).rejects.toBeInstanceOf(UpdateAssessmentDraftError);
  });
});

describe("regenerateAssessmentDraft", () => {
  beforeEach(() => jest.clearAllMocks());

  it("POSTs to /api/v1/assessments/{assessmentId}/draft/regenerate with { adjustmentNotes }", async () => {
    const regenerated = { ...sampleDraft, versionNumber: 2 };
    mockApiClient.mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve(regenerated) });

    await regenerateAssessmentDraft("assess-1", "Hazlo más simple");

    expect(mockApiClient).toHaveBeenCalledWith("/api/v1/assessments/assess-1/draft/regenerate", {
      method: "POST",
      body: JSON.stringify({ adjustmentNotes: "Hazlo más simple" }),
    });
  });

  it("returns the new AssessmentDraftDto with an incremented versionNumber", async () => {
    const regenerated = { ...sampleDraft, versionNumber: 2 };
    mockApiClient.mockResolvedValue({ ok: true, status: 200, json: () => Promise.resolve(regenerated) });

    const result = await regenerateAssessmentDraft("assess-1", "Hazlo más simple");

    expect(result.versionNumber).toBe(2);
    expect(result).toEqual(regenerated);
  });

  it("throws RegenerateAssessmentDraftError carrying the ApiErrorResponse body, status, and assessmentId on 422 (empty/agent-rejected notes)", async () => {
    mockApiClient.mockResolvedValue({
      ok: false,
      status: 422,
      json: () => Promise.resolve({ error: "VALIDATION_FAILED", message: "adjustmentNotes must not be blank" }),
    });

    await expect(regenerateAssessmentDraft("assess-1", "")).rejects.toMatchObject({
      status: 422,
      body: { error: "VALIDATION_FAILED", message: "adjustmentNotes must not be blank" },
      assessmentId: "assess-1",
    });
    await expect(regenerateAssessmentDraft("assess-1", "")).rejects.toBeInstanceOf(RegenerateAssessmentDraftError);
  });

  it("throws RegenerateAssessmentDraftError on 502 (agent down)", async () => {
    mockApiClient.mockResolvedValue({
      ok: false,
      status: 502,
      json: () => Promise.resolve({ error: "AGENT_CALL_FAILED", message: "AGENT_ERROR" }),
    });

    await expect(regenerateAssessmentDraft("assess-1", "notes")).rejects.toBeInstanceOf(RegenerateAssessmentDraftError);
  });

  it("throws RegenerateAssessmentDraftError on a generic 500 without a 409 branch (task-07: no draft endpoint returns 409)", async () => {
    mockApiClient.mockResolvedValue({
      ok: false,
      status: 500,
      json: () => Promise.resolve({ error: "INTERNAL_ERROR", message: "Server error" }),
    });

    await expect(regenerateAssessmentDraft("assess-1", "notes")).rejects.toMatchObject({ status: 500 });
    await expect(regenerateAssessmentDraft("assess-1", "notes")).rejects.toBeInstanceOf(RegenerateAssessmentDraftError);
  });
});

describe("updateAssessmentDraft / regenerateAssessmentDraft — logging level by criticality", () => {
  beforeEach(() => jest.clearAllMocks());

  it("logs WARN (not ERROR) for 422/502/503 on updateAssessmentDraft", async () => {
    const { logger } = jest.requireMock("@/lib/logging/logger") as { logger: { warn: jest.Mock; error: jest.Mock } };

    mockApiClient.mockResolvedValue({
      ok: false,
      status: 422,
      json: () => Promise.resolve({ error: "VALIDATION_FAILED", message: "bad input" }),
    });

    await expect(updateAssessmentDraft("assess-1", { title: "" })).rejects.toBeInstanceOf(UpdateAssessmentDraftError);

    expect(logger.warn).toHaveBeenCalled();
    expect(logger.error).not.toHaveBeenCalled();
  });

  it("logs ERROR (not WARN) for a generic 500 on updateAssessmentDraft", async () => {
    const { logger } = jest.requireMock("@/lib/logging/logger") as { logger: { warn: jest.Mock; error: jest.Mock } };

    mockApiClient.mockResolvedValue({
      ok: false,
      status: 500,
      json: () => Promise.resolve({ error: "INTERNAL_ERROR", message: "Server error" }),
    });

    await expect(updateAssessmentDraft("assess-1", { title: "x" })).rejects.toBeInstanceOf(UpdateAssessmentDraftError);

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

    await expect(regenerateAssessmentDraft("assess-1", "notes")).rejects.toBeInstanceOf(RegenerateAssessmentDraftError);

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

    await expect(regenerateAssessmentDraft("assess-1", "notes")).rejects.toBeInstanceOf(RegenerateAssessmentDraftError);

    expect(logger.error).toHaveBeenCalled();
    expect(logger.warn).not.toHaveBeenCalled();
  });
});
