"use client";

import { useState } from "react";

interface TextareaProps extends React.TextareaHTMLAttributes<HTMLTextAreaElement> {
  error?: string;
}

export default function Textarea({
  error,
  disabled,
  style,
  className,
  onFocus,
  onBlur,
  ...props
}: TextareaProps) {
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

  const textareaStyle: React.CSSProperties = {
    width: "100%",
    minHeight: 96,
    background: "var(--surface-card)",
    border: `1px solid ${borderColor}`,
    borderRadius: "var(--radius-md)",
    padding: "9px 12px",
    fontFamily: "var(--font-sans)",
    fontSize: "var(--text-md)",
    color: "var(--text-body)",
    lineHeight: 1.5,
    resize: "vertical",
    transition: "border-color 120ms, box-shadow 120ms",
    boxShadow,
    outline: "none",
    cursor: disabled ? "not-allowed" : undefined,
    opacity: disabled ? 0.5 : 1,
    boxSizing: "border-box",
    ...style,
  };

  return (
    <textarea
      disabled={disabled}
      aria-invalid={error ? true : undefined}
      style={textareaStyle}
      className={`ds-textarea${className ? ` ${className}` : ""}`}
      onFocus={(e) => {
        setIsFocused(true);
        onFocus?.(e);
      }}
      onBlur={(e) => {
        setIsFocused(false);
        onBlur?.(e);
      }}
      {...props}
    />
  );
}
