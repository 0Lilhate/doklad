---
description: "Red-team a current claim/plan against vault history. Sequential qmd search for counter-evidence. No spawn parallel subagents."
---

# om-challenge

Pressure-test a position against the vault's own past — find contradictions, prior failures, flagged risks. Replaces broken `obsidian-challenge`.

## Usage

```
/om-challenge "<claim or plan>"
```

If no argument — infer from recent conversation.

## Workflow

1. **Restate** the claim in 1-2 sentences. Show user, ask "Is this what you mean?"
2. **Extract premises** — what is taken as given for this claim to hold?
3. **Sequential qmd searches** (NOT parallel subagents):
   - **Decisions** — `qmd query "<topic>" + filter type:decision`. Find past decisions that touched similar tradeoffs.
   - **Incidents** — `qmd query "<topic>" + filter type:incident`. Past failures linked to similar approach.
   - **Gotchas** — read `brain/Gotchas.md`. Anything from past mistakes applies?
   - **Past contradictions** — qmd vsearch for opposite framings ("we should NOT do X").
4. **Synthesize**:
   - **Restated claim**
   - **Hidden premises** (what would invalidate this)
   - **Counter-evidence** (cited from vault notes, with `[[wikilinks]]` and dates)
   - **Blind spots** (what user might be ignoring based on their own past)
   - **Verdict**: consistent / cautionary / contradicted
5. **Log** in today's `journal/YYYY-MM-DD.md` под `## Thinking` секцией (если daily-нота существует, иначе skip).

## Output

```
## Red Team — <claim> (2026-04-30)

### Restated claim
"<claim>"

### Hidden premises
- Premise 1
- Premise 2

### Counter-evidence from vault
- [[Decision 2026-02-14 Auth Provider]] — chose simpler path; this proposes opposite
- [[Login Outage 2026-03-15]] — similar approach failed in prod
- [[brain/Gotchas#OAuth state machine]] — flagged "watch for race"

### Blind spots
- Past similar plans took 2x longer than estimated
- Team was 2x larger then; capacity now is constraint

### Verdict
**Cautionary**: claim isn't contradicted, but past evidence suggests scope разделить и pilot first.
```

## Important

- **No spawn parallel subagents** — sequential qmd is enough and предсказуемее.
- **Don't be agreeable** — challenge sharply, even if user wrote the claim.
- **Cite specific notes** — date + wikilink, не "vault history shows".
- If nothing contradictory found — **say so honestly**. Don't invent counter-evidence.
- This is `om-challenge` (constructive red team), not "second-guess everything" — paralysis-by-doubt — это failure mode.

## References

- [[brain/Gotchas|brain/Gotchas.md]]
- [[brain/Key Decisions|brain/Key Decisions.md]]
- [[work/Index|work/Index.md]] → Decisions Log
- Replaces deprecated `commands/obsidian-challenge.md` (in `_archive/`)
