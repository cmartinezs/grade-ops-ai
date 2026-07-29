import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { ShellProvider } from "@/components/shell/ShellContext";
import { DraftBuilderPageTestWrapper } from "@/features/assessment-creation/testUtils/DraftBuilderPageTestWrapper";
import * as assessmentsApi from "@/lib/api/assessments";
import {
  GetAssessmentDraftError,
  GetGenerationStatusError,
  CreateAssessmentRevisionError,
  RegenerateAssessmentDraftError,
} from "@/lib/api/assessments";
import type { AssessmentDraftDto, GenerationStatusDto } from "@/types/assessment";

// Mock AuthGuard to simulate an authenticated user
jest.mock("@/components/auth/AuthGuard", () => {
  return function MockAuthGuard({ children }: { children: React.ReactNode }) {
    return <>{children}</>;
  };
});

// Mock AppShell — only render children, no UI chrome
jest.mock("@/components/shell/AppShell", () => {
  return function MockAppShell({ children }: { children: React.ReactNode }) {
    return <>{children}</>;
  };
});

// Mock next/navigation for routing
jest.mock("next/navigation", () => ({
  useRouter: () => ({
    push: jest.fn(),
    replace: jest.fn(),
    prefetch: jest.fn(),
    back: jest.fn(),
    forward: jest.fn(),
  }),
  usePathname: () => "/assessments/test-id/draft",
  useSearchParams: () => new URLSearchParams(),
}));

// Real network boundary (apiClient) is mocked, not the page hook — this exercises the real
// loadAssessmentDraftBuilderPage + createAssessmentRevision/regenerateAssessmentDraft/
// getGenerationStatus/retryAssessmentDraftGeneration/generateAssessmentDraft call paths.
// A factory (not plain jest.mock(path)) is required here: auto-mocking would also replace the
// exported error classes, losing the constructor-assigned status/body/assessmentId properties
// these tests construct and assert on.
jest.mock("@/lib/api/assessments", () => ({
  ...jest.requireActual("@/lib/api/assessments"),
  getAssessmentDraft: jest.fn(),
  getAssessmentDraftVersions: jest.fn(),
  getGenerationStatus: jest.fn(),
  retryAssessmentDraftGeneration: jest.fn(),
  generateAssessmentDraft: jest.fn(),
  createAssessmentRevision: jest.fn(),
  regenerateAssessmentDraft: jest.fn(),
}));
jest.mock("@/lib/logging/logger", () => ({
  logger: { info: jest.fn(), warn: jest.fn(), error: jest.fn(), debug: jest.fn(), child: jest.fn().mockReturnThis() },
}));

const mockGetAssessmentDraft = assessmentsApi.getAssessmentDraft as jest.Mock;
const mockGetAssessmentDraftVersions = assessmentsApi.getAssessmentDraftVersions as jest.Mock;
const mockGetGenerationStatus = assessmentsApi.getGenerationStatus as jest.Mock;
const mockRetryAssessmentDraftGeneration = assessmentsApi.retryAssessmentDraftGeneration as jest.Mock;
const mockGenerateAssessmentDraft = assessmentsApi.generateAssessmentDraft as jest.Mock;
const mockCreateAssessmentRevision = assessmentsApi.createAssessmentRevision as jest.Mock;
const mockRegenerateAssessmentDraft = assessmentsApi.regenerateAssessmentDraft as jest.Mock;

const LONG_INSTRUCTIONS =
  "Implementa una función recursiva que calcule el n-ésimo número de la secuencia de Fibonacci. " +
  "Debe manejar casos base (n=0, n=1) explícitamente y evitar recursión no controlada para valores grandes de n. " +
  "Considera el costo computacional de tu solución: una recursión ingenua tiene complejidad exponencial; " +
  "explica en un comentario si tu solución usa memoización u otra técnica para mejorar el rendimiento. " +
  "Incluye al menos tres casos de prueba que cubran el caso base, un caso intermedio y un caso con un valor " +
  "de entrada considerablemente grande (por ejemplo, n=30), y documenta el tiempo de ejecución observado " +
  "para cada uno de ellos en un comentario al final de tu archivo de solución.";

