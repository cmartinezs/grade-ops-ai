#!/usr/bin/env node
import { execFileSync } from 'node:child_process';
import { existsSync, mkdirSync, readdirSync, readFileSync, copyFileSync } from 'node:fs';
import { homedir } from 'node:os';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const scriptPath = fileURLToPath(import.meta.url);
const scriptDir = path.dirname(scriptPath);
const cwd = process.cwd();

const args = process.argv.slice(2);
let from = '';
let to = '';
let dryRun = false;
let allowDirty = false;
let format = 'markdown';

function usage() {
  return `Usage:
  node .planning/scripts/update-version.mjs <from> <to> [--dry-run] [--allow-dirty] [--format markdown|json]

Resolves adjacent major-pair migrations, performs safety preflight, copies bundled
migration files into .planning/update-version/ when needed, and reports the
ordered migration files an agent must read and apply.`;
}

function fail(message, details = {}) {
  if (format === 'json') {
    console.log(JSON.stringify({ ok: false, error: message, ...details }, null, 2));
  } else {
    console.error(`ERROR: ${message}`);
    if (details.help) console.error(`\n${details.help}`);
  }
  process.exit(1);
}

function normalizeVersion(value) {
  return value.replace(/^v/i, '').trim();
}

function majorOf(value) {
  const normalized = normalizeVersion(value);
  const match = normalized.match(/^(\d+)(?:\.|$)/);
  if (!match) fail(`Invalid version label: ${value}`, { help: usage() });
  return Number(match[1]);
}

function rel(file) {
  const relative = path.relative(cwd, file);
  if (!relative) return '.';
  return relative.startsWith('..') ? file : relative;
}

function isGitRepo() {
  try {
    execFileSync('git', ['rev-parse', '--is-inside-work-tree'], {
      cwd,
      stdio: ['ignore', 'pipe', 'ignore'],
    });
    return true;
  } catch {
    return false;
  }
}

function gitStatus() {
  try {
    return execFileSync('git', ['status', '--porcelain'], {
      cwd,
      encoding: 'utf8',
      stdio: ['ignore', 'pipe', 'ignore'],
    }).trim();
  } catch {
    return '';
  }
}

function findClaudePluginTemplateDirs() {
  const roots = [
    path.join(homedir(), '.claude', 'plugins'),
    path.join(homedir(), '.claude'),
  ];
  const found = [];
  const seen = new Set();

  function walk(dir, depth) {
    if (depth > 6 || !existsSync(dir)) return;
    let entries = [];
    try {
      entries = readdirSync(dir, { withFileTypes: true });
    } catch {
      return;
    }
    for (const entry of entries) {
      const full = path.join(dir, entry.name);
      if (!entry.isDirectory()) continue;
      if (entry.name === 'planning-template' && full.includes('claude-planning-with-ai')) {
        const updateDir = path.join(full, 'update-version');
        if (existsSync(updateDir) && !seen.has(updateDir)) {
          found.push(updateDir);
          seen.add(updateDir);
        }
        continue;
      }
      if (['node_modules', '.git'].includes(entry.name)) continue;
      walk(full, depth + 1);
    }
  }

  for (const root of roots) walk(root, 0);
  return found;
}

for (let i = 0; i < args.length; i += 1) {
  const arg = args[i];
  if (arg === '--dry-run') {
    dryRun = true;
  } else if (arg === '--allow-dirty') {
    allowDirty = true;
  } else if (arg === '--format') {
    format = args[i + 1] || '';
    i += 1;
  } else if (!from) {
    from = arg;
  } else if (!to) {
    to = arg;
  } else {
    fail(`Unexpected argument: ${arg}`, { help: usage() });
  }
}

if (!['markdown', 'json'].includes(format)) fail(`Invalid --format: ${format}`, { help: usage() });

if (!from || !to) fail('Missing <from> <to> version labels.', { help: usage() });

const fromMajor = majorOf(from);
const toMajor = majorOf(to);

if (toMajor < fromMajor) fail('Downgrades are not supported.', { from, to });
if (toMajor === fromMajor) {
  fail('Patch and minor updates do not require an update-version migration.', { from, to });
}

