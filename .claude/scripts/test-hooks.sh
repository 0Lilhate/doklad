#!/bin/bash
# test-hooks.sh — verify safety hooks block what they should.
# Simulates Claude Code's PreToolUse hook input via stdin pipe.
#
# Usage: bash .claude/scripts/test-hooks.sh
# Exit: 0 = all hooks correct, 1 = some hook misbehaves

set -uo pipefail

if [[ -z "${CLAUDE_PROJECT_DIR:-}" ]]; then
  export CLAUDE_PROJECT_DIR="$(pwd)"
fi

PASS=0
FAIL=0

bash_hook=".claude/bin/claude-block-sensitive-bash.sh"
files_hook=".claude/bin/claude-block-sensitive-files.sh"

if [[ ! -f "$bash_hook" ]]; then
  echo "ERROR: $bash_hook not found"; exit 1
fi
if [[ ! -f "$files_hook" ]]; then
  echo "ERROR: $files_hook not found"; exit 1
fi

# Helper: run hook with input, expect exit code
test_hook() {
  local name="$1"
  local hook="$2"
  local input="$3"
  local expected_exit="$4"
  local description="$5"

  actual_exit=0
  echo "$input" | bash "$hook" >/dev/null 2>&1 || actual_exit=$?

  if [[ "$actual_exit" == "$expected_exit" ]]; then
    echo "  ✓ $name: $description (exit $actual_exit)"
    PASS=$((PASS+1))
  else
    echo "  ✗ $name: $description — expected exit $expected_exit, got $actual_exit"
    FAIL=$((FAIL+1))
  fi
}

echo "## Hook tests"
echo

echo "### Bash hook (claude-block-sensitive-bash.sh)"
test_hook "bash-1" "$bash_hook" \
  '{"tool_input":{"command":"git status"}}' \
  "0" "git status — should pass"

test_hook "bash-2" "$bash_hook" \
  '{"tool_input":{"command":"cat .env"}}' \
  "2" "cat .env — should be blocked"

test_hook "bash-3" "$bash_hook" \
  '{"tool_input":{"command":"cat .env.example"}}' \
  "0" "cat .env.example — whitelisted, should pass"

test_hook "bash-4" "$bash_hook" \
  '{"tool_input":{"command":"rm -rf /tmp/test"}}' \
  "2" "rm -rf — should be blocked"

test_hook "bash-5" "$bash_hook" \
  '{"tool_input":{"command":"cp secrets.json /tmp/"}}' \
  "2" "cp to /tmp — should be blocked"

echo

echo "### Files hook (claude-block-sensitive-files.sh)"
test_hook "files-1" "$files_hook" \
  '{"tool_name":"Read","tool_input":{"file_path":"CLAUDE.md"}}' \
  "0" "Read CLAUDE.md — should pass"

test_hook "files-2" "$files_hook" \
  '{"tool_name":"Read","tool_input":{"file_path":".env"}}' \
  "2" "Read .env — should be blocked"

test_hook "files-3" "$files_hook" \
  '{"tool_name":"Read","tool_input":{"file_path":".env.example"}}' \
  "0" "Read .env.example — whitelisted, should pass"

test_hook "files-4" "$files_hook" \
  '{"tool_name":"Read","tool_input":{"file_path":"vault-manifest.json"}}' \
  "0" "Read vault-manifest.json — should pass"

# .idea is gitignored if .gitignore exists
if [[ -f ".gitignore" ]] && grep -q "^\.idea/" .gitignore 2>/dev/null; then
  test_hook "files-5" "$files_hook" \
    '{"tool_name":"Read","tool_input":{"file_path":".idea/workspace.xml"}}' \
    "2" "Read .idea/* — gitignored, should be blocked (if file exists)"
fi

echo
echo "---"
echo "Pass: $PASS"
echo "Fail: $FAIL"
if [[ $FAIL -gt 0 ]]; then
  exit 1
fi
exit 0
