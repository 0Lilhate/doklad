#!/usr/bin/env node
// qmd-bootstrap.ts — initialize qmd index for this vault.
// Reads vault-manifest.json (qmd_index, qmd_context) and registers + indexes + embeds.
// Idempotent — safe to re-run.
// Usage: node --experimental-strip-types .claude/scripts/qmd-bootstrap.ts

import { readFileSync, existsSync } from "node:fs";
import { execSync } from "node:child_process";
import { resolve } from "node:path";

const manifestPath = resolve("vault-manifest.json");

if (!existsSync(manifestPath)) {
  console.error("ERROR: vault-manifest.json not found in current directory.");
  console.error("Run from vault root.");
  process.exit(1);
}

interface Manifest {
  qmd_index?: string;
  qmd_context?: string;
}

let manifest: Manifest;
try {
  manifest = JSON.parse(readFileSync(manifestPath, "utf8"));
} catch (e) {
  console.error(`Cannot parse vault-manifest.json: ${(e as Error).message}`);
  process.exit(1);
}

if (!manifest.qmd_index) {
  console.error("ERROR: vault-manifest.json missing 'qmd_index' field");
  process.exit(1);
}

const index = manifest.qmd_index;
const ctx = manifest.qmd_context ?? "";

// Verify qmd is available
try {
  execSync("qmd --help", { stdio: "pipe" });
} catch {
  console.error("ERROR: qmd CLI not found in PATH.");
  console.error("Install qmd, then re-run this script.");
  process.exit(1);
}

console.log(`Bootstrapping qmd index '${index}'...`);

// Step 1: register collection (idempotent — qmd handles existing)
try {
  if (ctx) {
    execSync(`qmd --index "${index}" register --context "${ctx.replace(/"/g, '\\"')}"`, {
      stdio: "inherit",
    });
  } else {
    execSync(`qmd --index "${index}" register`, { stdio: "inherit" });
  }
} catch {
  console.warn("Register step failed or already registered. Continuing.");
}

// Step 2: full update (walks vault, updates SQLite store)
console.log(`\nIndexing vault contents...`);
try {
  execSync(`qmd --index "${index}" update`, { stdio: "inherit" });
} catch (e) {
  console.error(`update failed: ${(e as Error).message}`);
  process.exit(1);
}

// Step 3: embeddings (slower)
console.log(`\nGenerating embeddings (slower, may take minutes on first run)...`);
try {
  execSync(`qmd --index "${index}" embed`, { stdio: "inherit" });
} catch (e) {
  console.error(`embed failed: ${(e as Error).message}`);
  process.exit(1);
}

console.log(`\n✓ Bootstrap complete.`);
console.log(`Test: qmd --index "${index}" query "test"`);
console.log(`Status: qmd --index "${index}" status`);
