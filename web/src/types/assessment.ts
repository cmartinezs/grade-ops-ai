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

export interface AssessmentDraftDto {
  draftId: string;
  title: string;
  context: string;
  instructions: string;
  objectives: string[];
  deliverables: string[];
  constraints: string[];
  versionNumber: number;
}

export interface UpdateAssessmentDraftRequestDto {
  title?: string;
  context?: string;
  instructions?: string;
  objectives?: string[];
  deliverables?: string[];
  constraints?: string[];
}

export interface FieldErrorResponse {
  field: string;
  message: string;
}

export interface ApiErrorResponse {
  error: string;
  message: string | null;
}
