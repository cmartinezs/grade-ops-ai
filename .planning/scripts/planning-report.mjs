#!/usr/bin/env node
import { execFileSync } from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';

const root = process.cwd();
const planningRoot = path.join(root, '.planning');
const today = new Date().toISOString().slice(0, 10);

const args = process.argv.slice(2);
let command = args.shift();
let output = 'markdown';
let exportFormat = 'markdown';
let metrics = false;
const commandArgs = [];

for (let i = 0; i < args.length; i += 1) {
  const arg = args[i];
  if (arg === '--output') {
    output = args[i + 1] || 'markdown';
    i += 1;
  } else if (arg === '--format') {
    exportFormat = args[i + 1] || 'markdown';
    i += 1;
  } else if (arg === '--metrics') {
    metrics = true;
  } else {
    commandArgs.push(arg);
  }
}

if (!command || ['-h', '--help', 'help'].includes(command)) {
  usage();
  process.exit(0);
}

if (!['markdown', 'json'].includes(output)) failUsage(`Unsupported output: ${output}`);

function usage() {
  console.log(`Usage:
  node .planning/scripts/planning-report.mjs status [NNN-slug] [--output markdown|json]
  node .planning/scripts/planning-report.mjs report <NNN-slug> [--metrics] [--output markdown|json]
  node .planning/scripts/planning-report.mjs export <NNN-slug> [--format pr|tickets|github-issue|jira|linear|markdown] [--output markdown|json]
  node .planning/scripts/planning-report.mjs standup <NNN-slug> [--output markdown|json]
  node .planning/scripts/planning-report.mjs history <NNN-slug> [--output markdown|json]

Read-only reporting for .planning/ state, summaries, exports, standups, and history.`);
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

function rel(filePath) {
  return path.relative(root, filePath).replaceAll(path.sep, '/');
}

function listDirs(dir) {
  if (!isDir(dir)) return [];
  return fs.readdirSync(dir, { withFileTypes: true })
    .filter((entry) => entry.isDirectory())
    .map((entry) => path.join(dir, entry.name))
    .sort();
}

function listFiles(dir, pattern = /.*/) {
  if (!isDir(dir)) return [];
  return fs.readdirSync(dir, { withFileTypes: true })
    .filter((entry) => entry.isFile() && pattern.test(entry.name))
    .map((entry) => path.join(dir, entry.name))
    .sort(compareNatural);
}

function walk(dir, predicate = () => true, output = []) {
  if (!isDir(dir)) return output;
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) walk(full, predicate, output);
    else if (entry.isFile() && predicate(full)) output.push(full);
  }
  return output.sort(compareNatural);
}

function compareNatural(a, b) {
  return a.localeCompare(b, undefined, { numeric: true, sensitivity: 'base' });
}

function escapeRegex(value) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function section(text, heading) {
  const pattern = new RegExp(`^##\\s+${escapeRegex(heading)}\\s*$`, 'im');
  const match = pattern.exec(text);
  if (!match) return '';
  const rest = text.slice(match.index + match[0].length);
  const next = /^##\s+/im.exec(rest);
  return (next ? rest.slice(0, next.index) : rest).trim();
}

function firstLine(value) {
  return (value || '').split(/\r?\n/).map((line) => line.trim()).find(Boolean) || '';
}

function firstSentence(value) {
  const text = (value || '').replace(/\s+/g, ' ').replace(/^\[|\]$/g, '').trim();
  const match = /^(.+?[.!?])(?:\s|$)/.exec(text);
  return match ? match[1] : text;
}

function firstHeading(text, fallback) {
  const match = /^#\s+(.+)$/m.exec(text);
  return match ? match[1].trim() : fallback;
}

function splitMarkdownRow(line) {
  const trimmed = line.trim();
  if (!trimmed.startsWith('|') || !trimmed.endsWith('|')) return null;
  return trimmed.slice(1, -1).split('|').map((cell) => cell.trim());
}

