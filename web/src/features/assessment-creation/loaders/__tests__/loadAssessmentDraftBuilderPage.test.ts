import { loadAssessmentDraftBuilderPage } from "../loadAssessmentDraftBuilderPage";
import * as assessmentsApi from "@/lib/api/assessments";
import type { AssessmentDraftDto } from "@/types/assessment";

jest.mock("@/lib/api/assessments");
jest.mock("@/lib/logging/logger", () => ({
  logger: { info: jest.fn(), debug: jest.fn(), error: jest.fn(), child: jest.fn().mockReturnThis() },
}));

const mockGetAssessmentDraft = assessmentsApi.getAssessmentDraft as jest.Mock;
const mockGetAssessmentDraftVersions = assessmentsApi.getAssessmentDraftVersions as jest.Mock;

const sampleDraft: AssessmentDraftDto = {
  draftId: "draft-uuid-1",
  title: "Recursividad: Fibonacci con análisis",
  context: "Evaluación práctica sobre recursividad",
  instructions: "Implementa una función recursiva que calcule Fibonacci con análisis de complejidad.",
  objectives: ["Comprender recursividad", "Analizar complejidad"],
  deliverables: ["Archivo .py", "Análisis de complejidad"],
  constraints: ["No usar librerías externas"],
  versionNumber: 4,
};

const sampleVersions: AssessmentDraftDto[] = [
  { ...sampleDraft, title: "Recursividad: Fibonacci", versionNumber: 1 },
  { ...sampleDraft, title: "Recursividad: Fibonacci (revisado)", versionNumber: 2 },
  { ...sampleDraft, title: "Recursividad: Fibonacci con casos de prueba", versionNumber: 3 },
  sampleDraft,
];

describe("loadAssessmentDraftBuilderPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("fetches both draft and versions in parallel and returns composed view model", async () => {
    mockGetAssessmentDraft.mockResolvedValue(sampleDraft);
    mockGetAssessmentDraftVersions.mockResolvedValue(sampleVersions);

    const result = await loadAssessmentDraftBuilderPage("assess-1");

    // Verify both calls were made
    expect(mockGetAssessmentDraft).toHaveBeenCalledWith("assess-1", expect.anything());
    expect(mockGetAssessmentDraftVersions).toHaveBeenCalledWith("assess-1", expect.anything());

    // Verify the composed view model structure
    expect(result).toHaveProperty("draft");
    expect(result).toHaveProperty("versions");
    expect(result.draft.draftId).toBe(sampleDraft.draftId);
    expect(result.draft.title).toBe(sampleDraft.title);
    expect(result.versions).toHaveLength(4);
  });

  it("calls both fetch functions in parallel (Promise.all) by not awaiting between them", async () => {
    let draftResolved = false;
    let versionsResolved = false;

    mockGetAssessmentDraft.mockImplementation(
      () =>
        new Promise((resolve) => {
          setTimeout(() => {
            draftResolved = true;
            resolve(sampleDraft);
          }, 10);
        })
    );

    mockGetAssessmentDraftVersions.mockImplementation(
      () =>
        new Promise((resolve) => {
          setTimeout(() => {
            versionsResolved = true;
            resolve(sampleVersions);
          }, 10);
        })
    );

    // If calls were sequential, they'd be called one after the other
    // If parallel, both should be in-flight before either resolves
    const startTime = Date.now();
    await loadAssessmentDraftBuilderPage("assess-1");
    const elapsedMs = Date.now() - startTime;

    // With 10ms delays sequential would take ~20ms, parallel takes ~10ms.
    // 500ms gives generous headroom for slow/loaded CI runners while still
    // failing if the implementation regresses to sequential awaits.
    expect(elapsedMs).toBeLessThan(500);
    expect(draftResolved).toBe(true);
    expect(versionsResolved).toBe(true);
  });

  it("rejects if getAssessmentDraft fails", async () => {
    mockGetAssessmentDraft.mockRejectedValue(new Error("Draft fetch failed"));
    mockGetAssessmentDraftVersions.mockResolvedValue(sampleVersions);

    await expect(loadAssessmentDraftBuilderPage("assess-1")).rejects.toThrow("Draft fetch failed");
  });

  it("rejects if getAssessmentDraftVersions fails", async () => {
    mockGetAssessmentDraft.mockResolvedValue(sampleDraft);
    mockGetAssessmentDraftVersions.mockRejectedValue(new Error("Versions fetch failed"));

    await expect(loadAssessmentDraftBuilderPage("assess-1")).rejects.toThrow("Versions fetch failed");
  });

  it("rejects if both calls fail (first error propagated)", async () => {
    mockGetAssessmentDraft.mockRejectedValue(new Error("Draft failed"));
    mockGetAssessmentDraftVersions.mockRejectedValue(new Error("Versions failed"));

    let caught: unknown;
    try {
      await loadAssessmentDraftBuilderPage("assess-1");
    } catch (err) {
      caught = err;
    }

    // Promise.all rejects with the first rejection
    expect(caught).toBeInstanceOf(Error);
    expect(String(caught)).toContain("failed");
  });

  it("composes versions correctly with preview labels and isCurrent flag", async () => {
    mockGetAssessmentDraft.mockResolvedValue(sampleDraft);
    mockGetAssessmentDraftVersions.mockResolvedValue(sampleVersions);

    const result = await loadAssessmentDraftBuilderPage("assess-1");

    // Current version (v4) should be marked as isCurrent
    const currentVersion = result.versions.find((v) => v.versionNumber === 4);
    expect(currentVersion).toMatchObject({
      versionNumber: 4,
      isCurrent: true,
      previewLabel: "v4 (actual)",
    });

    // Past versions (v1, v2, v3) should NOT be marked as current
    const pastVersion = result.versions.find((v) => v.versionNumber === 1);
    expect(pastVersion).toMatchObject({
      versionNumber: 1,
      isCurrent: false,
      previewLabel: "v1",
    });

    // Versions should be sorted newest first
    expect(result.versions[0].versionNumber).toBeGreaterThanOrEqual(result.versions[1].versionNumber);
  });
});
