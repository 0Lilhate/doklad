---
description: "Create a Decision Record (ADR) in work/active/ with type:decision. Replaces deprecated obsidian-adr. Uses templates/Decision Record.md, registers in work/Index.md Decisions Log."
---

# om-decision

Create a Decision Record. Per DP5: lives in `work/active/` with `type: decision`, archives with project to `work/archive/YYYY/`.

## Usage

```
/om-decision "<Decision Title>"
```

Examples:
```
/om-decision "OAuth migration: provider choice"
/om-decision "Bilingual aliases discipline"
/om-decision "Caching layer: Redis vs in-memory"
```

## Workflow

### 1. Validate

- Title — TitleCase, descriptive (not generic like "Auth").
- qmd-check: похожее decision уже есть?
- If yes — show user, ask "Update existing or create new"?

### 2. Determine context

Ask user (or infer from recent conversation):
- **Project**: к какому проекту относится решение? Wikilink to `work/active/<Project>.md` if exists.
- **Stakeholders**: who needs to be aware?
- **Decision type**: technical / process / strategic / personnel?

### 3. Apply template

Path: `work/active/<Decision Title>.md`

Use `templates/Decision Record.md`. If absent — fall back to `templates/Work Note.md` adapted.

Frontmatter:
```yaml
---
type: decision
status: active
created: <today>
quarter: <derived>
tags:
  - decision
  - work-note
related:
  - "[[<related project>]]"
  - "[[<related person/team>]]"
---
```

Body sections (per ADR convention):
- **Decision** — one-line summary
- **Context** — what triggered this
- **Options Considered** — 2-3 alternatives with pros/cons
- **Rationale** — why this choice
- **Consequences** — what changes
- **Related** — links

### 4. Register in `work/Index.md`

Edit `work/Index.md` → `## Decisions Log` section:
- Add: `- [[<Decision Title>]] — <one-line context>`
- Update `updated:` frontmatter to today

### 5. Cross-link to project

If decision is project-specific:
- Edit `work/active/<Project>.md` → `## Decisions` section
- Add: `- [[<Decision Title>]] — <one-line>`

If multi-project / vault-wide:
- Add to `brain/Key Decisions.md` instead.

### 6. Bilingual aliases (DP14)

If decision has stable name across languages — add aliases:
```yaml
aliases:
  - OAuth Provider Choice
  - Выбор OAuth-провайдера
```

For ad-hoc tactical decisions — single language fine.

### 7. Report

```
Created: work/active/<Decision Title>.md
Registered:
  - work/Index.md → Decisions Log
  - <project>.md → Decisions section (if applicable)
Aliases: <list>

Next:
- Fill Options/Rationale/Consequences
- Run cross-linker для backlinks
- When implemented — update status to "completed", optionally archive with project
```

## Archive

When project archived via `/om-project-archive`:
- Decision moves to `work/archive/YYYY/<Decision Title>.md` если scope-specific
- Vault-wide decisions остаются в `work/active/` или в `brain/Key Decisions.md`

## Important

- **Not for tactical day-to-day decisions** — те остаются inline в meeting notes / project notes.
- **Yes for**: structural choices, architectural trade-offs, irreversible commitments, lessons-учебные decisions.
- ADR — в `work/active/`, **не** в отдельной `decisions/` папке (DP5).
- При scope-conflict ("это проект-специфично или vault-wide?") — спрашивать user.

## References

- Replaces deprecated `commands/obsidian-adr.md` (now in `_archive/`)
- [[templates/Decision Record|Decision Record.md]]
- [[work/Index|work/Index.md]] → Decisions Log
- [[brain/Key Decisions|brain/Key Decisions.md]] для vault-level
- [[.claude/rules/frontmatter|frontmatter rules]]
