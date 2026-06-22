import * as React from "react";

export type TagTone = "neutral" | "brand" | "gold" | "info";

export interface TagProps extends React.HTMLAttributes<HTMLSpanElement> {
  /** @default "neutral" */
  tone?: TagTone;
  /** Optional leading colored dot. */
  dot?: boolean;
  /** Render a × remove button and call this when it's clicked. */
  onRemove?: () => void;
  /** Make the tag a clickable filter toggle. */
  onClick?: () => void;
}

export declare function Tag(props: TagProps): JSX.Element;
