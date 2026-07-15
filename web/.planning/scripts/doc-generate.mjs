#!/usr/bin/env node
import fs from 'node:fs';
import path from 'node:path';

const root = process.cwd();
const planningRoot = path.join(root, '.planning');
const docsRoot = path.join(root, 'docs');
const today = new Date().toISOString().slice(0, 10);
const args = process.argv.slice(2);

let format = 'markdown';
const commandArgs = [];
for (let i = 0; i < args.length; i += 1) {
  if (args[i] === '--format') {
    format = args[i + 1] || 'markdown';
    i += 1;
  } else {
    commandArgs.push(args[i]);
  }
}

if (!['markdown', 'json'].includes(format)) failUsage(`Unsupported format: ${format}`);
if (![1, 2, 3].includes(commandArgs.length)) failUsage('Expected: <NNN-slug> [story-NN] [task-NN]');

const [planningId, storyId, taskId] = commandArgs;
const level = commandArgs.length === 3 ? 'task' : commandArgs.length === 2 ? 'story' : 'planning';

function failUsage(message) {
  console.error(`${message}

Usage:
  node .planning/scripts/doc-generate.mjs <NNN-slug> [story-NN] [task-NN] [--format markdown|json]`);
  process.exit(2);
}

function exists(filePath) {
  return fs.existsSync(filePath);
}

function isFile(filePath) {
  return exists(filePath) && fs.statSync(filePath).isFile();
}

function isDir(filePath) {
  return exists(filePath) && fs.statSync(filePath).isDirectory();
}

function read(filePath) {
  return fs.readFileSync(filePath, 'utf8');
}

function write(filePath, content) {
  fs.mkdirSync(path.dirname(filePath), { recursive: true });
  fs.writeFileSync(filePath, content);
}

function append(filePath, content) {
  fs.mkdirSync(path.dirname(filePath), { recursive: true });
  fs.appendFileSync(filePath, content);
}

function rel(filePath) {
  return path.relative(root, filePath).replaceAll(path.sep, '/');
}

function listFiles(dir, pattern = /.*/) {
  if (!isDir(dir)) return [];
  return fs.readdirSync(dir, { withFileTypes: true })
    .filter((entry) => entry.isFile() && pattern.test(entry.name))
    .map((entry) => path.join(dir, entry.name))
    .sort(compareStoryNumber);
}

function compareStoryNumber(a, b) {
  return storyNumber(path.basename(a)) - storyNumber(path.basename(b)) || a.localeCompare(b);
}

function storyNumber(value) {
  const match = /story-(\d+)/i.exec(value);
  return match ? Number(match[1]) : Number.MAX_SAFE_INTEGER;
}

function section(text, heading) {
  const pattern = new RegExp(`^##\\s+${escapeRegex(heading)}\\s*$`, 'im');
  const match = pattern.exec(text);
  if (!match) return '';
  const rest = text.slice(match.index + match[0].length);
  const next = /^##\s+/im.exec(rest);
  return clean(next ? rest.slice(0, next.index) : rest);
}

function escapeRegex(value) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function clean(value) {
  return (value || '').trim();
}

function firstSentence(value) {
  const text = clean(value).replace(/\s+/g, ' ');
  const match = /^(.+?[.!?])(?:\s|$)/.exec(text);
  return match ? match[1] : text;
}

function firstHeading(text, fallback) {
  const match = /^#\s+(.+)$/m.exec(text);
  return match ? match[1].trim() : fallback;
}

function titleFromFile(filePath, prefixPattern) {
  return path.basename(filePath, '.md')
    .replace(prefixPattern, '')
    .replace(/-/g, ' ')
    .replace(/\b\w/g, (char) => char.toUpperCase());
}

function slugFromFile(filePath, prefixPattern) {
  return path.basename(filePath, '.md').replace(prefixPattern, '');
}

function findPlanning(planning) {
  const candidates = [
    path.join(planningRoot, 'active', planning),
    path.join(planningRoot, planning),
    path.join(planningRoot, 'finished', planning),
  ];
  return candidates.find(isDir) || null;
}

function findStoryFile(planningDir, story) {
  const dir = path.join(planningDir, '02-deepening');
  return listFiles(dir, new RegExp(`^${escapeRegex(story)}-.*\\.md$`, 'i'))[0] || null;
}

