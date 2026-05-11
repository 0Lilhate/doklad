#!/bin/bash
# claude-log-blocked.sh — append a blocked-event entry to .claude/logs/blocked-YYYY-MM.jsonl
# Per DP7: blocked-only logging.
#
# Usage: bash .claude/bin/claude-log-blocked.sh <tool> <target> <reason>
# Always exits 0 (logging failure must not break the calling hook).

set -uo pipefail

tool="${1:-unknown}"
target="${2:-unknown}"
reason="${3:-unspecified}"

project_dir="${CLAUDE_PROJECT_DIR:-.}"
log_dir="${project_dir}/.claude/logs"
log_file="${log_dir}/blocked-$(date +%Y-%m).jsonl"

# Try to create log dir. If it fails — silently exit 0.
mkdir -p "$log_dir" 2>/dev/null || exit 0

ts=$(date -Iseconds 2>/dev/null || date +%Y-%m-%dT%H:%M:%S%z)

# Build JSONL line. Escape via simple sed (good enough for tool/target/reason).
escape() {
  printf '%s' "$1" | sed 's/\\/\\\\/g; s/"/\\"/g' | tr -d '\n\r'
}

t=$(escape "$tool")
g=$(escape "$target")
r=$(escape "$reason")

printf '{"ts":"%s","tool":"%s","target":"%s","reason":"%s"}\n' \
  "$ts" "$t" "$g" "$r" >> "$log_file" 2>/dev/null || true

exit 0
