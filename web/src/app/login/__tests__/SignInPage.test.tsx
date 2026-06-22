import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { signInWithEmailAndPassword } from "firebase/auth";
import LoginPage from "../page";

jest.mock("@/lib/firebase/client", () => ({ auth: {} }));
jest.mock("@/lib/api/auth");
jest.mock("next/navigation", () => ({
  useRouter: jest.fn(() => ({ push: jest.fn() })),
  useSearchParams: jest.fn(() => ({ get: jest.fn().mockReturnValue(null) })),
}));

const mockSignIn = signInWithEmailAndPassword as jest.Mock;

describe("LoginPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    jest.mocked(require("next/navigation").useSearchParams).mockReturnValue({
      get: jest.fn().mockReturnValue(null),
    });
  });

  it("redirects to /dashboard when credentials are valid and email is verified", async () => {
    const pushMock = jest.fn();
    jest.mocked(require("next/navigation").useRouter).mockReturnValue({ push: pushMock });

    mockSignIn.mockResolvedValue({
      user: { emailVerified: true },
    });

    render(<LoginPage />);

    fireEvent.change(screen.getByLabelText("Correo electrónico"), { target: { value: "teacher@school.com" } });
    fireEvent.change(screen.getByLabelText("Contraseña"), { target: { value: "secret123" } });
    fireEvent.click(screen.getByRole("button", { name: /iniciar sesión/i }));

    await waitFor(() => {
      expect(mockSignIn).toHaveBeenCalledWith({}, "teacher@school.com", "secret123");
      expect(pushMock).toHaveBeenCalledWith("/dashboard");
    });
  });

  it("redirects to /verify-email when credentials are valid but email is not verified", async () => {
    const pushMock = jest.fn();
    jest.mocked(require("next/navigation").useRouter).mockReturnValue({ push: pushMock });

    mockSignIn.mockResolvedValue({
      user: { emailVerified: false },
    });

    render(<LoginPage />);

    fireEvent.change(screen.getByLabelText("Correo electrónico"), { target: { value: "teacher@school.com" } });
    fireEvent.change(screen.getByLabelText("Contraseña"), { target: { value: "secret123" } });
    fireEvent.click(screen.getByRole("button", { name: /iniciar sesión/i }));

    await waitFor(() => {
      expect(pushMock).toHaveBeenCalledWith("/verify-email");
    });
  });

  it("shows Spanish error message for auth/wrong-password", async () => {
    const error = Object.assign(new Error("wrong password"), { code: "auth/wrong-password" });
    mockSignIn.mockRejectedValue(error);

    render(<LoginPage />);

    fireEvent.change(screen.getByLabelText("Correo electrónico"), { target: { value: "teacher@school.com" } });
    fireEvent.change(screen.getByLabelText("Contraseña"), { target: { value: "wrongpass" } });
    fireEvent.click(screen.getByRole("button", { name: /iniciar sesión/i }));

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent("Correo o contraseña incorrectos");
    });
  });

  it("shows session expired banner when ?reason=expired is in the URL", async () => {
    jest.mocked(require("next/navigation").useSearchParams).mockReturnValue({
      get: (key: string) => (key === "reason" ? "expired" : null),
    });

    render(<LoginPage />);

    expect(screen.getByRole("alert")).toHaveTextContent("Tu sesión expiró");
  });

  it("shows Spanish error message for auth/user-not-found", async () => {
    const error = Object.assign(new Error("user not found"), { code: "auth/user-not-found" });
    mockSignIn.mockRejectedValue(error);

    render(<LoginPage />);

    fireEvent.change(screen.getByLabelText("Correo electrónico"), { target: { value: "nobody@school.com" } });
    fireEvent.change(screen.getByLabelText("Contraseña"), { target: { value: "anypass" } });
    fireEvent.click(screen.getByRole("button", { name: /iniciar sesión/i }));

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent("No encontramos una cuenta");
    });
  });
});
