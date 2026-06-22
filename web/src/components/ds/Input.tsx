"use client";

import { useState } from "react";
import LucideIcon from "./LucideIcon";

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  icon?: React.ReactNode;
  error?: string;
  showToggle?: boolean;
}

export default function Input({
  icon,
  error,
  disabled,
  style,
  className,
  onFocus,
  onBlur,
  type,
  showToggle = false,
  ...props
}: InputProps) {
  const [isFocused, setIsFocused] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

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

  const paddingLeft = icon ? "38px" : "12px";
  const paddingRight = showToggle ? "40px" : "12px";

  const inputStyle: React.CSSProperties = {
    width: "100%",
    background: "var(--surface-card)",
    border: `1px solid ${borderColor}`,
    borderRadius: "var(--radius-md)",
    padding: `9px ${paddingRight} 9px ${paddingLeft}`,
    fontFamily: "var(--font-sans)",
    fontSize: "var(--text-md)",
    color: "var(--text-body)",
    transition: "border-color 120ms, box-shadow 120ms",
    boxShadow,
    outline: "none",
    cursor: disabled ? "not-allowed" : undefined,
    opacity: disabled ? 0.5 : 1,
    boxSizing: "border-box",
    ...style,
  };

  const resolvedType = showToggle ? (showPassword ? "text" : "password") : type;

  return (
    <div>
      <div style={{ position: "relative" }}>
        {icon && (
          <span
            style={{
              position: "absolute",
              left: 12,
              top: "50%",
              transform: "translateY(-50%)",
              color: "var(--text-subtle)",
              display: "flex",
              alignItems: "center",
              pointerEvents: "none",
            }}
          >
            {icon}
          </span>
        )}
        <input
          type={resolvedType}
          disabled={disabled}
          style={inputStyle}
          className={`ds-input${className ? ` ${className}` : ""}`}
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
        {showToggle && (
          <button
            type="button"
            aria-label={showPassword ? "Ocultar contraseña" : "Mostrar contraseña"}
            onClick={() => setShowPassword((v) => !v)}
            style={{
              position: "absolute",
              right: 10,
              top: "50%",
              transform: "translateY(-50%)",
              background: "none",
              border: "none",
              padding: 4,
              cursor: "pointer",
              color: "var(--text-subtle)",
              display: "flex",
              alignItems: "center",
              lineHeight: 0,
            }}
          >
            <LucideIcon name={showPassword ? "eye-off" : "eye"} size={16} />
          </button>
        )}
      </div>
      {error && (
        <p style={{ fontSize: "var(--text-sm)", color: "var(--danger-600)", marginTop: 4, marginBottom: 0 }}>
          {error}
        </p>
      )}
    </div>
  );
}
