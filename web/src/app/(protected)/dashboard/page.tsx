"use client";

import { useEffect, useState } from "react";
import { getAssessments } from "@/lib/api/assessments";
import AssessmentCard from "@/components/dashboard/AssessmentCard";
import EmptyDashboard from "@/components/dashboard/EmptyDashboard";
import type { AssessmentSummaryDto } from "@/types/assessment";

export default function DashboardPage() {
  const [assessments, setAssessments] = useState<AssessmentSummaryDto[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getAssessments()
      .then(setAssessments)
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-blue-600 border-t-transparent" />
      </div>
    );
  }

  if (assessments.length === 0) {
    return (
      <main className="mx-auto max-w-4xl px-4 py-8">
        <EmptyDashboard />
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-4xl px-4 py-8">
      <h1 className="mb-6 text-2xl font-semibold text-gray-900">Your Assessments</h1>
      <div className="space-y-4">
        {assessments.map((assessment) => (
          <AssessmentCard key={assessment.id} assessment={assessment} />
        ))}
      </div>
    </main>
  );
}
