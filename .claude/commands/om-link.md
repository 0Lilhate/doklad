---
description: "Insert or repair wikilinks in a note. Args: <note path> [target]. Suggests bidirectional connections, fixes broken refs, adds missing aliases."
---

# om-link

Targeted wikilink editing in one note. Complement to:
- `cross-linker` agent (vault-wide missing-wikilinks audit)
- `/om-broken-links` (vault-wide broken-link report)

`/om-link` works on **one note** with **explicit user control** — read first, suggest, apply only after approval.

## Usage

```
/om-link <note path>                         # interactive: scan note, suggest links
/om-link <note path> <target>                # add specific [[target]] in best matching place
/om-link <note path> --fix-broken            # only repair broken wikilinks via fuzzy match
/om-link <note path> --add-related <other>   # add to ## Related section
```

## Workflow (interactive mode)

### 1. Read note

Parse frontmatter + body. Extract:
- All existing wikilinks `[[...]]`
- All bare-name mentions of likely-target entities (people, projects, concepts) — без brackets
- `## Related` section content

### 2. qmd context

- `qmd query` for note's title + first 100 words → top 10 related notes
- Filter: drop deprecated, drop already-linked

### 3. Suggest

Group suggestions:

#### A. Bare mentions worth linking
Note text says "Alice helped with the auth refactor" — Alice не в `[[]]` brackets. If `org/people/Alice Chen.md` exists и alias matches — suggest:
- "Wrap 'Alice' as `[[Alice Chen|Alice]]`?"

#### B. Missing in `## Related`
qmd top-10 minus already-linked. Each с similarity score.
- "Add to ## Related: [[OAuth Token Refresh]] (0.84)?"

#### C. Broken wikilinks
For each `[[X]]` где target не существует:
- Fuzzy match top-3 candidates from existing notes
- "Replace `[[Aliсe Chen]]` with `[[Alice Chen]]` (0.92 similarity)? Or create stub?"

#### D. Forward-refs (intentional)
Wikilink to non-existent note < 2 weeks old — keep, не flag.

### 4. Apply

Per-suggestion confirmation. User says yes/no/skip.

After approvals — Edit note:
- Wrap bare mentions: text "Alice helped" → "[[Alice Chen|Alice]] helped"
- Append to `## Related` section (if missing — create after main body)
- Replace broken `[[X]]` с corrected target

Update `updated:` frontmatter to today.

### 5. Bidirectional check

For each new outbound link added — check if target note has backlink to source:
- If нет — suggest "Also add `[[<source>]]` to `<target>.md` Related section?"
- Per-target confirmation.

### 6. Report

```
## /om-link applied to <note path>

### Wrapped bare mentions (3)
- 'Alice' → [[Alice Chen|Alice]] (line 12)
- 'OAuth' → [[OAuth Token Refresh|OAuth]] (line 24)
- 'Q2 review' → [[Q2 2026 Review]] (line 31)

### Added to Related (2)
- [[OAuth Token Refresh]]
- [[Auth Refactor]]

### Fixed broken wikilinks (1)
- [[Aliсe Chen]] → [[Alice Chen]]

### Suggested bidirectional updates (need approval)
- [[Alice Chen]] should backlink to <source> — apply?
- [[Auth Refactor]] should backlink to <source> — apply?

### Skipped
- 'team' (too generic, no specific target match)
- [[Future Concept]] (forward-ref, < 2 weeks old)
```

## Single-target mode (`<target>` argument)

```
/om-link work/active/Auth Refactor.md "OAuth Token Refresh"
```

- Find best place в body для `[[OAuth Token Refresh]]` reference (paragraph mentioning the topic).
- If no organic place — append to `## Related`.
- If target не существует — confirm forward-ref before write.

## `--fix-broken` mode

Only repair broken wikilinks. Skip suggestion / bare-mention / bidirectional steps.

Useful after rename, alias change, or migration when `/om-broken-links` showed issues in this note.

## `--add-related <target>` mode

Append `[[<target>]]` to `## Related` section. Создаёт section if missing.

Best для quick "link this note to that one" без полного scan.

## Important

- **Always read first**, never edit blind.
- **Per-link confirmation** — user controls each insertion.
- **Bidirectional updates require separate approval** for each target — мы не auto-edit other notes silently.
- **Preserve existing links** — никаких removals без явного `--fix-broken`.
- **Don't suggest generic terms** ("team", "system", "data") — only specific entities (people, projects, concepts, decisions).
- **Forward-refs OK** — note may reference future content. < 2 weeks rule.
- **Bilingual aliases** учитываются при matching: `[[Алиса Чен]]` resolves к `Alice Chen.md` если alias есть.

## Use cases

- After `note-normalizer` создал нот — fill in Related section.
- After rename — fix broken refs to old name.
- After 1-1/meeting — wrap mentions of people/projects.
- During session-end review (часть `/om-wrap-up` мог invoke).

## References

- [[.claude/agents/cross-linker|cross-linker]] — vault-wide audit (different scope)
- [[.claude/commands/om-broken-links|om-broken-links]] — vault-wide broken report
- [[.claude/rules/wikilinks|wikilinks rules]] — when to link vs tag vs nothing
- [[.claude/skills/obsidian-bilingual|obsidian-bilingual]] — alias-aware matching