function buildFakeVersions(): AssessmentDraftDto[] {
  return [
    {
      draftId: "draft-1",
      title: "Recursividad: Fibonacci",
      context: "Evaluación práctica sobre recursividad para el curso de Estructuras de Datos.",
      instructions: "Implementa una función recursiva para calcular Fibonacci.",
      objectives: ["Comprender recursividad básica"],
      deliverables: ["Archivo .py con la función implementada"],
      constraints: ["No usar librerías externas"],
      versionNumber: 1,
      origin: "AI_GENERATED",
      actorId: null,
      reason: null,
      previousRevisionId: null,
    },
    {
      draftId: "draft-2",
      title: "Recursividad: Fibonacci (revisado)",
      context: "Evaluación práctica sobre recursividad para el curso de Estructuras de Datos.",
      instructions: "Implementa una función recursiva para calcular Fibonacci, manejando casos base.",
      objectives: ["Comprender recursividad básica", "Identificar casos base"],
      deliverables: ["Archivo .py con la función implementada"],
      constraints: ["No usar librerías externas"],
      versionNumber: 2,
      origin: "AI_GENERATED",
      actorId: null,
      reason: null,
      previousRevisionId: "draft-1",
    },
    {
      draftId: "draft-3",
      title: "Recursividad: Fibonacci con casos de prueba",
      context: "Evaluación práctica sobre recursividad para el curso de Estructuras de Datos.",
      instructions: "Implementa una función recursiva para calcular Fibonacci y agrega casos de prueba.",
      objectives: ["Comprender recursividad básica", "Identificar casos base", "Escribir casos de prueba"],
      deliverables: ["Archivo .py con la función implementada", "Casos de prueba"],
      constraints: ["No usar librerías externas"],
      versionNumber: 3,
      origin: "AI_GENERATED",
      actorId: null,
      reason: null,
      previousRevisionId: "draft-2",
    },
    {
      draftId: "draft-4",
      title: "Recursividad: Fibonacci con análisis de complejidad",
      context: "Evaluación práctica sobre recursividad para el curso de Estructuras de Datos.",
      instructions: LONG_INSTRUCTIONS,
      objectives: [
        "Comprender recursividad básica",
        "Identificar casos base",
        "Escribir casos de prueba",
        "Analizar la complejidad computacional de una solución recursiva",
      ],
      deliverables: ["Archivo .py con la función implementada", "Casos de prueba", "Comentario con análisis de tiempo"],
      constraints: ["No usar librerías externas", "Documentar el tiempo de ejecución observado"],
      versionNumber: 4,
      origin: "AI_GENERATED",
      actorId: null,
      reason: null,
      previousRevisionId: "draft-3",
    },
  ];
}

