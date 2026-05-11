---
description: "Self-validation suite: schemas, broken refs, hooks. Per DP6 — manual run, not pre-commit. Output: pass/fail summary."
---

# om-self-audit

Run all self-validation checks. Per DP6 — invoked manually, not via git hooks.

## Usage

```
/om-self-audit
```

## Workflow

Run sequentially, capture pass/fail per step:

### 1. Schema validation
```bash
node --experimental-strip-types .claude/scripts/validate-schemas.ts
```
- Validates `vault-manifest.json` structure.
- Walks all `.md` in `user_content_roots` + scaffold.
- Per-note: `type`/`status` enum check, required fields per type, bilingual aliases for key categories.
- Exit 0 = all valid.

### 2. Broken refs scan
```bash
bash .claude/scripts/test-broken-refs.sh
```
- Slash-command refs (`/om-*`) — target exists.
- Subagent refs (`**\`agent\`**` pattern) — `.claude/agents/<name>.md` exists.
- Script refs (`.claude/scripts/<file>`) — exists.
- Skill refs (`.claude/skills/<name>`) — directory exists.
- Rule refs (`.claude/rules/<name>`) — file exists.
- Exit 0 = no broken refs.

### 3. Hooks smoke-test
```bash
bash .claude/scripts/test-hooks.sh
```
- Simulates `PreToolUse` JSON input to both hooks.
- Verifies blocks on `.env`, `rm -rf`, `cp ... /tmp/`.
- Verifies allow on `git status`, `Read CLAUDE.md`.
- Exit 0 = hooks behave correctly.

### 4. Secrets scan
```bash
bash .claude/scripts/test-secrets.sh
```
- Scans `.mcp.json`, `.claude/settings*.json`, `vault-manifest.json`, `.env*` для plaintext-token-patterns (JWT, base64-tokens, hex32+, AWS/GitHub/Slack tokens, PEM headers, generic password fields).
- Skips placeholders: `${VAR}`, `${env:VAR}`, `<placeholder>`, `YOUR_X_HERE`, `XXXX`, `__TRACKED_VAR__`.
- Never prints secret values — only masked summary с длиной и first/last 2 chars.
- Exit 0 = no patterns matched.

### 5. Aggregate

Compose final report:

```markdown
## /om-self-audit (2026-04-30)

### Schema validation
✓ vault-manifest.json valid
✓ <N> notes, no frontmatter violations
(or: ✗ <N> violations — see report)

### Broken refs
✓ All command/agent/script/skill/rule refs resolve
(or: ✗ <N> broken refs)

### Hooks
✓ <X>/<Y> tests passed
(or: ✗ <X>/<Y> tests passed — <list of failed>)

### Secrets
✓ No plaintext secrets detected
(or: ✗ <N> potential secrets — see masked report)

### Verdict
PASS | FAIL

### Suggested follow-up
- Run `/om-doctor` to check environment dependencies (qmd, defuddle, jq, node)
- If schema violations — `/om-frontmatter-fix` to auto-resolve where possible
- If broken refs — investigate per case (TODO comments? new feature? missing artifact?)
```

## Important

- **Manual only.** Per DP6 — no pre-commit hook auto-trigger.
- **Read-only.** Никаких автоfix'ов. `om-self-audit` репортит — user принимает решение.
- **Run before**: `/om-vault-audit` (eject early если broken refs), `om-vault-upgrade` (sanity check), любой mass-op.
- **Run after**: создание новых команд / агентов / skills (Patches), чтобы catch broken cross-refs.
- **Frequency**: weekly или после крупных изменений. Не каждую сессию.

## Failure modes

| Symptom | Likely cause |
|---|---|
| All schema checks fail | `vault-manifest.json` missing or moved |
| Bash hook tests fail с exit 0 везде | Hook не подключён в `settings.json` или `bash` не в PATH |
| Broken refs появились после patch | TODO comment не убран после implementation |
| Specific note: missing-required | Шаблон не применён, или type/status неправильно во frontmatter |

## References

- `.claude/scripts/validate-schemas.ts`
- `.claude/scripts/test-broken-refs.sh`
- `.claude/scripts/test-hooks.sh`
- [[.claude/commands/om-doctor|om-doctor]] — environment health (complementary)
- [[.claude/commands/om-vault-audit|om-vault-audit]] — vault content audit (different scope)
