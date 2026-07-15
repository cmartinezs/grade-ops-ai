import * as React from "react";

/**
 * Surface container for grouping content.
 *
 * @startingPoint section="Core" subtitle="Card surface with header / body / footer slots" viewport="700x260"
 */
export interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  /** @default "default" */
  elevation?: "flat" | "default" | "raised";
  /** Hover lift + pointer; use for clickable cards. */
  interactive?: boolean;
  /** Brand-colored top accent bar. */
  accent?: boolean;
}

export declare function Card(props: CardProps): JSX.Element & {
  Header: React.FC<React.HTMLAttributes<HTMLDivElement>>;
  Title: React.FC<React.HTMLAttributes<HTMLDivElement>>;
  Body: React.FC<React.HTMLAttributes<HTMLDivElement>>;
  Footer: React.FC<React.HTMLAttributes<HTMLDivElement>>;
};
