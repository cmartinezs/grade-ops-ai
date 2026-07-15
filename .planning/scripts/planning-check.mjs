#!/usr/bin/env node
import { execFileSync } from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';

const VALID_STORY_STATUSES = new Set(['TODO', 'IN PROGRESS', 'DONE', 'BLOCKED', 'SKIPPED', 'STANDBY']);
const VALID_TASK_STATUSES = new Set(['TODO', 'IN PROGRESS', 'DONE', 'BLOCKED']);
const CODE_EXTENSIONS = /\.(java|kt|ts|tsx|js|jsx|mjs|cjs|py|go|cs|rs|php|rb|scala|sql|tf|yaml|yml|json)$/i;
const DB_ORM_PATTERN = /\b(migration|schema|database|db|table|column|index|seed|prisma|typeorm|sequelize|sqlalchemy|hibernate|jpa|entity|model|repository|generated client|persistence|ddl|flyway|liquibase)\b/i;

const root = process.cwd();
const planningRoot = path.join(root, '.planning');

const args = process.argv.slice(2);
let command = args.shift();
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

if (!command || ['-h', '--help', 'help'].includes(command)) {
  usage();
  process.exit(0);
}

if (!['markdown', 'json'].includes(format)) {
  failUsage(`Unsupported format: ${format}`);
}

function usage() {
  console.log(`Usage:
  node .planning/scripts/planning-check.mjs health [--format markdown|json]
  node .planning/scripts/planning-check.mjs validate [NNN-slug] [--format markdown|json]
  node .planning/scripts/planning-check.mjs task-validate <NNN-slug> [story-NN] [task-NN] [--format markdown|json]
  node .planning/scripts/planning-check.mjs us-status [path/to/container] [--format markdown|json]
  node .planning/scripts/planning-check.mjs audit-docs <NNN-slug> [--docs-dir <path>] [--format markdown|json]
  node .planning/scripts/planning-check.mjs doctor [--plugin-root <path>] [--format markdown|json]

Read-only checks for planning structure, story/task indexes, user-story enrichment, generated docs, plugin metadata, and validation gaps.`);
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
    .sort();
}

function walk(dir, predicate = () => true, output = []) {
  if (!isDir(dir)) return output;
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      walk(full, predicate, output);
    } else if (entry.isFile() && predicate(full)) {
      output.push(full);
    }
  }
  return output;
}

function lineOf(text, needle) {
  const lines = text.split(/\r?\n/);
  if (needle instanceof RegExp) {
    const index = lines.findIndex((line) => needle.test(line));
    return index >= 0 ? index + 1 : 1;
  }
  const index = lines.findIndex((line) => line.includes(needle));
  return index >= 0 ? index + 1 : 1;
}

function result(status, message, filePath = null, line = null, suggestion = null) {
  return {
    status,
    message,
    path: filePath ? rel(filePath) : null,
    line,
    suggestion,
  };
}

function add(results, status, message, filePath = null, line = null, suggestion = null) {
  results.push(result(status, message, filePath, line, suggestion));
}

function section(text, heading) {
  const pattern = new RegExp(`^##\\s+${escapeRegex(heading)}\\s*$`, 'im');
  const match = pattern.exec(text);
  if (!match) return '';
  const start = match.index + match[0].length;
  const rest = text.slice(start);
  const next = /^##\s+/im.exec(rest);
  return next ? rest.slice(0, next.index) : rest;
}

function hasHeading(text, heading) {
  return new RegExp(`^##\\s+${escapeRegex(heading)}\\s*$`, 'im').test(text);
}

function hasSubheading(text, heading) {
  return new RegExp(`^###\\s+${escapeRegex(heading)}\\s*$`, 'im').test(text);
}

function escapeRegex(value) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
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
    const row = { line: i + 1, cells };
    header.forEach((name, index) => {
      row[name] = cells[index] || '';
    });
    rows.push(row);
  }
  return rows;
}

