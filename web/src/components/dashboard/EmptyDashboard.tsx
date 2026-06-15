"use client";

import Link from "next/link";

export default function EmptyDashboard() {
  return (
    <div className="flex flex-col items-center justify-center py-20 text-center">
      <h2 className="mb-2 text-xl font-semibold text-gray-800">No assessments yet</h2>
      <p className="mb-6 text-sm text-gray-500">
        Create your first assessment to start a grading cycle.
      </p>
      <Link
        href="/assessments/new"
        className="rounded-lg bg-blue-600 px-5 py-2.5 text-sm font-medium text-white hover:bg-blue-700"
      >
        Create assessment
      </Link>
    </div>
  );
}
