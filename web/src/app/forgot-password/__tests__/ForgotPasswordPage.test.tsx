import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import ForgotPasswordPage from "../page";

jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: jest.fn() }),
}));

const mockForgotPassword = jest.fn();
jest.mock("@/lib/api/auth", () => ({
  forgotPassword: (...args: unknown[]) => mockForgotPassword(...args),
}));

describe("ForgotPasswordPage", () => {
  beforeEach(() => jest.clearAllMocks());

  it("shows confirmation after valid email submit", async () => {
    mockForgotPassword.mockResolvedValue(undefined);
    render(<ForgotPasswordPage />);

    await userEvent.type(screen.getByLabelText("Correo electrónico"), "teacher@school.cl");
    await userEvent.click(screen.getByRole("button", { name: /enviar enlace/i }));

    await waitFor(() =>
      expect(screen.getByRole("status")).toBeInTheDocument()
    );
    expect(screen.getByText(/revisa tu correo/i)).toBeInTheDocument();
    expect(mockForgotPassword).toHaveBeenCalledWith("teacher@school.cl");
  });

  it("shows confirmation even when API throws (enumeration protection)", async () => {
    mockForgotPassword.mockRejectedValue(new Error("network error"));
    render(<ForgotPasswordPage />);

    await userEvent.type(screen.getByLabelText("Correo electrónico"), "unknown@school.cl");
    await userEvent.click(screen.getByRole("button", { name: /enviar enlace/i }));

    await waitFor(() =>
      expect(screen.getByRole("status")).toBeInTheDocument()
    );
  });

  it("shows field error for empty submit without calling API", async () => {
    render(<ForgotPasswordPage />);

    await userEvent.click(screen.getByRole("button", { name: /enviar enlace/i }));

    await waitFor(() =>
      expect(screen.getByText(/ingresa tu correo/i)).toBeInTheDocument()
    );
    expect(mockForgotPassword).not.toHaveBeenCalled();
  });

  it("shows field error for invalid email format without calling API", async () => {
    render(<ForgotPasswordPage />);

    await userEvent.type(screen.getByLabelText("Correo electrónico"), "test@nodomain");
    await userEvent.click(screen.getByRole("button", { name: /enviar enlace/i }));

    await waitFor(() =>
      expect(screen.getByText(/dirección de correo válida/i)).toBeInTheDocument()
    );
    expect(mockForgotPassword).not.toHaveBeenCalled();
  });

  it("shows 'Volver' button when form is visible", () => {
    render(<ForgotPasswordPage />);
    expect(screen.getByRole("button", { name: /volver a iniciar sesión/i })).toBeInTheDocument();
  });
});
