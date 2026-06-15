import { render, screen } from "@testing-library/react";
import AssessmentCard from "../AssessmentCard";
import type { AssessmentSummaryDto } from "@/types/assessment";

const baseAssessment: AssessmentSummaryDto = {
  id: "assess-1",
  title: "Java Basics",
  status: "OPEN",
  submissionCount: 10,
  pendingApprovals: 3,
  reportLink: null,
};

describe("AssessmentCard", () => {
  it("does not render a report link when reportLink is null", () => {
    render(<AssessmentCard assessment={baseAssessment} />);

    expect(screen.getByText("Java Basics")).toBeInTheDocument();
    expect(screen.getByText("OPEN")).toBeInTheDocument();
    expect(screen.getByText("10 submissions")).toBeInTheDocument();
    expect(screen.getByText("3 pending approvals")).toBeInTheDocument();
    expect(screen.queryByRole("link", { name: /view report/i })).not.toBeInTheDocument();
  });

  it("renders a report link when reportLink is present", () => {
    const assessment: AssessmentSummaryDto = {
      ...baseAssessment,
      reportLink: "https://reports.example.com/assess-1",
    };

    render(<AssessmentCard assessment={assessment} />);

    const link = screen.getByRole("link", { name: /view report/i });
    expect(link).toBeInTheDocument();
    expect(link).toHaveAttribute("href", "https://reports.example.com/assess-1");
  });
});
