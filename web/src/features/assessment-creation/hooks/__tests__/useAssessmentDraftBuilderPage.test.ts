import {
  isDraftNotFoundError,
  translateSaveError,
  translateRegenerateError,
} from "../useAssessmentDraftBuilderPage";
import {
  GetAssessmentDraftError,
  GetAssessmentDraftVersionsError,
  UpdateAssessmentDraftError,
  RegenerateAssessmentDraftError,
} from "@/lib/api/assessments";

describe("isDraftNotFoundError", () => {
  it("returns true for a 404 GetAssessmentDraftError", () => {
    expect(isDraftNotFoundError(new GetAssessmentDraftError(404, { error: "NOT_FOUND", message: null }, "a1"))).toBe(true);
  });

  it("returns true for a 404 GetAssessmentDraftVersionsError", () => {
    expect(
      isDraftNotFoundError(new GetAssessmentDraftVersionsError(404, { error: "NOT_FOUND", message: null }, "a1"))
    ).toBe(true);
  });

  it("returns false for a non-404 status", () => {
    expect(isDraftNotFoundError(new GetAssessmentDraftError(500, { error: "INTERNAL_ERROR", message: null }, "a1"))).toBe(
      false
    );
  });

  it("returns false for an unrelated error", () => {
    expect(isDraftNotFoundError(new Error("boom"))).toBe(false);
  });
});

describe("translateSaveError", () => {
  it("maps a field-validation (array body) error to per-field messages", () => {
    const error = new UpdateAssessmentDraftError(
      422,
      [{ field: "context", message: "must not be blank if provided" }],
      "a1"
    );
    expect(translateSaveError(error)).toEqual({
      fieldErrors: { context: "El contexto no puede estar vacío." },
      serverError: null,
    });
  });

  it("maps a no-prior-draft (APPLICATION_ERROR) error to the defensive message", () => {
    const error = new UpdateAssessmentDraftError(422, { error: "APPLICATION_ERROR", message: "No draft exists yet" }, "a1");
    expect(translateSaveError(error)).toEqual({
      fieldErrors: null,
      serverError: "Aún no se ha generado un borrador para esta evaluación.",
    });
  });

  it("maps a generic 500 to the generic retry message", () => {
    const error = new UpdateAssessmentDraftError(500, { error: "INTERNAL_ERROR", message: null }, "a1");
    expect(translateSaveError(error)).toEqual({
      fieldErrors: null,
      serverError: "Ocurrió un error inesperado. Intenta de nuevo.",
    });
  });

  it("maps an unrelated error to the generic retry message", () => {
    expect(translateSaveError(new Error("boom"))).toEqual({
      fieldErrors: null,
      serverError: "Ocurrió un error inesperado. Intenta de nuevo.",
    });
  });
});

describe("translateRegenerateError", () => {
  it("maps a field-validation (array body, empty adjustmentNotes) error to the required message", () => {
    const error = new RegenerateAssessmentDraftError(422, [{ field: "adjustmentNotes", message: "must not be blank" }], "a1");
    expect(translateRegenerateError(error)).toEqual({
      fieldError: "Ingresa notas de ajuste antes de regenerar.",
      agentError: null,
    });
  });

  it("maps a no-prior-draft (422 APPLICATION_ERROR) error to the defensive message", () => {
    const error = new RegenerateAssessmentDraftError(422, { error: "APPLICATION_ERROR", message: "No draft exists yet" }, "a1");
    expect(translateRegenerateError(error)).toEqual({
      fieldError: null,
      agentError: "Aún no se ha generado un borrador para esta evaluación.",
    });
  });

  it("maps an agent-rejected (422 AGENT_CALL_FAILED) error distinctly from no-prior-draft", () => {
    const error = new RegenerateAssessmentDraftError(422, { error: "AGENT_CALL_FAILED", message: "AGENT_REJECTED" }, "a1");
    expect(translateRegenerateError(error)).toEqual({
      fieldError: null,
      agentError: "No pudimos regenerar el borrador con estas notas. Ajusta el texto e intenta de nuevo.",
    });
  });

  it("maps a 502 agent-down (AGENT_ERROR) error to the service-unavailable message", () => {
    const error = new RegenerateAssessmentDraftError(502, { error: "AGENT_CALL_FAILED", message: "AGENT_ERROR" }, "a1");
    expect(translateRegenerateError(error)).toEqual({
      fieldError: null,
      agentError: "El servicio de generación no está disponible. Intenta de nuevo en unos minutos.",
    });
  });

  it("maps a 503 agent-down (UNREACHABLE) error to the same service-unavailable message", () => {
    const error = new RegenerateAssessmentDraftError(503, { error: "AGENT_CALL_FAILED", message: "UNREACHABLE" }, "a1");
    expect(translateRegenerateError(error)).toEqual({
      fieldError: null,
      agentError: "El servicio de generación no está disponible. Intenta de nuevo en unos minutos.",
    });
  });

  it("falls back to the generic retry message for a 502 that isn't AGENT_CALL_FAILED (e.g. an infra-level bad gateway)", () => {
    const error = new RegenerateAssessmentDraftError(502, { error: "BAD_GATEWAY", message: null }, "a1");
    expect(translateRegenerateError(error)).toEqual({
      fieldError: null,
      agentError: "Ocurrió un error inesperado. Intenta de nuevo.",
    });
  });

  it("maps a generic 500 to the generic retry message", () => {
    const error = new RegenerateAssessmentDraftError(500, { error: "INTERNAL_ERROR", message: null }, "a1");
    expect(translateRegenerateError(error)).toEqual({
      fieldError: null,
      agentError: "Ocurrió un error inesperado. Intenta de nuevo.",
    });
  });

  it("maps an unrelated error to the generic retry message", () => {
    expect(translateRegenerateError(new Error("boom"))).toEqual({
      fieldError: null,
      agentError: "Ocurrió un error inesperado. Intenta de nuevo.",
    });
  });
});
