import * as React from "react";

export interface ProgressBarProps extends React.HTMLAttributes<HTMLDivElement> {
  value?: number;
  max?: number;
  label?: React.ReactNode;
  showValue?: boolean;
  /** @default "brand" */
  tone?: "brand" | "gold" | "success" | "danger";
  /** @default "md" */
  size?: "md" | "lg";
  /** Animated stripes — use for in-progress bulk jobs. */
  striped?: boolean;
}

export declare function ProgressBar(props: ProgressBarProps): JSX.Element;
