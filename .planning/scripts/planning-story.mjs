#!/usr/bin/env node
import { spawnSync } from 'node:child_process';
import { existsSync, mkdirSync, readdirSync, readFileSync, statSync, writeFileSync } from 'node:fs';
import path from 'node:path';

const cwd = process.cwd();
const args = process.argv.slice(2);
const command = args.shift();

let format = 'markdown';
let write = false;
const positional = [];
const options = new Map();

function usage() {
  return `Usage:
  node .planning/scripts/planning-story.mjs <command> [args...] [--format markdown|json] [--write]

Backlog commands:
  backlog-inspect <container-or-story>
  backlog-new <container> --title <title> [--blank] [--priority <value>] [--narrative <text>] [--criteria <text>]
  backlog-enrich <story-ref> [--section "Heading::Body"]
  backlog-split <story-file> --new-title <title> --move-ac <1,2>

Planning commands:
  planning-inspect <planning-id> [story-NN]
  planning-add-story <planning-id> --title <title> [--area <code>] [--dependencies <list>] [--tasks <text>] [--done <text>]
  planning-enrich-story <planning-id> <story-NN> [--section "Heading::Body"]
  planning-split-story <planning-id> <story-NN> --new-title <title> --move-tasks <1,2>

Plan-story execution helpers:
  execute-inspect <planning-id> <story-NN> [--child-worktree] [--worktree-prefix <prefix>]
  execute-start <planning-id> <story-NN> [--write]
  execute-done <planning-id> <story-NN> [--write]
  execute-finalize <planning-id> <story-NN> [--child-worktree] [--worktree-prefix <prefix>]

All mutating commands are dry-run by default. Add --write after human approval.`;
}

function addOption(key, value) {
  if (!options.has(key)) options.set(key, []);
  options.get(key).push(value);
}

for (let i = 0; i < args.length; i += 1) {
  const arg = args[i];
  if (arg === '--write') write = true;
  else if (arg === '--format') {
    format = args[i + 1] || '';
    i += 1;
  } else if (arg.startsWith('--')) {
    const key = arg.slice(2);
    const value = args[i + 1];
    if (value === undefined || value.startsWith('--')) addOption(key, 'true');
    else {
      addOption(key, value);
      i += 1;
    }
  } else positional.push(arg);
}

if (!['markdown', 'json'].includes(format)) fail(`Invalid --format: ${format}`);

function opt(key, fallback = '') {
  const values = options.get(key);
  return values && values.length ? values[values.length - 1] : fallback;
}

function optAll(key) {
  return options.get(key) || [];
}

function fail(message, details = {}) {
  if (format === 'json') console.log(JSON.stringify({ ok: false, error: message, ...details }, null, 2));
  else {
    console.error(`ERROR: ${message}`);
    if (details.help) console.error(`\n${details.help}`);
  }
  process.exit(1);
}

function rel(file) {
  return path.relative(cwd, file) || '.';
}

function read(file) {
  return readFileSync(file, 'utf8');
}

function writeFile(file, text) {
  mkdirSync(path.dirname(file), { recursive: true });
  writeFileSync(file, text);
}

function isMarkdown(file) {
  return file.toLowerCase().endsWith('.md');
}

function slugify(value) {
  return String(value || 'story')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
    .slice(0, 80) || 'story';
}

function titleize(value) {
  return String(value || '')
    .replace(/^\s*#+\s*/, '')
    .replace(/\[[^\]]+\]\([^)]+\)/g, (match) => match.replace(/\[|\]\([^)]+\)/g, ''))
    .trim();
}

function listMarkdown(dir) {
  return readdirSync(dir)
    .filter((name) => isMarkdown(name))
    .sort()
    .map((name) => path.join(dir, name));
}

function isOverviewName(file) {
  return /^(README|EPIC|overview|index)\.md$/i.test(path.basename(file));
}

function storyShape(text) {
  const narrative = /\b(as a|como|i want|quiero|para poder|so that|so i can)\b/i.test(text);
  const criteria = /^#{1,6}\s*(Acceptance Criteria|Criterios de aceptaci[oó]n|Requirements|Tests|Checklist|Done Criteria)\b/mi.test(text);
  const checklist = /^\s*-\s+\[[ xX]\]\s+.+/m.test(text);
  return narrative || criteria || checklist;
}

