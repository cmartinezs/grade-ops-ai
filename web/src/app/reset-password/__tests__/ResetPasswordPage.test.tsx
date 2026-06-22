import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Suspense } from "react";
import ResetPasswordPage from "../page";
import { resetPassword, PasswordResetError } from "@/lib/api/auth";
import { useSearchParams } from "next/navigation";

// Jest hoists jest.mock() calls above imports at runtime
jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: jest.fn() }),
  useSearchParams: jest.fn(),
}));

jest.mock("@/lib/api/auth", () => ({
  resetPassword: jest.fn(),
  PasswordResetError: class PasswordResetError extends Error {
    constructor(public readonly code: string) { super(code); }
  },
}));

const mockResetPassword = resetPassword as jest.Mock;
const mockUseSearchParams = useSearchParams as jest.Mock;

function setup(code: string | null) {
  mockUseSearchParams.mockReturnValue({ get: (k: string) => k === "code" ? code : null });
  return render(<Suspense><ResetPasswordPage /></Suspense>);
}

describe("ResetPasswordPage", () => {
  beforeEach(() => jest.clearAllMocks());

  it("shows error state immediately when code param is missing", () => {
    setup(null);
    expect(screen.getByRole("alert")).toBeInTheDocument();
    expect(screen.getByText(/enlace no es válido/i)).toBeInTheDocument();
  });

  it("shows form when code param is present", () => {
    setup("550e8400-e29b-41d4-a716-446655440000");

    expect(screen.getByLabelText("Correo electrónico")).toBeInTheDocument();
    expect(screen.getByLabelText("Nueva contraseña")).toBeInTheDocument();
    expect(screen.getByLabelText("Confirmar contraseña")).toBeInTheDocument();
    expect(screen.queryByText("550e8400-e29b-41d4-a716-446655440000")).not.toBeInTheDocument();
  });

  it("shows success status after valid submit", async () => {
    mockResetPassword.mockResolvedValue(undefined);
    setup("valid-code");

    await userEvent.type(screen.getByLabelText("Correo electrónico"), "teacher@school.cl");
    await userEvent.type(screen.getByLabelText("Nueva contraseña"), "newPass99");
    await userEvent.type(screen.getByLabelText("Confirmar contraseña"), "newPass99");
    await userEvent.click(screen.getByRole("button", { name: /guardar nueva contraseña/i }));

    await waitFor(() =>
      expect(screen.getByRole("status")).toBeInTheDocument()
    );
    expect(screen.getByText(/contraseña actualizada/i)).toBeInTheDocument();
    expect(mockResetPassword).toHaveBeenCalledWith("valid-code", {
      email: "teacher@school.cl",
      password: "newPass99",
      passwordRepeat: "newPass99",
    });
  });

  it("shows passwords-mismatch Zod error without calling API", async () => {
    setup("valid-code");

    await userEvent.type(screen.getByLabelText("Correo electrónico"), "teacher@school.cl");
    await userEvent.type(screen.getByLabelText("Nueva contraseña"), "newPass99");
    await userEvent.type(screen.getByLabelText("Confirmar contraseña"), "differentPass");
    await userEvent.click(screen.getByRole("button", { name: /guardar nueva contraseña/i }));

    await waitFor(() =>
      expect(screen.getByText(/contraseñas no coinciden/i)).toBeInTheDocument()
    );
    expect(mockResetPassword).not.toHaveBeenCalled();
  });

  it("shows alert for RESET_CODE_NOT_FOUND (404)", async () => {
    mockResetPassword.mockRejectedValue(new PasswordResetError("RESET_CODE_NOT_FOUND"));
    setup("bad-code");

    await userEvent.type(screen.getByLabelText("Correo electrónico"), "teacher@school.cl");
    await userEvent.type(screen.getByLabelText("Nueva contraseña"), "newPass99");
    await userEvent.type(screen.getByLabelText("Confirmar contraseña"), "newPass99");
    await userEvent.click(screen.getByRole("button", { name: /guardar nueva contraseña/i }));

    await waitFor(() =>
      expect(screen.getByRole("alert")).toBeInTheDocument()
    );
    expect(screen.getByText(/enlace no es válido/i)).toBeInTheDocument();
  });

  it("shows alert for RESET_CODE_EXPIRED (410)", async () => {
    mockResetPassword.mockRejectedValue(new PasswordResetError("RESET_CODE_EXPIRED"));
    setup("expired-code");

    await userEvent.type(screen.getByLabelText("Correo electrónico"), "teacher@school.cl");
    await userEvent.type(screen.getByLabelText("Nueva contraseña"), "newPass99");
    await userEvent.type(screen.getByLabelText("Confirmar contraseña"), "newPass99");
    await userEvent.click(screen.getByRole("button", { name: /guardar nueva contraseña/i }));

    await waitFor(() =>
      expect(screen.getByRole("alert")).toBeInTheDocument()
    );
    expect(screen.getByText(/enlace expiró/i)).toBeInTheDocument();
  });

  it("shows alert for RESET_CODE_USED (410)", async () => {
    mockResetPassword.mockRejectedValue(new PasswordResetError("RESET_CODE_USED"));
    setup("used-code");

    await userEvent.type(screen.getByLabelText("Correo electrónico"), "teacher@school.cl");
    await userEvent.type(screen.getByLabelText("Nueva contraseña"), "newPass99");
    await userEvent.type(screen.getByLabelText("Confirmar contraseña"), "newPass99");
    await userEvent.click(screen.getByRole("button", { name: /guardar nueva contraseña/i }));

    await waitFor(() =>
      expect(screen.getByRole("alert")).toBeInTheDocument()
    );
    expect(screen.getByText(/ya fue utilizado/i)).toBeInTheDocument();
  });

  it("shows email field error for RESET_CODE_EMAIL_MISMATCH (422)", async () => {
    mockResetPassword.mockRejectedValue(new PasswordResetError("RESET_CODE_EMAIL_MISMATCH"));
    setup("ok-code");

    await userEvent.type(screen.getByLabelText("Correo electrónico"), "wrong@school.cl");
    await userEvent.type(screen.getByLabelText("Nueva contraseña"), "newPass99");
    await userEvent.type(screen.getByLabelText("Confirmar contraseña"), "newPass99");
    await userEvent.click(screen.getByRole("button", { name: /guardar nueva contraseña/i }));

    await waitFor(() =>
      expect(screen.getByText(/correo no coincide/i)).toBeInTheDocument()
    );
    expect(screen.queryByRole("alert")).not.toBeInTheDocument();
  });

  it("shows 'Volver' button when form is visible", () => {
    setup("valid-code");
    expect(screen.getByRole("button", { name: /volver a iniciar sesión/i })).toBeInTheDocument();
  });
});
