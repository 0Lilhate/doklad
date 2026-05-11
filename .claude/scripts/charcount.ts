#!/usr/bin/env node
// charcount.ts — count characters per Markdown section.
// Used by: commands/om-humanize.md
// Usage: node --experimental-strip-types .claude/scripts/charcount.ts <file.md>

import { readFileSync } from "node:fs";
import { resolve } from "node:path";

const file = process.argv[2];
if (!file) {
  console.error("Usage: charcount.ts <file.md>");
  process.exit(1);
}

let content: string;
try {
  content = readFileSync(resolve(file), "utf8");
} catch (e) {
  console.error(`Cannot read ${file}: ${(e as Error).message}`);
  process.exit(1);
}

interface Section {
  name: string;
  level: number;
  chars: number;
  lines: number;
}

const lines = content.split("\n");
const sections: Section[] = [];
let current: Section = { name: "(preamble)", level: 0, chars: 0, lines: 0 };

let inFrontmatter = false;
let frontmatterClosed = false;

for (const line of lines) {
  if (line === "---") {
    if (!inFrontmatter && !frontmatterClosed) {
      inFrontmatter = true;
      continue;
    }
    if (inFrontmatter) {
      inFrontmatter = false;
      frontmatterClosed = true;
      continue;
    }
  }
  if (inFrontmatter) continue;

  const heading = line.match(/^(#{1,6}) (.+)$/);
  if (heading) {
    sections.push(current);
    current = {
      name: heading[2],
      level: heading[1].length,
      chars: 0,
      lines: 0,
    };
    continue;
  }

  current.chars += line.length + 1;
  current.lines += 1;
}
sections.push(current);

const total = sections.reduce((s, x) => s + x.chars, 0);
const totalLines = sections.reduce((s, x) => s + x.lines, 0);

const output = {
  file: resolve(file),
  total_chars: total,
  total_lines: totalLines,
  sections: sections
    .filter((s) => s.chars > 0 || s.name !== "(preamble)")
    .map((s) => ({
      heading: "#".repeat(s.level || 1) + " " + s.name,
      chars: s.chars,
      lines: s.lines,
    })),
};

console.log(JSON.stringify(output, null, 2));
