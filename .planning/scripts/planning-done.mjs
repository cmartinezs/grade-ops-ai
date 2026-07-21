#!/usr/bin/env node
import { existsSync, readdirSync, readFileSync, writeFileSync } from 'node:fs';
import path from 'node:path';

const cwd = process.cwd();
const args = process.argv.slice(2);
let planningId = '';
let storyId = '';
let taskId = '';
let finalizeStory = false;
let gitPlan = false;
let format = 'markdown';

function usage() {
  return `Usage:
  node .planning/scripts/planning-done.mjs <NNN-slug> <story-NN> [task-NN] [--finalize-story] [--git-plan] [--format markdown|json]

Marks task completion or finalizes approved story state in .planning/active/.`;
}

function fail(message, details = {}) {
  if (format === 'json') console.log(JSON.stringify({ ok: false, error: message, ...details }, null, 2));
  else {
    console.error(`ERROR: ${message}`);
    if (details.help) console.error(`\n${details.help}`);
  }
  process.exit(1);
}

for (let i = 0; i < args.length; i += 1) {
  const arg = args[i];
  if (arg === '--finalize-story') finalizeStory = true;
  else if (arg === '--git-plan') gitPlan = true;
  else if (arg === '--format') {
    format = args[i + 1] || '';
    i += 1;
  } else if (!planningId) planningId = arg;
  else if (!storyId) storyId = arg;
  else if (!taskId) taskId = arg;
  else fail(`Unexpected argument: ${arg}`, { help: usage() });
}

if (!['markdown', 'json'].includes(format)) fail(`Invalid --format: ${format}`, { help: usage() });
if (!planningId || !storyId) fail('Missing <planning-id> <story-id>.', { help: usage() });

function rel(file) {
  return path.relative(cwd, file) || '.';
}

function read(file) {
  return readFileSync(file, 'utf8');
}

function write(file, text) {
  writeFileSync(file, text);
}

function lineOf(text, matcher) {
  const lines = text.split('\n');
  const index = lines.findIndex((line) => matcher.test(line));
  return index >= 0 ? index + 1 : null;
}

function storyNumber(id) {
  const match = id.match(/^story-(\d+)/);
  return match ? match[1].replace(/^0+/, '') || '0' : '';
}

function taskNumber(id) {
  const match = id.match(/^task-0*(\d+)/);
  return match ? match[1] : '';
}

function taskIdVariants(id) {
  const number = taskNumber(id);
  if (!number) return [id];
  return [...new Set([id, `task-${number}`, `task-${number.padStart(2, '0')}`])];
}

function findStoryFile(planningDir, id) {
  const deepening = path.join(planningDir, '02-deepening');
  if (!existsSync(deepening)) fail(`Missing deepening directory: ${rel(deepening)}`);
  const file = readdirSync(deepening)
    .filter((name) => name.startsWith(`${id}-`) && name.endsWith('.md'))
    .sort()[0];
  if (!file) fail(`Story not found: ${id}`, { checked: rel(deepening) });
  return path.join(deepening, file);
}

function findStoryDir(storyFile) {
  const dir = storyFile.replace(/\.md$/, '');
  return existsSync(dir) ? dir : '';
}

function findTaskFile(storyDir, id) {
  if (!storyDir) return '';
  const variants = taskIdVariants(id);
  const file = readdirSync(storyDir)
    .filter((name) => variants.some((variant) => name.startsWith(`${variant}-`)) && name.endsWith('.md'))
    .sort()[0];
  return file ? path.join(storyDir, file) : '';
}

function setStatusLine(text, status) {
  if (/^>\s*\*\*Status:\*\*.*$/m.test(text)) {
    return text.replace(/^>\s*\*\*Status:\*\*.*$/m, `> **Status:** ${status}`);
  }
  return text;
}

function getStatus(text) {
  const match = text.match(/^>\s*\*\*Status:\*\*\s*(.+)$/m);
  return match ? match[1].trim() : '';
}

function setStoryTaskStatus(text, id, status) {
  const number = taskNumber(id);
  return text.split('\n').map((line) => {
    if (!line.trim().startsWith('|')) return line;
    const cells = line.split('|');
    if (cells.length < 6) return line;
    const firstCell = cells[1].trim();
    const taskCell = cells[2] || '';
    const matchesNumber = firstCell === number;
    const matchesLink = taskIdVariants(id).some((variant) => taskCell.includes(`${variant}-`) || taskCell.includes(`${variant}.md`));
    if (!matchesNumber && !matchesLink) return line;
    cells[4] = ` ${status} `;
    return cells.join('|');
  }).join('\n');
}

