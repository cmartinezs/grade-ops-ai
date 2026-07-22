import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { createUserWithEmailAndPassword } from "firebase/auth";
import { useRouter } from "next/navigation";
import RegisterPage from "../page";
import { registerTeacher } from "@/lib/api/auth";

jest.mock("@/lib/firebase/client", () => ({ auth: {} }));
jest.mock("@/lib/api/auth");
jest.mock("next/navigation", () => ({
  useRouter: jest.fn(() => ({ push: jest.fn() })),
}));

const mockCreateUser = createUserWithEmailAndPassword as jest.Mock;
const mockRegisterTeacher = registerTeacher as jest.Mock;

function fillForm({
  firstName = "Ada",
  lastName = "Lovelace",
  email = "ada@school.com",
  password = "secret123",
}: { firstName?: string; lastName?: string; email?: string; password?: string } = {}) {
  fireEvent.change(screen.getByLabelText(/^Nombres/i), { target: { value: firstName } });
  fireEvent.change(screen.getByLabelText(/^Apellidos/i), { target: { value: lastName } });
  fireEvent.change(screen.getByLabelText(/^Correo electrónico/i), { target: { value: email } });
  fireEvent.change(screen.getByLabelText(/^Contraseña/i), { target: { value: password } });
  fireEvent.click(screen.getByRole("button", { name: /crear cuenta/i }));
}

describe("RegisterPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("calls createUserWithEmailAndPassword and navigates to /verify-email on success", async () => {
    const pushMock = jest.fn();
    jest.mocked(useRouter).mockReturnValue({ push: pushMock } as unknown as ReturnType<typeof useRouter>);

    mockCreateUser.mockResolvedValue({
      user: { getIdToken: async () => "id-token-123" },
    });
    mockRegisterTeacher.mockResolvedValue({ firebaseUid: "uid-abc" });

    render(<RegisterPage />);

    fillForm({ email: "ada@school.com", password: "secret123" });

    await waitFor(() => {
      expect(mockCreateUser).toHaveBeenCalledWith({}, "ada@school.com", "secret123");
      expect(mockRegisterTeacher).toHaveBeenCalledWith("id-token-123", "Ada", "Lovelace");
      expect(pushMock).toHaveBeenCalledWith("/verify-email");
    });
  });

  it("shows a Spanish message for auth/email-already-in-use", async () => {
    const error = Object.assign(new Error("dup"), { code: "auth/email-already-in-use" });
    mockCreateUser.mockRejectedValue(error);

    render(<RegisterPage />);

    fillForm({ email: "dup@school.com" });

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent(
        "Ya existe una cuenta con ese correo. Intenta iniciar sesión."
      );
    });
  });

  it("shows a Spanish message when Firebase rejects the password as weak", async () => {
    // 6+ chars so client-side Zod validation passes and the submission actually
    // reaches the mocked createUserWithEmailAndPassword call (Firebase's own
    // password policy can still reject a password the client considers valid).
    const error = Object.assign(new Error("weak"), { code: "auth/weak-password" });
    mockCreateUser.mockRejectedValue(error);

    render(<RegisterPage />);

    fillForm({ password: "secret" });

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent(
        "La contraseña debe tener al menos 6 caracteres."
      );
    });
  });
});
