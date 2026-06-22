interface AvatarProps {
  name: string;
  size?: "sm" | "md";
  className?: string;
}

const SIZES = { sm: 32, md: 40 } as const;
const FONT_SIZES = { sm: 13, md: 15 } as const;

function initials(name: string): string {
  return name
    .split(" ")
    .map((w) => w[0])
    .filter(Boolean)
    .slice(0, 2)
    .join("")
    .toUpperCase();
}

export default function Avatar({ name, size = "md", className = "" }: AvatarProps) {
  const px = SIZES[size];
  const fs = FONT_SIZES[size];

  return (
    <div
      className={className}
      style={{
        width: px,
        height: px,
        borderRadius: "50%",
        background: "var(--surface-brand-soft)",
        color: "var(--brand)",
        fontFamily: "var(--font-display)",
        fontWeight: 700,
        fontSize: fs,
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        flexShrink: 0,
        userSelect: "none",
      }}
    >
      {initials(name)}
    </div>
  );
}
