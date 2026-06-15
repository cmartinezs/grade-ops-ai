import { signOut } from "firebase/auth";
import { apiClient } from "../client";

const mockSignOut = signOut as jest.Mock;

jest.mock("@/lib/firebase/client", () => ({
  auth: {
    currentUser: {
      getIdToken: jest.fn().mockResolvedValue("test-token"),
    },
  },
}));

describe("apiClient", () => {
  const originalLocation = window.location;

  beforeEach(() => {
    jest.clearAllMocks();
    // Mock window.location.replace
    Object.defineProperty(window, "location", {
      value: { replace: jest.fn() },
      writable: true,
    });
  });

  afterEach(() => {
    Object.defineProperty(window, "location", {
      value: originalLocation,
      writable: true,
    });
  });

  it("calls signOut and redirects to /login?reason=expired on generic 401", async () => {
    mockSignOut.mockResolvedValue(undefined);
    global.fetch = jest.fn().mockResolvedValue({
      status: 401,
      clone: () => ({
        json: () => Promise.resolve({ error: "UNAUTHORIZED" }),
      }),
    });

    await apiClient("/some/path");

    expect(mockSignOut).toHaveBeenCalled();
    expect(window.location.replace).toHaveBeenCalledWith("/login?reason=expired");
  });

  it("redirects to /verify-email on 401 with EMAIL_NOT_VERIFIED body", async () => {
    global.fetch = jest.fn().mockResolvedValue({
      status: 401,
      clone: () => ({
        json: () => Promise.resolve({ error: "EMAIL_NOT_VERIFIED" }),
      }),
    });

    await apiClient("/some/path");

    expect(mockSignOut).not.toHaveBeenCalled();
    expect(window.location.replace).toHaveBeenCalledWith("/verify-email");
  });

  it("returns the response normally on 200", async () => {
    const mockResponse = {
      status: 200,
      ok: true,
      json: () => Promise.resolve({ data: "ok" }),
    };
    global.fetch = jest.fn().mockResolvedValue(mockResponse);

    const res = await apiClient("/some/path");

    expect(res.status).toBe(200);
    expect(mockSignOut).not.toHaveBeenCalled();
    expect(window.location.replace).not.toHaveBeenCalled();
  });
});
