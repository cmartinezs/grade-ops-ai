import { getAssessmentDraft, getAssessmentDraftVersions } from "@/lib/api/assessments";
import { logger } from "@/lib/logging/logger";
import { createCorrelationId } from "@/lib/logging/correlationId";
import { toAssessmentDraftBuilderPageViewModel } from "../mappers/toAssessmentDraftBuilderPageViewModel";
import type { AssessmentDraftBuilderPageData } from "../mappers/toAssessmentDraftBuilderPageViewModel";

export async function loadAssessmentDraftBuilderPage(
  assessmentId: string
): Promise<AssessmentDraftBuilderPageData> {
  const correlationId = createCorrelationId();
  const log = logger.child({ correlationId, assessmentId });

  log.info("loadAssessmentDraftBuilderPage started");

  try {
    // Fetch both sources in parallel (independent, no ordering dependency)
    const [draft, versions] = await Promise.all([
      getAssessmentDraft(assessmentId, log),
      getAssessmentDraftVersions(assessmentId, log),
    ]);

    const viewModel = toAssessmentDraftBuilderPageViewModel({ draft, versions });

    log.info({ versionCount: versions.length, currentVersion: draft.versionNumber }, "loadAssessmentDraftBuilderPage succeeded");
    return viewModel;
  } catch (error) {
    const message = error instanceof Error ? error.message : String(error);
    log.error({ error: message }, "loadAssessmentDraftBuilderPage failed");
    throw error;
  }
}