function normalizeHeader(value) {
  return value.toLowerCase().replace(/`/g, '').replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');
}

function parseTableAfterHeading(text, heading) {
  const lines = text.split(/\r?\n/);
  const headingPattern = new RegExp(`^##\\s+${escapeRegex(heading)}\\s*$`, 'i');
  const headingIndex = lines.findIndex((line) => headingPattern.test(line.trim()));
  if (headingIndex < 0) return [];
  const rows = [];
  let header = null;
  for (let i = headingIndex + 1; i < lines.length; i += 1) {
    const line = lines[i];
    if (/^##\s+/.test(line)) break;
    if (!line.trim().startsWith('|')) {
      if (header && rows.length > 0) break;
      continue;
    }
    const cells = splitMarkdownRow(line);
    if (!cells) continue;
    if (cells.every((cell) => /^:?-{2,}:?$/.test(cell))) continue;
    if (!header) {
      header = cells.map(normalizeHeader);
      continue;
    }
    const row = { cells, line: i + 1 };
    header.forEach((name, index) => {
      row[name] = cells[index] || '';
    });
    rows.push(row);
  }
  return rows;
}

function extractStatus(text) {
  const match = /^>\s*\*\*Status:\*\*\s*(.+?)\s*$/im.exec(text);
  return match ? match[1].trim() : 'UNKNOWN';
}

function storyIdFromValue(value) {
  const match = /story-\d+/i.exec(value || '');
  return match ? match[0].toLowerCase() : null;
}

function taskIdFromValue(value) {
  const match = /task-\d+/i.exec(value || '');
  return match ? match[0].toLowerCase() : null;
}

function planningNameMatches(name) {
  return /^\d{3}-/.test(name);
}

function discoverPlannings() {
  const locations = [
    { location: 'initial', dir: planningRoot },
    { location: 'active', dir: path.join(planningRoot, 'active') },
    { location: 'finished', dir: path.join(planningRoot, 'finished') },
  ];
  const plannings = [];
  for (const location of locations) {
    for (const dir of listDirs(location.dir)) {
      const id = path.basename(dir);
      if (planningNameMatches(id)) plannings.push({ id, location: location.location, dir });
    }
  }
  return plannings.sort((a, b) => `${a.location}:${a.id}`.localeCompare(`${b.location}:${b.id}`));
}

function locatePlanning(planningId) {
  return discoverPlannings().find((planning) => planning.id === planningId) || null;
}

function initialFile(planning) {
  return path.join(planning.dir, '00-initial.md');
}

function expansionFile(planning) {
  return path.join(planning.dir, '01-expansion.md');
}

function deepeningDir(planning) {
  return path.join(planning.dir, '02-deepening');
}

function planningIntent(planning) {
  const file = initialFile(planning);
  if (!isFile(file)) return planning.id;
  const text = read(file);
  return firstSentence(section(text, 'Intent')) || firstHeading(text, planning.id);
}

function planningWhy(planning) {
  const file = initialFile(planning);
  return isFile(file) ? firstLine(section(read(file), 'Why')) : '';
}

function openQuestions(planning) {
  const file = initialFile(planning);
  if (!isFile(file)) return [];
  return section(read(file), 'Open Questions')
    .split(/\r?\n/)
    .map((line) => line.replace(/^\s*[-*]\s*/, '').trim())
    .filter(Boolean);
}

function storyRows(planning) {
  const file = expansionFile(planning);
  if (!isFile(file)) return [];
  return parseTableAfterHeading(read(file), 'Story Summary')
    .map((row) => {
      const storyText = row.story || row.id || row.name || row.cells.join(' ');
      const id = storyIdFromValue(storyText);
      return {
        id,
        title: storyText.replace(/\[|\]|\([^)]*\)/g, '').trim() || id,
        area: row.area || '',
        risk: row.risk || '',
        externalIssue: row.external-issue || row.external-id || row.issue || '',
        status: row.status || '',
        dependsOn: row.depends-on || row.dependencies || row.depends || '',
        notes: row.notes || '',
      };
    })
    .filter((row) => row.id);
}

function storyFiles(planning) {
  return listFiles(deepeningDir(planning), /^story-\d+-.*\.md$/i);
}

function taskFolders(planning) {
  return listDirs(deepeningDir(planning)).filter((dir) => /^story-\d+-/i.test(path.basename(dir)));
}

function storyFileFor(planning, storyId) {
  return storyFiles(planning).find((file) => path.basename(file).toLowerCase().startsWith(`${storyId.toLowerCase()}-`)) || null;
}

function taskFilesFor(planning, storyId) {
  const folder = taskFolders(planning).find((dir) => path.basename(dir).toLowerCase().startsWith(`${storyId.toLowerCase()}-`));
  return folder ? listFiles(folder, /^task-\d+-.*\.md$/i) : [];
}

function doneCriteria(text) {
  return section(text, 'Done Criteria')
    .split(/\r?\n/)
    .map((line) => line.replace(/^\s*-\s*\[[ xX]\]\s*/, '').replace(/^\s*[-*]\s*/, '').trim())
    .filter(Boolean);
}

function storyDetails(planning) {
  const byId = new Map(storyRows(planning).map((row) => [row.id, row]));
  for (const file of storyFiles(planning)) {
    const text = read(file);
    const id = storyIdFromValue(path.basename(file));
    if (!id) continue;
    const row = byId.get(id) || { id, title: id, area: '', risk: '', externalIssue: '', status: '' };
    row.file = rel(file);
    row.title = firstHeading(text, row.title);
    row.status = extractStatus(text) !== 'UNKNOWN' ? extractStatus(text) : row.status;
    row.objective = firstSentence(section(text, 'Objective'));
    row.doneCriteria = doneCriteria(text);
    row.tasks = taskFilesFor(planning, id).map((taskFile) => taskSummary(taskFile));
    byId.set(id, row);
  }
  return [...byId.values()].sort((a, b) => compareNatural(a.id, b.id));
}

function taskSummary(taskFile) {
  const text = read(taskFile);
  const id = taskIdFromValue(path.basename(taskFile));
  return {
    id,
    file: rel(taskFile),
    title: firstHeading(text, path.basename(taskFile, '.md')),
    status: extractStatus(text),
    objective: firstSentence(section(text, 'Objective')),
    technicalDesign: section(text, 'Technical Design'),
    doneCriteria: doneCriteria(text),
  };
}

function statusCounts(stories) {
  const counts = { TODO: 0, 'IN PROGRESS': 0, DONE: 0, BLOCKED: 0, SKIPPED: 0, STANDBY: 0, UNKNOWN: 0 };
  for (const story of stories) {
    const status = (story.status || 'UNKNOWN').toUpperCase();
    counts[status] = (counts[status] || 0) + 1;
  }
  return counts;
}

function riskRows(planning, stories) {
  const rows = [];
  const expansion = expansionFile(planning);
  if (isFile(expansion)) {
    for (const row of parseTableAfterHeading(read(expansion), 'Risk Register')) {
      rows.push({
        risk: row.risk || row.id || row.cells[0] || '',
        impact: row.impact || '',
        likelihood: row.likelihood || '',
        mitigation: row.mitigation || '',
        status: row.status || 'Open',
      });
    }
  }
  for (const story of stories) {
    if (story.risk && !/^(-|none|n\/a)$/i.test(story.risk)) {
      rows.push({ risk: `${story.id}: ${story.risk}`, impact: '', likelihood: '', mitigation: '', status: story.status || '' });
    }
  }
  return rows;
}

function pdrRows(planning) {
  return listFiles(planning.dir, /^pdr-.*\.md$/i).map((file) => {
    const text = read(file);
    return {
      id: path.basename(file, '.md'),
      status: extractStatus(text),
      decision: firstSentence(section(text, 'Decision')),
      affectedAreas: firstLine(section(text, 'Affected Areas')) || '-',
      related: firstLine(section(text, 'Related')) || '-',
    };
  });
}

function technicalDecisions(stories) {
  const decisions = [];
  const pattern = /\b(chose|decided|use|using|instead of|avoid|rejected|selected)\b/i;
  for (const story of stories) {
    for (const task of story.tasks || []) {
      const sentences = task.technicalDesign.split(/(?<=[.!?])\s+/).map((item) => item.trim()).filter((item) => pattern.test(item));
      for (const sentence of sentences.slice(0, 2)) {
        decisions.push({ source: `${story.id} / ${task.id}`, decision: sentence });
      }
    }
  }
  return decisions.slice(0, 20);
}

function timeline(planning) {
  try {
    const out = execFileSync('git', ['log', '--format=%as', '--', rel(planning.dir)], { cwd: root, encoding: 'utf8', stdio: ['ignore', 'pipe', 'ignore'] }).trim();
    const dates = out.split(/\r?\n/).filter(Boolean);
    if (dates.length === 0) return { started: 'unknown', lastActivity: 'unknown', durationDays: 'unknown', commits: 0 };
    const lastActivity = dates[0];
    const started = dates[dates.length - 1];
    const durationDays = Math.max(0, Math.round((Date.parse(lastActivity) - Date.parse(started)) / 86400000));
    return { started, lastActivity, durationDays, commits: dates.length };
  } catch {
    return { started: 'unknown', lastActivity: 'unknown', durationDays: 'unknown', commits: 0 };
  }
}

function planningModel(planningId) {
  const planning = locatePlanning(planningId);
  if (!planning) throw new Error(`Planning ${planningId} not found in .planning/, .planning/active/, or .planning/finished/.`);
  const stories = storyDetails(planning);
  const counts = statusCounts(stories);
  const taskCount = stories.reduce((total, story) => total + (story.tasks?.length || 0), 0);
  const totalStories = stories.length;
  return {
    id: planning.id,
    location: planning.location,
    path: rel(planning.dir),
    intent: planningIntent(planning),
    why: planningWhy(planning),
    openQuestions: openQuestions(planning),
    stories,
    counts,
    riskRows: riskRows(planning, stories),
    pdrRows: pdrRows(planning),
    decisions: technicalDecisions(stories),
    timeline: timeline(planning),
    metrics: {
      completionRate: totalStories ? Math.round(((counts.DONE + counts.SKIPPED) / totalStories) * 100) : 0,
      blockedRatio: totalStories ? Math.round((counts.BLOCKED / totalStories) * 100) : 0,
      skippedRatio: totalStories ? Math.round((counts.SKIPPED / totalStories) * 100) : 0,
      averageTasksPerStory: totalStories ? Math.round((taskCount / totalStories) * 10) / 10 : 0,
      externalIssueCoverage: `${stories.filter((story) => story.externalIssue && story.externalIssue !== '-').length}/${totalStories}`,
      riskDistribution: riskDistribution(stories),
    },
  };
}

function riskDistribution(stories) {
  const counts = { H: 0, M: 0, L: 0, unknown: 0 };
  for (const story of stories) {
    const risk = (story.risk || '').toUpperCase();
    if (/\bH\b|HIGH/.test(risk)) counts.H += 1;
    else if (/\bM\b|MED/.test(risk)) counts.M += 1;
    else if (/\bL\b|LOW/.test(risk)) counts.L += 1;
    else counts.unknown += 1;
  }
  return counts;
}

function runStatus() {
  const target = commandArgs[0];
  if (!isDir(planningRoot)) return { kind: 'status', error: 'No .planning/ directory found. Run /plan-init first.' };
  if (target) return { kind: 'status', target: planningModel(target) };
  const plannings = discoverPlannings().map((planning) => ({
    id: planning.id,
    location: planning.location,
    path: rel(planning.dir),
    intent: planningIntent(planning),
    stories: planning.location === 'active' ? storyDetails(planning).map((story) => ({ id: story.id, status: story.status, title: story.title })) : [],
  }));
  return { kind: 'status', plannings };
}

function runReport() {
  const planningId = commandArgs[0];
  if (!planningId) failUsage('report requires <NNN-slug>');
  return { kind: 'report', includeMetrics: metrics, planning: planningModel(planningId) };
}

function runExport() {
  const planningId = commandArgs[0];
  if (!planningId) failUsage('export requires <NNN-slug>');
  if (!['pr', 'tickets', 'github-issue', 'jira', 'linear', 'markdown'].includes(exportFormat)) failUsage(`Unsupported export format: ${exportFormat}`);
  return { kind: 'export', exportFormat, planning: planningModel(planningId) };
}

function recentGitPaths(planning) {
  try {
    const out = execFileSync('git', ['log', '--since=yesterday 00:00', '--name-only', '--format=', '--', rel(planning.dir)], { cwd: root, encoding: 'utf8', stdio: ['ignore', 'pipe', 'ignore'] }).trim();
    return new Set(out.split(/\r?\n/).filter(Boolean));
  } catch {
    return new Set();
  }
}

function runStandup() {
  const planningId = commandArgs[0];
  if (!planningId) failUsage('standup requires <NNN-slug>');
  const planning = locatePlanning(planningId);
  if (!planning) throw new Error(`Planning ${planningId} not found in .planning/.`);
  const model = planningModel(planningId);
  const recent = recentGitPaths(planning);
  const yesterday = [];
  const todayItems = [];
  const blockers = [];
  for (const story of model.stories) {
    const touched = story.file && recent.has(story.file);
    if (story.status === 'DONE' && touched) yesterday.push(story);
    if (story.status === 'IN PROGRESS') todayItems.push(story);
    if (story.status === 'BLOCKED') blockers.push(story);
  }
  if (todayItems.length === 0) {
    const next = model.stories.find((story) => story.status === 'TODO');
    if (next) todayItems.push({ ...next, nextUp: true });
  }
  return { kind: 'standup', planning: model, yesterday, todayItems, blockers, gitActivityFound: recent.size > 0 };
}

function runHistory() {
  const planningId = commandArgs[0];
  if (!planningId) failUsage('history requires <NNN-slug>');
  const planning = locatePlanning(planningId);
  if (!planning) throw new Error(`Planning ${planningId} not found in .planning/.`);
  const events = [];
  try {
    const files = storyFiles(planning);
    for (const file of files) {
      const storyId = storyIdFromValue(path.basename(file));
      const commits = execFileSync('git', ['log', '--format=%H%x09%as%x09%s', '--', rel(file)], { cwd: root, encoding: 'utf8', stdio: ['ignore', 'pipe', 'ignore'] }).trim().split(/\r?\n/).filter(Boolean);
      for (const line of commits) {
        const [hash, date, ...subjectParts] = line.split('\t');
        const diff = execFileSync('git', ['show', '--format=', '--unified=0', hash, '--', rel(file)], { cwd: root, encoding: 'utf8', stdio: ['ignore', 'pipe', 'ignore'] });
        const from = /^-\s*>?\s*\*\*Status:\*\*\s*(.+)$/im.exec(diff)?.[1]?.trim();
        const to = /^\+\s*>?\s*\*\*Status:\*\*\s*(.+)$/im.exec(diff)?.[1]?.trim();
        if (from || to) events.push({ date, event: storyId, details: `${from || 'unknown'} -> ${to || 'unknown'} (${subjectParts.join(' ')})` });
      }
    }
  } catch {
    return { kind: 'history', planningId, sparse: true, events: [] };
  }
  events.sort((a, b) => a.date.localeCompare(b.date) || a.event.localeCompare(b.event));
  return { kind: 'history', planningId, sparse: events.length === 0, events };
}

function renderStatus(report) {
  if (report.error) return report.error;
  if (report.target) return renderReport({ planning: report.target, includeMetrics: true });
  const groups = ['initial', 'active', 'finished'];
  const titles = { initial: 'INITIAL', active: 'ACTIVE (EXPANSION / DEEPENING)', finished: 'COMPLETED' };
  const lines = ['# Planning Status', ''];
  for (const group of groups) {
    const items = report.plannings.filter((planning) => planning.location === group);
    lines.push(titles[group]);
    if (items.length === 0) {
      lines.push('  (none)', '');
      continue;
    }
    for (const planning of items) {
      lines.push(`  ${planning.id} - ${planning.intent}`);
      for (const story of planning.stories) lines.push(`    ${story.id} [${story.status || 'UNKNOWN'}] - ${story.title}`);
    }
    lines.push('');
  }
  lines.push('Read-only: no files were modified.');
  return lines.join('\n');
}

function renderReport(report) {
  const p = report.planning;
  const lines = [
    `# Planning Report - ${p.id}`,
    `Generated: ${today}`,
    '',
    '## Objective',
    p.intent || '-',
    '',
    '## Why',
    p.why || '-',
    '',
    '## Story Summary',
    '| Status | Count |',
    '|--------|-------|',
  ];
  for (const status of ['DONE', 'IN PROGRESS', 'TODO', 'BLOCKED', 'SKIPPED', 'STANDBY', 'UNKNOWN']) {
    if (p.counts[status]) lines.push(`| ${status} | ${p.counts[status]} |`);
  }
  lines.push(`| **Total** | **${p.stories.length}** |`, '', '## Story Detail');
  lines.push('| Story | Area | Risk | External Issue | Status | Notes |');
  lines.push('|-------|------|------|----------------|--------|-------|');
  for (const story of p.stories) lines.push(`| ${story.id} | ${story.area || '-'} | ${story.risk || '-'} | ${story.externalIssue || '-'} | ${story.status || 'UNKNOWN'} | ${story.notes || story.objective || '-'} |`);
  lines.push('', '## Risk Summary');
  if (p.riskRows.length === 0) lines.push('- No risks recorded.');
  else {
    lines.push('| Risk | Impact | Likelihood | Mitigation | Status |', '|------|--------|------------|------------|--------|');
    for (const risk of p.riskRows) lines.push(`| ${risk.risk || '-'} | ${risk.impact || '-'} | ${risk.likelihood || '-'} | ${risk.mitigation || '-'} | ${risk.status || '-'} |`);
  }
  if (report.includeMetrics) {
    const r = p.metrics.riskDistribution;
    lines.push('', '## Metrics');
    lines.push(`- Completion rate: ${p.metrics.completionRate}%`);
    lines.push(`- Blocked ratio: ${p.metrics.blockedRatio}%`);
    lines.push(`- Skipped ratio: ${p.metrics.skippedRatio}%`);
    lines.push(`- Average tasks per story: ${p.metrics.averageTasksPerStory}`);
    lines.push(`- Risk distribution: H=${r.H}, M=${r.M}, L=${r.L}, unknown=${r.unknown}`);
    lines.push(`- External issue coverage: ${p.metrics.externalIssueCoverage} stories`);
  }
  lines.push('', '## Key Technical Decisions');
  if (p.decisions.length === 0) lines.push('- None found in task Technical Design sections.');
  else for (const item of p.decisions) lines.push(`- [${item.source}] ${item.decision}`);
  lines.push('', '## Project Decision Records');
  if (p.pdrRows.length === 0) lines.push('- None.');
  else {
    lines.push('| PDR | Status | Decision | Affected Areas | Related |', '|-----|--------|----------|----------------|---------|');
    for (const pdr of p.pdrRows) lines.push(`| ${pdr.id} | ${pdr.status} | ${pdr.decision || '-'} | ${pdr.affectedAreas} | ${pdr.related} |`);
  }
  lines.push('', '## Timeline');
  lines.push(`- Started: ${p.timeline.started}`);
  lines.push(`- Last activity: ${p.timeline.lastActivity}`);
  lines.push(`- Duration: ${p.timeline.durationDays} days`);
  lines.push(`- Commits: ${p.timeline.commits}`);
  lines.push('', '## Open Questions');
  if (p.openQuestions.length === 0) lines.push('- None.');
  else lines.push(...p.openQuestions.map((item) => `- ${item}`));
  lines.push('', '## Next Steps');
  const remaining = p.stories.filter((story) => !['DONE', 'SKIPPED'].includes((story.status || '').toUpperCase()));
  if (remaining.length === 0) lines.push('Planning complete - ready to archive.');
  else lines.push(...remaining.map((story) => `- ${story.id}: ${story.status || 'UNKNOWN'} - ${story.title || story.objective || ''}`));
  lines.push('', 'Read-only: no files were modified.');
  return lines.join('\n');
}

function renderExport(report) {
  const p = report.planning;
  if (report.exportFormat === 'pr') return renderPrExport(p);
  if (report.exportFormat === 'tickets') return renderTicketsExport(p);
  if (['github-issue', 'jira', 'linear'].includes(report.exportFormat)) return renderIssueExport(p, report.exportFormat);
  return renderMarkdownExport(p);
}

function renderMarkdownExport(p) {
  const lines = [`# ${p.id}: ${p.intent}`, '', `**Why:** ${p.why || '-'}`, '', '## Stories', '', '| Story | Area | Status | Summary |', '|-------|------|--------|---------|'];
  for (const story of p.stories) lines.push(`| ${story.id} | ${story.area || '-'} | ${story.status || 'UNKNOWN'} | ${story.objective || story.title || '-'} |`);
  lines.push('', '## Done Criteria');
  for (const story of p.stories) {
    lines.push('', `### ${story.id}`);
    if (story.doneCriteria?.length) lines.push(...story.doneCriteria.map((item) => `- ${item}`));
    else lines.push('- None recorded.');
  }
  lines.push('', '## Open Questions');
  if (p.openQuestions.length) lines.push(...p.openQuestions.map((item) => `- ${item}`));
  else lines.push('- None.');
  lines.push('', `Generated by /plan-export from ${p.path} on ${today}.`);
  return lines.join('\n');
}

function renderPrExport(p) {
  const lines = ['## Summary', '', `- Implements ${p.intent}`];
  for (const story of p.stories) lines.push(`- ${story.id}: ${story.objective || story.title || story.status}`);
  lines.push('', '## Stories', '', '| Story | Status |', '|-------|--------|');
  for (const story of p.stories) lines.push(`| ${story.id} | ${story.status || 'UNKNOWN'} |`);
  lines.push('', '## Test Plan');
  const criteria = p.stories.flatMap((story) => story.doneCriteria || []).slice(0, 12);
  if (criteria.length) lines.push(...criteria.map((item) => `- [ ] ${item}`));
  else lines.push('- [ ] Review planning verification evidence.');
  lines.push('', '## Planning reference', `\`${p.path}/\``, '', `Generated by /plan-export on ${today}.`);
  return lines.join('\n');
}

function renderTicketsExport(p) {
  const lines = [`## Tickets - ${p.id}`];
  p.stories.forEach((story, index) => {
    lines.push('', `${index + 1}. **[${story.id}]** ${story.title || story.objective || story.id}`);
    lines.push(`   Area: ${story.area || '-'}`);
    lines.push(`   Description: ${story.objective || '-'}`);
    lines.push('   Acceptance criteria:');
    const criteria = story.doneCriteria?.length ? story.doneCriteria : ['Review story done criteria.'];
    for (const item of criteria) lines.push(`   - ${item}`);
  });
  lines.push('', `Generated by /plan-export from ${p.path} on ${today}.`);
  return lines.join('\n');
}

function renderIssueExport(p, target) {
  const lines = [`# ${target} issue drafts - ${p.id}`];
  for (const story of p.stories) {
    lines.push('', `## Issue: [${story.id}] ${story.title || story.objective || story.id}`);
    lines.push('', `Planning: ${p.id}`);
    lines.push(`Risk: ${story.risk || '-'}`);
    lines.push(`Depends on: ${story.dependsOn || '-'}`);
    lines.push(`External ID: ${story.externalIssue || '-'}`);
    lines.push('', '### Description', story.objective || '-');
    lines.push('', '### Acceptance Criteria');
    const criteria = story.doneCriteria?.length ? story.doneCriteria : ['Review story done criteria.'];
    lines.push(...criteria.map((item) => `- ${item}`));
    lines.push('', '### Implementation Notes');
    lines.push(`- Area: ${story.area || '-'}`);
    if (story.tasks?.length) lines.push(`- Tasks: ${story.tasks.map((task) => task.id).join(', ')}`);
  }
  lines.push('', `Generated by /plan-export from ${p.path} on ${today}.`);
  return lines.join('\n');
}

function renderStandup(report) {
  const lines = [`## Standup - ${report.planning.id} - ${today}`, '', `Context: ${report.planning.intent}`, '', '**Yesterday:**'];
  if (report.yesterday.length === 0) lines.push(report.gitActivityFound ? '- No DONE story changes detected.' : '- (no git activity found - status inferred from current story states)');
  else lines.push(...report.yesterday.map((story) => `- [${story.id}] Completed: ${story.objective || story.title}`));
  lines.push('', '**Today:**');
  if (report.todayItems.length === 0) lines.push('- No in-progress or next TODO story found.');
  else lines.push(...report.todayItems.map((story) => `- [${story.id}] ${story.nextUp ? 'Next up' : 'In progress'}: ${story.objective || story.title}`));
  lines.push('', '**Blockers:**');
  if (report.blockers.length === 0) lines.push('- None.');
  else lines.push(...report.blockers.map((story) => `- [${story.id}] BLOCKED: ${story.notes || story.objective || story.title}`));
  lines.push('', 'Read-only: no files were modified.');
  return lines.join('\n');
}

function renderHistory(report) {
  const lines = [`# Planning History - ${report.planningId}`, '', '| Date | Event | Details |', '|------|-------|---------|'];
  if (report.events.length === 0) lines.push('| - | no git history found for this planning | history may be incomplete |');
  else for (const event of report.events) lines.push(`| ${event.date} | ${event.event} | ${event.details} |`);
  if (report.sparse) lines.push('', '(history may be incomplete - status transitions inferred from available diffs)');
  lines.push('', 'Read-only: no files were modified.');
  return lines.join('\n');
}

function render(report) {
  if (output === 'json') return JSON.stringify(report, null, 2);
  if (report.kind === 'status') return renderStatus(report);
  if (report.kind === 'report') return renderReport(report);
  if (report.kind === 'export') return renderExport(report);
  if (report.kind === 'standup') return renderStandup(report);
  if (report.kind === 'history') return renderHistory(report);
  return JSON.stringify(report, null, 2);
}

let report;
try {
  if (command === 'status') report = runStatus();
  else if (command === 'report') report = runReport();
  else if (command === 'export') report = runExport();
  else if (command === 'standup') report = runStandup();
  else if (command === 'history') report = runHistory();
  else failUsage(`Unknown command: ${command}`);
} catch (error) {
  if (output === 'json') console.log(JSON.stringify({ kind: command, error: error.message }, null, 2));
  else console.error(`ERROR: ${error.message}`);
  process.exit(1);
}

console.log(render(report));
