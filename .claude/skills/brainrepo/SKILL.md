---
name: brainrepo
description: "DEPRECATED. Архивирован 2026-04-30. Не использовать. Замена: команда om-dump + агент note-normalizer. Полный оригинал в .claude/_archive/skills/brainrepo/."
status: deprecated
archived_on: 2026-04-30
archive_path: .claude/_archive/skills/brainrepo/
replacement:
  - command: om-dump
  - agent: note-normalizer
---

# brainrepo (DEPRECATED)

Этот skill архивирован и **не должен активироваться**.

## Почему

- Жёсткий путь `~/Documents/brainrepo/` уносил контент **вне vault'а** —
  не git-tracked в этом репо, теряется для Obsidian.
- Broad-триггеры ("save this", "remember", "note", "capture", "brain dump",
  daily/weekly review) перехватывали обычные реплики и подменяли
  obsidian-mind workflow.
- Конкурировал с CLAUDE.md (`work/`, `org/`, `perf/`, `brain/`,
  `Sources/`, `Inbox/`).

## Что использовать вместо

| Сценарий | Замена |
|---|---|
| "Save this: ..." / freeform capture | `/om-dump` |
| Раз-в-день обработка inbox | `/om-process-inbox` (Patch 6) |
| Weekly review | `/om-weekly` |
| Превратить сырое в durable | агент `note-normalizer` |

## Восстановление

Если действительно нужен оригинал:

```bash
cp -r .claude/_archive/skills/brainrepo/* .claude/skills/brainrepo/
# Перезапустить Claude Code.
```

Полный текст оригинала — в `.claude/_archive/skills/brainrepo/`.
