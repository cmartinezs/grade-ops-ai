export type BadgeTone = "brand" | "gold" | "success" | "warning" | "danger" | "info" | "neutral";

interface BadgeProps {
  tone?: BadgeTone;
  dot?: boolean;
  children: React.ReactNode;
  className?: string;
}

const TONE_STYLES: Record<BadgeTone, React.CSSProperties> = {
  brand:   { background: "var(--sprout-50)",  color: "var(--sprout-700)",  borderColor: "var(--sprout-200)"  },
  gold:    { background: "var(--gold-50)",    color: "var(--gold-700)",    borderColor: "var(--gold-200)"    },
  success: { background: "var(--success-50)", color: "var(--success-700)", borderColor: "var(--success-200)" },
  warning: { background: "var(--warning-50)", color: "var(--warning-700)", borderColor: "var(--warning-200)" },
  danger:  { background: "var(--danger-50)",  color: "var(--danger-700)",  borderColor: "var(--danger-200)"  },
  info:    { background: "var(--info-50)",    color: "var(--info-700)",    borderColor: "var(--info-200)"    },
  neutral: { background: "var(--slate-50)",   color: "var(--slate-700)",   borderColor: "var(--slate-200)"   },
};

export default function Badge({ tone = "neutral", dot = false, children, className }: BadgeProps) {
  const toneStyle = TONE_STYLES[tone];

  return (
    <span
      className={className}
      style={{
        display: "inline-flex",
        alignItems: "center",
        gap: 5,
        padding: "2px 8px",
        borderRadius: 999,
        fontSize: "var(--text-xs)",
        fontWeight: 500,
        border: "1px solid",
        fontFamily: "var(--font-sans)",
        ...toneStyle,
      }}
    >
      {dot && (
        <span
          style={{
            width: 6,
            height: 6,
            borderRadius: "50%",
            background: "currentColor",
            flexShrink: 0,
          }}
        />
      )}
      {children}
    </span>
  );
}
