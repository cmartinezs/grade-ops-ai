import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import RegenerateSection from "../RegenerateSection";

describe("RegenerateSection", () => {
  it("calls onRegenerate with the entered adjustment notes", async () => {
    const onRegenerate = jest.fn();
    render(<RegenerateSection isRegenerating={false} fieldError={null} agentError={null} onRegenerate={onRegenerate} />);

    fireEvent.change(screen.getByLabelText(/^Notas de ajuste/), { target: { value: "Agrega casos límite" } });
    fireEvent.click(screen.getByRole("button", { name: /regenerar con ia/i }));

    await waitFor(() => {
      expect(onRegenerate).toHaveBeenCalledWith("Agrega casos límite");
    });
  });

  it("blocks regeneration and shows a required error when notes are empty", async () => {
    const onRegenerate = jest.fn();
    render(<RegenerateSection isRegenerating={false} fieldError={null} agentError={null} onRegenerate={onRegenerate} />);

    fireEvent.click(screen.getByRole("button", { name: /regenerar con ia/i }));

    await waitFor(() => {
      expect(screen.getByText("Ingresa notas de ajuste antes de regenerar.")).toBeInTheDocument();
    });
    expect(onRegenerate).not.toHaveBeenCalled();
  });

  it("blocks regeneration when notes are whitespace-only", async () => {
    const onRegenerate = jest.fn();
    render(<RegenerateSection isRegenerating={false} fieldError={null} agentError={null} onRegenerate={onRegenerate} />);

    fireEvent.change(screen.getByLabelText(/^Notas de ajuste/), { target: { value: "   " } });
    fireEvent.click(screen.getByRole("button", { name: /regenerar con ia/i }));

    await waitFor(() => {
      expect(screen.getByText("Ingresa notas de ajuste antes de regenerar.")).toBeInTheDocument();
    });
    expect(onRegenerate).not.toHaveBeenCalled();
  });

  it("disables the textarea and button while isRegenerating", () => {
    render(<RegenerateSection isRegenerating fieldError={null} agentError={null} onRegenerate={jest.fn()} />);

    expect(screen.getByLabelText(/^Notas de ajuste/)).toBeDisabled();
    expect(screen.getByRole("button", { name: /regenerando/i })).toBeDisabled();
  });

  it("renders an agent-error banner without clearing the current draft (banner-only)", () => {
    render(
      <RegenerateSection
        isRegenerating={false}
        fieldError={null}
        agentError="No pudimos regenerar el borrador con estas notas. Ajusta el texto e intenta de nuevo."
        onRegenerate={jest.fn()}
      />
    );

    expect(screen.getByRole("alert")).toHaveTextContent(/no pudimos regenerar el borrador/i);
  });

  it("disables the textarea and button, and does not call onRegenerate, while disabled (stale-revision conflict blocking)", async () => {
    const onRegenerate = jest.fn();
    render(<RegenerateSection isRegenerating={false} fieldError={null} agentError={null} onRegenerate={onRegenerate} disabled />);

    expect(screen.getByLabelText(/^Notas de ajuste/)).toBeDisabled();
    expect(screen.getByRole("button", { name: /regenerar con ia/i })).toBeDisabled();

    fireEvent.change(screen.getByLabelText(/^Notas de ajuste/), { target: { value: "Agrega casos límite" } });
    fireEvent.click(screen.getByRole("button", { name: /regenerar con ia/i }));

    await waitFor(() => expect(onRegenerate).not.toHaveBeenCalled());
  });
});
