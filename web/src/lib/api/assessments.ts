import { apiClient } from "@/lib/api/client";
import type { AssessmentSummaryDto } from "@/types/assessment";

export async function getAssessments(): Promise<AssessmentSummaryDto[]> {
  const res = await apiClient("/api/v1/assessments");
  if (!res.ok) throw new Error(`Failed to fetch assessments: ${res.status}`);
  return res.json();
}
