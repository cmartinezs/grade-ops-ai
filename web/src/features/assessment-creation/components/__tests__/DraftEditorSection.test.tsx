import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import DraftEditorSection from "../DraftEditorSection";
import type { AssessmentDraftViewModel } from "../../mappers/toAssessmentDraftBuilderPageViewModel";

const draft: AssessmentDraftViewModel = {
  draftId: "fake-draft-1",
  title: "Recursividad: Fibonacci",
  context: "Evaluación práctica sobre recursividad.",
  instructions: "Implementa una función recursiva para calcular Fibonacci.",
  objectives: ["Comprender recursividad básica"],
  deliverables: ["Archivo .py con la función implementada"],
  constraints: ["No usar librerías externas"],
  versionNumber: 4,
};

describe("DraftEditorSection", () => {
  it("renders the current draft's fields and calls onSave with edited values", async () => {
    const onSave = jest.fn();
    render(
      <DraftEditorSection
        draft={draft}
        aiDisclosureLabel="version-actual"
        isReadOnly={false}
        isSaving={false}
        fieldErrors={null}
        serverError={null}
        onSave={onSave}
      />
    );

    expect(screen.getByLabelText(/^Título/)).toHaveValue("Recursividad: Fibonacci");

    fireEvent.change(screen.getByLabelText(/^Título/), { target: { value: "Recursividad: Fibonacci (editado)" } });
    fireEvent.click(screen.getByRole("button", { name: /guardar cambios/i }));

    await waitFor(() => {
      expect(onSave).toHaveBeenCalledWith(
        expect.objectContaining({ title: "Recursividad: Fibonacci (editado)", objectives: ["Comprender recursividad básica"] })
      );
    });
  });

  it("blocks submission and shows an error when a required field is emptied", async () => {
    const onSave = jest.fn();
    render(
      <DraftEditorSection
        draft={draft}
        aiDisclosureLabel="version-actual"
        isReadOnly={false}
        isSaving={false}
        fieldErrors={null}
        serverError={null}
        onSave={onSave}
      />
    );

    fireEvent.change(screen.getByLabelText(/^Título/), { target: { value: "" } });
    fireEvent.click(screen.getByRole("button", { name: /guardar cambios/i }));

    await waitFor(() => {
      expect(screen.getByText("Ingresa un título.")).toBeInTheDocument();
    });
    expect(onSave).not.toHaveBeenCalled();
  });

  it("disables every field while isSaving", () => {
    render(
      <DraftEditorSection
        draft={draft}
        aiDisclosureLabel="version-actual"
        isReadOnly={false}
        isSaving
        fieldErrors={null}
        serverError={null}
        onSave={jest.fn()}
      />
    );

    expect(screen.getByLabelText(/^Título/)).toBeDisabled();
    expect(screen.getByRole("button", { name: /guardando/i })).toBeDisabled();
  });

  it("is read-only and hides the save button while previewing a historical version, and onSave is never invoked", () => {
    const onSave = jest.fn();
    render(
      <DraftEditorSection
        draft={{ ...draft, title: "Recursividad: Fibonacci (v1)", versionNumber: 1 }}
        aiDisclosureLabel="version-actual"
        isReadOnly
        isSaving={false}
        fieldErrors={null}
        serverError={null}
        onSave={onSave}
      />
    );

    expect(screen.getByLabelText(/^Título/)).toBeDisabled();
    expect(screen.queryByRole("button", { name: /guardar cambios/i })).not.toBeInTheDocument();
    expect(screen.getByText(/viendo una versión anterior/i)).toBeInTheDocument();
    expect(onSave).not.toHaveBeenCalled();
  });

  it("re-enables editing and the save button once isReadOnly returns to false", () => {
    const { rerender } = render(
      <DraftEditorSection
        draft={draft}
        aiDisclosureLabel="version-actual"
        isReadOnly
        isSaving={false}
        fieldErrors={null}
        serverError={null}
        onSave={jest.fn()}
      />
    );
    expect(screen.getByLabelText(/^Título/)).toBeDisabled();
    expect(screen.queryByRole("button", { name: /guardar cambios/i })).not.toBeInTheDocument();

    rerender(
      <DraftEditorSection
        draft={draft}
        aiDisclosureLabel="version-actual"
        isReadOnly={false}
        isSaving={false}
        fieldErrors={null}
        serverError={null}
        onSave={jest.fn()}
      />
    );

    expect(screen.getByLabelText(/^Título/)).not.toBeDisabled();
    expect(screen.getByRole("button", { name: /guardar cambios/i })).toBeInTheDocument();
  });

  it("renders a server-side field error passed in via props", async () => {
    render(
      <DraftEditorSection
        draft={draft}
        aiDisclosureLabel="version-actual"
        isReadOnly={false}
        isSaving={false}
        fieldErrors={{ title: "Ya existe un draft con este título" }}
        serverError={null}
        onSave={jest.fn()}
      />
    );

    await waitFor(() => {
      expect(screen.getByText("Ya existe un draft con este título")).toBeInTheDocument();
    });
  });
});
