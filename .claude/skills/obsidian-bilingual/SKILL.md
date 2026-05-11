---
name: obsidian-bilingual
description: "Bilingual aliases workflow for RU/EN obsidian-mind vault. Use when creating concept/person/project/team notes that need cross-language discoverability. Triggers on 'add aliases', 'transliterate', 'bilingual aliases', 'cross-language search'."
---

# Obsidian Bilingual

Per DP14 — pragmatic bilingual mode. This skill encodes the rules and provides transliteration / alias-suggestion workflows.

## Mode

`vault-manifest.json:bilingual.mode = "pragmatic"` means:

- **Content** stays in the language of origin (DP4).
- **Aliases** are bilingual **only for key categories**: `concept`, `person`, `project`, `team`.
- For other types — bilingual aliases optional.

## When skill applies

Trigger contexts:
- Creating a note via `/om-new-note <key-type> <name>`.
- Editing existing note's `aliases:` field.
- Detecting missing bilingual aliases via `validate-schemas.ts` или `quality-reviewer`.
- User asks "add transliteration to X" or "this person needs Russian alias".

## Transliteration rules (RU ↔ EN)

### EN → RU (when filename is Latin, suggest Cyrillic)

Use **practical phonetic mapping**, not strict ISO 9. Examples:

| EN | RU |
|---|---|
| Alice Chen | Алиса Чен / Алис Чен |
| Bob Martinez | Боб Мартинес |
| Auth Refactor | Рефакторинг авторизации (semantic, not literal) |
| OAuth Migration | Миграция на OAuth (mixed RU/EN — keep "OAuth" as-is) |
| Q2 Review | Ревью Q2 |

### RU → EN (when filename is Cyrillic, suggest Latin)

Use **GOST R 52535** for personal names (Russian passport standard) или практический транслит:

| RU | EN |
|---|---|
| Иван Петров | Ivan Petrov |
| Алиса Чен | Alice Chen (if known) / Alisa Chen (default transliteration) |
| Проект Альфа | Alpha Project (semantic) / Proekt Alfa (literal) |
| Бухгалтерская сверка | Accounting reconciliation |

### Tech terms — keep as-is

`OAuth`, `JWT`, `gRPC`, `CI/CD`, `RFC`, `JSON`, `Redis`, `Docker`, `K8s`, `LLM`, `MCP`, `qmd`, `obsidian` — **don't translate**, even в Cyrillic-named ноте. Bilingual aliases для tech-terms unnecessary unless user explicitly wants.

### Mixed terms

`OAuth Migration` → можно как `Миграция на OAuth` (mixed). Это норма для tech-environment.

## Workflow: suggesting aliases

When called from `/om-new-note <key-type> <name>`:

1. **Detect title language**:
   - All Latin chars: title = EN
   - All Cyrillic chars: title = RU
   - Mixed: title = mixed (already bilingual-friendly)

2. **Generate suggestions** (per rules above):
   - For pure-EN title: 1-2 RU variants (semantic + literal).
   - For pure-RU title: 1-2 EN variants (practical + GOST).
   - For mixed title: maybe 1 fully-RU and 1 fully-EN variant if useful.

3. **Show user**:
   ```
   Suggested aliases for "Auth Refactor":
     1. Auth Refactor (filename — already alias)
     2. Рефакторинг авторизации (semantic)
     3. Authentication Refactor (full form)
     4. Authorization Refactor (alternative reading)

   Pick which to include. Default: filename + #2 (semantic RU).
   ```

4. **Confirm before write** — never auto-add без user approval.

## Workflow: bilingual audit

Used by `quality-reviewer` and `validate-schemas.ts`:

For each note where `type ∈ {concept, person, project, team}`:
- Read `aliases:`
- If `len(aliases) < 2` → flag missing bilingual.
- If aliases all in one language → flag missing other-language.
- If type=concept and concept is purely tech (e.g. "JWT Token Refresh") — skip flag (acceptable single-language).

## Workflow: cross-language qmd search

When user runs `qmd query`:
- Original query stays as-is.
- If qmd-MCP supports — pass aliases hint.
- If lang detection on note содержимом — surface results in either language naturally.

This is mostly automatic — qmd индексирует cyrillic + latin one collection.

## Anti-patterns

- ❌ **Literal transliteration of titles**: `Auth Refactor.md` → `Аутх Рефактор.md` — useless, не natural.
- ❌ **Translating tech terms** в `aliases:`: `JWT Token` → `Жетон ЖВТ` — wrong.
- ❌ **Auto-adding aliases без user approval** — пользователь должен контролировать как именно его людей зовут.
- ❌ **Forcing bilingual on ad-hoc source-нот** — DP14 ограничивает scope key categories.
- ❌ **GOST 9 для всего** — слишком строгий для людских имён, выглядит роботизированно.

## Examples

### Person — explicit user

```yaml
---
type: person
aliases:
  - Alice Chen          # canonical (filename)
  - Алиса Чен           # Russian phonetic
  - Alice               # short form
---
```

### Project — semantic translation

```yaml
---
type: project
aliases:
  - Auth Refactor       # canonical
  - Рефакторинг авторизации   # semantic Russian
  - Authentication Refactor   # alternate full form
---
```

### Concept — tech-heavy, keep English

```yaml
---
type: concept
aliases:
  - OAuth Token Refresh
  - Обновление OAuth-токена   # mixed (OAuth as-is, rest in Russian)
---
```

(`OAuth` not transliterated to "ОАут" — стандартная мировая аббревиатура.)

### Team — bilingual

```yaml
---
type: team
aliases:
  - Platform Team
  - Команда платформы
  - Платформенная команда
---
```

## When skill does NOT apply

- Daily journal entries (`type: journal`)
- Sources (`type: source`) — use original page language, no aliases mandatory
- Sessions (`type: session`)
- Decisions for one-time tactical calls (`type: decision`) если scope узкий
- Templates themselves
- Internal `.claude/` content

## Tools used

- `obsidian-frontmatter` skill — applies aliases changes
- `validate-schemas.ts` — checks `len(aliases) ≥ 2` for key types
- `quality-reviewer` agent — flags missing bilingual aliases
- `/om-new-note` command — invokes this skill для suggested aliases

## References

- [[.claude/rules/language|language.md]] — DP4/DP14 policy
- [[.claude/rules/frontmatter|frontmatter.md]] — aliases field discipline
- [[.claude/skills/obsidian-frontmatter|obsidian-frontmatter]] — normalization
- `vault-manifest.json:bilingual` — config
