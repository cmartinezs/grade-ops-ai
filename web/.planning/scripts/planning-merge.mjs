#!/usr/bin/env node
import {
  existsSync,
  mkdirSync,
  readdirSync,
  readFileSync,
  renameSync,
  statSync,
  writeFileSync,
} from 'node:fs';
import path from 'node:path';

const cwd = process.cwd();
const args = process.argv.slice(2);

let sourceId = '';
let storyId = '';
let targetId = '';
let dryRun = false;
let format = 'markdown';
let date = new Date().toISOString().slice(0, 10);

function usage() {
  return `Usage:
  node .planning/scripts/planning-merge.mjs <source-id> <story-NN> <target-id> [--dry-run] [--date YYYY-MM-DD] [--format markdown|json]

Moves a non-DONE story between active plannings, preserving any atomized task
folder and updating expansion indexes and retrospective raw logs.`;
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
  } else if (!sourceId) sourceId = arg;
  else if (!storyId) storyId = arg;
  else if (!targetId) targetId = arg;
  else fail(`Unexpected argument: ${arg}`, { help: usage() });
}

if (!['markdown', 'json'].includes(format)) fail(`Invalid --format: ${format}`, { help: usage() });
if (!/^\d{4}-\d{2}-\d{2}$/.test(date)) fail(`Invalid --date: ${date}`, { help: usage() });
if (!sourceId || !storyId || !targetId) fail('Missing <source-id> <story-id> <target-id>.', { help: usage() });
if (sourceId === targetId) fail('Source and target planning IDs must be different.');

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

