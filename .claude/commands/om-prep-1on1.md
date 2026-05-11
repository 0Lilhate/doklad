---
description: "Prep brief for a 1:1 with a person. Loads org/people/<Person>.md, recent work/1-1/, open action items, recent brag-doc context."
---

# om-prep-1on1

Person-focused meeting prep. Differs from `/om-meeting` (subject-focused).

## Usage

```
/om-prep-1on1 "<Person Name>"
```

## Workflow

1. **Find person note**: `org/people/<Person>.md`. If absent → suggest `/om-new-note person "<Name>"` first.
2. **Load context** (read in parallel):
   - `org/people/<Person>.md` — relationship, role, ongoing topics
   - 3 most recent `work/1-1/<Person>*.md` (sorted by filename date desc) — what's been discussed
   - `perf/Brag Doc.md` — recent wins involving the person
   - qmd query for `<Person>` lex+vec — surface mentions in projects, decisions, incidents
3. **Extract**:
   - Open action items (unchecked `- [ ]` from past 3 1-1s) attributable to either side
   - Last topics raised + status now
   - Wins to share / praise to give (from brag-doc)
   - Recurring themes
4. **Brainstorm prep questions**:
   - Career / growth — based on `## Goals` или `## What to Watch` in person note
   - Project alignment — what active projects involve them?
   - Feedback loops — anything raised in last 1-1 worth following up?
5. **Present brief**:

   ```
   ## 1:1 Prep — <Person> (next: today)

   ### Context
   - Role: <from person note>
   - Last 1-1: 2026-04-15 (<key takeaway>)

   ### Open Action Items
   - [ ] <me>: action from 2026-04-15 — status?
   - [ ] <person>: action from 2026-03-22 — still pending?

   ### Topics from last 3 1-1s
   - <Topic A>: progressed (last update 2026-04-15)
   - <Topic B>: stale, may be worth revisiting

   ### Wins to share / praise
   - From brag-doc: <wins involving person, last 30 days>

   ### Suggested questions
   - <gap-based question>
   - <project-alignment question>
   - <career question if appropriate>

   ### Notes-to-create after
   - work/1-1/<Person> 2026-04-30.md (use templates/1-1 Note.md)
   ```

## After 1:1

Use `/om-dump` or drop note into `work/meetings/` then `/om-intake` to capture outcomes.

## Important

- **Lead with relationship and growth**, not project status (that's `/om-meeting`).
- **Don't surface every old action item** — only ones still relevant.
- If person note is sparse — suggest enriching it first (could create `## Relationship`, `## Goals`, `## What to Watch` sections).
- Brevity: 1:1 prep should be readable in < 2 minutes.

## References

- [[.claude/commands/om-meeting|om-meeting]] — subject-focused alternative
- [[templates/1-1 Note|1-1 Note.md]] — for capturing outcomes
- [[org/People & Context]]
