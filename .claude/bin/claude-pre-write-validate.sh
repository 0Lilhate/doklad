#!/bin/bash
# claude-pre-write-validate.sh — opt-in PreToolUse hook for Write/Edit on .md files.
# Strict mode: rejects Write/Edit if frontmatter would be invalid per vault-manifest.json.
#
# NOT wired up in .claude/settings.json by default (per DP6 — manual self-audit preferred).
# To enable: add to settings.json hooks.PreToolUse with matcher "Write|Edit", filter to *.md.
#
# Usage (auto-invoked by Claude Code if wired):
#   echo '<json>' | bash .claude/bin/claude-pre-write-validate.sh
#
# Exit codes:
#   0 — content has valid frontmatter (or not a .md file — pass through)
#   2 — content would violate frontmatter rules (block)

set -uo pipefail

# Always exit 0 on internal errors — don't break Claude's flow on hook bugs.
trap 'exit 0' ERR

# Ensure jq is discoverable (on Git Bash Windows it usually lives in ~/bin)
export PATH="$PATH:$HOME/bin"

input=$(cat)
tool_name=$(echo "$input" | jq -r '.tool_name // empty')
file_path=$(echo "$input" | jq -r '.tool_input.file_path // empty')
content=$(echo "$input" | jq -r '.tool_input.content // .tool_input.new_string // empty')

# Logging helper
log_blocked() {
  bash "${CLAUDE_PROJECT_DIR:-.}/.claude/bin/claude-log-blocked.sh" "$tool_name" "$1" "$2" 2>/dev/null || true
}

# Skip if not Write/Edit
case "$tool_name" in
  Write|Edit) ;;
  *) exit 0 ;;
esac

# Skip non-.md files
[[ "$file_path" != *.md ]] && exit 0

# Skip files in excluded folders
case "$file_path" in
  */.claude/*|*/_archive/*|*/.obsidian/*|*/templates/*|*/thinking/*)
    exit 0
    ;;
esac

# Skip RESERVED files
basename_file=$(basename "$file_path")
case "$basename_file" in
  README.md|CLAUDE.md|MEMORY.md|Home.md|CHANGELOG.md)
    exit 0
    ;;
esac

# Skip if no content (Edit без new_string)
[[ -z "$content" ]] && exit 0

# Check frontmatter exists
if ! echo "$content" | head -1 | grep -q '^---'; then
  log_blocked "$file_path" "no frontmatter block in .md write"
  echo "🚫 Strict mode: durable .md write requires YAML frontmatter." >&2
  echo "   File: $file_path" >&2
  echo "   Add --- ... --- block at top, or write to thinking/, _archive/, or templates/ instead." >&2
  exit 2
fi

# Extract frontmatter (between first two --- markers)
frontmatter=$(echo "$content" | awk '
  /^---$/ { if (in_fm) { exit } else { in_fm=1; next } }
  in_fm { print }
')

# Check `type:` exists and non-empty
if ! echo "$frontmatter" | grep -qE '^type:\s*\S+'; then
  log_blocked "$file_path" "frontmatter missing 'type' field"
  echo "🚫 Strict mode: frontmatter must have non-empty 'type:' field." >&2
  echo "   File: $file_path" >&2
  echo "   See .claude/rules/frontmatter.md for allowed type values." >&2
  exit 2
fi

# Check type is in allowed list (если manifest присутствует)
manifest_path="${CLAUDE_PROJECT_DIR:-.}/vault-manifest.json"
if [[ -f "$manifest_path" ]]; then
  type_value=$(echo "$frontmatter" | grep -E '^type:\s*\S+' | head -1 | sed -E 's/^type:\s*//' | tr -d '"\047')
  if [[ -n "$type_value" ]]; then
    is_allowed=$(jq -r --arg t "$type_value" '.allowed_type_values | contains([$t])' "$manifest_path" 2>/dev/null)
    if [[ "$is_allowed" == "false" ]]; then
      log_blocked "$file_path" "type '$type_value' not in allowed_type_values"
      echo "🚫 Strict mode: type '$type_value' not in vault-manifest.json:allowed_type_values." >&2
      echo "   File: $file_path" >&2
      exit 2
    fi
  fi
fi

# All checks passed
exit 0
