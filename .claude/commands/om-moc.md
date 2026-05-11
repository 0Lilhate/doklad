---
description: "Create a thematic Map of Content. Args: <topic>. Searches vault for related notes via qmd, drafts curated MOC, user reviews/edits."
---

# om-moc

Bootstrap a thematic MOC. Curated, not auto-generated.

## Usage

```
/om-moc "<Topic>"
```

## Workflow

1. **qmd-search** для `<topic>` and aliases (lex + vec, top 30 results).
2. **Filter** results: `type` ∈ {concept, project, decision, source, person} only. Drop `journal`, `1-1`, raw `inbox`.
3. **Group** найденные notes по type:
   - Core concepts (`type: concept, status: evergreen`)
   - Active projects (`type: project, status: active`)
   - Decisions (`type: decision`)
   - Sources / further reading (`type: source, status: processed`)
   - People involved (`type: person`)
4. **Draft MOC** at `<Topic> MOC.md` (vault root by default; ask if user wants `brain/<Topic> MOC.md` or other location).
5. Apply `obsidian-moc` skill structure:
   - Frontmatter: `type: index, status: evergreen, tags: [moc, <topic-tag>]`, bilingual aliases
   - Sections: Why this exists / Core concepts / Active projects / Decisions / Sources / Related MOCs
   - Each wikilink with **one-line description**
6. **Show draft to user** — request edits before save.
7. Write final MOC.
8. Run `cross-linker` — suggest backlinks from notes referenced in MOC.

## Output

```
Drafted MOC: <Topic> MOC.md (15 wikilinks across 5 sections)

Wikilinks:
  Core concepts (3):
    - [[OAuth Token Refresh]] — token rotation race condition
    - ...
  Active projects (2):
    - [[Auth Refactor]] — current focus
    ...

Suggested location: vault root.
Suggested aliases: [Authentication MOC, Карта аутентификации]

Review and edit before saving. Approve to write?
```

## Important

- **MOC ≠ list dump.** Каждый wikilink с описанием. Голый список — `.base`, не MOC.
- **Minimum 5 linked notes.** Меньше — нет смысла в MOC, оставь inline.
- **Curated порядок.** Не алфавит, не auto-grouping. Думай: что читать сначала?
- **Bilingual aliases** обязательны (DP14): MOC на тему — это `concept`-like.
- Skip if MOC уже существует — предложи `/om-moc-update` вместо.

## References

- [[.claude/skills/obsidian-moc|obsidian-moc skill]] — patterns, anti-patterns
- `bases/*.base` — для live-фильтров вместо MOC
- [[.claude/commands/om-moc-update|om-moc-update]] — обновление существующего
