---
name: chronicle-session-documenter
description: "DEPRECATED. Архивирован 2026-04-30. Требовал внешнюю Chronicle DB которой нет. Замена: agents/session-documenter.md (после rewrite в Patch 7)."
status: deprecated
archived_on: 2026-04-30
archive_path: .claude/_archive/skills/chronicle-session-documenter/
replacement:
  - agent: session-documenter
---

# chronicle-session-documenter (DEPRECATED)

Этот skill архивирован и **не должен активироваться**.

## Почему

- Требовал внешнюю Chronicle DB и MCP-сервер `mcp__chronicle__*` — ни
  того, ни другого нет в этой среде.
- Hardcoded путь `Chronicle/Sessions/Session-{id}.md` не совпадает с
  фактической структурой vault'а.

## Что использовать вместо

После Patch 7 — `agents/session-documenter.md` (rewritten под session-log
по CLAUDE.md, `type: session, status: processed`, ссылки на artefacts через
`related`).

## Восстановление

Полный оригинал (315 строк) в git history. См. frontmatter
`.claude/_archive/skills/chronicle-session-documenter/SKILL.md` →
`restore_from_git`.
