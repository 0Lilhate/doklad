# Rules: Backup

> **Snapshot обязателен перед mass-ops.** Decided 2026-04-30 (часть DP6/safety).

## Что считается mass-op

| Операция | Mass-op? |
|---|---|
| Edit одной ноты | Нет |
| Создание новой ноты | Нет |
| Переименование одного файла | Нет |
| `git mv` 5+ файлов одной командой | **Да** |
| Любой запуск `vault-audit.py --apply` | **Да** |
| `om-vault-upgrade` (миграция) | **Да** |
| `obsidian_audit.py --apply` (deprecated) | **Да** |
| Bulk frontmatter rewrite через `om-frontmatter-fix` (Patch 6) на > 10 файлах | **Да** |
| `om-archive-stale` массовое архивирование | **Да** |
| Rename папки с детьми | **Да** |

## Pre-mass-op snapshot

Перед mass-op — **обязательный** запуск:

```bash
bash .claude/scripts/pre-mass-op-snapshot.sh
```

Скрипт:
1. Проверяет `git status` — working tree должен быть чистым.
2. Создаёт `git tag snapshot/YYYYMMDD-HHMMSS` с timestamp.
3. Печатает команду restore.

Если working tree dirty — скрипт **fails** с exit 1. Пользователь должен сначала закоммитить или stash'нуть.

## Restore procedure

```bash
# Список последних snapshots
git tag -l 'snapshot/*' --sort=-creatordate | head -10

# Откат к конкретному snapshot
git reset --hard snapshot/20260430-143022

# Если уже отпушено — лучше создать revert-commit, не reset
git revert <commit-range>
```

## Retention

- **Активные snapshots:** 30 последних. Старее — удаляются вручную через
  `git tag -d snapshot/<old>` после ревизии.
- Snapshots живут только в локальной репо если не пушите теги. Это OK для
  личного vault.

## Команды-обёртки

| Команда / агент | Snapshot обязателен? |
|---|---|
| `om-vault-upgrade` | Встроен в команду перед executing |
| `vault-audit.py --apply` | Скрипт сам требует `--backup` флаг (создаёт snapshot) |
| `om-frontmatter-fix` (Patch 6) | На > 10 файлах — automatic snapshot |
| `om-archive-stale` (Block J) | Automatic snapshot перед move |
| Manual `git mv` 5+ files | Пользователь сам запускает `pre-mass-op-snapshot.sh` |

## Запреты

- ❌ **Никогда** не запускать mass-op без snapshot.
- ❌ **Никогда** не использовать `git rebase --hard` или `git filter-repo`
  без отдельного approved patch plan.
- ❌ **Никогда** не удалять snapshot tags автоматически — только после
  ревизии пользователем.

## Подтверждение

После mass-op в `om-vault-audit` следующего цикла:
- проверка целостности wikilinks (broken-links не должно стать больше)
- если стало хуже — restore + investigate

## Не-mass-op гигиена

Для обычных edits / single-file ops — backup не нужен. Достаточно git commit
с осмысленным сообщением. `om-wrap-up` в конце сессии напоминает про commit.
