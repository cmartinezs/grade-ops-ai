import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { signOut } from "firebase/auth";
import SignOutButton from "../SignOutButton";

const mockReplace = jest.fn();

jest.mock("next/navigation", () => ({
  useRouter: () => ({ replace: mockReplace }),
}));

jest.mock("@/lib/firebase/client", () => ({
  auth: {
    currentUser: {
      getIdToken: jest.fn().mockResolvedValue("test-token"),
    },
  },
}));

const mockSignOut = signOut as jest.Mock;

describe("SignOutButton", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    global.fetch = jest.fn().mockResolvedValue({ ok: true });
  });

  it("calls API, then firebaseSignOut, then redirects to /login on success", async () => {
    mockSignOut.mockResolvedValue(undefined);

    render(<SignOutButton />);
    fireEvent.click(screen.getByRole("button", { name: /sign out/i }));

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalledWith("/api/auth/sign-out", {
        method: "POST",
        headers: { Authorization: "Bearer test-token" },
      });
      expect(mockSignOut).toHaveBeenCalled();
      expect(mockReplace).toHaveBeenCalledWith("/login");
    });
  });

  it("still calls firebaseSignOut and redirects even when API call fails", async () => {
    global.fetch = jest.fn().mockRejectedValue(new Error("Network error"));
    mockSignOut.mockResolvedValue(undefined);

    render(<SignOutButton />);
    fireEvent.click(screen.getByRole("button", { name: /sign out/i }));

    await waitFor(() => {
      expect(mockSignOut).toHaveBeenCalled();
      expect(mockReplace).toHaveBeenCalledWith("/login");
    });
  });
});
