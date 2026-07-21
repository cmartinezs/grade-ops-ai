import { execFileSync } from "node:child_process";
import path from "node:path";

// agents/ (Gemini) is not available in this local/CI environment (a documented ceiling from
// task-06/task-12/task-13, not a defect) — so draft generation itself can't be exercised.
// This inserts a draft row directly, exactly like task-13's manual walkthrough and
// api/scripts/smoke-test.sh's own §5 seeding step, to exercise the Draft Builder screen's
// real render/edit/save/refresh/regenerate seams against real api/ + Postgres.
const API_DIR = path.resolve(__dirname, "../../../api");

export function seedDraftVersion(
  assessmentId: string,
  opts: { versionNumber?: number; title?: string } = {}
): void {
  const versionNumber = opts.versionNumber ?? 1;
  const title = (opts.title ?? "Recursividad: Fibonacci").replace(/'/g, "''");

  const sql = `
    INSERT INTO assessment_drafts
      (assessment_id, version_number, title, context, instructions, objectives, deliverables, constraints)
    VALUES
      ('${assessmentId}', ${versionNumber}, '${title}', 'Evaluación práctica',
       'Implementa una función recursiva.', '["Comprender recursividad"]'::jsonb,
       '["Archivo .py"]'::jsonb, '["No usar librerías externas"]'::jsonb);
  `;

  execFileSync(
    "docker",
    [
      "compose",
      "-f",
      "compose.smoke.yml",
      "exec",
      "-T",
      "postgres",
      "psql",
      "-U",
      "gradeops",
      "-d",
      "gradeops",
      "-v",
      "ON_ERROR_STOP=1",
      "-c",
      sql,
    ],
    { cwd: API_DIR, stdio: "pipe" }
  );
}
