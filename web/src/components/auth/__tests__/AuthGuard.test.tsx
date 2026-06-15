import { render, screen, act } from "@testing-library/react";
import { onAuthStateChanged } from "firebase/auth";
import AuthGuard from "../AuthGuard";

jest.mock("@/lib/firebase/client", () => ({ auth: {} }));
jest.mock("next/navigation", () => ({
  useRouter: jest.fn(() => ({ replace: jest.fn() })),
}));

const mockOnAuthStateChanged = onAuthStateChanged as jest.Mock;

type FakeUser = { emailVerified: boolean; providerData?: { providerId: string }[] };

describe("AuthGuard", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("redirects to /login when user is null (unauthenticated)", async () => {
    const replaceMock = jest.fn();
    jest.mocked(require("next/navigation").useRouter).mockReturnValue({ replace: replaceMock });

    mockOnAuthStateChanged.mockImplementation((_auth: unknown, callback: (user: null) => void) => {
      callback(null);
      return jest.fn();
    });

    await act(async () => {
      render(<AuthGuard><div>Protected</div></AuthGuard>);
    });

    expect(replaceMock).toHaveBeenCalledWith("/login");
    expect(screen.queryByText("Protected")).not.toBeInTheDocument();
  });

  it("redirects to /verify-email when email is not verified and user is not a Google user", async () => {
    const replaceMock = jest.fn();
    jest.mocked(require("next/navigation").useRouter).mockReturnValue({ replace: replaceMock });

    mockOnAuthStateChanged.mockImplementation((_auth: unknown, callback: (user: FakeUser) => void) => {
      callback({ emailVerified: false, providerData: [{ providerId: "password" }] });
      return jest.fn();
    });

    await act(async () => {
      render(<AuthGuard><div>Protected</div></AuthGuard>);
    });

    expect(replaceMock).toHaveBeenCalledWith("/verify-email");
    expect(screen.queryByText("Protected")).not.toBeInTheDocument();
  });

  it("renders children when user is authenticated and email is verified", async () => {
    const replaceMock = jest.fn();
    jest.mocked(require("next/navigation").useRouter).mockReturnValue({ replace: replaceMock });

    mockOnAuthStateChanged.mockImplementation((_auth: unknown, callback: (user: FakeUser) => void) => {
      callback({ emailVerified: true, providerData: [{ providerId: "password" }] });
      return jest.fn();
    });

    await act(async () => {
      render(<AuthGuard><div>Protected Content</div></AuthGuard>);
    });

    expect(screen.getByText("Protected Content")).toBeInTheDocument();
    expect(replaceMock).not.toHaveBeenCalled();
  });

  it("renders children for Google user without email verification", async () => {
    const replaceMock = jest.fn();
    jest.mocked(require("next/navigation").useRouter).mockReturnValue({ replace: replaceMock });

    mockOnAuthStateChanged.mockImplementation((_auth: unknown, callback: (user: FakeUser) => void) => {
      callback({ emailVerified: false, providerData: [{ providerId: "google.com" }] });
      return jest.fn();
    });

    await act(async () => {
      render(<AuthGuard><div>Protected Content</div></AuthGuard>);
    });

    expect(screen.getByText("Protected Content")).toBeInTheDocument();
    expect(replaceMock).not.toHaveBeenCalledWith("/verify-email");
  });

  it("shows a spinner while auth state is loading (callback not yet called)", () => {
    mockOnAuthStateChanged.mockImplementation(() => {
      return jest.fn();
    });

    render(<AuthGuard><div>Protected</div></AuthGuard>);

    expect(screen.queryByText("Protected")).not.toBeInTheDocument();
    const spinner = document.querySelector(".animate-spin");
    expect(spinner).toBeInTheDocument();
  });
});