function normalizeHeader(value) {
  return value.toLowerCase().replace(/`/g, '').replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '');
}

function extractStatus(text) {
  const match = /^>\s*\*\*Status:\*\*\s*(.+?)\s*$/im.exec(text);
  return match ? match[1].trim() : 'UNKNOWN';
}

function extractField(text, names) {
  for (const name of names) {
    const pattern = new RegExp(`^(?:>\\s*)?\\*\\*${escapeRegex(name)}:\\*\\*\\s*(.+)$|^${escapeRegex(name)}:\\s*(.+)$`, 'im');
    const match = pattern.exec(text);
    if (match) return (match[1] || match[2] || '').trim();
  }
  return '';
}

function storyIdFromValue(value) {
  const match = /story-\d+/i.exec(value || '');
  return match ? match[0].toLowerCase() : null;
}

function taskIdFromValue(value) {
  const match = /task-\d+/i.exec(value || '');
  return match ? match[0].toLowerCase() : null;
}

function idPrefix(name) {
  const match = /^(\d{3})-/.exec(name);
  return match ? match[1] : null;
}

function planningNameMatches(name) {
  return /^\d{3}-/.test(name);
}

function discoverPlannings() {
  const locations = [
    { name: 'initial', dir: planningRoot },
    { name: 'active', dir: path.join(planningRoot, 'active') },
    { name: 'finished', dir: path.join(planningRoot, 'finished') },
  ];
  const plannings = [];
  for (const location of locations) {
    for (const dir of listDirs(location.dir)) {
      const name = path.basename(dir);
      if (planningNameMatches(name)) {
        plannings.push({ id: name, prefix: idPrefix(name), location: location.name, dir });
      }
    }
  }
  return plannings;
}

function locatePlanning(planningId, allowFinished = true) {
  const candidates = [
    { location: 'active', dir: path.join(planningRoot, 'active', planningId) },
    { location: 'finished', dir: path.join(planningRoot, 'finished', planningId) },
    { location: 'initial', dir: path.join(planningRoot, planningId) },
  ].filter((candidate) => allowFinished || candidate.location !== 'finished');
  return candidates.find((candidate) => isDir(candidate.dir)) || null;
}

function workflowCatalog() {
  const readme = path.join(planningRoot, 'WORKFLOWS', 'README.md');
  const ids = new Set();
  if (!isFile(readme)) return ids;
  const text = read(readme);
  for (const match of text.matchAll(/\((?:[^)]*\/)?([A-Z][A-Z-]+)\.md\)/g)) {
    ids.add(match[1]);
  }
  for (const match of text.matchAll(/\|\s*\[?([A-Z][A-Z-]+)\]?/g)) {
    ids.add(match[1]);
  }
  return ids;
}

function readConfig() {
  const configPath = path.join(planningRoot, 'config.yml');
  const config = {
    projectType: 'software',
    requiresGit: true,
    smokeTestsFile: 'SMOKE-TESTS.md',
  };
  if (!isFile(configPath)) return config;
  const text = read(configPath);
  const projectType = /^\s*type:\s*([a-zA-Z0-9_-]+)/m.exec(sectionFromYaml(text, 'project'));
  const requiresGit = /^\s*requires_git:\s*(true|false)/m.exec(sectionFromYaml(text, 'execution'));
  const smokeTestsFile = /^\s*smoke_tests_file:\s*(.+)$/m.exec(sectionFromYaml(text, 'software'));
  if (projectType) config.projectType = projectType[1];
  if (requiresGit) config.requiresGit = requiresGit[1] === 'true';
  if (smokeTestsFile) config.smokeTestsFile = smokeTestsFile[1].trim().replace(/^['"]|['"]$/g, '');
  return config;
}

function sectionFromYaml(text, key) {
  const lines = text.split(/\r?\n/);
  const start = lines.findIndex((line) => line.trim() === `${key}:`);
  if (start < 0) return '';
  const out = [];
  for (let i = start + 1; i < lines.length; i += 1) {
    if (/^\S/.test(lines[i]) && lines[i].trim().endsWith(':')) break;
    out.push(lines[i]);
  }
  return out.join('\n');
}

function storyFileFor(planningDir, storyId) {
  const deepeningDir = path.join(planningDir, '02-deepening');
  return listFiles(deepeningDir, new RegExp(`^${escapeRegex(storyId)}-.*\\.md$`, 'i'))[0] || null;
}

function taskFolderFor(planningDir, storyId) {
  const deepeningDir = path.join(planningDir, '02-deepening');
  return listDirs(deepeningDir).find((dir) => path.basename(dir).toLowerCase().startsWith(`${storyId.toLowerCase()}-`)) || null;
}

function taskFileFor(storyTaskDir, taskId) {
  return listFiles(storyTaskDir, new RegExp(`^${escapeRegex(taskId)}-.*\\.md$`, 'i'))[0] || null;
}

function rowsByStoryId(expansionText) {
  const rows = parseTableAfterHeading(expansionText, 'Story Summary');
  return rows.map((row) => ({
    row,
    id: storyIdFromValue(row.story || row.id || row.name || row.cells.join(' ')),
    status: row.status || '',
    dependsOn: row.depends-on || row.dependencies || row.depends || '',
  })).filter((item) => item.id);
}

function rowsByTaskId(storyText) {
  const rows = parseTableAfterHeading(storyText, 'Tasks');
  return rows.map((row) => ({
    row,
    id: taskIdFromValue(row.task || row.id || row.name || row.cells.join(' ')),
    status: row.status || '',
    workflow: row.workflow || '',
    dependsOn: row.depends-on || row.dependencies || row.depends || '',
  })).filter((item) => item.id);
}

function dependencies(value, kind) {
  const pattern = kind === 'story' ? /story-\d+/gi : /task-\d+/gi;
  if (!value || /^(-|none|n\/a|na)$/i.test(value.trim())) return [];
  return [...value.matchAll(pattern)].map((match) => match[0].toLowerCase());
}

function detectCycle(nodes, edges) {
  const visiting = new Set();
  const visited = new Set();
  const visit = (node) => {
    if (visiting.has(node)) return true;
    if (visited.has(node)) return false;
    visiting.add(node);
    for (const next of edges.get(node) || []) {
      if (nodes.has(next) && visit(next)) return true;
    }
    visiting.delete(node);
    visited.add(node);
    return false;
  };
  for (const node of nodes) {
    if (visit(node)) return true;
  }
  return false;
}

function allDoneCriteriaChecked(text) {
  const done = section(text, 'Done Criteria');
  const checkboxes = done.split(/\r?\n/).filter((line) => /^\s*-\s*\[[ xX]\]/.test(line));
  return checkboxes.length > 0 && checkboxes.every((line) => /^\s*-\s*\[[xX]\]/.test(line));
}

function hasUncheckedDoneCriteria(text) {
  return section(text, 'Done Criteria').split(/\r?\n/).some((line) => /^\s*-\s*\[ \]/.test(line));
}

function placeholderSeverity(text, finished) {
  const placeholders = /\[(?:Task description|Story Name|WORKFLOW-NAME|Task Name|Step naming|Decision Title|State the decision|Describe the context)/i;
  if (!placeholders.test(text)) return null;
  return finished ? 'FAIL' : 'WARN';
}

function isCodeTask(text) {
  return CODE_EXTENSIONS.test(text) || /\b(code|api|endpoint|service|controller|component|repository|migration|schema|database|cli|worker|adapter|module)\b/i.test(text);
}

function isDbOrmTask(text) {
  return DB_ORM_PATTERN.test(text);
}

function validateRequiredPlanningFiles(planning, results) {
  const initial = path.join(planning.dir, '00-initial.md');
  const expansion = path.join(planning.dir, '01-expansion.md');
  const traceability = path.join(planning.dir, 'TRACEABILITY.md');
  const deepening = path.join(planning.dir, '02-deepening');
  const retrospectiveRaw = path.join(planning.dir, 'RETROSPECTIVE-RAW.md');
  if (!isFile(initial)) add(results, 'FAIL', 'missing 00-initial.md', initial, null, 'Restore the initial planning artifact.');
  if (['active', 'finished'].includes(planning.location) && !isFile(expansion)) add(results, 'FAIL', 'missing 01-expansion.md', expansion);
  if (['active', 'finished'].includes(planning.location) && !isFile(traceability)) add(results, 'FAIL', 'missing TRACEABILITY.md', traceability);
  if (['active', 'finished'].includes(planning.location) && !isFile(retrospectiveRaw)) add(results, 'WARN', 'missing RETROSPECTIVE-RAW.md for retrospective capture', retrospectiveRaw, null, 'Create it with /plan-edge-case or /plan-retrospective.');
  if (planning.location === 'initial' && (isFile(expansion) || isDir(deepening))) {
    add(results, 'FAIL', 'initial planning contains expansion/deepening artifacts and should be moved to active', planning.dir, null, '/plan-expand should own this transition.');
  }
  if (planning.location === 'active' && isFile(expansion) && !isDir(deepening)) {
    add(results, 'WARN', 'active planning has no 02-deepening directory yet', deepening);
  }
  for (const pdr of listFiles(planning.dir, /^pdr-.*\.md$/i)) {
    const text = read(pdr);
    if (/\[(Decision Title|State the decision|Describe the context)/i.test(text)) {
      add(results, 'WARN', 'PDR still contains template placeholders', pdr, lineOf(text, /\[(Decision Title|State the decision|Describe the context)/i), 'Complete it with /plan-decision or remove the empty legacy file.');
    }
  }
}

function validatePlanning(planning, catalog, config) {
  const results = [];
  validateRequiredPlanningFiles(planning, results);
  const expansionFile = path.join(planning.dir, '01-expansion.md');
  if (!isFile(expansionFile)) return results;

  const expansionText = read(expansionFile);
  const storyRows = rowsByStoryId(expansionText);
  const storyIds = new Set(storyRows.map((item) => item.id));
  const storyEdges = new Map(storyRows.map((item) => [item.id, dependencies(item.dependsOn, 'story')]));

  for (const item of storyRows) {
    if (item.status && !VALID_STORY_STATUSES.has(item.status.toUpperCase())) {
      add(results, 'WARN', `invalid story status "${item.status}" for ${item.id}`, expansionFile, item.row.line, 'Normalize to TODO, IN PROGRESS, DONE, BLOCKED, SKIPPED, or STANDBY.');
    }
    for (const dep of dependencies(item.dependsOn, 'story')) {
      if (!storyIds.has(dep)) add(results, 'FAIL', `${item.id} depends on unknown story ${dep}`, expansionFile, item.row.line);
    }
  }
  if (detectCycle(storyIds, storyEdges)) add(results, 'FAIL', 'story dependencies contain a cycle', expansionFile);

  const deepeningDir = path.join(planning.dir, '02-deepening');
  const storyFiles = listFiles(deepeningDir, /^story-\d+-.*\.md$/i);
  const fileStoryIds = new Set(storyFiles.map((file) => storyIdFromValue(path.basename(file))).filter(Boolean));
  const shouldHaveStoryFiles = planning.location !== 'initial' && isDir(deepeningDir);

  if (shouldHaveStoryFiles) {
    for (const item of storyRows) {
      if (!fileStoryIds.has(item.id)) add(results, 'FAIL', `${item.id} is listed in Story Summary but has no story file`, expansionFile, item.row.line, `/plan-enrich-story ${planning.id} ${item.id}`);
    }
  }
  for (const file of storyFiles) {
    const sid = storyIdFromValue(path.basename(file));
    if (sid && !storyIds.has(sid)) add(results, 'FAIL', `${sid} story file has no row in Story Summary`, file, 1);
  }

  for (const storyFile of storyFiles) {
    validateStoryFile(planning, storyFile, storyRows.find((item) => item.id === storyIdFromValue(path.basename(storyFile))), catalog, config, results);
  }

  for (const item of storyRows) {
    const blockers = dependencies(item.dependsOn, 'story').filter((dep) => {
      const depStatus = storyRows.find((row) => row.id === dep)?.status.toUpperCase();
      return depStatus && !['DONE', 'SKIPPED'].includes(depStatus);
    });
    if (['IN PROGRESS', 'DONE', 'STANDBY'].includes(item.status.toUpperCase()) && blockers.length > 0) {
      add(results, 'WARN', `${item.id} is ${item.status} but depends on unfinished stories: ${blockers.join(', ')}`, expansionFile, item.row.line);
    }
  }

  return results;
}

function validateStoryFile(planning, storyFile, summaryRow, catalog, config, results) {
  const text = read(storyFile);
  const storyId = storyIdFromValue(path.basename(storyFile));
  const status = extractStatus(text);
  for (const required of ['Objective', 'Tasks', 'Done Criteria']) {
    if (!hasHeading(text, required)) add(results, 'FAIL', `${storyId} missing ## ${required}`, storyFile);
  }
  if (status !== 'UNKNOWN' && !VALID_STORY_STATUSES.has(status.toUpperCase())) {
    add(results, 'WARN', `${storyId} has invalid status "${status}"`, storyFile, lineOf(text, /^>\s*\*\*Status:\*\*/i));
  }
  if (summaryRow && summaryRow.status && status !== 'UNKNOWN' && summaryRow.status.toUpperCase() !== status.toUpperCase()) {
    add(results, 'FAIL', `${storyId} status mismatch: Story Summary=${summaryRow.status}, file=${status}`, storyFile, lineOf(text, /^>\s*\*\*Status:\*\*/i), `/plan-done ${planning.id} ${storyId}`);
  }
  if (status.toUpperCase() === 'DONE' && !allDoneCriteriaChecked(text)) {
    add(results, 'FAIL', `${storyId} is DONE with unchecked done criteria`, storyFile, lineOf(text, '## Done Criteria'));
  }
  if (status.toUpperCase() === 'SKIPPED' && !/skip|skipped|reason/i.test(text)) {
    add(results, 'FAIL', `${storyId} is SKIPPED without a skipped reason`, storyFile, lineOf(text, /^>\s*\*\*Status:\*\*/i));
  }
  const placeholder = placeholderSeverity(text, planning.location === 'finished');
  if (placeholder) add(results, placeholder, `${storyId} contains template placeholder text`, storyFile, lineOf(text, /\[[^\]]+\]/));

  const taskRows = rowsByTaskId(text);
  for (const item of taskRows) {
    if (item.workflow && !catalog.has(item.workflow)) add(results, 'FAIL', `${item.id} references unknown workflow ${item.workflow}`, storyFile, item.row.line);
  }
  validateSequentialIds(taskRows.map((item) => item.id), 'task', storyFile, results);

  const taskDir = taskFolderFor(planning.dir, storyId);
  const allTasksDone = taskRows.length > 0 && taskRows.every((item) => item.status.toUpperCase() === 'DONE');
  if (allTasksDone && status.toUpperCase() !== 'DONE') {
    add(results, 'WARN', `${storyId} has all indexed tasks DONE but story status is ${status}`, storyFile, lineOf(text, /^>\s*\*\*Status:\*\*/i), `/plan-done ${planning.id} ${storyId}`);
  }
  if (taskDir) validateAtomizedStory(planning, storyFile, taskDir, taskRows, catalog, config, results);

  if (/TRACEABILITY/i.test(section(text, 'Done Criteria')) && allDoneCriteriaChecked(text)) {
    const traceability = path.join(planning.dir, 'TRACEABILITY.md');
    if (!isFile(traceability) || read(traceability).trim().length === 0) {
      add(results, 'WARN', `${storyId} done criteria references traceability but TRACEABILITY.md is missing or empty`, traceability);
    }
  }
}

