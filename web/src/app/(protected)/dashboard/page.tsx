"use client";

import { useEffect, useState } from "react";
import { getAssessments } from "@/lib/api/assessments";
import { useShellConfig } from "@/components/shell/ShellContext";
import { Button, StatCard, Card } from "@/components/ds";
import AssessmentRow from "@/components/dashboard/AssessmentRow";
import DashboardEmptyState from "@/components/dashboard/DashboardEmptyState";
import type { AssessmentSummaryDto } from "@/types/assessment";

export default function DashboardPage() {
  const [assessments, setAssessments] = useState<AssessmentSummaryDto[]>([]);
  const [loading, setLoading] = useState(true);

  useShellConfig({
    title: "Panel de control",
    subtitle: "Resumen de tus cursos y evaluaciones",
    actions: <Button variant="primary">+ Nueva evaluación</Button>,
  });

  useEffect(() => {
    getAssessments()
      .then(setAssessments)
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div style={{ display: "flex", alignItems: "center", justifyContent: "center", height: "100%" }}>
        <div
          style={{
            width: 32,
            height: 32,
            border: "3px solid var(--surface-sunken)",
            borderTopColor: "var(--brand)",
            borderRadius: "50%",
            animation: "spin 0.7s linear infinite",
          }}
        />
      </div>
    );
  }

  if (assessments.length === 0) {
    return <DashboardEmptyState />;
  }

  const inReviewCount = assessments.filter((a) => a.status === "GRADING").length;

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: 22, maxWidth: "var(--content-max)" }}>

      {/* Stat cards — maqueta con valores hardcoded excepto "Por corregir" */}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: 16 }}>
        <StatCard label="Promedio general" value="—" />
        <StatCard label="Por corregir" value={inReviewCount} iconTone="gold" />
        <StatCard label="Entregas a tiempo" value="—" unit="%" iconTone="info" />
        <StatCard label="En riesgo" value="—" unit="alumnos" iconTone="danger" />
      </div>

      {/* Lista de evaluaciones reales */}
      <Card>
        <Card.Header>
          <Card.Title>Evaluaciones recientes</Card.Title>
        </Card.Header>
        <div>
          {assessments.map((assessment) => (
            <AssessmentRow key={assessment.id} assessment={assessment} />
          ))}
        </div>
        <Card.Footer>
          <span style={{ fontFamily: "var(--font-sans)", fontSize: "var(--text-sm)", color: "var(--text-muted)" }}>
            {assessments.length} evaluación{assessments.length !== 1 ? "es" : ""}
          </span>
        </Card.Footer>
      </Card>

    </div>
  );
}
