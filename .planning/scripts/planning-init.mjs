#!/usr/bin/env node
import { execFileSync } from 'node:child_process';
import {
  copyFileSync,
  existsSync,
  mkdirSync,
  readdirSync,
  readFileSync,
  statSync,
  writeFileSync,
} from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const scriptPath = fileURLToPath(import.meta.url);
const templateDir = path.resolve(path.dirname(scriptPath), '..');
const cwd = process.cwd();
const planningDir = path.join(cwd, '.planning');

const args = process.argv.slice(2);
let blank = false;
let force = false;
let detectOnly = false;
let format = 'markdown';
let areasFile = '';
let projectMode = 'software';
let baseBranch = '';

function usage() {
  return `Usage:
  node <planning-template>/scripts/planning-init.mjs [--blank] [--force] [--detect-only] [--areas-file areas.json] [--project-mode mode] [--base-branch branch] [--format markdown|json]

Initializes .planning/ in the current directory from the planning template.`;
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

for (let i = 0; i < args.length; i += 1) {
  const arg = args[i];
  if (arg === '--blank') blank = true;
  else if (arg === '--force') force = true;
  else if (arg === '--detect-only') detectOnly = true;
  else if (arg === '--areas-file') {
    areasFile = args[i + 1] || '';
    i += 1;
  } else if (arg === '--project-mode') {
    projectMode = args[i + 1] || '';
    i += 1;
  } else if (arg === '--base-branch') {
    baseBranch = args[i + 1] || '';
    i += 1;
  } else if (arg === '--format') {
    format = args[i + 1] || '';
    i += 1;
  } else if (arg === '-h' || arg === '--help') {
    console.log(usage());
    process.exit(0);
  } else {
    fail(`Unexpected argument: ${arg}`, { help: usage() });
  }
}

if (!['markdown', 'json'].includes(format)) fail(`Invalid --format: ${format}`, { help: usage() });

const validModes = new Set(['software', 'general', 'documentation', 'research', 'operations']);
if (!validModes.has(projectMode)) fail(`Invalid --project-mode: ${projectMode}`, { help: usage() });

function rel(file) {
  const relative = path.relative(cwd, file);
  if (!relative) return '.';
  return relative.startsWith('..') ? file : relative;
}

function shouldExcludeDir(name) {
  return [
    '.git',
    '.planning',
    'node_modules',
    '__pycache__',
    'target',
    'build',
    'dist',
    'out',
    '.next',
  ].includes(name);
}

function listTopLevelDirs(root) {
  return readdirSync(root, { withFileTypes: true })
    .filter((entry) => entry.isDirectory())
    .map((entry) => entry.name)
    .filter((name) => !name.startsWith('.') && !shouldExcludeDir(name))
    .sort();
}

function hasFile(dir, file) {
  return existsSync(path.join(cwd, dir, file));
}

function hasGlobLike(dir, predicate) {
  try {
    return readdirSync(path.join(cwd, dir)).some(predicate);
  } catch {
    return false;
  }
}

function detectStack(dir) {
  if (hasFile(dir, 'package.json')) {
    const pkgPath = path.join(cwd, dir, 'package.json');
    try {
      const pkg = JSON.parse(readFileSync(pkgPath, 'utf8'));
      const deps = { ...(pkg.dependencies || {}), ...(pkg.devDependencies || {}) };
      if (deps.next) return 'JavaScript / TypeScript - Next.js';
      if (deps.vite) return 'JavaScript / TypeScript - Vite';
      if (deps.express) return 'JavaScript / TypeScript - Express';
    } catch {
      // Fall through to generic package.json stack.
    }
    return 'JavaScript / TypeScript';
  }
  if (hasFile(dir, 'pom.xml')) return 'Java / Maven';
  if (hasGlobLike(dir, (entry) => entry === 'build.gradle' || entry === 'build.gradle.kts')) return 'Java / Gradle';
  if (hasFile(dir, 'pyproject.toml') || hasFile(dir, 'requirements.txt')) return 'Python';
  if (hasFile(dir, 'go.mod')) return 'Go';
  if (hasGlobLike(dir, (entry) => entry.endsWith('.tf')) || existsSync(path.join(cwd, dir, 'terraform'))) return 'Infrastructure / Terraform';
  try {
    const files = readdirSync(path.join(cwd, dir), { withFileTypes: true }).filter((entry) => entry.isFile());
    if (files.length > 0 && files.every((entry) => entry.name.endsWith('.md') || entry.name.endsWith('.rst'))) return 'Documentation';
  } catch {
    // Ignore unreadable dirs.
  }
  return 'Generic project area';
}

function baseCodeForDir(dir) {
  const normalized = dir.toLowerCase();
  const last = normalized.split('/').pop() || normalized;
  if (/(api|backend|server)/.test(last)) return 'AP';
  if (/(web|frontend|ui|client)/.test(last)) return 'WB';
  if (/(docs|documentation|doc)/.test(last)) return 'DO';
  if (/(infra|infrastructure|terraform|deploy)/.test(last)) return 'IN';
  if (/(agent|agents|ai|ml)/.test(last)) return 'AG';
  if (/(mobile|ios|android)/.test(last)) return 'MB';
  if (/(lib|shared|common|core)/.test(last)) return 'LB';
  if (/(services|svc)/.test(last)) return 'SV';
  return last.replace(/[^a-z0-9]/g, '').slice(0, 2).toUpperCase() || 'AR';
}

function descriptionFor(dir, stack) {
  if (stack.includes('Documentation')) return 'documentation';
  if (stack.includes('Terraform')) return 'infrastructure';
  if (/api|backend|server/i.test(dir)) return 'backend service';
  if (/web|frontend|ui|client/i.test(dir)) return 'frontend';
  return stack.toLowerCase();
}

function disambiguate(code, used) {
  if (!used.has(code)) {
    used.add(code);
    return code;
  }
  let index = 1;
  while (used.has(`${code}${index}`)) index += 1;
  const next = `${code}${index}`;
  used.add(next);
  return next;
}

function detectAreas() {
  const dirs = [];
  for (const dir of listTopLevelDirs(cwd)) {
    if (dir === 'packages') {
      const packageRoot = path.join(cwd, dir);
      const children = readdirSync(packageRoot, { withFileTypes: true })
        .filter((entry) => entry.isDirectory() && !shouldExcludeDir(entry.name))
        .map((entry) => `${dir}/${entry.name}`)
        .sort();
      if (children.length > 0) {
        dirs.push(...children);
        continue;
      }
    }
    dirs.push(dir);
  }

  const used = new Set();
  return dirs.map((dir) => {
    const stack = detectStack(dir);
    const code = disambiguate(baseCodeForDir(dir), used);
    return {
      code,
      dir,
      stack,
      description: descriptionFor(dir, stack),
    };
  });
}

function readAreas() {
  if (blank) return [];
  if (!areasFile) return detectAreas();
  const parsed = JSON.parse(readFileSync(path.resolve(cwd, areasFile), 'utf8'));
  if (!Array.isArray(parsed)) fail('--areas-file must contain a JSON array.');
  return parsed.map((area) => ({
    code: String(area.code || '').trim().toUpperCase(),
    dir: String(area.dir || '').replace(/\/$/, ''),
    stack: String(area.stack || 'Generic project area'),
    description: String(area.description || 'project area'),
  })).filter((area) => area.code && area.dir);
}

function detectBaseBranch() {
  if (baseBranch) return baseBranch;
  try {
    const output = execFileSync('git', ['remote', 'show', 'origin'], {
      cwd,
      encoding: 'utf8',
      stdio: ['ignore', 'pipe', 'ignore'],
    });
    const line = output.split('\n').find((item) => item.includes('HEAD branch:'));
    const branch = line?.split(':').pop()?.trim();
    if (branch) return branch;
  } catch {
    // Fall back below.
  }
  return 'main';
}

function isForcePreservedPath(relative) {
  if (!force) return false;
  if (!relative) return false;
  const normalized = relative.split(path.sep).join('/');
  if (normalized === 'active' || normalized.startsWith('active/')) return true;
  if (normalized === 'finished' || normalized.startsWith('finished/')) return true;
  if (normalized === '_template' || normalized.startsWith('_template/')) return true;
  if (normalized === 'WORKFLOWS/05-SDLC-PHASE-GUIDANCE' || normalized.startsWith('WORKFLOWS/05-SDLC-PHASE-GUIDANCE/')) return true;
  return [
    'README.md',
    'GUIDE.md',
    'GLOSSARY.md',
    'TRACEABILITY-GLOBAL.md',
    'SMOKE-TESTS.md',
    'LOGGING.md',
    'PDR-TEMPLATE.md',
    'PROMPTING.md',
    'config.yml',
  ].includes(normalized);
}

function copyRecursive(source, target, copied, skipped, relative = '') {
  const sourceStat = statSync(source);
  if (sourceStat.isDirectory()) {
    mkdirSync(target, { recursive: true });
    for (const entry of readdirSync(source, { withFileTypes: true })) {
      const childRelative = relative ? path.join(relative, entry.name) : entry.name;
      const normalizedChild = childRelative.split(path.sep).join('/');
      if (
        force
        && entry.isDirectory()
        && (normalizedChild === 'active' || normalizedChild === 'finished')
        && existsSync(path.join(target, entry.name))
      ) {
        skipped.push(childRelative);
        continue;
      }
      if (!entry.isDirectory() && isForcePreservedPath(childRelative) && existsSync(path.join(target, entry.name))) {
        skipped.push(childRelative);
        continue;
      }
      copyRecursive(path.join(source, entry.name), path.join(target, entry.name), copied, skipped, childRelative);
    }
    return;
  }
  mkdirSync(path.dirname(target), { recursive: true });
  copyFileSync(source, target);
  copied.push(relative);
}

function replaceAreaTable(file, marker, replacement, terminator) {
  const full = path.join(planningDir, file);
  let text = readFileSync(full, 'utf8');
  const start = text.indexOf(marker);
  if (start === -1) return false;
  const end = terminator ? text.indexOf(terminator, start) : -1;
  if (end === -1) {
    text = `${text.slice(0, start)}${replacement}${text.slice(start + marker.length)}`;
  } else {
    text = `${text.slice(0, start)}${replacement}\n\n${text.slice(end)}`;
  }
  writeFileSync(full, text);
  return true;
}

function areaFilename(area) {
  const slug = area.dir.replace(/[^a-zA-Z0-9]+/g, '-').replace(/^-|-$/g, '').toLowerCase();
  return `AREA-${area.code}-${slug}.md`;
}

function renderAreaFile(area) {
  return `# Area ${area.code} - ${area.dir}/

> [<- README](README.md)
>
> **Generated by plan-init.** Update the stack details, doc references, and rules to match your project.

**What This Area Is**: ${area.description}

---

| Item | Value |
|------|-------|
| Area code | \`${area.code}\` |
| Directory | \`${area.dir}/\` |
| Stack | ${area.stack} |
| Key doc references | [Which docs govern contracts for this area? Fill in.] |

**Key Checks**: [\`[CHECK-AGNOSTIC-BOUNDARY]\`](../04-SUB-WORKFLOWS/CHECK-AGNOSTIC-BOUNDARY.md)

---

## Done Criteria

- [ ] Output placed in the correct directory under \`${area.dir}/\`
- [ ] Follows the naming and structure conventions of \`${area.dir}/\`
- [ ] No contradictions introduced with existing documentation
- [ ] TRACEABILITY.md updated with any new terms introduced

---

## Key Rules

- [Add the architectural rules and constraints that govern this area]

---

> [<- README](README.md)
`;
}

function renderGuideTable(areas) {
  const rows = areas.map((area) => `| \`${area.code}\` | \`${area.dir}/\` - ${area.description} |`);
  rows.push('| `W` | `.planning/` - meta-workflow (this system) |');
  return `<!-- AREAS-TABLE: populated by plan-init - edit manually if you add or remove areas later -->
| Code | Repository / Area |
|------|------------------|
${rows.join('\n')}`;
}

function renderAreasRef(areas) {
  const rows = areas.map((area) => `| \`${area.code}\` | ${area.description} (\`${area.dir}/\`) |`);
  rows.push('| `W` | Planning System (`.planning/`) |');
  return `<!-- AREAS-REF: populated by plan-init - keep in sync with GUIDE.md AREAS-TABLE -->
| Code | Area |
|------|------|
${rows.join('\n')}`;
}

function renderWorkflowTable(areas) {
  const rows = areas.map((area) => `| ${area.code} | \`${area.dir}/\` - ${area.description} | [fill in] | [${areaFilename(area)}](${areaFilename(area)}) |`);
  rows.push('| W | `.planning/` - meta-workflow | *(built-in)* | *(built-in)* |');
  return `<!-- AREAS-TABLE: populated by plan-init based on project structure - one AREA-<CODE>-<dir>.md file per area -->
| Code | Area | Key Doc References | File |
|------|------|--------------------|------|
${rows.join('\n')}`;
}

function renderGlobalMatrix(areas) {
  const headers = ['Term / Concept', 'Source Planning', ...areas.map((area) => area.code), 'W'];
  const sep = headers.map(() => '---');
  const areaCells = areas.map(() => 'N/A').join(' | ');
  return `<!-- MATRIX-HEADER: plan-init adds area columns after "Source Planning" -->
| ${headers.join(' | ')} |
| ${sep.join(' | ')} |
| Planning System | framework bootstrap | ${areaCells ? `${areaCells} | ` : ''}✅ |
| Workflow (meta) | framework bootstrap | ${areaCells ? `${areaCells} | ` : ''}✅ |
| Fundamental Rule | framework bootstrap | ${areaCells ? `${areaCells} | ` : ''}✅ |`;
}

function renderLocalMatrix(areas) {
  const headers = ['Term / Concept', ...areas.map((area) => area.code), 'W', 'Notes'];
  const sep = headers.map(() => '---');
  const blanks = areas.map(() => '').join(' | ');
  return `<!-- MATRIX-HEADER: plan-init adds one column per area between "Term / Concept" and "Notes" -->
| ${headers.join(' | ')} |
| ${sep.join(' | ')} |
| *[term or concept, e.g. password reset token]* | ${blanks ? `${blanks} | ` : ''} | [Which area owns or consumes this concept?] |`;
}

function writeAreaConfiguration(areas) {
  const areaDir = path.join(planningDir, 'WORKFLOWS', '05-SDLC-PHASE-GUIDANCE');
  mkdirSync(areaDir, { recursive: true });
  for (const area of areas) {
    writeFileSync(path.join(areaDir, areaFilename(area)), renderAreaFile(area));
  }

  replaceAreaTable('GUIDE.md', '<!-- AREAS-TABLE:', renderGuideTable(areas), '> This table is filled in');
  replaceAreaTable('WORKFLOWS/05-SDLC-PHASE-GUIDANCE/README.md', '<!-- AREAS-TABLE:', renderWorkflowTable(areas), '> Area files are generated');
  replaceAreaTable('TRACEABILITY-GLOBAL.md', '<!-- AREAS-REF:', renderAreasRef(areas), 'Cell values:');
  replaceAreaTable('_template/TRACEABILITY.md', '<!-- AREAS-REF:', renderAreasRef(areas), '**Cell values:**');
  replaceAreaTable('TRACEABILITY-GLOBAL.md', '<!-- MATRIX-HEADER:', renderGlobalMatrix(areas), '---\n\n## Consolidated Residuals');
  replaceAreaTable('_template/TRACEABILITY.md', '<!-- MATRIX-HEADER:', renderLocalMatrix(areas), '---\n\n## Decisions Made');
}

function writeConfig() {
  const configPath = path.join(planningDir, 'config.yml');
  if (force && existsSync(configPath)) {
    const existing = readFileSync(configPath, 'utf8');
    const branch = existing.match(/^\s*base_branch:\s*([^#\n]+)/m)?.[1]?.trim().replace(/^["']|["']$/g, '') || 'main';
    return { branch, written: false };
  }
  const mode = projectMode;
  const requiresTests = mode === 'software';
  const requiresGit = mode === 'software' || mode === 'documentation';
  const branch = detectBaseBranch();
  const config = `# Planning system configuration - edit as needed.
# Generated by /plan-init.

git:
  base_branch: ${branch}   # Story branches start here; final story pull requests target this branch

project:
  type: ${mode}      # software | general | documentation | research | operations

terminology:
  planning_item: story          # story | workstream | deliverable | experiment | initiative
  verification_label: Verification

autonomy:
  level: assisted       # manual | assisted | autonomous

execution:
  requires_git: ${requiresGit}
  requires_tests: ${requiresTests}

software:
  smoke_tests_file: SMOKE-TESTS.md   # Optional override for the smoke-test plan file under .planning/
  test_suite_generator: scripts/generate-test-suite.sh   # Deterministic test-suite matrix generator under .planning/
  logging_file: LOGGING.md   # Project logging policy under .planning/

integrations:
  external_issues: disabled     # disabled | github | jira | linear

docs:
  output_dir: docs
`;
  writeFileSync(configPath, config);
  return { branch, written: true };
}

function report(payload) {
  if (format === 'json') {
    console.log(JSON.stringify(payload, null, 2));
    return;
  }

  if (payload.detectOnly) {
    console.log('# Planning Init Detection\n');
    console.log('Proposed areas:\n');
    for (const area of payload.areas) console.log(`- \`${area.code}\` | \`${area.dir}/\` - ${area.description} (${area.stack})`);
    return;
  }

  console.log('# Planning Init\n');
  console.log(`Initialized: \`${payload.planningDir}\``);
  console.log(`Mode: ${payload.blank ? 'blank' : 'area-configured'}${payload.force ? ' (force)' : ''}`);
  console.log(`Base branch: \`${payload.baseBranch}\``);
  if (payload.force) {
    console.log(`Preserved existing project-specific files: ${payload.preservedCount}`);
  }
  if (payload.areas.length > 0) {
    console.log('\nAreas configured:\n');
    for (const area of payload.areas) console.log(`- \`${area.code}\` | \`${area.dir}/\` - ${area.description}`);
  } else {
    console.log('\nArea configuration skipped.');
  }
  console.log('\nNext steps:');
  console.log('- `/plan-new NNN-slug -- intent`');
  console.log('- `/plan-from-epic NNN path/to/epic/`');
}

const areas = readAreas();

if (detectOnly) {
  report({ ok: true, detectOnly: true, areas });
  process.exit(0);
}

if (existsSync(planningDir) && !force) {
  fail('`.planning/` already exists. Use --force to reinitialize system files without touching active plannings or area configuration.');
}

const copied = [];
const skipped = [];
copyRecursive(templateDir, planningDir, copied, skipped);
mkdirSync(path.join(planningDir, 'active'), { recursive: true });
mkdirSync(path.join(planningDir, 'finished'), { recursive: true });
if (!existsSync(path.join(planningDir, 'active', 'README.md'))) copyFileSync(path.join(templateDir, 'active', 'README.md'), path.join(planningDir, 'active', 'README.md'));
if (!existsSync(path.join(planningDir, 'finished', 'README.md'))) copyFileSync(path.join(templateDir, 'finished', 'README.md'), path.join(planningDir, 'finished', 'README.md'));

if (!blank && !force) writeAreaConfiguration(areas);
const configResult = writeConfig();

report({
  ok: true,
  detectOnly: false,
  planningDir: rel(planningDir),
  blank,
  force,
  areas: blank || force ? [] : areas,
  baseBranch: configResult.branch,
  configWritten: configResult.written,
  copiedCount: copied.length,
  skippedCount: skipped.length,
  preservedCount: skipped.length,
});
