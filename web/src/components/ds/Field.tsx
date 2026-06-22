interface FieldProps {
  label: string;
  htmlFor: string;
  children: React.ReactNode;
  style?: React.CSSProperties;
}

export default function Field({ label, htmlFor, children, style }: FieldProps) {
  return (
    <div style={style}>
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
      </label>
      {children}
    </div>
  );
}
