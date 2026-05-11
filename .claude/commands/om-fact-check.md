---
description: "Verify claims in a note have sources cited. Reports unsupported claims (statements without [[wikilink]] or [URL])."
---

# om-fact-check

Find factual claims that lack source attribution. AI-quality control to catch hallucination/drift.

## Usage

```
/om-fact-check <note path>
```

## Workflow

### 1. Read note

Parse frontmatter + body. Skip frontmatter, focus on body content.

### 2. Identify claims

Sentences that look factual:
- Numeric/quantitative ("4-hour MTTR", "12k users affected", "10x faster")
- Dated events ("released 2024", "Q2 2026 launch")
- Named-entity assertions ("OAuth 2.0 specifies X", "Alice said Y")
- Causal claims ("X causes Y", "because of Z")
- Historical statements ("we previously decided X")
- Comparative ("simpler than Y", "more reliable than Z")

Skip:
- Clearly opinion ("I think", "in my view", "we should consider")
- Internal references with wikilinks already
- Code blocks, frontmatter, callouts'd `> [!quote]` content (already attributed)

### 3. For each claim — check support

| Support type | What counts |
|---|---|
| Wikilink in same paragraph | `[[Source A]]`, `[[brain/X]]` |
| External URL | `[text](https://...)` |
| Quote attribution | `> [!quote]` callout ABOVE claim with source |
| `## Sources` section с relevant entry | Sources section в той же ноте |
| Inline source attribution | `(per [[Source]])`, `(see <URL>)`, `(from <person>)` |

If none of these — claim is **unsupported**.

### 4. Report

```markdown
## Fact Check: <note path>

### Claims found: 12
### Supported: 9
### Unsupported: 3

### Unsupported claims (review)

1. **Section: ## Why it matters**
   > "OAuth 2.0 token rotation is widely considered better than long-lived tokens."
   - No nearby wikilink or URL.
   - Suggestion: cite RFC 6749 §6 or Auth0/Okta best-practice doc.

2. **Section: ## Details**
   > "Auth Refactor saved 4 hours/week of developer time."
   - Quantitative claim without source.
   - Suggestion: link to retrospective или dashboard data.

3. **Section: ## Connections**
   > "We tried Redis-backed sessions in 2024 and it failed."
   - Historical claim without source.
   - Suggestion: link to [[Decision 2024-XX-XX Redis Sessions]] (создать ноту если её нет).

### Supported claims (sample)

- "RFC 6749 §6 describes the refresh token flow." → cited [[Sources/Docs/RFC 6749]]
- "Alice raised concern about rotation race в 1-1 2026-04-15." → cited [[work/1-1/Alice Chen 2026-04-15]]

### Verdict
3 unsupported claims. Suggested action: add citations OR mark as opinion ("I believe…", "in my view…") OR remove.
```

## Important

- **Read-only.** Никаких edits.
- **Не false-positive opinions.** "I think X is better" — это opinion, не factual claim.
- **Bilingual support** — claims на русском тоже проверяй.
- **Not strict scientific citation.** Wikilink to vault note считается as source — это knowledge graph, не academic paper.
- If note нулевые claims — skip с "✓ no factual claims requiring citation".

## Use cases

- After AI-assisted writing (`/om-dump`, `note-normalizer`) — verify what Claude wrote actually has support.
- Before promoting `status: processed` → `evergreen` — quality bar check.
- During `/om-vault-audit` для random sample.
- Before sharing нот с stakeholders.

## Не делать

- Не verify external claims через web — too slow, не scope. Just check that claim **has** a source link/ref, не whether source actually says X.
- Не mass-run по всему vault'у autoматом — invokes manual review per note.
- Не auto-add citations — отчёт only, user decides.

## References

- [[.claude/agents/quality-reviewer|quality-reviewer]] — sibling check (frontmatter quality)
- [[.claude/rules/wikilinks|wikilinks rules]] — when to link
- [[.claude/skills/obsidian-atomic|obsidian-atomic]] — quality bar для concept-нот
