#!/usr/bin/env node
import fs from 'node:fs';
import path from 'node:path';

const root = process.cwd();
const releasesRoot = path.join(root, '.releases');
const planningRoot = path.join(root, '.planning');
const args = process.argv.slice(2);

let format = 'markdown';
let target = '';
let date = '';
let force = false;
const commandArgs = [];

for (let i = 0; i < args.length; i += 1) {
  const arg = args[i];
  if (arg === '--format') {
    format = args[i + 1] || 'markdown';
    i += 1;
  } else if (arg === '--target') {
    target = args[i + 1] || '';
    i += 1;
  } else if (arg === '--date') {
    date = args[i + 1] || '';
    i += 1;
  } else if (arg === '--force') {
    force = true;
  } else {
    commandArgs.push(arg);
  }
}

const command = commandArgs.shift();
if (!command || ['-h', '--help', 'help'].includes(command)) {
  usage();
  process.exit(0);
}
if (!['markdown', 'json'].includes(format)) failUsage(`Unsupported format: ${format}`);

function usage() {
  console.log(`Usage:
  node .planning/scripts/release.mjs init [--format markdown|json]
  node .planning/scripts/release.mjs new <vX.Y.Z> -- <purpose> --target <YYYY-QN-MN-WN> --date <YYYY-MM-DD>
  node .planning/scripts/release.mjs add <vX.Y.Z> <NNN-slug> [<NNN-slug> ...]
  node .planning/scripts/release.mjs remove <vX.Y.Z> <NNN-slug> [--force]
  node .planning/scripts/release.mjs status [<vX.Y.Z>] [--mark-planned|--mark-in-progress|--mark-blocked|--mark-released|--mark-cancelled] [--force]

Release management for .releases/ backed by live .planning/ status.`);
}

function failUsage(message) {
  console.error(message);
  usage();
  process.exit(2);
}

function exists(filePath) {
  return fs.existsSync(filePath);
}

function isDir(filePath) {
  return exists(filePath) && fs.statSync(filePath).isDirectory();
}

function isFile(filePath) {
  return exists(filePath) && fs.statSync(filePath).isFile();
}

function read(filePath) {
  return fs.readFileSync(filePath, 'utf8');
}

function write(filePath, content) {
  fs.mkdirSync(path.dirname(filePath), { recursive: true });
  fs.writeFileSync(filePath, content);
}

function rel(filePath) {
  return path.relative(root, filePath).replaceAll(path.sep, '/');
}

function releasePath(version) {
  return path.join(releasesRoot, `${version}.md`);
}

function releaseReadmePath() {
  return path.join(releasesRoot, 'README.md');
}

function requireReleases() {
  if (!isDir(releasesRoot)) throw new Error('Run /release-init first.');
}

function assertVersion(version) {
  if (!/^v\d+\.\d+\.\d+$/.test(version || '')) {
    throw new Error('Invalid version format. Use vX.Y.Z, for example v1.0.0.');
  }
}

function splitPurpose(tokens) {
  const joined = tokens.join(' ');
  const index = joined.indexOf(' -- ');
  if (index < 0) return { version: tokens[0], purpose: '' };
  return {
    version: joined.slice(0, index).trim(),
    purpose: joined.slice(index + 4).trim(),
  };
}

function statusLine(text) {
  const match = /^>\s*\*\*Status:\*\*\s*(.+?)\s*$/im.exec(text);
  return match ? match[1].trim() : 'UNKNOWN';
}

function fieldLine(text, field) {
  const match = new RegExp(`^>\\s*\\*\\*${escapeRegex(field)}:\\*\\*\\s*(.+?)\\s*$`, 'im').exec(text);
  return match ? match[1].trim() : '';
}

function replaceField(text, field, value) {
  const pattern = new RegExp(`^>\\s*\\*\\*${escapeRegex(field)}:\\*\\*\\s*.*$`, 'im');
  return text.replace(pattern, `> **${field}:** ${value}`);
}

function escapeRegex(value) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function splitMarkdownRow(line) {
  const trimmed = line.trim();
  if (!trimmed.startsWith('|') || !trimmed.endsWith('|')) return null;
  return trimmed.slice(1, -1).split('|').map((cell) => cell.trim());
}

function parseIncludedRows(text) {
  return parseTableAfterHeading(text, 'Included Plannings')
    .filter((row) => row.cells[1] && row.cells[1] !== '—')
    .map((row) => ({
      number: row.cells[0],
      planning: row.cells[1],
      summary: row.cells[2] || '',
      status: row.cells[3] || 'UNKNOWN',
      line: row.line,
    }));
}

