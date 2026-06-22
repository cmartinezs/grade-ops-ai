"use client";

import { useState, useEffect } from "react";

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: "primary" | "ghost" | "outline";
  size?: "sm" | "md";
  loading?: boolean;
  block?: boolean;
  iconRight?: React.ReactNode;
}

const SIZE_STYLES: Record<"sm" | "md", React.CSSProperties> = {
  sm: { height: 32, padding: "0 12px", fontSize: "var(--text-sm)" },
  md: { height: 40, padding: "0 16px", fontSize: "var(--text-md)" },
};

function Spinner({ variant }: { variant: "primary" | "ghost" | "outline" }) {
  const isPrimary = variant === "primary";
  return (
    <span
      style={{
        width: 14,
        height: 14,
        border: `2px solid ${isPrimary ? "rgba(255,255,255,0.4)" : "rgba(0,0,0,0.2)"}`,
        borderTopColor: isPrimary ? "#fff" : "var(--text-body)",
        borderRadius: "50%",
        display: "inline-block",
        animation: "spin 0.7s linear infinite",
        flexShrink: 0,
      }}
    />
  );
}

export default function Button({
  variant = "primary",
  size = "md",
  loading = false,
  block = false,
  iconRight,
  children,
  disabled,
  style,
  ...props
}: ButtonProps) {
  const [hovered, setHovered] = useState(false);
  const [pressed, setPressed] = useState(false);

  const isDisabled = disabled || loading;

  useEffect(() => {
    if (isDisabled) {
      setHovered(false);
      setPressed(false);
    }
  }, [isDisabled]);

  const variantStyle: React.CSSProperties =
    variant === "primary"
      ? {
          background: hovered && !isDisabled ? "var(--brand-hover)" : "var(--brand)",
          color: "#fff",
          border: "none",
          boxShadow: hovered && !isDisabled ? "var(--shadow-brand)" : "none",
        }
      : variant === "ghost"
      ? {
          background: hovered && !isDisabled ? "var(--surface-sunken)" : "transparent",
          color: "var(--text-body)",
          border: "none",
        }
      : {
          background: hovered && !isDisabled ? "var(--surface-sunken)" : "transparent",
          color: "var(--text-body)",
          border: "1px solid var(--border-default)",
        };

  return (
    <button
      disabled={isDisabled}
      style={{
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        gap: 8,
        borderRadius: "var(--radius-md)",
        fontFamily: "var(--font-sans)",
        fontWeight: 500,
        cursor: isDisabled ? "not-allowed" : "pointer",
        opacity: isDisabled ? 0.6 : 1,
        transition: "background 120ms, box-shadow 120ms, transform 80ms",
        width: block ? "100%" : undefined,
        transform: pressed && !isDisabled ? "translateY(1px)" : undefined,
        ...SIZE_STYLES[size],
        ...variantStyle,
        ...style,
      }}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => {
        setHovered(false);
        setPressed(false);
      }}
      onMouseDown={() => setPressed(true)}
      onMouseUp={() => setPressed(false)}
      {...props}
    >
      {loading && <Spinner variant={variant} />}
      {children}
      {iconRight && !loading && iconRight}
    </button>
  );
}
