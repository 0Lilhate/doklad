# Rules: Wikilinks

> **Когда `[[link]]`, когда `#tag`, когда `[text](url)`.**

## Decision tree

```
Я хочу сослаться на что-то.
│
├─ Это файл в этом vault'е?
│  ├─ ДА → [[wikilink]]
│  └─ НЕТ → продолжить
│
├─ Это категория/тип, а не конкретная сущность?
│  ├─ ДА → #tag
│  └─ НЕТ → продолжить
│
├─ Это URL во внешнюю систему?
│  ├─ ДА → [text](url)
│  └─ НЕТ → продолжить
│
└─ Это термин без существующей ноты, но достоин стать нотой?
   ├─ Возможно → [[wikilink]] (forward-link, нота создастся когда будет смысл)
   └─ Нет — это просто слово → ничего, plain text
```

## Wikilinks — когда

- Существующий target в vault'е: `[[Auth Refactor]]`, `[[Alice Chen]]`
- Forward-ref на ноту, которая **скоро** появится (в течение недели)
- Heading: `[[Auth Refactor#Status]]`
- Block: `[[Auth Refactor#^block-id]]`
- С display text: `[[Auth Refactor|the auth project]]`

### Plurals и склонения

`[[Alice Chen]]` склоняется через alias или display text:
- `Видел [[Alice Chen|Alice]] вчера` — display text
- `Обсудил с [[Alice Chen|Алисой]]` — display text для русского склонения
- В aliases можно положить варианты: `aliases: [Alice Chen, Алиса Чен, Алисе Чен]`

## Tags — когда

- Классификация без navigation target: `#work-note`, `#brain`, `#learning`
- Группировка для Bases-фильтра: `tags: [work-note, project]`
- Status-маркер: `#urgent`, `#blocked`, `#wip`
- См. `tags.md` для taxonomy

## External URLs — когда

- Ссылки во внешние системы (Confluence page, GitHub repo, blog post)
- В source-нотах — `source:` field во frontmatter, плюс `[Original](url)` в body

## Bad patterns

❌ **Wikilink на каждое существительное:**

```markdown
The [[auth]] [[refactor]] for the [[login]] [[service]] needs [[review]].
```

Это шум. Только концептуально-значимые сущности.

❌ **Decorative wikilinks:**

```markdown
This is a [[meta|good]] [[meta|idea]].
```

Decorative wikilinks путают graph view.

❌ **Wikilinks на generic terms без существующей ноты:**

```markdown
Talked about [[engineering]] today.
```

Создаёт orphan-link к воображаемой ноте `engineering.md`. Если нет конкретной ноты — оставь plain text.

❌ **Tag и wikilink на одну сущность:**

```markdown
---
tags: [alice]
related: [[Alice Chen]]
---
```

Дублирование. Wikilink побеждает.

## Bidirectional discipline

Если нота A ссылается на ноту B через `## Related` секцию — нота B **должна**
ссылаться обратно (если связь действительно двусторонняя).

`cross-linker` агент находит missing backlinks и предлагает fix.

Когда не нужно bidirectional:
- `[[wikilink]]` в narrative тексте — не требует backlink
- Источники, цитаты — A → B, без обратной ссылки

## Forward-refs (forward links)

Wikilink на несуществующую ноту допустим, если:
- Нота **скоро** появится (в течение недели)
- Это сигнал "тут нужна нота, создай позже"

Если forward-ref остался unresolved через 2 недели — `vault-librarian`
покажет в orphan-report и предложит либо создать stub, либо убрать ссылку.

## Aliases-discovery

Wikilink резолвится по filename И по `aliases:` во frontmatter target-ноты.

`[[Рефакторинг авторизации]]` найдёт `Auth Refactor.md`, если у того есть
`aliases: [..., Рефакторинг авторизации]` (см. `language.md`).

## Headings и blocks

- `[[Note#Heading]]` — линк на section
- `[[Note#^block-id]]` — линк на конкретный paragraph (block ID `^my-id` после параграфа)
- Используй sparingly — слишком granular ссылки усложняют рефакторинг

## Quality bar

Wikilink хороший если:
- Target существует или **скоро будет**
- Связь содержательна (не decorative, не каждое существительное)
- Помогает navigation/retrieval, а не shadows graph view
- При removal target-ноты — wikilink становится подсказкой для cleanup, а не silent decay