function validateAtomizedStory(planning, storyFile, taskDir, taskRows, catalog, config, results, targetTaskId = null) {
  const storyText = read(storyFile);
  const storyId = storyIdFromValue(path.basename(storyFile));
  const indexedIds = new Set(taskRows.map((item) => item.id));
  const taskFiles = listFiles(taskDir, /^task-\d+-.*\.md$/i);
  const taskFileIds = new Set(taskFiles.map((file) => taskIdFromValue(path.basename(file))).filter(Boolean));

  for (const item of taskRows) {
    if (!taskFileIds.has(item.id)) add(results, 'FAIL', `${item.id} is indexed but has no task file`, storyFile, item.row.line);
  }
  for (const file of taskFiles) {
    const tid = taskIdFromValue(path.basename(file));
    if (tid && !indexedIds.has(tid)) add(results, 'FAIL', `${tid} task file has no row in story task index`, file, 1);
  }
  validateSequentialIds([...taskFileIds], 'task', taskDir, results);

  const taskEdges = new Map();
  const taskStatuses = new Map();
  for (const file of taskFiles) {
    const taskId = taskIdFromValue(path.basename(file));
    if (!taskId || (targetTaskId && taskId !== targetTaskId)) continue;
    const text = read(file);
    const status = extractStatus(text);
    taskStatuses.set(taskId, status);
    const dependsOn = extractField(text, ['Depends On', 'Depends on']) || taskRows.find((row) => row.id === taskId)?.dependsOn || '';
    taskEdges.set(taskId, dependencies(dependsOn, 'task'));
    validateTaskFile(planning, storyFile, file, taskRows.find((row) => row.id === taskId), catalog, config, results);
  }
  for (const [taskId, deps] of taskEdges.entries()) {
    for (const dep of deps) {
      if (!taskFileIds.has(dep)) add(results, 'FAIL', `${taskId} depends on unknown task ${dep}`, taskFileFor(taskDir, taskId) || taskDir);
      const currentNum = Number(taskId.replace('task-', ''));
      const depNum = Number(dep.replace('task-', ''));
      if (depNum >= currentNum) add(results, 'FAIL', `${taskId} depends on ${dep}, which is not an earlier task`, taskFileFor(taskDir, taskId) || taskDir);
      const depStatus = taskStatuses.get(dep) || extractStatusFromTaskFile(taskDir, dep);
      const status = taskStatuses.get(taskId);
      if (['IN PROGRESS', 'DONE'].includes((status || '').toUpperCase()) && depStatus.toUpperCase() !== 'DONE') {
        add(results, 'WARN', `${taskId} is ${status} but dependency ${dep} is ${depStatus}`, taskFileFor(taskDir, taskId) || taskDir);
      }
    }
  }
  if (detectCycle(taskFileIds, taskEdges)) add(results, 'FAIL', `${storyId} task dependencies contain a cycle`, storyFile);

  const storyStatus = extractStatus(storyText);
  if (storyStatus.toUpperCase() === 'DONE') {
    const pending = [...taskFileIds].filter((taskId) => extractStatusFromTaskFile(taskDir, taskId).toUpperCase() !== 'DONE');
    if (pending.length > 0) add(results, 'FAIL', `${storyId} is DONE but tasks are not DONE: ${pending.join(', ')}`, storyFile, lineOf(storyText, /^>\s*\*\*Status:\*\*/i));
  }

  const dbTasks = taskFiles.filter((file) => isDbOrmTask(read(file)));
  if (dbTasks.length > 0) {
    const validationTask = taskFiles.find((file) => {
      const text = read(file);
      return /database\s*\/\s*orm|db\s*\/\s*orm|persistence smoke|static db/i.test(text)
        && dependencies(extractField(text, ['Depends On', 'Depends on']), 'task').length > 0;
    });
    if (!validationTask) {
      add(results, 'FAIL', `${storyId} has DB/ORM change tasks but no later explicit DB/ORM validation task`, storyFile);
    }
  }
}

