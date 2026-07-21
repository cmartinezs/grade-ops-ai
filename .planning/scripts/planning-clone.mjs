#!/usr/bin/env node
import {
  copyFileSync,
  existsSync,
  mkdirSync,
  readdirSync,
  readFileSync,
  rmSync,
  statSync,
  writeFileSync,
} from 'node:fs';
import path from 'node:path';

const cwd = process.cwd();
const args = process.argv.slice(2);

let sourceId = '';
let targetId = '';
let dryRun = false;
let format = 'markdown';
let date = new Date().toISOString().slice(0, 10);

function usage() {
  return `Usage:
  node .planning/scripts/planning-clone.mjs <source-id> <target-id> [--dry-run] [--date YYYY-MM-DD] [--format markdown|json]

Clones an existing planning from .planning/, .planning/active/, or .planning/finished/
into a fresh INITIAL-state planning under .planning/<target-id>/.`;
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
  else if (!targetId) targetId = arg;
  else fail(`Unexpected argument: ${arg}`, { help: usage() });
}

if (!['markdown', 'json'].includes(format)) fail(`Invalid --format: ${format}`, { help: usage() });
if (!/^\d{4}-\d{2}-\d{2}$/.test(date)) fail(`Invalid --date: ${date}`, { help: usage() });
if (!sourceId || !targetId) fail('Missing <source-id> <target-id>.', { help: usage() });
if (sourceId === targetId) fail('Source and target IDs must be different.');

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

function copyFile(source, target, touched) {
  touched.push(rel(target));
  if (!dryRun) {
    mkdirSync(path.dirname(target), { recursive: true });
    copyFileSync(source, target);
  }
}

function copyDir(source, target, touched) {
  touched.push(rel(target));
  if (!dryRun) mkdirSync(target, { recursive: true });
  for (const entry of readdirSync(source, { withFileTypes: true })) {
    const sourcePath = path.join(source, entry.name);
    const targetPath = path.join(target, entry.name);
    if (entry.isDirectory()) copyDir(sourcePath, targetPath, touched);
    else copyFile(sourcePath, targetPath, touched);
  }
}

function removePath(file, touched) {
  touched.push(rel(file));
  if (!dryRun && existsSync(file)) rmSync(file, { recursive: true, force: true });
}

function locatePlanning(id) {
  const candidates = [
    path.join(planningRoot, id),
    path.join(planningRoot, 'active', id),
    path.join(planningRoot, 'finished', id),
  ];
  return candidates.find((candidate) => existsSync(candidate) && statSync(candidate).isDirectory()) || '';
}

function targetExists(id) {
  return [
    path.join(planningRoot, id),
    path.join(planningRoot, 'active', id),
    path.join(planningRoot, 'finished', id),
  ].some((candidate) => existsSync(candidate));
}

