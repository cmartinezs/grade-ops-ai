"use client";

import type { AssessmentSummaryDto, AssessmentStatus } from "@/types/assessment";

const STATUS_BADGE_CLASSES: Record<AssessmentStatus, string> = {
  DRAFT: "bg-gray-100 text-gray-700",
  OPEN: "bg-blue-100 text-blue-700",
  GRADING: "bg-yellow-100 text-yellow-700",
  CLOSED: "bg-green-100 text-green-700",
};

interface AssessmentCardProps {
  assessment: AssessmentSummaryDto;
}

export default function AssessmentCard({ assessment }: AssessmentCardProps) {
  const { title, status, submissionCount, pendingApprovals, reportLink } = assessment;

  return (
    <div className="rounded-xl border border-gray-200 bg-white p-5 shadow-sm">
      <div className="flex items-start justify-between gap-4">
        <h3 className="text-base font-semibold text-gray-900">{title}</h3>
        <span
          className={`shrink-0 rounded-full px-2.5 py-0.5 text-xs font-medium ${STATUS_BADGE_CLASSES[status]}`}
        >
          {status}
        </span>
      </div>

      <div className="mt-3 flex gap-4 text-sm text-gray-600">
        <span>{submissionCount} submissions</span>
        <span>{pendingApprovals} pending approvals</span>
      </div>

      {reportLink !== null && (
        <a
          href={reportLink}
          className="mt-3 inline-block text-sm font-medium text-blue-600 hover:underline"
          target="_blank"
          rel="noopener noreferrer"
        >
          View report
        </a>
      )}
    </div>
  );
}