function validateTaskFile(planning, storyFile, taskFile, taskRow, catalog, config, results) {
  const text = read(taskFile);
  const taskId = taskIdFromValue(path.basename(taskFile));
  const status = extractStatus(text);
  for (const required of ['Objective', 'Technical Design', 'Implementation Steps', 'Done Criteria']) {
    if (!hasHeading(text, required)) add(results, 'FAIL', `${taskId} missing ## ${required}`, taskFile);
  }
  if (!hasHeading(text, 'Verification') && !hasHeading(text, 'Unit Tests')) {
    add(results, 'FAIL', `${taskId} missing ## Verification or legacy ## Unit Tests`, taskFile);
  }
  if (status !== 'UNKNOWN' && !VALID_TASK_STATUSES.has(status.toUpperCase())) {
    add(results, 'FAIL', `${taskId} has invalid task status "${status}"`, taskFile, lineOf(text, /^>\s*\*\*Status:\*\*/i));
  }
  if (taskRow?.status && status !== 'UNKNOWN' && taskRow.status.toUpperCase() !== status.toUpperCase()) {
    add(results, 'FAIL', `${taskId} status mismatch: story index=${taskRow.status}, file=${status}`, taskFile, lineOf(text, /^>\s*\*\*Status:\*\*/i));
  }
  const workflow = extractField(text, ['Workflow']) || taskRow?.workflow || '';
  if (workflow && !catalog.has(workflow)) add(results, 'FAIL', `${taskId} references unknown workflow ${workflow}`, taskFile, lineOf(text, /Workflow/i));
  const placeholder = placeholderSeverity(text, ['IN PROGRESS', 'DONE'].includes(status.toUpperCase()));
  if (placeholder) add(results, placeholder, `${taskId} contains template placeholder text`, taskFile, lineOf(text, /\[[^\]]+\]/));
  if (status.toUpperCase() === 'DONE' && hasUncheckedDoneCriteria(text)) {
    add(results, 'FAIL', `${taskId} is DONE with unchecked done criteria`, taskFile, lineOf(text, '## Done Criteria'));
  }
  if (section(text, 'Verification').trim().length === 0 && !hasHeading(text, 'Unit Tests')) {
    add(results, 'FAIL', `${taskId} has an empty Verification section`, taskFile, lineOf(text, '## Verification'));
  }
  if (/\b(and|plus|also)\b/i.test(section(text, 'Objective'))) {
    add(results, 'WARN', `${taskId} objective may name more than one deliverable`, taskFile, lineOf(text, '## Objective'), 'Consider splitting if the deliverables are independently verifiable.');
  }
  if (/\b(works well|reasonable|properly|as expected)\b/i.test(section(text, 'Done Criteria'))) {
    add(results, 'WARN', `${taskId} has non-binary done criteria wording`, taskFile, lineOf(text, '## Done Criteria'));
  }

  if (config.projectType === 'software') {
    const done = section(text, 'Done Criteria');
    const codeTask = isCodeTask(text);
    if (!hasSubheading(text, 'Software Smoke Test Check') || !/smoke|startup|build|connectivity|schema/i.test(done) || !/human.*review|PR review|developer review/i.test(done)) {
      add(results, 'FAIL', `${taskId} missing software smoke or human review criteria`, taskFile, lineOf(text, '## Done Criteria'));
    }
    if (config.requiresGit && (!/PR/i.test(done) || !/correction|same PR|push/i.test(done))) {
      add(results, 'FAIL', `${taskId} missing git PR publication/correction done criteria`, taskFile, lineOf(text, '## Done Criteria'));
    }
    if (!hasSubheading(text, 'Logging / Observability') || (codeTask && !/correlation|trace|INFO|DEBUG|WARN|ERROR|level/i.test(done))) {
      add(results, codeTask ? 'FAIL' : 'WARN', `${taskId} missing logging/observability criteria`, taskFile, lineOf(text, '## Done Criteria'));
    }
    if (!hasSubheading(text, 'Generated Test Suite') || !/generated|test-suite|quality gate|acceptance dependency/i.test(done)) {
      add(results, 'FAIL', `${taskId} missing generated test-suite gate evidence criteria`, taskFile, lineOf(text, '## Done Criteria'));
    }
    if (isDbOrmTask(text) && (!hasSubheading(text, 'Database / ORM Consistency Check') || !/static.*(db|database|orm)|runtime.*(persistence|smoke)|persistence smoke/i.test(done))) {
      add(results, 'FAIL', `${taskId} missing DB/ORM static consistency and runtime persistence smoke criteria`, taskFile, lineOf(text, '## Done Criteria'));
    }
  }
}

function extractStatusFromTaskFile(taskDir, taskId) {
  const file = taskFileFor(taskDir, taskId);
  return file ? extractStatus(read(file)) : 'UNKNOWN';
}

function validateSequentialIds(ids, kind, filePath, results) {
  const numbers = ids.map((id) => Number(id.replace(`${kind}-`, ''))).filter((num) => Number.isFinite(num)).sort((a, b) => a - b);
  const unique = new Set(numbers);
  if (unique.size !== numbers.length) add(results, 'FAIL', `${kind} numbers are duplicated`, filePath);
  for (let i = 0; i < numbers.length; i += 1) {
    if (numbers[i] !== i + 1) {
      add(results, 'FAIL', `${kind} numbers are not sequential from ${kind}-01`, filePath);
      break;
    }
  }
}

