---
description: "Refresh existing MOC: rescan related notes via qmd, suggest additions/removals. User approves before write."
---

# om-moc-update

Pull additions and detect drift in existing MOC. Curation stays human.

## Usage

```
/om-moc-update <moc-file>
```

Examples:
```
/om-moc-update "Authentication MOC.md"
/om-moc-update brain/Memories.md
```

## Workflow

1. **Read existing MOC** — frontmatter (`tags`, `aliases`), all wikilinks, all annotations.
2. **qmd-search** для MOC's title and aliases (lex + vec, top 50).
3. **Diff**:
   - **Already in MOC**: confirmed wikilinks (skip).
   - **New candidates**: qmd-related notes NOT yet in MOC. Rank by similarity + recency.
   - **Possibly stale**: MOC-linked notes with `status: archived` or no qmd hits → flag for removal.
4. **Show diff** to user:

   ```
   MOC: Authentication MOC.md (15 current links, last updated 2026-03-15)

   ### Suggested additions (5):
   - [[OAuth Token Refresh]] (similarity 0.84) — concept, evergreen
   - [[Auth Failure Mode 2026-04]] (0.79) — incident
   - ...

   ### Possibly stale (2):
   - [[Old Auth Provider]] — status: archived 2025-12
   - [[Auth Spec Draft]] — referenced 2024, no longer in qmd top results

   ### Recommended changes:
   - Move "Old Auth Provider" to "Historical" subsection?
   - Drop "Auth Spec Draft"?
   ```
5. **User approves** per item or batch.
6. Write updated MOC; bump `updated:` to today.
7. `cross-linker` — verify backlinks for new entries.

## Important

- **Never auto-write** additions. MOC is curated — user decides ordering and grouping.
- **Never auto-remove** items. Flag, ask.
- Suggested wikilinks must include **one-line description** (extract from target note's first paragraph or summary).
- If MOC absent — redirect to `/om-moc <topic>` instead.

## References

- [[.claude/skills/obsidian-moc|obsidian-moc skill]]
- [[.claude/commands/om-moc|om-moc]]
