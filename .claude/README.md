# `.claude/` — Navigation Map

How to orient yourself in this directory. Aimed at: new contributor, future-you 6 months from now, new Claude Code session.

## Quick start

| I want to… | Go to |
|---|---|
| Understand the philosophy | `../CLAUDE.md` (orchestrator) |
| Understand separation of concerns | `docs/architecture.md` |
| Add a new workflow / command / agent | `docs/contributing.md` |
| Diagnose "why doesn't X work" | `docs/troubleshooting.md` |
| See history of changes | `CHANGELOG.md` |
| See decision rationale | `../thinking/2026-04-30-claude-setup-audit-roadmap.md` |

## Directory map

```
.claude/
├── README.md                # this file — navigation map
├── CHANGELOG.md             # version history of .claude/ itself
├── settings.json            # shared config (hooks). DO NOT edit casually.
├── settings.local.json      # per-user permissions. DO NOT commit secrets.
├── memory-template.md       # MEMORY.md template (auto-loaded by Claude Code)
│
├── docs/                    # human-readable docs about this directory
│   ├── architecture.md      # separation of concerns (skills/agents/commands/rules/scripts)
│   ├── contributing.md      # how to add new workflow
│   └── troubleshooting.md   # common issues and fixes
│
├── rules/                   # canonical policies (override CLAUDE.md inline)
│   ├── frontmatter.md
│   ├── naming.md
│   ├── tags.md
│   ├── language.md
│   ├── wikilinks.md
│   ├── backup.md
│   └── safety.md
│
├── schemas/                 # JSON schemas for vault-manifest.json + frontmatter
│   └── vault-manifest.schema.json
│
├── scripts/                 # automation scripts
│   ├── charcount.ts         # per-section char counter (om-humanize)
│   ├── qmd-bootstrap.ts     # initial qmd index
│   ├── validate-schemas.ts  # frontmatter + manifest validation
│   ├── vault-audit.py       # naming/structure audit (replaces obsidian_audit.py)
│   ├── pre-mass-op-snapshot.sh   # git tag snapshot before mass-op
│   ├── test-broken-refs.sh  # find broken cross-refs
│   └── test-hooks.sh        # verify hook behaviour
│
├── bin/                     # PreToolUse hooks (auto-invoked by Claude Code)
│   ├── claude-block-sensitive-bash.sh
│   ├── claude-block-sensitive-files.sh
│   └── claude-log-blocked.sh    # logger called by both hooks
│
├── logs/                    # gitignored. Blocked-events log (.jsonl)
│
├── skills/                  # knowledge units (auto-loaded by Claude on triggers)
│   ├── obsidian-markdown/   # kepano
│   ├── obsidian-bases/      # kepano
│   ├── obsidian-cli/        # kepano
│   ├── json-canvas/         # kepano
│   ├── defuddle/            # kepano
│   ├── obsidian-clipper-template-creator/   # community
│   ├── qmd/                 # custom — semantic search
│   ├── obsidian-organizer/  # community (with deprecated audit script)
│   ├── obsidian-atomic/     # custom — atomic notes lifecycle (Patch 6)
│   ├── obsidian-moc/        # custom — MOC patterns (Patch 6)
│   ├── obsidian-frontmatter/ # custom — YAML normalization (Patch 6)
│   ├── brainrepo/           # DEPRECATED 2026-04-30 (stub)
│   └── chronicle-session-documenter/   # DEPRECATED 2026-04-30 (stub)
│
├── agents/                  # subagent definitions (invoked via Task tool)
│   ├── note-normalizer.md
│   ├── dashboard-builder.md
│   ├── capture-designer.md
│   ├── context-loader.md
│   ├── cross-linker.md
│   ├── vault-librarian.md   # owns orphan-detection (Patch 7)
│   ├── vault-migrator.md
│   ├── brag-spotter.md      # NEW Patch 7
│   ├── knowledge-architect.md  # NEW Patch 7
│   ├── quality-reviewer.md  # NEW Patch 7
│   ├── external-researcher.md  # lean rewrite Patch 7
│   ├── research-synthesizer.md # lean rewrite Patch 7
│   └── session-documenter.md   # lean rewrite Patch 7
│
├── commands/                # slash-commands (/om-*)
│   ├── om-dump.md, om-intake.md, om-process-inbox.md, om-source-clip.md
│   ├── om-new-note.md, om-new-project.md, om-decision.md
│   ├── om-moc.md, om-moc-update.md
│   ├── om-standup.md, om-today.md, om-weekly.md, om-wrap-up.md
│   ├── om-meeting.md, om-prep-1on1.md
│   ├── om-vault-audit.md, om-frontmatter-fix.md, om-tags-audit.md, om-broken-links.md
│   ├── om-self-audit.md, om-doctor.md   # NEW Block H
│   ├── om-metrics.md   # NEW Block I
│   ├── om-humanize.md, om-challenge.md, om-vault-upgrade.md, om-project-archive.md
│   ├── obsidian-adr.md, obsidian-challenge.md   # DEPRECATED stubs
│
└── _archive/                # archived skills/agents/commands (never auto-loaded)
    ├── README.md
    ├── skills/{brainrepo,chronicle-session-documenter}/
    ├── agents/{external-researcher,research-synthesizer,session-documenter}.md.old
    └── commands/{obsidian-adr,obsidian-challenge}.md.old
```

## Authority order (when in conflict)

```
1. .claude/rules/safety.md          (highest — hard prohibitions)
2. .claude/rules/*.md               (canonical policies)
3. CLAUDE.md                        (orchestrator)
4. vault-manifest.json              (structural facts)
5. agents/<name>.md                 (executor specs)
6. commands/om-*.md                 (entry points)
7. skills/*/SKILL.md                (knowledge units)
```

If two files conflict — earlier one wins.

## Conventions

- TitleCase for durable folder content (DP2). Kebab-case OK in `_archive/`, `thinking/`.
- ISO dates `YYYY-MM-DD` for temporal files.
- Bilingual aliases for key types (concept/person/project/team) per DP14.
- Bash scripts: `set -euo pipefail`, shebang `#!/bin/bash`.
- TS scripts: `node --experimental-strip-types <file>` (no compilation step).
- Python scripts: stdlib only (no pip install).

## See also

- [[../CLAUDE]] — orchestrator
- [[../Home]] — vault entry-point
- [[../thinking/2026-04-30-claude-setup-audit-roadmap]] — full roadmap with rationale
