---
description: "Distill a single note into a concise TL;DR. Reports summary inline (read-only by default). Optional --apply writes Summary section back into note."
---

# om-summary

Single-note distillation. Different from `research-synthesizer` agent (multi-source) — focuses on one existing note.

## Usage

```
/om-summary <note path>
/om-summary <note path> --apply         # write Summary back into note
/om-summary <note path> --length short  # short (3 sentences) | medium (default, 1 paragraph) | long (~150 words)
```

## Workflow

### 1. Read note

Parse frontmatter + body. Identify:
- `type` (concept/project/decision/source/etc.)
- Existing `## Summary` section, if any.
- Body length and structural sections.

### 2. Determine summary style by type

| Type | Style |
|---|---|
| `concept` | Definition + why-it-matters in 1 paragraph |
| `project` | Status + goal + next-step in 3 sentences |
| `decision` | Decision + rationale + consequence in 3 sentences |
| `source` | What it covers + key claim + relevance в 1 paragraph |
| `incident` | What happened + root cause + outcome (timeline-aware) |
| `meeting`/`1-1` | Decisions + action items + key takeaway |
| `session` | Goal + accomplished + remaining |
| `index`/MOC | Skip — these are already curated indexes |
| Other | Generic 1-paragraph |

### 3. Compose

Stay close to source. **No new claims** — only distillation of what's already there. Cited content (wikilinks, quoted excerpts) preserved.

If note has multiple sections — synthesize across, не просто copy first paragraph.

If note is < 200 words — say "note already concise; no summary needed" and exit.

### 4. Length control

- `short` — 2-3 sentences, ~30 words
- `medium` (default) — 1 paragraph, ~80 words
- `long` — ~150 words с key sections

### 5. Output

#### Read-only (default)

Show summary inline. User decides where to use it.

```
## Summary of: <note path>

<summary text>

---
Stats: original ~<N> words, summary ~<M> words (<%> compression).
Skipped citations: <N> wikilinks preserved verbatim.
```

#### `--apply` mode

1. Show summary first.
2. Ask: "Insert as `## Summary` section after frontmatter? [y/n]"
3. If yes: read note, find frontmatter end, insert summary section before existing body (or replace existing `## Summary` if present).
4. Update `updated:` field в frontmatter.
5. Confirm: "Updated <path>".

## Important

- **No external info added** — only what's in the note. If source notes are referenced — read them is OK to ground summary, но не выдумывать новое.
- **Preserve wikilinks** в summary — `[[X]]` остаётся клик-выживым.
- **Bilingual** — если note содержит RU+EN content, summary в основном языке тела.
- **Skip MOCs and indexes** — они уже distillation by design.
- For `--apply` — short (3 sentences) пишется как стандарт; полный paragraph только if user explicitly asked `--length medium/long`.
- Если note ALREADY имеет `## Summary` — `--apply` без подтверждения **не overwrite**, спросить.

## When to use

- After `note-normalizer` обработал big source — нужен compact view.
- Before sharing note или referencing в другой ноте.
- Quality check: если can't summarize → нота, возможно, не атомарна (split candidate).
- For active project notes weekly — summary в `## Status` секции.

## Не использовать для

- Generating new content from scratch — это `research-synthesizer`.
- Summarizing multiple notes — то же.
- Daily journal entries — they ARE the summary already.
- Templates — нет body to summarize.

## References

- [[.claude/agents/research-synthesizer|research-synthesizer]] — multi-source distillation
- [[.claude/skills/obsidian-atomic|obsidian-atomic]] — quality bar (атомарность = легко summarize)
- `obsidian-frontmatter` skill — для `updated:` field handling
