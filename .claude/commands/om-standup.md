---
description: "Morning kickoff. Load today's context, review yesterday, surface open tasks, and identify priorities."
---

Run the morning standup:

1. Read `Home.md` for current dashboard state. If file does not exist (vault structure not yet bootstrapped), report "Home.md missing — run Patch 6 to scaffold" and continue.
2. Read `brain/North Star.md` for current goals. Same fallback if missing.
3. Check `work/Index.md` for active projects. Same fallback if missing.
4. Read yesterday's and today's daily notes:
   - **Preferred**: `obsidian daily:read` (requires Obsidian CLI installed and Obsidian app running).
   - **Fallback** (no CLI): use the Read tool to open `journal/$(date +%Y-%m-%d).md` and `journal/$(date -d 'yesterday' +%Y-%m-%d).md` directly. If the `journal/` folder does not exist yet, skip with a note.
5. List open tasks:
   - **Preferred**: `obsidian tasks daily todo`.
   - **Fallback** (no CLI): `grep -rn '^- \[ \]' work/active/ work/incidents/ 2>/dev/null | head -30` — show recent open checkboxes from active work notes.
6. Check recent git activity: `git log --oneline --since="24 hours ago" --no-merges`
7. Check for any unlinked notes or inbox items needing processing (read `Inbox/` if it exists; otherwise skip).

Present a structured standup summary:
- **Yesterday**: What got done (from git log and daily note)
- **Active Work**: Current projects in work/active/ with their status
- **Open Tasks**: Pending items
- **North Star Alignment**: How active work maps to current goals
- **Suggested Focus**: What to prioritize today based on goals + open items

Keep it concise. This is a quick orientation, not a deep dive.
