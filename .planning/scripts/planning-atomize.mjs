#!/usr/bin/env node
import { spawnSync } from 'node:child_process';
import { existsSync, mkdirSync, readdirSync, readFileSync, statSync, writeFileSync } from 'node:fs';
import path from 'node:path';

const cwd = process.cwd();
const args = process.argv.slice(2);
const command = args.shift();
const positional = [];
let format = 'markdown';
let write = false;
let runSuite = false;
let fromFile = '';

function usage() {
  return `Usage:
  node .planning/scripts/planning-atomize.mjs inspect <planning-id> [story-NN] [--format markdown|json]
  node .planning/scripts/planning-atomize.mjs apply <planning-id> <story-NN> --from <breakdown.json> [--write] [--run-suite] [--format markdown|json]

Breakdown JSON schema:
{
  "tasks": [
    {
      "title": "Implement reset endpoint",
      "workflow": "GENERATE-DOCUMENT",
      "dependsOn": ["task-01"],
      "output": "Endpoint, tests, and evidence",
      "objective": "Single verifiable deliverable.",
      "technicalDesign": {
        "approach": "...",
        "affectedFiles": ["src/..."],
        "interfaces": "...",
        "risk": "Medium - ...",
        "designNotes": "..."
      },
      "implementationSteps": ["Edit src/...", "Add tests..."],
      "verification": [{"check": "Tests pass", "how": "Run npm test"}],
      "doneCriteria": ["Deliverable exists", "Verification evidence captured"]
    }
  ]
}`;
}

for (let i = 0; i < args.length; i += 1) {
  const arg = args[i];
  if (arg === '--format') {
    format = args[i + 1] || '';
    i += 1;
  } else if (arg === '--write') write = true;
  else if (arg === '--run-suite') runSuite = true;
  else if (arg === '--from') {
    fromFile = args[i + 1] || '';
    i += 1;
  } else positional.push(arg);
}

if (!['markdown', 'json'].includes(format)) fail(`Invalid --format: ${format}`);

function fail(message, details = {}) {
  if (format === 'json') console.log(JSON.stringify({ ok: false, error: message, ...details }, null, 2));
  else {
    console.error(`ERROR: ${message}`);
    if (details.help) console.error(`\n${details.help}`);
  }
  process.exit(1);
}

function rel(file) {
  return path.relative(cwd, file).replaceAll(path.sep, '/') || '.';
}

function read(file) {
  return readFileSync(file, 'utf8');
}

function writeOutput(file, text, touched) {
  mkdirSync(path.dirname(file), { recursive: true });
  touched.push(rel(file));
  if (write) writeFileSync(file, text);
}

function isDir(file) {
  return existsSync(file) && statSync(file).isDirectory();
}

function escapeRegex(value) {
  return String(value).replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function slugify(value) {
  return String(value || 'task')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
    .slice(0, 80) || 'task';
}

function titleize(value) {
  return String(value || '')
    .replace(/^\s*#+\s*/, '')
    .replace(/\[([^\]]+)\]\([^)]+\)/g, '$1')
    .trim();
}

function normalizeStoryId(value) {
  const match = String(value || '').match(/^story-0*(\d+)$/i);
  if (!match) fail(`Invalid story id: ${value}`);
  return `story-${match[1].padStart(2, '0')}`;
}

function normalizeTaskId(value) {
  const match = String(value || '').match(/^task-0*(\d+)/i);
  return match ? `task-${match[1].padStart(2, '0')}` : '';
}

function taskId(number) {
  return `task-${String(number).padStart(2, '0')}`;
}

function planningRoot() {
  const root = path.join(cwd, '.planning');
  if (!isDir(root)) fail('No .planning/ directory found in the current workspace. Run /plan-init first or move to the project root.');
  return root;
}

function planningDir(id) {
  const dir = path.join(planningRoot(), 'active', id);
  if (!isDir(dir)) fail(`Active planning not found in current directory: .planning/active/${id}`);
  return dir;
}

