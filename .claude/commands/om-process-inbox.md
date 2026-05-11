---
description: "Walk Inbox/, normalize each note via note-normalizer agent, route to correct location. Confirm per file before action."
---

# om-process-inbox

Process raw items in `Inbox/` — classify, normalize, route. One-by-one with user approval.

## Usage

```
/om-process-inbox
```

## Workflow

1. **List `Inbox/`** — all `.md` files except `.gitkeep`. If empty — report "Inbox empty" and stop.

2. For each file, sequentially:

   ### Per-file flow
   a. Read content + frontmatter.
   b. qmd-search for similar notes (lex + vec, top 5).
   c. Classify (note-normalizer logic):
      - Source clip? → `Sources/<sub>/`, `type: source, status: processed`
      - Concept candidate? → `brain/<Title>.md`, `type: concept, status: processed` (not evergreen yet)
      - Project-related? → append to existing `work/active/<Project>.md` or create new
      - Person-related? → `org/people/<Person>.md`
      - Decision? → `om-decision` flow
      - Meeting note? → `work/meetings/`
      - 1-1? → `work/1-1/`
      - Unclear? → flag, ask user
   d. Show classification + target path + planned changes (frontmatter additions, body cleanup).
   e. **Ask user** per file: "Apply / Skip / Defer / Delete?"
      - **Apply**: write target file, delete from Inbox.
      - **Skip**: leave in Inbox, move to next.
      - **Defer**: leave with timestamp, ask again next run.
      - **Delete**: confirm, then `rm Inbox/<file>`.
   f. If apply — invoke `note-normalizer` agent for body cleanup, frontmatter normalization, wikilink suggestions.

3. **After all files**:
   - Run `cross-linker` on newly-created notes.
   - Update `Inbox/.gitkeep` (recreate if Inbox is empty).
   - Report summary.

## Report

```
## Inbox Processing 2026-04-30

Processed: <N> files

### Routed
- inbox-thought-1.md → brain/OAuth Token Refresh.md (concept, processed)
- inbox-clip-2.md → Sources/Web/<Title>.md (source)

### Deferred
- inbox-fragment-3.md (unclear, kept for next run)

### Deleted
- inbox-noise.md

### Suggested follow-up
- New concept brain/OAuth Token Refresh.md needs related-links — run /om-link
- 2 notes touched [[Auth Refactor]] — verify project status
```

## Important

- **Never auto-delete** without user confirmation, even if note looks empty.
- **Never auto-promote `status: raw` → `evergreen`**. Evergreen requires explicit user approval (concept must stabilize first).
- If Inbox > 20 файлов — предложить batch-mode по category вместо one-by-one.
- If qmd выдаёт high-similarity hit — суживаем выбор: append/extend existing, не создавать дубль.
- Запуск pre-mass-op-snapshot если ожидается > 10 file operations.

## References

- [[.claude/agents/note-normalizer|note-normalizer agent]]
- [[.claude/agents/cross-linker|cross-linker agent]]
- [[.claude/skills/obsidian-atomic|obsidian-atomic — lifecycle]]
- [[.claude/rules/safety|safety rules]]
