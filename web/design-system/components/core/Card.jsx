import React from "react";
import { injectStyle } from "../_lib/styleInjector.js";

const CSS = `
.gops-card {
  background: var(--surface-card);
  border: var(--border-width) solid var(--border-subtle);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  overflow: clip;
}
.gops-card--flat { box-shadow: none; }
.gops-card--raised { box-shadow: var(--shadow-md); border-color: transparent; }
.gops-card--interactive { cursor: pointer; transition: box-shadow var(--dur-base) var(--ease-out), transform var(--dur-base) var(--ease-out), border-color var(--dur-base) var(--ease-out); }
.gops-card--interactive:hover { box-shadow: var(--shadow-lg); transform: translateY(-2px); border-color: var(--border-default); }
.gops-card--accent { border-top: 3px solid var(--brand); }
.gops-card__pad { padding: var(--space-5); }
.gops-card__header { padding: var(--space-4) var(--space-5); border-bottom: 1px solid var(--border-subtle); display: flex; align-items: center; justify-content: space-between; gap: var(--space-3); }
.gops-card__title { font-family: var(--font-display); font-weight: var(--weight-semibold); font-size: var(--text-lg); color: var(--text-strong); }
.gops-card__footer { padding: var(--space-4) var(--space-5); border-top: 1px solid var(--border-subtle); background: var(--surface-sunken); }
`;

/** Surface container. Compose with Card.Header / Card.Body / Card.Footer. */
export function Card({ elevation = "default", interactive = false, accent = false, className = "", children, ...rest }) {
  injectStyle("gops-card", CSS);
  const cls = [
    "gops-card",
    elevation === "flat" ? "gops-card--flat" : "",
    elevation === "raised" ? "gops-card--raised" : "",
    interactive ? "gops-card--interactive" : "",
    accent ? "gops-card--accent" : "",
    className,
  ].filter(Boolean).join(" ");
  return <div className={cls} {...rest}>{children}</div>;
}

Card.Header = function CardHeader({ children, ...rest }) {
  return <div className="gops-card__header" {...rest}>{children}</div>;
};
Card.Title = function CardTitle({ children, ...rest }) {
  return <div className="gops-card__title" {...rest}>{children}</div>;
};
Card.Body = function CardBody({ children, ...rest }) {
  return <div className="gops-card__pad" {...rest}>{children}</div>;
};
Card.Footer = function CardFooter({ children, ...rest }) {
  return <div className="gops-card__footer" {...rest}>{children}</div>;
};
