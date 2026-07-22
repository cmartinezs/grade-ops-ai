"use client";

import { useState } from "react";

interface SelectProps extends React.SelectHTMLAttributes<HTMLSelectElement> {
  error?: string;
}

export default function Select({
  error,
  disabled,
  style,
  className,
  onFocus,
  onBlur,
  children,
  ...props
}: SelectProps) {
  const [isFocused, setIsFocused] = useState(false);

  const borderColor = error
    ? "var(--danger-500)"
    : isFocused
    ? "var(--border-brand)"
    : "var(--border-default)";

  const boxShadow = isFocused
    ? error
      ? "0 0 0 3px color-mix(in srgb, var(--danger-500) 20%, transparent)"
      : "var(--ring)"
    : "none";

  const selectStyle: React.CSSProperties = {
    width: "100%",
    appearance: "none",
    background: "var(--surface-card)",
    border: `1px solid ${borderColor}`,
    borderRadius: "var(--radius-md)",
    padding: "9px 38px 9px 12px",
    fontFamily: "var(--font-sans)",
    fontSize: "var(--text-md)",
    color: "var(--text-body)",
    transition: "border-color 120ms, box-shadow 120ms",
    boxShadow,
    outline: "none",
    cursor: disabled ? "not-allowed" : "pointer",
    opacity: disabled ? 0.5 : 1,
    boxSizing: "border-box",
    ...style,
  };

  return (
    <div style={{ position: "relative" }}>
      <select
        disabled={disabled}
        aria-invalid={error ? true : undefined}
        style={selectStyle}
        className={`ds-select${className ? ` ${className}` : ""}`}
        onFocus={(e) => {
          setIsFocused(true);
          onFocus?.(e);
        }}
        onBlur={(e) => {
          setIsFocused(false);
          onBlur?.(e);
        }}
        {...props}
      >
        {children}
      </select>
      <span
        aria-hidden="true"
        style={{
          position: "absolute",
          right: 12,
          top: "50%",
          transform: "translateY(-50%)",
          color: "var(--text-subtle)",
          display: "flex",
          pointerEvents: "none",
        }}
      >
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M6 9l6 6 6-6" />
        </svg>
      </span>
    </div>
  );
}