function heading(text) {
  const match = text.match(/^#\s+(.+)$/m) || text.match(/^#{2,6}\s+(.+)$/m);
  return match ? titleize(match[1]) : '';
}

function acceptanceBullets(text) {
  const section = text.match(/^#{1,6}\s*(Acceptance Criteria|Criterios de aceptaci[oó]n|Requirements|Tests|Checklist|Done Criteria)\b[^\n]*\n([\s\S]*?)(?=\n#{1,6}\s+|\n---\n|(?![\s\S]))/im);
  if (!section) return [];
  return section[2].split('\n')
    .map((line, index) => ({ line, index }))
    .filter((item) => /^\s*[-*]\s+/.test(item.line) || /^\s*-\s+\[[ xX]\]\s+/.test(item.line))
    .map((item) => ({ number: item.index + 1, text: item.line.replace(/^\s*[-*]\s+(?:\[[ xX]\]\s*)?/, '').trim(), raw: item.line }));
}

function taskRows(text) {
  return stripFenced(markdownSection(text, 'Tasks')).split('\n')
    .map((line) => line.trim())
    .filter((line) => line.startsWith('|'))
    .map((line) => line.split('|').map((cell) => cell.trim()))
    .filter((cells) => /^\d+$/.test(cells[1] || '') && (cells[2] || '').length)
    .map((cells) => ({ number: cells[1], task: cells[2], workflow: cells[3] || 'GENERATE-DOCUMENT', status: cells[4] || 'TODO', output: cells[5] || '—' }));
}

function storyRows(text) {
  return stripFenced(markdownSection(text, 'Story Summary')).split('\n')
    .map((line) => line.trim())
    .filter((line) => line.startsWith('|'))
    .map((line) => line.split('|').map((cell) => cell.trim()))
    .filter((cells) => /^\d+$/.test(cells[1] || '') && (cells[2] || '').length)
    .map((cells) => ({
      number: cells[1],
      story: cells[2],
      area: cells[3] || 'W',
      dependsOn: cells[4] || '—',
      risk: cells[5] || 'M',
      external: cells[6] || '—',
      status: cells[7] || 'TODO',
    }));
}

function markdownSection(text, name) {
  const escaped = name.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const match = text.match(new RegExp(`^#{1,6}\\s+${escaped}\\b[^\\n]*\\n([\\s\\S]*?)(?=\\n#{1,6}\\s+|(?![\\s\\S]))`, 'im'));
  return match ? match[1] : '';
}

function stripFenced(text) {
  return text.replace(/```[\s\S]*?```/g, '');
}

function nextNumber(files, rows = []) {
  const values = [];
  for (const file of files) {
    const name = path.basename(file);
    const fileNumber = name.match(/^(\d+)[-_]/) || name.match(/^story-(\d+)/i);
    if (fileNumber) values.push(Number(fileNumber[1]));
    const text = existsSync(file) ? read(file) : '';
    for (const match of text.matchAll(/\b(?:US|FEAT|story)[-_ ]?0*(\d+)\b/gi)) values.push(Number(match[1]));
  }
  for (const row of rows) values.push(Number(row.number));
  const max = values.filter(Number.isFinite).reduce((acc, value) => Math.max(acc, value), 0);
  return max + 1;
}

function idScheme(files) {
  for (const file of files) {
    const text = existsSync(file) ? read(file) : '';
    const combined = `${path.basename(file)}\n${text}`;
    const match = combined.match(/\b(US|FEAT|story)[-_ ]?(\d+)\b/i);
    if (match) return { prefix: match[1].toUpperCase() === 'STORY' ? 'story-' : `${match[1].toUpperCase()}-`, width: match[2].length };
  }
  return { prefix: '', width: 2 };
}

function inspectContainer(target) {
  if (!target) fail('Missing container or story path.', { help: usage() });
  const absolute = path.resolve(cwd, target);
  if (!existsSync(absolute)) fail(`Path does not exist: ${target}`);
  assertBacklogPath(absolute);
  const stat = statSync(absolute);
  const files = stat.isDirectory() ? listMarkdown(absolute) : [absolute];
  const stories = files
    .filter((file) => !stat.isDirectory() || !isOverviewName(file))
    .map((file) => {
      const text = read(file);
      return {
        path: rel(file),
        title: heading(text) || path.basename(file, '.md'),
        shape: storyShape(text),
        criteriaCount: acceptanceBullets(text).length,
      };
    })
    .filter((item) => item.shape || !stat.isDirectory());
  const overviewFiles = stat.isDirectory() ? files.filter(isOverviewName).map(rel) : [];
  const scheme = idScheme(files);
  const next = nextNumber(files);
  const filenameWidth = Math.max(2, ...files.map((file) => {
    const match = path.basename(file).match(/^(\d+)[-_]/);
    return match ? match[1].length : 0;
  }));
  return {
    ok: true,
    mode: stat.isDirectory() ? 'directory' : 'single-file',
    target: rel(absolute),
    storyCount: stories.length,
    stories,
    overviewFiles,
    next: {
      number: next,
      id: scheme.prefix ? `${scheme.prefix}${String(next).padStart(scheme.width, '0')}` : '',
      filenameNumber: String(next).padStart(filenameWidth, '0'),
    },
  };
}

function renderBacklogContent({ id, title, priority, narrative, criteria, blank }) {
  const lines = [`# ${id ? `${id} — ` : ''}${title || '[Story title]'}`, ''];
  if (priority) lines.push(`Priority: ${priority}`, '');
  lines.push('## Story', '');
  lines.push(blank ? '[Narrative statement]' : (narrative || 'As a user, I want this capability so I can complete the workflow.'));
  lines.push('', '## Acceptance Criteria', '');
  const items = splitList(criteria);
  if (blank || items.length === 0) lines.push('- [ ] [Criterion]');
  else for (const item of items) lines.push(`- [ ] ${item}`);
  lines.push('');
  return lines.join('\n');
}

function splitList(value) {
  return String(value || '')
    .split(/\n|\|/)
    .map((item) => item.trim())
    .filter(Boolean);
}

function appendIndexRow(container, file, title, id) {
  const readme = path.join(container, 'README.md');
  if (!existsSync(readme)) return false;
  const text = read(readme);
  if (!/\|\s*(Story|Title|User Story|Historia)/i.test(text)) return false;
  const row = `| ${id || path.basename(file, '.md')} | [${title}](${path.basename(file)}) | TODO |`;
  const lines = text.split('\n');
  let insertAt = -1;
  for (let i = lines.length - 1; i >= 0; i -= 1) {
    if (/^\|.+\|$/.test(lines[i]) && !/^\|\s*-+/.test(lines[i])) {
      insertAt = i + 1;
      break;
    }
  }
  if (insertAt < 0) return false;
  lines.splice(insertAt, 0, row);
  writeFile(readme, lines.join('\n'));
  return true;
}

function runBacklogNew() {
  const containerArg = positional[0];
  if (!containerArg) fail('Missing <container>.', { help: usage() });
  const container = path.resolve(cwd, containerArg);
  if (!existsSync(container)) fail(`Container does not exist: ${containerArg}`);
  const info = inspectContainer(containerArg);
  const title = opt('title', opt('name', 'new-story'));
  const blank = opt('blank') === 'true';
  const id = info.next.id;
  const content = renderBacklogContent({
    id,
    title,
    priority: opt('priority'),
    narrative: opt('narrative'),
    criteria: opt('criteria'),
    blank,
  });
  const stat = statSync(container);
  const target = stat.isDirectory()
    ? path.join(container, `${info.next.filenameNumber}-${slugify(title)}.md`)
    : container;
  const result = {
    ok: true,
    command: 'backlog-new',
    write,
    target: rel(target),
    id,
    content,
    indexUpdated: false,
  };
  if (write) {
    if (stat.isDirectory()) writeFile(target, content);
    else writeFile(target, `${read(target).replace(/\s*$/, '\n\n')}${content}`);
    if (stat.isDirectory()) result.indexUpdated = appendIndexRow(container, target, title, id);
  }
  print(result, renderBacklogNew);
}

function existingSections(text) {
  const sections = new Set();
  for (const match of text.matchAll(/^#{1,6}\s+(.+)$/gm)) sections.add(match[1].trim().toLowerCase());
  return sections;
}

function resolveStoryRef(value) {
  if (!value) fail('Missing story reference.', { help: usage() });
  const direct = path.resolve(cwd, value);
  if (existsSync(direct) && statSync(direct).isFile()) {
    assertBacklogPath(direct);
    return direct;
  }
  const matches = [];
  function walk(dir) {
    for (const name of readdirSync(dir)) {
      if (['.git', 'node_modules', '.planning', '.next', 'out'].includes(name)) continue;
      const file = path.join(dir, name);
      const stat = statSync(file);
      if (stat.isDirectory()) walk(file);
      else if (isMarkdown(file)) {
        const haystack = `${path.basename(file)}\n${read(file)}`;
        if (haystack.toLowerCase().includes(value.toLowerCase())) matches.push(file);
      }
    }
  }
  walk(cwd);
  if (matches.length === 1) {
    assertBacklogPath(matches[0]);
    return matches[0];
  }
  if (matches.length > 1) fail(`Ambiguous story reference: ${value}`, { matches: matches.map(rel) });
  fail(`Story not found: ${value}`);
}

function assertBacklogPath(file) {
  const relative = path.relative(cwd, file);
  if (relative === '.planning' || relative.startsWith(`.planning${path.sep}`)) {
    fail('Backlog commands do not operate inside the current .planning/. Use planning-* subcommands instead.');
  }
}

function parseSections() {
  return optAll('section').map((value) => {
    const index = value.indexOf('::');
    if (index < 0) fail(`Invalid --section value. Use "Heading::Body": ${value}`);
    return { heading: value.slice(0, index).trim(), body: value.slice(index + 2).trim() };
  }).filter((section) => section.heading && section.body);
}

function appendSections(text, sections) {
  const additions = [];
  const present = existingSections(text);
  for (const section of sections) {
    if (present.has(section.heading.toLowerCase())) continue;
    additions.push(`## ${section.heading}\n\n${section.body}`);
  }
  if (!additions.length) return text;
  return `${text.replace(/\s*$/, '\n\n')}${additions.join('\n\n')}\n`;
}

function runBacklogEnrich() {
  const file = resolveStoryRef(positional[0]);
  const text = read(file);
  const sections = parseSections();
  const present = existingSections(text);
  const expected = ['definition of done', 'done criteria', 'technical notes', 'dependencies', 'complexity'];
  const missing = expected.filter((name) => !present.has(name));
  const updated = appendSections(text, sections);
  const result = {
    ok: true,
    command: 'backlog-enrich',
    write,
    file: rel(file),
    found: {
      title: heading(text) || path.basename(file, '.md'),
      criteriaCount: acceptanceBullets(text).length,
      sections: [...present].sort(),
    },
    missing,
    sectionsToAppend: sections.map((section) => section.heading),
  };
  if (write && updated !== text) writeFile(file, updated);
  print(result, renderEnrich);
}

function parseIndexes(value) {
  return new Set(String(value || '').split(',').map((item) => Number(item.trim())).filter(Number.isFinite));
}

function splitAcceptance(text, moveIndexes) {
  const match = text.match(/^(#{1,6}\s*(?:Acceptance Criteria|Criterios de aceptaci[oó]n|Requirements|Tests|Checklist|Done Criteria)\b[^\n]*\n)([\s\S]*?)(?=\n#{1,6}\s+|\n---\n|(?![\s\S]))/im);
  if (!match) fail('No acceptance criteria section found.');
  let bulletOrdinal = 0;
  const moved = [];
  const keptLines = match[2].split('\n').filter((line) => {
    if (!/^\s*[-*]\s+/.test(line) && !/^\s*-\s+\[[ xX]\]\s+/.test(line)) return true;
    bulletOrdinal += 1;
    if (moveIndexes.has(bulletOrdinal)) {
      moved.push(line);
      return false;
    }
    return true;
  });
  if (!moved.length) fail('No acceptance criteria matched --move-ac.');
  const updated = text.replace(match[0], `${match[1]}${keptLines.join('\n')}`);
  return { updated, moved };
}

function runBacklogSplit() {
  const file = resolveStoryRef(positional[0]);
  const text = read(file);
  const title = opt('new-title');
  const moveIndexes = parseIndexes(opt('move-ac'));
  if (!title) fail('Missing --new-title.');
  if (!moveIndexes.size) fail('Missing --move-ac <1,2>.');
  const { updated, moved } = splitAcceptance(text, moveIndexes);
  const container = path.dirname(file);
  const info = inspectContainer(container);
  const target = path.join(container, `${info.next.filenameNumber}-${slugify(title)}.md`);
  const newContent = [
    `# ${title}`,
    '',
    `Split from: [${path.basename(file)}](${path.basename(file)})`,
    '',
    '## Acceptance Criteria',
    '',
    ...moved,
    '',
  ].join('\n');
  const originalWithRef = `${updated.replace(/\s*$/, '\n\n')}Related: [${title}](${path.basename(target)})\n`;
  const result = {
    ok: true,
    command: 'backlog-split',
    write,
    original: rel(file),
    newStory: rel(target),
    movedCriteria: moved.map((line) => line.replace(/^\s*[-*]\s+(?:\[[ xX]\]\s*)?/, '').trim()),
  };
  if (write) {
    writeFile(file, originalWithRef);
    writeFile(target, newContent);
    appendIndexRow(container, target, title, info.next.id);
  }
  print(result, renderSplit);
}

function planningDir(id) {
  const dir = path.join(cwd, '.planning', 'active', id);
  if (!existsSync(dir)) fail(`Active planning not found in current directory: .planning/active/${id}`);
  return dir;
}

function findStoryFile(planning, storyId) {
  const deepening = path.join(planning, '02-deepening');
  if (!existsSync(deepening)) fail(`Missing deepening directory: ${rel(deepening)}`);
  const file = readdirSync(deepening).filter((name) => name.startsWith(`${storyId}-`) && name.endsWith('.md')).sort()[0];
  if (!file) fail(`Story not found: ${storyId}`, { checked: rel(deepening) });
  return path.join(deepening, file);
}

function inspectPlanning(id, storyId = '') {
  const dir = planningDir(id);
  const expansion = path.join(dir, '01-expansion.md');
  if (!existsSync(expansion)) fail(`Missing expansion file: ${rel(expansion)}`);
  const rows = storyRows(read(expansion));
  const max = rows.map((row) => Number(row.number)).filter(Number.isFinite).reduce((acc, value) => Math.max(acc, value), 0);
  const report = {
    ok: true,
    planning: id,
    expansion: rel(expansion),
    storyCount: rows.length,
    stories: rows,
    next: { number: max + 1, id: `story-${String(max + 1).padStart(2, '0')}` },
  };
  if (storyId) {
    const file = findStoryFile(dir, storyId);
    const text = read(file);
    report.story = {
      id: storyId,
      file: rel(file),
      title: heading(text) || path.basename(file, '.md'),
      status: (text.match(/^>\s*\*\*Status:\*\*\s*(.+)$/m) || [])[1] || '',
      taskCount: taskRows(text).length,
      doneCriteriaCount: acceptanceBullets(text).length,
      sections: [...existingSections(text)].sort(),
    };
  }
  return report;
}

function renderStoryFile(number, title, area, dependencies, tasks, done) {
  const id = `story-${String(number).padStart(2, '0')}`;
  const slug = slugify(title);
  const taskItems = splitList(tasks);
  const doneItems = splitList(done);
  const lines = [
    `# 🔍 DEEPENING: Story ${String(number).padStart(2, '0')} — ${title}`,
    '',
    '> **Status:** TODO',
    '> [← 01-expansion.md](../01-expansion.md) | [← planning/README.md](../../README.md)',
    '',
    '---',
    '',
    '## Objective',
    '',
    `Define and deliver ${title}.`,
    '',
    '---',
    '',
    '## Risk',
    '',
    '| Risk | Impact | Likelihood | Mitigation |',
    '|------|--------|------------|------------|',
    '| Scope or dependency gap discovered during execution | M | M | Validate against planning context before atomization |',
    '',
    '---',
    '',
    '## Tasks',
    '',
    '| # | Task | Workflow | Status | Output |',
    '|---|------|----------|--------|--------|',
  ];
  if (taskItems.length) {
    taskItems.forEach((task, index) => {
      const taskId = `task-${String(index + 1).padStart(2, '0')}-${slugify(task)}`;
      lines.push(`| ${index + 1} | [${task}](${id}-${slug}/${taskId}.md) | GENERATE-DOCUMENT | TODO | ${task} delivered |`);
    });
  } else {
    lines.push('| 1 | [Refine implementation plan](story-NN-name/task-01-refine-implementation-plan.md) | GENERATE-DOCUMENT | TODO | Executable task plan |');
  }
  lines.push('', '---', '', '## Test Suite', '', '- [ ] Story-level test suite is generated or refreshed with `/plan-test-suite`.', '', '---', '', '## Done Criteria', '');
  if (doneItems.length) for (const item of doneItems) lines.push(`- [ ] ${item}`);
  else lines.push('- [ ] Story output is implemented and reviewed', '- [ ] TRACEABILITY.md updated with new terms from this story');
  lines.push('', '---', '', '## Dependencies', '', dependencies || '—', '', '---', '', '## Area', '', area || 'W', '');
  return lines.join('\n');
}

function insertExpansionRow(text, row) {
  const lines = text.split('\n');
  const rowText = `| ${row.number} | ${row.story} | ${row.area} | ${row.dependsOn || '—'} | ${row.risk || 'M'} | ${row.external || '—'} | TODO |`;
  const headingIndex = lines.findIndex((line) => /^##\s+Story Summary\s*$/i.test(line.trim()));
  if (headingIndex < 0) fail('Could not locate Story Summary section in 01-expansion.md.');
  let sectionEnd = lines.length;
  for (let i = headingIndex + 1; i < lines.length; i += 1) {
    if (/^##\s+/.test(lines[i].trim())) {
      sectionEnd = i;
      break;
    }
  }
  let insertAt = -1;
  for (let i = sectionEnd - 1; i > headingIndex; i -= 1) {
    const cells = lines[i].split('|').map((cell) => cell.trim());
    if (/^\d+$/.test(cells[1] || '')) {
      insertAt = i + 1;
      break;
    }
  }
  if (insertAt < 0) fail('Could not locate Story Summary table rows in 01-expansion.md.');
  lines.splice(insertAt, 0, rowText);
  return lines.join('\n');
}

function runPlanningAddStory() {
  const id = positional[0];
  const title = opt('title');
  if (!id) fail('Missing <planning-id>.', { help: usage() });
  if (!title) fail('Missing --title <title>.');
  const dir = planningDir(id);
  const info = inspectPlanning(id);
  const number = info.next.number;
  const storyId = info.next.id;
  const slug = slugify(title);
  const file = path.join(dir, '02-deepening', `${storyId}-${slug}.md`);
  const content = renderStoryFile(number, title, opt('area', 'W'), opt('dependencies', '—'), opt('tasks'), opt('done'));
  const expansion = path.join(dir, '01-expansion.md');
  const result = {
    ok: true,
    command: 'planning-add-story',
    write,
    planning: id,
    story: storyId,
    file: rel(file),
    expansion: rel(expansion),
  };
  if (write) {
    writeFile(file, content);
    const updated = insertExpansionRow(read(expansion), {
      number: String(number).padStart(2, '0'),
      story: slug,
      area: opt('area', 'W'),
      dependsOn: opt('dependencies', '—'),
      risk: opt('risk', 'M'),
      external: opt('external', '—'),
    });
    writeFile(expansion, updated);
    appendEdgeCase(dir, '/plan-enrich-epic', `Added ${storyId}-${slug} after initial expansion.`);
  }
  print(result, renderPlanningAdd);
}

function appendEdgeCase(dir, source, body) {
  const file = path.join(dir, 'RETROSPECTIVE-RAW.md');
  if (!existsSync(file)) return false;
  const stamp = new Date().toISOString().slice(0, 10);
  writeFile(file, `${read(file).replace(/\s*$/, '\n\n')}## ${stamp} — ${source}\n\n${body}\n`);
  return true;
}

function runPlanningEnrichStory() {
  const id = positional[0];
  const storyId = positional[1];
  if (!id || !storyId) fail('Missing <planning-id> <story-NN>.', { help: usage() });
  const dir = planningDir(id);
  const file = findStoryFile(dir, storyId);
  const text = read(file);
  const sections = parseSections();
  const updated = appendSections(text, sections);
  const result = {
    ok: true,
    command: 'planning-enrich-story',
    write,
    planning: id,
    story: storyId,
    file: rel(file),
    sectionsToAppend: sections.map((section) => section.heading),
  };
  if (write && updated !== text) {
    writeFile(file, updated);
    appendEdgeCase(dir, '/plan-enrich-story', `Enriched ${storyId}: ${sections.map((section) => section.heading).join(', ') || 'no new sections'}.`);
  }
  print(result, renderEnrich);
}

function splitTasks(text, moveIndexes) {
  const lines = text.split('\n');
  const moved = [];
  const kept = lines.map((line) => {
    const cells = line.split('|').map((cell) => cell.trim());
    if (!/^\d+$/.test(cells[1] || '')) return line;
    if (moveIndexes.has(Number(cells[1]))) {
      moved.push(cells);
      return null;
    }
    return line;
  }).filter((line) => line !== null);
  if (!moved.length) fail('No task rows matched --move-tasks.');
  return { updated: kept.join('\n'), moved };
}

function runPlanningSplitStory() {
  const id = positional[0];
  const storyId = positional[1];
  const title = opt('new-title');
  const moveIndexes = parseIndexes(opt('move-tasks'));
  if (!id || !storyId) fail('Missing <planning-id> <story-NN>.', { help: usage() });
  if (!title) fail('Missing --new-title <title>.');
  if (!moveIndexes.size) fail('Missing --move-tasks <1,2>.');
  const dir = planningDir(id);
  const file = findStoryFile(dir, storyId);
  const text = read(file);
  const status = (text.match(/^>\s*\*\*Status:\*\*\s*(.+)$/m) || [])[1] || '';
  if (status === 'DONE') fail('Cannot split a DONE story. Use planning-add-story instead.');
  const { updated, moved } = splitTasks(text, moveIndexes);
  const info = inspectPlanning(id);
  const number = info.next.number;
  const newStoryId = info.next.id;
  const slug = slugify(title);
  const target = path.join(dir, '02-deepening', `${newStoryId}-${slug}.md`);
  const movedTasks = moved.map((cells) => cells[2].replace(/\[[^\]]+\]\([^)]+\)/g, (match) => match.replace(/\[|\]\([^)]+\)/g, '')));
  const content = renderStoryFile(number, title, opt('area', 'W'), storyId.replace('story-', ''), movedTasks.join('|'), opt('done'));
  const result = {
    ok: true,
    command: 'planning-split-story',
    write,
    planning: id,
    original: rel(file),
    newStory: rel(target),
    movedTasks,
  };
  if (write) {
    writeFile(file, `${updated.replace(/\s*$/, '\n\n')}Split note: moved selected tasks to [${newStoryId}-${slug}](${path.basename(target)}).\n`);
    writeFile(target, content);
    const expansion = path.join(dir, '01-expansion.md');
    writeFile(expansion, insertExpansionRow(read(expansion), {
      number: String(number).padStart(2, '0'),
      story: slug,
      area: opt('area', 'W'),
      dependsOn: storyId.replace('story-', ''),
      risk: opt('risk', 'M'),
      external: opt('external', '—'),
    }));
    appendEdgeCase(dir, '/plan-split-story', `Split ${storyId}; moved tasks to ${newStoryId}-${slug}.`);
  }
  print(result, renderSplit);
}

function planningRoot() {
  const root = path.join(cwd, '.planning');
  if (!existsSync(root) || !statSync(root).isDirectory()) fail('No .planning/ directory found in the current workspace. Run /plan-init first or move to the project root.');
  return root;
}

function normalizeStoryId(value) {
  const match = String(value || '').match(/^story-0*(\d+)$/i);
  if (!match) fail(`Invalid story id: ${value}`);
  return `story-${match[1].padStart(2, '0')}`;
}

function escapeRegex(value) {
  return String(value).replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function valueFor(text, key, fallback) {
  const match = new RegExp(`^\\s*${escapeRegex(key)}:\\s*([^#\\n]+)`, 'm').exec(text);
  return match ? match[1].trim().replace(/^["']|["']$/g, '') : fallback;
}

function parseConfig() {
  const root = planningRoot();
  const file = path.join(root, 'config.yml');
  const text = existsSync(file) ? read(file) : '';
  return {
    file: existsSync(file) ? rel(file) : null,
    baseBranch: valueFor(text, 'base_branch', 'main'),
    projectType: valueFor(text, 'type', 'software'),
    requiresGit: valueFor(text, 'requires_git', 'true'),
    smokeTestsFile: valueFor(text, 'smoke_tests_file', 'SMOKE-TESTS.md'),
  };
}

function gitOutput(argsList) {
  const result = spawnSync('git', argsList, { cwd, encoding: 'utf8' });
  return result.status === 0 ? result.stdout.trim() : '';
}

function currentBranch() {
  return gitOutput(['branch', '--show-current']);
}

function storySlugFromFile(storyFile) {
  return path.basename(storyFile, '.md');
}

function branchPrefixOption() {
  const explicit = opt('worktree-prefix');
  if (explicit) return explicit.replace(/^\/+|\/+$/g, '');
  if (opt('child-worktree') === 'true') return slugify(path.basename(cwd));
  return '';
}

function deriveStoryBranch(storyFile, config) {
  const storyBase = storySlugFromFile(storyFile);
  const branch = currentBranch();
  let storyBranch = storyBase;
  const storyIndex = branch.indexOf(storyBase);
  if (storyIndex > 0) storyBranch = branch.slice(0, storyIndex + storyBase.length);
  if (branch === storyBase || branch.endsWith(`/${storyBase}`)) storyBranch = branch;
  const prefix = branchPrefixOption();
  if (prefix && storyBranch === storyBase) storyBranch = `${prefix}/${storyBase}`;
  return {
    baseBranch: config.baseBranch,
    currentBranch: branch,
    storyBranch,
    storyBase,
    worktreePrefix: storyBranch === storyBase ? '' : storyBranch.slice(0, -storyBase.length).replace(/\/$/, ''),
  };
}

function extractStatus(text) {
  const match = /^>\s*\*\*Status:\*\*\s*(.+)$/im.exec(text);
  return match ? match[1].trim() : 'UNKNOWN';
}

function setStatus(text, status) {
  return /^>\s*\*\*Status:\*\*.*$/m.test(text)
    ? text.replace(/^>\s*\*\*Status:\*\*.*$/m, `> **Status:** ${status}`)
    : text;
}

function extractField(text, name) {
  const pattern = new RegExp(`^>\\s*\\*\\*${escapeRegex(name)}:\\*\\*\\s*(.+)$|^-\\s+\\*\\*${escapeRegex(name)}:\\*\\*\\s*(.+)$`, 'im');
  const match = pattern.exec(text);
  return match ? (match[1] || match[2] || '').trim() : '';
}

function normalizeTaskId(value) {
  const match = String(value || '').match(/^task-0*(\d+)/i);
  if (!match) return '';
  return `task-${match[1].padStart(2, '0')}`;
}

function taskIdFromRow(row) {
  const linked = linkTarget(row.task);
  const source = linked ? path.basename(linked) : row.task;
  const match = source.match(/task-0*(\d+)/i);
  return match ? `task-${match[1].padStart(2, '0')}` : `task-${String(row.number).padStart(2, '0')}`;
}

function linkTarget(markdown) {
  const match = String(markdown || '').match(/\]\(([^)]+)\)/);
  return match ? match[1].trim() : '';
}

function taskTitle(markdown) {
  return titleize(String(markdown || '').replace(/\[([^\]]+)\]\([^)]+\)/g, '$1'));
}

function findTaskForRow(storyFile, row) {
  const storyDir = storyFile.replace(/\.md$/, '');
  const target = linkTarget(row.task);
  if (target) {
    const direct = path.resolve(path.dirname(storyFile), target);
    if (existsSync(direct) && statSync(direct).isFile()) return direct;
  }
  if (!existsSync(storyDir) || !statSync(storyDir).isDirectory()) return null;
  const id = taskIdFromRow(row);
  const file = readdirSync(storyDir)
    .filter((name) => name.startsWith(`${id}-`) && name.endsWith('.md'))
    .sort()[0];
  return file ? path.join(storyDir, file) : null;
}

function dependencyIds(taskText) {
  const depends = extractField(taskText, 'Depends On');
  if (!depends || ['-', '—', 'none', 'n/a'].includes(depends.toLowerCase())) return [];
  return depends.split(/,|\n/)
    .map((item) => normalizeTaskId(item.trim()))
    .filter(Boolean);
}

function taskBranchName(branches, taskFile, taskId) {
  const base = taskFile ? path.basename(taskFile, '.md') : taskId;
  return `${branches.storyBranch}--${base}`;
}

function storyExecutionContext(planningId, storyIdArg) {
  const storyId = normalizeStoryId(storyIdArg);
  const config = parseConfig();
  const dir = planningDir(planningId);
  const storyFile = findStoryFile(dir, storyId);
  const storyText = read(storyFile);
  const branches = deriveStoryBranch(storyFile, config);
  const rows = taskRows(storyText);
  const tasks = rows.map((row) => {
    const file = findTaskForRow(storyFile, row);
    const taskText = file ? read(file) : '';
    const id = taskIdFromRow(row);
    const status = file ? extractStatus(taskText) : (row.status || 'MISSING');
    return {
      id,
      number: row.number,
      title: taskTitle(row.task),
      status,
      rowStatus: row.status || '',
      file: file ? rel(file) : null,
      missingFile: !file,
      dependsOn: file ? dependencyIds(taskText) : [],
      branch: taskBranchName(branches, file, id),
    };
  });
  const statusById = new Map(tasks.map((task) => [task.id, task.status]));
  const atomized = rows.length > 0 && tasks.every((task) => !task.missingFile);
  const pending = tasks.filter((task) => task.status !== 'DONE');
  const blocked = pending
    .map((task) => ({
      ...task,
      unmetDependencies: task.dependsOn.filter((dep) => statusById.get(dep) !== 'DONE'),
    }))
    .filter((task) => task.unmetDependencies.length);
  const nextTask = pending.find((task) => task.dependsOn.every((dep) => statusById.get(dep) === 'DONE')) || null;
  const completed = tasks.filter((task) => task.status === 'DONE');
  return {
    ok: atomized && !tasks.some((task) => task.missingFile),
    command: command || '',
    write,
    planning: planningId,
    story: storyId,
    storyFile: rel(storyFile),
    storyTitle: heading(storyText) || path.basename(storyFile, '.md'),
    storyStatus: extractStatus(storyText),
    config,
    branches,
    atomized,
    tasks,
    completedTasks: completed,
    nextTask,
    blockedTasks: nextTask ? [] : blocked,
    closeoutReady: atomized && pending.length === 0,
    doneCriteria: markdownSection(storyText, 'Done Criteria').trim(),
    verificationSummaries: completed.map((task) => ({
      task: task.id,
      file: task.file,
      summary: task.file ? verificationSummary(read(path.join(cwd, task.file))) : '',
    })),
    commands: planStoryCommands(config, branches, completed, nextTask),
  };
}

function verificationSummary(taskText) {
  for (const name of ['Verification Summary', 'Verification', 'Human Review', 'Review Summary']) {
    const section = markdownSection(taskText, name).trim();
    if (section) return section.split('\n').slice(0, 12).join('\n');
  }
  return '';
}

function planStoryCommands(config, branches, completed, nextTask) {
  const commands = {
    openTaskPrCheck: [`gh pr list --base ${branches.storyBranch} --state open`],
    storyBranchSetup: [],
    completedTaskCleanup: completed.map((task) => `git branch -d ${task.branch}`),
    nextTask: nextTask ? `/plan-task <planning-id> <story-id> ${nextTask.id}` : '',
    finalize: [
      'git fetch origin',
      `git checkout ${branches.storyBranch}`,
      `git pull --ff-only origin ${branches.storyBranch}`,
      `git rebase origin/${branches.baseBranch}`,
      `git push -u origin ${branches.storyBranch}`,
      `gh pr create --title "<story-NN>: <story-name>" --body "Closes story <story-id> of planning <planning-id>." --base ${branches.baseBranch} --head ${branches.storyBranch}`,
    ],
    storyBranchCleanupAfterMerge: [
      `git checkout ${branches.baseBranch}`,
      `git pull --ff-only origin ${branches.baseBranch}`,
      `git branch -d ${branches.storyBranch}`,
    ],
  };
  if (config.requiresGit === 'true') {
    commands.storyBranchSetup = [
      'git status --porcelain',
      'git fetch origin',
      `git checkout ${branches.baseBranch}`,
      `git pull --ff-only origin ${branches.baseBranch}`,
      `git checkout -B ${branches.storyBranch}`,
      `git push -u origin ${branches.storyBranch}`,
    ];
  }
  return commands;
}

function updateExpansionStoryStatus(expansionText, storyId, status) {
  const number = Number(storyId.replace(/^story-0*/, ''));
  return expansionText.split(/\r?\n/).map((line) => {
    const cells = line.split('|');
    if (cells.length < 8 || !/^\s*\|\s*\d+/.test(line)) return line;
    if (Number(cells[1].trim()) !== number) return line;
    cells[7] = ` ${status} `;
    return cells.join('|');
  }).join('\n');
}

function mutateStoryStatus(planningId, storyId, status) {
  const dir = planningDir(planningId);
  const storyFile = findStoryFile(dir, storyId);
  const expansion = path.join(dir, '01-expansion.md');
  const touched = [rel(storyFile)];
  if (write) writeFile(storyFile, setStatus(read(storyFile), status));
  if (existsSync(expansion)) {
    touched.push(rel(expansion));
    if (write) writeFile(expansion, updateExpansionStoryStatus(read(expansion), storyId, status));
  }
  return touched;
}

function runExecuteInspect() {
  const planningId = positional[0];
  const storyId = positional[1];
  if (!planningId || !storyId) fail('Missing <planning-id> <story-NN>.', { help: usage() });
  print(storyExecutionContext(planningId, storyId), renderExecute);
}

function runExecuteStart() {
  const planningId = positional[0];
  const storyId = normalizeStoryId(positional[1]);
  if (!planningId || !storyId) fail('Missing <planning-id> <story-NN>.', { help: usage() });
  const ctx = storyExecutionContext(planningId, storyId);
  const touched = ctx.storyStatus === 'TODO' ? mutateStoryStatus(planningId, storyId, 'IN PROGRESS') : [];
  print({ ...ctx, command: 'execute-start', touched }, renderExecute);
}

function runExecuteDone() {
  const planningId = positional[0];
  const storyId = normalizeStoryId(positional[1]);
  if (!planningId || !storyId) fail('Missing <planning-id> <story-NN>.', { help: usage() });
  const ctx = storyExecutionContext(planningId, storyId);
  if (!ctx.closeoutReady) {
    print({ ...ctx, command: 'execute-done', ok: false, blockers: ['Story still has pending or missing task files.'] }, renderExecute);
    process.exit(1);
  }
  const touched = mutateStoryStatus(planningId, storyId, 'DONE');
  print({ ...ctx, command: 'execute-done', touched }, renderExecute);
}

function runExecuteFinalize() {
  const planningId = positional[0];
  const storyId = positional[1];
  if (!planningId || !storyId) fail('Missing <planning-id> <story-NN>.', { help: usage() });
  const ctx = storyExecutionContext(planningId, storyId);
  print({ ...ctx, command: 'execute-finalize' }, renderExecute);
}

function print(result, markdownRenderer) {
  if (format === 'json') console.log(JSON.stringify(result, null, 2));
  else console.log(markdownRenderer(result));
}

function renderInspect(result) {
  const lines = [`# Story Utility Inspect`, '', `Target: \`${result.target || result.planning}\``];
  if (result.mode) lines.push(`Mode: \`${result.mode}\``);
  lines.push(`Stories: ${result.storyCount}`);
  if (result.next) lines.push(`Next: \`${result.next.id || result.next.number}\``);
  if (result.overviewFiles?.length) lines.push(`Overview files: ${result.overviewFiles.map((file) => `\`${file}\``).join(', ')}`);
  if (result.stories?.length) {
    lines.push('', '| Story | Title | Status/Criteria |', '|-------|-------|-----------------|');
    for (const story of result.stories) lines.push(`| ${story.number || story.path} | ${story.title || story.story} | ${story.status || `${story.criteriaCount} criteria`} |`);
  }
  if (result.story) {
    lines.push('', `Story file: \`${result.story.file}\``, `Status: \`${result.story.status || 'unknown'}\``, `Tasks: ${result.story.taskCount}`);
  }
  return lines.join('\n');
}

