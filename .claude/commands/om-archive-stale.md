---
description: "Find stale active notes (work/active/ with mtime > 60 days), suggest archive or status update. Per-file confirmation. Mass-op snapshot обязателен."
---

# om-archive-stale

Find work-notes that look stale and suggest archive. Per-file confirmation. Mass-op safety: requires snapshot перед apply.

## Usage

```
/om-archive-stale
/om-archive-stale --threshold 90    # custom days threshold (default 60)
/om-archive-stale --dry-run         # report only, never write
```

## Workflow

### 1. Scan

Glob `work/active/**/*.md`:
- Read frontmatter (`status`, `updated`, `quarter`)
- Read mtime от filesystem
- Compute `stale_days = (now - max(mtime, updated)).days`

Default threshold: 60 days. Configurable via `--threshold N`.

### 2. Categorize candidates

For each stale note:
- **Project (status: active)** — likely needs archive (`/om-project-archive`) or status flip
- **Decision (status: active)** — should be `completed` once implemented
- **Note (no clear status)** — flag for user review

### 3. Read context

For each candidate (top 10 staleness):
- Read first 30 lines (Goals, Status, Open Questions sections)
- qmd-search for recent mentions in `work/1-1/`, `journal/`, или active projects
- If recent mentions found — note may be active despite mtime (e.g. discussed but not edited)

### 4. Show plan

```markdown
## Archive Stale — Candidates (10)

### High confidence — archive
1. **work/active/Old Project A.md**
   - Type: project, status: active
   - Stale: 87 days (last touched 2026-02-02)
   - Status section says "shipped Q1 2026"
   - Recent mentions: 0
   - **Suggest**: `/om-project-archive "Old Project A"` → work/archive/2026/

2. **work/active/Decision X.md**
   - Type: decision, status: active
   - Stale: 73 days
   - Decision says "implemented in PR #234"
   - Recent mentions: 0
   - **Suggest**: status: active → status: completed (no archive — keep in active for now)

### Medium confidence — review
3. **work/active/Project B.md**
   - Type: project, status: active
   - Stale: 65 days
   - Open Questions section non-empty
   - Recent mentions: 1 (1-1 with Alice 2026-04-15 — "still on hold")
   - **Suggest**: keep active, but update status to "on hold" + add ## Pause section

### Low confidence — touch only
4-10. ...
```

### 5. Per-action confirmation

For each "high confidence" candidate ask:
- **Archive** (calls `/om-project-archive`)
- **Status update** (e.g. `active` → `completed`, `on-hold`)
- **Skip** (mark "reviewed", don't bug for 30 days — track в `.claude/logs/`?)

For "medium" — show context, ask user judgment.

For "low" — just list, no per-file ask.

### 6. Snapshot before mass-op

If user confirms ≥ 3 archive operations:
```
bash .claude/scripts/pre-mass-op-snapshot.sh archive-stale
```
**Required.** Per `rules/backup.md`.

### 7. Execute

Apply confirmed actions sequentially:
- For archive — invoke `/om-project-archive <name>` логику.
- For status update — Edit frontmatter only, не trogать body.

### 8. Report

```
✓ Archive operation complete
Snapshot: snapshot/archive-stale-20260430-143022

Actions:
  Archived: 2 (Old Project A → 2026/, Old Project B → 2026/)
  Status updated: 1 (Decision X → completed)
  Skipped: 5 (user kept active)
  Reviewed only: 2

Updated:
  - work/Index.md (Active → Completed sections)
  - <2 affected project subfolders>
```

## Important

- **Per-file confirmation** обязателен for high-confidence archives.
- **Mass-op snapshot** required if ≥ 3 file moves.
- **Read recent qmd-mentions** before suggesting archive — note может быть active discussion-wise even if not edited.
- **Don't archive incidents** if `status: active` (those need explicit postmortem write-up flow или manual review; see `templates/Decision Record.md` adapted for postmortem).
- **Don't change `status: archived` notes** — already archived.
- Default threshold 60 days — configurable, but не делать ниже 30 (создаст false positives).

## When to run

- Monthly (in `/om-monthly`).
- Quarterly cleanup before review prep.
- Before `/om-vault-upgrade` to clean before migration.

## References

- [[.claude/rules/backup|backup rules]] — snapshot policy
- [[.claude/commands/om-project-archive|om-project-archive]] — invoked для archives
- [[.claude/scripts/pre-mass-op-snapshot.sh|pre-mass-op-snapshot.sh]]
- [[.claude/agents/vault-librarian|vault-librarian]] — sibling staleness check (informational)
