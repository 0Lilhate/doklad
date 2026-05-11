# `.claude/` Changelog

История изменений среды Claude Code в этом vault'е. Format: версия = `claude_mind_version` из `vault-manifest.json`.

## [3.4.0] — 2026-04-30

Initial bootstrap from audit-driven roadmap. 8 patches applied (Patch 1.1 through Block I).

### Added

#### Patch 1.1 — Security
- `.gitignore` (root) — secrets, IDE, Claude Code logs/tmp.
- `.claude/settings.local.json` — narrowed `permissions.allow`, added `permissions.deny` for cmd.exe / powershell / rm -rf / obsidian eval.

#### Patch 1.2 — Hooks
- `.claude/settings.json` (shared) — wired up bin/*.sh hooks via PreToolUse matcher.

#### Patch 3 — Dangerous skills archived
- `.claude/_archive/skills/brainrepo/` (full content preserved).
- `.claude/_archive/skills/chronicle-session-documenter/` (stub + git-restore note).

#### Patch 4 — Broken command refs archived
- `.claude/_archive/commands/{obsidian-adr,obsidian-challenge}.md.old`.

#### Patch 5 — Manifest, scripts, rules
- `vault-manifest.json` (root).
- `.claude/rules/{frontmatter,naming,tags,language,wikilinks,backup,safety}.md`.
- `.claude/scripts/{charcount.ts,qmd-bootstrap.ts,vault-audit.py,pre-mass-op-snapshot.sh}`.

#### Patch 6 — Vault structure + MVP commands
- Folders: `work/{active,archive/2026,incidents,1-1,meetings}/`, `org/{people,teams}/`, `perf/{competencies,evidence}/`, `brain/`, `Sources/{Web,Docs,GitHub,Papers,Videos}/`, `Inbox/`, `journal/`.
- Index-stubs: `Home.md`, `work/Index.md`, `org/People & Context.md`, `perf/Brag Doc.md`, `brain/{Memories,North Star,Patterns,Gotchas,Key Decisions,Skills}.md`.
- Templates: `Concept.md`, `Source.md`, `Project.md`, `Meeting Note.md`, `1-1 Note.md`.
- Skills: `obsidian-atomic`, `obsidian-moc`, `obsidian-frontmatter`.
- Commands (13): `om-new-note`, `om-new-project`, `om-decision`, `om-source-clip`, `om-process-inbox`, `om-moc`, `om-moc-update`, `om-prep-1on1`, `om-frontmatter-fix`, `om-tags-audit`, `om-broken-links`, `om-challenge`, `om-today`.

#### Patch 7 — MVP agents + lean rewrites
- New agents: `brag-spotter`, `knowledge-architect`, `quality-reviewer`.
- Lean rewrites: `external-researcher` (287→75 lines), `research-synthesizer` (287→110), `session-documenter` (313→110).
- `_archive/agents/{external-researcher,research-synthesizer,session-documenter}.md.old`.

#### Block H — Self-validation
- `.claude/schemas/vault-manifest.schema.json`.
- `.claude/scripts/{validate-schemas.ts,test-broken-refs.sh,test-hooks.sh}`.
- Commands: `om-self-audit`, `om-doctor`.

#### Block I — Observability & Living docs
- `.claude/bin/claude-log-blocked.sh` — JSONL logger for blocked events (DP7).
- `.claude/logs/` — gitignored runtime logs.
- `bases/Vault Health.base` — drill-down by type, status, staleness.
- `Home.md` — `## Vault Health` metrics section.
- `.claude/README.md` — directory navigation map.
- `.claude/docs/{architecture,contributing,troubleshooting}.md`.
- `.claude/CHANGELOG.md` — this file.
- `om-metrics` command (Block I).

### Changed

- `.mcp.json` — `CONFLUENCE_PERSONAL_TOKEN` plaintext → `${CONFLUENCE_PAT}` env reference.
- `agents/note-normalizer.md` — removed `brainrepo` from skills list.
- `agents/cross-linker.md` — removed orphan-detection (moved to vault-librarian).
- `agents/vault-librarian.md` — explicitly owns orphan-detection.
- `commands/{om-weekly,om-wrap-up,om-meeting,om-humanize,om-standup}.md` — restored refs after dependencies created.
- `skills/obsidian-organizer/scripts/obsidian_audit.py` — deprecation banner; `--apply` blocked.
- `skills/{brainrepo,chronicle-session-documenter}/SKILL.md` — replaced with deprecation stubs.
- `commands/{obsidian-adr,obsidian-challenge}.md` — replaced with deprecation stubs.
- `bin/claude-block-sensitive-{bash,files}.sh` — added `log_blocked` calls before `exit 2`.
- `CLAUDE.md` — added "Rules location" section pointing to `.claude/rules/*`.

### Removed

- (Logical removal only — physical deletion deferred to user's terminal)
- `skills/brainrepo/` original content (now stub; archive in `_archive/`).
- `skills/chronicle-session-documenter/` original (now stub).
- Original `obsidian-adr.md`, `obsidian-challenge.md` (now stubs).

### Decisions resolved (2026-04-30)

15 Decision Points resolved — see `vault-manifest.json:decision_points_resolved` and `thinking/2026-04-30-claude-setup-audit-roadmap.md` "Decision Points" section.

### Migration (from blank vault)

For new clones:
1. Set `CONFLUENCE_PAT` OS env var (если используете Confluence integration).
2. `bash .claude/scripts/qmd-bootstrap.ts` (если qmd установлен).
3. `/om-doctor` — verify environment.
4. `/om-self-audit` — verify .claude/ integrity.
5. Open `Home.md` в Obsidian — vault entry-point.

### Stats

- ~120 files created/modified across 9 patch blocks.
- ~22 hours of focused work (estimate).
- Cumulative score progression: 50 → 55 → 70 → 75 → 78 → 83 → 88 → 92 → 94.

---

## Future versions

### [3.5.0] — TBD (Block J)

Planned:
- `/om-monthly` — monthly review.
- `/om-confluence-import <page-id>` (DP12) — pull-on-demand via mcp-atlassian.
- Reading list: `templates/Literature Note.md`, `bases/Reading List.base`.
- (DP13 GitHub integration deferred — not planned.)

### [3.6.0] — TBD (Block K)

Planned:
- `obsidian-bilingual` skill — transliteration utilities.
- Recovery procedures: `scripts/restore-from-snapshot.sh`, `scripts/rebuild-from-zero.sh`.
- Performance: lazy-load skills, qmd hot-cache.
- `claude-pre-write-validate.sh` hook — frontmatter strict mode.

## Versioning

`claude_mind_version` follows semver:
- **Major** (X.0.0): breaking changes to `vault-manifest.json` schema, removed agents, changed allowed type/status enums.
- **Minor** (3.X.0): new commands/agents/skills/rules without breaking existing.
- **Patch** (3.4.X): fixes, doc updates, minor edits.

Migration paths between major versions documented в `.claude/docs/migrations/<from>-to-<to>.md` (added на необходимости).
