---
description: "Create an atomic note from template. Args: <type> <name>. Type ∈ concept|source|project|decision|person|team. Routes to correct folder, applies template, fills frontmatter."
---

# om-new-note

Create an atomic note from the right template, in the right folder, with valid frontmatter.

## Usage

```
/om-new-note <type> "<Title>"
```

**Types**:
- `concept` → `brain/<Title>.md` from `templates/Concept.md`
- `source` → ask which subfolder (Web/Docs/GitHub/Papers/Videos), then `Sources/<sub>/<Title>.md` from `templates/Source.md`
- `project` → use `/om-new-project` instead
- `decision` → use `/om-decision` instead
- `person` → `org/people/<Title>.md` from `templates/Concept.md` adapted for `type: person`
- `team` → `org/teams/<Title>.md` adapted for `type: team`

For meetings/1-1 — use `/om-intake` (after dropping into `work/meetings/`) or manual.

## Workflow

### 1. Validate type

`type` ∈ {`concept`, `source`, `person`, `team`}.

If type is `project` or `decision` — redirect: "Use `/om-new-project` or `/om-decision`".

### 2. Search first

Run qmd before creating:
- `mcp__qmd__query` with hybrid search for `<title>` and aliases (try ru and en variants if title looks english/russian)
- If hit ≥ 0.8 similarity — show user, ask "Add to existing or create new?"
- If no hit — proceed

### 3. Determine target path

Per [[.claude/rules/naming|naming.md]]:
- `concept` → `brain/<Title>.md` (TitleCase preserved)
- `source` → ask for subfolder, then `Sources/<sub>/<Title>.md`
- `person` → `org/people/<Title>.md` (TitleCase, Cyrillic OK)
- `team` → `org/teams/<Title>.md`

### 4. Apply template

Read template from `templates/`:
- `concept` → `templates/Concept.md`
- `source` → `templates/Source.md`
- `person` → `templates/Concept.md` (adapt: `type: person`, add `## Relationship`, `## Interaction Log`)
- `team` → `templates/Concept.md` (adapt: `type: team`, add `## Members`, `## Charter`)

Replace placeholders:
- `{{title}}` → user's title
- `{{date:YYYY-MM-DD}}` → today
- `{{url}}` → ask user (for source)
- `{{person}}` → if 1-1 etc.

### 5. Fill bilingual aliases (DP14)

For `concept`, `person`, `team` (key categories per `rules/language.md`):
- Detect title language (Latin or Cyrillic)
- Suggest the other-language alias to user; ask to confirm/edit
- Example: title `Auth Refactor` → suggest alias `Рефакторинг авторизации`

For `source` — aliases optional, skip if user doesn't want.

### 6. Write file

Write to target path with completed frontmatter and template body.

If file exists — abort, suggest `/om-link` for linking-only or open existing.

### 7. Post-create

- Run `cross-linker` agent on the new note (find missing wikilinks).
- Suggest 1-3 backlinks to add (other notes that should reference this one).
- For `concept` — invoke `knowledge-architect` (when available, Patch 7) to suggest which MOC should reference it.

### 8. Report

```
Created: <path>
Template: <template>
Aliases: <list>
Suggested backlinks: <list>
qmd-related: <top 3 similar notes>

Next: review the note, fill in body, add tags.
```

## Examples

```
/om-new-note concept "OAuth Token Refresh"
→ brain/OAuth Token Refresh.md
   aliases: [OAuth Token Refresh, Обновление OAuth-токена]
   tags: [concept]
   related: [] (added after qmd suggestion)

/om-new-note person "Alice Chen"
→ org/people/Alice Chen.md
   aliases: [Alice Chen, Алиса Чен, Alice]
   tags: [person]

/om-new-note source "OAuth Spec RFC 6749"
→ ask: Web/Docs/GitHub/Papers/Videos? → Docs
→ Sources/Docs/OAuth Spec RFC 6749.md
```

## Important

- **Always search qmd first** — don't create duplicates.
- **Preserve TitleCase** in filename.
- **Ask for bilingual alias** for key types — don't auto-translate without confirmation.
- If user types in different language, accept it and add alias on the other.
- If qmd or template missing → fail loudly with explanation, don't fall back to ad-hoc.

## References

- [[.claude/skills/obsidian-atomic|obsidian-atomic skill]]
- [[.claude/skills/obsidian-frontmatter|obsidian-frontmatter skill]]
- [[.claude/rules/frontmatter|frontmatter rules]]
- [[.claude/rules/naming|naming rules]]
- [[.claude/rules/language|language rules]]
