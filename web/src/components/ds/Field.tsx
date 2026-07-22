import { cloneElement, isValidElement, useId } from "react";

interface FieldProps {
  label?: string;
  htmlFor: string;
  required?: boolean;
  hint?: string;
  error?: string;
  children: React.ReactNode;
  style?: React.CSSProperties;
}

export default function Field({ label, htmlFor, required = false, hint, error, children, style }: FieldProps) {
  const messageId = useId();
  const message = error ?? hint;

  const control = isValidElement(children)
    ? cloneElement(children as React.ReactElement<{ "aria-describedby"?: string }>, {
        "aria-describedby": message ? messageId : undefined,
      })
    : children;

  return (
    <div style={style}>
      {label && (
        <label
          htmlFor={htmlFor}
          style={{
            display: "block",
            fontSize: "var(--text-sm)",
            fontWeight: 500,
            color: "var(--text-body)",
            marginBottom: 6,
          }}
        >
          {label}
          {required && (
            <span aria-hidden="true" style={{ color: "var(--danger-500)", marginLeft: 4 }}>
              *
            </span>
          )}
        </label>
      )}
      {control}
      {message && (
        <p
          id={messageId}
          role={error ? "alert" : undefined}
          style={{
            fontSize: "var(--text-sm)",
            color: error ? "var(--danger-600)" : "var(--text-subtle)",
            marginTop: 4,
            marginBottom: 0,
          }}
        >
          {message}
        </p>
      )}
    </div>
  );
}
