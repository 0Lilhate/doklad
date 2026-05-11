#!/usr/bin/env node
// validate-schemas.ts — validate vault-manifest.json schema + per-note frontmatter
// against rules from vault-manifest.json.
//
// No external deps — uses simple validators (Node's stdlib only).
// Sufficient for the schemas we have.
//
// Usage:
//   node --experimental-strip-types .claude/scripts/validate-schemas.ts
//   node --experimental-strip-types .claude/scripts/validate-schemas.ts --json
//
// Exit codes:
//   0 — all valid
//   1 — violations found
//   2 — manifest itself invalid

import { readFileSync, existsSync, readdirSync, statSync } from "node:fs";
import { resolve, join, relative } from "node:path";

const ROOT = process.cwd();
const MANIFEST_PATH = resolve(ROOT, "vault-manifest.json");
const SCHEMA_PATH = resolve(ROOT, ".claude/schemas/vault-manifest.schema.json");

interface Manifest {
  claude_mind_version: string;
  vault_name: string;
  qmd_index: string;
  allowed_type_values: string[];
  allowed_status_values: string[];
  frontmatter_required: Record<string, string[]>;
  field_aliases?: Record<string, string[]>;
  user_content_roots: string[];
  infrastructure: string[];
  bilingual?: {
    key_categories_require_bilingual_aliases?: string[];
  };
}

interface Violation {
  file: string;
  kind: string;
  message: string;
}

const isJson = process.argv.includes("--json");

function fail(msg: string, code: number = 2): never {
  if (isJson) {
    console.log(JSON.stringify({ error: msg }, null, 2));
  } else {
    console.error(`ERROR: ${msg}`);
  }
  process.exit(code);
}

// --- Load and validate manifest itself ---

if (!existsSync(MANIFEST_PATH)) fail("vault-manifest.json not found in cwd");
if (!existsSync(SCHEMA_PATH)) fail(".claude/schemas/vault-manifest.schema.json not found");

let manifest: Manifest;
try {
  manifest = JSON.parse(readFileSync(MANIFEST_PATH, "utf8"));
} catch (e) {
  fail(`vault-manifest.json parse error: ${(e as Error).message}`);
}

// Manifest required fields (subset check — full schema validation needs ajv)
const manifestRequired = [
  "claude_mind_version",
  "vault_name",
  "qmd_index",
  "allowed_type_values",
  "allowed_status_values",
  "frontmatter_required",
  "infrastructure",
  "user_content_roots",
];
const manifestMissing = manifestRequired.filter((k) => !(k in manifest));
if (manifestMissing.length > 0) {
  fail(`vault-manifest.json missing required fields: ${manifestMissing.join(", ")}`);
}

if (!manifest.frontmatter_required.default) {
  fail("frontmatter_required.default is required in vault-manifest.json");
}

// --- Walk vault and validate each note's frontmatter ---

const EXCLUDES = new Set([
  ".obsidian", ".git", ".idea", "node_modules",
  "_archive", ".claude", "venv", ".venv", "__pycache__",
]);

function walk(dir: string, files: string[] = []): string[] {
  for (const entry of readdirSync(dir)) {
    if (EXCLUDES.has(entry)) continue;
    const full = join(dir, entry);
    const st = statSync(full);
    if (st.isDirectory()) {
      walk(full, files);
    } else if (entry.endsWith(".md")) {
      files.push(full);
    }
  }
  return files;
}

const RESERVED = new Set([
  "README.md", "CLAUDE.md", "MEMORY.md", "Home.md",
  "CHANGELOG.md", "LICENSE",
]);

interface Frontmatter {
  type?: string;
  status?: string;
  [key: string]: unknown;
}

