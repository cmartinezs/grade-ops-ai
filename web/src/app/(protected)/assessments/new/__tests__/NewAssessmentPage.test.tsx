import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import NewAssessmentPage from "../page";
import {
  submitAssessmentBrief,
  CreateAssessmentBriefError,
  GenerateAssessmentDraftError,
} from "@/lib/api/assessments";

jest.mock("@/lib/api/assessments", () => {
  const actual = jest.requireActual("@/lib/api/assessments");
  return { ...actual, submitAssessmentBrief: jest.fn() };
});

const mockPush = jest.fn();
jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: mockPush }),
}));

const mockSubmitAssessmentBrief = submitAssessmentBrief as jest.Mock;

function fillValidBrief() {
  fireEvent.change(screen.getByLabelText(/^Objetivo de aprendizaje/), { target: { value: "Entender recursividad" } });
  fireEvent.change(screen.getByLabelText(/^Tema/), { target: { value: "Recursion" } });
  fireEvent.change(screen.getByLabelText(/^Nivel/), { target: { value: "Intermedio" } });
  fireEvent.change(screen.getByLabelText(/^Duración/), { target: { value: "45 min" } });
  fireEvent.change(screen.getByLabelText(/^Idioma/), { target: { value: "Java" } });
}

function submit() {
  fireEvent.click(screen.getByRole("button", { name: /crear evaluación/i }));
}

describe("NewAssessmentPage (real useIntakeAssessmentPage hook, submitAssessmentBrief mocked)", () => {
  beforeEach(() => jest.clearAllMocks());

  it("navigates to /assessments/{assessmentId}/draft with the real id on success", async () => {
    mockSubmitAssessmentBrief.mockResolvedValue({ assessmentId: "assess-1" });
    render(<NewAssessmentPage />);

    fillValidBrief();
    submit();

    await waitFor(() => {
      expect(mockPush).toHaveBeenCalledWith("/assessments/assess-1/draft");
    });
  });

  it("disables the submit button while submitAssessmentBrief is in flight", async () => {
    let resolveSubmit: (value: { assessmentId: string }) => void;
    mockSubmitAssessmentBrief.mockReturnValue(
      new Promise((resolve) => {
        resolveSubmit = resolve;
      })
    );
    render(<NewAssessmentPage />);

    fillValidBrief();
    submit();

    expect(await screen.findByRole("button", { name: /creando evaluación/i })).toBeDisabled();

    resolveSubmit!({ assessmentId: "assess-1" });
    await waitFor(() => {
      expect(mockPush).toHaveBeenCalled();
    });
  });

  it("shows per-field translated messages for a List<FieldErrorResponse> 422 from brief creation", async () => {
    mockSubmitAssessmentBrief.mockRejectedValue(
      new CreateAssessmentBriefError(422, [{ field: "topic", message: "El tema ya existe." }])
    );
    render(<NewAssessmentPage />);

    fillValidBrief();
    submit();

    await waitFor(() => {
      expect(screen.getByText("El tema ya existe.")).toBeInTheDocument();
    });
    expect(mockPush).not.toHaveBeenCalled();
  });

  it("shows a distinct business-rejection message for AGENT_REJECTED", async () => {
    mockSubmitAssessmentBrief.mockRejectedValue(
      new GenerateAssessmentDraftError(422, { error: "AGENT_CALL_FAILED", message: "AGENT_REJECTED" }, "assess-1")
    );
    render(<NewAssessmentPage />);

    fillValidBrief();
    submit();

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent(/no pudimos generar un borrador/i);
    });
  });

  it("shows a distinct service-unavailable message for AGENT_ERROR/UNREACHABLE (502/503)", async () => {
    mockSubmitAssessmentBrief.mockRejectedValue(
      new GenerateAssessmentDraftError(503, { error: "AGENT_CALL_FAILED", message: "UNREACHABLE" }, "assess-1")
    );
    render(<NewAssessmentPage />);

    fillValidBrief();
    submit();

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent(/servicio de generación de ia no está disponible/i);
    });
  });

  it("shows a generic retry message for a 500 response", async () => {
    mockSubmitAssessmentBrief.mockRejectedValue(
      new CreateAssessmentBriefError(500, { error: "INTERNAL_ERROR", message: null })
    );
    render(<NewAssessmentPage />);

    fillValidBrief();
    submit();

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent("Ocurrió un error inesperado. Intenta de nuevo.");
    });
  });
});
