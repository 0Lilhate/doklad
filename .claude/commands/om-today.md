---
description: "Open or create today's daily journal at journal/YYYY-MM-DD.md. Per DP9: by request, no auto-create."
---

# om-today

Today's journal entry. Per DP9 — only on request, no auto-create on session start.

## Usage

```
/om-today
```

## Workflow

1. **Determine today's date** in ISO format `YYYY-MM-DD`.
2. **Check** if `journal/YYYY-MM-DD.md` exists.
3. **If exists**: open and show it.
4. **If absent**: ask user "Create today's journal entry?"
   - If yes — create from minimal template (below).
   - If no — abort, suggest other commands (`/om-standup`, `/om-dump`).
5. **Show summary** of today's entry — focus, accomplishments so far, open tasks.

## Template (if creating)

```markdown
---
type: source
status: processed
date: YYYY-MM-DD
tags:
  - journal
related:
  - "[[brain/North Star]]"
---

# YYYY-MM-DD

## Mood / Energy
-

## Focus
What's the one thing today is for?

## Accomplished
-

## Captured / Routed
- (links to notes created/touched today)

## Open
- [ ]

## Tomorrow
-

## Reflections
```

## Important

- **Per DP9** — никаких auto-create. Только по запросу `/om-today`.
- Daily entry — не source-of-truth, а scratchpad. Promote значимое в `brain/`, projects, decisions.
- Не создавать `.gitkeep` — `journal/` уже имеет один.
- `/om-standup` читает journal без создания. `/om-today` — для добавления.

## References

- [[journal/]] — folder
- [[brain/North Star]] — alignment check
- [[.claude/commands/om-standup|om-standup]] — read-only view of recent entries
