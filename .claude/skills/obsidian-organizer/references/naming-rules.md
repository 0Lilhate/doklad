# File Naming Rules

> **DEPRECATED for this vault (2026-04-30).** Authoritative naming policy moved
> to `.claude/rules/naming.md` per DP2. This file kept for skill backwards-
> compatibility but does NOT apply.
>
> Per DP2 — actual policy:
> - **TitleCase** for durable folders (`work/`, `org/`, `perf/`, `brain/`, `templates/`, `bases/`, `Sources/`)
> - **kebab-case allowed** in `_archive/`, `thinking/`, `reference/`
> - **ISO dates** for temporal files (`YYYY-MM-DD.md`, `<Person> YYYY-MM-DD.md`)
>
> See: [[../../../rules/naming|.claude/rules/naming.md]]

---

## Original kebab-case-only rules (NOT APPLIED here)

The original obsidian-organizer skill prescribed kebab-case for everything.
Kept below for reference — but `.claude/scripts/vault-audit.py` (the active
naming auditor in this vault) does NOT enforce kebab-case. Instead it enforces
TitleCase per `.claude/rules/naming.md`.

If you genuinely want the original behaviour for some other vault — copy this
content elsewhere; do not re-enable in this vault without changing DP2.

### Original (deprecated)

Use deterministic naming:

1. ~~Kebab-case only: `lowercase-words.md`~~ → **TitleCase here**
2. ~~No spaces, emojis, or punctuation besides `-`~~ → **spaces allowed**
3. Daily notes: `YYYY-MM-DD.md` ✅ kept
4. Meeting notes: ~~`YYYY-MM-DD-topic.md`~~ → `YYYY-MM-DD <Topic>.md` (TitleCase)
5. Project specs: ~~`project-name-spec.md`~~ → `<Project Name>.md` (TitleCase)
6. Decision logs: ~~`project-name-decision-001.md`~~ → `<Decision Title>.md` with `type: decision`

Avoid (still applies):
- `Final`, `v2`, `new`, `copy` suffixes
- mixed date formats (`2-4-26`, `Feb24`)
- duplicate names in different folders without qualifiers
