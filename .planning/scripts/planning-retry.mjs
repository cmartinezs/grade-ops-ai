#!/usr/bin/env node
import { existsSync, readdirSync, readFileSync, statSync, writeFileSync } from 'node:fs';
import path from 'node:path';

const cwd = process.cwd();
const args = process.argv.slice(2);

let planningId = '';
let dryRun = false;
let format = 'markdown';
let date = new Date().toISOString().slice(0, 10);

function usage() {
  return `Usage:
  node .planning/scripts/planning-retry.mjs <planning-id> [--dry-run] [--date YYYY-MM-DD] [--format markdown|json]

Finds BLOCKED stories whose dependencies are DONE, resets them to TODO, updates
indexes, records retry/skipped notes, and emits the /plan-story commands to run.`;
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
  } else if (!planningId) planningId = arg;
  else fail(`Unexpected argument: ${arg}`, { help: usage() });
}

if (!['markdown', 'json'].includes(format)) fail(`Invalid --format: ${format}`, { help: usage() });
if (!/^\d{4}-\d{2}-\d{2}$/.test(date)) fail(`Invalid --date: ${date}`, { help: usage() });
if (!planningId) fail('Missing <planning-id>.', { help: usage() });

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

function setStatusLine(text, status) {
  return /^>\s*\*\*Status:\*\*.*$/m.test(text)
    ? text.replace(/^>\s*\*\*Status:\*\*.*$/m, `> **Status:** ${status}`)
    : text;
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

function expansionRows(expansionFile) {
  if (!existsSync(expansionFile)) fail(`Missing expansion file: ${rel(expansionFile)}`);
  const text = read(expansionFile);
  const bounds = storySummaryBounds(text);
  if (!bounds) fail(`Missing Story Summary section: ${rel(expansionFile)}`);
  return text.slice(bounds.start, bounds.end).split('\n')
    .map(parseStoryRow)
    .filter(Boolean)
    .map((cells) => ({
      id: storyIdFromNumber(cells[1].trim()),
      number: Number(cells[1].trim()),
      dependsOn: cells[4].trim(),
      status: cells[7].trim(),
    }));
}

function dependencyIds(dependsOn) {
  if (!dependsOn || ['—', '-'].includes(dependsOn.trim())) return [];
  return dependsOn.split(',')
    .map((item) => item.trim())
    .filter(Boolean)
    .map((item) => {
      const story = item.match(/^story-0*(\d+)$/);
      if (story) return storyIdFromNumber(story[1]);
      const number = item.match(/^0*(\d+)$/);
      if (number) return storyIdFromNumber(number[1]);
      return item;
    });
}

function updateExpansionStatus(expansionFile, ids, status, touched) {
  if (ids.length === 0) return false;
  const targetNumbers = new Set(ids.map(storyNumber));
  const text = read(expansionFile);
  const bounds = storySummaryBounds(text);
  if (!bounds) fail(`Missing Story Summary section: ${rel(expansionFile)}`);
  let changed = false;
  const section = text.slice(bounds.start, bounds.end).split('\n').map((line) => {
    const cells = parseStoryRow(line);
    if (!cells) return line;
    if (!targetNumbers.has(Number(cells[1].trim()))) return line;
    cells[7] = ` ${status} `;
    changed = true;
    return cells.join('|');
  }).join('\n');
  if (changed) write(expansionFile, `${text.slice(0, bounds.start)}${section}${text.slice(bounds.end)}`, touched);
  return changed;
}

function storyFiles(deepeningDir) {
  if (!existsSync(deepeningDir)) fail(`Missing deepening directory: ${rel(deepeningDir)}`);
  return readdirSync(deepeningDir)
    .filter((name) => /^story-\d+-.+\.md$/.test(name))
    .sort()
    .map((name) => path.join(deepeningDir, name));
}

function updatePlanningReadme(readmeFile, storyBases, touched) {
  if (!existsSync(readmeFile) || storyBases.length === 0) return false;
  const baseSet = new Set(storyBases);
  const text = read(readmeFile);
  const updated = text.split('\n').map((line) => {
    if (![...baseSet].some((base) => line.includes(base))) return line;
    return line.replace(/\[(BLOCKED|STANDBY|IN PROGRESS)\]/g, '[TODO]');
  }).join('\n');
  if (updated === text) return false;
  write(readmeFile, updated, touched);
  return true;
}

function appendRetrospectiveEntries(file, retryable, skipped, statusById, touched) {
  if (retryable.length === 0 && skipped.length === 0) return false;
  const entries = [];
  for (const item of retryable) {
    entries.push(`### ${date} - Story retry prepared

- **Source:** plan-retry
- **Related story/task:** ${item.id}
- **What happened:** ${item.base} was reset from BLOCKED to TODO after blocker remediation.
- **Expected instead:** The story should continue through /plan-story now that dependencies are DONE.
- **Resolution:** Run \`/plan-story ${planningId} ${item.id}\`.
- **Retrospective signal:** retry prepared; watch for repeat blocker evidence.`);
  }
  for (const item of skipped) {
    const blockers = item.dependencies
      .filter((dependency) => statusById.get(dependency) !== 'DONE')
      .map((dependency) => `${dependency}=${statusById.get(dependency) || 'missing'}`)
      .join(', ');
    entries.push(`### ${date} - Story retry skipped

- **Source:** plan-retry
- **Related story/task:** ${item.id}
- **What happened:** ${item.base} remained BLOCKED because dependencies are not DONE.
- **Expected instead:** Dependencies should be DONE before retrying the story.
- **Resolution:** Skipped retry until dependencies are complete: ${blockers || 'unknown'}.
- **Retrospective signal:** dependency blocker still active.`);
  }

  let text = existsSync(file)
    ? read(file)
    : `# Retrospective Raw Notes: ${planningId}\n\n## Log\n\n*(No unexpected events recorded yet.)*\n`;
  const block = entries.join('\n\n');
  if (text.includes('*(No unexpected events recorded yet.)*')) {
    text = text.replace('*(No unexpected events recorded yet.)*', block);
  } else if (text.includes('<!-- Add newest entries at the top. -->')) {
    text = text.replace('<!-- Add newest entries at the top. -->', `<!-- Add newest entries at the top. -->\n\n${block}`);
  } else {
    text = `${text.trimEnd()}\n\n${block}\n`;
  }
  write(file, text, touched);
  return true;
}

const planningDir = activePlanningDir(planningId);
if (!planningDir) fail(`Planning is not active or does not exist: ${planningId}`, { checked: rel(path.join(planningRoot, 'active', planningId)) });

const deepeningDir = path.join(planningDir, '02-deepening');
const expansionFile = path.join(planningDir, '01-expansion.md');
const touched = [];

const rows = expansionRows(expansionFile);
const rowById = new Map(rows.map((row) => [row.id, row]));
const stories = storyFiles(deepeningDir).map((file) => {
  const base = path.basename(file, '.md');
  const id = storyIdFromNumber(storyNumber(base));
  const text = read(file);
  return {
    id,
    base,
    file,
    status: storyStatus(text),
    dependencies: dependencyIds(rowById.get(id)?.dependsOn || ''),
  };
}).sort((a, b) => storyNumber(a.id) - storyNumber(b.id));

const statusById = new Map(stories.map((story) => [story.id, story.status]));
for (const row of rows) {
  if (!statusById.has(row.id)) statusById.set(row.id, row.status);
}

const blocked = stories.filter((story) => story.status === 'BLOCKED');
const retryable = blocked.filter((story) => story.dependencies.every((dependency) => statusById.get(dependency) === 'DONE'));
const skipped = blocked.filter((story) => !retryable.includes(story));

for (const story of retryable) {
  write(story.file, setStatusLine(read(story.file), 'TODO'), touched);
}
updateExpansionStatus(expansionFile, retryable.map((story) => story.id), 'TODO', touched);
const planningReadmeUpdated = updatePlanningReadme(path.join(planningDir, 'README.md'), retryable.map((story) => story.base), touched);
appendRetrospectiveEntries(path.join(planningDir, 'RETROSPECTIVE-RAW.md'), retryable, skipped, statusById, touched);

const report = {
  ok: true,
  dryRun,
  planningId,
  blocked: blocked.map((story) => story.id),
  retryStories: retryable.map((story) => story.id),
  retryCommands: retryable.map((story) => `/plan-story ${planningId} ${story.id}`),
  skipped: skipped.map((story) => ({
    story: story.id,
    dependencies: story.dependencies.map((dependency) => ({
      story: dependency,
      status: statusById.get(dependency) || 'missing',
    })).filter((dependency) => dependency.status !== 'DONE'),
  })),
  planningReadmeUpdated,
  touched: [...new Set(touched)].sort(),
};

if (format === 'json') {
  console.log(JSON.stringify(report, null, 2));
} else {
  console.log('# Planning Retry\n');
  if (dryRun) console.log('Dry run only. No files were changed.\n');
  if (blocked.length === 0) {
    console.log('No BLOCKED stories found. Nothing to retry.');
  } else {
    console.log(`Blocked stories: \`${blocked.map((story) => story.id).join(', ')}\``);
    console.log(`Retryable now: \`${retryable.map((story) => story.id).join(', ') || 'none'}\``);
    console.log(`Skipped: \`${skipped.map((story) => story.id).join(', ') || 'none'}\`\n`);
    if (retryable.length > 0) {
      console.log('Run next:');
      for (const command of report.retryCommands) console.log(`- \`${command}\``);
      console.log('');
    }
    if (skipped.length > 0) {
      console.log('Skipped dependency blockers:');
      for (const item of report.skipped) {
        const blockers = item.dependencies.map((dependency) => `${dependency.story}=${dependency.status}`).join(', ');
        console.log(`- \`${item.story}\`: ${blockers || 'unknown'}`);
      }
      console.log('');
    }
    console.log('Touched paths:');
    for (const file of report.touched) console.log(`- \`${file}\``);
  }
}