function runHealth() {
  if (!isDir(planningRoot)) return baseReport('health', [result('FAIL', 'No .planning/ directory found. Run /plan-init first.', planningRoot)], {});
  const plannings = discoverPlannings();
  const results = [];
  const counts = countBy(plannings, (planning) => planning.location);

  const byPrefix = groupBy(plannings, (planning) => planning.prefix);
  for (const [prefix, group] of byPrefix.entries()) {
    if (prefix && group.length > 1) add(results, 'FAIL', `duplicate planning prefix ${prefix}: ${group.map((item) => item.id).join(', ')}`);
  }

  for (const planning of plannings) {
    const expansion = path.join(planning.dir, '01-expansion.md');
    if (planning.location === 'initial' && isFile(expansion)) add(results, 'WARN', `${planning.id} is in .planning/ but has 01-expansion.md`, planning.dir, null, `/plan-expand ${planning.id}`);
    if (planning.location === 'active' && activePlanningAllStoriesDone(planning)) add(results, 'WARN', `${planning.id} is active but all stories appear DONE/SKIPPED`, planning.dir, null, `/plan-archive ${planning.id}`);
    if (!isFile(path.join(planning.dir, '00-initial.md'))) add(results, 'FAIL', `${planning.id} has no 00-initial.md`, planning.dir);
  }

  for (const planning of plannings.filter((item) => item.location === 'active')) {
    const expansion = path.join(planning.dir, '01-expansion.md');
    if (isFile(expansion)) {
      const storyIds = rowsByStoryId(read(expansion)).map((item) => item.id);
      for (const storyId of storyIds) {
        if (!storyFileFor(planning.dir, storyId)) add(results, 'FAIL', `${planning.id} lists ${storyId} but the story file is missing`, expansion, lineOf(read(expansion), storyId));
      }
    }
    const days = daysSinceGitActivity(rel(planning.dir));
    if (days !== null && days > 30) add(results, 'WARN', `${planning.id} has no git activity in ${days} days`, planning.dir);
  }

  for (const planning of plannings) {
    const deepening = path.join(planning.dir, '02-deepening');
    for (const dir of listDirs(deepening).filter((item) => /^story-\d+-/i.test(path.basename(item)))) {
      const storyId = storyIdFromValue(path.basename(dir));
      if (storyId && !storyFileFor(planning.dir, storyId)) add(results, 'FAIL', `${rel(dir)} has no matching ${storyId}-*.md story file`, dir);
    }
  }

  const readme = path.join(planningRoot, 'README.md');
  if (isFile(readme)) {
    const text = read(readme);
    for (const planning of plannings) {
      if (!text.includes(planning.id)) add(results, 'WARN', `${planning.id} is not listed in .planning/README.md`, readme);
    }
    for (const match of text.matchAll(/\b(\d{3}-[a-z0-9-]+)\b/gi)) {
      const id = match[1];
      if (!plannings.some((planning) => planning.id === id)) add(results, 'WARN', `.planning/README.md references missing planning ${id}`, readme, lineOf(text, id));
    }
  }

  return baseReport('health', results, {
    planningsFound: plannings.length,
    initial: counts.initial || 0,
    active: counts.active || 0,
    finished: counts.finished || 0,
  });
}

function activePlanningAllStoriesDone(planning) {
  const expansion = path.join(planning.dir, '01-expansion.md');
  if (!isFile(expansion)) return false;
  const rows = rowsByStoryId(read(expansion));
  return rows.length > 0 && rows.every((row) => ['DONE', 'SKIPPED'].includes(row.status.toUpperCase()));
}

function daysSinceGitActivity(relativePath) {
  try {
    const out = execFileSync('git', ['log', '--since=30 days ago', '--format=%ct', '--', relativePath], { cwd: root, encoding: 'utf8', stdio: ['ignore', 'pipe', 'ignore'] }).trim();
    if (out) return 0;
    const last = execFileSync('git', ['log', '-1', '--format=%ct', '--', relativePath], { cwd: root, encoding: 'utf8', stdio: ['ignore', 'pipe', 'ignore'] }).trim();
    if (!last) return null;
    return Math.floor((Date.now() / 1000 - Number(last)) / 86400);
  } catch {
    return null;
  }
}

function optionValue(args, name, fallback = null) {
  const index = args.indexOf(name);
  if (index < 0) return fallback;
  return args[index + 1] || fallback;
}

function positionalArgs(args, optionsWithValues = []) {
  const out = [];
  for (let i = 0; i < args.length; i += 1) {
    if (optionsWithValues.includes(args[i])) {
      i += 1;
    } else {
      out.push(args[i]);
    }
  }
  return out;
}

function markdownFilesForTarget(targetValue) {
  const target = path.resolve(root, targetValue || '.');
  if (isFile(target)) return /\.md$/i.test(target) ? [target] : [];
  if (!isDir(target)) return [];
  const direct = listFiles(target, /\.md$/i);
  if (direct.length > 0) return direct;
  return walk(target, (file) => /\.md$/i.test(file)).sort();
}

