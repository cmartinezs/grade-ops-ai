import * as React from "react";

export interface TabItem {
  id?: string;
  label: React.ReactNode;
  icon?: React.ReactNode;
  count?: number;
}

export interface TabsProps {
  tabs: TabItem[];
  /** Active tab id (or label if no id). */
  value: string;
  onChange?: (id: string) => void;
  /** @default "underline" */
  variant?: "underline" | "pills";
  className?: string;
}

export declare function Tabs(props: TabsProps): JSX.Element;
