#!/usr/bin/env node
import { spawnSync } from 'node:child_process';
import { existsSync } from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const scriptDir = path.dirname(fileURLToPath(import.meta.url));
const args = process.argv.slice(2);
const command = args.shift();

const commands = {
  archive: 'planning-archive.mjs',
  clone: 'planning-clone.mjs',
  done: 'planning-done.mjs',
  merge: 'planning-merge.mjs',
  retry: 'planning-retry.mjs',
  rollback: 'planning-rollback.mjs',
  'story-skip': 'planning-story-skip.mjs',
};

function usage() {
  return `Usage:
  node .planning/scripts/planning-mutate.mjs <command> [args...]

Commands:
  archive    Audit and move a completed planning to finished/
  clone       Clone a planning into a new INITIAL planning
  done        Mark task/story completion state
  merge       Move a story between active plannings
  retry       Prepare BLOCKED stories for retry
  rollback   Reset a DONE story to TODO
  story-skip Mark a story SKIPPED`;
}

if (command === '--help' || command === '-h') {
  console.log(usage());
  process.exit(0);
}

if (!command || !commands[command]) {
  console.error(`ERROR: Unknown or missing mutation command: ${command || '(missing)'}`);
  console.error(`\n${usage()}`);
  process.exit(1);
}

const target = path.join(scriptDir, commands[command]);
if (!existsSync(target)) {
  console.error(`ERROR: Missing mutation implementation: ${path.relative(process.cwd(), target)}`);
  console.error('Re-run /plan-init --force from the project root or copy the current planning template scripts into .planning/scripts/.');
  process.exit(1);
}

const result = spawnSync(process.execPath, [target, ...args], {
  cwd: process.cwd(),
  stdio: 'inherit',
});

if (result.error) {
  console.error(`ERROR: Failed to run mutation command ${command}: ${result.error.message}`);
  process.exit(1);
}

process.exit(result.status ?? 1);
