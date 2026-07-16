import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import BriefForm from "../BriefForm";

describe("BriefForm", () => {
  it("renders all 5 fields and accepts input", () => {
    render(<BriefForm onSubmit={jest.fn()} isSubmitting={false} serverError={null} fieldErrors={null} />);

    expect(screen.getByLabelText(/^Objetivo de aprendizaje/).tagName).toBe("TEXTAREA");
    expect(screen.getByLabelText(/^Tema/).tagName).toBe("INPUT");
    expect(screen.getByLabelText(/^Nivel/).tagName).toBe("INPUT");
    expect(screen.getByLabelText(/^Duración/).tagName).toBe("INPUT");
    expect(screen.getByLabelText(/^Idioma/).tagName).toBe("INPUT");

    fireEvent.change(screen.getByLabelText(/^Tema/), { target: { value: "Recursion" } });
    expect(screen.getByLabelText(/^Tema/)).toHaveValue("Recursion");
  });

  it("blocks submission and shows a per-field error when a required field is empty", async () => {
    const onSubmit = jest.fn();
    render(<BriefForm onSubmit={onSubmit} isSubmitting={false} serverError={null} fieldErrors={null} />);

    fireEvent.click(screen.getByRole("button", { name: /crear evaluación/i }));

    await waitFor(() => {
      expect(screen.getByText("Ingresa el tema.")).toBeInTheDocument();
    });
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it("disables the submit button while isSubmitting", () => {
    render(<BriefForm onSubmit={jest.fn()} isSubmitting fieldErrors={null} serverError={null} />);

    expect(screen.getByRole("button", { name: /creando evaluación/i })).toBeDisabled();
  });

  it("disables every field while isSubmitting, preventing edit/submit races", () => {
    render(<BriefForm onSubmit={jest.fn()} isSubmitting fieldErrors={null} serverError={null} />);

    expect(screen.getByLabelText(/^Objetivo de aprendizaje/)).toBeDisabled();
    expect(screen.getByLabelText(/^Tema/)).toBeDisabled();
    expect(screen.getByLabelText(/^Nivel/)).toBeDisabled();
    expect(screen.getByLabelText(/^Duración/)).toBeDisabled();
    expect(screen.getByLabelText(/^Idioma/)).toBeDisabled();
  });

  it("blocks submission when a required field is whitespace-only", async () => {
    const onSubmit = jest.fn();
    render(<BriefForm onSubmit={onSubmit} isSubmitting={false} serverError={null} fieldErrors={null} />);

    fireEvent.change(screen.getByLabelText(/^Objetivo de aprendizaje/), { target: { value: "Entender recursividad" } });
    fireEvent.change(screen.getByLabelText(/^Tema/), { target: { value: "   " } });
    fireEvent.change(screen.getByLabelText(/^Nivel/), { target: { value: "Intermedio" } });
    fireEvent.change(screen.getByLabelText(/^Duración/), { target: { value: "45 min" } });
    fireEvent.change(screen.getByLabelText(/^Idioma/), { target: { value: "Java" } });
    fireEvent.click(screen.getByRole("button", { name: /crear evaluación/i }));

    await waitFor(() => {
      expect(screen.getByText("Ingresa el tema.")).toBeInTheDocument();
    });
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it("renders the fake server-side field error inline via DynamicForm's externalErrors prop", async () => {
    render(
      <BriefForm
        onSubmit={jest.fn()}
        isSubmitting={false}
        serverError={null}
        fieldErrors={{ topic: "Ya existe una evaluación con este tema" }}
      />
    );

    await waitFor(() => {
      expect(screen.getByText("Ya existe una evaluación con este tema")).toBeInTheDocument();
    });
  });

  it("renders a top-level server error banner when present", () => {
    render(
      <BriefForm onSubmit={jest.fn()} isSubmitting={false} serverError="Algo salió mal. Intenta de nuevo." fieldErrors={null} />
    );

    expect(screen.getByRole("alert")).toHaveTextContent("Algo salió mal. Intenta de nuevo.");
  });
});
