/**
 * AppLogo — componente de marca de GradeOps AI.
 * Usa el logo-mark SVG del design system (verde Sprout + punto dorado)
 * y tipografía Bricolage Grotesque via var(--font-display).
 *
 * Props:
 *   className — clase aplicada al contenedor (útil para margin/padding)
 *   size      — controla el tamaño del mark y del wordmark
 *   variant   — "mark-wordmark" (default) | "mark-only" | "wordmark-only"
 */

interface AppLogoProps {
  className?: string;
  size?: "sm" | "md" | "lg";
  variant?: "mark-wordmark" | "mark-only" | "wordmark-only";
}

const MARK_SIZES = { sm: 28, md: 36, lg: 44 } as const;
const FONT_SIZES = { sm: 18, md: 22, lg: 27 } as const;

export default function AppLogo({
  className = "",
  size = "md",
  variant = "mark-wordmark",
}: AppLogoProps) {
  const markPx = MARK_SIZES[size];
  const fontPx = FONT_SIZES[size];

  return (
    <div
      className={className}
      style={{
        display: "flex",
        alignItems: "center",
        gap: 10,
        userSelect: "none",
      }}
    >
      {variant !== "wordmark-only" && (
        // eslint-disable-next-line @next/next/no-img-element
        <img
          src="/brand/logo-mark.svg"
          alt=""
          aria-hidden="true"
          width={markPx}
          height={markPx}
          style={{ display: "block", flexShrink: 0 }}
        />
      )}

      {variant !== "mark-only" && (
        <span
          style={{
            fontFamily: "var(--font-display)",
            fontWeight: 700,
            fontSize: fontPx,
            letterSpacing: "-0.02em",
            lineHeight: 1,
            color: "var(--text-strong)",
          }}
        >
          GradeOps
          <span style={{ color: "var(--brand)" }}>AI</span>
        </span>
      )}
    </div>
  );
}
