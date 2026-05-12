# Claude Code Operating Manual for Obsidian Knowledge Base

This file is the **orchestrator** for Claude Code in this Obsidian vault. It is not a subagent and must not be invoked as one. It defines how Claude Code thinks about the vault, which skills and agents to use, and what is forbidden.

If a rule here conflicts with a skill's internal documentation, **this file wins**.

> **Rules location (since 2026-04-30, Patch 5):** canonical rules live in `.claude/rules/`:
> - [[.claude/rules/frontmatter|frontmatter.md]] — YAML schema, allowed types/status, required fields
> - [[.claude/rules/naming|naming.md]] — TitleCase для durable, kebab для archive/thinking, ISO для temporal
> - [[.claude/rules/tags|tags.md]] — taxonomy
> - [[.claude/rules/language|language.md]] — bilingual pragmatic mode (RU + EN)
> - [[.claude/rules/wikilinks|wikilinks.md]] — когда `[[link]]`, `#tag`, `[text](url)`
> - [[.claude/rules/backup|backup.md]] — обязательный snapshot перед mass-ops
> - [[.claude/rules/safety|safety.md]] — hard prohibitions
>
> Inline-сводки ниже сохранены для контекста. **При конфликте — `.claude/rules/*.md` побеждает.**

---

## Current state (2026-05-12)

This is a **talk-preparation vault**, not a generic obsidian-mind vault yet. Primary deliverable: a 60-minute Russian-language talk *«AI-agent как усилитель delivery-процесса: от требования до MR»* with live demo on **opencode** against a real Java microservice.

**On disk right now:**

- `talk/` — 10 working talk artifacts (~3500 lines, `type: output`). Master script: `talk/AI-Agent Delivery Workflow.md`. Demo script: `talk/Live Demo Script.md`. Other operational docs: `Speaker Notes.md`, `Pre-Show Checklist.md`, `Fallback Demo Script.md`, `Real Stories.md`, `Templates.md`, `Cannot Show.md`, `Call To Action.md`, `ai_agent_patterns_section_reworked.md`.
- `Sources/Web/` — 4 research notes feeding the talk: `AI Agents.md`, `Analyst Workflow.md`, `Developer Workflow.md`, plus the older full draft `ai_agent_delivery_talk_full.md`.
- `Sources/programm/packagesearch/` — **a live Java/Spring service** used as the demo target. See "Live demo target" section below. Treat this directory as a real codebase, not vault material.
- `.claude/` — agents, slash-commands (`/om-*`), rules, scripts, hooks. Configured.
- `.idea/`, `.obsidian/` — IDE/editor metadata.

**Not yet on disk (target layout per obsidian-mind, do not assume present):** `work/`, `org/`, `perf/`, `brain/`, `templates/`, `bases/`, `journal/`, `thinking/`, `Inbox/`, `Home.md`, `vault-manifest.json`. Several `.claude/` references are also aspirational: `.claude/docs/`, the file `senior-vectorized-flute.md` referenced by `Live Demo Script.md`, the `fallback/` folder referenced by `Fallback Demo Script.md`, and `demo/ticket.md` referenced by step 1 of the demo. Treat structural references in this file and in `.claude/rules/` as **intent**; verify with `Glob`/`Read` before assuming.

When asked to create a note in a folder that does not yet exist, create the folder but flag it to the user — folder choices are policy decisions, not housekeeping.

`.mcp.json` provides: `github`, `context7`, `exa`, `memory`, `playwright`, `sequential-thinking`. Reach for them before reinventing.

**Language:** vault content (`talk/`, `Sources/`) is **Russian**; `.claude/` config, rules, scripts, and code in `packagesearch/` are **English**. Do not "normalize for consistency" across this boundary — it is intentional.

---

## Purpose

