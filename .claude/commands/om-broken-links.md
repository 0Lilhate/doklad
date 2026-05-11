---
description: "Narrow check: find broken wikilinks across vault. Suggest fuzzy-match replacements. Reports only."
---

# om-broken-links

Узкая проверка `[[wikilinks]]` ссылок без полного `vault-audit`. Дешевле по токенам.

## Usage

```
/om-broken-links
```

## Workflow

1. **Glob** all `.md` files (exclude `_archive`, `.claude`, `.obsidian`).
2. **Parse** wikilinks: `[[Note]]`, `[[Note|alias]]`, `[[Note#Heading]]`, `[[Note#^block-id]]`.
3. **Build target index**: filenames + aliases from frontmatter (per `rules/language.md` aliases система).
4. **For each wikilink** — check if target exists (filename match OR alias match).
5. **For broken wikilinks** — fuzzy-match suggestions (Levenshtein на filenames + aliases).
6. **Report** в `thinking/broken-links-YYYY-MM-DD.md`:

   ```
   ## Broken Links Report YYYY-MM-DD

   ### Summary
   - Scanned: 89 notes, 312 wikilinks
   - Broken: 7

   ### Broken with high-confidence suggestion (fuzzy-match > 0.8)
   - work/active/Project A.md → [[Alise Chen]] (broken). Suggest: [[Alice Chen]] (0.92)
   - brain/Concept.md → [[OAuth Refresh]] (broken). Suggest: [[OAuth Token Refresh]] (0.85)

   ### Broken without confident match
   - thinking/draft.md → [[Vague Concept]] (no close match) — likely forward-ref or typo

   ### Forward-refs (intentional, < 2 weeks old)
   - brain/New Idea.md → [[Future Concept]] (note created 2026-04-28) — track, no action
   ```

7. Suggest follow-up:
   - Manual fixes via `om-link` (Block J) или edit ноты.
   - For long-stale forward-refs (> 2 weeks) — promote или удалить.

## Important

- **Reports only.** `om-broken-links` не редактирует ссылки.
- Differs from `vault-librarian` — узкая, быстрая. Используй между большими audit'ами.
- Forward-refs (target создан в течение 2 недель) — не считаются broken.
- Aliases из frontmatter учитываются как валидные target'ы.

## References

- [[.claude/rules/wikilinks|wikilinks rules]]
- [[.claude/agents/cross-linker|cross-linker]] — missing wikilinks (другая задача)
- [[.claude/agents/vault-librarian|vault-librarian]] — comprehensive audit
