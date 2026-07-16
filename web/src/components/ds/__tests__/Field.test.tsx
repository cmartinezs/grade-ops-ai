import { render, screen } from "@testing-library/react";
import Field from "../Field";

describe("Field", () => {
  it("renders the label without a required marker by default", () => {
    render(
      <Field label="Correo electrónico" htmlFor="email">
        <input id="email" />
      </Field>
    );

    expect(screen.getByText("Correo electrónico")).toBeInTheDocument();
    expect(screen.queryByText("*")).not.toBeInTheDocument();
  });

  it("renders a required marker when required", () => {
    render(
      <Field label="Correo electrónico" htmlFor="email" required>
        <input id="email" />
      </Field>
    );

    expect(screen.getByText("*")).toBeInTheDocument();
  });

  it("shows hint text when there is no error", () => {
    render(
      <Field label="Correo electrónico" htmlFor="email" hint="Usa el correo de tu institución.">
        <input id="email" />
      </Field>
    );

    expect(screen.getByText("Usa el correo de tu institución.")).toBeInTheDocument();
  });

  it("shows error text in place of hint when both are present", () => {
    render(
      <Field
        label="Correo electrónico"
        htmlFor="email"
        hint="Usa el correo de tu institución."
        error="Ingresa una dirección de correo válida."
      >
        <input id="email" />
      </Field>
    );

    expect(screen.getByText("Ingresa una dirección de correo válida.")).toBeInTheDocument();
    expect(screen.queryByText("Usa el correo de tu institución.")).not.toBeInTheDocument();
    expect(screen.getByRole("alert")).toHaveTextContent("Ingresa una dirección de correo válida.");
  });

  it("associates the error text with the control via aria-describedby", () => {
    render(
      <Field label="Correo electrónico" htmlFor="email" error="Ingresa una dirección de correo válida.">
        <input id="email" />
      </Field>
    );

    const input = screen.getByRole("textbox");
    const message = screen.getByRole("alert");
    expect(input).toHaveAttribute("aria-describedby", message.id);
  });
});
