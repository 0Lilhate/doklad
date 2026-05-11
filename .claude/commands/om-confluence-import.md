---
description: "Import a Confluence page → Sources/Docs/<Title>.md via mcp-atlassian. Pull-on-demand per DP12. No auto-sync."
---

# om-confluence-import

Single-page Confluence pull. Per DP12 — pull-on-demand, never auto-sync of spaces.

## Usage

```
/om-confluence-import <page-id>
/om-confluence-import <page-url>
```

Examples:
```
/om-confluence-import 3161116831
/om-confluence-import https://confluence.moscow.alfaintra.net/pages/viewpage.action?pageId=3161116831
```

## Prerequisites

- `CONFLUENCE_PAT` OS env var set (per Patch 1.1).
- `mcp-atlassian` server running (in `.mcp.json`).
- `mcp__mcp-atlassian__confluence_get_page` available in tool menu.

If any missing → `/om-doctor` диагностика.

## Workflow

### 1. Resolve page-id

If user passed URL — extract `pageId` from query string, или `<space>/<title>` slug from path.

### 2. Fetch via MCP

```
mcp__mcp-atlassian__confluence_get_page(page_id="<id>")
```

Capture: title, body (storage format → markdown), version, space, ancestors, lastUpdated, author.

### 3. Clean / convert

Confluence storage format → clean markdown:
- Strip Confluence-specific macros или преобразовать в callouts (info, note, warning).
- Resolve internal links: Confluence-internal page-links → comment with original URL для будущей референции.
- Tables → markdown tables.
- Code blocks preserve language.

If output messy — fall back to `defuddle` on the rendered URL.

### 4. qmd duplicate check

Search `Sources/Docs/` для title. If hit ≥ 0.85 — show user, ask: "Update existing or create new"?

### 5. Determine target

Default: `Sources/Docs/<Title>.md` (TitleCase, sanitize filename).

Если ancestors указывают на тематическую space — может быть `Sources/Docs/<Space>/<Title>.md`. Ask user если ambiguous.

### 6. Apply Source.md template

```yaml
---
type: source
status: raw
source: <full Confluence URL>
author: <Confluence author>
published: <lastUpdated>
created: <today>
captured: <today>
site: confluence.moscow.alfaintra.net
space: <Confluence space key>
confluence_page_id: <id>
confluence_version: <version>
tags:
  - source
  - confluence
  - <space-tag>
related: []
---
```

### 7. Write

Write to target. Show confirmation:

```
Imported: Sources/Docs/<Title>.md
From: confluence.moscow.alfaintra.net page <id>
Version: <N>
Last updated: <date>
Word count: ~<N>
Related (from qmd): [...]
Suggested follow-up:
  - Run /om-process-inbox or note-normalizer to extract concepts
```

## Re-import / sync

If page имеет `confluence_page_id` — `/om-confluence-import <same-id>` shows diff:

```
Page <id> уже imported (Sources/Docs/<Title>.md).
Confluence version: 12 (was 8 when imported)
Difference: +156 lines, -43 lines

Options:
  1. Overwrite Sources/Docs/<Title>.md with new version
  2. Append diff section
  3. Cancel
```

**Never auto-overwrite.** Always ask.

## Important

- **Single-page pull only.** Per DP12 — no auto-sync of spaces. If user wants whole space — sequential single-page imports с явной user approval каждый раз.
- **PAT не выводить** anywhere в logs или output.
- **status: raw** — clipped Confluence ≠ knowledge. Use `note-normalizer` потом.
- **Preserve original URL** в `source:` — Confluence — primary source of truth for team docs.
- If page имеет attachments (images, PDFs) — flag, ask user если нужно download'ить (уйдёт в `attachments/` или `Sources/Papers/`).

## Не делать

- Не делать bulk space import.
- Не повторять auto-sync (cron-style).
- Не комитить PAT никуда.
- Не делать fallback на raw `curl` если MCP недоступен — лучше fail с инструкцией set up MCP.

## When to use

- Reading a Confluence page и хочется durable copy для notes/quotes.
- Confluence page — input для work-note (e.g. spec → project plan).
- Periodic snapshot важной shared doc (manually triggered).

## References

- mcp-atlassian server (`.mcp.json`)
- DP12 — pull-on-demand decision
- `templates/Source.md`
- [[.claude/skills/defuddle|defuddle]] — fallback parser
- [[.claude/commands/om-process-inbox|om-process-inbox]] — после import
