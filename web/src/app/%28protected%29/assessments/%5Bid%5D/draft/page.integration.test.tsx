import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { ShellProvider } from "@/components/shell/ShellContext";
import { useShellConfig } from "@/components/shell/ShellContext";
import { useAssessmentDraftBuilderPage } from "@/features/assessment-creation/hooks/useAssessmentDraftBuilderPage";
import DraftEditorSection from "@/features/assessment-creation/components/DraftEditorSection";
import RegenerateSection from "@/features/assessment-creation/components/RegenerateSection";
import VersionHistorySection from "@/features/assessment-creation/components/VersionHistorySection";

// Mock AuthGuard to simulate an authenticated user
jest.mock("@/components/auth/AuthGuard", () => {
  return function MockAuthGuard({ children }: { children: React.ReactNode }) {
    return <>{children}</>;
  };
});

// Mock AppShell — only render children, no UI chrome
jest.mock("@/components/shell/AppShell", () => {
  return function MockAppShell({ children }: { children: React.ReactNode }) {
    return <>{children}</>;
  };
});

// Mock next/navigation for routing
jest.mock("next/navigation", () => ({
  useRouter: () => ({
    push: jest.fn(),
    replace: jest.fn(),
    prefetch: jest.fn(),
    back: jest.fn(),
    forward: jest.fn(),
  }),
  usePathname: () => "/assessments/test-id/draft",
  useSearchParams: () => new URLSearchParams(),
}));

/**
 * Test wrapper that simulates what DraftBuilderPage does.
 * This allows us to test the page logic (hook + components) without importing a "use client" page.
 */
function DraftBuilderPageTestWrapper({ assessmentId }: { assessmentId: string }) {
  // Simulate what the page does
  useShellConfig({
    title: "Draft de la evaluación",
    subtitle: "Revisa, edita y regenera el borrador generado por IA",
  });

  const page = useAssessmentDraftBuilderPage(assessmentId);

  if (page.status === "loading") {
    return <p role="status">Cargando…</p>;
  }

  if (page.status === "error") {
    return <p role="alert">{page.error}</p>;
  }

  const { data } = page;

  return (
    <div style={{ maxWidth: "var(--content-max)", display: "flex", flexDirection: "column", gap: 32 }}>
      <DraftEditorSection
        draft={data.draft}
        aiDisclosureLabel={data.aiDisclosureLabel}
        isReadOnly={data.isViewingHistoricalVersion}
        isSaving={data.isSaving}
        fieldErrors={data.saveFieldErrors}
        serverError={data.saveServerError}
        onSave={data.onSave}
      />
      <RegenerateSection
        isRegenerating={data.isRegenerating}
        fieldError={data.regenerateFieldError}
        agentError={data.regenerateAgentError}
        onRegenerate={data.onRegenerate}
      />
      <VersionHistorySection versions={data.versions} selectedVersion={data.selectedVersion} onViewVersion={data.onViewVersion} />
    </div>
  );
}

describe("DraftBuilderPage (integration)", () => {
  const testAssessmentId = "test-assessment-123";

  function renderPage() {
    return render(
      <ShellProvider>
        <DraftBuilderPageTestWrapper assessmentId={testAssessmentId} />
      </ShellProvider>
    );
  }

  it("renders all three main sections without crashing", () => {
    renderPage();

    // Verify DraftEditorSection is present (has title input and "Editor de draft" heading)
    expect(screen.getByLabelText(/^Título/)).toBeInTheDocument();
    expect(screen.getByLabelText(/^Contexto/)).toBeInTheDocument();
    expect(screen.getByLabelText(/^Instrucciones/)).toBeInTheDocument();

    // Verify RegenerateSection is present
    expect(screen.getByRole("button", { name: /regenerar/i })).toBeInTheDocument();

    // Verify VersionHistorySection is present
    expect(screen.getByText(/Historial de versiones/i)).toBeInTheDocument();
  });

  it("displays the current draft with correct data", () => {
    renderPage();

    // The current version (v4) should be displayed in the editor
    const titleField = screen.getByLabelText(/^Título/) as HTMLInputElement;
    expect(titleField.value).toBe("Recursividad: Fibonacci con análisis de complejidad");

    const contextField = screen.getByLabelText(/^Contexto/) as HTMLInputElement;
    expect(contextField.value).toContain("Evaluación práctica sobre recursividad para el curso de Estructuras de Datos");

    // Verify long instructions are present (v4 edge case with 500+ chars)
    const instructionsField = screen.getByLabelText(/^Instrucciones/) as HTMLTextAreaElement;
    expect(instructionsField.value).toMatch(/Implementa una función recursiva/);
    expect(instructionsField.value).toMatch(/costo computacional/); // Long text verification
  });

  it("allows editing and saving the current version", async () => {
    const user = userEvent.setup();
    renderPage();

    // Edit title
    const titleField = screen.getByLabelText(/^Título/) as HTMLInputElement;

    await user.clear(titleField);
    await user.type(titleField, "Nuevo Título");
    expect(titleField.value).toBe("Nuevo Título");

    // Click save
    const saveButton = screen.getByRole("button", { name: /guardar cambios/i });
    await user.click(saveButton);

    // Verify the new title is reflected (state update should be instant in this fake implementation)
    await waitFor(() => {
      expect(screen.getByLabelText(/^Título/)).toHaveValue("Nuevo Título");
    });
  });

  it("displays version history section with multiple versions (many-versions edge case)", () => {
    renderPage();

    const versionHistorySection = screen.getByText(/Historial de versiones/i).closest("section");
    expect(versionHistorySection).toBeInTheDocument();

    // Should have at least 4 version buttons (v1, v2, v3, v4)
    const versionButtons = versionHistorySection?.querySelectorAll("button") ?? [];
    expect(versionButtons.length).toBeGreaterThanOrEqual(4);

    // All versions should be present and interactive
    versionButtons.forEach((btn) => {
      expect(btn).toBeInTheDocument();
    });
  });

  it("displays regenerate section with functional controls", async () => {
    const user = userEvent.setup();
    renderPage();

    // The regenerate button should exist
    const regenerateButton = screen.getByRole("button", { name: /Regenerar con IA/i });
    expect(regenerateButton).toBeInTheDocument();
    expect(regenerateButton).not.toBeDisabled();

    // Find the textarea for adjustment notes using its label
    const adjustmentNotesInput = screen.getByLabelText(/^Notas de ajuste/) as HTMLTextAreaElement;
    expect(adjustmentNotesInput).toBeInTheDocument();

    // Verify we can type in it
    await user.click(adjustmentNotesInput);
    await user.type(adjustmentNotesInput, "Test note");
    expect(adjustmentNotesInput.value).toContain("Test note");
  });
});
