# Rules: Naming

> **File and folder naming conventions.** Decided 2026-04-30 (DP2).

## TitleCase для durable folders

Применяется в:
- `work/{active,archive,incidents,1-1,meetings}/`
- `org/{people,teams}/`
- `perf/{competencies,evidence}/`
- `brain/`
- `Sources/{Web,Docs,GitHub,Papers,Videos}/`
- `templates/`
- `bases/`

**Правило:** каждое слово с заглавной буквы, пробелы разрешены, дефис разрешён.

| Хорошо | Плохо |
|---|---|
| `Auth Refactor.md` | `auth-refactor.md` |
| `Alice Chen.md` | `alice_chen.md` |
| `Q2 2026 Review.md` | `q2-2026-review.md` |
| `Decision Record.md` | `decision_record.md` |
| `Work Dashboard.base` | `work-dashboard.base` |

## kebab-case разрешён

Применяется в:
- `_archive/`
- `thinking/`
- `reference/`

Эти зоны для черновиков, копий, сырых материалов — naming-дисциплина смягчена.

## ISO-даты для temporal files

Daily, 1-1, meeting notes:

| Тип | Шаблон | Пример |
|---|---|---|
| Daily journal | `YYYY-MM-DD.md` | `2026-04-30.md` |
| 1-1 | `<Person> YYYY-MM-DD.md` | `Alice Chen 2026-04-30.md` |
| Meeting | `YYYY-MM-DD <Topic>.md` | `2026-04-30 Auth Refactor Sync.md` |
| Incident postmortem | `YYYY-MM-DD <Incident>.md` | `2026-04-15 Login Outage.md` |

## Bilingual filenames (DP14)

Latin (TitleCase) — default для технических/structural notes (`Auth Refactor.md`).

Cyrillic — допускается для имён людей и проектов где это естественно
(`Иван Петров.md`, `Проект Альфа.md`). Transliteration не обязательна.

Для cross-link discovery — добавляй `aliases:` на оба языка во frontmatter
(см. `language.md`).

## Запрещённые паттерны

- ❌ Суффиксы: `Final`, `v2`, `v3`, `copy`, `new`, `(1)`, `(2)`
- ❌ Mixed date formats: `2-4-26`, `Feb24`, `апр-26`
- ❌ Spaces в начале/конце имени
- ❌ Спецсимволы кроме дефиса и пробела: `*`, `?`, `#`, `<`, `>`, `:`, `\`, `/`, `|`, `"`
- ❌ Дубликаты имён в разных папках без уточнителей (`Auth.md` и `Auth.md` в разных папках — добавь `Auth Project.md` / `Auth Concept.md`)

## Reserved files

Эти файлы **никогда** не переименовываются автоматически:

- `README.md`, `CLAUDE.md`, `MEMORY.md`, `Home.md`, `LICENSE`
- `vault-manifest.json`, `CHANGELOG.md`, `.gitignore`, `.mcp.json`

## Folder depth

- **Max 3 уровня** в durable folders (`work/active/<Project>/<sub>` — допустимо если проект сложный, иначе `work/active/<Project>.md`).
- Под-папки внутри `work/active/<Project>/`: `meetings/`, `decisions/`, `risks/` — только если 5+ нот в каждой.

## Применение

- `vault-audit.py` (Patch 5) проверяет соответствие, не делает auto-rename.
- `obsidian-organizer/scripts/obsidian_audit.py` — **deprecated** в пользу `vault-audit.py`.
- Manual rename — через `git mv` + `pre-mass-op-snapshot.sh`.
