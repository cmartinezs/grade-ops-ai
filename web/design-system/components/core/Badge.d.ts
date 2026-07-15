import * as React from "react";

export type BadgeTone =
  | "neutral" | "brand" | "gold" | "success"
  | "warning" | "danger" | "info" | "solid";

export interface BadgeProps extends React.HTMLAttributes<HTMLSpanElement> {
  /** @default "neutral" */
  tone?: BadgeTone;
  /** Show a leading status dot. */
  dot?: boolean;
}

export declare function Badge(props: BadgeProps): JSX.Element;
