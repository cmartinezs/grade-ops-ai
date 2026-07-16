export type AssessmentStatus = "DRAFT" | "OPEN" | "GRADING" | "CLOSED";

export interface AssessmentSummaryDto {
  id: string;
  title: string;
  status: AssessmentStatus;
  submissionCount: number;
  pendingApprovals: number;
  reportLink: string | null;
}

export interface CreateAssessmentBriefRequestDto {
  learningGoal: string;
  topic: string;
  level: string;
  duration: string;
  language: string;
}

export interface CreateAssessmentBriefResponseDto {
  assessmentId: string;
}

export interface FieldErrorResponse {
  field: string;
  message: string;
}

export interface ApiErrorResponse {
  error: string;
  message: string | null;
}
