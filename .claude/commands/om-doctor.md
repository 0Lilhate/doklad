---
description: "Diagnose environment dependencies — bash, jq, node, python, qmd, defuddle, obsidian CLI, mcp servers. Reports what's installed/missing."
---

# om-doctor

Environment health check. Verifies that all dependencies referenced by `.claude/` are actually available.

## Usage

```
/om-doctor
```

## Workflow

Run each check, capture status. Use Bash tool where available; otherwise Glob/Read for file-based checks.

### 1. Core shell

| Tool | Required by | Check |
|---|---|---|
| `bash` 4+ | hooks (`bin/*.sh`), test scripts | `bash --version \| head -1` |
| `jq` | hooks (parse JSON tool input) | `jq --version` |
| `git` | snapshots, history checks | `git --version` |
| `node` 22+ | `.ts` scripts (`charcount`, `qmd-bootstrap`, `validate-schemas`) | `node --version` |
| `python3` 3.10+ | `vault-audit.py` | `python3 --version` |

### 2. Vault tools

| Tool | Required by | Check |
|---|---|---|
| `qmd` | semantic search, `qmd-bootstrap.ts` | `qmd --help` (returns help) |
| `defuddle` | `om-source-clip`, `capture-designer` | `defuddle --version` |
| `obsidian` (CLI) | `om-standup`, `om-intake` (with fallback) | `obsidian --version` (optional — fallback exists) |

### 3. MCP servers

Read `.mcp.json` and check declared servers:

| Server | Used by |
|---|---|
| `memory` | persistent memory |
| `sequential-thinking` | chain-of-thought |
| `context7` | live docs |
| `playwright` | browser automation |
| `mcp-atlassian` | Confluence integration (DP12) |

For each — check command in PATH:
- `npx --version` (for npx-based servers)
- `mcp-atlassian --version` for Atlassian server

### 4. Vault structure

Use Glob:
- `vault-manifest.json` exists
- `Home.md` exists
- All paths in `manifest.scaffold` exist
- All paths in `manifest.user_content_roots` (as folders) exist

### 5. Hooks

- `.claude/settings.json` exists
- Block `hooks.PreToolUse` defined
- Both `bin/*.sh` files exist and have shebang `#!/bin/bash`

### 6. Env vars

| Var | Required by |
|---|---|
| `CONFLUENCE_PAT` | `.mcp.json` mcp-atlassian server |
| `CLAUDE_PROJECT_DIR` | hooks (auto-set by Claude Code) |

For `CONFLUENCE_PAT` — check length only, never print value:
```bash
echo "CONFLUENCE_PAT length: ${#CONFLUENCE_PAT}"
```

If 0 or unset — flag as missing.

### 7. Recovery readiness (Block K)

- Snapshot tags exist: `git tag -l 'snapshot/*' --sort=-creatordate | head -5`
  - If zero — flag warning: "no snapshots yet — first mass-op should create one via pre-mass-op-snapshot.sh"
- Recovery scripts present:
  - `.claude/scripts/restore-from-snapshot.sh`
  - `.claude/scripts/rebuild-from-zero.sh`
  - `.claude/scripts/pre-mass-op-snapshot.sh`
- Recovery doc: `.claude/docs/recovery.md`

### 8. Bilingual readiness (Block K, DP14)

- Skill `.claude/skills/obsidian-bilingual/SKILL.md` present
- Manifest `bilingual.key_categories_require_bilingual_aliases` non-empty
- Sample check: pick 3 random notes with `type ∈ {concept, person, project, team}`, count `aliases:`
  - If < 2 aliases for any — flag warning, suggest `/om-frontmatter-fix`

### 9. Strict-mode hook (Block K, opt-in)

- `.claude/bin/claude-pre-write-validate.sh` exists (script ready)
- Wired up in `.claude/settings.json`? If yes — flag "strict mode active". If no — info "available, opt-in"

## Output

```markdown
## /om-doctor — Environment Diagnostic (2026-04-30)

### Core shell
- ✓ bash 5.2.21 (Git Bash)
- ✓ jq 1.7.1
- ✓ git 2.43.0
- ✓ node 22.5.1
- ✓ python3 3.11.5

### Vault tools
- ✓ qmd 0.x (installed)
- ✓ defuddle 0.x
- ⚠ obsidian CLI not installed — fallback paths used in /om-standup

### MCP servers
- ✓ npx available (memory, sequential-thinking, context7, playwright via npx)
- ✓ mcp-atlassian available
- ⚠ CONFLUENCE_PAT env var: not set — Confluence integration unavailable

### Vault structure
- ✓ vault-manifest.json present
- ✓ Home.md present
- ✓ All scaffold files present (10/10)
- ✓ All user_content_roots exist

### Hooks
- ✓ .claude/settings.json with hooks block
- ✓ bin/claude-block-sensitive-bash.sh
- ✓ bin/claude-block-sensitive-files.sh

### Verdict
**Healthy** with 2 warnings (obsidian CLI optional, CONFLUENCE_PAT optional).

### Recommended actions
- Install obsidian CLI for richer `/om-standup` output (optional)
- Set CONFLUENCE_PAT в OS env if you want `/om-confluence-import` (Block J)
```

## Failure modes

| Symptom | Fix |
|---|---|
| `bash --version` not found | Install Git for Windows (Git Bash) |
| `jq --version` not found | `winget install jqlang.jq` or download jq.exe |
| `node --version` < 22 | Update Node.js |
| `qmd --help` not found | Install qmd CLI |
| `vault-manifest.json` missing | Run Patch 5 (manifest creation) |
| Hook scripts missing shebang | Restore from git or recreate |
| `CONFLUENCE_PAT` length = 0 | Set OS env var (per Patch 1.1 instructions) |

## Important

- **Read-only diagnostic.** No fixes — only reports.
- **Some warnings are OK** — obsidian CLI is optional, MCP servers могут быть disabled.
- **Critical vs warning**:
  - Critical: bash, jq, git, node, vault-manifest.json, hooks
  - Warning: qmd, defuddle, obsidian CLI, MCP servers, env vars

## When to run

- After fresh clone (verify environment).
- After OS update / tool upgrade.
- When `/om-self-audit` shows hook misbehaviour (might be missing dep).
- Before debugging "why isn't X working".

## References

- [[.claude/commands/om-self-audit|om-self-audit]] — schema/refs/hooks (different scope)
- [[.claude/commands/om-vault-audit|om-vault-audit]] — vault content audit
