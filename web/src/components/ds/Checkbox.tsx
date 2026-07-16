interface CheckboxProps extends Omit<React.InputHTMLAttributes<HTMLInputElement>, "type"> {
  label?: string;
  description?: string;
  required?: boolean;
}

export default function Checkbox({
  label,
  description,
  required = false,
  disabled,
  style,
  ...props
}: CheckboxProps) {
  return (
    <label
      style={{
        display: "inline-flex",
        alignItems: "flex-start",
        gap: 10,
        cursor: disabled ? "not-allowed" : "pointer",
        color: disabled ? "var(--text-disabled)" : "var(--text-body)",
        fontFamily: "var(--font-sans)",
        fontSize: "var(--text-md)",
        userSelect: "none",
        ...style,
      }}
    >
      <input
        type="checkbox"
        disabled={disabled}
        style={{ marginTop: 2, cursor: disabled ? "not-allowed" : "pointer" }}
        {...props}
      />
      {(label || description) && (
        <span style={{ display: "flex", flexDirection: "column", gap: 1 }}>
          {label && (
            <span>
              {label}
              {required && (
                <span aria-hidden="true" style={{ color: "var(--danger-500)", marginLeft: 4 }}>
                  *
                </span>
              )}
            </span>
          )}
          {description && (
            <span style={{ fontSize: "var(--text-xs)", color: "var(--text-subtle)" }}>{description}</span>
          )}
        </span>
      )}
    </label>
  );
}
