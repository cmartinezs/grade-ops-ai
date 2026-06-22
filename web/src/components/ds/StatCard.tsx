import LucideIcon from "./LucideIcon";
import type { LucideIconName } from "./LucideIcon";

type IconTone = "default" | "gold" | "info" | "danger";

interface StatCardProps {
  label: string;
  value: string | number;
  unit?: string;
  delta?: string;
  deltaDir?: "up" | "down";
  iconTone?: IconTone;
  icon?: React.ReactNode;
  className?: string;
}

const ICON_TONE_STYLES: Record<IconTone, React.CSSProperties> = {
  default: { background: "var(--surface-brand-soft)", color: "var(--brand)" },
  gold:    { background: "var(--gold-50)",            color: "var(--gold-600)" },
  info:    { background: "var(--info-50)",            color: "var(--info-600)" },
  danger:  { background: "var(--danger-50)",          color: "var(--danger-600)" },
};

const DEFAULT_ICONS: Record<IconTone, LucideIconName> = {
  default: "trending-up",
  gold:    "alert-triangle",
  info:    "bar-chart-3",
  danger:  "alert-triangle",
};

export default function StatCard({ label, value, unit, delta, deltaDir, iconTone = "default", icon, className }: StatCardProps) {
  const toneStyle = ICON_TONE_STYLES[iconTone];
  const defaultIconName = DEFAULT_ICONS[iconTone];

  return (
    <div
      className={className}
      style={{
        background: "var(--surface-card)",
        border: "1px solid var(--border-subtle)",
        borderRadius: "var(--radius-lg)",
        boxShadow: "var(--shadow-sm)",
        padding: 20,
        display: "flex",
        justifyContent: "space-between",
        alignItems: "flex-start",
      }}
    >
      {/* Left: label + value + delta */}
      <div>
        <p
          style={{
            fontFamily: "var(--font-sans)",
            fontSize: "var(--text-xs)",
            fontWeight: 500,
            color: "var(--text-muted)",
            textTransform: "uppercase",
            letterSpacing: "var(--tracking-caps)",
            margin: "0 0 6px",
          }}
        >
          {label}
        </p>
        <div style={{ display: "flex", alignItems: "baseline", gap: 4 }}>
          <span
            style={{
              fontFamily: "var(--font-display)",
              fontWeight: 700,
              fontSize: "var(--text-3xl)",
              color: "var(--text-strong)",
              lineHeight: 1,
            }}
          >
            {value}
          </span>
          {unit && (
            <span style={{ fontFamily: "var(--font-sans)", fontSize: "var(--text-sm)", color: "var(--text-muted)" }}>
              {unit}
            </span>
          )}
        </div>
        {delta && (
          <p
            style={{
              fontFamily: "var(--font-sans)",
              fontSize: "var(--text-sm)",
              color: deltaDir === "up" ? "var(--success-600)" : "var(--danger-600)",
              margin: "4px 0 0",
            }}
          >
            {delta}
          </p>
        )}
      </div>

      {/* Right: icon circle */}
      <div
        style={{
          width: 36,
          height: 36,
          borderRadius: "50%",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          flexShrink: 0,
          ...toneStyle,
        }}
      >
        {icon ?? <LucideIcon name={defaultIconName} size={16} color="currentColor" />}
      </div>
    </div>
  );
}
