"use client";

import { useState, useId } from "react";

interface FieldWithHelperProps {
  label: string;
  htmlFor: string;
  helper: string;
  children: React.ReactNode;
  style?: React.CSSProperties;
}

export default function FieldWithHelper({
  label,
  htmlFor,
  helper,
  children,
  style,
}: FieldWithHelperProps) {
  const [visible, setVisible] = useState(false);
  const tooltipId = useId();

  return (
    <div style={{ ...style, position: "relative" }}>
      <div style={{ display: "flex", alignItems: "center", gap: 5, marginBottom: 6 }}>
        <label
          htmlFor={htmlFor}
          style={{
            fontSize: "var(--text-sm)",
            fontWeight: 500,
            color: "var(--text-body)",
          }}
        >
          {label}
        </label>

        <button
          type="button"
          aria-label={`¿Qué es ${label}?`}
          aria-describedby={visible ? tooltipId : undefined}
          onMouseEnter={() => setVisible(true)}
          onMouseLeave={() => setVisible(false)}
          onFocus={() => setVisible(true)}
          onBlur={() => setVisible(false)}
          style={{
            width: 15,
            height: 15,
            borderRadius: "50%",
            border: "1.5px solid var(--info-500)",
            background: "transparent",
            color: "var(--info-600)",
            fontSize: 9,
            fontWeight: 700,
            cursor: "pointer",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            padding: 0,
            lineHeight: 1,
            fontFamily: "var(--font-sans)",
            flexShrink: 0,
          }}
        >
          ?
        </button>
      </div>

      {children}

      {visible && (
        <div
          id={tooltipId}
          role="tooltip"
          style={{
            position: "absolute",
            bottom: "calc(100% + 6px)",
            left: 0,
            right: 0,
            background: "var(--info-50)",
            border: "1px solid var(--info-500)",
            borderRadius: "var(--radius-md)",
            color: "var(--info-700)",
            fontSize: "var(--text-xs)",
            lineHeight: 1.55,
            padding: "8px 12px",
            zIndex: 20,
            boxShadow: "0 4px 20px rgba(0, 0, 0, 0.12), 0 1px 6px rgba(30, 80, 180, 0.10)",
            pointerEvents: "none",
          }}
        >
          {helper}
        </div>
      )}
    </div>
  );
}
