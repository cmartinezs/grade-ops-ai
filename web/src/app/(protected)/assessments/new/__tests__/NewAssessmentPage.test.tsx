import { render, screen, fireEvent, waitFor, act } from "@testing-library/react";
import NewAssessmentPage from "../page";

function fillValidBrief() {
  fireEvent.change(screen.getByLabelText(/^Objetivo de aprendizaje/), { target: { value: "Entender recursividad" } });
  fireEvent.change(screen.getByLabelText(/^Tema/), { target: { value: "Recursion" } });
  fireEvent.change(screen.getByLabelText(/^Nivel/), { target: { value: "Intermedio" } });
  fireEvent.change(screen.getByLabelText(/^Duración/), { target: { value: "45 min" } });
  fireEvent.change(screen.getByLabelText(/^Idioma/), { target: { value: "Java" } });
}

describe("NewAssessmentPage (real useIntakeAssessmentPage hook, not mocked props)", () => {
  it("shows the submitting state while the fake submit is in flight, then returns to idle on success", async () => {
    jest.useFakeTimers();
    render(<NewAssessmentPage />);

    fillValidBrief();
    fireEvent.click(screen.getByRole("button", { name: /crear evaluación/i }));

    expect(await screen.findByRole("button", { name: /creando evaluación/i })).toBeDisabled();

    act(() => { jest.advanceTimersByTime(800); });
    await waitFor(() => {
      expect(screen.getByRole("button", { name: /crear evaluación/i })).not.toBeDisabled();
    });

    jest.useRealTimers();
  });

  it("routes the fake server-side field error for topic='trigger-field-error' through the real hook into the DOM", async () => {
    jest.useFakeTimers();
    render(<NewAssessmentPage />);

    fireEvent.change(screen.getByLabelText(/^Objetivo de aprendizaje/), { target: { value: "Entender recursividad" } });
    fireEvent.change(screen.getByLabelText(/^Tema/), { target: { value: "trigger-field-error" } });
    fireEvent.change(screen.getByLabelText(/^Nivel/), { target: { value: "Intermedio" } });
    fireEvent.change(screen.getByLabelText(/^Duración/), { target: { value: "45 min" } });
    fireEvent.change(screen.getByLabelText(/^Idioma/), { target: { value: "Java" } });
    fireEvent.click(screen.getByRole("button", { name: /crear evaluación/i }));

    act(() => { jest.advanceTimersByTime(800); });
    await waitFor(() => {
      expect(screen.getByText("Ya existe una evaluación con este tema")).toBeInTheDocument();
    });

    jest.useRealTimers();
  });
});
