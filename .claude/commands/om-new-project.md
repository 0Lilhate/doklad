---
description: "Bootstrap a new project: create work/active/<Project>.md from Project template, optionally subfolder for meetings/decisions, register in work/Index.md."
---

# om-new-project

Develop a new project structure: file in `work/active/`, optional subfolder, registration in `work/Index.md`.

## Usage

```
/om-new-project "<Project Name>"
```

## Workflow

### 1. Validate

- `<Project Name>` — TitleCase, no special chars except `-`/space.
- qmd-check: project уже не существует?
- If exists in `work/active/` — abort, suggest opening existing.
- If exists in `work/archive/` — ask: "Reactivate or create new?"

### 2. Create main file

Path: `work/active/<Project Name>.md`

Apply `templates/Project.md`:
- `{{title}}` → `<Project Name>`
- `{{date:YYYY-MM-DD}}` → today
- `quarter` → derive from today's date (Q1=Jan-Mar, Q2=Apr-Jun, Q3=Jul-Sep, Q4=Oct-Dec) → e.g. `Q2-2026`
- `team` → leave empty, ask user
- `priority` → leave empty
- `aliases` → ask for bilingual (DP14): if `Auth Refactor` — suggest `Рефакторинг авторизации`

### 3. Subfolder decision

Ask user: "Create subfolder `work/active/<Project Name>/` for meetings/decisions/risks?"

If yes — create `work/active/<Project Name>/.gitkeep`. Subfolder used for project-specific meeting notes, decision records, etc.

If no — single file approach. Project notes link to meeting notes in `work/meetings/` etc.

**Rule of thumb**: subfolder если 5+ нот ожидаются. Иначе single file.

### 4. Register in `work/Index.md`

Edit `work/Index.md`:
- Add to `## Active Projects` section: `- [[<Project Name>]] — <one-line description from user>`
- Update `updated:` frontmatter to today

### 5. Cross-link

Run `cross-linker` agent on the new project file:
- Suggest connections to existing `org/people/`, `org/teams/`, related projects
- Suggest connections to active goals in `brain/North Star.md`

### 6. Report

```
Created: work/active/<Project Name>.md
Subfolder: <yes|no>
Aliases: <list>
Registered in: work/Index.md
Suggested connections: <list from cross-linker>

Next:
- Fill in goals, status, risks
- Optionally invoke knowledge-architect to suggest MOC placement
- Schedule first meeting → drop notes into work/meetings/, run /om-intake
```

## Important

- Default subfolder = NO (single-file). Only create subfolder if explicitly needed.
- Always register in `work/Index.md` — orphan project нет.
- Bilingual aliases для `type: project` — обязательны (DP14).
- If user wants quick prototype project — use `/om-new-note concept` instead, promote later.

## References

- [[templates/Project|Project.md]]
- [[work/Index|work/Index.md]]
- [[.claude/skills/obsidian-atomic|obsidian-atomic]]
- [[.claude/rules/frontmatter|frontmatter rules]]
