import { render, screen } from "@testing-library/react";
import EmptyDashboard from "../EmptyDashboard";

jest.mock("next/navigation", () => ({ useRouter: () => ({ push: jest.fn() }) }));

describe("EmptyDashboard", () => {
  it("renders heading, subtext, and CTA link to /assessments/new", () => {
    render(<EmptyDashboard />);

    expect(screen.getByText("No assessments yet")).toBeInTheDocument();
    expect(
      screen.getByText("Create your first assessment to start a grading cycle.")
    ).toBeInTheDocument();

    const link = screen.getByRole("link", { name: "Create assessment" });
    expect(link).toHaveAttribute("href", "/assessments/new");
  });
});
