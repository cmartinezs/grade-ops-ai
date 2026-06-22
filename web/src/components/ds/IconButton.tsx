"use client";

import { useState } from "react";

interface IconButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  label: string;
  size?: "sm" | "md";
  children: React.ReactNode;
}

const SIZES = { sm: 32, md: 40 } as const;

export default function IconButton({
  label,
  size = "md",
  children,
  style,
  ...props
}: IconButtonProps) {
  const [hovered, setHovered] = useState(false);
  const px = SIZES[size];

  return (
    <button
      title={label}
      aria-label={label}
      style={{
        width: px,
        height: px,
        display: "inline-flex",
        alignItems: "center",
        justifyContent: "center",
        background: hovered ? "var(--surface-sunken)" : "transparent",
        border: "none",
        borderRadius: "var(--radius-md)",
        cursor: "pointer",
        transition: "background 120ms",
        padding: 0,
        color: "var(--text-body)",
        flexShrink: 0,
        ...style,
      }}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      {...props}
    >
      {children}
    </button>
  );
}
