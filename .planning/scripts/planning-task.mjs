#!/usr/bin/env node
import { spawnSync } from 'node:child_process';
import { existsSync, readdirSync, readFileSync, statSync, writeFileSync } from 'node:fs';
import path from 'node:path';

const cwd = process.cwd();
const args = process.argv.slice(2);
const stage = args.shift();
let format = 'markdown';
let execute = false;
let write = false;
let message = '';
const positional = [];
const files = [];

function usage() {
  return `Usage:
  node .planning/scripts/planning-task.mjs <stage> <planning-id> <story-NN> <task-NN> [options]

Stages:
  inspect     Locate task context, derive branches, commit metadata, affected files, and gate hints
  readiness  Run deterministic preflight checks before implementation
  git-setup  Prepare story/task branch commands; add --execute to run git commands
  start      Mark task/story IN PROGRESS; add --write to apply
  publish    Stage allowlisted files, commit, push, and create/reuse task PR; add --execute to run
  correction Commit and push review corrections on the same task branch; add --execute to run
  closeout   Mark task DONE and update the story task table; add --write to apply

Options:
  --format markdown|json
  --execute              Run git/gh commands for git-setup, publish, or correction
  --write                Apply planning file mutations for start or closeout
  --file <path>          Add an explicit publish/correction staging allowlist file
  --message <text>       Override commit message for publish/correction`;
}

for (let i = 0; i < args.length; i += 1) {
  const arg = args[i];
  if (arg === '--format') {
    format = args[i + 1] || '';
    i += 1;
  } else if (arg === '--execute') execute = true;
  else if (arg === '--write') write = true;
  else if (arg === '--file') {
    files.push(args[i + 1] || '');
    i += 1;
  } else if (arg === '--message') {
    message = args[i + 1] || '';
    i += 1;
  } else positional.push(arg);
}

if (!stage || ['-h', '--help', 'help'].includes(stage)) {
  console.log(usage());
  process.exit(0);
}
if (!['markdown', 'json'].includes(format)) fail(`Invalid --format: ${format}`, { help: usage() });
const [planningId, storyIdArg, taskIdArg] = positional;
if (!planningId || !storyIdArg || !taskIdArg) fail('Missing <planning-id> <story-NN> <task-NN>.', { help: usage() });

const planningRoot = path.join(cwd, '.planning');
if (!existsSync(planningRoot)) fail('No .planning/ directory found in the current workspace. Run /plan-init first or move to the project root.');