const planningDir = path.join(cwd, '.planning');
if (!existsSync(planningDir)) {
  fail('No .planning/ directory found in the current workspace. Run /plan-init first or move to the project root.');
}

const chain = [];
for (let major = fromMajor; major < toMajor; major += 1) {
  chain.push(`${major}-${major + 1}.md`);
}

const planningUpdateDir = path.join(planningDir, 'update-version');
const bundledUpdateDir = path.resolve(scriptDir, '..', 'update-version');
const candidateDirs = [
  planningUpdateDir,
  bundledUpdateDir,
  ...findClaudePluginTemplateDirs(),
].filter((dir, index, arr) => existsSync(dir) && arr.indexOf(dir) === index);

const migrations = [];
const missing = [];

for (const file of chain) {
  let source = '';
  let sourceKind = '';
  for (const dir of candidateDirs) {
    const candidate = path.join(dir, file);
    if (existsSync(candidate)) {
      source = candidate;
      sourceKind = dir === planningUpdateDir ? 'workspace' : 'template';
      break;
    }
  }

  if (!source) {
    missing.push(file);
    continue;
  }

  const target = path.join(planningUpdateDir, file);
  migrations.push({
    file,
    source,
    target,
    sourceKind,
    copied: false,
    wouldCopy: source !== target,
    bytes: readFileSync(source).length,
  });
}

if (missing.length > 0) {
  fail('One or more adjacent migration files are missing.', {
    chain,
    missing,
    checkedDirectories: candidateDirs.map(rel),
  });
}

const dirty = isGitRepo() ? gitStatus() : '';
if (dirty && !dryRun && !allowDirty) {
  fail('Git worktree has existing changes. Commit/stash them or re-run with --allow-dirty.', {
    dirtyFiles: dirty.split('\n'),
  });
}

if (!dryRun) {
  mkdirSync(planningUpdateDir, { recursive: true });
  for (const migration of migrations) {
    if (migration.source !== migration.target) {
      copyFileSync(migration.source, migration.target);
      migration.copied = true;
      migration.source = migration.target;
      migration.sourceKind = 'workspace';
    }
  }
}

const report = {
  ok: true,
  from,
  to,
  fromMajor,
  toMajor,
  dryRun,
  allowDirty,
  dirty: dirty ? dirty.split('\n') : [],
  chain,
  checkedDirectories: candidateDirs.map(rel),
  migrations: migrations.map((migration) => ({
    file: migration.file,
    path: rel(migration.copied || migration.sourceKind === 'workspace' ? migration.target : migration.source),
    source: rel(migration.source),
    copied: migration.copied,
    wouldCopy: migration.wouldCopy,
    bytes: migration.bytes,
  })),
  nextSteps: [
    'Read each migration file fully in listed order.',
    'Apply one migration completely before starting the next.',
    'Run the verification commands defined by each migration.',
    'Record the final applied summary in .planning/update-version/APPLIED.md after changes are complete.',
  ],
};

if (format === 'json') {
  console.log(JSON.stringify(report, null, 2));
} else {
  console.log(`# Update-Version Preflight\n`);
  console.log(`Version range: \`${from}\` -> \`${to}\``);
  console.log(`Mode: ${dryRun ? 'dry-run' : 'apply-preflight'}`);
  console.log(`Migration chain: ${chain.map((item) => `\`${item}\``).join(' -> ')}\n`);

  console.log(`## Migration Files\n`);
  for (const migration of report.migrations) {
    const copyNote = migration.copied
      ? 'copied into workspace'
      : migration.wouldCopy && dryRun
        ? 'would copy into workspace'
        : 'workspace file';
    console.log(`- \`${migration.path}\` (${copyNote}, ${migration.bytes} bytes)`);
  }

  console.log(`\n## Safety\n`);
  if (report.dirty.length > 0) {
    console.log(`- Git worktree had existing changes and ${allowDirty ? '`--allow-dirty` was passed' : '`--dry-run` was passed'}.`);
    for (const file of report.dirty) console.log(`  - \`${file}\``);
  } else {
    console.log(`- Git worktree is clean, or this directory is not a git worktree.`);
  }

  console.log(`\n## Next Steps\n`);
  for (const step of report.nextSteps) console.log(`- ${step}`);
}
