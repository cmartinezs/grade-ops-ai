import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import NewAssessmentPage from "../page";
import { submitAssessmentBrief, CreateAssessmentBriefError } from "@/lib/api/assessments";

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

  it("navigates to the draft page even when submitAssessmentBrief's underlying generation failed — Web no longer invents recovery behavior, it always routes to the resumable draft page (Research 02 §5.6 fix)", async () => {
    // submitAssessmentBrief itself never throws for a generate-only failure (see
    // assessments.test.ts) — it always resolves with the assessmentId once create succeeds.
    // This hook-level test locks in that the intake page trusts that contract and always
    // navigates, rather than re-adding a dead-end error branch for generation.
    mockSubmitAssessmentBrief.mockResolvedValue({ assessmentId: "assess-2" });
    render(<NewAssessmentPage />);

    fillValidBrief();
    submit();

    await waitFor(() => {
      expect(mockPush).toHaveBeenCalledWith("/assessments/assess-2/draft");
    });
  });

  it("passes a create and a generate idempotency key, generated client-side, once per submit", async () => {
    mockSubmitAssessmentBrief.mockResolvedValue({ assessmentId: "assess-1" });
    render(<NewAssessmentPage />);

    fillValidBrief();
    submit();

    await waitFor(() => expect(mockSubmitAssessmentBrief).toHaveBeenCalled());
    const [, keys] = mockSubmitAssessmentBrief.mock.calls[0];
    expect(typeof keys.createIdempotencyKey).toBe("string");
    expect(keys.createIdempotencyKey.length).toBeGreaterThan(0);
    expect(typeof keys.generateIdempotencyKey).toBe("string");
    expect(keys.generateIdempotencyKey.length).toBeGreaterThan(0);
    expect(keys.createIdempotencyKey).not.toBe(keys.generateIdempotencyKey);
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

  it("shows per-field translated messages for a List<FieldErrorResponse> 422 from brief creation, ignoring the backend's own (English, untranslated) message text", async () => {
    // Real backend behavior (confirmed against the local api/ stack, not assumed): the
    // @NotBlank constraint has no custom message, so the body is Hibernate Validator's
    // default English text. The hook must never show that raw string to the teacher.
    mockSubmitAssessmentBrief.mockRejectedValue(new CreateAssessmentBriefError(422, [{ field: "topic", message: "must not be blank" }]));
    render(<NewAssessmentPage />);

    fillValidBrief();
    submit();

    await waitFor(() => {
      expect(screen.getByText("Ingresa el tema.")).toBeInTheDocument();
    });
    expect(screen.queryByText("must not be blank")).not.toBeInTheDocument();
    expect(mockPush).not.toHaveBeenCalled();
  });

  it("shows a distinct conflict message for a 409 IDEMPOTENCY_KEY_PAYLOAD_MISMATCH on create, without silently resubmitting", async () => {
    mockSubmitAssessmentBrief.mockRejectedValue(
      new CreateAssessmentBriefError(409, { code: "IDEMPOTENCY_KEY_PAYLOAD_MISMATCH", message: "Same key, different payload" })
    );
    render(<NewAssessmentPage />);

    fillValidBrief();
    submit();

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent(/conflicto al enviar el formulario/i);
    });
    expect(mockPush).not.toHaveBeenCalled();
  });

  it("shows a generic retry message for a 500 response", async () => {
    mockSubmitAssessmentBrief.mockRejectedValue(new CreateAssessmentBriefError(500, { error: "INTERNAL_ERROR", message: null }));
    render(<NewAssessmentPage />);

    fillValidBrief();
    submit();

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent("Ocurrió un error inesperado. Intenta de nuevo.");
    });
  });

  it("generates a fresh create idempotency key for a genuinely new attempt after a field-validation error, not the same key reused (a corrected payload under the old key would risk a mismatch)", async () => {
    mockSubmitAssessmentBrief.mockRejectedValueOnce(
      new CreateAssessmentBriefError(422, [{ field: "topic", message: "must not be blank" }])
    );
    mockSubmitAssessmentBrief.mockResolvedValueOnce({ assessmentId: "assess-1" });
    render(<NewAssessmentPage />);

    fillValidBrief();
    submit();
    await waitFor(() => expect(mockSubmitAssessmentBrief).toHaveBeenCalledTimes(1));
    const firstKey = mockSubmitAssessmentBrief.mock.calls[0][1].createIdempotencyKey;

    submit();
    await waitFor(() => expect(mockSubmitAssessmentBrief).toHaveBeenCalledTimes(2));
    const secondKey = mockSubmitAssessmentBrief.mock.calls[1][1].createIdempotencyKey;

    expect(secondKey).not.toBe(firstKey);
  });
});