function movePath(source, target, touched) {
  touched.push(rel(source));
  touched.push(rel(target));
  if (!dryRun) {
    mkdirSync(path.dirname(target), { recursive: true });
    renameSync(source, target);
  }
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

function nextStoryId(planningDir) {
  const deepening = path.join(planningDir, '02-deepening');
  if (!existsSync(deepening)) return 'story-01';
  const max = readdirSync(deepening)
    .map((name) => {
      const match = name.match(/^story-(\d+)-.+\.md$/);
      return match ? Number(match[1]) : 0;
    })
    .reduce((highest, value) => Math.max(highest, value), 0);
  return `story-${String(max + 1).padStart(2, '0')}`;
}

function replaceStoryRefs(text, oldId, newId, oldBase, newBase) {
  const oldNumber = String(storyNumber(oldId)).padStart(2, '0');
  const newNumber = String(storyNumber(newId)).padStart(2, '0');
  return text
    .replaceAll(oldBase, newBase)
    .replace(new RegExp(`\\b${oldId}\\b`, 'g'), newId)
    .replace(new RegExp(`Story ${oldNumber}\\b`, 'g'), `Story ${newNumber}`);
}

function updateMarkdownFilesInDir(dir, oldId, newId, oldBase, newBase, touched) {
  for (const file of listFilesRecursive(dir).filter((item) => item.endsWith('.md'))) {
    const text = read(file);
    const updated = replaceStoryRefs(text, oldId, newId, oldBase, newBase);
    if (updated !== text) write(file, updated, touched);
  }
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

function removeStoryRow(expansionFile, oldId, oldBase, touched) {
  if (!existsSync(expansionFile)) fail(`Missing expansion file: ${rel(expansionFile)}`);
  const oldNum = storyNumber(oldId);
  const text = read(expansionFile);
  const bounds = storySummaryBounds(text);
  if (!bounds) fail(`Missing Story Summary section: ${rel(expansionFile)}`);
  let removed = null;
  const section = text.slice(bounds.start, bounds.end);
  const updatedSection = section.split('\n').filter((line) => {
    const cells = parseStoryRow(line);
    if (!cells) return true;
    const matches = Number(cells[1].trim()) === oldNum || cells[2].includes(oldId) || cells[2].includes(oldBase);
    if (!matches) return true;
    removed = cells;
    return false;
  }).join('\n');
  if (!removed) fail(`Story row not found in source expansion: ${oldId}`, { file: rel(expansionFile) });
  const updated = `${text.slice(0, bounds.start)}${updatedSection}${text.slice(bounds.end)}`;
  write(expansionFile, updated, touched);
  return { row: removed, updatedText: updated };
}

function appendStoryRow(expansionFile, rowCells, newId, oldId, oldBase, newBase, touched) {
  if (!existsSync(expansionFile)) fail(`Missing expansion file: ${rel(expansionFile)}`);
  const text = read(expansionFile);
  const bounds = storySummaryBounds(text);
  if (!bounds) fail(`Missing Story Summary section: ${rel(expansionFile)}`);
  const newNumber = String(storyNumber(newId)).padStart(2, '0');
  const cells = [...rowCells];
  cells[1] = ` ${newNumber} `;
  cells[2] = ` ${replaceStoryRefs(cells[2].trim(), oldId, newId, oldBase, newBase)} `;
  const row = cells.join('|');
  const sectionLines = text.slice(bounds.start, bounds.end).split('\n');
  const lastRowIndex = sectionLines.reduce((last, line, index) => (parseStoryRow(line) ? index : last), -1);
  if (lastRowIndex < 0) fail(`Story Summary table has no story rows: ${rel(expansionFile)}`);
  sectionLines.splice(lastRowIndex + 1, 0, row);
  const updated = `${text.slice(0, bounds.start)}${sectionLines.join('\n')}${text.slice(bounds.end)}`;
  write(expansionFile, updated, touched);
}

function expansionRowsFromText(text) {
  const bounds = storySummaryBounds(text);
  if (!bounds) return [];
  return text.slice(bounds.start, bounds.end).split('\n')
    .map(parseStoryRow)
    .filter(Boolean)
    .map((cells) => ({
      number: Number(cells[1].trim()),
      dependsOn: cells[4].trim(),
      status: cells[7].trim(),
    }));
}

function expansionRows(expansionFile) {
  if (!existsSync(expansionFile)) return [];
  return expansionRowsFromText(read(expansionFile));
}

function allRemainingDoneOrSkipped(expansionFile) {
  const rows = expansionRows(expansionFile);
  return rows.length > 0 && rows.every((row) => ['DONE', 'SKIPPED'].includes(row.status));
}

function removeReadmeStory(readmeFile, oldId, oldBase, touched) {
  if (!existsSync(readmeFile)) return false;
  const text = read(readmeFile);
  const updated = text.split('\n').filter((line) => !(line.includes(oldId) || line.includes(oldBase))).join('\n');
  if (updated === text) return false;
  write(readmeFile, updated, touched);
  return true;
}

function addReadmeStory(readmeFile, newId, newBase, touched) {
  if (!existsSync(readmeFile)) return false;
  const entry = `- [${newBase}](02-deepening/${newBase}.md)`;
  const text = read(readmeFile);
  if (text.includes(entry)) return false;
  const heading = '## Story Index';
  let updated = text;
  if (updated.includes(heading)) {
    const start = updated.indexOf(heading) + heading.length;
    const next = updated.slice(start).search(/\n## /);
    const end = next < 0 ? updated.length : start + next;
    const before = updated.slice(0, start);
    const section = updated.slice(start, end).replace(/\n\*\(none\)\*\n?/, '\n');
    const after = updated.slice(end);
    updated = `${before}${section.trimEnd()}\n\n${entry}\n${after}`;
  } else {
    const currentState = updated.indexOf('\n## Current State');
    const insert = `\n## Story Index\n\n${entry}\n`;
    updated = currentState >= 0
      ? `${updated.slice(0, currentState)}${insert}${updated.slice(currentState)}`
      : `${updated.trimEnd()}${insert}\n`;
  }
  write(readmeFile, updated, touched);
  return true;
}

function appendRetrospectiveEntry(file, role, oldId, newId, oldBase, newBase, taskFolderMoved, touched) {
  const entry = `### ${date} - Story moved by plan-merge

- **Source:** plan-merge
- **Related story/task:** ${oldId} -> ${newId}
- **What happened:** ${oldBase} was moved ${role === 'source' ? `from ${sourceId} to ${targetId}` : `into ${targetId} from ${sourceId}`}.
- **Expected instead:** Story ownership changed after planning had already been expanded.
- **Resolution:** ${taskFolderMoved ? 'The story file and task folder were' : 'The story file was'} moved as ${newBase}.
- **Retrospective signal:** review Depends On fields and cross-planning dependencies manually after the move.
`;
  let text = existsSync(file)
    ? read(file)
    : `# Retrospective Raw Notes: ${role === 'source' ? sourceId : targetId}\n\n## Log\n\n*(No unexpected events recorded yet.)*\n`;
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
const sourceDir = activePlanningDir(sourceId);
const targetDir = activePlanningDir(targetId);
if (!sourceDir) fail(`Source planning is not active or does not exist: ${sourceId}`, { checked: rel(path.join(planningRoot, 'active', sourceId)) });
if (!targetDir) fail(`Target planning is not active or does not exist: ${targetId}`, { checked: rel(path.join(planningRoot, 'active', targetId)) });

const sourceStoryFile = findStoryFile(sourceDir, normalizedStoryId);
if (!sourceStoryFile) fail(`Story not found in source planning: ${normalizedStoryId}`);

const oldBase = path.basename(sourceStoryFile, '.md');
const oldSuffix = oldBase.replace(/^story-\d+/, '');
const sourceStoryDir = path.join(path.dirname(sourceStoryFile), oldBase);
const storyText = read(sourceStoryFile);
const status = storyStatus(storyText);
if (status === 'DONE') fail("Cannot move a DONE story; it is already part of the source planning's history.", { story: normalizedStoryId });

const newStoryId = nextStoryId(targetDir);
const newBase = `${newStoryId}${oldSuffix}`;
const targetDeepening = path.join(targetDir, '02-deepening');
const targetStoryFile = path.join(targetDeepening, `${newBase}.md`);
const targetStoryDir = path.join(targetDeepening, newBase);
if (existsSync(targetStoryFile) || existsSync(targetStoryDir)) fail(`Target story already exists: ${newBase}`);

const touched = [];
const followUps = [];
const sourceExpansion = path.join(sourceDir, '01-expansion.md');
const targetExpansion = path.join(targetDir, '01-expansion.md');
const sourceRemoval = removeStoryRow(sourceExpansion, normalizedStoryId, oldBase, touched);
const removedRow = sourceRemoval.row;
appendStoryRow(targetExpansion, removedRow, newStoryId, normalizedStoryId, oldBase, newBase, touched);

const sourceRowsAfterMove = expansionRowsFromText(sourceRemoval.updatedText);
const blockedByMovedStory = sourceRowsAfterMove
  .filter((row) => row.dependsOn.split(',').map((part) => part.trim()).includes(String(storyNumber(normalizedStoryId)).padStart(2, '0')))
  .map((row) => `story-${String(row.number).padStart(2, '0')}`);
if (removedRow[4] && !['—', '-'].includes(removedRow[4].trim())) followUps.push(`Review moved story dependencies: ${removedRow[4].trim()}`);
if (blockedByMovedStory.length > 0) followUps.push(`Review source stories depending on the moved story: ${blockedByMovedStory.join(', ')}`);

const updatedStoryText = replaceStoryRefs(storyText, normalizedStoryId, newStoryId, oldBase, newBase);
write(sourceStoryFile, updatedStoryText, touched);
movePath(sourceStoryFile, targetStoryFile, touched);

let taskFolderMoved = false;
if (existsSync(sourceStoryDir)) {
  const movedFiles = listFilesRecursive(sourceStoryDir);
  for (const file of movedFiles) {
    touched.push(rel(file));
    touched.push(rel(path.join(targetStoryDir, path.relative(sourceStoryDir, file))));
  }
  movePath(sourceStoryDir, targetStoryDir, touched);
  updateMarkdownFilesInDir(targetStoryDir, normalizedStoryId, newStoryId, oldBase, newBase, touched);
  taskFolderMoved = true;
}

const sourceReadmeUpdated = removeReadmeStory(path.join(sourceDir, 'README.md'), normalizedStoryId, oldBase, touched);
const targetReadmeUpdated = addReadmeStory(path.join(targetDir, 'README.md'), newStoryId, newBase, touched);

appendRetrospectiveEntry(path.join(sourceDir, 'RETROSPECTIVE-RAW.md'), 'source', normalizedStoryId, newStoryId, oldBase, newBase, taskFolderMoved, touched);
appendRetrospectiveEntry(path.join(targetDir, 'RETROSPECTIVE-RAW.md'), 'target', normalizedStoryId, newStoryId, oldBase, newBase, taskFolderMoved, touched);

const suggestArchiveSource = sourceRowsAfterMove.length > 0 && sourceRowsAfterMove.every((row) => ['DONE', 'SKIPPED'].includes(row.status));

const report = {
  ok: true,
  dryRun,
  sourceId,
  targetId,
  oldStoryId: normalizedStoryId,
  newStoryId,
  oldStoryPath: rel(sourceStoryFile),
  newStoryPath: rel(targetStoryFile),
  taskFolderMoved,
  sourceReadmeUpdated,
  targetReadmeUpdated,
  suggestArchiveSource,
  followUps,
  touched: [...new Set(touched)].sort(),
};

if (format === 'json') {
  console.log(JSON.stringify(report, null, 2));
} else {
  console.log('# Planning Merge\n');
  if (dryRun) console.log('Dry run only. No files were changed.\n');
  console.log(`Source: \`${sourceId}/${normalizedStoryId}\``);
  console.log(`Target: \`${targetId}/${newStoryId}\``);
  console.log(`Story file: \`${report.oldStoryPath}\` -> \`${report.newStoryPath}\``);
  console.log(`Task folder moved: \`${taskFolderMoved ? 'yes' : 'no'}\``);
  console.log(`Source ready to archive: \`${suggestArchiveSource ? 'yes' : 'no'}\`\n`);
  console.log('Touched paths:');
  for (const file of report.touched) console.log(`- \`${file}\``);
  if (followUps.length > 0) {
    console.log('\nFollow-ups:');
    for (const item of followUps) console.log(`- ${item}`);
  }
  console.log('\nReview Depends On fields manually after the move.');
}
