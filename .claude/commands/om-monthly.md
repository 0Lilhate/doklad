---
description: "Monthly review — cross-week synthesis, North Star alignment, archived candidates, brag aggregation. Sits between weekly and quarterly."
---

# om-monthly

Cross-week monthly synthesis. Bigger lens than `/om-weekly`, smaller than quarterly.

## Usage

```
/om-monthly
/om-monthly 2026-04   # specific month
```

## Workflow

### 1. Gather month's activity

- `git log --since="<month-start>" --until="<month-end>" --oneline --no-merges`
- All notes modified in `work/active/`, `work/incidents/`, `work/1-1/`, `journal/`, `brain/`, `Sources/` within month.
- Read `journal/YYYY-MM-*.md` files (if any from `/om-today`).
- Read all `thinking/weekly-YYYY-MM-*.md` if user saved weekly synthesis.

### 2. North Star alignment (per-month)

Read `brain/North Star.md`. Compare actual activity vs stated focus areas:

- **Aligned**: which focus areas got attention?
- **Drift**: work outside stated focus (flag, don't judge — может быть emerging).
- **Silent**: focus items zero attention this month.
- **Emerging**: pattern of work suggesting focus shift.

### 3. Cross-week patterns

Look across the month для:
- Themes appearing in multiple weeks (signal stronger than weekly view).
- Projects that shifted scope/priority mid-month.
- Recurring blockers (same thing flagged 2+ times — incident-worthy?).
- New people who appeared (added to `org/people/`?).

### 4. Brag aggregation

Run `brag-spotter` agent с `scope: month`. Filter to wins clearly captured this month. Cross-check `perf/Brag Doc.md` для already-captured items.

### 5. Stale active check

Glob `work/active/*.md`. Flag anything mtime > 30 days as candidate for:
- Archive (`/om-project-archive`)
- Status update (still active, just dormant?)
- Decision (drop or recommit)

### 6. Reading list / sources progress

If reading-list используется:
- Books/articles started but not finished this month.
- Sources captured but not normalized (raw status > 14 days).

### 7. Present synthesis

```markdown
## Monthly Synthesis — YYYY-MM

### This month
- 3-5 bullets — what actually happened

### North Star check
- Aligned: [list]
- Drift: [list]
- Silent: [list]

### Cross-week patterns
- Theme A — appeared in weeks 2, 3, 4
- ...

### Wins (from brag-spotter)
- ...

### Stale active candidates
- [[Project A]] — last touched 35 days ago. Archive? Update? Drop?

### Reading list
- Started this month: 2
- Finished: 1
- Dropped: 0
- Raw sources awaiting normalization: 4

### Goals для next month
- Suggested priority shifts based on patterns above
```

### 8. Offer

- "Save synthesis to `thinking/monthly-YYYY-MM.md`?"
- "Update `brain/North Star.md` if focus shifted?"
- "Run `/om-archive-stale` for stale active candidates?"

## Important

- **Transient by default** — не сохраняем file без user request.
- **Read-only for vault content** — synthesis is reporting, не auto-update.
- **Don't double-count wins** уже captured в weekly synth.
- If месяц light (отпуск, etc.) — say honestly. Don't pad.

## When to run

- 1-3 числа следующего месяца (auto-mental trigger).
- Перед quarterly review.

## References

- [[.claude/commands/om-weekly|om-weekly]] — weekly siblings
- [[.claude/commands/om-vault-audit|om-vault-audit]] — quarterly deep
- [[.claude/agents/brag-spotter|brag-spotter]] — wins detection
- [[brain/North Star]] — alignment baseline