function setAllStoryTaskStatuses(text, status) {
  return text.split('\n').map((line) => {
    if (!line.trim().startsWith('|')) return line;
    const cells = line.split('|');
    if (cells.length < 6) return line;
    if (!/^\d+$/.test(cells[1].trim())) return line;
    cells[4] = ` ${status} `;
    return cells.join('|');
  }).join('\n');
}

function doneCriteria(text) {
  const match = text.match(/## Done Criteria\n([\s\S]*?)(?:\n---|\n## |\n> \[|$)/);
  if (!match) return [];
  return match[1].split('\n')
    .filter((line) => /^\s*-\s+\[[ xX]\]/.test(line))
    .map((line) => ({
      checked: /^\s*-\s+\[[xX]\]/.test(line),
      text: line.replace(/^\s*-\s+\[[ xX]\]\s*/, '').trim(),
    }));
}

function taskFiles(storyDir) {
  if (!storyDir) return [];
  return readdirSync(storyDir)
    .filter((name) => /^task-\d+-.+\.md$/.test(name))
    .sort()
    .map((name) => path.join(storyDir, name));
}

function updateExpansionStatus(expansionFile, id, status) {
  if (!existsSync(expansionFile)) return false;
  const number = storyNumber(id);
  const text = read(expansionFile);
  const updated = text.split('\n').map((line) => {
    if (!line.trim().startsWith('|')) return line;
    const cells = line.split('|');
    if (cells.length < 8) return line;
    if (String(Number(cells[1].trim())) !== number) return line;
    cells[7] = ` ${status} `;
    return cells.join('|');
  }).join('\n');
  if (updated !== text) write(expansionFile, updated);
  return updated !== text;
}

function expansionRows(expansionFile) {
  if (!existsSync(expansionFile)) return [];
  return read(expansionFile).split('\n')
    .filter((line) => line.trim().startsWith('|'))
    .map((line) => line.split('|').map((cell) => cell.trim()))
    .filter((cells) => /^\d+$/.test(cells[1] || ''))
    .map((cells) => ({
      number: cells[1],
      id: `story-${cells[1].padStart(2, '0')}`,
      dependsOn: cells[4] || '',
      status: cells[7] || '',
    }));
}

function chooseNextStory(expansionFile) {
  const rows = expansionRows(expansionFile);
  const done = new Set(rows.filter((row) => ['DONE', 'SKIPPED'].includes(row.status)).map((row) => String(Number(row.number))));
  return rows.find((row) => {
    if (row.status !== 'TODO') return false;
    if (!row.dependsOn || row.dependsOn === '—' || row.dependsOn === '-') return true;
    return row.dependsOn.split(',').map((item) => item.trim()).filter(Boolean).every((dependency) => done.has(String(Number(dependency))));
  }) || null;
}

function configValue(key, fallback) {
  const config = path.join(cwd, '.planning', 'config.yml');
  if (!existsSync(config)) return fallback;
  const text = read(config);
  if (key === 'base_branch') {
    const match = text.match(/base_branch:\s*([^\s#]+)/);
    return match ? match[1].trim() : fallback;
  }
  if (key === 'requires_git') {
    const match = text.match(/requires_git:\s*(true|false)/);
    return match ? match[1].trim() : fallback;
  }
  return fallback;
}

function printGitPlan(storyFile, storyDir) {
  const storySlug = path.basename(storyFile, '.md');
  const baseBranch = configValue('base_branch', 'main');
  const requiresGit = configValue('requires_git', 'true');
  const taskBranches = taskFiles(storyDir).map((file) => `${storySlug}--${path.basename(file, '.md')}`);
  const report = {
    ok: true,
    mode: 'git-plan',
    storyBranch: storySlug,
    baseBranch,
    requiresGit,
    taskBranches,
    commands: [
      `git checkout ${storySlug}`,
      `gh pr list --base ${storySlug} --state open`,
      ...taskBranches.map((branch) => `git branch -d ${branch}`),
      'git fetch origin',
      `git pull --ff-only origin ${storySlug}`,
      `git rebase origin/${baseBranch}`,
      `git push -u origin ${storySlug}`,
      `gh pr create --title "${storyId}: ${storySlug.replace(/^story-\d+-?/, '')}" --body "Closes story ${storyId} of planning ${planningId}." --base ${baseBranch} --head ${storySlug}`,
      `git checkout ${baseBranch}`,
      `git pull --ff-only origin ${baseBranch}`,
      `git branch -d ${storySlug}`,
    ],
  };

  if (format === 'json') {
    console.log(JSON.stringify(report, null, 2));
    return;
  }
  console.log('# Planning Done Git Plan\n');
  console.log(`Story branch: \`${storySlug}\``);
  console.log(`Base branch: \`${baseBranch}\``);
  console.log(`Git required: \`${requiresGit}\`\n`);
  if (requiresGit === 'false') {
    console.log('Git finalization is disabled by `.planning/config.yml`.');
    return;
  }
  console.log('Commands/checks:\n');
  for (const command of report.commands) console.log(`- \`${command}\``);
  console.log('\nUse `git branch -d` only. Do not force-delete local or remote branches.');
  console.log('Task branches use a sibling `--task-...` suffix, for example `<story-branch>--task-NN-<slug>`, so Git can keep the story branch ref at the same time.');
  console.log('For child planning worktrees, prepend the worktree prefix to the story branch name; task branches then append `--task-...` to that prefixed story branch.');
}

function print(report) {
  if (format === 'json') {
    console.log(JSON.stringify(report, null, 2));
    return;
  }
  console.log(`# Planning Done\n`);
  console.log(`Planning: \`${planningId}\``);
  console.log(`Story: \`${storyId}\``);
  console.log(`Mode: ${report.mode}\n`);
  if (report.files?.length) {
    console.log(`Files updated:`);
    for (const file of report.files) console.log(`- \`${file}\``);
    console.log('');
  }
  if (report.doneCriteria?.length) {
    console.log(`Done criteria:`);
    for (const item of report.doneCriteria) console.log(`- ${item.checked ? '[x]' : '[ ]'} ${item.text}`);
    console.log('');
  }
  if (report.taskStatuses?.length) {
    console.log(`Task file statuses:`);
    for (const item of report.taskStatuses) console.log(`- \`${item.file}\`: ${item.status}`);
    console.log('');
  }
  if (report.nextStory) console.log(`Next story: \`${report.nextStory}\``);
  if (report.warnings?.length) {
    console.log(`Warnings:`);
    for (const warning of report.warnings) console.log(`- ${warning}`);
  }
}

const planningDir = path.join(cwd, '.planning', 'active', planningId);
if (!existsSync(planningDir)) fail(`Planning not found in active state: ${planningId}`, { checked: rel(path.join(cwd, '.planning', 'active')) });

const storyFile = findStoryFile(planningDir, storyId);
const storyDir = findStoryDir(storyFile);
const expansionFile = path.join(planningDir, '01-expansion.md');
const updatedFiles = [];
const warnings = [];

if (gitPlan) {
  printGitPlan(storyFile, storyDir);
  process.exit(0);
}

if (taskId) {
  const taskFile = findTaskFile(storyDir, taskId);
  if (!taskFile) fail(`Task not found: ${taskId}`, { checked: storyDir ? rel(storyDir) : rel(storyFile) });
  const taskText = read(taskFile);
  const updatedTask = setStatusLine(taskText, 'DONE');
  if (updatedTask !== taskText) {
    write(taskFile, updatedTask);
    updatedFiles.push(rel(taskFile));
  }
  const storyText = read(storyFile);
  const updatedStory = setStoryTaskStatus(storyText, taskId, 'DONE');
  if (updatedStory !== storyText) {
    write(storyFile, updatedStory);
    updatedFiles.push(rel(storyFile));
  }
  print({
    ok: true,
    mode: 'task',
    task: taskId,
    files: updatedFiles,
  });
  process.exit(0);
}

const storyText = read(storyFile);
const statuses = taskFiles(storyDir).map((file) => ({ file: rel(file), status: getStatus(read(file)) || 'UNKNOWN' }));
const criteria = doneCriteria(storyText);

if (!finalizeStory) {
  print({
    ok: true,
    mode: 'review-story',
    storyFile: rel(storyFile),
    doneCriteria: criteria,
    taskStatuses: statuses,
    warnings: statuses.filter((item) => item.status !== 'DONE').map((item) => `${item.file} is ${item.status}, not DONE`),
  });
  process.exit(0);
}

const incomplete = statuses.filter((item) => item.status !== 'DONE');
if (incomplete.length > 0) {
  fail('Cannot finalize story while task files are not DONE.', { incomplete });
}

let finalStory = setStatusLine(storyText, 'DONE');
finalStory = setAllStoryTaskStatuses(finalStory, 'DONE');
if (finalStory !== storyText) {
  write(storyFile, finalStory);
  updatedFiles.push(rel(storyFile));
}
if (updateExpansionStatus(expansionFile, storyId, 'DONE')) updatedFiles.push(rel(expansionFile));
const next = chooseNextStory(expansionFile);
if (next && updateExpansionStatus(expansionFile, next.id, 'IN PROGRESS') && !updatedFiles.includes(rel(expansionFile))) updatedFiles.push(rel(expansionFile));

print({
  ok: true,
  mode: 'finalize-story',
  files: updatedFiles,
  nextStory: next ? next.id : null,
  doneCriteria: doneCriteria(finalStory),
});