function renderBacklogNew(result) {
  return [
    '# Backlog Story Draft',
    '',
    `Mode: ${result.write ? 'write' : 'dry-run'}`,
    `Target: \`${result.target}\``,
    result.id ? `ID: \`${result.id}\`` : '',
    '',
    '```md',
    result.content.trimEnd(),
    '```',
    result.write ? `\nIndex updated: ${result.indexUpdated ? 'yes' : 'no'}` : '\nRe-run with `--write` after approval.',
  ].filter(Boolean).join('\n');
}

function renderEnrich(result) {
  const lines = ['# Story Enrichment', '', `Mode: ${result.write ? 'write' : 'dry-run'}`, `File: \`${result.file}\``];
  if (result.found) lines.push(`Criteria: ${result.found.criteriaCount}`, `Missing common sections: ${result.missing.join(', ') || 'none'}`);
  lines.push(`Sections to append: ${result.sectionsToAppend?.join(', ') || 'none'}`);
  if (!result.write) lines.push('', 'Re-run with `--write` after approval.');
  return lines.join('\n');
}

function renderSplit(result) {
  const lines = ['# Story Split', '', `Mode: ${result.write ? 'write' : 'dry-run'}`];
  if (result.original) lines.push(`Original: \`${result.original}\``);
  if (result.newStory) lines.push(`New story: \`${result.newStory}\``);
  const moved = result.movedCriteria || result.movedTasks || [];
  if (moved.length) {
    lines.push('', 'Moved:');
    for (const item of moved) lines.push(`- ${item}`);
  }
  if (!result.write) lines.push('', 'Re-run with `--write` after approval.');
  return lines.join('\n');
}

