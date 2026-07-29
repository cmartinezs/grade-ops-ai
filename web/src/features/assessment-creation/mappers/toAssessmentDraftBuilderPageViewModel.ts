import type { AssessmentDraftDto, AssessmentRevisionOrigin } from "@/types/assessment";

export interface AssessmentDraftViewModel {
  draftId: string;
  title: string;
  context: string;
  instructions: string;
  objectives: string[];
  deliverables: string[];
  constraints: string[];
  versionNumber: number;
  origin: AssessmentRevisionOrigin;
  actorId: string | null;
  reason: string | null;
  previousRevisionId: string | null;
}

export interface AssessmentDraftVersionViewModel {
  versionNumber: number;
  isCurrent: boolean;
  previewLabel: string;
  titlePreview: string;
  origin: AssessmentRevisionOrigin;
  actorId: string | null;
  reason: string | null;
  previousRevisionId: string | null;
}

export interface AssessmentDraftBuilderPageData {
  draft: AssessmentDraftViewModel;
  versions: AssessmentDraftVersionViewModel[];
  // Full content per version (current + past) — the history section (`versions` above) only
  // carries preview fields, but the page hook needs full content to show a past version
  // read-only in the editor when the teacher browses history, without a second network
  // round-trip or bypassing the Screen Data Facade (task-10's own risk note).
  versionDrafts: AssessmentDraftViewModel[];
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
    origin: dto.origin,
    actorId: dto.actorId,
    reason: dto.reason,
    previousRevisionId: dto.previousRevisionId,
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
        origin: version.origin,
        actorId: version.actorId,
        reason: version.reason,
        previousRevisionId: version.previousRevisionId,
      };
    });

  return {
    draft: toDraftViewModel(input.draft),
    versions,
    versionDrafts: input.versions.map(toDraftViewModel),
  };
}
