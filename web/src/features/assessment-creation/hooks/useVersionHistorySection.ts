"use client";

import type { AssessmentDraftVersionViewModel } from "../mappers/toAssessmentDraftBuilderPageViewModel";

export interface VersionHistoryRowViewModel extends AssessmentDraftVersionViewModel {
  isSelected: boolean;
}

interface UseVersionHistorySectionParams {
  versions: AssessmentDraftVersionViewModel[];
  selectedVersion: number;
}

export function useVersionHistorySection({ versions, selectedVersion }: UseVersionHistorySectionParams) {
  const rows: VersionHistoryRowViewModel[] = versions.map((version) => ({
    ...version,
    isSelected: version.versionNumber === selectedVersion,
  }));

  return { rows };
}