function renderPlanningAdd(result) {
  return [
    '# Planning Story Draft',
    '',
    `Mode: ${result.write ? 'write' : 'dry-run'}`,
    `Planning: \`${result.planning}\``,
    `Story: \`${result.story}\``,
    `File: \`${result.file}\``,
    `Expansion: \`${result.expansion}\``,
    '',
    result.write ? 'Story file and expansion row updated.' : 'Re-run with `--write` after approval.',
  ].join('\n');
}

function renderExecute(result) {
  const lines = [`# Plan Story ${result.command.replace(/^execute-/, '') || 'inspect'}`, '', `Planning: \`${result.planning}\``, `Story: \`${result.story}\``, `Story file: \`${result.storyFile}\``, `Status: \`${result.storyStatus}\``];
  lines.push(`Branch: \`${result.branches.storyBranch}\` -> \`${result.branches.baseBranch}\``);
  lines.push(`Atomized: ${result.atomized ? 'yes' : 'no'}`);
  if (result.tasks?.length) {
    lines.push('', '| Task | Status | File | Depends On |', '|------|--------|------|------------|');
    for (const task of result.tasks) lines.push(`| ${task.id} | ${task.status} | ${task.file ? `\`${task.file}\`` : 'MISSING'} | ${task.dependsOn.join(', ') || '—'} |`);
  }
  if (result.blockers?.length) {
    lines.push('', 'Blockers:');
    for (const item of result.blockers) lines.push(`- ${item}`);
  }
  if (result.nextTask) lines.push('', `Next task: \`${result.nextTask.id}\` — ${result.nextTask.title}`, `Invoke: \`/plan-task ${result.planning} ${result.story} ${result.nextTask.id}\``);
  else if (result.blockedTasks?.length) {
    lines.push('', 'Blocked tasks:');
    for (const task of result.blockedTasks) lines.push(`- \`${task.id}\` waits for ${task.unmetDependencies.join(', ')}`);
  } else if (result.closeoutReady) {
    lines.push('', 'Closeout ready: all task files are DONE.');
  }
  const missing = result.tasks?.filter((task) => task.missingFile) || [];
  if (missing.length) {
    lines.push('', 'Missing task files:');
    for (const task of missing) lines.push(`- \`${task.id}\` from story task table`);
  }
  if (result.doneCriteria && (result.command === 'execute-inspect' || result.command === 'execute-finalize')) {
    lines.push('', '## Done Criteria', '', result.doneCriteria);
  }
  if (result.verificationSummaries?.some((item) => item.summary) && (result.command === 'execute-inspect' || result.command === 'execute-finalize')) {
    lines.push('', '## Task Verification Summaries');
    for (const item of result.verificationSummaries.filter((entry) => entry.summary)) {
      lines.push('', `### ${item.task}`, '', item.summary);
    }
  }
  if (result.commands && result.command === 'execute-finalize') {
    lines.push('', 'Finalize commands:');
    for (const cmd of result.commands.finalize) lines.push(`- \`${cmd}\``);
    lines.push('', 'Story branch cleanup after merge:');
    for (const cmd of result.commands.storyBranchCleanupAfterMerge) lines.push(`- \`${cmd}\``);
  }
  if (result.touched?.length) {
    lines.push('', 'Touched paths:');
    for (const file of result.touched) lines.push(`- \`${file}\``);
    if (!result.write) lines.push('', 'Dry run only. Re-run with `--write` after approval.');
  }
  return lines.join('\n');
}

switch (command) {
  case 'backlog-inspect':
    print(inspectContainer(positional[0]), renderInspect);
    break;
  case 'backlog-new':
    runBacklogNew();
    break;
  case 'backlog-enrich':
    runBacklogEnrich();
    break;
  case 'backlog-split':
    runBacklogSplit();
    break;
  case 'planning-inspect':
    print(inspectPlanning(positional[0], positional[1]), renderInspect);
    break;
  case 'planning-add-story':
    runPlanningAddStory();
    break;
  case 'planning-enrich-story':
    runPlanningEnrichStory();
    break;
  case 'planning-split-story':
    runPlanningSplitStory();
    break;
  case 'execute-inspect':
    runExecuteInspect();
    break;
  case 'execute-start':
    runExecuteStart();
    break;
  case 'execute-done':
    runExecuteDone();
    break;
  case 'execute-finalize':
    runExecuteFinalize();
    break;
  case '--help':
  case '-h':
  case undefined:
    console.log(usage());
    break;
  default:
    fail(`Unknown command: ${command}`, { help: usage() });
}
