import { Button, LucideIcon } from "@/components/ds";

export default function DashboardEmptyState() {
  return (
    <div
      style={{
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        height: "100%",
        minHeight: 320,
      }}
    >
      <div
        style={{
          background: "var(--surface-card)",
          border: "1px dashed var(--border-subtle)",
          borderRadius: "var(--radius-lg)",
          padding: "48px 32px",
          maxWidth: 480,
          textAlign: "center",
        }}
      >
        <LucideIcon name="file-pen-line" size={40} color="var(--text-subtle)" />
        <h2
          style={{
            fontFamily: "var(--font-display)",
            fontWeight: 600,
            fontSize: "var(--text-xl)",
            color: "var(--text-strong)",
            margin: "16px 0 0",
          }}
        >
          Aún no tienes evaluaciones
        </h2>
        <p
          style={{
            fontFamily: "var(--font-sans)",
            fontSize: "var(--text-md)",
            color: "var(--text-muted)",
            margin: "8px 0 0",
          }}
        >
          Crea tu primera evaluación y empieza a corregir con IA.
        </p>
        <Button variant="primary" style={{ marginTop: 24 }}>
          + Nueva evaluación
        </Button>
      </div>
    </div>
  );
}