function findTaskFile(planningDir, story, task) {
  const dir = path.join(planningDir, '02-deepening');
  if (!isDir(dir)) return null;
  const taskFolder = fs.readdirSync(dir, { withFileTypes: true })
    .filter((entry) => entry.isDirectory() && entry.name.toLowerCase().startsWith(`${story.toLowerCase()}-`))
    .map((entry) => path.join(dir, entry.name))[0];
  if (!taskFolder) return null;
  return listFiles(taskFolder, new RegExp(`^${escapeRegex(task)}-.*\\.md$`, 'i'))[0] || null;
}

function areaFromStory(storyText) {
  const match = /^(?:>\s*)?(?:\*\*)?(?:Repository\s+Area|Area):(?:\*\*)?\s*`?([A-Za-z0-9_-]+)`?/im.exec(storyText);
  return match ? match[1].toUpperCase() : 'unknown';
}

function areaGate(area) {
  return ['DO', 'W'].includes(area);
}

function ensureDocs() {
  fs.mkdirSync(path.join(docsRoot, 'guides'), { recursive: true });
  fs.mkdirSync(path.join(docsRoot, 'adr'), { recursive: true });
  fs.mkdirSync(path.join(docsRoot, 'changelog'), { recursive: true });
}

function splitMarkdownRow(line) {
  const trimmed = line.trim();
  if (!trimmed.startsWith('|') || !trimmed.endsWith('|')) return null;
  return trimmed.slice(1, -1).split('|').map((cell) => cell.trim());
}

function parseTableAfterHeading(text, heading) {
  const lines = text.split(/\r?\n/);
  const headingPattern = new RegExp(`^##\\s+${escapeRegex(heading)}\\s*$`, 'i');
  const headingIndex = lines.findIndex((line) => headingPattern.test(line.trim()));
  if (headingIndex < 0) return [];
  let header = null;
  const rows = [];
  for (let i = headingIndex + 1; i < lines.length; i += 1) {
    if (/^##\s+/.test(lines[i])) break;
    if (!lines[i].trim().startsWith('|')) {
      if (header && rows.length > 0) break;
      continue;
    }
    const cells = splitMarkdownRow(lines[i]);
    if (!cells) continue;
    if (cells.every((cell) => /^:?-{2,}:?$/.test(cell))) continue;
    if (!header) {
      header = cells.map((cell) => cell.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, ''));
      continue;
    }
    const row = { cells };
    header.forEach((name, index) => {
      row[name] = cells[index] || '';
    });
    rows.push(row);
  }
  return rows;
}

function bulletsFromDoneCriteria(text) {
  return section(text, 'Done Criteria')
    .split(/\r?\n/)
    .map((line) => line.replace(/^\s*-\s*\[[ xX]\]\s*/, '').trim())
    .filter(Boolean)
    .slice(0, 6);
}

function taskRows(storyText) {
  return parseTableAfterHeading(storyText, 'Tasks')
    .map((row) => {
      const text = row.task || row.name || row.cells.join(' ');
      const id = (/task-\d+/i.exec(text) || [null])[0];
      return { id, text: text.replace(/\[|\]|\([^)]*\)/g, '').trim() };
    })
    .filter((row) => row.id);
}

function usageFromImplementation(taskText) {
  const steps = section(taskText, 'Implementation Steps');
  if (!steps) return 'Usage is derived from the implemented surface described in the task. Review the linked source files for exact invocation details.';
  return steps
    .split(/\r?\n/)
    .map((line) => line.replace(/^\s*(?:[-*]|\d+\.)\s*/, '').trim())
    .filter(Boolean)
    .slice(0, 6)
    .map((line) => `- ${line}`)
    .join('\n');
}

