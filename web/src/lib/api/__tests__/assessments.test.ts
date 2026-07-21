import {
  createAssessmentBrief,
  generateAssessmentDraft,
  submitAssessmentBrief,
  getAssessmentDraft,
  getAssessmentDraftVersions,
  CreateAssessmentBriefError,
  GenerateAssessmentDraftError,
  GetAssessmentDraftError,
  GetAssessmentDraftVersionsError,
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
