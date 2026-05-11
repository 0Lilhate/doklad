# Folder Structure Standard

> **DEPRECATED for this vault (2026-04-30).** This vault uses obsidian-mind
> structure per DP1, NOT PARA. The original PARA-style baseline below is
> retained for reference but does NOT apply.

## Active structure (this vault)

Per DP1 / `vault-manifest.json:user_content_roots`:

- `work/active/` — active projects
- `work/archive/YYYY/` — completed work by year
- `work/incidents/` — incident notes
- `work/1-1/` — one-on-one meeting notes
- `work/meetings/` — other meeting notes
- `work/Index.md` — work MOC
- `org/people/` — person notes
- `org/teams/` — team notes
- `org/People & Context.md` — people MOC
- `perf/competencies/` — competency map
- `perf/evidence/` — review evidence
- `perf/Brag Doc.md` — wins log
- `brain/` — durable knowledge: Memories, North Star, Patterns, Gotchas, Key Decisions, Skills + atomic concept notes
- `Sources/{Web,Docs,GitHub,Papers,Videos}/` — external source captures
- `Inbox/` — quick capture
- `journal/` — daily notes (`YYYY-MM-DD.md`)
- `thinking/` — drafts, scratchpads (kebab-case allowed)
- `reference/` — long-term reference material
- `bases/` — `.base` dashboards
- `templates/` — note templates
- `_archive/` — archived skills/agents/commands (no auto-load)

Top-level каталоги intentionally exceed 10 — это намеренное разделение
work/org/perf/brain доменов per obsidian-mind methodology.

See: `vault-manifest.json:user_content_roots`, [[../../../rules/naming|.claude/rules/naming.md]]

---

## Original PARA baseline (NOT APPLIED here)

Original obsidian-organizer skill prescribed PARA. Kept below for reference;
do not apply in this vault.

### PARA (deprecated for this vault)

- ~~`inbox/`~~ → use `Inbox/` (TitleCase)
- ~~`projects/`~~ → use `work/active/`
- ~~`areas/`~~ → не применяется; ongoing responsibilities в `org/teams/` или `brain/`
- ~~`resources/`~~ → use `reference/` или `Sources/`
- ~~`archive/`~~ → use `work/archive/YYYY/` + `_archive/`
- ~~`journal/`~~ → kept as-is

Original rules (deprecated):
1. ~~Keep top-level folders under 10~~ → этот vault имеет ~12 (work, org, perf, brain, Sources, Inbox, journal, thinking, reference, bases, templates, _archive) per obsidian-mind методологии
2. Store each note in exactly one primary location ✅ применимо
3. ~~Move stale project notes to `archive/` monthly~~ → use `/om-archive-stale` команда
4. Do not nest deeper than 3 levels unless required ✅ применимо
