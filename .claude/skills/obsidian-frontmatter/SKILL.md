---
name: obsidian-frontmatter
description: YAML frontmatter normalization for obsidian-mind. Use when validating frontmatter, fixing duplicate tags, normalizing field aliases (created/created_at/date_created → created), enforcing required fields per type. Triggers on "fix frontmatter", "normalize YAML", "validate frontmatter", "frontmatter audit".
---

# Obsidian Frontmatter Normalization

YAML-frontmatter rules and normalization workflows. Source of truth: `vault-manifest.json` + `rules/frontmatter.md`.

## Что нормализует skill

1. **Field aliases**: `created_at`, `date_created`, `date` → `created`. Все варианты в `vault-manifest.json:field_aliases`.
2. **Tag deduplication**: `tags: [person, person]` → `tags: [person]`.
3. **Tags as array**: `tags: "person,team"` → `tags: [person, team]`.
4. **Required fields per type**: добавление недостающих полей с `null` или derived value.
5. **Type/status validation**: `type` и `status` должны быть из `vault-manifest.json:allowed_*_values`.
6. **Bilingual aliases для key**: `person`, `project`, `concept`, `team` должны иметь оба варианта.
7. **ISO date format**: `created: 2026/04/30` → `created: 2026-04-30`.
8. **Empty list handling**: `related: ""` → `related:` (пустое поле, не null-string).

## Workflow

```
1. Read нот(ы)
2. Parse frontmatter (YAML)
3. Применить правила:
   a. resolve aliases (preserve original, add canonical)
   b. dedup tags
   c. validate enum values (type/status)
   d. add missing required fields
   e. normalize dates
4. Show diff (dry-run по умолчанию)
5. Apply (только с user approval, или --apply на mass-op)
```

## Field aliases (canonical)

Из `vault-manifest.json`:

| Legacy | Canonical |
|---|---|
| `incident-id`, `issue`, `jira` | `ticket` |
| `date_created`, `created_at`, `date` | `created` |
| `modified`, `last_updated`, `updated_at` | `updated` |
| `url`, `link`, `origin` | `source` |
| `links`, `references` | `related` |

**Non-destructive**: оригинальные поля **сохраняются**, canonical добавляется. Удаление оригинала — отдельный шаг с подтверждением.

## Required fields per type

См. `vault-manifest.json:frontmatter_required`:

| Type | Required |
|---|---|
| Default | `type`, `status`, `created`, `tags` |
| concept | + `updated`, `aliases`, `related` |
| source | + `source`, `captured` |
| project | + `updated`, `related` |
| decision | + `related` |
| person | + `aliases`, `related` |
| team | + `related` |
| incident | + `ticket`, `severity`, `role`, `related` |
| session | + `related` |
| meeting | (replace `created` with `date`) + `attendees`, `related` |
| 1-1 | (replace `created` with `date`) + `person`, `related` |

## Type / status validation

Allowed `type` values: `source`, `concept`, `project`, `decision`, `person`, `team`, `tool`, `problem`, `bug`, `incident`, `experiment`, `session`, `meeting`, `1-1`, `competency`, `output`, `area`, `archive`, `index`, `goal`.

Allowed `status` values: `raw`, `processed`, `evergreen`, `active`, `completed`, `archived`, `deprecated`, `draft`.

Любое другое значение — flag как violation.

## Tag normalization

```yaml
# Bad
tags: "work-note, person"

# Bad — дубль
tags:
  - person
  - person

# Bad — mixed case
tags:
  - WorkNote

# Good
tags:
  - work-note
  - person
```

## Date normalization

ISO `YYYY-MM-DD`. Пустое поле — `created:` (не `created: null`, не `created: ""`).

| Bad | Good |
|---|---|
| `2026/04/30` | `2026-04-30` |
| `Apr 30, 2026` | `2026-04-30` |
| `2026-4-30` | `2026-04-30` |
| `30.04.2026` | `2026-04-30` |

## Bilingual aliases (DP14)

Для `type` ∈ {`person`, `project`, `concept`, `team`} — `aliases` обязательны на оба языка, если есть значимый перевод.

```yaml
# Person
aliases:
  - Alice Chen
  - Алиса Чен
  - Alice  # short form

# Project
aliases:
  - Auth Refactor
  - Рефакторинг авторизации
```

Не насильно для каждой ad-hoc заметки — только key.

## Workflow для команд

`/om-frontmatter-fix [path]` использует этот skill:

1. Сканирует path (по умолчанию весь vault, кроме `_archive/`, `.claude/`, `.obsidian/`).
2. Для каждой ноты — apply rules выше.
3. Создаёт **dry-run report** в `thinking/frontmatter-fix-YYYY-MM-DD.md`.
4. Если user one-by-one approve — пишет изменения. Mass-op > 10 файлов — требует `pre-mass-op-snapshot.sh`.

## Quality bar

Нормализованная нота считается "good" если:
- `type` и `status` из allowed-list
- Все required fields per type заполнены (или явно `null` с обоснованием)
- Нет deprecated alias-полей при наличии canonical
- Tags — массив, lowercase, kebab, без дублей
- Dates — ISO YYYY-MM-DD
- Bilingual aliases для key

## Anti-patterns

- ❌ Auto-promote `status: raw` → `status: evergreen` без user approval.
- ❌ Удаление alias-поля без preserved canonical.
- ❌ Mass-rewrite без dry-run.
- ❌ Inventing new properties when canonical exists ("ai-first: true", "urgent: yes").

## References

- `vault-manifest.json` — source of truth
- [[.claude/rules/frontmatter|frontmatter.md]] — canonical rules
- [[.claude/rules/tags|tags.md]] — tag taxonomy
- [[.claude/rules/language|language.md]] — bilingual rules
