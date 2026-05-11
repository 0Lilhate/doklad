---
name: obsidian-moc
description: Maps of Content (MOC) patterns and discipline. Use when creating thematic indexes, navigation pages, or curated note collections. Triggers on "create MOC", "index page", "map of content", "thematic index", "knowledge map".
---

# Obsidian MOC (Maps of Content)

Patterns для curated thematic indexes в obsidian-mind. Complements `bases/` (live filtered views) — MOC = human-curated порядок, описание, группировка.

## MOC vs Bases — когда что

| Use case | Tool |
|---|---|
| Live-фильтр по frontmatter (все active projects, все incidents) | `.base` |
| Curated порядок (приоритет, последовательность чтения, narrative grouping) | MOC |
| Тематический индекс с описаниями каждого linked-нот | MOC |
| Onboarding-страница для темы | MOC |
| Cross-domain связи, которые не выражаются одним фильтром | MOC |
| Dashboard поверх Bases | MOC (с embedded `.base`) |

**Правило**: если можно одним фильтром — `.base`. Если нужно "порядок + объяснение + группа" — MOC.

## Типы MOC

### 1. Structural MOC

Карта корня области знаний. Примеры: `Home.md`, `work/Index.md`, `brain/Memories.md`.

Структура:
```markdown
# {Area Name}

## Overview
What this area covers.

## Sub-areas
- [[Sub-area 1]] — short description
- [[Sub-area 2]]

## Active items
- [[Item 1]] — status
- [[Item 2]]

## Archive
- [[Archived Item]]
```

### 2. Thematic MOC

Curated тема, которая не привязана к области. Примеры: `Authentication MOC.md`, `Performance Reviews MOC.md`.

Структура:
```markdown
---
type: index
status: evergreen
tags:
  - moc
  - <theme>
---

# {Theme} MOC

## Why this exists
Context — what is this MOC for, who reads it, when.

## Core concepts
- [[Concept A]] — defines X
- [[Concept B]] — relates to A via Y

## Active projects
- [[Project 1]] — status

## Decisions
- [[Decision A]] — rationale: ...

## Sources / further reading
- [[Source A]]
- [[Source B]]
```

### 3. Dashboard MOC

MOC поверх Bases. Embed `.base` views внутрь markdown с контекстом.

```markdown
# Home

## Vault Health
![[bases/Vault Health.base]]

## Active Work
![[bases/Work Dashboard.base#Active]]

## Recent decisions
![[bases/Work Dashboard.base#Decisions]]
```

### 4. Navigation MOC

Карта-указатель: где что лежит. Примеры: `org/People & Context.md`.

## MOC discipline

### Создание

1. **Search first** через qmd — нет ли уже похожего MOC.
2. Решить тип (structural / thematic / dashboard / navigation).
3. Минимум 5 linked нот для оправдания MOC. Меньше — нет смысла.
4. Frontmatter `type: index, status: evergreen`.
5. Каждый wikilink — с **коротким описанием** (one-liner). Голый список — это `.base`, не MOC.

### Обновление

1. **Не auto-вставлять** новые ноты. MOC — curated.
2. `om-moc-update` сканирует и **предлагает** добавления, ждёт approval.
3. Промежуточный режим: список "Recently added" в конце MOC, переносится вручную в основные секции.

### Naming

- TitleCase (per `rules/naming.md`).
- Suffix `MOC` обязателен для thematic: `Authentication MOC.md`.
- Structural без suffix: `Home.md`, `work/Index.md`.

### Archive

- MOC не архивируется автоматически. Только если тема исчерпана и заменена другим MOC.
- При archive — переместить в `_archive/moc/` с date-prefix.

## Frontmatter

```yaml
---
type: index
status: evergreen
created: YYYY-MM-DD
updated: YYYY-MM-DD
tags:
  - moc
  - <theme-tags>
aliases:
  - <theme name>
  - <bilingual variant>
related:
  - "[[<root MOC>]]"
---
```

## Quality bar

Хороший MOC:
- **Curated**: видно, что человек думал, а не auto-generated.
- **Annotated**: каждый wikilink с описанием.
- **Stable**: ноты, на которые ссылается, реально durable (не drafts).
- **Updated**: `updated:` отражает реальную ревизию (раз в месяц минимум для активных тем).
- **Linked**: backlinks с notes, на которые MOC ссылается (cross-linker проверяет).
- **Discoverable**: из `Home.md` или `work/Index.md` есть путь к этому MOC.

## Anti-patterns

- ❌ MOC из 3 wikilinks — лучше inline-список в parent ноте.
- ❌ MOC без описаний — это просто `.base` фильтр.
- ❌ MOC, который всё-всё перечисляет — должен быть selective.
- ❌ MOC, который никогда не обновляется — stale, либо удалить, либо переписать.
- ❌ Auto-generated MOC через скрипт — это `.base`, не MOC.

## Команды

| User says | Action |
|---|---|
| "create MOC for X" | `/om-moc <topic>` (Patch 6 — создаёт черновик) |
| "update Auth MOC" | `/om-moc-update <moc-file>` (предлагает new links) |
| "what's in Auth MOC" | Read `<MOC>.md` |
| "MOC vs Bases — что выбрать" | Decision tree выше |

## References

- `bases/*.base` — examples of `.base` views, complementary
- [[.claude/skills/obsidian-bases|obsidian-bases skill]]
- [[.claude/skills/obsidian-atomic|obsidian-atomic skill]]
- [[.claude/rules/wikilinks|wikilinks.md]] — link discipline
