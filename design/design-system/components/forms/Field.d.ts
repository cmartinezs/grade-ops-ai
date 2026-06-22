import * as React from "react";

export interface FieldProps extends React.HTMLAttributes<HTMLDivElement> {
  label?: React.ReactNode;
  htmlFor?: string;
  required?: boolean;
  optional?: boolean;
  /** Helper text shown below when there is no error. */
  hint?: React.ReactNode;
  /** Error text — replaces hint and turns red. */
  error?: React.ReactNode;
}

export declare function Field(props: FieldProps): JSX.Element;
