# US-114: Publish Closed Assessment and Freeze Snapshot

- **Epic:** 12 — Question Bank and Closed Assessment Creation
- **Priority:** P0
- **ID:** US-114

## Story

As a teacher, I want to publish the closed assessment with a frozen snapshot so that subsequent bank edits do not corrupt in-flight results.

## Acceptance Criteria

- Publishing creates immutable snapshots of questions, options, answer key, scoring policy, and grade scale.
- Published assessment cannot be structurally edited.
- Changes after publication require a new assessment version or explicit teacher action (annulment/key correction).
- Snapshot creation is logged.
