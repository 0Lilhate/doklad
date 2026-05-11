#!/bin/bash
# test-secrets.sh — scan for plaintext secrets in tracked configuration files.
# Runs known regex patterns against .mcp.json, .claude/settings*.json, .env*,
# and selected vault-meta files. Reports findings without printing secret values.
#
# Usage: bash .claude/scripts/test-secrets.sh
# Exit: 0 — no patterns matched; 1 — potential secrets found

set -uo pipefail

VIOLATIONS=0
SCAN_FILES=(
  ".mcp.json"
  ".claude/settings.json"
  ".claude/settings.local.json"
  "vault-manifest.json"
  ".env"
  ".env.local"
  ".env.production"
  ".env.development"
)

# Patterns: each is "<name>|<regex>"
# Regexes try to match common token shapes without false-positiving on placeholders.
PATTERNS=(
  "JWT|eyJ[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}\.[A-Za-z0-9_-]{10,}"
  "Confluence PAT (base64-style)|[A-Za-z0-9+/]{40,}={0,2}"
  "Hex token (32+)|[a-fA-F0-9]{32,}"
  "AWS Access Key|AKIA[0-9A-Z]{16}"
  "GitHub PAT|ghp_[A-Za-z0-9]{36}"
  "GitHub fine-grained|github_pat_[A-Za-z0-9_]{82}"
  "Slack token|xox[baprs]-[A-Za-z0-9-]{10,}"
  "Generic Bearer in JSON|\"Bearer [A-Za-z0-9._-]{20,}\""
  "Private key header|-----BEGIN (RSA |EC |DSA |OPENSSH |)PRIVATE KEY-----"
  "Generic password field with value|\"password\"\\s*:\\s*\"[A-Za-z0-9._@!#$%^&*+-]{8,}\""
)

# Patterns that are KNOWN-OK (env placeholders, references, doc examples)
SAFE_PATTERNS=(
  '\${[A-Za-z_][A-Za-z0-9_]*}'                    # ${ENV_VAR}
  '\${env:[A-Za-z_][A-Za-z0-9_]*}'                # ${env:ENV_VAR}
  '<[A-Za-z][A-Za-z0-9 _-]+>'                     # <placeholder>
  'YOUR_[A-Z_]+_HERE'                             # YOUR_TOKEN_HERE
  'XXXX+'                                          # XXXX masks
  '__TRACKED_VAR__'                                # tracked-var placeholder
)

is_safe_context() {
  local line="$1"
  for pattern in "${SAFE_PATTERNS[@]}"; do
    if echo "$line" | grep -qE "$pattern"; then
      return 0
    fi
  done
  return 1
}

mask() {
  # Print only length and first/last 2 chars for context (without revealing secret).
  local s="$1"
  local len=${#s}
  if (( len <= 6 )); then
    printf '<masked %d chars>' "$len"
  else
    printf '<masked %d chars: %s…%s>' "$len" "${s:0:2}" "${s: -2}"
  fi
}

echo "## Secret pattern scan"
echo

scanned=0
for f in "${SCAN_FILES[@]}"; do
  if [[ ! -f "$f" ]]; then
    continue
  fi
  scanned=$((scanned + 1))

  while IFS= read -r entry; do
    name="${entry%%|*}"
    regex="${entry#*|}"

    while IFS= read -r match; do
      [[ -z "$match" ]] && continue
      line=$(grep -nE "$regex" "$f" 2>/dev/null | head -1)
      lineno=$(echo "$line" | cut -d: -f1)
      raw=$(echo "$line" | cut -d: -f2- | sed -E 's/^[[:space:]]+//')

      if is_safe_context "$raw"; then
        continue
      fi

      masked=$(mask "$match")
      echo "  ⚠ $f:$lineno — $name match: $masked"
      VIOLATIONS=$((VIOLATIONS + 1))
    done < <(grep -oE "$regex" "$f" 2>/dev/null | sort -u)
  done < <(printf '%s\n' "${PATTERNS[@]}")
done

echo
echo "Scanned: $scanned files"
echo "Violations: $VIOLATIONS"
echo

if [[ $VIOLATIONS -eq 0 ]]; then
  echo "✓ No plaintext secrets detected in known config files"
  exit 0
else
  echo "✗ Potential secrets detected. Review manually."
  echo "  - Verify each match is not a false positive (commit hash, content hash, etc.)."
  echo "  - If real secret: rotate at source, replace with env placeholder, scrub git history if committed."
  echo "  - Add SAFE_PATTERNS entry if it's a known-safe placeholder type."
  exit 1
fi