This vault is an AI-assisted knowledge base built around the [obsidian-mind](https://github.com/breferrari/obsidian-mind) operating model. Claude Code is the assistant for:

- capturing raw material (web clips, transcripts, dumps),
- normalizing it into durable knowledge,
- linking it correctly,
- surfacing it through Bases, canvases, and retrieval,
- maintaining vault hygiene over time.

Claude Code does not own the vault. The user owns the vault. Claude Code proposes, audits, and executes — but never restructures unilaterally.

**Right now, the dominant working context is the talk** — not generic vault hygiene. When the user asks an ambiguous question, default to "is this about the talk, the demo service, or the obsidian-mind infrastructure?" before reaching for a vault-wide tool.

---

## Live demo target — `Sources/programm/packagesearch/`

This subdirectory is a **real Java/Spring Boot 3 microservice** (Java 17, Maven, parent `ru.it_alnc.ita_forms.core:ita-lib-bom`, artifactId `fprodsearch`). It is the demo subject for the talk's 20-minute live segment. The 12-step demo adds derived field `Integer daysRemaining` to `POST /FL/gracePeriod` response (`GracePeriodResponseDto`).

**Treat this directory as a codebase, not vault notes:**

- Do not apply `note-normalizer`, `defuddle`, `obsidian-*` skills, or frontmatter rules to `.java`, `.yaml`, `Dockerfile`, `pom.xml`.
- Do not add YAML frontmatter to `README.md` here.
- `qmd` indexing is for vault notes; do not include Java sources unless the user explicitly asks.

**Common commands (run from `Sources/programm/packagesearch/`):**

| Command | Purpose |
|---|---|
| `mvn clean install` | Full build + tests |
| `mvn test` | Tests only |
| `mvn -Dtest=GracePeriodServiceTest test` | **Single test (used live on stage in demo step 10)** |
| `mvn -Dtest=ClassName#methodName test` | Single test method |
| `mvn dependency:go-offline` | Warm `.m2` cache (run as part of pre-show T-25) |
| `bash devops/build_app.sh` | Full docker build per project convention |

**Runtime env (from local `README.md`):** the service connects to Postgres at `postgres.it-alnc.ru:5432` (schema `PRC`), Kafka `itaforms-node1/2.it-alnc.ru:9092`, and `ita-info` at `192.168.0.18:8071`. **Before any live `mvn test` on stage:** verify the target test class is unit-only (no real DB/Kafka calls). If integration tests are present, isolate them behind a profile.

**AGENTS.md:** the talk teaches the AGENTS.md pattern; the demo repo should have one. If `Sources/programm/packagesearch/AGENTS.md` is missing when the user prepares for the talk, surface that gap.

---

## Vault principles

1. **obsidian-mind is the operating system.** Lifecycle, agent roles, and routing follow that model.
2. **Raw and durable notes are separate.** A web clip is never a final knowledge note.
3. **Retrieval first, generation second.** Always check what the vault already knows before producing new notes.
4. **Frontmatter is the spine.** Properties are stabilized before dashboards or large refactors.
5. **Edits are surgical.** No mass renames, mass moves, or vault-wide rewrites without an explicit dry-run plan and user approval.
6. **Source truth is preserved.** Original links, clips, and transcripts stay intact after normalization.
7. **Folder structure follows what already exists.** Do not invent `work/org/perf` or other layouts unless the user has them.

---

## Common scripts (run from vault root)

| Script | Purpose |
|---|---|
| `bash .claude/scripts/pre-mass-op-snapshot.sh` | **Required** before any mass-op (creates `git tag snapshot/…`). |
| `python .claude/scripts/vault-audit.py` | Naming/structure audit. Dry-run by default; needs `--apply --backup` to mutate. |
| `bash .claude/scripts/test-broken-refs.sh` | Find broken wikilinks across the vault. |
| `bash .claude/scripts/test-hooks.sh` | Verify the `PreToolUse` safety hooks fire as expected. |
| `bash .claude/scripts/test-secrets.sh` | Verify the secret-blocking hook. |
| `bash .claude/scripts/restore-from-snapshot.sh` | Roll back a mass-op via snapshot tag. |
| `node --experimental-strip-types .claude/scripts/validate-schemas.ts` | Frontmatter + `vault-manifest.json` schema validation. |
| `node --experimental-strip-types .claude/scripts/qmd-bootstrap.ts` | Build initial `qmd` index. |

TypeScript scripts run via `node --experimental-strip-types` — no compile step. Python scripts use stdlib only.

---

## Deeper reading

These cover material this file deliberately omits to stay short.

- `.claude/README.md` — directory map + authority order (rules > CLAUDE.md > manifest > agents > commands > skills)
- `.claude/docs/architecture.md` — five-layer separation (commands / agents / skills / rules / scripts)
- `.claude/docs/agents-map.md` — agent catalog with triggers
- `.claude/docs/troubleshooting.md` — common issues and fixes
- `.claude/docs/recovery.md` — undo a mass-op
- `.claude/docs/contributing.md` — how to add a new workflow
- `.claude/CHANGELOG.md` — history of `.claude/` itself

---

## Installed skills and responsibilities

Each skill has **one job**. Do not stretch a skill outside its lane.

| Skill | Use it for | Do NOT use it for |
|---|---|---|
| `obsidian-markdown` | Writing/validating Obsidian-flavored markdown (callouts, embeds, properties, wikilinks). | Generic markdown; non-Obsidian output. |
| `obsidian-cli` | File operations on vault notes (create, move, rename, read structured). | Anything outside the vault. |
| `obsidian-bases` | `.base` dashboards and views in `bases/`. | Notes, frontmatter design, retrieval. |
| `json-canvas` | `.canvas` files: visual maps, architecture maps, knowledge graphs. | Linear notes. |
| `defuddle` | Cleaning a fetched web page before saving as markdown. | Knowledge-note authoring. |
| `qmd` | Semantic + keyword retrieval before answering or writing. | Editing notes. |
| `obsidian-organizer` | Audits, naming conventions, folder consistency, migration planning. | Day-to-day note authoring. |
| `obsidian-clipper-template-creator` | Authoring/updating Web Clipper templates. | Anything outside Web Clipper templates. |
| `obsidian-atomic` | Atomic-note lifecycle and discipline (split bloated notes, promote to evergreen). | Dashboards; web-clip cleanup. |
| `obsidian-bilingual` | RU/EN aliases for `concept`/`person`/`project`/`team` notes. | Filename rewriting. |
| `obsidian-frontmatter` | YAML normalization, duplicate-tag fixes, field aliasing. | Note authoring. |
| `obsidian-moc` | Maps of Content (thematic indexes, navigation pages). | Atomic notes. |

**Deprecated (do not invoke; archived 2026-04-30, stubs remain):**

- `chronicle-session-documenter` → use the `session-documenter` agent instead.
- `brainrepo` → use the `note-normalizer` agent (which used to call brainrepo internally) and `/om-dump`.

Not installed by default:

- `para-pkm` — do not use unless the user explicitly requests a PARA audit.

---

## Agent routing

Subagents live in `.claude/agents/`. Pick the agent whose description matches the trigger; do not improvise.

| Trigger | Agent |
|---|---|
| "What does the vault know about X?" / load context on a person, project, incident, concept | `context-loader` |
| Missing wikilinks, orphan notes, unlinked mentions | `cross-linker` |
| Stale notes, duplicate metadata, broken links, frontmatter validation, vault health | `vault-librarian` |
| Importing another vault, large structural changes, classification of unknown content | `vault-migrator` (dry-run first; no execution without explicit approval) |
| Raw notes, clipped pages, transcripts, dumps, messy markdown → durable knowledge | `note-normalizer` |
| Building or updating `.base` dashboards and views | `dashboard-builder` |
| Designing/improving Web Clipper templates and intake mappings | `capture-designer` |
| Logging a Claude Code / dev / research session | `session-documenter` |
| Searching the open web, docs, GitHub, papers | `external-researcher` |
| Multi-source synthesis with trend identification | `research-synthesizer` (only when the user explicitly asks for synthesis, not single-fact lookup) |
| Surfacing uncaptured wins, recognition, competency demonstrations | `brag-spotter` (used by `/om-weekly`, `/om-wrap-up`) |
| Structural placement of a new concept/project — which MOC, which dashboard, which backlinks | `knowledge-architect` |
| Checking freshly-created or modified notes against `.claude/rules/` | `quality-reviewer` (used by `/om-wrap-up`; reports, does not auto-fix) |

If two agents seem to fit, pick the narrower one and explain the choice in one line.

---

## Commands routing

Commands live in `.claude/commands/` and are invoked as `/<name>`. Most-used:

| Command | Use when |
|---|---|
| `/om-dump` | Freeform capture; let the orchestrator route content to the right notes. |
| `/om-intake` / `/om-process-inbox` | Process unread meeting notes / inbox items. |
| `/om-standup` | Morning kickoff: load context, yesterday, open tasks, priorities. |
| `/om-today` | Open or create today's daily journal. |
| `/om-meeting` / `/om-prep-1on1` | Prep for a meeting or 1:1 on a topic / person. |
| `/om-weekly` / `/om-monthly` | Cross-session synthesis. |
| `/om-wrap-up` | End-of-session wrap-up. |
| `/om-new-note` / `/om-new-project` / `/om-decision` | Create an atomic note / project / decision record. |
| `/om-source-clip` / `/om-confluence-import` | Capture an external source. |
| `/om-vault-audit` / `/om-frontmatter-fix` / `/om-tags-audit` / `/om-broken-links` | Vault health and fixes. |
| `/om-vault-upgrade` | Migration/upgrade flow (uses `vault-migrator`). |
| `/om-doctor` / `/om-self-audit` / `/om-metrics` | Environment + integrity checks. |
| `/om-humanize` | Voice calibration of Claude-drafted text. |
| `/om-challenge` | Red-team a current claim against vault history. |

For the full list see `.claude/commands/`.

**Deprecated stubs (do not invoke):** `/obsidian-adr` → `/om-decision`; `/obsidian-challenge` → `/om-challenge`.

Hooks live in `.claude/bin/` (sensitive-bash + sensitive-files blockers, called via `PreToolUse` per `.claude/settings.json`). Automation scripts live in `.claude/scripts/`. Do not edit either casually.

---

## Note lifecycle

```
Inbox  →  Source  →  Processed Note  →  Concept | Project | Decision | Output  →  Archive
```

- **Inbox**: anything not yet triaged. Short-lived.
- **Source**: raw clipped page, transcript, paste, screenshot. `type: source`, `status: raw`. Source link mandatory.
- **Processed Note**: cleaned, structured, summarized. `status: processed`. Still references its sources.
- **Concept / Project / Decision / Output**: durable, evergreen knowledge. `status: evergreen` or `active`.
- **Archive**: kept for history. `status: archived`. Do not delete unless the user explicitly says so.

A clipped page never skips levels. Source → Processed → Concept is the path.

---

## Frontmatter conventions

Canonical schema (allowed `type` / `status`, required fields per type, field aliases) lives in `.claude/rules/frontmatter.md`. Read it before authoring durable notes.

Minimum spine:

```yaml
---
type:
status:
created:
tags:
---
```

Quick rules:

- Dates are ISO `YYYY-MM-DD`.
- Do not invent new properties when an equivalent exists.
- `source` is a URL, file path, or `[[wikilink]]`; never empty for `type: source` or `status: processed`.
- `related` holds wikilinks, not free text.
- `aliases` is for searchable name variants — bilingual (RU + EN) обязательны для `concept` / `person` / `project` / `team` (см. `.claude/rules/language.md`).

Properties are stabilized **before** building any new dashboard.

---

## Source handling

- A web clip is captured via Web Clipper → `defuddle` cleans it → saved with `type: source`, `status: raw`.
- Source notes always preserve the original URL, capture date, and author/site if known.
- Raw sources are never deleted automatically. After normalization, they remain unless the user explicitly approves removal.
- Never mix raw clipped content into a concept/permanent note. Quote, link, summarize — but keep the boundary.

---

## Retrieval rules

Before writing a new note or answering a knowledge question:

1. Run `qmd` query first (semantic + keyword) for the topic and its aliases.
2. If `qmd` returns hits, prefer reading and citing existing notes over generating new ones.
3. Use `Grep`/`Glob` only as a fallback when `qmd` is unavailable or returns nothing.
4. After creating or editing a note, re-check `qmd` for duplicates and related notes the user should link.

`context-loader` is the agent for non-trivial retrieval; do not reimplement its behavior inline.

---

## Normalization rules

When turning raw material into a processed note, separate clearly:

- **Facts** — what the source actually states. Cite.
- **Interpretations** — your reading of it. Mark as such.
- **Decisions** — what was decided and by whom.
- **Assumptions** — things taken as given but not verified.
- **Open questions** — what is still unknown.
- **Tasks** — actionable follow-ups, with owner if known.

Use Obsidian callouts (`> [!note]`, `> [!warning]`, `> [!todo]`) to make these visible. `note-normalizer` is the agent that owns this transformation.

---

## Dashboard rules

- Dashboards are `.base` files in `bases/`, built by `dashboard-builder` using the `obsidian-bases` skill.
- `bases/` is currently empty; reuse existing `.base` properties before inventing new ones once the directory has files.
- Do **not** create a new dashboard until the underlying frontmatter properties are stable and present on enough notes (rule of thumb: ≥10 notes with the property).
- A dashboard that filters on a property must list that property's allowed values explicitly.

---

## Capture rules

- Web Clipper templates are authored only via `obsidian-clipper-template-creator` and live where the skill places them.
- `capture-designer` agent designs the template behavior (selectors, frontmatter mapping, routing folder).
- Cleaning the clipped HTML is `defuddle`'s job. Do not reimplement HTML stripping in scripts.
- Every clipped note lands as `type: source`, `status: raw`, with `source:` populated.

---

## Session documentation rules

- Use the `session-documenter` agent only for: Claude Code sessions, coding sessions, research sessions.
- Do **not** use it for: meeting notes, daily logs, journals, decisions — those have their own commands (`/om-meeting`, `/om-today`, `/om-decision`).
- A session note has `type: session`, lives in `work/sessions/YYYY-MM-DD <Topic>.md`, and links via `related` to the artifacts it produced.

---

## Migration and audit rules

- Any migration starts with **audit / dry-run**. Output a plan, get approval, then execute.
- `vault-migrator` runs in two modes: classification (analyze and propose) and execution (only after explicit approval).
- `obsidian-organizer` is the auditor for naming, folders, and consistency. Use it before proposing renames.
- Every batch operation produces a manifest the user can review and revert.
- Never run a migration in the same turn as the user's first request — always present a plan first.

---

## Safety rules (hard)

- **Never** mass-rename, mass-move, or mass-delete without a dry-run plan that the user has approved.
- **Never** rewrite the entire vault.
- **Never** mix raw clipped sources into permanent concept notes.
- **Never** treat a web clip as a finished knowledge note.
- **Never** build dashboards on top of unstabilized frontmatter.
- **Never** create new properties when an equivalent already exists.
- **Always** preserve source links.
- **Always** separate facts, interpretations, decisions, assumptions, open questions, and tasks.
- **Always** start migrations with audit/dry-run.
- **Never** delete raw sources after normalization without explicit user approval.
- **Never** edit `.claude/scripts/` or hooks unless the user explicitly asks.

---

## Prohibited behavior

- Inventing folder structures the user has not adopted.
- Invoking deprecated skills (`brainrepo`, `chronicle-session-documenter`) — use their replacements (`note-normalizer` + `/om-dump`, `session-documenter`).
- Using `para-pkm` by default.
- Treating `CLAUDE.md` as a subagent, or invoking it as one.
- Generating long abstract documents when the user asked for a note.
- Adding emojis to notes unless the user asked.
- Creating documentation files (READMEs) unless the user explicitly asked.
- Running destructive shell commands (`rm -rf`, force pushes, hard resets) without explicit approval.
- Polluting frontmatter with task-specific or one-off keys.

---

## Quality bar

A note is "done" when:

- Frontmatter is complete and matches allowed `type` / `status` values.
- All claims either cite a source or are clearly marked as interpretation/assumption.
- Wikilinks point to real notes (or are explicitly marked as forward refs).
- The note appears once and only once in `qmd` for its topic (no near-duplicates).
- It fits its lifecycle stage — no concept-note ambitions on a still-raw clip.

A dashboard is "done" when:

- It uses only stabilized properties.
- Filters and views are documented inside the `.base` file.
- It does not duplicate an existing dashboard.

---

## Default workflow examples

### 1. User pastes a URL or raw text

1. Ask whether to capture as **Source** (clip-style) or **Inbox dump** (route via `/om-dump`).
2. If Source: run `defuddle` → save with `type: source`, `status: raw`, source link populated. Stop there.
3. If Inbox dump: invoke `/om-dump` and let it route.

### 2. User asks "what do we know about X?"

1. Run `qmd` query for X and its aliases.
2. If hits exist: use `context-loader` to assemble a briefing.
3. If no hits: say so explicitly. Offer to spawn `external-researcher` if the user wants outside-the-vault search.

### 3. User wants a clipped page turned into a real note

1. Confirm the source note exists and is `status: raw`.
2. Invoke `note-normalizer`. It produces a processed note with separated facts/interpretations/etc., linked back to the source.
3. Run `cross-linker` on the new note to surface missing wikilinks.
4. Leave the source note in place.

### 4. User wants a new dashboard

1. Check whether the relevant property exists on enough notes (≥10) and is consistently used.
2. If not: propose a frontmatter stabilization pass first (via `vault-librarian` or `obsidian-organizer`).
3. Only after frontmatter is stable: invoke `dashboard-builder` with `obsidian-bases`.

### 5. User wants to import another vault

1. Run `/om-vault-upgrade` → `vault-migrator` in classification mode.
2. Present the migration map (counts, target folders, frontmatter rewrites, conflicts).
3. Wait for explicit approval.
4. Execute in batches with manifests.

### 6. User asks for a session log

1. Only if the session is dev / research / Claude-Code work.
2. Invoke the `session-documenter` agent.
3. Save with `type: session` under `work/sessions/`, link related artifacts via `related`.

### 7. User wants to edit the talk

1. Treat `talk/AI-Agent Delivery Workflow.md` as the master script. `Sources/Web/ai_agent_delivery_talk_full.md` is the older draft — read for reference, do not edit in parallel.
2. Companion docs (`Live Demo Script.md`, `Speaker Notes.md`, `Pre-Show Checklist.md`, `Fallback Demo Script.md`, `Real Stories.md`, `Templates.md`, `Cannot Show.md`, `Call To Action.md`) are operational artifacts. Edits should keep their cross-references (`related:` in frontmatter) consistent.
3. Russian content. Do not "improve" prose stylistically without an explicit request — voice is deliberate (see `/om-humanize`).
4. Timing budgets in `Live Demo Script.md` (per-step minutes) are load-bearing — flag any edit that changes total to ≠ 20:00 demo / 60:00 talk.

### 8. User wants to do something with `packagesearch/`

1. This is real Java code, not vault material. Use standard code tools (`Read`, `Edit`, `Grep`, `Bash` with `mvn`).
2. Do not add YAML frontmatter. Do not run `note-normalizer` or any `obsidian-*` skill against this directory.
3. Before running `mvn test` live or as a check, verify the target test class is unit-only — see "Live demo target" section.

---

## Where things live

- Vault-level docs and config: vault root.
- Subagents: `.claude/agents/`.
- Slash commands: `.claude/commands/`.
- Skills: `.claude/skills/`.
- Hooks: `.claude/bin/` (PreToolUse blockers).
- Automation scripts: `.claude/scripts/` (do not edit casually).
- Rules (canonical policy): `.claude/rules/`.
- Bases dashboards: `bases/` (not yet present on disk).
- Templates: `templates/` (not yet present on disk).
- Memory (Claude Code): managed via the auto-memory system at `C:\Users\Egor\.claude\projects\C--Users-Egor-work-doklad\memory\`, not as vault notes.
- Talk artifacts: `talk/` (master: `talk/AI-Agent Delivery Workflow.md`). Research feeding the talk: `Sources/Web/`. Demo target codebase: `Sources/programm/packagesearch/`.

When in doubt: search first, ask second, write last.
