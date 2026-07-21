#!/usr/bin/env node
import { existsSync, readdirSync, readFileSync, statSync, writeFileSync } from 'node:fs';
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
  node .planning/scripts/planning-story-skip.mjs <planning-id> <story-NN> [-- reason] [--reason text] [--dry-run] [--date YYYY-MM-DD] [--format markdown|json]

Marks a non-DONE story as SKIPPED, updates indexes, records the reason, and
reports whether the planning is ready for retrospective/archive.`;
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
  if (arg === '--') {
    reason = args.slice(i + 1).join(' ').trim();
    break;
  } else if (arg === '--dry-run') dryRun = true;
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
  else reason = [reason, arg].filter(Boolean).join(' ');
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

function activePlanningDir(id) {
  const dir = path.join(planningRoot, 'active', id);
  return existsSync(dir) && statSync(dir).isDirectory() ? dir : '';
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

function storyIdFromNumber(number) {
  return `story-${String(Number(number)).padStart(2, '0')}`;
}

function storyStatus(text) {
  const match = text.match(/^>\s*\*\*Status:\*\*\s*(.+)$/m);
  return match ? match[1].trim() : '';
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

function markStorySkipped(text, reasonText) {
  let output = text.replace(/^>\s*\*\*Status:\*\*.*$/m, '> **Status:** SKIPPED');
  const line = `> **Skipped reason:** ${reasonText} (${date})`;
  if (/^>\s*\*\*Skipped reason:\*\*.*$/m.test(output)) {
    output = output.replace(/^>\s*\*\*Skipped reason:\*\*.*$/m, line);
  } else {
    output = output.replace(/^>\s*\*\*Status:\*\*.*$/m, (match) => `${match}\n${line}`);
  }
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

function mapStorySummaryRows(text, updater) {
  let inFence = false;
  return text.split('\n').map((line) => {
    if (line.trim().startsWith('```')) {
      inFence = !inFence;
      return line;
    }
    if (inFence) return line;
    return updater(line);
  }).join('\n');
}

function storySummaryRows(text) {
  let inFence = false;
  const rows = [];
  for (const line of text.split('\n')) {
    if (line.trim().startsWith('```')) {
      inFence = !inFence;
      continue;
    }
    if (inFence) continue;
    const row = parseStoryRow(line);
    if (row) rows.push(row);
  }
  return rows;
}

function updateExpansionStatus(expansionFile, id, status, touched) {
  if (!existsSync(expansionFile)) fail(`Missing expansion file: ${rel(expansionFile)}`);
  const text = read(expansionFile);
  const bounds = storySummaryBounds(text);
  if (!bounds) fail(`Missing Story Summary section: ${rel(expansionFile)}`);
  const number = storyNumber(id);
  let changed = false;
  const section = mapStorySummaryRows(text.slice(bounds.start, bounds.end), (line) => {
    const cells = parseStoryRow(line);
    if (!cells) return line;
    if (Number(cells[1].trim()) !== number) return line;
    cells[7] = ` ${status} `;
    changed = true;
    return cells.join('|');
  });
  if (!changed) fail(`Story row not found in expansion: ${id}`, { file: rel(expansionFile) });
  write(expansionFile, `${text.slice(0, bounds.start)}${section}${text.slice(bounds.end)}`, touched);
}

function expansionRows(expansionFile) {
  if (!existsSync(expansionFile)) return [];
  const text = read(expansionFile);
  const bounds = storySummaryBounds(text);
  if (!bounds) return [];
  return storySummaryRows(text.slice(bounds.start, bounds.end))
    .map((cells) => ({
      id: storyIdFromNumber(cells[1].trim()),
      status: cells[7].trim(),
    }));
}

function updatePlanningReadme(readmeFile, storyBase, touched) {
  if (!existsSync(readmeFile)) return false;
  const text = read(readmeFile);
  const updated = text.split('\n').map((line) => {
    if (!line.includes(storyBase)) return line;
    return line.replace(/\[(TODO|IN PROGRESS|BLOCKED|STANDBY)\]/g, '[SKIPPED]');
  }).join('\n');
  if (updated === text) return false;
  write(readmeFile, updated, touched);
  return true;
}

function appendRetrospectiveEntry(file, story, storyBase, reasonText, touched) {
  const entry = `### ${date} - Story skipped

- **Source:** plan-story-skip
- **Related story/task:** ${story}
- **What happened:** ${storyBase} was marked SKIPPED.
- **Expected instead:** The story would have remained executable if still applicable.
- **Resolution:** Story status was set to SKIPPED. Reason: ${reasonText}.
- **Retrospective signal:** check whether this skip implies a follow-up planning or a PDR-worthy scope decision.
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
const storyText = read(storyFile);
const status = storyStatus(storyText);
if (status === 'DONE') fail('Story is already DONE; use /plan-rollback first if you want to re-evaluate it.', { story: normalizedStoryId });
if (status === 'SKIPPED') fail('Story is already SKIPPED.', { story: normalizedStoryId });

const reasonText = reason.trim() || 'not provided';
const touched = [];
write(storyFile, markStorySkipped(storyText, reasonText), touched);
const expansionFile = path.join(planningDir, '01-expansion.md');
updateExpansionStatus(expansionFile, normalizedStoryId, 'SKIPPED', touched);
const planningReadmeUpdated = updatePlanningReadme(path.join(planningDir, 'README.md'), storyBase, touched);
appendRetrospectiveEntry(path.join(planningDir, 'RETROSPECTIVE-RAW.md'), normalizedStoryId, storyBase, reasonText, touched);

const rows = expansionRows(expansionFile).map((row) => (
  row.id === normalizedStoryId ? { ...row, status: 'SKIPPED' } : row
));
const remainingOpen = rows
  .filter((row) => !['DONE', 'SKIPPED'].includes(row.status))
  .map((row) => ({ story: row.id, status: row.status }));
const readyForCloseout = rows.length > 0 && remainingOpen.length === 0;

const report = {
  ok: true,
  dryRun,
  planningId,
  storyId: normalizedStoryId,
  storyPath: rel(storyFile),
  reason: reasonText,
  planningReadmeUpdated,
  readyForCloseout,
  remainingOpen,
  suggestedCommands: readyForCloseout
    ? [`/plan-retrospective ${planningId}`, `/plan-archive ${planningId}`]
    : [],
  touched: [...new Set(touched)].sort(),
};

if (format === 'json') {
  console.log(JSON.stringify(report, null, 2));
} else {
  console.log('# Planning Story Skip\n');
  if (dryRun) console.log('Dry run only. No files were changed.\n');
  console.log(`Planning: \`${planningId}\``);
  console.log(`Story: \`${normalizedStoryId}\``);
  console.log(`Reason: ${reasonText}`);
  console.log(`Ready for closeout: \`${readyForCloseout ? 'yes' : 'no'}\`\n`);
  if (remainingOpen.length > 0) {
    console.log('Remaining non-DONE/SKIPPED stories:');
    for (const row of remainingOpen) console.log(`- \`${row.story}\` ${row.status}`);
    console.log('');
  }
  if (readyForCloseout) {
    console.log('Suggested next commands:');
    for (const command of report.suggestedCommands) console.log(`- \`${command}\``);
    console.log('');
  }
  console.log('Touched paths:');
  for (const file of report.touched) console.log(`- \`${file}\``);
}