function findStoryFile(dir, storyId) {
  const deepening = path.join(dir, '02-deepening');
  if (!isDir(deepening)) fail(`Missing deepening directory: ${rel(deepening)}`);
  const file = readdirSync(deepening)
    .filter((name) => name.startsWith(`${storyId}-`) && name.endsWith('.md'))
    .sort()[0];
  if (!file) fail(`Story not found: ${storyId}`, { checked: rel(deepening) });
  return path.join(deepening, file);
}

function listStoryFiles(dir) {
  const deepening = path.join(dir, '02-deepening');
  if (!isDir(deepening)) fail(`Missing deepening directory: ${rel(deepening)}`);
  return readdirSync(deepening)
    .filter((name) => /^story-\d+-.*\.md$/i.test(name))
    .sort((a, b) => storyNumber(a) - storyNumber(b) || a.localeCompare(b))
    .map((name) => path.join(deepening, name));
}

function storyNumber(value) {
  const match = String(value || '').match(/story-0*(\d+)/i);
  return match ? Number(match[1]) : 0;
}

function markdownSection(text, heading) {
  const escaped = escapeRegex(heading);
  const match = text.match(new RegExp(`^(#{1,6})\\s+${escaped}\\b[^\\n]*\\n`, 'im'));
  if (!match) return '';
  const level = match[1].length;
  const start = match.index + match[0].length;
  const rest = text.slice(start);
  const next = rest.match(new RegExp(`^#{1,${level}}\\s+`, 'm'));
  return text.slice(start, next ? start + next.index : text.length);
}