describe("DraftBuilderPage (integration)", () => {
  const testAssessmentId = "test-assessment-123";
  let versions: AssessmentDraftDto[];

  function currentDraft() {
    return versions.find((v) => v.versionNumber === Math.max(...versions.map((x) => x.versionNumber)))!;
  }

  function renderPage() {
    return render(
      <ShellProvider>
        <DraftBuilderPageTestWrapper assessmentId={testAssessmentId} />
      </ShellProvider>
    );
  }

  beforeEach(() => {
    jest.clearAllMocks();
    versions = buildFakeVersions();
    mockGetAssessmentDraft.mockImplementation(async () => currentDraft());
    mockGetAssessmentDraftVersions.mockImplementation(async () => versions);
  });

  it("renders all three main sections without crashing", async () => {
    renderPage();

    await waitFor(() => expect(screen.getByLabelText(/^Título/)).toBeInTheDocument());
    expect(screen.getByLabelText(/^Contexto/)).toBeInTheDocument();
    expect(screen.getByLabelText(/^Instrucciones/)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /regenerar/i })).toBeInTheDocument();
    expect(screen.getByText(/Historial de versiones/i)).toBeInTheDocument();
  });

  it("calls the real loadAssessmentDraftBuilderPage (getAssessmentDraft + getAssessmentDraftVersions) on mount", async () => {
    renderPage();

    await waitFor(() => expect(mockGetAssessmentDraft).toHaveBeenCalledWith(testAssessmentId, expect.anything()));
    expect(mockGetAssessmentDraftVersions).toHaveBeenCalledWith(testAssessmentId, expect.anything());
  });

  it("displays the current draft with correct data", async () => {
    renderPage();

    const titleField = (await screen.findByLabelText(/^Título/)) as HTMLInputElement;
    expect(titleField.value).toBe("Recursividad: Fibonacci con análisis de complejidad");

    const contextField = screen.getByLabelText(/^Contexto/) as HTMLInputElement;
    expect(contextField.value).toContain("Evaluación práctica sobre recursividad para el curso de Estructuras de Datos");

    const instructionsField = screen.getByLabelText(/^Instrucciones/) as HTMLTextAreaElement;
    expect(instructionsField.value).toMatch(/Implementa una función recursiva/);
    expect(instructionsField.value).toMatch(/costo computacional/);
  });

  it("shows 'Generado por IA' provenance for the AI-generated current draft", async () => {
    renderPage();

    await screen.findByLabelText(/^Título/);
    expect(screen.getByText(/Generado por IA/)).toBeInTheDocument();
  });

  it("successful save calls createAssessmentRevision with the current revision's id as expectedRevisionId, refetches, and shows human-edited provenance", async () => {
    const user = userEvent.setup();
    mockCreateAssessmentRevision.mockImplementation(async (_id: string, changes: Partial<AssessmentDraftDto>, expectedRevisionId: string) => {
      expect(expectedRevisionId).toBe(currentDraft().draftId);
      const updated: AssessmentDraftDto = { ...currentDraft(), ...changes, origin: "HUMAN_EDITED", actorId: "teacher-1", reason: null };
      versions = versions.map((v) => (v.versionNumber === currentDraft().versionNumber ? updated : v));
      return updated;
    });
    renderPage();

    const titleField = (await screen.findByLabelText(/^Título/)) as HTMLInputElement;
    await user.clear(titleField);
    await user.type(titleField, "Nuevo Título");

    const saveButton = screen.getByRole("button", { name: /guardar cambios/i });
    await user.click(saveButton);

    await waitFor(() => expect(mockCreateAssessmentRevision).toHaveBeenCalledWith(testAssessmentId, expect.objectContaining({ title: "Nuevo Título" }), "draft-4"));
    // Refetch after success — getAssessmentDraft/getAssessmentDraftVersions called again beyond the initial mount call
    await waitFor(() => expect(mockGetAssessmentDraft).toHaveBeenCalledTimes(2));
    expect(mockGetAssessmentDraftVersions).toHaveBeenCalledTimes(2);
    await waitFor(() => expect(screen.getByLabelText(/^Título/)).toHaveValue("Nuevo Título"));
    const editorSection = screen.getByText("Editor de draft").closest("section");
    expect(editorSection?.textContent).toMatch(/Editado por teacher-1/);
  });

  it("successful regenerate calls regenerateAssessmentDraft with expectedRevisionId and a client-generated idempotency key, refetches, and shows the new version as current", async () => {
    const user = userEvent.setup();
    mockRegenerateAssessmentDraft.mockImplementation(
      async (_id: string, adjustmentNotes: string, expectedRevisionId: string, idempotencyKey: string) => {
        expect(expectedRevisionId).toBe(currentDraft().draftId);
        expect(typeof idempotencyKey).toBe("string");
        expect(idempotencyKey.length).toBeGreaterThan(0);
        const current = currentDraft();
        const regenerated: AssessmentDraftDto = {
          ...current,
          draftId: "draft-5",
          versionNumber: current.versionNumber + 1,
          instructions: `${current.instructions}\n\n(${adjustmentNotes})`,
          previousRevisionId: current.draftId,
        };
        versions = [...versions, regenerated];
        return regenerated;
      }
    );
    renderPage();

    await screen.findByLabelText(/^Título/);

    const notesInput = screen.getByLabelText(/^Notas de ajuste/);
    await user.type(notesInput, "Hazlo más simple");
    await user.click(screen.getByRole("button", { name: /regenerar con ia/i }));

    await waitFor(() => expect(mockRegenerateAssessmentDraft).toHaveBeenCalled());
    await waitFor(() => expect(mockGetAssessmentDraft).toHaveBeenCalledTimes(2));

    // v5 is now current — the "Historial de versiones" section shows it as the selected/current entry
    await waitFor(() => {
      const versionHistorySection = screen.getByText(/Historial de versiones/i).closest("section");
      expect(versionHistorySection?.textContent).toMatch(/v5 \(actual\)/);
    });
  });

  it("clicking a past version in history shows its full content read-only, without a second network call", async () => {
    const user = userEvent.setup();
    renderPage();

    await screen.findByLabelText(/^Título/);
    expect(mockGetAssessmentDraft).toHaveBeenCalledTimes(1);
    expect(mockGetAssessmentDraftVersions).toHaveBeenCalledTimes(1);

    // v1's title, per buildFakeVersions()
    await user.click(screen.getByRole("button", { name: /v1/i }));

    const titleField = screen.getByLabelText(/^Título/) as HTMLInputElement;
    await waitFor(() => expect(titleField.value).toBe("Recursividad: Fibonacci"));

    // Read-only while viewing a past version — no Save button — per task-08's hierarchy
    expect(screen.queryByRole("button", { name: /guardar cambios/i })).not.toBeInTheDocument();
    expect(screen.getByText(/viendo una versión anterior/i)).toBeInTheDocument();

    // Browsing history is purely a local selection — versionDrafts already has full content
    // for every version from the initial load, no facade bypass or second round-trip needed.
    expect(mockGetAssessmentDraft).toHaveBeenCalledTimes(1);
    expect(mockGetAssessmentDraftVersions).toHaveBeenCalledTimes(1);
  });

  it("displays version history section with multiple versions (many-versions edge case)", async () => {
    renderPage();

    await screen.findByLabelText(/^Título/);
    const versionHistorySection = screen.getByText(/Historial de versiones/i).closest("section");
    expect(versionHistorySection).toBeInTheDocument();

    const versionButtons = versionHistorySection?.querySelectorAll("button") ?? [];
    expect(versionButtons.length).toBeGreaterThanOrEqual(4);
    versionButtons.forEach((btn) => expect(btn).toBeInTheDocument());
  });

  it("displays regenerate section with functional controls", async () => {
    const user = userEvent.setup();
    renderPage();

    await screen.findByLabelText(/^Título/);
    const regenerateButton = screen.getByRole("button", { name: /Regenerar con IA/i });
    expect(regenerateButton).toBeInTheDocument();
    expect(regenerateButton).not.toBeDisabled();

    const adjustmentNotesInput = screen.getByLabelText(/^Notas de ajuste/) as HTMLTextAreaElement;
    await user.click(adjustmentNotesInput);
    await user.type(adjustmentNotesInput, "Test note");
    expect(adjustmentNotesInput.value).toContain("Test note");
  });

  it("resolves via generation-status on a 404 load, and shows the full-screen not-found state only when generation-status also reports not found", async () => {
    mockGetAssessmentDraft.mockRejectedValue(new GetAssessmentDraftError(404, { error: "NOT_FOUND", message: null }, testAssessmentId));
    mockGetAssessmentDraftVersions.mockRejectedValue(new GetAssessmentDraftError(404, { error: "NOT_FOUND", message: null }, testAssessmentId));
    mockGetGenerationStatus.mockRejectedValue(new GetGenerationStatusError(404, { error: "NOT_FOUND", message: null }, testAssessmentId));
    renderPage();

    await waitFor(() => expect(mockGetGenerationStatus).toHaveBeenCalledWith(testAssessmentId));
    await waitFor(() => expect(screen.getByRole("alert")).toHaveTextContent(/No encontramos esta evaluación/));
    expect(screen.queryByLabelText(/^Título/)).not.toBeInTheDocument();
  });

  it("shows a generic error message on a 500 during initial load, without calling generation-status (not a not-found signal)", async () => {
    mockGetAssessmentDraft.mockRejectedValue(new GetAssessmentDraftError(500, { error: "INTERNAL_ERROR", message: null }, testAssessmentId));
    renderPage();

    await waitFor(() => expect(screen.getByRole("alert")).toHaveTextContent(/inesperado/i));
    expect(mockGetGenerationStatus).not.toHaveBeenCalled();
  });

  it("agent-rejected (422) on regenerate shows a banner without clearing the current draft", async () => {
    const user = userEvent.setup();
    mockRegenerateAssessmentDraft.mockRejectedValue(
      new RegenerateAssessmentDraftError(422, { error: "AGENT_CALL_FAILED", message: "AGENT_REJECTED" }, testAssessmentId)
    );
    renderPage();

    const titleField = (await screen.findByLabelText(/^Título/)) as HTMLInputElement;
    const originalTitle = titleField.value;

    await user.type(screen.getByLabelText(/^Notas de ajuste/), "notas raras");
    await user.click(screen.getByRole("button", { name: /regenerar con ia/i }));

    await waitFor(() => expect(screen.getByRole("alert")).toHaveTextContent(/no pudimos regenerar/i));
    // Current draft is preserved — not cleared by the failed regenerate
    expect((screen.getByLabelText(/^Título/) as HTMLInputElement).value).toBe(originalTitle);
  });

  it("agent-down (502/503) on regenerate shows a distinct banner from agent-rejected", async () => {
    const user = userEvent.setup();
    mockRegenerateAssessmentDraft.mockRejectedValue(
      new RegenerateAssessmentDraftError(503, { error: "AGENT_CALL_FAILED", message: "UNREACHABLE" }, testAssessmentId)
    );
    renderPage();

    await screen.findByLabelText(/^Título/);
    await user.type(screen.getByLabelText(/^Notas de ajuste/), "notas");
    await user.click(screen.getByRole("button", { name: /regenerar con ia/i }));

    await waitFor(() => expect(screen.getByRole("alert")).toHaveTextContent(/no está disponible/i));
  });

  it("field validation (422) on save shows an inline error under the affected field, editor stays editable", async () => {
    const user = userEvent.setup();
    mockCreateAssessmentRevision.mockRejectedValue(
      new CreateAssessmentRevisionError(422, [{ field: "title", message: "must not be blank if provided" }], testAssessmentId)
    );
    renderPage();

    await screen.findByLabelText(/^Título/);
    await user.click(screen.getByRole("button", { name: /guardar cambios/i }));

    await waitFor(() => expect(screen.getByLabelText(/^Título/)).toBeEnabled());
    // The inline error is set via useDraftEditorSection's own effect reacting to the new
    // fieldErrors prop — that's an extra render tick after isSaving flips back, so this needs
    // its own waitFor rather than a synchronous assertion right after the isEnabled check.
    await waitFor(() => expect(screen.getByText(/el título no puede estar vacío/i)).toBeInTheDocument());
  });

  it("a 409 STALE_REVISION on save shows an explicit conflict banner, blocks further edits, and Recargar re-fetches instead of silently resubmitting", async () => {
    const user = userEvent.setup();
    mockCreateAssessmentRevision.mockRejectedValue(
      new CreateAssessmentRevisionError(409, { code: "STALE_REVISION", message: "expectedRevisionId no longer current" }, testAssessmentId)
    );
    renderPage();

    await screen.findByLabelText(/^Título/);
    await user.click(screen.getByRole("button", { name: /guardar cambios/i }));

    await waitFor(() => expect(screen.getByText(/la versión actual cambió mientras editabas/i)).toBeInTheDocument());
    // Blocked, not hidden — editor and regenerate stay visible but disabled until reload
    expect(screen.getByLabelText(/^Título/)).toBeDisabled();
    expect(screen.queryByRole("button", { name: /guardar cambios/i })).not.toBeInTheDocument();
    expect(screen.getByRole("button", { name: /regenerar con ia/i })).toBeDisabled();

    // Reload discards the in-flight edit and re-fetches — not an automatic silent resubmit
    mockCreateAssessmentRevision.mockClear();
    await user.click(screen.getByRole("button", { name: /recargar/i }));

    await waitFor(() => expect(mockGetAssessmentDraft).toHaveBeenCalledTimes(2));
    await waitFor(() => expect(screen.queryByText(/la versión actual cambió mientras editabas/i)).not.toBeInTheDocument());
    expect(screen.getByLabelText(/^Título/)).not.toBeDisabled();
    expect(mockCreateAssessmentRevision).not.toHaveBeenCalled();
  });

  describe("resume-on-load states (LOCAL-CONTRACTS.md § Recovery after refresh)", () => {
    beforeEach(() => {
      mockGetAssessmentDraft.mockRejectedValue(new GetAssessmentDraftError(404, { error: "NOT_FOUND", message: null }, testAssessmentId));
      mockGetAssessmentDraftVersions.mockRejectedValue(new GetAssessmentDraftError(404, { error: "NOT_FOUND", message: null }, testAssessmentId));
    });

    function generationStatus(overrides: Partial<GenerationStatusDto>): GenerationStatusDto {
      return { operationType: "GENERATE_INITIAL_REVISION", status: "NOT_STARTED", retryable: false, currentRevisionId: null, ...overrides };
    }

    it("NOT_STARTED shows a Generate action; clicking it calls generateAssessmentDraft with a fresh idempotency key and reloads", async () => {
      const user = userEvent.setup();
      mockGetGenerationStatus.mockResolvedValue(generationStatus({ status: "NOT_STARTED" }));
      mockGenerateAssessmentDraft.mockImplementation(async () => {
        mockGetAssessmentDraft.mockResolvedValue(currentDraft());
        mockGetAssessmentDraftVersions.mockResolvedValue(versions);
        return { outcome: "revision-created", revision: currentDraft() };
      });
      renderPage();

      await waitFor(() => expect(screen.getByText(/aún no se ha generado un borrador/i)).toBeInTheDocument());
      await user.click(screen.getByRole("button", { name: /generar borrador/i }));

      await waitFor(() => expect(mockGenerateAssessmentDraft).toHaveBeenCalledWith(testAssessmentId, expect.any(String)));
      await waitFor(() => expect(screen.getByLabelText(/^Título/)).toBeInTheDocument());
    });

    it("IN_PROGRESS shows a pending state with no retry action, to avoid double-dispatch", async () => {
      mockGetGenerationStatus.mockResolvedValue(generationStatus({ status: "IN_PROGRESS" }));
      renderPage();

      await waitFor(() => expect(screen.getByText(/generando el borrador/i)).toBeInTheDocument());
      expect(screen.queryByRole("button", { name: /reintentar/i })).not.toBeInTheDocument();
    });

    it("FAILED_RETRYABLE shows a Retry action; clicking it calls retryAssessmentDraftGeneration with no key/body and reloads", async () => {
      const user = userEvent.setup();
      mockGetGenerationStatus.mockResolvedValue(generationStatus({ status: "FAILED_RETRYABLE", failureCode: "AGENT_UNAVAILABLE", retryable: true }));
      mockRetryAssessmentDraftGeneration.mockImplementation(async () => {
        mockGetAssessmentDraft.mockResolvedValue(currentDraft());
        mockGetAssessmentDraftVersions.mockResolvedValue(versions);
        return { id: "op-1", status: "IN_PROGRESS" };
      });
      renderPage();

      await waitFor(() => expect(screen.getByRole("alert")).toHaveTextContent(/no pudimos generar el borrador/i));
      await user.click(screen.getByRole("button", { name: /^reintentar$/i }));

      expect(mockRetryAssessmentDraftGeneration).toHaveBeenCalledWith(testAssessmentId);
      await waitFor(() => expect(screen.getByLabelText(/^Título/)).toBeInTheDocument());
    });

    it("INDETERMINATE shows a cautionary retry-anyway action, distinct copy from FAILED_RETRYABLE", async () => {
      mockGetGenerationStatus.mockResolvedValue(generationStatus({ status: "INDETERMINATE" }));
      renderPage();

      await waitFor(() => expect(screen.getByRole("alert")).toHaveTextContent(/no pudimos confirmar el estado de la generación/i));
      expect(screen.getByRole("button", { name: /reintentar de todas formas/i })).toBeInTheDocument();
    });
  });
});
