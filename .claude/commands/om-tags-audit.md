---
description: "Tag taxonomy audit: duplicates in arrays, orphan tags, stale tags, naming violations. Reports only, no auto-fix."
---

# om-tags-audit

Audit tag usage across vault. Reports findings; does not auto-fix.

## Usage

```
/om-tags-audit
```

## Workflow

1. **Scan** all `.md` notes (excluding `_archive`, `.claude`, `.obsidian`).
2. **Extract** tags from frontmatter.
3. **Compute statistics**:
   - All tags + frequency
   - Tags used only once (orphan candidates)
   - Tags not in `rules/tags.md` taxonomy (orphan tags)
   - Tags violating naming rule (UPPERCASE, snake_case, spaces, emoji)
   - Duplicate entries within same array (`tags: [person, person]`)
4. **Compare** against canonical taxonomy from `rules/tags.md`.
5. **Write report** `thinking/tags-audit-YYYY-MM-DD.md`:

   ```markdown
   ## Tag Audit YYYY-MM-DD

   ### Stats
   - Total unique tags: 47
   - Total notes: 89
   - Avg tags per note: 2.3

   ### In-array duplicates (auto-fixable)
   - work/active/Project A.md: tags: [person, person] → [person]

   ### Naming violations
   - brain/Concept.md: tag "WorkNote" → should be "work-note"

   ### Orphan tags (not in taxonomy, frequency = 1)
   - "experiment-2026" (1 use) — promote to taxonomy or remove?
   - "wip-only" (1 use)

   ### Stale tags (not in any current note but still in taxonomy)
   - "review-2024" (taxonomy, 0 uses) — remove from taxonomy?

   ### Top 10 most-used
   - work-note: 34
   - person: 18
   ```

6. Suggest follow-up:
   - "Run /om-frontmatter-fix to auto-fix in-array duplicates"
   - "Consider promoting `experiment-2026` to taxonomy or merge with existing"

## Important

- **Reports only.** Auto-fix duplicates через `/om-frontmatter-fix`, не здесь.
- **Не удалять** tags из notes — только flagging.
- **Не добавлять** tags в taxonomy без user approval.
- Запуск раз в месяц — нормальная частота.

## References

- [[.claude/rules/tags|tags rules]]
- [[.claude/skills/obsidian-frontmatter|obsidian-frontmatter — auto-fix duplicates]]
- [[.claude/agents/vault-librarian|vault-librarian]] — comprehensive audit
