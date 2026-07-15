import * as React from "react";

export type RubricLevelKey =
  | "exceeds"     // Logrado destacado
  | "meets"       // Logrado
  | "developing"  // En desarrollo
  | "beginning"   // Inicial
  | "notmet";     // No logrado

export interface RubricLevelProps extends React.HTMLAttributes<HTMLSpanElement> {
  /** @default "meets" */
  level?: RubricLevelKey;
  /** Override the default Spanish label. */
  label?: React.ReactNode;
  /** Filled style for compact tables / heatmaps. */
  solid?: boolean;
}

export declare function RubricLevel(props: RubricLevelProps): JSX.Element;
