// Mirrors task-01's confirmed `GenerateAssessmentDraftResponse` shape. Moves to
// src/types/assessment.ts as `AssessmentDraftDto` once task-10 adds the real
// lib/api functions — this task only needs the shape to feed the mapper below.
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

export interface AssessmentDraftViewModel {
  draftId: string;
  title: string;
  context: string;
  instructions: string;
  objectives: string[];
  deliverables: string[];
  constraints: string[];
  versionNumber: number;
}

export interface AssessmentDraftVersionViewModel {
  versionNumber: number;
  isCurrent: boolean;
  previewLabel: string;
  titlePreview: string;
}

export interface AssessmentDraftBuilderPageData {
  draft: AssessmentDraftViewModel;
  versions: AssessmentDraftVersionViewModel[];
}

const TITLE_PREVIEW_MAX_LENGTH = 48;

function toTitlePreview(title: string): string {
  const trimmed = title.trim();
  return trimmed.length > TITLE_PREVIEW_MAX_LENGTH
    ? `${trimmed.slice(0, TITLE_PREVIEW_MAX_LENGTH).trimEnd()}…`
    : trimmed;
}

// Exported so useAssessmentDraftBuilderPage can re-derive the displayed `draft` slice
// when the teacher previews a past version — AssessmentDraftVersionViewModel only
// carries preview fields (§ VersionHistorySection has no need for full content), so
// the page hook looks up the matching raw DTO and converts it with this function.
export function toDraftViewModel(dto: AssessmentDraftDto): AssessmentDraftViewModel {
  return {
    draftId: dto.draftId,
    title: dto.title,
    context: dto.context,
    instructions: dto.instructions,
    objectives: dto.objectives,
    deliverables: dto.deliverables,
    constraints: dto.constraints,
    versionNumber: dto.versionNumber,
  };
}

export function toAssessmentDraftBuilderPageViewModel(input: {
  draft: AssessmentDraftDto;
  versions: AssessmentDraftDto[];
}): AssessmentDraftBuilderPageData {
  const currentVersionNumber = input.draft.versionNumber;

  const versions: AssessmentDraftVersionViewModel[] = [...input.versions]
    .sort((a, b) => b.versionNumber - a.versionNumber)
    .map((version) => {
      const isCurrent = version.versionNumber === currentVersionNumber;
      return {
        versionNumber: version.versionNumber,
        isCurrent,
        previewLabel: isCurrent ? `v${version.versionNumber} (actual)` : `v${version.versionNumber}`,
        titlePreview: toTitlePreview(version.title),
      };
    });

  return {
    draft: toDraftViewModel(input.draft),
    versions,
  };
}
