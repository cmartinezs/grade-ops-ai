import BriefForm from "./BriefForm";
import type { IntakeAssessmentPageViewModel } from "../hooks/useIntakeAssessmentPage";

interface BriefFormSectionProps {
  view: IntakeAssessmentPageViewModel;
}

export default function BriefFormSection({ view }: BriefFormSectionProps) {
  return (
    <section aria-labelledby="brief-form-title">
      <h2
        id="brief-form-title"
        style={{ fontFamily: "var(--font-display)", fontSize: "var(--text-xl)", color: "var(--text-strong)", margin: "0 0 16px" }}
      >
        Cuéntanos sobre tu evaluación
      </h2>
      <BriefForm
        onSubmit={view.handleSubmit}
        isSubmitting={view.isSubmitting}
        serverError={view.serverError}
        fieldErrors={view.fieldErrors}
      />
    </section>
  );
}
