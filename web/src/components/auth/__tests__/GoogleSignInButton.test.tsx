import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { signInWithPopup } from "firebase/auth";
import GoogleSignInButton from "../GoogleSignInButton";

jest.mock("@/lib/firebase/client", () => ({ auth: {} }));

const mockSignInWithPopup = signInWithPopup as jest.Mock;

const fakeUser = (overrides: Partial<{ displayName: string | null; email: string }> = {}) => ({
  getIdToken: jest.fn().mockResolvedValue("google-id-token"),
  displayName: "Ada Lovelace",
  email: "ada@gmail.com",
  ...overrides,
});

describe("GoogleSignInButton", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders "Continuar con Google" button', () => {
    render(<GoogleSignInButton onSuccess={jest.fn()} />);
    expect(screen.getByRole("button", { name: /continuar con google/i })).toBeInTheDocument();
  });

  it("calls onSuccess with idToken and displayName on successful sign-in", async () => {
    const onSuccess = jest.fn().mockResolvedValue(undefined);
    mockSignInWithPopup.mockResolvedValue({ user: fakeUser() });

    render(<GoogleSignInButton onSuccess={onSuccess} />);
    fireEvent.click(screen.getByRole("button", { name: /continuar con google/i }));

    await waitFor(() => {
      expect(onSuccess).toHaveBeenCalledWith("google-id-token", "Ada Lovelace");
    });
  });

  it("uses email as displayName fallback when displayName is null", async () => {
    const onSuccess = jest.fn().mockResolvedValue(undefined);
    mockSignInWithPopup.mockResolvedValue({ user: fakeUser({ displayName: null }) });

    render(<GoogleSignInButton onSuccess={onSuccess} />);
    fireEvent.click(screen.getByRole("button", { name: /continuar con google/i }));

    await waitFor(() => {
      expect(onSuccess).toHaveBeenCalledWith("google-id-token", "ada@gmail.com");
    });
  });

  it('shows "Conectando…" text while loading', async () => {
    mockSignInWithPopup.mockReturnValue(new Promise(() => {})); // never resolves

    render(<GoogleSignInButton onSuccess={jest.fn()} />);
    fireEvent.click(screen.getByRole("button"));

    await waitFor(() => {
      expect(screen.getByText(/conectando/i)).toBeInTheDocument();
    });
  });

  it("shows error message when onSuccess throws", async () => {
    const onSuccess = jest.fn().mockRejectedValue(new Error("Registration failed: 500"));
    mockSignInWithPopup.mockResolvedValue({ user: fakeUser() });

    render(<GoogleSignInButton onSuccess={onSuccess} />);
    fireEvent.click(screen.getByRole("button", { name: /continuar con google/i }));

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent(/no pudimos completar/i);
    });
  });

  it("shows no error when user closes the popup", async () => {
    const popupClosed = Object.assign(new Error("popup closed"), { code: "auth/popup-closed-by-user" });
    mockSignInWithPopup.mockRejectedValue(popupClosed);

    render(<GoogleSignInButton onSuccess={jest.fn()} />);
    fireEvent.click(screen.getByRole("button", { name: /continuar con google/i }));

    await waitFor(() => {
      expect(screen.queryByRole("alert")).not.toBeInTheDocument();
    });
  });
});
