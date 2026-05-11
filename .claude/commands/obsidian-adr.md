---
description: "DEPRECATED. Архивирован 2026-04-30. Не использовать. Замена: /om-decision (создаётся в Patch 6)."
status: deprecated
archived_on: 2026-04-30
archive_path: .claude/_archive/commands/obsidian-adr.md.old
replacement:
  - command: om-decision
---

# obsidian-adr (DEPRECATED)

Команда архивирована и **не должна использоваться**.

## Почему

Оригинальная команда содержала множественные broken references на несуществующие
сущности этого vault'а:

- skill `obsidian-second-brain` — не установлен
- файл `_CLAUDE.md` — не существует (есть только `CLAUDE.md`)
- папка `Knowledge/` — не существует
- файлы `index.md`, `log.md` — не существуют
- файл `references/ai-first-rules.md` — не существует
- команды `/obsidian-graduate`, `/obsidian-health` — не существуют
- frontmatter ключ `ai-first: true` — не входит в allowed list `CLAUDE.md`

Запуск команды создавал параллельную структуру в vault'е, конкурирующую с
obsidian-mind методологией.

## Замена

После Patch 6 — `/om-decision <title>`. Использует `templates/Decision Record.md`,
пишет в `work/active/` или `work/archive/YYYY/`, обновляет `work/Index.md`.

## Восстановление

Полный оригинал: `.claude/_archive/commands/obsidian-adr.md.old`.
