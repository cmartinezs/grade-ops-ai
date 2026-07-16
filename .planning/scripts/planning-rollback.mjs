#!/usr/bin/env node
import {
  existsSync,
  readdirSync,
  readFileSync,
  rmSync,
  statSync,
  writeFileSync,
} from 'node:fs';
import path from 'node:path';

const cwd = process.cwd();
const args = process.argv.slice(2);

let planningId = '';
let storyId = '';
let dryRun = false;
let format = 'markdown';
let date = new Date().toISOString().slice(0, 10);
let reason = '';

function usage() {
  return `Usage:
  node .planning/scripts/planning-rollback.mjs <planning-id> <story-NN> [--dry-run] [--date YYYY-MM-DD] [--reason text] [--format markdown|json]

Resets a DONE story to TODO, removes its atomized task folder if present, updates
the expansion row, and records the rollback in RETROSPECTIVE-RAW.md.`;
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
  if (arg === '--dry-run') dryRun = true;
  else if (arg === '--format') {
    format = args[i + 1] || '';
    i += 1;
  } else if (arg === '--date') {
    date = args[i + 1] || '';
    i += 1;
  } else if (arg === '--reason') {
    reason = args[i + 1] || '';
    i += 1;
  } else if (!planningId) planningId = arg;
  else if (!storyId) storyId = arg;
  else fail(`Unexpected argument: ${arg}`, { help: usage() });
}

if (!['markdown', 'json'].includes(format)) fail(`Invalid --format: ${format}`, { help: usage() });
if (!/^\d{4}-\d{2}-\d{2}$/.test(date)) fail(`Invalid --date: ${date}`, { help: usage() });
if (!planningId || !storyId) fail('Missing <planning-id> <story-id>.', { help: usage() });

const planningRoot = path.join(cwd, '.planning');
if (!existsSync(planningRoot)) fail('No .planning/ directory found in the current workspace. Run /plan-init first or move to the project root.');

function rel(file) {
  const relative = path.relative(cwd, file);
  return relative || '.';
}

function read(file) {
  return readFileSync(file, 'utf8');
}

function write(file, text, touched) {
  touched.push(rel(file));
  if (!dryRun) writeFileSync(file, text);
}

function normalizeStoryId(id) {
  const match = id.match(/^story-0*(\d+)$/);
  if (!match) fail(`Invalid story id: ${id}`, { help: usage() });
  return `story-${match[1].padStart(2, '0')}`;
}

function storyNumber(id) {
  const match = id.match(/^story-0*(\d+)/);
  return match ? Number(match[1]) : 0;
}

function storyStatus(text) {
  const match = text.match(/^>\s*\*\*Status:\*\*\s*(.+)$/m);
  return match ? match[1].trim() : '';
}

function activePlanningDir(id) {
  const dir = path.join(planningRoot, 'active', id);
  return existsSync(dir) && statSync(dir).isDirectory() ? dir : '';
}

function findStoryFile(planningDir, id) {
  const deepening = path.join(planningDir, '02-deepening');
  if (!existsSync(deepening)) fail(`Missing deepening directory: ${rel(deepening)}`);
  const normalized = normalizeStoryId(id);
  const file = readdirSync(deepening)
    .filter((name) => name.startsWith(`${normalized}-`) && name.endsWith('.md'))
    .sort()[0];
  return file ? path.join(deepening, file) : '';
}

function listFilesRecursive(dir) {
  if (!existsSync(dir)) return [];
  const found = [];
  for (const entry of readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) found.push(...listFilesRecursive(full));
    else found.push(full);
  }
  return found;
}

function removePath(file, touched) {
  if (!existsSync(file)) return false;
  for (const child of listFilesRecursive(file)) touched.push(rel(child));
  touched.push(rel(file));
  if (!dryRun) rmSync(file, { recursive: true, force: true });
  return true;
}

function updateSection(text, heading, updater) {
  const start = text.indexOf(heading);
  if (start < 0) return text;
  const afterHeading = start + heading.length;
  const next = text.slice(afterHeading).search(/\n## /);
  const end = next < 0 ? text.length : afterHeading + next;
  return `${text.slice(0, start)}${updater(text.slice(start, end))}${text.slice(end)}`;
}

function stripMarkdownLink(value) {
  return value.replace(/\[([^\]]+)\]\([^)]+\)/g, '$1');
}

function resetTaskTable(section) {
  return section.split('\n').map((line) => {
    if (!line.trim().startsWith('|')) return line;
    const cells = line.split('|');
    if (cells.length < 6 || !/^\d+$/.test(cells[1].trim())) return line;
    cells[2] = ` ${stripMarkdownLink(cells[2].trim())} `;
    cells[4] = ' TODO ';
    return cells.join('|');
  }).join('\n');
}

function resetStory(text) {
  let output = text
    .replace(/^>\s*\*\*Status:\*\*.*$/m, '> **Status:** TODO')
    .replace(/\[[xX]\]/g, '[ ]');
  output = updateSection(output, '## Tasks', resetTaskTable);
  return output;
}