function isUserStory(text, filePath) {
  return /##\s+Acceptance Criteria/i.test(text)
    || /^\s*(?:##\s+)?As an?\b/im.test(text)
    || /\*\*As an?\*\*/i.test(text)
    || /\bUS-\d+\b/i.test(text)
    || /\bUS-\d+\b/i.test(path.basename(filePath));
}

function firstHeading(text, fallback) {
  const match = /^#\s+(.+)$/m.exec(text);
  return match ? match[1].trim() : fallback;
}

function storyStatusFor(filePath) {
  const text = read(filePath);
  const id = (/\bUS-\d+\b/i.exec(text) || /\bUS-\d+\b/i.exec(path.basename(filePath)) || [null])[0];
  const title = firstHeading(text, path.basename(filePath, path.extname(filePath)));
  return {
    filePath,
    id: id ? id.toUpperCase() : path.basename(filePath, path.extname(filePath)),
    title,
    dod: /##\s+(Definition of Done|DoD)\b/i.test(text),
    technicalNotes: /##\s+Technical Notes\b/i.test(text),
    dependencies: /##\s+Dependencies\b/i.test(text),
    complexity: /\bComplexity\b\s*[:|#-]/i.test(text),
    planning: linkedPlanningForStory(id, filePath),
  };
}

function linkedPlanningForStory(storyId, filePath) {
  const activeRoot = path.join(planningRoot, 'active');
  if (!isDir(activeRoot)) return '';
  const needles = [storyId, path.basename(filePath), rel(filePath)].filter(Boolean).map((item) => item.toLowerCase());
  for (const dir of listDirs(activeRoot)) {
    const expansion = path.join(dir, '01-expansion.md');
    if (!isFile(expansion)) continue;
    const text = read(expansion).toLowerCase();
    if (needles.some((needle) => text.includes(needle))) return path.basename(dir);
  }
  return '';
}

function runUsStatus() {
  const targetValue = positionalArgs(commandArgs)[0] || '.';
  const files = markdownFilesForTarget(targetValue);
  const storyFiles = files.filter((file) => isUserStory(read(file), file));
  const stories = storyFiles.map(storyStatusFor);
  return baseReport('us-status', [], {
    target: targetValue,
    scannedFiles: files.length,
    stories,
    summary: {
      stories: stories.length,
      dod: stories.filter((story) => story.dod).length,
      technicalNotes: stories.filter((story) => story.technicalNotes).length,
      dependencies: stories.filter((story) => story.dependencies).length,
      complexity: stories.filter((story) => story.complexity).length,
      linked: stories.filter((story) => story.planning).length,
    },
  });
}

function latestMtime(paths) {
  let latest = 0;
  for (const filePath of paths.filter(isFile)) {
    latest = Math.max(latest, fs.statSync(filePath).mtimeMs);
  }
  return latest;
}

function docRootFromConfig(fallback = 'docs') {
  const configPath = path.join(planningRoot, 'config.yml');
  if (!isFile(configPath)) return fallback;
  const docsSection = sectionFromYaml(read(configPath), 'docs');
  const outputDir = /^\s*output_dir:\s*(.+)$/m.exec(docsSection);
  return outputDir ? outputDir[1].trim().replace(/^['"]|['"]$/g, '') : fallback;
}

function normalizeDocRef(ref, sourceFile, docsRoot) {
  if (!ref || /^https?:\/\//i.test(ref)) return null;
  const clean = ref.split('#')[0].replace(/^['"`(<]+|['"`)>]+$/g, '');
  if (!clean || !/\.md$/i.test(clean)) return null;
  if (/^(docs|documentation|adr|adrs)\//i.test(clean) || /^(README|CHANGELOG)\.md$/i.test(clean)) return path.resolve(root, clean);
  const resolved = path.resolve(path.dirname(sourceFile), clean);
  const docsPath = path.resolve(root, docsRoot);
  if (resolved === docsPath || resolved.startsWith(`${docsPath}${path.sep}`)) return resolved;
  return null;
}

function expectedDocsFromPlanning(planning, docsRoot) {
  const files = walk(planning.dir, (file) => /\.md$/i.test(file)).sort();
  const docs = new Map();
  const markdownLink = /\[[^\]]+\]\(([^)]+\.md(?:#[^)]+)?)\)/gi;
  const bareDocPath = /\b(?:docs|documentation|adr|adrs)\/[A-Za-z0-9._/-]+\.md\b/gi;
  const rootDocPath = /\b(?:README|CHANGELOG)\.md\b/g;
  for (const file of files) {
    const text = read(file);
    const refs = [
      ...[...text.matchAll(markdownLink)].map((match) => match[1]),
      ...[...text.matchAll(bareDocPath)].map((match) => match[0]),
      ...[...text.matchAll(rootDocPath)].map((match) => match[0]),
    ];
    for (const ref of refs) {
      const docPath = normalizeDocRef(ref, file, docsRoot);
      if (docPath && !docs.has(docPath)) docs.set(docPath, { path: docPath, source: file });
    }
  }
  return [...docs.values()].sort((a, b) => rel(a.path).localeCompare(rel(b.path)));
}

function brokenLocalLinks(filePath) {
  if (!isFile(filePath)) return [];
  const text = read(filePath);
  const findings = [];
  for (const match of text.matchAll(/\[[^\]]+\]\(([^)]+\.md)(?:#[^)]+)?\)/gi)) {
    const target = normalizeDocRef(match[1], filePath, '.');
    if (target && !isFile(target)) {
      findings.push(result('FAIL', `broken local markdown link to ${match[1]}`, filePath, lineOf(text, match[0])));
    }
  }
  return findings;
}

function runAuditDocs() {
  const args = positionalArgs(commandArgs, ['--docs-dir']);
  const planningId = args[0];
  if (!planningId) failUsage('audit-docs requires <NNN-slug>');
  const docsRoot = optionValue(commandArgs, '--docs-dir', docRootFromConfig());
  const planning = locatePlanning(planningId);
  if (!planning) return baseReport('audit-docs', [result('FAIL', `Planning ${planningId} not found in .planning/, .planning/active/, or .planning/finished/.`)], { planningId, docsRoot, coverage: [], consistency: [] });

  const planningFiles = walk(planning.dir, (file) => /\.md$/i.test(file)).sort();
  const planningLatest = latestMtime(planningFiles);
  const expected = expectedDocsFromPlanning(planning, docsRoot);
  const coverage = [];
  const consistency = [];

  if (expected.length === 0) {
    consistency.push(result('WARN', `No expected documentation references were found in ${planningId}.`, planning.dir));
  }

  for (const item of expected) {
    if (!isFile(item.path)) {
      coverage.push({ doc: rel(item.path), source: rel(item.source), status: 'FAIL', notes: 'Missing expected document' });
      continue;
    }
    const text = read(item.path);
    const stale = fs.statSync(item.path).mtimeMs + 1000 < planningLatest;
    const placeholder = /\[(?:TODO|Document title|Describe|TBD|placeholder)[^\]]*\]/i.test(text);
    const status = placeholder ? 'WARN' : stale ? 'WARN' : 'PASS';
    const notes = placeholder ? 'Contains placeholder text' : stale ? 'Older than latest planning artifact' : '-';
    coverage.push({ doc: rel(item.path), source: rel(item.source), status, notes });
    consistency.push(...brokenLocalLinks(item.path));
  }

  return baseReport('audit-docs', [], { planningId, docsRoot, coverage, consistency });
}

function runDoctor() {
  const pluginRoot = path.resolve(root, optionValue(commandArgs, '--plugin-root', '.'));
  const checks = [];
  const requiredFiles = [
    '.claude-plugin/plugin.json',
    'README.md',
    'docs/reference.md',
    'docs/commands.yml',
    'planning-template/README.md',
    'planning-template/config.yml',
    'planning-template/PDR-TEMPLATE.md',
    'planning-template/scripts/planning-check.mjs',
  ];

  const requiredMissing = requiredFiles.filter((file) => !isFile(path.join(pluginRoot, file)));
  checks.push({
    name: 'Required files',
    status: requiredMissing.length ? 'FAIL' : 'PASS',
    notes: requiredMissing.length ? `Missing: ${requiredMissing.join(', ')}` : '-',
  });

  const skillsDir = path.join(pluginRoot, 'skills');
  const commandsFile = path.join(pluginRoot, 'docs/commands.yml');
  const commandText = isFile(commandsFile) ? read(commandsFile) : '';
  const skillFindings = [];
  if (!isDir(skillsDir)) {
    skillFindings.push('skills/ directory missing');
  } else {
    for (const dir of listDirs(skillsDir)) {
      const skillName = path.basename(dir);
      const skillFile = path.join(dir, 'SKILL.md');
      if (!isFile(skillFile)) {
        skillFindings.push(`${skillName}: missing SKILL.md`);
        continue;
      }
      const text = read(skillFile);
      for (const field of ['name', 'description', 'argument-hint', 'allowed-tools']) {
        if (!new RegExp(`^${field}:`, 'm').test(text)) skillFindings.push(`${skillName}: missing ${field}`);
      }
      if (!new RegExp(`^name:\\s*${escapeRegex(skillName)}$`, 'm').test(text)) skillFindings.push(`${skillName}: name mismatch`);
      if (commandText && !new RegExp(`name:\\s*${escapeRegex(skillName)}\\b`).test(commandText)) skillFindings.push(`${skillName}: missing from docs/commands.yml`);
    }
  }
  checks.push({
    name: 'Skill metadata',
    status: skillFindings.length ? 'FAIL' : 'PASS',
    notes: skillFindings.length ? skillFindings.slice(0, 8).join('; ') : '-',
  });

  const templateChecks = [
    'planning-template/WORKFLOWS/README.md',
    'planning-template/update-version/README.md',
    'planning-template/_template/01-expansion.md',
    'planning-template/_template/02-deepening/story-NN-name.md',
    'planning-template/_template/02-deepening/task-NN-name.md',
  ];
  const templateMissing = templateChecks.filter((file) => !isFile(path.join(pluginRoot, file)));
  checks.push({
    name: 'Template integrity',
    status: templateMissing.length ? 'FAIL' : 'PASS',
    notes: templateMissing.length ? `Missing: ${templateMissing.join(', ')}` : '-',
  });

  const legacyFindings = [];
  for (const file of [
    path.join(pluginRoot, 'README.md'),
    path.join(pluginRoot, 'docs/reference.md'),
    ...walk(path.join(pluginRoot, 'planning-template/TUTORIAL'), (item) => /\.md$/i.test(item)),
  ].filter(isFile)) {
    const text = read(file);
    if (/\b(plan-scope|doc-scope|scope-NN)\b/.test(text)) legacyFindings.push(rel(file));
  }
  checks.push({
    name: 'Legacy drift',
    status: legacyFindings.length ? 'WARN' : 'PASS',
    notes: legacyFindings.length ? `Legacy scope references: ${legacyFindings.join(', ')}` : '-',
  });

  const verifyScript = path.join(pluginRoot, 'scripts/verify-plugin.sh');
  if (isFile(verifyScript)) {
    try {
      execFileSync('bash', [verifyScript], { cwd: pluginRoot, encoding: 'utf8', stdio: ['ignore', 'pipe', 'pipe'] });
      checks.push({ name: 'verify-plugin.sh', status: 'PASS', notes: '-' });
    } catch (error) {
      checks.push({ name: 'verify-plugin.sh', status: 'FAIL', notes: String(error.stdout || error.stderr || error.message).trim().split(/\r?\n/).slice(-5).join(' | ') });
    }
  } else {
    checks.push({ name: 'verify-plugin.sh', status: 'WARN', notes: 'scripts/verify-plugin.sh not found' });
  }

  return baseReport('doctor', [], { pluginRoot, checks });
}

function runValidate() {
  if (!isDir(planningRoot)) return baseReport('validate', [result('FAIL', 'No .planning/ directory found. Run /plan-init first.', planningRoot)], {});
  const target = commandArgs[0];
  const catalog = workflowCatalog();
  const config = readConfig();
  let plannings = discoverPlannings();
  const results = [];
  if (target) {
    plannings = plannings.filter((planning) => planning.id === target);
    if (plannings.length === 0) add(results, 'FAIL', `Planning ${target} not found in .planning/, .planning/active/, or .planning/finished/.`);
  }
  for (const [id, group] of groupBy(plannings, (planning) => planning.id).entries()) {
    if (group.length > 1) add(results, 'FAIL', `duplicate planning ${id} exists in: ${group.map((item) => item.location).join(', ')}`);
  }
  const planningReports = [];
  for (const planning of plannings) {
    planningReports.push({
      id: planning.id,
      location: planning.location,
      results: validatePlanning(planning, catalog, config),
    });
  }
  return baseReport('validate', results, { plannings: planningReports });
}

function runTaskValidate() {
  if (!isDir(planningRoot)) return baseReport('task-validate', [result('FAIL', 'No .planning/ directory found. Run /plan-init first.', planningRoot)], {});
  const [planningId, storyId, taskId] = commandArgs;
  if (!planningId) failUsage('task-validate requires <NNN-slug>');
  const planning = locatePlanning(planningId);
  const results = [];
  if (!planning) return baseReport('task-validate', [result('FAIL', `Planning ${planningId} not found in active or finished state.`)], {});
  const catalog = workflowCatalog();
  const config = readConfig();
  const storyFiles = storyId
    ? [storyFileFor(planning.dir, storyId)].filter(Boolean)
    : listFiles(path.join(planning.dir, '02-deepening'), /^story-\d+-.*\.md$/i);
  if (storyId && storyFiles.length === 0) add(results, 'FAIL', `${storyId} not found in ${planningId}`, path.join(planning.dir, '02-deepening'));
  const storyReports = [];
  for (const storyFile of storyFiles) {
    const sid = storyIdFromValue(path.basename(storyFile));
    const taskDir = taskFolderFor(planning.dir, sid);
    const storyResults = [];
    if (!taskDir) {
      add(storyResults, 'WARN', `${sid} is not atomized; no task folder found`, storyFile, null, `/plan-atomize ${planning.id} ${sid}`);
    } else {
      const storyText = read(storyFile);
      const taskRows = rowsByTaskId(storyText);
      if (taskId && !taskFileFor(taskDir, taskId)) {
        add(storyResults, 'FAIL', `${taskId} not found under ${sid}`, taskDir);
      } else {
        validateAtomizedStory(planning, storyFile, taskDir, taskRows, catalog, config, storyResults, taskId || null);
      }
    }
    storyReports.push({ id: sid, results: storyResults });
  }
  return baseReport('task-validate', results, { planningId, stories: storyReports });
}

function baseReport(kind, results, data) {
  return { kind, root, generatedAt: new Date().toISOString().slice(0, 10), results, ...data };
}

function countBy(items, selector) {
  const out = {};
  for (const item of items) out[selector(item)] = (out[selector(item)] || 0) + 1;
  return out;
}

function groupBy(items, selector) {
  const out = new Map();
  for (const item of items) {
    const key = selector(item);
    if (!out.has(key)) out.set(key, []);
    out.get(key).push(item);
  }
  return out;
}

function summary(results) {
  return {
    pass: results.filter((item) => item.status === 'PASS').length,
    warn: results.filter((item) => item.status === 'WARN').length,
    fail: results.filter((item) => item.status === 'FAIL').length,
  };
}

function allFindings(report) {
  const findings = [...(report.results || [])];
  for (const planning of report.plannings || []) findings.push(...planning.results);
  for (const story of report.stories || []) findings.push(...story.results);
  return findings;
}

function formatFinding(item) {
  const location = item.path ? ` (${item.path}${item.line ? `:${item.line}` : ''})` : '';
  const suggestion = item.suggestion ? ` Suggestion: ${item.suggestion}` : '';
  const punctuation = /[.!?]$/.test(item.message) ? '' : '.';
  return `- ${item.status}: ${item.message}${location}${punctuation}${suggestion}`;
}

function renderMarkdown(report) {
  if (report.kind === 'health') return renderHealth(report);
  if (report.kind === 'validate') return renderValidate(report);
  if (report.kind === 'task-validate') return renderTaskValidate(report);
  if (report.kind === 'us-status') return renderUsStatus(report);
  if (report.kind === 'audit-docs') return renderAuditDocs(report);
  if (report.kind === 'doctor') return renderDoctor(report);
  return JSON.stringify(report, null, 2);
}

function renderHealth(report) {
  const findings = allFindings(report);
  const counts = summary(findings);
  const lines = [
    `## Planning Health Report - ${report.generatedAt}`,
    '',
    `Plannings found: ${report.planningsFound || 0} (INITIAL: ${report.initial || 0}, ACTIVE: ${report.active || 0}, FINISHED: ${report.finished || 0})`,
    '',
  ];
  if (findings.length === 0) {
    lines.push('Planning system is healthy - no issues found.');
  } else {
    lines.push(`Summary: ${counts.fail} FAIL, ${counts.warn} WARN, ${counts.pass} PASS`);
    lines.push('');
    lines.push(...findings.map(formatFinding));
    appendReviewPrompt(lines, findings);
  }
  lines.push('', 'Read-only: no files were modified.');
  return lines.join('\n');
}

function renderValidate(report) {
  const lines = [`## Planning Validation Report - ${report.generatedAt}`, ''];
  if (report.results?.length) lines.push(...report.results.map(formatFinding), '');
  let clean = 0;
  let warn = 0;
  let fail = 0;
  for (const planning of report.plannings || []) {
    const counts = summary(planning.results);
    if (counts.fail > 0) fail += 1;
    else if (counts.warn > 0) warn += 1;
    else clean += 1;
    lines.push(`### ${planning.id} (${planning.location})`);
    if (planning.results.length === 0) lines.push('- PASS: no structural issues found.');
    else lines.push(...planning.results.map(formatFinding));
    lines.push('');
  }
  const findings = allFindings(report);
  lines.push(`${(report.plannings || []).length} plannings validated - ${clean} clean, ${warn} with warnings, ${fail} with failures.`);
  appendReviewPrompt(lines, findings);
  lines.push('', 'Read-only: no files were modified.');
  return lines.join('\n');
}

function renderTaskValidate(report) {
  const lines = [`## Task Validation Report - ${report.generatedAt}`, ''];
  if (report.results?.length) lines.push(...report.results.map(formatFinding), '');
  let tasks = 0;
  let clean = 0;
  let warn = 0;
  let fail = 0;
  for (const story of report.stories || []) {
    const counts = summary(story.results);
    if (counts.fail > 0) fail += 1;
    else if (counts.warn > 0) warn += 1;
    else clean += 1;
    tasks += story.results.filter((item) => /task-\d+/.test(item.message)).length;
    lines.push(`### ${story.id}`);
    if (story.results.length === 0) lines.push('- PASS: no atomized task issues found.');
    else lines.push(...story.results.map(formatFinding));
    lines.push('');
  }
  const findings = allFindings(report);
  lines.push(`${tasks} task findings audited - ${clean} clean stories, ${warn} with warnings, ${fail} with failures.`);
  appendReviewPrompt(lines, findings);
  lines.push('', 'Read-only: no files were modified.');
  return lines.join('\n');
}

function check(value) {
  return value ? 'yes' : 'no';
}

function renderUsStatus(report) {
  const lines = [
    `## Story Status - ${report.target}`,
    '',
  ];
  if ((report.stories || []).length === 0) {
    lines.push(`No story-shaped markdown files found. Scanned ${report.scannedFiles || 0} markdown files.`);
    lines.push('', 'Read-only: no files were modified.');
    return lines.join('\n');
  }
  lines.push('| Story | DoD | Tech Notes | Deps | Complexity | Planning |');
  lines.push('|-------|-----|------------|------|------------|----------|');
  for (const story of report.stories) {
    lines.push(`| ${story.id} ${story.title} | ${check(story.dod)} | ${check(story.technicalNotes)} | ${check(story.dependencies)} | ${check(story.complexity)} | ${story.planning || '-'} |`);
  }
  const s = report.summary || {};
  lines.push('', `Summary: ${s.stories || 0} stories - ${s.dod || 0} have DoD, ${s.technicalNotes || 0} have Technical Notes, ${s.dependencies || 0} have Dependencies, ${s.complexity || 0} have Complexity, ${s.linked || 0} linked to active planning.`);
  const needing = report.stories.filter((story) => !story.dod || !story.technicalNotes || !story.dependencies || !story.complexity);
  if (needing.length > 0) {
    lines.push('', `Stories needing enrichment: ${needing.map((story) => story.id).join(', ')}`);
    lines.push(`Suggestion: ${needing.slice(0, 3).map((story) => `/us-enrich ${story.id}`).join(' | ')}`);
  }
  lines.push('', 'Read-only: no files were modified.');
  return lines.join('\n');
}

function renderAuditDocs(report) {
  const lines = [
    `# Documentation Audit - ${report.planningId || 'unknown'}`,
    '',
  ];
  const topLevel = report.results || [];
  const coverage = report.coverage || [];
  const consistency = report.consistency || [];
  const fail = topLevel.filter((item) => item.status === 'FAIL').length + coverage.filter((item) => item.status === 'FAIL').length + consistency.filter((item) => item.status === 'FAIL').length;
  const warn = topLevel.filter((item) => item.status === 'WARN').length + coverage.filter((item) => item.status === 'WARN').length + consistency.filter((item) => item.status === 'WARN').length;
  lines.push('## Result');
  lines.push(fail > 0 ? 'FAIL' : warn > 0 ? 'WARN' : 'PASS');
  if (topLevel.length > 0) {
    lines.push('', '## Planning Findings');
    lines.push(...topLevel.map(formatFinding));
  }
  lines.push('', '## Coverage');
  if (coverage.length === 0) {
    lines.push('- WARN: no expected documentation files were discovered from planning references.');
  } else {
    lines.push('| Expected document | Source | Result | Notes |');
    lines.push('|-------------------|--------|--------|-------|');
    for (const row of coverage) {
      lines.push(`| ${row.doc} | ${row.source} | ${row.status} | ${row.notes} |`);
    }
  }
  lines.push('', '## Consistency Findings');
  if (consistency.length === 0) lines.push('- PASS: no consistency findings.');
  else lines.push(...consistency.map(formatFinding));
  const missing = coverage.filter((item) => item.status !== 'PASS');
  lines.push('', '## Missing or Stale Docs');
  if (missing.length === 0) lines.push('- PASS: no missing or stale docs detected.');
  else lines.push(...missing.map((item) => `- ${item.status}: ${item.doc} - ${item.notes}`));
  lines.push('', '## Recommended Next Commands');
  lines.push(`- /plan-validate ${report.planningId || '<planning-id>'}`);
  lines.push(`- /doc-generate ${report.planningId || '<planning-id>'}`);
  lines.push('', 'Read-only: no files were modified.');
  return lines.join('\n');
}

function renderDoctor(report) {
  const checks = report.checks || [];
  const fail = checks.filter((item) => item.status === 'FAIL').length;
  const warn = checks.filter((item) => item.status === 'WARN').length;
  const lines = [
    '# Plugin Doctor Report',
    '',
    `Plugin root: ${report.pluginRoot}`,
    '',
    '## Result',
    fail > 0 ? 'FAIL' : warn > 0 ? 'WARN' : 'PASS',
    '',
    '## Checks',
    '| Check | Result | Notes |',
    '|-------|--------|-------|',
  ];
  for (const checkItem of checks) {
    lines.push(`| ${checkItem.name} | ${checkItem.status} | ${checkItem.notes || '-'} |`);
  }
  const required = checks.filter((item) => item.status === 'FAIL');
  lines.push('', '## Required Fixes');
  if (required.length === 0) lines.push('- None.');
  else lines.push(...required.map((item) => `- ${item.name}: ${item.notes}`));
  const improvements = checks.filter((item) => item.status === 'WARN');
  lines.push('', '## Recommended Improvements');
  if (improvements.length === 0) lines.push('- None.');
  else lines.push(...improvements.map((item) => `- ${item.name}: ${item.notes}`));
  lines.push('', 'Read-only: no files were modified.');
  return lines.join('\n');
}

function appendReviewPrompt(lines, findings) {
  const referenced = [...new Map(findings.filter((item) => item.path && item.status !== 'PASS').map((item) => [item.path, item])).values()];
  if (referenced.length === 0) return;
  lines.push('', 'Before applying any fix, inspect these files or directories directly:');
  for (const item of referenced) {
    lines.push(`- ${item.path} - verify: ${item.message}`);
  }
}

let report;
if (command === 'health') report = runHealth();
else if (command === 'validate') report = runValidate();
else if (command === 'task-validate') report = runTaskValidate();
else if (command === 'us-status') report = runUsStatus();
else if (command === 'audit-docs') report = runAuditDocs();
else if (command === 'doctor') report = runDoctor();
else failUsage(`Unknown command: ${command}`);

if (format === 'json') {
  console.log(JSON.stringify(report, null, 2));
} else {
  console.log(renderMarkdown(report));
}
