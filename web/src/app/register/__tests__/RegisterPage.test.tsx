import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { createUserWithEmailAndPassword } from "firebase/auth";
import RegisterPage from "../page";
import { registerTeacher } from "@/lib/api/auth";

jest.mock("@/lib/firebase/client", () => ({ auth: {} }));
jest.mock("@/lib/api/auth");
jest.mock("next/navigation", () => ({
  useRouter: jest.fn(() => ({ push: jest.fn() })),
}));

const mockCreateUser = createUserWithEmailAndPassword as jest.Mock;
const mockRegisterTeacher = registerTeacher as jest.Mock;

describe("RegisterPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("calls createUserWithEmailAndPassword and navigates to /verify-email on success", async () => {
    const pushMock = jest.fn();
    jest.mocked(require("next/navigation").useRouter).mockReturnValue({ push: pushMock });

    mockCreateUser.mockResolvedValue({
      user: { getIdToken: async () => "id-token-123" },
    });
    mockRegisterTeacher.mockResolvedValue({ firebaseUid: "uid-abc" });

    render(<RegisterPage />);

    fireEvent.change(screen.getByLabelText(/full name/i), { target: { value: "Ada Lovelace" } });
    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: "ada@school.com" } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: "secret123" } });
    fireEvent.click(screen.getByRole("button", { name: /create account/i }));

    await waitFor(() => {
      expect(mockCreateUser).toHaveBeenCalledWith({}, "ada@school.com", "secret123");
      expect(mockRegisterTeacher).toHaveBeenCalledWith("id-token-123", "Ada Lovelace");
      expect(pushMock).toHaveBeenCalledWith("/verify-email");
    });
  });

  it("shows 'Email already registered' for auth/email-already-in-use", async () => {
    const error = Object.assign(new Error("dup"), { code: "auth/email-already-in-use" });
    mockCreateUser.mockRejectedValue(error);

    render(<RegisterPage />);

    fireEvent.change(screen.getByLabelText(/full name/i), { target: { value: "Ada Lovelace" } });
    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: "dup@school.com" } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: "secret123" } });
    fireEvent.click(screen.getByRole("button", { name: /create account/i }));

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent("An account with this email already exists");
    });
  });

  it("shows error when password is too short (Firebase rejects)", async () => {
    const error = Object.assign(new Error("weak"), { code: "auth/weak-password" });
    mockCreateUser.mockRejectedValue(error);

    render(<RegisterPage />);

    fireEvent.change(screen.getByLabelText(/full name/i), { target: { value: "Ada Lovelace" } });
    fireEvent.change(screen.getByLabelText(/email/i), { target: { value: "ada@school.com" } });
    fireEvent.change(screen.getByLabelText(/password/i), { target: { value: "123" } });
    fireEvent.click(screen.getByRole("button", { name: /create account/i }));

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent("Password must be at least 6");
    });
  });
});
