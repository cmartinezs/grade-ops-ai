#!/usr/bin/env node
import { existsSync, mkdirSync, readdirSync, readFileSync, renameSync, statSync, writeFileSync } from 'node:fs';
import path from 'node:path';

const cwd = process.cwd();
const args = process.argv.slice(2);

let planningId = '';
let dryRun = false;
let format = 'markdown';
let date = new Date().toISOString().slice(0, 10);

function usage() {
  return `Usage:
  node .planning/scripts/planning-archive.mjs <planning-id> [--dry-run] [--date YYYY-MM-DD] [--format markdown|json]

Audits a completed active planning, moves it to .planning/finished/, and updates
active, finished, and root planning indexes.`;
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

const activeDir = path.join(planningRoot, 'active', planningId);
const finishedDir = path.join(planningRoot, 'finished', planningId);
if (!existsSync(activeDir) || !statSync(activeDir).isDirectory()) {
  fail(`Active planning not found: ${planningId}`, { checked: rel(activeDir) });
}
if (existsSync(finishedDir)) fail(`Finished planning already exists: ${rel(finishedDir)}`);

function rel(file) {
  const relative = path.relative(cwd, file);
  return relative || '.';
}

function read(file) {
  return readFileSync(file, 'utf8');
}

function write(file, text, touched) {
  touched.push(rel(file));
  if (!dryRun) {
    mkdirSync(path.dirname(file), { recursive: true });
    writeFileSync(file, text);
  }
}

function storyStatus(text) {
  const match = text.match(/^>\s*\*\*Status:\*\*\s*(.+)$/m);
  return match ? match[1].trim() : '';
}

function storyNumber(id) {
  const match = id.match(/^story-0*(\d+)/);
  return match ? Number(match[1]) : 0;
}

function storyFiles() {
  const deepening = path.join(activeDir, '02-deepening');
  if (!existsSync(deepening)) return [];
  return readdirSync(deepening)
    .filter((name) => /^story-\d+-.+\.md$/.test(name))
    .sort()
    .map((name) => path.join(deepening, name));
}

function markdownSection(text, heading) {
  const escaped = heading.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const headingMatch = text.match(new RegExp(`^(#{1,6})\\s+${escaped}\\b[^\\n]*\\n`, 'im'));
  if (!headingMatch) return '';
  const level = headingMatch[1].length;
  const contentStart = headingMatch.index + headingMatch[0].length;
  const rest = text.slice(contentStart);
  const nextMatch = rest.match(new RegExp(`^#{1,${level}}\\s+`, 'm'));
  const contentEnd = nextMatch ? contentStart + nextMatch.index : text.length;
  return text.slice(contentStart, contentEnd);
}

function stripFenced(text) {
  return text.replace(/```[\s\S]*?```/g, '');
}

function taskRows(text) {
  return stripFenced(markdownSection(text, 'Tasks')).split('\n')
    .filter((line) => line.trim().startsWith('|'))
    .map((line) => line.split('|').map((cell) => cell.trim()))
    .filter((cells) => /^\d+$/.test(cells[1] || ''))
    .map((cells) => ({
      number: cells[1],
      task: cells[2] || '',
      status: cells[4] || '',
      output: cells[5] || '',
    }));
}

function retrospectiveComplete(readmeFile) {
  if (!existsSync(readmeFile)) return false;
  const section = markdownSection(read(readmeFile), 'Retrospective');
  if (!section.trim()) return false;
  const placeholderPatterns = [
    /\[What shipped, changed, or was decided\?\]/i,
    /\[What changed from the original scope/i,
    /\[Open improvements, deferred work/i,
    /\[What should carry forward/i,
    /Complete this section before archiving/i,
  ];
  return !placeholderPatterns.some((pattern) => pattern.test(section));
}

function extractIntent() {
  const readme = path.join(activeDir, 'README.md');
  if (existsSync(readme)) {
    const match = read(readme).match(/^-\s+\*\*Intent:\*\*\s*(.+)$/m);
    if (match && match[1].trim() && !match[1].includes('[')) return match[1].trim();
  }
  const initial = path.join(activeDir, '00-initial.md');
  if (existsSync(initial)) {
    const section = markdownSection(read(initial), 'Intent');
    const line = section.split('\n').map((item) => item.trim()).find((item) => item && !item.startsWith('>') && !item.startsWith('['));
    if (line) return line.replace(/^[-*]\s+/, '');
  }
  return planningId;
}

function traceabilityWarnings() {
  const warnings = [];
  const traceability = path.join(activeDir, 'TRACEABILITY.md');
  if (!existsSync(traceability)) {
    warnings.push('TRACEABILITY.md is missing.');
    return warnings;
  }
  const text = read(traceability);
  if (/\[[^\]]+\]/.test(text) || /\bTODO\b/i.test(text)) warnings.push('TRACEABILITY.md still contains placeholders or TODO markers.');
  const hasEmptyCells = text.split('\n')
    .filter((line) => line.trim().startsWith('|') && !/^\|\s*-+/.test(line.trim()))
    .some((line) => line.split('|').slice(1, -1).some((cell) => cell.trim() === ''));
  if (hasEmptyCells) warnings.push('TRACEABILITY.md may contain empty table cells.');
  return warnings;
}

function inconsistencyWarnings() {
  const warnings = [];
  for (const file of storyFiles()) {
    const section = markdownSection(read(file), 'Inconsistencies Found');
    if (/\|\s*\d+\s*\|.+\|\s*Open\s*\|/i.test(section)) {
      warnings.push(`${path.basename(file)} has open inconsistencies.`);
    }
  }
  return warnings;
}

function audit() {
  const blockers = [];
  const warnings = [];
  const stories = storyFiles().map((file) => {
    const text = read(file);
    const status = storyStatus(text);
    const id = path.basename(file, '.md').match(/^story-\d+/)?.[0] || path.basename(file, '.md');
    const tasks = taskRows(text);
    if (!['DONE', 'SKIPPED'].includes(status)) {
      blockers.push(`${id} is ${status || 'missing status'}`);
    }
    if (status === 'DONE') {
      for (const task of tasks) {
        if (task.status !== 'DONE') blockers.push(`${id} task ${task.number} is ${task.status || 'missing status'}`);
        if (!task.output || task.output === '—' || /\[.+\]/.test(task.output)) {
          warnings.push(`${id} task ${task.number} has weak output evidence.`);
        }
      }
    }
    return { id, status, tasks: tasks.length, file: rel(file) };
  }).sort((a, b) => storyNumber(a.id) - storyNumber(b.id));

  if (stories.length === 0) blockers.push('No story files found under 02-deepening/.');
  if (!retrospectiveComplete(path.join(activeDir, 'README.md'))) {
    const raw = path.join(activeDir, 'RETROSPECTIVE-RAW.md');
    const hasRawEntries = existsSync(raw) && !/\*\(No unexpected events recorded yet\.\)\*/.test(read(raw));
    blockers.push(hasRawEntries
      ? 'README.md retrospective is missing or placeholder-only; run /plan-retrospective first.'
      : 'README.md retrospective is missing or placeholder-only.');
  }
  warnings.push(...traceabilityWarnings(), ...inconsistencyWarnings());
  return { blockers, warnings, stories };
}

function removePlanningEntry(text) {
  return text.split('\n').filter((line) => !line.includes(planningId)).join('\n');
}

function addEntryToSection(text, sectionHeading, entry, nonePattern = /\n\*\(none yet\)\*/g) {
  if (text.includes(entry)) return text;
  const markerIndex = text.indexOf(sectionHeading);
  if (markerIndex < 0) return `${text.trimEnd()}\n\n${sectionHeading}\n\n${entry}\n`;
  const afterMarker = markerIndex + sectionHeading.length;
  const rest = text.slice(afterMarker);
  const nextSection = rest.search(/\n#{2,3}\s/);
  const sectionEnd = nextSection < 0 ? text.length : afterMarker + nextSection;
  const before = text.slice(0, afterMarker);
  const section = text.slice(afterMarker, sectionEnd).replace(nonePattern, '');
  const after = text.slice(sectionEnd);
  return `${before}${section.trimEnd()}\n\n${entry}\n${after}`;
}

function updatePlanningReadme(touched) {
  const readme = path.join(finishedDir, 'README.md');
  if (!existsSync(readme)) return false;
  let text = read(readme);
  text = text.replace(/^-\s+\*\*Current status:\*\*.*$/m, '- **Current status:** Completed');
  text = text.replace(/^-\s+\*\*Completed:\*\*.*$/m, `- **Completed:** ${date}`);
  text = text.replace(/^- \[ \] Stories are DONE or intentionally SKIPPED\.$/m, '- [x] Stories are DONE or intentionally SKIPPED.');
  text = text.replace(/^- \[ \] Retrospective is complete\.$/m, '- [x] Retrospective is complete.');
  write(readme, text, touched);
  return true;
}

function updateIndexes(intent, touched) {
  const activeReadme = path.join(planningRoot, 'active', 'README.md');
  if (existsSync(activeReadme)) write(activeReadme, removePlanningEntry(read(activeReadme)), touched);

  const finishedReadme = path.join(planningRoot, 'finished', 'README.md');
  const finishedEntry = `- [${planningId}](${planningId}/) — ${intent} (COMPLETED ${date})`;
  if (existsSync(finishedReadme)) {
    write(finishedReadme, addEntryToSection(read(finishedReadme), '## Completed', finishedEntry), touched);
  }

  const rootReadme = path.join(planningRoot, 'README.md');
  if (existsSync(rootReadme)) {
    let text = removePlanningEntry(read(rootReadme));
    const rootEntry = `- [${planningId}](finished/${planningId}/README.md) — ${intent} (COMPLETED ${date})`;
    text = addEntryToSection(text, '### ✅ Completed', rootEntry);
    write(rootReadme, text, touched);
  }
}

const auditReport = audit();
const intent = extractIntent();
const touched = [rel(activeDir), rel(finishedDir)];

if (auditReport.blockers.length === 0) {
  if (dryRun) {
    touched.push(rel(path.join(finishedDir, 'README.md')));
    updateIndexes(intent, touched);
  } else {
    mkdirSync(path.dirname(finishedDir), { recursive: true });
    renameSync(activeDir, finishedDir);
    updatePlanningReadme(touched);
    updateIndexes(intent, touched);
  }
}

const report = {
  ok: auditReport.blockers.length === 0,
  dryRun,
  planningId,
  source: rel(activeDir),
  target: rel(finishedDir),
  stories: auditReport.stories,
  blockers: auditReport.blockers,
  warnings: auditReport.warnings,
  touched: [...new Set(touched)].sort(),
};

if (format === 'json') {
  console.log(JSON.stringify(report, null, 2));
} else {
  console.log('# Planning Archive\n');
  if (dryRun) console.log('Dry run only. No files were changed.\n');
  console.log(`Planning: \`${planningId}\``);
  console.log(`Source: \`${rel(activeDir)}\``);
  console.log(`Target: \`${rel(finishedDir)}\``);
  console.log(`Audit: \`${report.ok ? 'pass' : 'blocked'}\`\n`);
  if (report.blockers.length) {
    console.log('Blockers:');
    for (const blocker of report.blockers) console.log(`- ${blocker}`);
    console.log('');
  }
  if (report.warnings.length) {
    console.log('Warnings:');
    for (const warning of report.warnings) console.log(`- ${warning}`);
    console.log('');
  }
  console.log('Stories:');
  for (const story of report.stories) console.log(`- \`${story.id}\` ${story.status || 'UNKNOWN'} (${story.tasks} tasks)`);
  console.log('\nTouched paths:');
  for (const file of report.touched) console.log(`- \`${file}\``);
  if (report.ok) console.log(dryRun ? '\nRe-run without `--dry-run` after approval.' : '\nArchived successfully.');
}
