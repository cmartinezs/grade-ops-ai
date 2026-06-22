import * as React from "react";

export interface StatCardProps extends React.HTMLAttributes<HTMLDivElement> {
  label: React.ReactNode;
  value: React.ReactNode;
  unit?: React.ReactNode;
  icon?: React.ReactNode;
  /** @default "brand" */
  iconTone?: "brand" | "gold" | "info" | "danger";
  /** Trend value text, e.g. "+0,4". */
  delta?: React.ReactNode;
  /** @default "up" */
  deltaDir?: "up" | "down";
}

export declare function StatCard(props: StatCardProps): JSX.Element;
