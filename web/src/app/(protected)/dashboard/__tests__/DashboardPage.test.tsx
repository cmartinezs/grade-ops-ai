import { render, screen, act } from "@testing-library/react";
import DashboardPage from "../page";
import * as assessmentsApi from "@/lib/api/assessments";
import type { AssessmentSummaryDto } from "@/types/assessment";

jest.mock("@/lib/api/assessments");
jest.mock("@/lib/firebase/client", () => ({ auth: { currentUser: null } }));

const mockGetAssessments = assessmentsApi.getAssessments as jest.Mock;

describe("DashboardPage", () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it("renders 3 assessment cards when API returns 3 assessments", async () => {
    const assessments: AssessmentSummaryDto[] = [
      {
        id: "a1",
        title: "Java Basics",
        status: "OPEN",
        submissionCount: 10,
        pendingApprovals: 2,
        reportLink: null,
      },
      {
        id: "a2",
        title: "Data Structures",
        status: "GRADING",
        submissionCount: 25,
        pendingApprovals: 0,
        reportLink: "https://example.com/report/a2",
      },
      {
        id: "a3",
        title: "Algorithms",
        status: "CLOSED",
        submissionCount: 30,
        pendingApprovals: 0,
        reportLink: null,
      },
    ];

    mockGetAssessments.mockResolvedValue(assessments);

    await act(async () => {
      render(<DashboardPage />);
    });

    expect(screen.getByText("Java Basics")).toBeInTheDocument();
    expect(screen.getByText("Data Structures")).toBeInTheDocument();
    expect(screen.getByText("Algorithms")).toBeInTheDocument();
  });

  it("renders EmptyDashboard when API returns empty array", async () => {
    mockGetAssessments.mockResolvedValue([]);

    await act(async () => {
      render(<DashboardPage />);
    });

    expect(screen.getByText("No assessments yet")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Create assessment" })).toBeInTheDocument();
    expect(screen.queryByText("Your Assessments")).not.toBeInTheDocument();
  });
});
