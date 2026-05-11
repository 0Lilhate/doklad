# Rules: Language

> **Bilingual pragmatic mode.** Decided 2026-04-30 (DP4 + DP14).
> Vault содержит и английский, и русский контент.

## Принципы

1. **Контент — на языке оригинала.** Не переводить ради перевода.
2. **Aliases — билингвальные для key.** Cross-link discovery работает на оба языка.
3. **Config-слой (CLAUDE.md, rules/, scripts/) — английский.** Это технические артефакты.
4. **Templates — английский.** Для consistency и copy-paste.

## Контент-язык

| Источник | Язык ноты |
|---|---|
| Встреча на русском | RU |
| Confluence страница на русском | RU |
| GitHub README/issue | EN |
| Tech blog post | EN (или язык оригинала) |
| Code review notes | EN |
| Личные размышления | любой, по предпочтению |
| 1-1 с русскоязычным коллегой | RU |
| Decision Record для team-wide audience | язык команды (обычно RU) |

**Правило:** пиши на том языке, на котором будешь думать о теме потом.
Перевод "для consistency" разрушает естественность и теряет нюансы оригинала.

## Filenames

- **TitleCase Latin** — default для technical/structural notes:
  `Auth Refactor.md`, `Decision Record.md`, `OAuth Migration.md`
- **TitleCase Cyrillic** — допускается для имён людей, доменно-специфичных
  проектов, тем без устоявшегося английского эквивалента:
  `Иван Петров.md`, `Проект Альфа.md`, `Бухгалтерская сверка.md`
- Transliteration не обязательна. Cyrillic в filename работает в Obsidian
  и git, не ломает wikilinks.

## Aliases — обязательны для key (DP14)

Категории, где **обязательны** билингвальные aliases:

- `type: person` (`org/people/`)
- `type: project` (`work/active/`)
- `type: concept` (`brain/`)
- `type: team` (`org/teams/`)

Пример:

```yaml
# Сама нота называется Auth Refactor.md
aliases:
  - Auth Refactor
  - Рефакторинг авторизации
  - Authorization Refactor
```

Это позволяет пользователю писать в любой ноте `[[Рефакторинг авторизации]]`
и Obsidian найдёт `Auth Refactor.md`. Так же qmd семантически найдёт оба варианта.

## Aliases — не обязательны

- `type: source` (web clips, docs) — обычно один язык, single-use
- `type: session` — Claude Code session log, single language
- `journal/` daily notes — single language
- `thinking/` черновики

Не насилуй себя двойным набором, если нота однократная.

## Cross-language wikilinks

Можно писать `[[Auth Refactor]]` или `[[Рефакторинг авторизации]]` — оба
резолвятся в один файл, потому что `Auth Refactor.md` содержит этот alias.

## Обработка cyrillic в Bases / qmd

- **Bases**: `.base` filters работают с Cyrillic в frontmatter (`person == "Иван Петров"`).
- **qmd**: индексирует cyrillic embedding'и, semantic search работает на двух языках.
- **Wikilinks**: Obsidian резолвит по filename, поддерживает любую UTF-8.

Никаких специальных шагов не нужно — Unicode "просто работает".

## Workflow для bilingual нот

При создании ноты с типом `person`/`project`/`concept`/`team`:

1. Решить filename language (см. правило выше).
2. Заполнить frontmatter `aliases:` обоими вариантами.
3. Если нужно — добавить третий вариант (transliteration, abbreviation).
4. После создания — `om-link` (Patch 6) или `cross-linker` агент проверит
   consistency aliases в backlinks.

## Skill `obsidian-bilingual` (Block K)

Будущий skill автоматизирует:
- Suggesting aliases при создании key-ноты
- Transliteration utilities
- Detecting missing bilingual aliases в audit
- Cross-language search hints

До его создания — manual discipline по этому файлу.
