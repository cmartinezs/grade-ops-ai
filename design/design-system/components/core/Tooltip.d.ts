import * as React from "react";

export interface TooltipProps {
  /** Tooltip text. */
  label: React.ReactNode;
  /** @default "top" */
  placement?: "top" | "bottom";
  children: React.ReactNode;
  className?: string;
}

export declare function Tooltip(props: TooltipProps): JSX.Element;
