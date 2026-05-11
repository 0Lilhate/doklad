---
description: "Update Home.md Vault Health metrics. Counts notes/orphans/broken-links/stale-active/violations. Per DP8: lives in Home.md + bases/Vault Health.base."
---

# om-metrics

Compute vault health numbers and write into `Home.md`'s `## Vault Health` section between `<!-- om-metrics-start -->` and `<!-- om-metrics-end -->` markers.

## Usage

```
/om-metrics
```

## Workflow

### 1. Compute metrics

Run in parallel where possible:

| Metric | How |
|---|---|
| **Total notes** | `Glob "**/*.md"` — exclude `.claude/`, `.obsidian/`, `_archive/`, `node_modules/`. Count. |
| **Frontmatter violations** | Run `node --experimental-strip-types .claude/scripts/validate-schemas.ts --json`. Parse `count`. |
| **Orphans** | Glob `user_content_roots`. For each, Grep entire vault for `[[<filename>]]` или alias. Notes with 0 incoming = orphans. |
| **Broken wikilinks** | Grep all `[[Note]]` patterns; check target exists (filename or alias). Report count. |
| **Stale active** (>60d) | Glob `work/active/**/*.md`. For each, check mtime. Count those > 60 days old. |
| **Bilingual aliases missing** | Walk `concept`, `person`, `project`, `team` notes. Count those with `len(aliases) < 2`. |

### 2. Read last-audit dates

- Last `/om-vault-audit` — search for `thinking/vault-audit-*.md`, take latest by date suffix.
- Last `/om-self-audit` — same for `thinking/self-audit-*.md` (if reports written; otherwise "—").

### 3. Update `Home.md`

Read `Home.md`. Find markers `<!-- om-metrics-start -->` and `<!-- om-metrics-end -->`.

Replace content between them с обновлённой таблицей:

```markdown
<!-- om-metrics-start -->
| Metric | Value | Last update |
|---|---|---|
| Total notes | <N> | 2026-04-30 |
| Orphans | <N> | 2026-04-30 |
| Broken wikilinks | <N> | 2026-04-30 |
| Frontmatter violations | <N> | 2026-04-30 |
| Stale active (> 60d) | <N> | 2026-04-30 |
| Bilingual aliases missing | <N> | 2026-04-30 |
| Last `/om-vault-audit` | YYYY-MM-DD | — |
| Last `/om-self-audit` | YYYY-MM-DD | — |
<!-- om-metrics-end -->
```

Update `Home.md` `updated:` frontmatter to today.

### 4. Diff

If previous metrics exist (read from `Home.md` before update), show delta:

```
## Vault Health Update 2026-04-30

| Metric | Was | Now | Δ |
|---|---|---|---|
| Total notes | 87 | 92 | +5 |
| Orphans | 3 | 2 | -1 |
| Broken wikilinks | 0 | 0 | — |
| ...
```

If first run — show "(baseline established)".

## Output

```
✓ Metrics updated in Home.md

Current state:
  Total notes: 92
  Orphans: 2 (-1 vs last)
  Broken wikilinks: 0
  Frontmatter violations: 1 (new)
  Stale active: 3 (+1)
  Bilingual aliases missing: 5 (-2)

Next:
  - 1 new frontmatter violation: run /om-frontmatter-fix
  - 1 new stale active note: review work/active/
```

## Important

- **Read-only on vault content** — only writes to `Home.md` `## Vault Health` section between markers.
- **Markers obligatory.** If markers missing — abort with instruction "add `<!-- om-metrics-start -->` / `<!-- om-metrics-end -->` to Home.md".
- **No external deps** beyond what `validate-schemas.ts` provides.
- **Frequency**: weekly (in `/om-weekly`) or on-demand. Not auto.
- **If no Bash available** in session: do counts via Glob+Grep+Read. Slower but works.

## When to run

- Weekly during `/om-weekly`.
- After `/om-vault-audit` (post-audit refresh).
- After major content changes (e.g. import, mass cleanup).
- Before sharing vault status with stakeholder.

## References

- `Home.md` `## Vault Health` section (target)
- `bases/Vault Health.base` (drill-down view)
- [[.claude/scripts/validate-schemas.ts|validate-schemas.ts]]
- [[.claude/commands/om-self-audit|om-self-audit]] — separate scope
- DP8 (Home.md + Vault Health.base)