function parseTableAfterHeading(text, heading) {
  const lines = text.split(/\r?\n/);
  const headingPattern = new RegExp(`^##\\s+${escapeRegex(heading)}\\s*$`, 'i');
  const headingIndex = lines.findIndex((line) => headingPattern.test(line.trim()));
  if (headingIndex < 0) return [];
  const rows = [];
  let headerFound = false;
  for (let i = headingIndex + 1; i < lines.length; i += 1) {
    const line = lines[i];
    if (/^##\s+/.test(line)) break;
    if (!line.trim().startsWith('|')) {
      if (headerFound && rows.length > 0) break;
      continue;
    }
    const cells = splitMarkdownRow(line);
    if (!cells) continue;
    if (cells.every((cell) => /^:?-{2,}:?$/.test(cell))) continue;
    if (!headerFound) {
      headerFound = true;
      continue;
    }
    rows.push({ line: i + 1, cells });
  }
  return rows;
}

function replaceIncludedTable(text, rows) {
  const lines = text.split(/\r?\n/);
  const headingIndex = lines.findIndex((line) => /^##\s+Included Plannings\s*$/i.test(line.trim()));
  if (headingIndex < 0) throw new Error('Release file is missing ## Included Plannings.');
  let firstTable = -1;
  let lastTable = -1;
  for (let i = headingIndex + 1; i < lines.length; i += 1) {
    if (/^##\s+/.test(lines[i])) break;
    if (lines[i].trim().startsWith('|')) {
      if (firstTable < 0) firstTable = i;
      lastTable = i;
    } else if (firstTable >= 0 && lastTable >= 0) {
      break;
    }
  }
  if (firstTable < 0) throw new Error('Release file has no Included Plannings table.');
  const table = [
    '| # | Planning | Summary | Status |',
    '|---|----------|---------|--------|',
    ...(rows.length > 0
      ? rows.map((row, index) => `| ${index + 1} | ${row.planning} | ${row.summary} | ${row.status} |`)
      : ['| — | — | — | — |']),
  ];
  lines.splice(firstTable, lastTable - firstTable + 1, ...table);
  return lines.join('\n');
}

function extractSection(text, heading) {
  const pattern = new RegExp(`^##\\s+${escapeRegex(heading)}\\s*$`, 'im');
  const match = pattern.exec(text);
  if (!match) return '';
  const rest = text.slice(match.index + match[0].length);
  const next = /^##\s+/im.exec(rest);
  return (next ? rest.slice(0, next.index) : rest).trim();
}

function locatePlanning(planningId) {
  const candidates = [
    path.join(planningRoot, 'active', planningId),
    path.join(planningRoot, 'finished', planningId),
    path.join(planningRoot, planningId),
  ];
  return candidates.find(isDir) || null;
}

function planningInitial(planningId) {
  const dir = locatePlanning(planningId);
  if (!dir) return null;
  const initial = path.join(dir, '00-initial.md');
  return isFile(initial) ? initial : null;
}

function livePlanningStatus(planningId) {
  const initial = planningInitial(planningId);
  if (!initial) return 'NOT FOUND';
  return statusLine(read(initial));
}

function planningSummary(planningId) {
  const initial = planningInitial(planningId);
  if (!initial) throw new Error(`Planning ${planningId} not found in .planning/active/, .planning/finished/, or .planning/.`);
  const text = read(initial);
  const intent = extractSection(text, 'Intent').split(/\r?\n/).find((line) => line.trim()) || '';
  return firstSentence(intent.replace(/^\[|\]$/g, '').trim()) || planningId;
}

function firstSentence(value) {
  const text = (value || '').replace(/\s+/g, ' ').trim();
  const match = /^(.+?[.!?])(?:\s|$)/.exec(text);
  return match ? match[1] : text;
}

function releaseFiles() {
  if (!isDir(releasesRoot)) return [];
  return fs.readdirSync(releasesRoot, { withFileTypes: true })
    .filter((entry) => entry.isFile() && /^v\d+\.\d+\.\d+\.md$/.test(entry.name))
    .map((entry) => path.join(releasesRoot, entry.name))
    .sort();
}

function setReadmeReleaseRow(version, updates) {
  const readme = releaseReadmePath();
  if (!isFile(readme)) return;
  const lines = read(readme).split(/\r?\n/);
  const index = lines.findIndex((line) => line.includes(`[${version}](${version}.md)`));
  if (index < 0) return;
  const cells = splitMarkdownRow(lines[index]);
  if (!cells || cells.length < 5) return;
  if (updates.target !== undefined) cells[1] = updates.target;
  if (updates.status !== undefined) cells[2] = updates.status;
  if (updates.date !== undefined) cells[3] = updates.date;
  if (updates.progress !== undefined) cells[4] = updates.progress;
  lines[index] = `| ${cells.join(' | ')} |`;
  write(readme, lines.join('\n'));
}

function upsertReadmeRelease(version, targetValue, status, estimatedDate, progress) {
  const readme = releaseReadmePath();
  if (!isFile(readme)) throw new Error('.releases/README.md is missing. Run /release-init again or restore it.');
  const text = read(readme);
  if (text.includes(`[${version}](${version}.md)`)) {
    setReadmeReleaseRow(version, { target: targetValue, status, date: estimatedDate, progress });
    return;
  }
  const row = `| [${version}](${version}.md) | ${targetValue} | ${status} | ${estimatedDate} | ${progress} |`;
  const lines = text.split(/\r?\n/);
  const placeholder = lines.findIndex((line) => /^\|\s*—\s*\|\s*—\s*\|\s*—\s*\|\s*—\s*\|\s*—\s*\|/.test(line.trim()));
  if (placeholder >= 0) lines[placeholder] = row;
  else {
    const lastTable = lines.reduce((last, line, index) => line.trim().startsWith('|') ? index : last, -1);
    lines.splice(lastTable + 1, 0, row);
  }
  write(readme, lines.join('\n'));
}

function progressForRows(rows) {
  const total = rows.length;
  const completed = rows.filter((row) => row.status === 'COMPLETED').length;
  return `${completed}/${total}`;
}

function currentRowsWithLiveStatus(releaseFile) {
  const rows = parseIncludedRows(read(releaseFile));
  return rows.map((row) => {
    const live = livePlanningStatus(row.planning);
    return {
      ...row,
      liveStatus: live,
      displayStatus: live === row.status ? live : `${live} (cached: ${row.status})`,
    };
  });
}

function reportBase(action) {
  return { action, written: [], skipped: [], warnings: [], message: '' };
}

function markWritten(report, filePath) {
  if (!report.written.includes(rel(filePath))) report.written.push(rel(filePath));
}

function runInit() {
  const report = reportBase('init');
  if (isDir(releasesRoot)) throw new Error('.releases/ already initialized - run /release-status to see existing releases.');
  fs.mkdirSync(releasesRoot, { recursive: true });
  const readme = releaseReadmePath();
  write(readme, `# Releases

| Version | Target | Status | Est. Date | Plannings |
|---------|--------|--------|-----------|-----------|
| — | — | — | — | — |
`);
  markWritten(report, readme);
  report.message = '.releases/ initialized. Next step: /release-new <vX.Y.Z> -- <purpose>.';
  return report;
}

function runNew(tokens) {
  const report = reportBase('new');
  requireReleases();
  const parsed = splitPurpose(tokens);
  assertVersion(parsed.version);
  if (!parsed.purpose) throw new Error('Missing release purpose. Use /release-new <vX.Y.Z> -- <purpose>.');
  if (!target) throw new Error('Missing --target <YYYY-QN-MN-WN>. Ask the user for the target period and rerun.');
  if (!date) throw new Error('Missing --date <YYYY-MM-DD>. Ask the user for the estimated date and rerun.');
  if (!/^\d{4}-Q[1-4]-M[1-3]-W[1-4]$/.test(target)) throw new Error('Invalid target format. Use YYYY-QN-MN-WN, for example 2026-Q1-M1-W2.');
  if (!/^\d{4}-\d{2}-\d{2}$/.test(date)) throw new Error('Invalid date format. Use YYYY-MM-DD.');
  const file = releasePath(parsed.version);
  if (isFile(file)) throw new Error(`Release ${parsed.version} already exists. Use /release-add ${parsed.version} <NNN-slug>.`);
  write(file, `# Release ${parsed.version}

> **Status:** DRAFT
> **Target:** ${target}
> **Estimated Date:** ${date}
> [← .releases/README.md](README.md)

---

## Purpose

${parsed.purpose}

---

## Scope

[Which areas, services, or repositories are affected by this release.]

---

## Included Plannings

| # | Planning | Summary | Status |
|---|----------|---------|--------|
| — | — | — | — |

---

## Done Criteria

- [ ] All included plannings are COMPLETED
- [ ] Release notes drafted and reviewed

---

## Release Notes

> *Fill when the release is delivered. Summarize what changed for end users.*

*(pending)*

---

> [← .releases/README.md](README.md)
`);
  upsertReadmeRelease(parsed.version, target, 'DRAFT', date, '0/0');
  markWritten(report, file);
  markWritten(report, releaseReadmePath());
  report.message = `Release ${parsed.version} created at ${rel(file)}. Next step: /release-add ${parsed.version} <NNN-slug>.`;
  return report;
}

function runAdd(tokens) {
  const report = reportBase('add');
  requireReleases();
  const version = tokens.shift();
  assertVersion(version);
  if (tokens.length === 0) throw new Error('Missing planning IDs to add.');
  const file = releasePath(version);
  if (!isFile(file)) throw new Error(`Release ${version} not found. Create it first with /release-new ${version} -- <purpose>.`);
  let text = read(file);
  const rows = parseIncludedRows(text);
  let added = 0;
  let skipped = 0;
  for (const planningId of tokens) {
    if (rows.some((row) => row.planning === planningId)) {
      skipped += 1;
      report.skipped.push(`${planningId} already in release`);
      continue;
    }
    rows.push({
      planning: planningId,
      summary: planningSummary(planningId),
      status: livePlanningStatus(planningId),
    });
    added += 1;
  }
  text = replaceIncludedTable(text, rows);
  write(file, text);
  setReadmeReleaseRow(version, { progress: progressForRows(rows) });
  markWritten(report, file);
  markWritten(report, releaseReadmePath());
  report.message = `${added} planning(s) added, ${skipped} skipped.`;
  report.table = includedTable(rows);
  return report;
}

function runRemove(tokens) {
  const report = reportBase('remove');
  requireReleases();
  const [version, planningId] = tokens;
  assertVersion(version);
  if (!planningId) throw new Error('Missing planning ID to remove.');
  const file = releasePath(version);
  if (!isFile(file)) throw new Error(`Release ${version} not found.`);
  const text = read(file);
  const releaseStatus = statusLine(text);
  if (releaseStatus === 'RELEASED' && !force) {
    throw new Error(`Release ${version} has already shipped. Rerun with --force only after explicit confirmation.`);
  }
  const rows = parseIncludedRows(text);
  const nextRows = rows.filter((row) => row.planning !== planningId);
  if (nextRows.length === rows.length) throw new Error(`Planning ${planningId} is not in release ${version}.`);
  write(file, replaceIncludedTable(text, nextRows));
  setReadmeReleaseRow(version, { progress: progressForRows(nextRows) });
  markWritten(report, file);
  markWritten(report, releaseReadmePath());
  report.message = `Planning ${planningId} removed from release ${version}. Plannings remaining: ${nextRows.length}.`;
  return report;
}

function runStatus(tokens) {
  const report = reportBase('status');
  requireReleases();
  const version = tokens.find((token) => !token.startsWith('--'));
  const mark = tokens.find((token) => token.startsWith('--mark-'));
  if (mark) return runMark(version, mark);
  if (version) return detailStatus(version);
  return summaryStatus();
}

function runMark(version, mark) {
  const report = reportBase('mark');
  assertVersion(version);
  const file = releasePath(version);
  if (!isFile(file)) throw new Error(`Release ${version} not found.`);
  const status = {
    '--mark-planned': 'PLANNED',
    '--mark-in-progress': 'IN PROGRESS',
    '--mark-blocked': 'BLOCKED',
    '--mark-released': 'RELEASED',
    '--mark-cancelled': 'CANCELLED',
  }[mark];
  if (!status) throw new Error(`Unknown transition flag: ${mark}`);
  const rows = currentRowsWithLiveStatus(file);
  if (status === 'RELEASED') {
    const incomplete = rows.filter((row) => row.liveStatus !== 'COMPLETED');
    if (incomplete.length > 0) {
      throw new Error(`Cannot mark as RELEASED. Not completed: ${incomplete.map((row) => `${row.planning}=${row.liveStatus}`).join(', ')}.`);
    }
  }
  if (status === 'BLOCKED' && !force && !rows.some((row) => row.liveStatus === 'BLOCKED')) {
    throw new Error(`No included planning is currently BLOCKED. Rerun with --force only after explicit confirmation.`);
  }
  const next = replaceField(read(file), 'Status', status);
  write(file, next);
  setReadmeReleaseRow(version, { status });
  markWritten(report, file);
  markWritten(report, releaseReadmePath());
  report.message = `Release ${version} marked as ${status}.`;
  return report;
}

function summaryStatus() {
  const report = reportBase('status');
  const files = releaseFiles();
  if (files.length === 0) {
    report.message = 'No releases yet. Create one with /release-new <vX.Y.Z> -- <purpose>.';
    return report;
  }
  report.rows = files.map((file) => {
    const text = read(file);
    const version = path.basename(file, '.md');
    const releaseStatus = statusLine(text);
    const rows = currentRowsWithLiveStatus(file);
    const completed = rows.filter((row) => row.liveStatus === 'COMPLETED').length;
    const total = rows.length;
    return {
      version,
      target: fieldLine(text, 'Target') || 'UNKNOWN',
      status: releaseStatus,
      date: fieldLine(text, 'Estimated Date') || 'UNKNOWN',
      progress: `${completed}/${total}`,
      suggestion: suggestionFor(version, releaseStatus, rows),
    };
  });
  return report;
}

function detailStatus(version) {
  assertVersion(version);
  const file = releasePath(version);
  if (!isFile(file)) throw new Error(`Release ${version} not found.`);
  const report = reportBase('status');
  const text = read(file);
  const rows = currentRowsWithLiveStatus(file);
  report.version = version;
  report.status = statusLine(text);
  report.target = fieldLine(text, 'Target') || 'UNKNOWN';
  report.date = fieldLine(text, 'Estimated Date') || 'UNKNOWN';
  report.purpose = extractSection(text, 'Purpose');
  report.scope = extractSection(text, 'Scope');
  report.rows = rows;
  report.suggestion = suggestionFor(version, report.status, rows);
  return report;
}

function suggestionFor(version, releaseStatus, rows) {
  if (rows.length === 0) return `add plannings with /release-add ${version} <NNN-slug>`;
  if (rows.some((row) => row.liveStatus === 'NOT FOUND')) return 'planning not found in .planning/';
  if (rows.some((row) => row.liveStatus === 'BLOCKED') && releaseStatus !== 'BLOCKED') return `has BLOCKED planning - run /release-status ${version} --mark-blocked or resolve the blocker`;
  if (rows.every((row) => row.liveStatus === 'COMPLETED') && releaseStatus !== 'RELEASED') return `ready - run /release-status ${version} --mark-released`;
  if (rows.some((row) => row.liveStatus === 'COMPLETED') && releaseStatus === 'DRAFT') return `consider moving to IN PROGRESS - run /release-status ${version} --mark-in-progress`;
  return '—';
}

function includedTable(rows) {
  return [
    '| # | Planning | Summary | Status |',
    '|---|----------|---------|--------|',
    ...(rows.length ? rows.map((row, index) => `| ${index + 1} | ${row.planning} | ${row.summary} | ${row.status} |`) : ['| — | — | — | — |']),
  ].join('\n');
}

function render(report) {
  if (format === 'json') return JSON.stringify(report, null, 2);
  if (report.action === 'status' && report.rows && !report.version) {
    return [
      '| Version | Target | Status | Est. Date | Progress | Suggestion |',
      '|---------|--------|--------|-----------|----------|------------|',
      ...report.rows.map((row) => `| ${row.version} | ${row.target} | ${row.status} | ${row.date} | ${row.progress} | ${row.suggestion} |`),
    ].join('\n');
  }
  if (report.action === 'status' && report.version) {
    return [
      `# Release ${report.version}`,
      '',
      `Status: ${report.status}`,
      `Target: ${report.target}`,
      `Estimated Date: ${report.date}`,
      '',
      `## Purpose`,
      report.purpose || '—',
      '',
      `## Included Plannings`,
      includedTable(report.rows.map((row) => ({ ...row, status: row.displayStatus }))),
      '',
      report.suggestion && report.suggestion !== '—' ? `Suggestion: ${report.suggestion}` : '',
    ].filter((line) => line !== '').join('\n');
  }
  const lines = [`## Release ${report.action}`];
  if (report.message) lines.push('', report.message);
  if (report.table) lines.push('', report.table);
  if (report.written?.length) {
    lines.push('', 'Files written:');
    for (const file of report.written) lines.push(`- ${file}`);
  }
  if (report.skipped?.length) {
    lines.push('', 'Skipped:');
    for (const item of report.skipped) lines.push(`- ${item}`);
  }
  return lines.join('\n');
}

let report;
try {
  if (command === 'init') report = runInit();
  else if (command === 'new') report = runNew(commandArgs);
  else if (command === 'add') report = runAdd(commandArgs);
  else if (command === 'remove') report = runRemove(commandArgs);
  else if (command === 'status') report = runStatus(commandArgs);
  else failUsage(`Unknown command: ${command}`);
} catch (error) {
  if (format === 'json') console.log(JSON.stringify({ action: command, error: error.message }, null, 2));
  else console.error(`ERROR: ${error.message}`);
  process.exit(1);
}

console.log(render(report));
