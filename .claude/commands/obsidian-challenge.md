---
description: "DEPRECATED. Архивирован 2026-04-30. Не использовать. Замена: /om-challenge (создаётся в Patch 6)."
status: deprecated
archived_on: 2026-04-30
archive_path: .claude/_archive/commands/obsidian-challenge.md.old
replacement:
  - command: om-challenge
---

# obsidian-challenge (DEPRECATED)

Команда архивирована и **не должна использоваться**.

## Почему

- Те же broken refs, что и `obsidian-adr.md` (`_CLAUDE.md`,
  `obsidian-second-brain`, `references/ai-first-rules.md`).
- "Spawn parallel subagents: Decisions/Failures/Contradictions" — таких
  агентов не существует. Claude изобретал бы их или делал непредсказуемые
  Task-вызовы.

## Замена

После Patch 6 — `/om-challenge <claim>`. Последовательный поиск
counter-evidence через qmd, без spawn parallel subagents.

## Восстановление

Полный оригинал: `.claude/_archive/commands/obsidian-challenge.md.old`.