function exampleFromTask(taskText) {
  const implementation = section(taskText, 'Implementation Steps');
  const technical = section(taskText, 'Technical Design');
  const source = `${implementation}\n${technical}`;
  const endpoint = /\b(GET|POST|PUT|PATCH|DELETE)\s+([/][^\s`]+)/i.exec(source);
  if (endpoint) return `\`${endpoint[1].toUpperCase()} ${endpoint[2]}\``;
  const cli = /\b([a-z0-9_-]+)\s+(--[a-z0-9-]+)/i.exec(source);
  if (cli) return `\`${cli[0]}\``;
  const component = /\b([A-Z][A-Za-z0-9]+)\b/.exec(source);
  if (component) return `Use \`${component[1]}\` through the public interface introduced by this task.`;
  return 'See the implementation and verification evidence for a concrete usage example.';
}

function decisionSentences(text) {
  const indicators = /\b(chose|decided to|over|instead of|alternative discarded|alternative considered|rejected|we avoid)\b/i;
  return text
    .split(/(?<=[.!?])\s+/)
    .map((sentence) => sentence.trim())
    .filter((sentence) => indicators.test(sentence));
}

function alternativeSentences(text) {
  const indicators = /\b(alternative|discarded|rejected|instead of)\b/i;
  return text
    .split(/(?<=[.!?])\s+/)
    .map((sentence) => sentence.trim())
    .filter((sentence) => indicators.test(sentence));
}

function reportBase() {
  return { level, planningId, storyId, taskId, written: [], skipped: [], warnings: [] };
}

function addWritten(report, filePath, action) {
  report.written.push({ path: rel(filePath), action });
}

function addSkipped(report, step, reason) {
  report.skipped.push({ step, reason });
}

function taskLevel(report) {
  const planningDir = findPlanning(planningId);
  if (!planningDir) throw new Error(`Planning ${planningId} not found.`);
  const storyFile = findStoryFile(planningDir, storyId);
  if (!storyFile) throw new Error(`Story ${storyId} not found in ${rel(planningDir)}.`);
  const taskFile = findTaskFile(planningDir, storyId, taskId);
  if (!taskFile) throw new Error(`Task ${taskId} not found under ${storyId}.`);

  const storyText = read(storyFile);
  const taskText = read(taskFile);
  const area = areaFromStory(storyText);
  report.area = area;
  if (areaGate(area)) {
    report.silent = true;
    return report;
  }
  ensureDocs();

  if (area !== 'IN') writeInlineTaskDoc(report, storyText, taskText, taskFile, area);
  else addSkipped(report, 'inline doc', 'area IN uses ADR-only task documentation');

  if (area !== 'WB') writeTaskAdr(report, taskText, taskFile, area);
  else addSkipped(report, 'ADR', 'area WB does not generate task ADRs');

  return report;
}

function writeInlineTaskDoc(report, storyText, taskText, taskFile, area) {
  const target = path.join(docsRoot, 'guides', planningId, storyId, `${taskId}.md`);
  const objective = section(taskText, 'Objective') || firstSentence(taskText);
  const title = titleFromFile(taskFile, /^task-\d+-/i);
  const body = isFile(target)
    ? `\n\n---\n\n## Update - ${today}\n\n**Source:** ${taskId}\n\n### What changed\n${objective}\n\n### How to use it\n${usageFromImplementation(taskText)}\n\n### Example\n${exampleFromTask(taskText)}\n`
    : `# ${title}\n\n**Source:** ${taskId} | **Area:** ${area} | **Date:** ${today}\n\n## What it does\n${objective}\n\n## How to use it\n${usageFromImplementation(taskText)}\n\n## Example\n${exampleFromTask(taskText)}\n`;
  if (isFile(target)) {
    append(target, body);
    addWritten(report, target, 'appended');
  } else {
    write(target, body);
    addWritten(report, target, 'created');
  }
}

function writeTaskAdr(report, taskText, taskFile) {
  const technical = section(taskText, 'Technical Design');
  const decisions = decisionSentences(technical);
  if (!technical || decisions.length === 0) {
    addSkipped(report, 'ADR', 'no architectural decision detected in Technical Design');
    return;
  }
  const taskSlug = slugFromFile(taskFile, /^task-\d+-/i);
  const target = path.join(docsRoot, 'adr', `${today}-${taskSlug}.md`);
  const objective = section(taskText, 'Objective') || titleFromFile(taskFile, /^task-\d+-/i);
  const alternatives = alternativeSentences(technical);
  const body = isFile(target)
    ? `\n\n---\n\n## Revision - ${today}\n\n**Source:** ${taskId}\n\n### Decision\n${decisions.join('\n\n')}\n\n### Alternatives Considered\n${alternatives.length ? alternatives.join('\n\n') : 'None documented'}\n`
    : `# ADR: ${firstSentence(objective)}\n\n**Date:** ${today}\n**Status:** Accepted\n**Planning:** ${planningId} / ${storyId} / ${taskId}\n\n## Context\n${technical}\n\n## Decision\n${decisions.join('\n\n')}\n\n## Consequences\n${sectionField(technical, 'Design notes') || 'Consequences are captured in the task technical design and verification evidence.'}\n\n## Alternatives Considered\n${alternatives.length ? alternatives.join('\n\n') : 'None documented'}\n`;
  if (isFile(target)) {
    append(target, body);
    addWritten(report, target, 'appended');
  } else {
    write(target, body);
    addWritten(report, target, 'created');
  }
}

function sectionField(text, label) {
  const pattern = new RegExp(`^\\s*(?:[-*]\\s*)?(?:\\*\\*)?${escapeRegex(label)}(?:\\*\\*)?:\\s*(.+)$`, 'im');
  const match = pattern.exec(text);
  return match ? match[1].trim() : '';
}

function storyLevel(report) {
  const planningDir = findPlanning(planningId);
  if (!planningDir) throw new Error(`Planning ${planningId} not found.`);
  const storyFile = findStoryFile(planningDir, storyId);
  if (!storyFile) throw new Error(`Story ${storyId} not found in ${rel(planningDir)}.`);
  const storyText = read(storyFile);
  const area = areaFromStory(storyText);
  report.area = area;
  if (areaGate(area)) {
    report.silent = true;
    return report;
  }
  ensureDocs();

  if (!['IN', 'AG'].includes(area)) {
    writeStoryChangelog(report, storyText, storyFile, area);
    writeStoryGuide(report, storyText, storyFile, area);
  } else {
    addSkipped(report, 'changelog', `area ${area} does not generate story changelog`);
    addSkipped(report, 'user guide', `area ${area} does not generate story guide`);
  }

  if (area === 'IN') writeStoryAdrSummary(report, storyText, storyFile);
  return report;
}

function writeStoryChangelog(report, storyText, storyFile, area) {
  const target = path.join(docsRoot, 'changelog', planningId, `${storyId}.md`);
  const title = firstHeading(storyText, titleFromFile(storyFile, /^story-\d+-/i));
  const objective = section(storyText, 'Objective');
  const bullets = [firstSentence(objective), ...bulletsFromDoneCriteria(storyText)].filter(Boolean).slice(0, 6);
  const audience = area === 'WB' ? 'Web users' : area === 'AP' ? 'API consumers' : 'End users and API consumers';
  const changes = bullets.map((item) => `- ${item}`).join('\n') || '- Documentation generated from story evidence.';
  const body = isFile(target)
    ? `\n\n---\n\n## Update - ${today}\n\n### What changed\n${changes}\n\n### Who is affected\n${audience}\n`
    : `# ${title}\n\n**Planning:** ${planningId} | **Date:** ${today} | **Area:** ${area}\n\n## What changed\n${changes}\n\n## Who is affected\n${audience}\n`;
  if (isFile(target)) {
    append(target, body);
    addWritten(report, target, 'appended');
  } else {
    write(target, body);
    addWritten(report, target, 'created');
  }
}

function writeStoryGuide(report, storyText, storyFile, area) {
  const target = path.join(docsRoot, 'guides', planningId, `${storyId}.md`);
  const title = firstHeading(storyText, titleFromFile(storyFile, /^story-\d+-/i));
  const overview = section(storyText, 'Objective') || firstSentence(storyText);
  const taskDocsDir = path.join(docsRoot, 'guides', planningId, storyId);
  const taskDocs = listFiles(taskDocsDir, /^task-\d+.*\.md$/i);
  const related = taskDocs.length
    ? taskDocs.map((file) => `- [${path.basename(file, '.md')}](${storyId}/${path.basename(file)})`).join('\n')
    : 'None';
  const taskSummary = taskRows(storyText).map((task) => `- ${task.text}`).join('\n');
  const docSummaries = taskDocs.map((file) => `### ${firstHeading(read(file), path.basename(file, '.md'))}\n${section(read(file), 'How to use it') || section(read(file), 'What it does')}`).join('\n\n');
  const howTo = docSummaries || taskSummary || 'Follow the story done criteria and task documentation for operational details.';
  const body = isFile(target)
    ? `\n\n---\n\n## Revision - ${today}\n\n### Overview\n${overview}\n\n### How to use\n${howTo}\n\n### Related components\n${related}\n`
    : `# ${title}\n\n**Date:** ${today} | **Area:** ${area}\n\n## Overview\n${overview}\n\n## How to use\n${howTo}\n\n## Related components\n${related}\n`;
  if (isFile(target)) {
    append(target, body);
    addWritten(report, target, 'appended');
  } else {
    write(target, body);
    addWritten(report, target, 'created');
  }
}

function writeStoryAdrSummary(report, storyText, storyFile) {
  const sourceAdrs = listFiles(path.join(docsRoot, 'adr'), /\.md$/i)
    .filter((file) => path.basename(file).toLowerCase().includes(storyId.toLowerCase()) || read(file).includes(`${planningId} / ${storyId}`));
  if (sourceAdrs.length === 0) {
    addSkipped(report, 'consolidated ADR', `no task-level ADRs found for ${storyId}`);
    return;
  }
  const storySlug = slugFromFile(storyFile, /^story-\d+-/i);
  const target = path.join(docsRoot, 'adr', `${today}-${storySlug}.md`);
  const title = firstHeading(storyText, titleFromFile(storyFile, /^story-\d+-/i));
  const decisions = sourceAdrs.map((file) => `### ${path.basename(file)}\n${section(read(file), 'Decision') || section(read(file), 'Decisions Made in This Story') || 'No decision section found.'}`).join('\n\n');
  const consequences = sourceAdrs.map((file) => section(read(file), 'Consequences') || section(read(file), 'Combined Consequences')).filter(Boolean).join('\n\n');
  const body = isFile(target)
    ? `\n\n---\n\n## Update - ${today}\n\n**Source ADRs:** ${sourceAdrs.map((file) => path.basename(file)).join(', ')}\n\n### Decisions Made in This Story\n${decisions}\n\n### Combined Consequences\n${consequences || 'None documented'}\n`
    : `# ADR Summary: ${title}\n\n**Date:** ${today}\n**Status:** Accepted\n**Planning:** ${planningId} / ${storyId}\n**Source ADRs:** ${sourceAdrs.map((file) => path.basename(file)).join(', ')}\n\n## Decisions Made in This Story\n${decisions}\n\n## Combined Consequences\n${consequences || 'None documented'}\n`;
  if (isFile(target)) {
    append(target, body);
    addWritten(report, target, 'appended');
  } else {
    write(target, body);
    addWritten(report, target, 'created');
  }
}

function planningLevel(report) {
  const planningDir = findPlanning(planningId);
  if (!planningDir) throw new Error(`Planning ${planningId} not found.`);
  report.planningPath = rel(planningDir);
  ensureDocs();
  const changelogs = listFiles(path.join(docsRoot, 'changelog', planningId), /^story-\d+.*\.md$/i);
  if (changelogs.length === 0) {
    addSkipped(report, 'release notes', `no story changelog entries; run /doc-generate ${planningId} <story-id> first`);
    return report;
  }
  const releaseTarget = path.join(docsRoot, 'changelog', planningId, 'RELEASE.md');
  write(releaseTarget, `# Release Notes - ${planningId}\n\n**Generated:** ${today}\n\n${changelogs.map((file) => read(file).trim()).join('\n\n---\n\n')}\n`);
  addWritten(report, releaseTarget, 'regenerated');

  const guides = listFiles(path.join(docsRoot, 'guides', planningId), /^story-\d+.*\.md$/i);
  if (guides.length === 0) {
    addSkipped(report, 'user guide index', `no story guides found; run /doc-generate ${planningId} <story-id> first`);
    return report;
  }
  const toc = guides.map((file) => `- [${firstHeading(read(file), path.basename(file, '.md'))}](${path.basename(file)})`).join('\n');
  const guideTarget = path.join(docsRoot, 'guides', planningId, 'README.md');
  write(guideTarget, `# User Guide - ${planningId}\n\n**Generated:** ${today}\n\n## Table of Contents\n${toc}\n\n---\n\n${guides.map((file) => read(file).trim()).join('\n\n---\n\n')}\n`);
  addWritten(report, guideTarget, 'regenerated');
  return report;
}

function renderMarkdown(report) {
  if (report.silent) return '';
  const lines = [`## Documentation Generation Report - ${planningId}`, ''];
  lines.push(`Level: ${report.level}${report.storyId ? ` / ${report.storyId}` : ''}${report.taskId ? ` / ${report.taskId}` : ''}`);
  if (report.area) lines.push(`Area: ${report.area}`);
  if (report.written.length > 0) {
    lines.push('', 'Files written:');
    for (const item of report.written) lines.push(`- ${item.path} (${item.action})`);
  }
  if (report.skipped.length > 0) {
    lines.push('', 'Skipped:');
    for (const item of report.skipped) lines.push(`- ${item.step}: ${item.reason}`);
  }
  if (report.warnings.length > 0) {
    lines.push('', 'Warnings:');
    for (const item of report.warnings) lines.push(`- ${item}`);
  }
  if (report.written.length === 0 && report.skipped.length === 0 && report.warnings.length === 0) {
    lines.push('', 'No files written.');
  }
  return lines.join('\n');
}

let report = reportBase();
try {
  if (level === 'task') report = taskLevel(report);
  else if (level === 'story') report = storyLevel(report);
  else report = planningLevel(report);
} catch (error) {
  report.error = error.message;
  if (format === 'json') console.log(JSON.stringify(report, null, 2));
  else console.error(`ERROR: ${error.message}`);
  process.exit(1);
}

if (format === 'json') {
  console.log(JSON.stringify(report, null, 2));
} else {
  const output = renderMarkdown(report);
  if (output) console.log(output);
}