function replaceInitiatorDate(text) {
  const marker = '## Initiator';
  const start = text.indexOf(marker);
  if (start < 0) return text;
  const next = text.slice(start + marker.length).search(/\n## |\n---/);
  const end = next < 0 ? text.length : start + marker.length + next;
  const before = text.slice(0, start);
  const section = text.slice(start, end).replace(/^-\s+\*\*Date:\*\*.*$/m, `- **Date:** ${date}`);
  const after = text.slice(end);
  return `${before}${section}${after}`;
}

function resetInitial(text) {
  let output = text;
  output = output.replace(/^#.*$/m, `# 🌱 INITIAL: ${targetId}`);
  output = output.replace(/^>\s*\*\*Status:\*\*.*$/m, '> **Status:** Initial');
  output = replaceInitiatorDate(output);
  output = output.replace(
    /-\s+\*\*Related planning \(if continuation\):\*\*.*$/m,
    `- **Related planning (if continuation):** ${sourceId}`,
  );

  const lines = output.split('\n');
  const headingIndex = lines.findIndex((line) => line.startsWith('# '));
  const note = `> Cloned from: ${sourceId}`;
  const existingNoteIndex = lines.findIndex((line) => line.startsWith('> Cloned from:'));
  if (existingNoteIndex >= 0) lines[existingNoteIndex] = note;
  else if (headingIndex >= 0) lines.splice(headingIndex + 1, 0, '', note);
  return lines.join('\n');
}

function updateSection(text, heading, updater) {
  const start = text.indexOf(heading);
  if (start < 0) return text;
  const afterHeading = start + heading.length;
  const next = text.slice(afterHeading).search(/\n## /);
  const end = next < 0 ? text.length : afterHeading + next;
  return `${text.slice(0, start)}${updater(text.slice(start, end))}${text.slice(end)}`;
}

function resetTableStatusRows(section, status) {
  return section.split('\n').map((line) => {
    if (!line.trim().startsWith('|')) return line;
    const cells = line.split('|');
    if (cells.length < 4) return line;
    if (!/^\d+$/.test(cells[1].trim())) return line;
    cells[cells.length - 2] = ` ${status} `;
    return cells.join('|');
  }).join('\n');
}

function resetTaskRows(section, status) {
  return section.split('\n').map((line) => {
    if (!line.trim().startsWith('|')) return line;
    const cells = line.split('|');
    if (cells.length < 6) return line;
    if (!/^\d+$/.test(cells[1].trim())) return line;
    cells[4] = ` ${status} `;
    return cells.join('|');
  }).join('\n');
}

function resetExpansion(text) {
  let output = text.replace(/^#.*$/m, `# 🚀 EXPANSION: ${targetId}`);
  output = updateSection(output, '## Story Summary', (section) => resetTableStatusRows(section, 'TODO'));
  output = updateSection(output, '## Linked Child Plannings', (section) => section.split('\n').map((line) => {
    if (!line.trim().startsWith('|')) return line;
    const cells = line.split('|');
    if (cells.length < 7) return line;
    const first = cells[1].trim();
    if (!first || first === 'Child Worktree' || first === '----------------' || first === '—') return line;
    cells[cells.length - 2] = ' TODO ';
    return cells.join('|');
  }).join('\n'));
  return output;
}

function resetStory(text) {
  const output = text
    .replace(/^>\s*\*\*Status:\*\*.*$/m, '> **Status:** TODO')
    .replace(/\[[xX]\]/g, '[ ]');
  return updateSection(output, '## Tasks', (section) => resetTaskRows(section, 'TODO'));
}

function extractIntent(initialText) {
  const match = initialText.match(/## Intent\n([\s\S]*?)(?:\n---|\n## |$)/);
  if (!match) return targetId;
  const candidate = match[1].split('\n')
    .map((line) => line.trim())
    .find((line) => line && !line.startsWith('>') && !line.startsWith('['));
  return candidate || targetId;
}

function updateRootReadme(rootReadme, intent, touched) {
  if (!existsSync(rootReadme)) return false;
  const entry = `- [${targetId}](${targetId}/00-initial.md) — ${intent} (cloned from ${sourceId})`;
  const text = read(rootReadme);
  if (text.includes(entry)) return false;
  let updated = text;
  const marker = '### 🆕 Initial';
  const markerIndex = updated.indexOf(marker);
  if (markerIndex < 0) {
    updated = `${updated.trimEnd()}\n\n${marker}\n\n${entry}\n`;
  } else {
    const afterMarker = markerIndex + marker.length;
    const rest = updated.slice(afterMarker);
    const nextSection = rest.search(/\n### /);
    const sectionEnd = nextSection < 0 ? updated.length : afterMarker + nextSection;
    const before = updated.slice(0, afterMarker);
    const section = updated.slice(afterMarker, sectionEnd);
    const after = updated.slice(sectionEnd);
    const cleaned = section.replace(/\n\*\(none yet\)\*\n?/, '\n');
    updated = `${before}${cleaned.trimEnd()}\n\n${entry}\n${after}`;
  }
  write(rootReadme, updated, touched);
  return true;
}

const sourceDir = locatePlanning(sourceId);
if (!sourceDir) fail(`Source planning not found: ${sourceId}`, {
  checked: [
    rel(path.join(planningRoot, sourceId)),
    rel(path.join(planningRoot, 'active', sourceId)),
    rel(path.join(planningRoot, 'finished', sourceId)),
  ],
});
if (targetExists(targetId)) fail('Target ID already exists.', { targetId });

const templateDir = path.join(planningRoot, '_template');
if (!existsSync(templateDir)) fail('Missing .planning/_template/. Re-run /plan-init --force to refresh the planning template.');

const sourceInitial = path.join(sourceDir, '00-initial.md');
if (!existsSync(sourceInitial)) fail(`Source is missing 00-initial.md: ${rel(sourceInitial)}`);

const targetDir = path.join(planningRoot, targetId);
const touched = [];
const copiedStories = [];

copyDir(templateDir, targetDir, touched);

const sourceInitialText = read(sourceInitial);
write(path.join(targetDir, '00-initial.md'), resetInitial(sourceInitialText), touched);

const sourceExpansion = path.join(sourceDir, '01-expansion.md');
if (existsSync(sourceExpansion)) {
  write(path.join(targetDir, '01-expansion.md'), resetExpansion(read(sourceExpansion)), touched);
}

const sourceDeepening = path.join(sourceDir, '02-deepening');
const targetDeepening = path.join(targetDir, '02-deepening');
removePath(path.join(targetDeepening, 'story-NN-name.md'), touched);
removePath(path.join(targetDeepening, 'task-NN-name.md'), touched);
if (existsSync(sourceDeepening)) {
  for (const entry of readdirSync(sourceDeepening, { withFileTypes: true }).filter((item) => item.isFile() && /^story-\d+-.+\.md$/.test(item.name))) {
    copiedStories.push(entry.name);
    write(path.join(targetDeepening, entry.name), resetStory(read(path.join(sourceDeepening, entry.name))), touched);
  }
}

const sourceTraceability = path.join(sourceDir, 'TRACEABILITY.md');
if (existsSync(sourceTraceability)) copyFile(sourceTraceability, path.join(targetDir, 'TRACEABILITY.md'), touched);

const rootReadmeUpdated = updateRootReadme(path.join(planningRoot, 'README.md'), extractIntent(sourceInitialText), touched);

if (dryRun && existsSync(targetDir)) {
  rmSync(targetDir, { recursive: true, force: true });
}

const report = {
  ok: true,
  dryRun,
  sourceId,
  sourcePath: rel(sourceDir),
  targetId,
  targetPath: rel(targetDir),
  copiedStories: copiedStories.length,
  storyFiles: copiedStories,
  rootReadmeUpdated,
  touched: [...new Set(touched)].sort(),
};

if (format === 'json') {
  console.log(JSON.stringify(report, null, 2));
} else {
  console.log('# Planning Clone\n');
  if (dryRun) console.log('Dry run only. No files were changed.\n');
  console.log(`Source: \`${report.sourcePath}\``);
  console.log(`Target: \`${report.targetPath}\``);
  console.log(`Stories copied: \`${copiedStories.length}\``);
  console.log(`Root index updated: \`${rootReadmeUpdated ? 'yes' : 'no'}\`\n`);
  console.log('Touched paths:');
  for (const file of report.touched) console.log(`- \`${file}\``);
  console.log('\nNext step: run `/plan-expand ' + targetId + '` if the clone needs re-expansion, or `/plan-story ' + targetId + ' story-01` to start executing directly.');
}
