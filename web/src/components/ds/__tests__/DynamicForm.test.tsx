import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import DynamicForm, { type FieldDefinition } from "../DynamicForm";

const schema = z.object({
  topic: z.string().min(1, "Ingresa el tema."),
  learningGoal: z.string().min(1, "Ingresa el objetivo de aprendizaje."),
  level: z.string().min(1, "Selecciona un nivel."),
  acceptsTerms: z.boolean().optional(),
});

type FormValues = z.infer<typeof schema>;

const fields: FieldDefinition[] = [
  { name: "topic", label: "Tema", control: "input", required: true },
  { name: "learningGoal", label: "Objetivo de aprendizaje", control: "textarea", required: true },
  {
    name: "level",
    label: "Nivel",
    control: "select",
    required: true,
    options: [
      { value: "", label: "Selecciona un nivel" },
      { value: "basic", label: "Básico" },
    ],
  },
  { name: "acceptsTerms", label: "Acepto los términos", control: "checkbox" },
];

describe("DynamicForm", () => {
  it("renders one control per field definition, matching the declared control type", () => {
    render(<DynamicForm fields={fields} onSubmit={jest.fn()} resolver={zodResolver(schema)} />);

    expect(screen.getByLabelText(/^Tema/).tagName).toBe("INPUT");
    expect(screen.getByLabelText(/^Objetivo de aprendizaje/).tagName).toBe("TEXTAREA");
    expect(screen.getByLabelText(/^Nivel/).tagName).toBe("SELECT");
    expect(screen.getByLabelText("Acepto los términos").tagName).toBe("INPUT");
  });

  it("blocks submission and surfaces field errors when a required field is empty", async () => {
    const onSubmit = jest.fn();
    render(<DynamicForm<FormValues> fields={fields} onSubmit={onSubmit} resolver={zodResolver(schema)} />);

    fireEvent.submit(screen.getByLabelText(/^Tema/).closest("form")!);

    await waitFor(() => {
      expect(screen.getByText("Ingresa el tema.")).toBeInTheDocument();
    });
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it("calls onSubmit with validated values when all required fields are filled", async () => {
    const onSubmit = jest.fn();
    render(<DynamicForm<FormValues> fields={fields} onSubmit={onSubmit} resolver={zodResolver(schema)} />);

    fireEvent.change(screen.getByLabelText(/^Tema/), { target: { value: "Recursion" } });
    fireEvent.change(screen.getByLabelText(/^Objetivo de aprendizaje/), { target: { value: "Entender la recursividad" } });
    fireEvent.change(screen.getByLabelText(/^Nivel/), { target: { value: "basic" } });
    fireEvent.submit(screen.getByLabelText(/^Tema/).closest("form")!);

    await waitFor(() => {
      expect(onSubmit).toHaveBeenCalledWith(
        expect.objectContaining({ topic: "Recursion", learningGoal: "Entender la recursividad", level: "basic" }),
        expect.anything()
      );
    });
  });

  it("blocks submission on a required field even without a resolver", async () => {
    const onSubmit = jest.fn();
    const noResolverFields: FieldDefinition[] = [{ name: "topic", label: "Tema", control: "input", required: true }];
    render(<DynamicForm<{ topic: string }> fields={noResolverFields} onSubmit={onSubmit} />);

    fireEvent.submit(screen.getByLabelText(/^Tema/).closest("form")!);

    await waitFor(() => {
      expect(screen.getByText("Tema es obligatorio.")).toBeInTheDocument();
    });
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it("applies externalErrors to the matching field and clears it once the user edits that field", async () => {
    const { rerender } = render(
      <DynamicForm<FormValues>
        fields={fields}
        onSubmit={jest.fn()}
        resolver={zodResolver(schema)}
        externalErrors={{ topic: "Ya existe una evaluación con este tema." }}
      />
    );

    await waitFor(() => {
      expect(screen.getByText("Ya existe una evaluación con este tema.")).toBeInTheDocument();
    });

    rerender(
      <DynamicForm<FormValues>
        fields={fields}
        onSubmit={jest.fn()}
        resolver={zodResolver(schema)}
        externalErrors={{ topic: "Ya existe una evaluación con este tema." }}
      />
    );

    fireEvent.change(screen.getByLabelText(/^Tema/), { target: { value: "Un tema nuevo" } });

    await waitFor(() => {
      expect(screen.queryByText("Ya existe una evaluación con este tema.")).not.toBeInTheDocument();
    });
  });
});
