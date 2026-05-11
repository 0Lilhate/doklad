---
name: obsidian-atomic
description: Atomic notes lifecycle and discipline for obsidian-mind. Use when creating concept/source/decision notes, splitting bloated notes, or deciding whether material is atomic-ready. Triggers on "atomic note", "concept note", "split this note", "promote to evergreen".
---

# Obsidian Atomic Notes

Discipline for atomic-style notes inside obsidian-mind. Complements `note-normalizer` agent (this skill provides the "what makes a good atom" knowledge; the agent does the work).

## Lifecycle

```
Inbox → Source → Processed Note → Concept | Project | Decision | Output → Archive
```

| Stage | Frontmatter | Where | When to promote |
|---|---|---|---|
| Inbox | `type: source, status: raw` | `Inbox/` | When user processes inbox or `om-process-inbox` runs |
| Source | `type: source, status: raw` | `Sources/Web/`, `Sources/Docs/`, etc. | When normalized into structured note |
| Processed | `type: source, status: processed` | Same `Sources/` location | When concepts extracted |
| Concept | `type: concept, status: evergreen` | `brain/` | Permanent — only when concept stabilizes |
| Project | `type: project, status: active` | `work/active/` | Has goals + deliverable + deadline |
| Decision | `type: decision, status: active` | `work/active/` | Distinct architectural/strategic choice |
| Output | `type: output, status: completed` | `work/archive/YYYY/` | Shipped artifact |
| Archive | `type: archive, status: archived` | `work/archive/YYYY/` | No longer current |

**Никогда не прыгать** через стадии. Web clip → concept без processed-стадии = смешение raw и durable.

## Quality bar для atomic-note

Хороший atom:
- **Одна идея.** Заголовок = тезис в одном предложении.
- **Самодостаточен.** Понятен без открытия source/parent.
- **Reusable.** Можно сослаться из 3+ контекстов без подгонки.
- **Cited.** Каждое внешнее утверждение ссылается на source (URL или wikilink).
- **Linked.** Минимум 2 wikilinks (in or out).
- **Не дубликат.** qmd-проверка перед созданием.

## Когда дробить

Split одной ноты на несколько atoms — **только** если:

1. Нота содержит **несколько независимых концептов**, каждый из которых:
   - Имеет смысл сам по себе
   - Нужен в других notes
2. Source-нота слишком большая (> 500 строк) и **смешивает** raw содержимое с durable выводами.
3. Decision внутри project-ноты заслуживает отдельной decision-ноты (структурное решение, не tactical).
4. Experiment / experiment results заслуживают отдельной experiment-ноты.

## Когда **не** дробить

- Концепт всегда обсуждается в контексте одного проекта → оставить inline.
- Сплит создаст forward-link на пустую ноту, которая не появится → оставить.
- "Может быть полезно отдельно" — слабая мотивация, не сплитить.

## Naming для atomic concept

- **TitleCase** (per `rules/naming.md`).
- Заголовок ноты = тезис, а не общая категория.
  - ✅ `OAuth Token Refresh Race Condition.md`
  - ❌ `OAuth.md`
- Bilingual aliases для key concepts (per `rules/language.md`):
  ```yaml
  aliases:
    - OAuth Token Refresh Race Condition
    - Гонка обновления OAuth-токена
  ```

## Frontmatter

Используй `templates/Concept.md` как baseline. Минимум:

```yaml
---
type: concept
status: evergreen   # только если действительно stable; иначе processed
created: YYYY-MM-DD
updated: YYYY-MM-DD
tags:
  - concept
aliases:
  - <name>
related:
  - "[[<source>]]"   # минимум одна обратная ссылка
---
```

## Workflow при создании

1. **Search first** через qmd: тема уже есть? Если да — link/extend, не создавать дубль.
2. Выбрать stage (concept / project / decision).
3. Применить шаблон (`templates/Concept.md` etc.).
4. Заполнить atomic-bar (одна идея, self-contained, cited, linked).
5. После save — `cross-linker` агент проверяет orphans / missing backlinks.
6. Если concept промотирован из source — ссылка `related: [[<source>]]` обязательна.

## Запреты

- ❌ Никогда `type: concept, status: evergreen` без cited sources.
- ❌ Никогда atomic note без `aliases:` для key (person/project/concept/team).
- ❌ Никогда сплит без plan + approval (если меняет > 2 файла).
- ❌ Никогда промотировать raw clip напрямую в `brain/`.

## Команды

| User says | Action |
|---|---|
| "create atomic note about X" | `/om-new-note concept "X"` |
| "split this note" | `note-normalizer` agent с явным split-plan |
| "promote source to concept" | `note-normalizer` — extract concepts, create separate notes, leave source unchanged |
| "what notes link here" | `cross-linker` agent или Obsidian backlinks pane |

## References

- [[.claude/rules/frontmatter|frontmatter.md]] — required fields per type
- [[.claude/rules/naming|naming.md]] — TitleCase для durable
- [[.claude/rules/language|language.md]] — bilingual aliases
- [[.claude/rules/wikilinks|wikilinks.md]] — when to link
