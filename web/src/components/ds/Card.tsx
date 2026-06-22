interface CardProps { children: React.ReactNode; className?: string; style?: React.CSSProperties; }
interface CardHeaderProps { children: React.ReactNode; }
interface CardTitleProps { children: React.ReactNode; }
interface CardBodyProps { children: React.ReactNode; }
interface CardFooterProps { children: React.ReactNode; }

function CardHeader({ children }: CardHeaderProps) {
  return (
    <div
      style={{
        padding: "16px 20px",
        borderBottom: "1px solid var(--border-subtle)",
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
      }}
    >
      {children}
    </div>
  );
}

function CardTitle({ children }: CardTitleProps) {
  return (
    <span
      style={{
        fontFamily: "var(--font-display)",
        fontWeight: 600,
        fontSize: "var(--text-lg)",
        color: "var(--text-strong)",
      }}
    >
      {children}
    </span>
  );
}

function CardBody({ children }: CardBodyProps) {
  return <div style={{ padding: "16px 20px" }}>{children}</div>;
}

function CardFooter({ children }: CardFooterProps) {
  return (
    <div
      style={{
        padding: "12px 20px",
        borderTop: "1px solid var(--border-subtle)",
      }}
    >
      {children}
    </div>
  );
}

function Card({ children, className, style }: CardProps) {
  return (
    <div
      className={className}
      style={{
        background: "var(--surface-card)",
        border: "1px solid var(--border-subtle)",
        borderRadius: "var(--radius-lg)",
        boxShadow: "var(--shadow-sm)",
        overflow: "clip",
        ...style,
      }}
    >
      {children}
    </div>
  );
}

Card.Header = CardHeader;
Card.Title = CardTitle;
Card.Body = CardBody;
Card.Footer = CardFooter;

export default Card;
