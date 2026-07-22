import { render, screen, fireEvent } from "@testing-library/react";
import VersionHistorySection from "../VersionHistorySection";
import type { AssessmentDraftVersionViewModel } from "../../mappers/toAssessmentDraftBuilderPageViewModel";

const manyVersions: AssessmentDraftVersionViewModel[] = [
  { versionNumber: 4, isCurrent: true, previewLabel: "v4 (actual)", titlePreview: "Recursividad: Fibonacci con análisis" },
  { versionNumber: 3, isCurrent: false, previewLabel: "v3", titlePreview: "Recursividad: Fibonacci con casos de prueba" },
  { versionNumber: 2, isCurrent: false, previewLabel: "v2", titlePreview: "Recursividad: Fibonacci (revisado)" },
  { versionNumber: 1, isCurrent: false, previewLabel: "v1", titlePreview: "Recursividad: Fibonacci" },
];

const singleVersion: AssessmentDraftVersionViewModel[] = [
  { versionNumber: 1, isCurrent: true, previewLabel: "v1 (actual)", titlePreview: "Recursividad: Fibonacci" },
];

describe("VersionHistorySection", () => {
  it("renders every version row with its preview label", () => {
    render(<VersionHistorySection versions={manyVersions} selectedVersion={4} onViewVersion={jest.fn()} />);

    expect(screen.getByText("v4 (actual)")).toBeInTheDocument();
    expect(screen.getByText("v3")).toBeInTheDocument();
    expect(screen.getByText("v2")).toBeInTheDocument();
    expect(screen.getByText("v1")).toBeInTheDocument();
  });

  it("calls onViewVersion with the clicked version's number", () => {
    const onViewVersion = jest.fn();
    render(<VersionHistorySection versions={manyVersions} selectedVersion={4} onViewVersion={onViewVersion} />);

    fireEvent.click(screen.getByRole("button", { name: /v2/i }));

    expect(onViewVersion).toHaveBeenCalledWith(2);
  });

  it("marks the row matching selectedVersion as pressed/highlighted", () => {
    render(<VersionHistorySection versions={manyVersions} selectedVersion={3} onViewVersion={jest.fn()} />);

    expect(screen.getByRole("button", { name: /v3/i })).toHaveAttribute("aria-pressed", "true");
    expect(screen.getByRole("button", { name: /v4/i })).toHaveAttribute("aria-pressed", "false");
  });

  it("renders a single-current-version list (one item, no prior history) without a broken UI", () => {
    render(<VersionHistorySection versions={singleVersion} selectedVersion={1} onViewVersion={jest.fn()} />);

    const rows = screen.getAllByRole("button");
    expect(rows).toHaveLength(1);
    expect(screen.getByText("v1 (actual)")).toBeInTheDocument();
  });

  it("has no onRestore affordance — only view/selection buttons are rendered", () => {
    render(<VersionHistorySection versions={manyVersions} selectedVersion={4} onViewVersion={jest.fn()} />);

    expect(screen.queryByRole("button", { name: /restaurar/i })).not.toBeInTheDocument();
  });
});
