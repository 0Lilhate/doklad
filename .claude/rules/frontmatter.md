# Rules: Frontmatter

> **Canonical YAML schema for vault notes.** Authoritative for any conflict
> between this file, CLAUDE.md inline rules, and skill/agent docs.
> Source of truth: `vault-manifest.json` (`frontmatter_required`, `allowed_type_values`, `allowed_status_values`).

## Minimum frontmatter

Every durable note (anything in `work/`, `org/`, `perf/`, `brain/`, `Sources/`):

```yaml
---
type:
status:
created:
tags:
---
```

## Allowed `type` values

Defined in `vault-manifest.json:allowed_type_values`:

`source`, `concept`, `project`, `decision`, `person`, `team`, `tool`,
`problem`, `bug`, `incident`, `experiment`, `session`, `meeting`, `1-1`,
`competency`, `output`, `area`, `archive`, `index`, `goal`.

## Allowed `status` values

Defined in `vault-manifest.json:allowed_status_values`:

`raw`, `processed`, `evergreen`, `active`, `completed`, `archived`, `deprecated`, `draft`.

## Required fields per type

See `vault-manifest.json:frontmatter_required`. Summary:

| Type | Required |
|---|---|
| **Default** | `type`, `status`, `created`, `tags` |
| **concept** | + `updated`, `aliases`, `related` |
| **source** | + `source`, `captured` |
| **project** | + `updated`, `related` |
| **decision** | + `related` |
| **person** | + `aliases`, `related` |
| **team** | + `related` |
| **incident** | + `ticket`, `severity`, `role`, `related` |
| **session** | + `related` |
| **meeting** | replace `created` with `date`; + `attendees`, `related` |
| **1-1** | replace `created` with `date`; + `person`, `related` |

## Field discipline

- **Dates** — ISO `YYYY-MM-DD`, no timezones, no time-of-day for daily fields. Use `datetime` only for incident timestamps where seconds matter.
- **`source`** — URL, vault wikilink, or file path. Never empty for `type: source`.
- **`related`** — list of `[[wikilinks]]`, not free text.
- **`aliases`** — searchable name variants only. **Bilingual обязательны** for `person`, `project`, `concept`, `team` (per DP14).
- **`tags`** — list, lowercase, kebab-case, parent/child via `/`. See `tags.md`.

## Field aliases

If a note has legacy fields, the manifest defines normalized targets:

| Legacy field | Normalize to |
|---|---|
| `incident-id`, `issue`, `jira` | `ticket` |
| `date_created`, `created_at`, `date` | `created` |
| `modified`, `last_updated`, `updated_at` | `updated` |
| `url`, `link`, `origin` | `source` |
| `links`, `references` | `related` |

`om-frontmatter-fix` (Patch 6) applies these aliases non-destructively (preserves original fields, adds canonical).

## Strict rules

- **Never invent properties** if equivalent exists. Check `vault-manifest.json:field_aliases` first.
- **Never** add task-specific or one-off keys (`ai-first: true`, `urgent: yes`, etc.). Use tags.
- **Never** leave `type` empty for durable notes.
- **Never** mix raw clip content with concept-level frontmatter (`status: evergreen` on raw source).
- **Always** preserve existing fields when normalizing — additions only.

## Examples

### Concept

```yaml
---
type: concept
status: evergreen
created: 2026-04-30
updated: 2026-04-30
tags:
  - knowledge
  - patterns
aliases:
  - Authorization Refactor
  - Рефакторинг авторизации
related:
  - "[[Auth System]]"
  - "[[Decision 2026-04-15 OAuth Migration]]"
---
```

### Source (web clip)

```yaml
---
type: source
status: raw
source: https://example.com/article
created: 2026-04-30
captured: 2026-04-30
tags:
  - article
  - reference
---
```

### Decision

```yaml
---
type: decision
status: active
created: 2026-04-30
tags:
  - decision
  - work-note
related:
  - "[[Auth Refactor]]"
  - "[[Alice Chen]]"
---
```

### Incident

```yaml
---
type: incident
status: completed
ticket: INC-1234
severity: P2
role: incident-commander
created: 2026-04-15
tags:
  - incident
  - work-note
related:
  - "[[Login Service]]"
  - "[[Postmortem 2026-04-15]]"
---
```

### 1-1

```yaml
---
type: 1-1
status: processed
date: 2026-04-30
person: "[[Alice Chen]]"
tags:
  - work-note
  - 1-1
related:
  - "[[Alice Chen]]"
  - "[[Q2 Goals]]"
---
```