function parseFrontmatter(content: string): Frontmatter | null {
  if (!content.startsWith("---\n") && !content.startsWith("---\r\n")) return null;
  const end = content.indexOf("\n---", 4);
  if (end === -1) return null;
  const yaml = content.substring(4, end);

  // Naive YAML parse — handles `key: value` and `key:\n  - item` lists.
  const result: Frontmatter = {};
  const lines = yaml.split(/\r?\n/);
  let currentKey: string | null = null;
  let currentList: string[] | null = null;
  for (const line of lines) {
    if (line.trim() === "") continue;
    const listMatch = line.match(/^\s*-\s+(.*)$/);
    if (listMatch && currentKey && currentList) {
      currentList.push(listMatch[1].replace(/^["']|["']$/g, ""));
      continue;
    }
    const kvMatch = line.match(/^([A-Za-z_][A-Za-z0-9_-]*)\s*:\s*(.*)$/);
    if (!kvMatch) continue;
    const [, key, val] = kvMatch;
    currentKey = key;
    if (val.trim() === "") {
      currentList = [];
      result[key] = currentList;
    } else {
      currentList = null;
      const v = val.trim();
      result[key] = v.replace(/^["']|["']$/g, "");
    }
  }
  return result;
}

const violations: Violation[] = [];

// Find user content
const allFiles: string[] = [];
walk(ROOT, allFiles);

for (const file of allFiles) {
  const rel = relative(ROOT, file).replace(/\\/g, "/");
  const basename = rel.split("/").pop()!;
  if (RESERVED.has(basename)) continue;

  // Only validate files under user_content_roots (or top-level scaffold)
  const inUserRoot = manifest.user_content_roots.some((root) => {
    const prefix = root.replace(/\/\*\*$/, "").replace(/\/\*$/, "");
    return rel.startsWith(prefix + "/") || rel === prefix;
  });
  const isScaffold = (manifest as { scaffold?: string[] }).scaffold?.includes(rel) ?? false;
  if (!inUserRoot && !isScaffold) continue;

  let content: string;
  try {
    content = readFileSync(file, "utf8");
  } catch {
    continue;
  }

  const fm = parseFrontmatter(content);
  if (!fm) {
    violations.push({ file: rel, kind: "no-frontmatter", message: "missing YAML frontmatter block" });
    continue;
  }

  // Check `type` is in allowed list
  if (fm.type && !manifest.allowed_type_values.includes(fm.type as string)) {
    violations.push({
      file: rel,
      kind: "invalid-type",
      message: `type "${fm.type}" not in allowed_type_values`,
    });
  }

  // Check `status` is in allowed list
  if (fm.status && !manifest.allowed_status_values.includes(fm.status as string)) {
    violations.push({
      file: rel,
      kind: "invalid-status",
      message: `status "${fm.status}" not in allowed_status_values`,
    });
  }

  // Check required fields per type
  const t = fm.type as string;
  const required = manifest.frontmatter_required[t] ?? manifest.frontmatter_required.default;
  for (const field of required) {
    if (!(field in fm) || fm[field] === undefined || fm[field] === null || fm[field] === "") {
      violations.push({
        file: rel,
        kind: "missing-required",
        message: `missing required field "${field}" for type "${t || "default"}"`,
      });
    }
  }

  // Check bilingual aliases for key categories
  const keyCats = manifest.bilingual?.key_categories_require_bilingual_aliases ?? [];
  if (t && keyCats.includes(t)) {
    const aliases = fm.aliases as string[] | undefined;
    if (!Array.isArray(aliases) || aliases.length < 2) {
      violations.push({
        file: rel,
        kind: "bilingual-aliases-missing",
        message: `type "${t}" requires at least 2 aliases (bilingual per DP14)`,
      });
    }
  }
}

// --- Output ---

if (isJson) {
  console.log(JSON.stringify(
    { manifest_valid: true, violations, count: violations.length },
    null, 2
  ));
} else {
  if (violations.length === 0) {
    console.log("✓ vault-manifest.json valid");
    console.log(`✓ ${allFiles.length} notes scanned, no frontmatter violations`);
  } else {
    console.log(`✓ vault-manifest.json valid`);
    console.log(`Found ${violations.length} frontmatter violation(s):\n`);
    const byKind: Record<string, Violation[]> = {};
    for (const v of violations) {
      (byKind[v.kind] ??= []).push(v);
    }
    for (const [kind, items] of Object.entries(byKind).sort()) {
      console.log(`## ${kind} (${items.length})`);
      for (const { file, message } of items.slice(0, 20)) {
        console.log(`  ${file}: ${message}`);
      }
      if (items.length > 20) console.log(`  ... and ${items.length - 20} more`);
      console.log();
    }
  }
}

process.exit(violations.length > 0 ? 1 : 0);
