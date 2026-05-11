#!/bin/bash
# rebuild-from-zero.sh — recovery procedure для broken state.
# Re-bootstraps qmd index, verifies hooks, runs full self-audit, reports state.
# Does NOT modify any vault content — recovery is read + verify + re-index.
#
# Usage: bash .claude/scripts/rebuild-from-zero.sh

set -uo pipefail

PROJECT_DIR="${CLAUDE_PROJECT_DIR:-$(pwd)}"
cd "$PROJECT_DIR"

PASS=0
FAIL=0
WARN=0

ok()   { echo "  ✓ $1"; PASS=$((PASS+1)); }
fail() { echo "  ✗ $1"; FAIL=$((FAIL+1)); }
warn() { echo "  ⚠ $1"; WARN=$((WARN+1)); }
section() { echo ""; echo "=== $1 ==="; }

section "1. Git repo state"

if git rev-parse --git-dir > /dev/null 2>&1; then
  ok "git repo present"
  if [[ -z "$(git status --porcelain)" ]]; then
    ok "working tree clean"
  else
    warn "uncommitted changes present (review with git status)"
  fi
else
  fail "not in git repo — recovery limited"
fi

section "2. Critical files"

for f in CLAUDE.md vault-manifest.json .gitignore .mcp.json; do
  if [[ -f "$f" ]]; then ok "$f present"; else fail "$f missing"; fi
done

for d in .claude .claude/agents .claude/commands .claude/skills .claude/scripts .claude/bin .claude/rules; do
  if [[ -d "$d" ]]; then ok "$d/ present"; else fail "$d/ missing"; fi
done

section "3. Vault structure"

source_manifest_value() {
  local field="$1"
  if [[ -f vault-manifest.json ]] && command -v jq > /dev/null 2>&1; then
    # Strip CR (vault-manifest.json may have CRLF on Windows)
    jq -r "$field // empty" vault-manifest.json 2>/dev/null | tr -d '\r'
  fi
}

scaffold=$(source_manifest_value '.scaffold[]')
if [[ -n "$scaffold" ]]; then
  while IFS= read -r f; do
    [[ -z "$f" ]] && continue
    if [[ -f "$f" ]]; then ok "scaffold: $f"; else warn "scaffold missing: $f"; fi
  done <<< "$scaffold"
else
  warn "scaffold list not readable from manifest"
fi

section "4. Hooks"

if [[ -f .claude/settings.json ]]; then
  ok ".claude/settings.json present"
  if grep -q '"hooks"' .claude/settings.json 2>/dev/null; then
    ok "hooks block present in settings.json"
  else
    fail "hooks block missing"
  fi
else
  fail ".claude/settings.json missing"
fi

for f in .claude/bin/claude-block-sensitive-bash.sh .claude/bin/claude-block-sensitive-files.sh .claude/bin/claude-log-blocked.sh; do
  if [[ -f "$f" ]]; then ok "$f present"; else fail "$f missing"; fi
done

section "5. Dependencies"

for cmd in bash jq git node python3; do
  if command -v "$cmd" > /dev/null 2>&1; then
    ok "$cmd in PATH"
  else
    fail "$cmd missing — install required"
  fi
done

for cmd in qmd defuddle; do
  if command -v "$cmd" > /dev/null 2>&1; then
    ok "$cmd in PATH"
  else
    warn "$cmd missing — some workflows degraded"
  fi
done

section "6. qmd index (если установлен)"

if command -v qmd > /dev/null 2>&1; then
  index_name=$(source_manifest_value '.qmd_index')
  if [[ -n "$index_name" ]]; then
    if qmd --index "$index_name" status > /dev/null 2>&1; then
      ok "qmd index '$index_name' available"
    else
      warn "qmd index '$index_name' not initialized — run: node --experimental-strip-types .claude/scripts/qmd-bootstrap.ts"
    fi
  fi
fi

section "7. Run self-validation"

if command -v node > /dev/null 2>&1 && [[ -f .claude/scripts/validate-schemas.ts ]]; then
  echo "  Running validate-schemas.ts..."
  if node --experimental-strip-types .claude/scripts/validate-schemas.ts > /dev/null 2>&1; then
    ok "validate-schemas.ts: no violations"
  else
    warn "validate-schemas.ts found violations — run manually for details"
  fi
fi

if [[ -f .claude/scripts/test-broken-refs.sh ]]; then
  echo "  Running test-broken-refs.sh..."
  if bash .claude/scripts/test-broken-refs.sh > /dev/null 2>&1; then
    ok "test-broken-refs: no broken refs"
  else
    warn "test-broken-refs found issues — run manually for details"
  fi
fi

if [[ -f .claude/scripts/test-hooks.sh ]]; then
  echo "  Running test-hooks.sh..."
  export CLAUDE_PROJECT_DIR="$PROJECT_DIR"
  if bash .claude/scripts/test-hooks.sh > /dev/null 2>&1; then
    ok "test-hooks: all pass"
  else
    warn "test-hooks failures — run manually for details"
  fi
fi

section "Summary"

echo ""
echo "  Pass: $PASS"
echo "  Warn: $WARN"
echo "  Fail: $FAIL"
echo ""

if [[ $FAIL -gt 0 ]]; then
  echo "Status: BROKEN — $FAIL critical issue(s)"
  echo ""
  echo "Recovery suggestions:"
  echo "  - Restore last known good state: bash .claude/scripts/restore-from-snapshot.sh"
  echo "  - Re-clone repo, copy current uncommitted changes manually."
  echo "  - See .claude/docs/recovery.md for detailed procedures."
  exit 1
elif [[ $WARN -gt 0 ]]; then
  echo "Status: DEGRADED — $WARN warning(s)"
  echo ""
  echo "Suggestions:"
  echo "  - Run /om-doctor for environment diagnosis"
  echo "  - Run /om-self-audit for detailed validation report"
  exit 0
else
  echo "Status: HEALTHY"
  exit 0
fi
