import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { ShellProvider } from "@/components/shell/ShellContext";
import NewAssessmentPage from "@/app/(protected)/assessments/new/page";
import { DraftBuilderPageTestWrapper } from "../testUtils/DraftBuilderPageTestWrapper";
import { apiClient } from "@/lib/api/client";
import type { AssessmentDraftDto } from "@/types/assessment";

// This is the single most important test in the web packet (see TEST-PLAN.md § The single
// most important test in this packet): it reproduces Research 02 §5.6's demonstrated dead end
// end-to-end from the client side — brief created, generation fails, reload, retry succeeds,
// exactly one Assessment — and is the regression guard for the fix. Mocking at the apiClient
// (transport) boundary rather than at individual @/lib/api/assessments functions means the
// real createAssessmentBrief/generateAssessmentDraft/getAssessmentDraft/getGenerationStatus/
// retryAssessmentDraftGeneration/submitAssessmentBrief implementations all run for real; the
// "exactly one Assessment" assertion below counts real POST /api/v1/assessments calls.
jest.mock("@/lib/api/client");
jest.mock("@/lib/logging/logger", () => ({
  logger: { info: jest.fn(), warn: jest.fn(), error: jest.fn(), debug: jest.fn(), child: jest.fn().mockReturnThis() },
}));

const mockPush = jest.fn();
jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: mockPush }),
}));

const mockApiClient = apiClient as jest.Mock;

interface MockResponse {
  ok: boolean;
  status: number;
  json: () => Promise<unknown>;
}

function jsonResponse(status: number, body: unknown): MockResponse {
  return { ok: status >= 200 && status < 300, status, json: () => Promise.resolve(body) };
}

const ASSESSMENT_ID = "assess-1";

function buildRevision(overrides: Partial<AssessmentDraftDto> = {}): AssessmentDraftDto {
  return {
    draftId: "revision-1",
    title: "Recursividad: Fibonacci",
    context: "Evaluación práctica sobre recursividad.",
    instructions: "Implementa una función recursiva para calcular Fibonacci.",
    objectives: ["Comprender recursividad básica"],
    deliverables: ["Archivo .py con la función implementada"],
    constraints: ["No usar librerías externas"],
    versionNumber: 1,
    origin: "AI_GENERATED",
    actorId: null,
    reason: null,
    previousRevisionId: null,
    ...overrides,
  };
}

describe("Research 02 §5.6 regression: resume a failed generation after refresh without duplicating the Assessment", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("brief created, generation fails (durable, retryable), reload shows Retry, retry succeeds, and POST /api/v1/assessments was called exactly once throughout", async () => {
    const user = userEvent.setup();
    let generationSucceededServerSide = false;

    mockApiClient.mockImplementation(async (path: string, options: RequestInit = {}) => {
      const method = options.method ?? "GET";
      const key = `${method} ${path}`;

      // 1. Create the assessment intent — the only call that may ever create an Assessment.
      if (key === `POST /api/v1/assessments`) {
        return jsonResponse(201, { assessmentId: ASSESSMENT_ID });
      }

      // 2. Generate the initial revision — fails, but durably (202, not a bare 5xx). This is
      // the exact defect from Research 02 §5.6: the old code had no way back to this assessment.
      if (key === `POST /api/v1/assessments/${ASSESSMENT_ID}/draft`) {
        return jsonResponse(202, { id: "op-1", status: "FAILED_RETRYABLE", failureCode: "AGENT_UNAVAILABLE" });
      }

      // 3. On the draft page's load: no revision exists yet.
      if (key === `GET /api/v1/assessments/${ASSESSMENT_ID}/draft`) {
        if (generationSucceededServerSide) {
          return jsonResponse(200, buildRevision());
        }
        return jsonResponse(404, { error: "NOT_FOUND", message: null });
      }
      if (key === `GET /api/v1/assessments/${ASSESSMENT_ID}/draft/versions`) {
        if (generationSucceededServerSide) {
          return jsonResponse(200, [buildRevision()]);
        }
        return jsonResponse(404, { error: "NOT_FOUND", message: null });
      }

      // 4. The resume-on-load check: disambiguates "no revision yet" into a concrete,
      // recoverable state instead of the old dead end.
      if (key === `GET /api/v1/assessments/${ASSESSMENT_ID}/generation-status`) {
        return jsonResponse(200, {
          operationType: "GENERATE_INITIAL_REVISION",
          status: "FAILED_RETRYABLE",
          failureCode: "AGENT_UNAVAILABLE",
          retryable: true,
          currentRevisionId: generationSucceededServerSide ? "revision-1" : null,
        });
      }

      // 5. The user-initiated retry — targets the same AiOperation, no idempotency key.
      if (key === `POST /api/v1/assessments/${ASSESSMENT_ID}/draft/retry`) {
        generationSucceededServerSide = true;
        return jsonResponse(202, { id: "op-1", status: "IN_PROGRESS" });
      }

      throw new Error(`Unexpected apiClient call: ${key}`);
    });

    // --- Step 1: submit the intake form; create succeeds, generate fails durably. ---
    const { unmount: unmountIntake } = render(
      <ShellProvider>
        <NewAssessmentPage />
      </ShellProvider>
    );

    await user.type(screen.getByLabelText(/^Objetivo de aprendizaje/), "Entender recursividad");
    await user.type(screen.getByLabelText(/^Tema/), "Recursion");
    await user.type(screen.getByLabelText(/^Nivel/), "Intermedio");
    await user.type(screen.getByLabelText(/^Duración/), "45 min");
    await user.type(screen.getByLabelText(/^Idioma/), "Java");
    await user.click(screen.getByRole("button", { name: /crear evaluación/i }));

    // Web does not invent recovery behavior here — it navigates to the draft page regardless
    // of generation's outcome, because the assessment is already durably created and addressable.
    await waitFor(() => expect(mockPush).toHaveBeenCalledWith(`/assessments/${ASSESSMENT_ID}/draft`));
    unmountIntake();

    // --- Step 2: navigate to the draft page (simulated: mount it for the returned id). This
    // is the "reload" — the page has no client-side memory of what just happened, it only has
    // the assessmentId from the URL, and must resolve everything else from the server. ---
    render(
      <ShellProvider>
        <DraftBuilderPageTestWrapper assessmentId={ASSESSMENT_ID} />
      </ShellProvider>
    );

    await waitFor(() => expect(screen.getByRole("alert")).toHaveTextContent(/no pudimos generar el borrador/i));
    expect(screen.getByRole("button", { name: /reintentar/i })).toBeInTheDocument();

    // --- Step 3: retry. ---
    await user.click(screen.getByRole("button", { name: /reintentar/i }));

    // --- Step 4: generation has now succeeded server-side; the retry's own reload picks up
    // the new revision and renders the normal ready state. ---
    await waitFor(() => expect(screen.getByLabelText(/^Título/)).toBeInTheDocument());
    expect((screen.getByLabelText(/^Título/) as HTMLInputElement).value).toBe("Recursividad: Fibonacci");

    // --- The regression assertion: across create, the failed generate, the reload, and the
    // retry, POST /api/v1/assessments (the only call that can create an Assessment) fired
    // exactly once. ---
    const createCalls = mockApiClient.mock.calls.filter(([path, options]: [string, RequestInit?]) => {
      return path === "/api/v1/assessments" && (options?.method ?? "GET") === "POST";
    });
    expect(createCalls).toHaveLength(1);
  });
});
