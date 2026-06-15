export type AssessmentStatus = "DRAFT" | "OPEN" | "GRADING" | "CLOSED";

export interface AssessmentSummaryDto {
  id: string;
  title: string;
  status: AssessmentStatus;
  submissionCount: number;
  pendingApprovals: number;
  reportLink: string | null;
}
