"use client";
import Badge from "@/components/ds/Badge";
import { Button } from "@/components/ds";

interface PlaceholderPageProps {
  icon: React.ReactNode;
  title: string;
  description: string;
  badge?: string;
  cta?: {
    label: string;
    onClick?: () => void;
  };
}

export default function PlaceholderPage({ icon, title, description, badge, cta }: PlaceholderPageProps) {
  return (
    <div style={{ display: "flex", alignItems: "center", justifyContent: "center", height: "100%" }}>
      <div
        style={{
          background: "var(--surface-card)",
          border: "1px dashed var(--border-subtle)",
          borderRadius: "var(--radius-lg)",
          padding: "64px 32px",
          maxWidth: 480,
          width: "100%",
          textAlign: "center",
        }}
      >
        <div style={{ display: "inline-flex", color: "var(--text-subtle)", marginBottom: 16 }}>
          {icon}
        </div>

        {badge && (
          <div style={{ marginBottom: 12 }}>
            <Badge tone="neutral">{badge}</Badge>
          </div>
        )}

        <h1
          style={{
            fontFamily: "var(--font-display)",
            fontWeight: 600,
            fontSize: "var(--text-xl)",
            color: "var(--text-strong)",
            margin: "12px 0 0",
          }}
        >
          {title}
        </h1>

        <p
          style={{
            color: "var(--text-muted)",
            fontSize: "var(--text-md)",
            lineHeight: 1.6,
            marginTop: 8,
            maxWidth: 360,
            marginLeft: "auto",
            marginRight: "auto",
          }}
        >
          {description}
        </p>

        {cta && (
          <Button variant="primary" onClick={cta.onClick} style={{ marginTop: 24 }}>
            {cta.label}
          </Button>
        )}
      </div>
    </div>
  );
}
