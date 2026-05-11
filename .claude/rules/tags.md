# Rules: Tags

> **Tag taxonomy and discipline.** Используется `note-normalizer`,
> `om-tags-audit` (Patch 6), `vault-librarian`.

## Формат

- **lowercase**, kebab-case (`work-note`, не `WorkNote` или `work_note`).
- Иерархия через `/`: `work-note/incident`, `work-note/1-1`, `learning/book`.
- Без spaces, без emoji, без punctuation кроме `-` и `/`.
- Длина — короткие, 1–3 слова.

## Canonical tag taxonomy

### Type-теги (зеркалят `type:` frontmatter)

`person`, `team`, `incident`, `project`, `decision`, `concept`, `source`,
`session`, `competency`, `goal`, `tool`.

### Domain-теги (тематические)

- `work-note` — любая work-related нота
- `work-note/1-1` — 1-on-1 заметки
- `work-note/incident` — инциденты
- `brain` — durable memory (gotchas, patterns, decisions)
- `journal` — daily/monthly entries
- `learning` — обучение
- `learning/book`, `learning/course`, `learning/article`
- `reading-list` — to-read очередь

### Status-теги (опционально, frontmatter `status:` предпочтительнее)

Используй только когда `status:` недостаточен:

- `urgent`, `blocked`, `waiting`, `idea`, `wip`

### Project / area теги

- `area/health`, `area/family`, `area/career` — для нот в `Areas/` (если структура расширится)
- `project/<slug>` — для нот, относящихся к конкретному проекту, но живущих не в его папке

## Tag vs Wikilink (cross-ref `wikilinks.md`)

| Цель | Использовать |
|---|---|
| Классификация: "это work-note" | `tag` |
| Связь с конкретной сущностью: "об этом проекте" | `wikilink` |
| Группировка для Bases-фильтра | `tag` |
| Цитируемая референция | `wikilink` |

Правило: **если есть target-нота — wikilink; если категория — tag**.

## Frontmatter синтаксис

Tags — массив, не string:

```yaml
# Хорошо
tags:
  - work-note
  - work-note/incident
  - brain

# Плохо
tags: work-note, work-note/incident, brain
```

## Запрещённые паттерны

- ❌ Дубликаты в массиве: `tags: [person, person]` — `om-tags-audit` ловит и предлагает fix.
- ❌ Mixed-case: `tags: [WorkNote]` или `tags: [work-Note]`.
- ❌ Spaces: `tags: [work note]`.
- ❌ Emoji-теги: `tags: [✅]`, `tags: [🔥]`.
- ❌ Тег и wikilink на одну сущность одновременно (`tags: [alice]` + `[[Alice Chen]]` в related): дублирование. Wikilink побеждает.

## Audit checks

`om-tags-audit` (Patch 6) проверяет:
- duplicate tags в массиве
- orphan tags (не в taxonomy, на 1 ноте)
- stale tags (нигде не используются)
- naming violations (UPPERCASE, snake_case, spaces)

## Обновление taxonomy

Новые теги добавляются в этот файл **только** после 3+ использований. До того
— используются ad-hoc, потом либо мигрируют в canonical, либо в `om-tags-audit`
flagged как orphan для решения.
