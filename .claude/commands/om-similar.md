---
description: "Find semantically similar notes via qmd vsearch. Args: <note path or topic>. Returns top-N ranked by similarity."
---

# om-similar

Semantic neighbors discovery. Different from `qmd query` (exact terms) — uses vector similarity for "related but not identical".

## Usage

```
/om-similar <note path>
/om-similar "<topic phrase>"
```

Examples:
```
/om-similar brain/OAuth Token Refresh.md
/om-similar "race conditions in async auth"
/om-similar work/active/Auth Refactor.md
```

## Workflow

### 1. Resolve input

If path → read note's title + first 2 paragraphs as query basis.
If topic phrase → use as-is.

### 2. qmd vsearch

```
mcp__qmd__query searches=[{type:"vec", query:"<basis>"}] -n 10
```

Or CLI fallback: `qmd --index <name> vsearch "<basis>" --json -n 10`.

### 3. Filter

Drop:
- The input note itself (если path given).
- Notes in `_archive/`, `.claude/`, `thinking/`.
- Notes with `status: deprecated` or `status: archived`.

### 4. Categorize

Group results by `type`:
- Core concepts (`type: concept`) — durable knowledge connections.
- Active projects (`type: project, status: active`) — applied/applicable.
- Decisions (`type: decision`) — choices that touched this topic.
- Sources (`type: source`) — primary material.
- People (`type: person`) — who knows about this.

### 5. Show with similarity scores

```markdown
## Similar to: "<input>"

### Core concepts (3)
- [[brain/Token Rotation Patterns]] (0.87) — concept, evergreen
  > Brief excerpt from note's Summary section
- [[brain/OAuth State Machine]] (0.82)
- [[brain/Async Auth Race Conditions]] (0.78)

### Active projects (2)
- [[work/active/Auth Refactor]] (0.81) — current
- [[work/active/SSO Migration]] (0.74)

### Decisions (1)
- [[work/active/OAuth Provider Choice]] (0.79)

### Sources (4)
- [[Sources/Docs/RFC 6749]] (0.85)
- [[Sources/Web/Auth0 Token Rotation Guide]] (0.78)
- [[Sources/Papers/OAuth Replay Attacks 2024]] (0.72)
- [[Sources/Videos/OAuth Deep Dive Talk]] (0.68)

### People (1)
- [[org/people/Alice Chen]] (0.71) — works on Auth Refactor

### Suggested follow-up
- 4 of 11 results don't backlink to input note — consider adding via `cross-linker`.
- 2 results (Sources) raw — run `note-normalizer` to extract concepts.
```

## Important

- **Read-only.** Никаких file edits.
- **Show similarity score** — let user judge whether matches are real or noise.
- **Threshold 0.65 default** — below = noise. Configurable via mental "show all" if user says так.
- **Dedup aliases** — note с aliases `[Foo, Бар]` не показывать дважды.
- If qmd unavailable — degraded fallback на Grep по title; flag warning.

## Use cases

- Before creating new concept-note — check «уже не написано ли где-то».
- Brainstorming connections для MOC.
- Research: «что в vault есть про X» с broader semantic match чем `qmd query`.
- After capturing source — find "where it fits".

## References

- `qmd` skill — vsearch semantics
- [[.claude/agents/cross-linker|cross-linker]] — followup для backlinks
- [[.claude/agents/knowledge-architect|knowledge-architect]] — для placement decisions
