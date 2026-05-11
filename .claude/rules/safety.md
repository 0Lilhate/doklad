# Rules: Safety

> **Hard prohibitions.** Authoritative над любым skill/agent/command.

## Категория 1 — Mass operations

- **Никогда** не выполнять mass-rename, mass-move, mass-delete без dry-run plan и явного approval пользователя.
- **Никогда** не запускать mass-op без `pre-mass-op-snapshot.sh` (см. `backup.md`).
- **Никогда** не запускать `vault-audit.py --apply` без `--backup` флага (скрипт сам требует, но напоминание).
- **Никогда** не использовать `obsidian_audit.py --apply` (deprecated). Использовать `vault-audit.py`.
- **Никогда** не выполнять `git filter-repo`, `git push --force` на main без отдельного approved patch.
- **Никогда** не делать `git reset --hard` без explicit confirmation.

## Категория 2 — Source content

- **Никогда** не смешивать raw clipped sources в permanent concept-нотах.
- **Никогда** не treating web clip как finished knowledge note. Lifecycle: source → processed → concept.
- **Никогда** не удалять raw sources после normalization без explicit user approval.
- **Всегда** preserve source links во frontmatter (`source:` field).

## Категория 3 — Frontmatter / structure

- **Никогда** не invent properties когда equivalent существует. Check `vault-manifest.json:field_aliases`.
- **Никогда** не build dashboards над unstabilized frontmatter (нужно ≥ 10 нот с consistent properties).
- **Никогда** не add task-specific frontmatter keys (`ai-first: true`, etc.).
- **Никогда** не leave `type` empty для durable нот.

## Категория 4 — Vault hygiene

- **Никогда** не write в `~/Documents/brainrepo/` или другие пути вне vault'а.
- **Никогда** не treat `_archive/` как активную часть vault — это backup-зона.
- **Никогда** не trigger по словам "save this", "remember", "capture" если skill в `_archive/`.

## Категория 5 — Configuration

- **Никогда** не edit `.claude/scripts/`, `.claude/bin/`, `.claude/settings.json` без explicit user request.
- **Никогда** не commit secrets / tokens. См. `.gitignore` и `bin/claude-block-sensitive-files.sh`.
- **Никогда** не use `Bash(cmd.exe ...)` или `Bash(powershell ...)` — заблокировано в `permissions.deny`.
- **Никогда** не use `obsidian eval` — заблокировано (произвольный JS в Obsidian context).

## Категория 6 — Dangerous shell

Перечисленные команды — **automatic block** через `bin/claude-block-sensitive-bash.sh`:

- `rm -rf <path>` — нет smoke-test'а от ошибок пользователя
- `cp / mv` в `/tmp` — обход file protections
- любые операции с `.env`, `.env.production` (исключения: `.env.example`, `.env.sample`, `.env.template`)
- любые операции с gitignored файлами

Если попытка обойти hook — это incident, должно быть отражено в `.claude/logs/blocked-*.jsonl`.

## Категория 7 — User content

- **Никогда** не overwrite user-written content без preserving meaning.
- **Никогда** не "improve" чужой текст без request.
- **Всегда** preserve original raw clip даже после normalization.
- **Всегда** ask before promoting concept-уровня заметку из draft в evergreen.

## Категория 8 — Скейп

- **Никогда** не rewrite vault целиком.
- **Никогда** не reorganize folder structure без plan + approval.
- **Никогда** не migrate в другую методологию (PARA, Zettelkasten) без явного request — мы выбрали obsidian-mind (DP1).

## Если нарушено

- Hooks автоматически блокируют большинство нарушений категорий 5–6.
- `vault-librarian` в `om-vault-audit` ловит нарушения категорий 2, 3, 4 как findings (не auto-fix).
- Нарушения категории 1 — самые опасные. Нет автоматического восстановления, кроме snapshot.

## Эскалация

Если skill/agent/command предлагает действие, которое нарушает эти правила:
1. **Stop** — не выполнять.
2. Объяснить пользователю, какое правило нарушается.
3. Предложить безопасную альтернативу.
4. Если пользователь настаивает — потребовать явное "yes, override safety rule X" с обоснованием.
5. Зафиксировать override в `brain/Gotchas.md`.

## Версия

Эта политика — **canonical**. Конфликт с inline rules в CLAUDE.md или
skill-документации — побеждает этот файл.