function heading(text) {
  const match = text.match(/^#\s+(.+)$/m) || text.match(/^#{2,6}\s+(.+)$/m);
  return match ? titleize(match[1]) : '';
}

function extractStatus(text) {
  const match = /^>\s*\*\*Status:\*\*\s*(.+)$/im.exec(text);
  return match ? match[1].trim() : 'UNKNOWN';
}

function parseConfig() {
  const root = planningRoot();
  const file = path.join(root, 'config.yml');
  const text = existsSync(file) ? read(file) : '';
  return {
    file: existsSync(file) ? rel(file) : null,
    projectType: valueFor(text, 'type', 'software'),
    requiresTests: valueFor(text, 'requires_tests', 'true'),
    requiresGit: valueFor(text, 'requires_git', 'true'),
    smokeTestsFile: valueFor(text, 'smoke_tests_file', 'SMOKE-TESTS.md'),
    testSuiteGenerator: valueFor(text, 'test_suite_generator', 'scripts/generate-test-suite.sh'),
    loggingFile: valueFor(text, 'logging_file', 'LOGGING.md'),
  };
}

function valueFor(text, key, fallback) {
  const match = new RegExp(`^\\s*${escapeRegex(key)}:\\s*([^#\\n]+)`, 'm').exec(text);
  return match ? match[1].trim().replace(/^["']|["']$/g, '') : fallback;
}

function splitMarkdownRow(line) {
  const trimmed = line.trim();
  if (!trimmed.startsWith('|') || !trimmed.endsWith('|')) return null;
  return trimmed.slice(1, -1).split('|').map((cell) => cell.trim());
}

function taskRows(storyText) {
  let inFence = false;
  const rows = [];
  for (const line of markdownSection(storyText, 'Tasks').split(/\r?\n/)) {
    if (line.trim().startsWith('```')) {
      inFence = !inFence;
      continue;
    }
    if (inFence) continue;
    const cells = splitMarkdownRow(line);
    if (!cells || !/^\d+$/.test(cells[0] || '')) continue;
    rows.push({ number: Number(cells[0]), task: cells[1] || '', workflow: cells[2] || '', status: cells[3] || '', output: cells[4] || '' });
  }
  return rows;
}

function storyInfo(file) {
  const text = read(file);
  const dir = file.replace(/\.md$/, '');
  const taskFiles = isDir(dir)
    ? readdirSync(dir).filter((name) => /^task-\d+-.*\.md$/i.test(name)).sort()
    : [];
  return {
    id: (path.basename(file).match(/^(story-\d+)/i) || [])[1],
    title: heading(text) || path.basename(file, '.md'),
    file: rel(file),
    status: extractStatus(text),
    taskRows: taskRows(text),
    atomized: taskFiles.length > 0,
    taskDir: rel(dir),
    taskFiles: taskFiles.map((name) => rel(path.join(dir, name))),
  };
}

function inspect(planningId, storyId = '') {
  const dir = planningDir(planningId);
  const config = parseConfig();
  const storyFiles = storyId ? [findStoryFile(dir, normalizeStoryId(storyId))] : listStoryFiles(dir);
  const stories = storyFiles.map(storyInfo);
  const worklist = stories
    .filter((story) => story.status !== 'DONE')
    .filter((story) => !story.atomized);
  return {
    ok: true,
    command: 'inspect',
    planning: planningId,
    config,
    storyCount: stories.length,
    stories,
    worklist: worklist.map((story) => story.id),
    nothingToAtomize: worklist.length === 0,
  };
}

function loadBreakdown(file) {
  if (!file) fail('Missing --from <breakdown.json>.', { help: usage() });
  const absolute = path.resolve(cwd, file);
  if (!existsSync(absolute)) fail(`Breakdown file does not exist: ${file}`);
  let parsed;
  try {
    parsed = JSON.parse(read(absolute));
  } catch (error) {
    fail(`Invalid breakdown JSON: ${error.message}`);
  }
  const tasks = Array.isArray(parsed) ? parsed : parsed.tasks;
  if (!Array.isArray(tasks) || tasks.length === 0) fail('Breakdown must contain a non-empty tasks array.');
  const normalized = tasks.map(normalizeTask).map((task, index) => ({ ...task, id: taskId(index + 1), number: index + 1 }));
  validateDependencies(normalized);
  return normalized;
}

function validateDependencies(tasks) {
  const numbers = new Map(tasks.map((task) => [task.id, task.number]));
  for (const task of tasks) {
    for (const dep of task.dependsOn) {
      if (!numbers.has(dep)) fail(`${task.id} depends on unknown task: ${dep}`);
      if (numbers.get(dep) >= task.number) fail(`${task.id} depends on ${dep}, but dependencies must point only to lower-numbered tasks.`);
    }
  }
}

function normalizeTask(task) {
  const title = String(task.title || task.name || '').trim();
  if (!title) fail('Every task needs a title.');
  const tech = task.technicalDesign || {};
  return {
    title,
    slug: slugify(title),
    workflow: String(task.workflow || 'GENERATE-DOCUMENT').trim(),
    dependsOn: normalizeDepends(task.dependsOn),
    output: String(task.output || `${title} delivered`).trim(),
    objective: String(task.objective || task.deliverable || `${title} exists and can be verified.`).trim(),
    technicalDesign: {
      approach: stringOr(tech.approach, 'Follow the story design and keep this task limited to one verifiable deliverable.'),
      affectedFiles: arrayOr(tech.affectedFiles || task.affectedFiles, ['TBD during task execution']),
      interfaces: stringOr(tech.interfaces, 'None documented.'),
      risk: stringOr(tech.risk, 'Medium - validate scope and evidence during execution.'),
      designNotes: stringOr(tech.designNotes, 'No extra constraints documented.'),
    },
    implementationSteps: arrayOr(task.implementationSteps || task.steps, [`Implement ${title}.`, 'Add or update verification evidence.']),
    verification: verificationRows(task.verification),
    smokeChecks: verificationRows(task.smokeChecks),
    dbOrmChecks: verificationRows(task.dbOrmChecks),
    logging: task.logging || {},
    testSuite: task.testSuite || {},
    doneCriteria: arrayOr(task.doneCriteria, [`${title} deliverable exists and is verified.`, 'No unintended expansion: the task satisfies [CHECK-ATOMICITY].']),
  };
}

function stringOr(value, fallback) {
  const text = String(value || '').trim();
  return text || fallback;
}

function arrayOr(value, fallback) {
  if (Array.isArray(value)) return value.map((item) => String(item).trim()).filter(Boolean);
  if (typeof value === 'string') return value.split(/\n|\|/).map((item) => item.trim()).filter(Boolean);
  return fallback;
}

function normalizeDepends(value) {
  return arrayOr(value, [])
    .map((item) => normalizeTaskId(item) || String(item).trim())
    .filter(Boolean);
}

function verificationRows(value) {
  if (Array.isArray(value) && value.length) {
    return value.map((item) => {
      if (typeof item === 'string') return { check: item, how: 'Review evidence during task execution.' };
      return { check: stringOr(item.check || item.verification, 'Verification check'), how: stringOr(item.how || item.validate, 'Review evidence during task execution.') };
    });
  }
  return [{ check: 'Task deliverable is verified', how: 'Run the generated task test suite or capture manual evidence.' }];
}

function taskTemplate() {
  const file = path.join(planningRoot(), '_template', '02-deepening', 'task-NN-name.md');
  if (!existsSync(file)) fail(`Missing task template: ${rel(file)}`);
  return read(file);
}

function renderTaskFile(task, storyId, storyBase, config) {
  const rows = (items) => items.map((item, index) => `| ${index + 1} | ${item.check} | ${item.how} |`).join('\n');
  const affected = task.technicalDesign.affectedFiles.map((item) => `\`${item}\``).join(', ');
  const testSuiteFile = `test-suites/${task.id}-${task.slug}-test-suite.md`;
  const loggingMechanism = stringOr(task.logging.mechanism, `Use .planning/${config.loggingFile}; if no logger exists, propose a stack-appropriate mechanism and wait for human decision.`);
  const correlation = stringOr(task.logging.correlation, 'Propagate request id / trace id / span id where the changed flow crosses boundaries.');
  const tracePoints = stringOr(task.logging.tracePoints, 'Entry point, decision points, external calls, persistence calls, retries/fallbacks, and completion.');
  const evidence = stringOr(task.logging.evidence, 'Capture test, review, or log sample evidence during execution.');
  const dbRows = task.dbOrmChecks.length ? task.dbOrmChecks : [{ check: 'Database / ORM consistency', how: 'N/A if this task does not change database or ORM artifacts.' }];
  const smokeRows = task.smokeChecks.length ? task.smokeChecks : [
    { check: 'Supporting services are ready', how: `Use .planning/${config.smokeTestsFile} or infer minimal local services.` },
    { check: 'App compiles and starts', how: 'Run the project build/start command or document why it is not applicable.' },
    { check: 'Changed surface responds correctly', how: 'Exercise the smallest changed path and capture evidence.' },
  ];
  return `# ${task.id.toUpperCase()} - ${task.title}

> **Status:** TODO
> **Workflow:** ${task.workflow}
> **Depends On:** ${task.dependsOn.length ? task.dependsOn.join(', ') : '—'}
> [← story file](../${storyBase}.md)

---

## Objective

${task.objective}

---

## Technical Design

- **Approach:** ${task.technicalDesign.approach}
- **Affected files / components:** ${affected}
- **Interfaces / contracts:** ${task.technicalDesign.interfaces}
- **Risk:** ${task.technicalDesign.risk}
- **Design notes:** ${task.technicalDesign.designNotes}

---

## Implementation Steps

${task.implementationSteps.map((step, index) => `${index + 1}. ${step}`).join('\n')}

---

## Verification

| # | Verification | How to validate |
|---|-------------|----------------|
${rows(task.verification)}

### Software Smoke Test Check

| # | Check | How to validate |
|---|-------|----------------|
${rows(smokeRows)}

### Database / ORM Consistency Check

| # | Check | How to validate |
|---|-------|----------------|
${rows(dbRows)}

### Logging / Observability

- **Logging mechanism:** ${loggingMechanism}
- **Correlation / trace context:** ${correlation}
- **Levels by event criticality:** TRACE for detailed flow, DEBUG for diagnostics, INFO for milestones, WARN for recoverable anomalies, ERROR for failed operations, FATAL for unsafe process/system failure
- **Execution trace points:** ${tracePoints}
- **Sensitive data guardrails:** Do not log secrets, tokens, credentials, passwords, personal data, or raw payloads unless explicitly approved and redacted.
- **Verification evidence:** ${evidence}

### Generated Test Suite

- **Task suite file:** \`${testSuiteFile}\`
- **Required gates:** ${stringOr(task.testSuite.requiredGates, 'unit, coverage, integration, acceptance/e2e, static analysis, code style, architecture/design guide review, smoke, security/dependency scan, and mutation/test-strength when applicable.')}
- **Architecture guides:** ${stringOr(task.testSuite.architectureGuides, 'DDD, Hexagonal, DRY, SOLID, GoF pattern use, and project-specific guides under docs/ or .planning/WORKFLOWS/05-SDLC-PHASE-GUIDANCE/.')}
- **Acceptance environment:** ${stringOr(task.testSuite.acceptanceEnvironment, 'Prefer Docker Compose, Testcontainers, local emulators, sandbox profiles, or disposable fixtures over shared services.')}
- **Acceptance dependency inventory:** ${stringOr(task.testSuite.acceptanceDependencyInventory, 'List every internal module, database, migration, external API, queue, storage, env var, secret, port, readiness check, and teardown action.')}

---

## Done Criteria

${task.doneCriteria.map((item) => `- [ ] ${item}`).join('\n')}
- [ ] Task test suite was generated/refreshed and every applicable gate has command output or documented evidence
${config.requiresGit === 'true' ? '- [ ] For git-enabled tasks, implementation was committed, pushed, and published in a task PR before human review\n- [ ] Human developer PR review completed; requested corrections, if any, were implemented, pushed to the same PR, and re-reviewed' : '- [ ] Human review completed when required by the planning workflow'}

---

> [← story file](../${storyBase}.md)
`;
}

function replaceTasksSection(storyText, storyBase, tasks) {
  const table = [
    '| # | Task | Workflow | Status | Output |',
    '|---|------|----------|--------|--------|',
    ...tasks.map((task) => `| ${task.number} | [${task.title}](${storyBase}/${task.id}-${task.slug}.md) | ${task.workflow} | TODO | ${task.output} |`),
  ].join('\n');
  const replacement = `## Tasks\n\n${table}\n`;
  if (/^#{1,6}\s+Tasks\b[^\n]*\n/im.test(storyText)) {
    return storyText.replace(/^#{1,6}\s+Tasks\b[^\n]*\n[\s\S]*?(?=\n#{1,6}\s+|(?![\s\S]))/im, replacement);
  }
  return `${storyText.replace(/\s*$/, '\n\n')}${replacement}`;
}

function apply(planningId, storyIdArg) {
  const storyId = normalizeStoryId(storyIdArg);
  const dir = planningDir(planningId);
  const config = parseConfig();
  const storyFile = findStoryFile(dir, storyId);
  const storyText = read(storyFile);
  if (extractStatus(storyText) === 'DONE') fail('Cannot atomize a DONE story.');
  const storyBase = path.basename(storyFile, '.md');
  const taskDir = storyFile.replace(/\.md$/, '');
  const existingTasks = isDir(taskDir) ? readdirSync(taskDir).filter((name) => /^task-\d+-.*\.md$/i.test(name)) : [];
  if (existingTasks.length) fail('Story already has task files. Use /plan-task-validate before changing an atomized story.', { taskDir: rel(taskDir), existingTasks });
  const tasks = loadBreakdown(fromFile);
  const touched = [];
  taskTemplate();
  const nextStory = replaceTasksSection(storyText, storyBase, tasks);
  writeOutput(storyFile, nextStory, touched);
  for (const task of tasks) {
    const file = path.join(taskDir, `${task.id}-${task.slug}.md`);
    writeOutput(file, renderTaskFile(task, storyId, storyBase, config), touched);
  }
  const suiteCommand = ['bash', path.join('.planning', config.testSuiteGenerator), '--planning', planningId, '--story', storyId, '--all'];
  const suite = runSuite && write ? runCommand(suiteCommand) : null;
  return {
    ok: true,
    command: 'apply',
    write,
    planning: planningId,
    story: storyId,
    storyFile: rel(storyFile),
    taskDir: rel(taskDir),
    taskCount: tasks.length,
    tasks: tasks.map((task) => ({ id: task.id, title: task.title, file: rel(path.join(taskDir, `${task.id}-${task.slug}.md`)), dependsOn: task.dependsOn })),
    touched,
    suiteCommand: suiteCommand.join(' '),
    suite,
  };
}

function runCommand(commandParts) {
  const result = spawnSync(commandParts[0], commandParts.slice(1), { cwd, encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe'] });
  return {
    command: commandParts.join(' '),
    status: result.status,
    stdout: result.stdout.trim(),
    stderr: result.stderr.trim(),
  };
}

function print(result) {
  if (format === 'json') console.log(JSON.stringify(result, null, 2));
  else console.log(renderMarkdown(result));
}

function renderMarkdown(result) {
  if (result.command === 'inspect') {
    const lines = ['# Plan Atomize Inspect', '', `Planning: \`${result.planning}\``, `Stories: ${result.storyCount}`, `Worklist: ${result.worklist.length ? result.worklist.map((item) => `\`${item}\``).join(', ') : 'none'}`];
    if (result.stories.length) {
      lines.push('', '| Story | Status | Atomized | Task Rows | Task Files |', '|-------|--------|----------|-----------|------------|');
      for (const story of result.stories) lines.push(`| ${story.id} | ${story.status} | ${story.atomized ? 'yes' : 'no'} | ${story.taskRows.length} | ${story.taskFiles.length} |`);
    }
    if (result.nothingToAtomize) lines.push('', 'Nothing to atomize.');
    return lines.join('\n');
  }
  const lines = ['# Plan Atomize Apply', '', `Mode: ${result.write ? 'write' : 'dry-run'}`, `Planning: \`${result.planning}\``, `Story: \`${result.story}\``, `Task dir: \`${result.taskDir}\``, `Tasks: ${result.taskCount}`];
  if (result.tasks.length) {
    lines.push('', '| Task | Title | Depends On | File |', '|------|-------|------------|------|');
    for (const task of result.tasks) lines.push(`| ${task.id} | ${task.title} | ${task.dependsOn.join(', ') || '—'} | \`${task.file}\` |`);
  }
  lines.push('', 'Touched paths:');
  for (const file of result.touched) lines.push(`- \`${file}\``);
  lines.push('', `Test-suite command: \`${result.suiteCommand}\``);
  if (result.suite) lines.push(`Suite command exit: ${result.suite.status}`);
  if (!result.write) lines.push('', 'Dry run only. Re-run with `--write` after approval.');
  return lines.join('\n');
}

switch (command) {
  case 'inspect': {
    const planningId = positional[0];
    if (!planningId) fail('Missing <planning-id>.', { help: usage() });
    print(inspect(planningId, positional[1]));
    break;
  }
  case 'apply': {
    const planningId = positional[0];
    const storyId = positional[1];
    if (!planningId || !storyId) fail('Missing <planning-id> <story-NN>.', { help: usage() });
    print(apply(planningId, storyId));
    break;
  }
  case '--help':
  case '-h':
  case undefined:
    console.log(usage());
    break;
  default:
    fail(`Unknown command: ${command}`, { help: usage() });
}