function fail(error, details = {}) {
  if (format === 'json') console.log(JSON.stringify({ ok: false, error, ...details }, null, 2));
  else {
    console.error(`ERROR: ${error}`);
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

function writeFile(file, text, touched) {
  touched.push(rel(file));
  if (write) writeFileSync(file, text);
}

function isDir(file) {
  return existsSync(file) && statSync(file).isDirectory();
}

function normalizeStoryId(value) {
  const match = String(value || '').match(/^story-0*(\d+)$/);
  if (!match) fail(`Invalid story id: ${value}`);
  return `story-${match[1].padStart(2, '0')}`;
}

function normalizeTaskId(value) {
  const match = String(value || '').match(/^task-0*(\d+)$/);
  if (!match) fail(`Invalid task id: ${value}`);
  return `task-${match[1].padStart(2, '0')}`;
}

function storyNumber(value) {
  const match = String(value || '').match(/^story-0*(\d+)/);
  return match ? Number(match[1]) : 0;
}

function taskNumber(value) {
  const match = String(value || '').match(/^task-0*(\d+)/);
  return match ? Number(match[1]) : 0;
}

function activePlanningDir() {
  const dir = path.join(planningRoot, 'active', planningId);
  if (!isDir(dir)) fail(`Active planning not found: ${planningId}`, { checked: rel(dir) });
  return dir;
}

function findStoryFile(planningDir, storyId) {
  const deepening = path.join(planningDir, '02-deepening');
  if (!isDir(deepening)) fail(`Missing deepening directory: ${rel(deepening)}`);
  const file = readdirSync(deepening)
    .filter((name) => name.startsWith(`${storyId}-`) && name.endsWith('.md'))
    .sort()[0];
  if (!file) fail(`Story file not found: ${storyId}`, { checked: rel(deepening) });
  return path.join(deepening, file);
}

function taskIdVariants(taskId) {
  const number = taskNumber(taskId);
  return [`task-${String(number).padStart(2, '0')}`, `task-${number}`];
}

function findTaskFile(storyFile, taskId) {
  const dir = storyFile.replace(/\.md$/, '');
  if (!isDir(dir)) fail(`Task directory not found: ${rel(dir)}. Run /plan-atomize or use /plan-task after task files exist.`);
  const variants = taskIdVariants(taskId);
  const file = readdirSync(dir)
    .filter((name) => variants.some((variant) => name.startsWith(`${variant}-`)) && name.endsWith('.md'))
    .sort()[0];
  if (!file) fail(`Task file not found: ${taskId}`, { checked: rel(dir) });
  return path.join(dir, file);
}

function extractStatus(text) {
  const match = /^>\s*\*\*Status:\*\*\s*(.+)$/im.exec(text);
  return match ? match[1].trim() : 'UNKNOWN';
}

function extractField(text, name) {
  const pattern = new RegExp(`^>\\s*\\*\\*${escapeRegex(name)}:\\*\\*\\s*(.+)$|^-\\s+\\*\\*${escapeRegex(name)}:\\*\\*\\s*(.+)$`, 'im');
  const match = pattern.exec(text);
  return match ? (match[1] || match[2] || '').trim() : '';
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

function escapeRegex(value) {
  return String(value).replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
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

function parseConfig() {
  const config = path.join(planningRoot, 'config.yml');
  const text = existsSync(config) ? read(config) : '';
  return {
    file: existsSync(config) ? rel(config) : null,
    baseBranch: valueFor(text, 'base_branch', 'main'),
    projectType: valueFor(text, 'type', 'software'),
    requiresTests: valueFor(text, 'requires_tests', 'true'),
    requiresGit: valueFor(text, 'requires_git', 'true'),
    docsOutputDir: valueFor(text, 'output_dir', 'docs'),
    smokeTestsFile: valueFor(text, 'smoke_tests_file', 'SMOKE-TESTS.md'),
    testSuiteGenerator: valueFor(text, 'test_suite_generator', 'scripts/generate-test-suite.sh'),
    loggingFile: valueFor(text, 'logging_file', 'LOGGING.md'),
  };
}

function valueFor(text, key, fallback) {
  const match = new RegExp(`^\\s*${escapeRegex(key)}:\\s*([^#\\n]+)`, 'm').exec(text);
  return match ? match[1].trim().replace(/^["']|["']$/g, '') : fallback;
}

function git(argsList, opts = {}) {
  const result = spawnSync('git', argsList, { cwd, encoding: 'utf8' });
  if (opts.allowFail) return result;
  if (result.status !== 0) fail(`git ${argsList.join(' ')} failed`, { stderr: result.stderr.trim(), stdout: result.stdout.trim() });
  return result;
}

function commandExists(name) {
  const result = spawnSync(name, ['--version'], { cwd, encoding: 'utf8' });
  return result.status === 0;
}

function currentBranch() {
  const result = git(['branch', '--show-current'], { allowFail: true });
  return result.status === 0 ? result.stdout.trim() : '';
}

function deriveBranches(storyFile, taskFile, config) {
  const storyBase = path.basename(storyFile, '.md');
  const taskBase = path.basename(taskFile, '.md');
  const branch = currentBranch();
  let storyBranch = storyBase;
  const storyIndex = branch.indexOf(storyBase);
  if (storyIndex > 0) storyBranch = branch.slice(0, storyIndex + storyBase.length);
  if (branch === storyBase || branch.endsWith(`/${storyBase}`)) storyBranch = branch;
  const taskBranch = `${storyBranch}/${taskBase}`;
  return { baseBranch: config.baseBranch, currentBranch: branch, storyBranch, taskBranch, storyBase, taskBase };
}

function affectedFiles(taskText) {
  const direct = extractField(taskText, 'Affected files / components');
  const source = direct || markdownSection(taskText, 'Technical Design');
  const candidates = [];
  for (const match of source.matchAll(/`([^`]+)`/g)) candidates.push(match[1]);
  if (candidates.length === 0 && direct) {
    candidates.push(...direct.split(/,|\n/).map((item) => item.trim()));
  }
  return [...new Set(candidates
    .map((item) => item.replace(/^\[[^\]]+\]\(([^)]+)\)$/, '$1').trim())
    .filter((item) => item && !item.includes('[') && !/^none$/i.test(item) && item !== '—'))];
}

function commitMeta(taskText, storyBase, taskBase, correction = false) {
  const objective = markdownSection(taskText, 'Objective');
  const workflow = extractField(taskText, 'Workflow');
  const lower = `${objective}\n${workflow}`.toLowerCase();
  let type = 'feat';
  if (/generate-document|document|write docs|update docs/.test(lower)) type = 'docs';
  else if (/\b(fix|correct|resolve|repair)\b/.test(lower)) type = 'fix';
  else if (/\b(refactor|restructure|reorganize|clean)\b/.test(lower)) type = 'refactor';
  else if (/\b(test|spec|coverage)\b/.test(lower)) type = 'test';
  else if (/\b(setup|configure|scaffold|init|install)\b/.test(lower)) type = 'chore';
  const scope = storyBase.replace(/^story-\d+-?/, '') || storyBase;
  const desc = correction ? `address review for ${taskBase.replace(/^task-\d+-?/, '')}` : taskBase.replace(/^task-\d+-?/, '').replace(/-/g, ' ');
  return { type, scope, description: desc, message: message || `${type}(${scope}): ${desc}` };
}

function dependencyStatuses(storyFile, taskText) {
  const depends = extractField(taskText, 'Depends On');
  if (!depends || ['—', '-', 'none'].includes(depends.toLowerCase())) return [];
  const taskDir = path.dirname(storyFile).replace(/\.md$/, '');
  return depends.split(',').map((item) => item.trim()).filter(Boolean).map((item) => {
    const id = normalizeTaskId(item);
    const variants = taskIdVariants(id);
    const file = isDir(taskDir)
      ? readdirSync(taskDir).filter((name) => variants.some((variant) => name.startsWith(`${variant}-`)) && name.endsWith('.md')).sort()[0]
      : '';
    if (!file) return { id, status: 'MISSING', file: null };
    const full = path.join(taskDir, file);
    return { id, status: extractStatus(read(full)), file: rel(full) };
  });
}

function codeTask(taskText, affected) {
  const text = `${taskText}\n${affected.join('\n')}`;
  return /\.(java|kt|ts|tsx|js|jsx|mjs|cjs|py|go|cs|rs|php|rb|scala|sql|tf|yaml|yml)$/i.test(text)
    || /\b(api|service|handler|controller|repository|adapter|worker|cli|migration|schema|database|infra)\b/i.test(text);
}

function dbTask(taskText, affected) {
  return /\b(migration|schema|database|db|table|column|index|seed|prisma|typeorm|sequelize|sqlalchemy|hibernate|jpa|entity|model|repository|persistence|ddl|flyway|liquibase)\b/i.test(`${taskText}\n${affected.join('\n')}`);
}

function context() {
  const storyId = normalizeStoryId(storyIdArg);
  const taskId = normalizeTaskId(taskIdArg);
  const planningDir = activePlanningDir();
  const storyFile = findStoryFile(planningDir, storyId);
  const taskFile = findTaskFile(storyFile, taskId);
  const storyText = read(storyFile);
  const taskText = read(taskFile);
  const config = parseConfig();
  const affected = affectedFiles(taskText);
  const branches = deriveBranches(storyFile, taskFile, config);
  const meta = commitMeta(taskText, branches.storyBase, branches.taskBase, stage === 'correction');
  const taskSuite = path.join(path.dirname(taskFile), 'test-suites', `${branches.taskBase}-test-suite.md`);
  return {
    planningId,
    storyId,
    taskId,
    planningDir,
    storyFile,
    taskFile,
    storyText,
    taskText,
    config,
    affectedFiles: affected,
    branches,
    commit: meta,
    statuses: {
      story: extractStatus(storyText),
      task: extractStatus(taskText),
      dependencies: dependencyStatuses(storyFile, taskText),
    },
    gates: {
      taskSuite: rel(taskSuite),
      taskSuiteExists: existsSync(taskSuite),
      codeTask: codeTask(taskText, affected),
      dbOrOrmTask: dbTask(taskText, affected),
      loggingFile: rel(path.join(planningRoot, config.loggingFile)),
      smokeFile: rel(path.join(planningRoot, config.smokeTestsFile)),
      testSuiteGenerator: rel(path.join(planningRoot, config.testSuiteGenerator)),
    },
  };
}

function readiness(ctx) {
  const blockers = [];
  const warnings = [];
  if (ctx.statuses.task === 'DONE') blockers.push('Task is already DONE.');
  for (const dep of ctx.statuses.dependencies) {
    if (dep.status !== 'DONE') blockers.push(`${dep.id} dependency is ${dep.status}.`);
  }
  if (ctx.config.requiresTests === 'true' && !ctx.gates.taskSuiteExists) warnings.push(`Task test suite is missing: ${ctx.gates.taskSuite}`);
  if (ctx.gates.codeTask && !existsSync(path.join(planningRoot, ctx.config.loggingFile))) warnings.push(`Logging policy file is missing: ${ctx.gates.loggingFile}`);
  if (ctx.affectedFiles.length === 0) warnings.push('Affected files / components is empty or placeholder-only; publish requires --file allowlist.');
  return { blockers, warnings };
}

function gitSetupCommands(ctx) {
  const b = ctx.branches;
  return [
    ['git', 'status', '--porcelain'],
    ['git', 'fetch', 'origin'],
    ['git', 'checkout', b.baseBranch],
    ['git', 'pull', '--ff-only', 'origin', b.baseBranch],
    ['git', 'checkout', '-B', b.storyBranch],
    ['git', 'push', '-u', 'origin', b.storyBranch],
    ['git', 'checkout', '-B', b.taskBranch],
    ['git', 'push', '-u', 'origin', b.taskBranch],
  ];
}

function publishCommands(ctx, correction = false) {
  const allowlist = [...new Set([...ctx.affectedFiles, rel(ctx.taskFile), ...files.filter(Boolean)])];
  const title = `${ctx.taskId}: ${ctx.branches.taskBase.replace(/^task-\d+-?/, '').replace(/-/g, ' ')}`;
  const body = `Task ${ctx.taskId} of story ${ctx.storyId} in planning ${ctx.planningId}.`;
  return {
    allowlist,
    commands: [
      ['git', 'add', ...allowlist],
      ['git', 'commit', '-m', correction ? (message || `fix(${ctx.commit.scope}): address review for ${ctx.taskId}`) : ctx.commit.message],
      ['git', 'push', '-u', 'origin', ctx.branches.taskBranch],
      ['gh', 'pr', 'create', '--title', title, '--body', body, '--base', ctx.branches.storyBranch, '--head', ctx.branches.taskBranch],
    ],
  };
}

function shell(command) {
  return command.map((part) => /[\s"'$`\\]/.test(part) ? JSON.stringify(part) : part).join(' ');
}

function runCommand(command, allowFailure = false) {
  const [bin, ...commandArgs] = command;
  const result = spawnSync(bin, commandArgs, { cwd, encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe'] });
  if (result.status !== 0 && !allowFailure) fail(`${shell(command)} failed`, { stdout: result.stdout.trim(), stderr: result.stderr.trim() });
  return { command: shell(command), status: result.status, stdout: result.stdout.trim(), stderr: result.stderr.trim() };
}

function executeGitSetup(ctx) {
  const dirty = git(['status', '--porcelain'], { allowFail: true });
  if (dirty.status !== 0) fail('Current directory is not a git repository or git status failed.', { stderr: dirty.stderr.trim() });
  if (dirty.stdout.trim()) fail('Worktree is dirty. Commit, stash, or discard changes before git-setup.', { dirty: dirty.stdout.trim().split('\n') });
  return gitSetupCommands(ctx).map((command) => runCommand(command));
}

function executePublish(ctx, correction = false) {
  const plan = publishCommands(ctx, correction);
  if (plan.allowlist.length === 0) fail('No staging allowlist. Add --file paths or fill Affected files / components.');
  const results = [];
  results.push(runCommand(['git', 'add', ...plan.allowlist]));
  const diff = spawnSync('git', ['diff', '--cached', '--quiet'], { cwd, encoding: 'utf8' });
  if (diff.status === 0) {
    results.push({ command: 'git diff --cached --quiet', status: 0, stdout: 'nothing staged', stderr: '' });
    return results;
  }
  results.push(runCommand(plan.commands[1]));
  results.push(runCommand(plan.commands[2]));
  if (commandExists('gh')) {
    const existing = spawnSync('gh', ['pr', 'list', '--head', ctx.branches.taskBranch, '--base', ctx.branches.storyBranch, '--state', 'all', '--limit', '1'], { cwd, encoding: 'utf8' });
    if (existing.status === 0 && existing.stdout.trim()) {
      results.push({ command: `gh pr list --head ${ctx.branches.taskBranch} --base ${ctx.branches.storyBranch}`, status: 0, stdout: existing.stdout.trim(), stderr: '' });
    } else {
      results.push(runCommand(plan.commands[3]));
    }
  } else {
    results.push({ command: shell(plan.commands[3]), status: 127, stdout: '', stderr: 'gh is not available; create the PR manually.' });
  }
  return results;
}

function setStatusLine(text, status) {
  return /^>\s*\*\*Status:\*\*.*$/m.test(text)
    ? text.replace(/^>\s*\*\*Status:\*\*.*$/m, `> **Status:** ${status}`)
    : text;
}

function updateStoryTaskStatus(storyText, taskId, status) {
  const number = taskNumber(taskId);
  return storyText.split(/\r?\n/).map((line) => {
    const cells = line.split('|');
    if (cells.length < 6 || !/^\s*\|\s*\d+/.test(line)) return line;
    if (Number(cells[1].trim()) !== number && !cells[2].includes(taskId)) return line;
    cells[4] = ` ${status} `;
    return cells.join('|');
  }).join('\n');
}

function checkDoneCriteria(text) {
  return text.replace(/^(\s*-\s+\[)[ xX](\]\s+)/gm, '$1x$2');
}

function mutateStatus(ctx, status) {
  const touched = [];
  let taskText = setStatusLine(ctx.taskText, status);
  if (status === 'DONE') taskText = checkDoneCriteria(taskText);
  writeFile(ctx.taskFile, taskText, touched);
  let storyText = updateStoryTaskStatus(ctx.storyText, ctx.taskId, status);
  if (status === 'IN PROGRESS' && extractStatus(storyText) === 'TODO') storyText = setStatusLine(storyText, 'IN PROGRESS');
  writeFile(ctx.storyFile, storyText, touched);
  return touched;
}

function render(ctx, extra = {}) {
  const report = {
    ok: !(extra.blockers && extra.blockers.length),
    stage,
    execute,
    write,
    planningId: ctx.planningId,
    storyId: ctx.storyId,
    taskId: ctx.taskId,
    paths: {
      story: rel(ctx.storyFile),
      task: rel(ctx.taskFile),
    },
    statuses: ctx.statuses,
    branches: ctx.branches,
    config: ctx.config,
    affectedFiles: ctx.affectedFiles,
    commit: ctx.commit,
    gates: ctx.gates,
    ...extra,
  };
  if (format === 'json') console.log(JSON.stringify(report, null, 2));
  else printMarkdown(report);
}

function printMarkdown(report) {
  console.log(`# Plan Task ${report.stage}\n`);
  console.log(`Task: \`${report.planningId} ${report.storyId} ${report.taskId}\``);
  console.log(`Task file: \`${report.paths.task}\``);
  console.log(`Story file: \`${report.paths.story}\``);
  console.log(`Status: task=\`${report.statuses.task}\`, story=\`${report.statuses.story}\``);
  console.log(`Branches: story=\`${report.branches.storyBranch}\`, task=\`${report.branches.taskBranch}\``);
  if (report.blockers?.length) {
    console.log('\nBlockers:');
    for (const item of report.blockers) console.log(`- ${item}`);
  }
  if (report.warnings?.length) {
    console.log('\nWarnings:');
    for (const item of report.warnings) console.log(`- ${item}`);
  }
  if (report.affectedFiles?.length) {
    console.log('\nAffected files:');
    for (const file of report.affectedFiles) console.log(`- \`${file}\``);
  }
  if (report.commands?.length) {
    console.log('\nCommands:');
    for (const command of report.commands) console.log(`- \`${Array.isArray(command) ? shell(command) : command}\``);
  }
  if (report.results?.length) {
    console.log('\nExecution results:');
    for (const result of report.results) console.log(`- \`${result.command}\` -> ${result.status}`);
  }
  if (report.touched?.length) {
    console.log('\nTouched paths:');
    for (const file of report.touched) console.log(`- \`${file}\``);
    if (!report.write) console.log('\nDry run only. Re-run with `--write` to apply.');
  }
  if (report.publish) {
    console.log(`\nCommit message: \`${report.publish.message}\``);
    console.log(`PR target: \`${report.branches.taskBranch}\` -> \`${report.branches.storyBranch}\``);
    if (!report.execute) console.log('Dry run only. Re-run with `--execute` after approval.');
  }
}

const ctx = context();
const checks = readiness(ctx);

function stopOnBlockers() {
  if (checks.blockers.length === 0) return;
  render(ctx, checks);
  process.exit(1);
}

switch (stage) {
  case 'inspect':
    render(ctx, checks);
    break;
  case 'readiness':
    render(ctx, checks);
    process.exit(checks.blockers.length ? 1 : 0);
    break;
  case 'git-setup': {
    stopOnBlockers();
    const commands = gitSetupCommands(ctx);
    const results = execute ? executeGitSetup(ctx) : [];
    render(ctx, { ...checks, commands, results });
    break;
  }
  case 'start': {
    stopOnBlockers();
    const touched = mutateStatus(ctx, 'IN PROGRESS');
    render(ctx, { ...checks, touched });
    break;
  }
  case 'publish':
  case 'correction': {
    stopOnBlockers();
    const correction = stage === 'correction';
    const publish = publishCommands(ctx, correction);
    const results = execute ? executePublish(ctx, correction) : [];
    render(ctx, { ...checks, commands: publish.commands, results, publish: { allowlist: publish.allowlist, message: correction ? (message || `fix(${ctx.commit.scope}): address review for ${ctx.taskId}`) : ctx.commit.message } });
    break;
  }
  case 'closeout': {
    stopOnBlockers();
    const touched = mutateStatus(ctx, 'DONE');
    render(ctx, { ...checks, touched });
    break;
  }
  default:
    fail(`Unknown stage: ${stage}`, { help: usage() });
}
