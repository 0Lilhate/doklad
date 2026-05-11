#!/bin/bash
# Claude Code hook: Block access to .env files and gitignored files
# Used with PreToolUse matcher: Read|Edit|Write|Glob|Grep
set -euo pipefail

# Ensure jq is discoverable (on Git Bash Windows it usually lives in ~/bin)
export PATH="$PATH:$HOME/bin"

input=$(cat)
tool_name=$(echo "$input" | jq -r '.tool_name')
tool_input=$(echo "$input" | jq -r '.tool_input')

# Logging helper — call before any exit 2
log_blocked() {
  bash "${CLAUDE_PROJECT_DIR:-.}/.claude/bin/claude-log-blocked.sh" "$tool_name" "$1" "$2" 2>/dev/null || true
}

# Extract file path based on tool type
case "$tool_name" in
  Read|Write|Edit)
    file_path=$(echo "$tool_input" | jq -r '.file_path // empty')
    ;;
  Glob|Grep)
    file_path=$(echo "$tool_input" | jq -r '.path // empty')
    ;;
  *)
    exit 0
    ;;
esac

# Skip if no file path
[[ -z "$file_path" ]] && exit 0

# Allow /tmp — no sensitive files live here, needed for temp workspaces
[[ "$file_path" == /tmp/* ]] && exit 0

# Resolve to absolute path if relative
if [[ "$file_path" != /* ]]; then
  file_path="$CLAUDE_PROJECT_DIR/$file_path"
fi

# 1. Block .env files (allow .example templates)
basename_file=$(basename "$file_path")
if [[ "$basename_file" == .env || "$basename_file" == .env.* ]]; then
  # Allow .env.example, .env.sample, .env.template patterns
  if [[ "$basename_file" =~ \.(example|sample|template)$ ]]; then
    : # Allow these safe templates
  else
    log_blocked "$file_path" ".env file (protected)"
    echo "🚫 Access denied: .env files are protected" >&2
    exit 2
  fi
fi

# 2. Allow .claude/ config directory even if gitignored
[[ "$file_path" == */.claude/* ]] && exit 0

# 3. Block files in .gitignore (only if in a git repo)
if git -C "$CLAUDE_PROJECT_DIR" rev-parse --git-dir &>/dev/null; then
  if git -C "$CLAUDE_PROJECT_DIR" check-ignore -q "$file_path" 2>/dev/null; then
    log_blocked "$file_path" "gitignored"
    echo "🚫 Access denied: '$file_path' is in .gitignore" >&2
    exit 2
  fi
fi

exit 0
