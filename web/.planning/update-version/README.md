# Planning Update-Version Migrations

This directory stores versioned migrations for `.planning/` workspaces created by older plugin versions.

Use:

```bash
/plan-update-version <from> <to>
```

The command resolves exact version labels to an ordered chain of adjacent major-pair migration files named `<N>-<N+1>.md`, then falls back to the plugin template. For example, `/plan-update-version 1.4.0 2.0.0` uses `1-2.md`, `/plan-update-version 2.1.0 3.10.0` uses `2-3.md`, and `/plan-update-version 2.1.0 4.0.0` uses `2-3.md` followed by `3-4.md`.

## Available Migrations

| From | To | File | Purpose |
|------|----|------|---------|
| `1` | `2` | [`1-2.md`](1-2.md) | Migrate any `1.x` planning workspace to the current `2.x` planning baseline, including old `scope` terminology to `story` terminology. |
| `2` | `3` | [`2-3.md`](2-3.md) | Migrate any `2.x` planning workspace to the current `3.10.0` planning baseline. |

## Authoring Rules

- Keep each migration self-contained and executable by an agent.
- Include discovery, apply, and verification sections.
- List terms that must not be replaced blindly.
- Prefer conservative Markdown edits over global substitutions.
- For patch or minor releases, update the existing `<previous-major>-<current-major>.md` migration so it remains cumulative for the current major line.
- Add a new adjacent `<previous-major>-<current-major>.md` migration file only when releasing a new major version.
- Do not create skip migrations such as `2-4.md`; the command applies `2-3.md` and then `3-4.md`.
