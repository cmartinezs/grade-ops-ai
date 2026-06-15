import { render, screen, fireEvent, waitFor, act } from "@testing-library/react";
import { sendEmailVerification } from "firebase/auth";
import VerifyEmailPage from "../page";

// Mock Firebase client — currentUser with configurable properties.
const mockReload = jest.fn();
const mockSignOut = jest.fn();
let mockEmailVerified = false;

jest.mock("@/lib/firebase/client", () => ({
  auth: {
    get currentUser() {
      return {
        reload: mockReload,
        get emailVerified() {
          return mockEmailVerified;
        },
      };
    },
    signOut: (...args: unknown[]) => mockSignOut(...args),
  },
}));

// Mock next/navigation
const mockPush = jest.fn();
const mockReplace = jest.fn();
let mockSentParam: string | null = null;

jest.mock("next/navigation", () => ({
  useRouter: () => ({ push: mockPush, replace: mockReplace }),
  useSearchParams: () => ({
    get: (key: string) => (key === "sent" ? mockSentParam : null),
  }),
}));

const mockSendEmailVerification = sendEmailVerification as jest.Mock;

describe("VerifyEmailPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockEmailVerified = false;
    mockSentParam = null;
    mockSendEmailVerification.mockResolvedValue(undefined);
    mockReload.mockResolvedValue(undefined);
    mockSignOut.mockResolvedValue(undefined);
  });

  // --- Case 1: Page renders on first visit → sendEmailVerification called once, buttons shown ---

  it("calls sendEmailVerification on first visit and renders buttons", async () => {
    mockSentParam = null; // No ?sent param — first visit.

    render(<VerifyEmailPage />);

    await waitFor(() => {
      expect(mockSendEmailVerification).toHaveBeenCalledTimes(1);
    });

    expect(screen.getByRole("button", { name: /resend email/i })).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: /i've verified my email/i })
    ).toBeInTheDocument();
    // Should replace URL to prevent re-send on refresh.
    expect(mockReplace).toHaveBeenCalledWith("/verify-email?sent=1");
  });

  it("does NOT call sendEmailVerification when ?sent=1 is already present", async () => {
    mockSentParam = "1"; // Already sent — page was refreshed.

    render(<VerifyEmailPage />);

    // Wait a tick for any effects.
    await act(async () => {});

    expect(mockSendEmailVerification).not.toHaveBeenCalled();
  });

  // --- Case 2: Resend clicked → sendEmailVerification called, button disabled for 60 s ---

  it("disables resend button after clicking and shows countdown", async () => {
    jest.useFakeTimers();
    mockSentParam = "1";

    render(<VerifyEmailPage />);

    const resendButton = screen.getByRole("button", { name: /resend email/i });
    expect(resendButton).not.toBeDisabled();

    await act(async () => {
      fireEvent.click(resendButton);
    });

    expect(mockSendEmailVerification).toHaveBeenCalledTimes(1);
    expect(resendButton).toBeDisabled();
    expect(resendButton).toHaveTextContent(/resend in \d+s/i);

    jest.useRealTimers();
  });

  // --- Case 3: "I've verified" clicked with verified user → reload called, router pushes /dashboard ---

  it("redirects to /dashboard when user is verified after reload", async () => {
    mockSentParam = "1";
    mockEmailVerified = true; // User verified between page load and button click.

    render(<VerifyEmailPage />);

    fireEvent.click(screen.getByRole("button", { name: /i've verified my email/i }));

    await waitFor(() => {
      expect(mockReload).toHaveBeenCalledTimes(1);
      expect(mockPush).toHaveBeenCalledWith("/dashboard");
    });
  });

  // --- Case 4: "I've verified" clicked with unverified user → error message shown, stays on page ---

  it("shows error message when user is still unverified after reload", async () => {
    mockSentParam = "1";
    mockEmailVerified = false; // Still unverified.

    render(<VerifyEmailPage />);

    fireEvent.click(screen.getByRole("button", { name: /i've verified my email/i }));

    await waitFor(() => {
      expect(mockReload).toHaveBeenCalledTimes(1);
      expect(screen.getByRole("alert")).toHaveTextContent(
        /email not verified yet/i
      );
    });

    expect(mockPush).not.toHaveBeenCalled();
  });

  // --- Sign out ---

  it("signs out and redirects to /login", async () => {
    mockSentParam = "1";

    render(<VerifyEmailPage />);

    fireEvent.click(screen.getByRole("button", { name: /sign out/i }));

    await waitFor(() => {
      expect(mockSignOut).toHaveBeenCalledTimes(1);
      expect(mockPush).toHaveBeenCalledWith("/login");
    });
  });
});