function storySummaryBounds(text) {
  const heading = '## Story Summary';
  const start = text.indexOf(heading);
  if (start < 0) return null;
  const afterHeading = start + heading.length;
  const next = text.slice(afterHeading).search(/\n## /);
  const end = next < 0 ? text.length : afterHeading + next;
  return { start, end };
}

function parseStoryRow(line) {
  if (!line.trim().startsWith('|')) return null;
  const cells = line.split('|');
  if (cells.length < 8) return null;
  if (!/^\d+$/.test(cells[1].trim())) return null;
  return cells;
}

function updateExpansionStatus(expansionFile, id, status, touched) {
  if (!existsSync(expansionFile)) fail(`Missing expansion file: ${rel(expansionFile)}`);
  const text = read(expansionFile);
  const bounds = storySummaryBounds(text);
  if (!bounds) fail(`Missing Story Summary section: ${rel(expansionFile)}`);
  const number = storyNumber(id);
  let changed = false;
  const section = text.slice(bounds.start, bounds.end).split('\n').map((line) => {
    const cells = parseStoryRow(line);
    if (!cells) return line;
    if (Number(cells[1].trim()) !== number) return line;
    cells[7] = ` ${status} `;
    changed = true;
    return cells.join('|');
  }).join('\n');
  if (!changed) fail(`Story row not found in expansion: ${id}`, { file: rel(expansionFile) });
  write(expansionFile, `${text.slice(0, bounds.start)}${section}${text.slice(bounds.end)}`, touched);
}

function updatePlanningReadme(readmeFile, storyBase, touched) {
  if (!existsSync(readmeFile)) return false;
  const text = read(readmeFile);
  const updated = text.split('\n').map((line) => {
    if (!line.includes(storyBase)) return line;
    return line.replace(/\[(DONE|IN PROGRESS|BLOCKED|STANDBY|SKIPPED)\]/g, '[TODO]');
  }).join('\n');
  if (updated === text) return false;
  write(readmeFile, updated, touched);
  return true;
}

function appendRetrospectiveEntry(file, story, storyBase, taskFolderDeleted, touched) {
  const reasonText = reason || 'Rollback requested because the completed story must be re-run.';
  const entry = `### ${date} - Story rollback

- **Source:** plan-rollback
- **Related story/task:** ${story}
- **What happened:** ${storyBase} was reset from DONE to TODO.
- **Expected instead:** Completed story output should have remained valid.
- **Resolution:** Story state was reset, atomized task folder deletion was ${taskFolderDeleted ? 'performed' : 'not needed'}, and generated code/files were left untouched.
- **Retrospective signal:** ${reasonText}
`;
  let text = existsSync(file)
    ? read(file)
    : `# Retrospective Raw Notes: ${planningId}\n\n## Log\n\n*(No unexpected events recorded yet.)*\n`;
  if (text.includes('*(No unexpected events recorded yet.)*')) {
    text = text.replace('*(No unexpected events recorded yet.)*', entry.trimEnd());
  } else if (text.includes('<!-- Add newest entries at the top. -->')) {
    text = text.replace('<!-- Add newest entries at the top. -->', `<!-- Add newest entries at the top. -->\n\n${entry.trimEnd()}`);
  } else {
    text = `${text.trimEnd()}\n\n${entry.trimEnd()}\n`;
  }
  write(file, text, touched);
}

const normalizedStoryId = normalizeStoryId(storyId);
const planningDir = activePlanningDir(planningId);
if (!planningDir) fail(`Planning is not active or does not exist: ${planningId}`, { checked: rel(path.join(planningRoot, 'active', planningId)) });

const storyFile = findStoryFile(planningDir, normalizedStoryId);
if (!storyFile) fail(`Story not found: ${normalizedStoryId}`);

const storyBase = path.basename(storyFile, '.md');
const taskFolder = path.join(path.dirname(storyFile), storyBase);
const storyText = read(storyFile);
const status = storyStatus(storyText);
if (status !== 'DONE') fail('Story is not DONE; nothing to roll back.', { story: normalizedStoryId, status });

const touched = [];
const taskFolderDeleted = removePath(taskFolder, touched);
write(storyFile, resetStory(storyText), touched);
updateExpansionStatus(path.join(planningDir, '01-expansion.md'), normalizedStoryId, 'TODO', touched);
const planningReadmeUpdated = updatePlanningReadme(path.join(planningDir, 'README.md'), storyBase, touched);
appendRetrospectiveEntry(path.join(planningDir, 'RETROSPECTIVE-RAW.md'), normalizedStoryId, storyBase, taskFolderDeleted, touched);

const report = {
  ok: true,
  dryRun,
  planningId,
  storyId: normalizedStoryId,
  storyPath: rel(storyFile),
  taskFolderPath: rel(taskFolder),
  taskFolderDeleted,
  planningReadmeUpdated,
  generatedCodeReverted: false,
  touched: [...new Set(touched)].sort(),
};

if (format === 'json') {
  console.log(JSON.stringify(report, null, 2));
} else {
  console.log('# Planning Rollback\n');
  if (dryRun) console.log('Dry run only. No files were changed.\n');
  console.log(`Planning: \`${planningId}\``);
  console.log(`Story: \`${normalizedStoryId}\``);
  console.log(`Story file: \`${report.storyPath}\``);
  console.log(`Task folder deleted: \`${taskFolderDeleted ? 'yes' : 'no'}\``);
  console.log('Generated code/files reverted: `no`\n');
  console.log('Touched paths:');
  for (const file of report.touched) console.log(`- \`${file}\``);
  console.log('\nGenerated code/files are not reverted by this command. Revert implementation changes separately.');
}
