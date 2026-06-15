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

  it('renders "Sign in with Google" by default', () => {
    render(<GoogleSignInButton onSuccess={jest.fn()} />);
    expect(screen.getByRole("button", { name: /sign in with google/i })).toBeInTheDocument();
  });

  it("renders a custom label when provided", () => {
    render(<GoogleSignInButton label="Sign up with Google" onSuccess={jest.fn()} />);
    expect(screen.getByRole("button", { name: /sign up with google/i })).toBeInTheDocument();
  });

  it("calls onSuccess with idToken and displayName on successful sign-in", async () => {
    const onSuccess = jest.fn().mockResolvedValue(undefined);
    mockSignInWithPopup.mockResolvedValue({ user: fakeUser() });

    render(<GoogleSignInButton onSuccess={onSuccess} />);
    fireEvent.click(screen.getByRole("button", { name: /sign in with google/i }));

    await waitFor(() => {
      expect(onSuccess).toHaveBeenCalledWith("google-id-token", "Ada Lovelace");
    });
  });

  it("uses email as displayName fallback when displayName is null", async () => {
    const onSuccess = jest.fn().mockResolvedValue(undefined);
    mockSignInWithPopup.mockResolvedValue({ user: fakeUser({ displayName: null }) });

    render(<GoogleSignInButton onSuccess={onSuccess} />);
    fireEvent.click(screen.getByRole("button", { name: /sign in with google/i }));

    await waitFor(() => {
      expect(onSuccess).toHaveBeenCalledWith("google-id-token", "ada@gmail.com");
    });
  });

  it('shows "Signing up…" when loading with label "Sign up with Google"', async () => {
    const onSuccess = jest.fn(() => new Promise<void>(() => {})); // never resolves
    mockSignInWithPopup.mockResolvedValue({ user: fakeUser() });

    render(<GoogleSignInButton label="Sign up with Google" onSuccess={onSuccess} />);
    fireEvent.click(screen.getByRole("button", { name: /sign up with google/i }));

    await waitFor(() => {
      expect(screen.getByRole("button")).toHaveTextContent("Signing up…");
    });
  });

  it("shows Firebase error when popup is closed by user", async () => {
    mockSignInWithPopup.mockRejectedValue(
      Object.assign(new Error("popup closed"), { code: "auth/popup-closed-by-user" })
    );

    render(<GoogleSignInButton onSuccess={jest.fn()} />);
    fireEvent.click(screen.getByRole("button", { name: /sign in with google/i }));

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent("Cancelled. Please try again.");
    });
  });

  it("shows Firebase error when Google sign-in is not enabled", async () => {
    mockSignInWithPopup.mockRejectedValue(
      Object.assign(new Error("not allowed"), { code: "auth/operation-not-allowed" })
    );

    render(<GoogleSignInButton onSuccess={jest.fn()} />);
    fireEvent.click(screen.getByRole("button", { name: /sign in with google/i }));

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent(
        "Google sign-in is not enabled. Contact the administrator."
      );
    });
  });

  it("shows backend error separately when onSuccess throws", async () => {
    mockSignInWithPopup.mockResolvedValue({ user: fakeUser() });
    const onSuccess = jest.fn().mockRejectedValue(new Error("backend down"));

    render(<GoogleSignInButton onSuccess={onSuccess} />);
    fireEvent.click(screen.getByRole("button", { name: /sign in with google/i }));

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent(
        "Something went wrong. Please try again later."
      );
    });
  });

  it("shows generic error for unknown Firebase error codes", async () => {
    mockSignInWithPopup.mockRejectedValue(
      Object.assign(new Error("unknown"), { code: "auth/unknown-code" })
    );

    render(<GoogleSignInButton onSuccess={jest.fn()} />);
    fireEvent.click(screen.getByRole("button", { name: /sign in with google/i }));

    await waitFor(() => {
      expect(screen.getByRole("alert")).toHaveTextContent(
        "Google sign-in failed. Please try again."
      );
    });
  });
});
