#!/bin/bash
# test-broken-refs.sh — find references to non-existent commands, agents, skills, scripts
# in active .claude/ files (excluding _archive/).
#
# Usage: bash .claude/scripts/test-broken-refs.sh
# Exit: 0 = clean, 1 = broken refs found

set -uo pipefail

VIOLATIONS=0

echo "## Broken refs scan"
echo

# 1. Slash-command references — extract /om-* mentions, check command file exists
echo "### Command refs"
mapfile -t cmd_refs < <(
  grep -rhoE '/om-[a-z][a-z0-9-]*' \
    .claude/agents/ .claude/commands/ .claude/skills/ CLAUDE.md 2>/dev/null \
    | sort -u
)
for cmd in "${cmd_refs[@]}"; do
  name="${cmd#/}"
  if [[ ! -f ".claude/commands/${name}.md" ]]; then
    # Allow archived deprecation refs
    if grep -lE "/(${name}\b|deprecated)" .claude/_archive/commands/ 2>/dev/null > /dev/null; then
      continue
    fi
    echo "  BROKEN: $cmd referenced but .claude/commands/${name}.md not found"
    VIOLATIONS=$((VIOLATIONS+1))
  fi
done
[[ $VIOLATIONS -eq 0 ]] && echo "  ✓ all /om-* refs resolve"
echo

# 2. Agent references in commands — look for "subagent:" or "**`agent-name`**" patterns
echo "### Agent refs"
local_violations=0
mapfile -t agent_refs < <(
  grep -rhoE '\*\*`[a-z][a-z0-9-]*`\*\*' .claude/commands/ 2>/dev/null \
    | sed -E 's/^.+`([a-z][a-z0-9-]*)`.+$/\1/' \
    | sort -u
)
for agent in "${agent_refs[@]}"; do
  # Skip non-agent things (could be field names, etc.)
  if [[ -f ".claude/agents/${agent}.md" ]]; then
    continue
  fi
  # Allow common false positives (field names, command-fragments)
  case "$agent" in
    description|tool|tools|model|status|created|updated|tags|aliases|related|summary|inputs|outputs|date|type|file|files)
      continue
      ;;
  esac
  # Check if it's a known noun, not an agent ref
  if grep -lE "^- \\*\\*\\\`${agent}\\\`\\*\\* —" .claude/commands/*.md 2>/dev/null > /dev/null; then
    echo "  BROKEN: agent \`${agent}\` referenced but .claude/agents/${agent}.md not found"
    VIOLATIONS=$((VIOLATIONS+1))
    local_violations=$((local_violations+1))
  fi
done
[[ $local_violations -eq 0 ]] && echo "  ✓ all subagent refs resolve"
echo

# 3. Script references — look for .claude/scripts/<name>
echo "### Script refs"
local_violations=0
mapfile -t script_refs < <(
  grep -rhoE '\.claude/scripts/[a-zA-Z][a-zA-Z0-9._-]*' \
    .claude/agents/ .claude/commands/ .claude/skills/ CLAUDE.md 2>/dev/null \
    | sort -u
)
for script in "${script_refs[@]}"; do
  if [[ ! -f "$script" ]]; then
    echo "  BROKEN: $script referenced but file not found"
    VIOLATIONS=$((VIOLATIONS+1))
    local_violations=$((local_violations+1))
  fi
done
[[ $local_violations -eq 0 ]] && echo "  ✓ all script refs resolve"
echo

# 4. Skill references — look for `obsidian-*` skill names
echo "### Skill refs"
local_violations=0
mapfile -t skill_refs < <(
  grep -rhoE '\[\[\.claude/skills/[a-z][a-z0-9-]*' \
    .claude/agents/ .claude/commands/ CLAUDE.md 2>/dev/null \
    | sed -E 's/^.+\/([a-z][a-z0-9-]+).*$/\1/' \
    | sort -u
)
for skill in "${skill_refs[@]}"; do
  if [[ ! -d ".claude/skills/${skill}" ]]; then
    echo "  BROKEN: skill ${skill} referenced but .claude/skills/${skill}/ not found"
    VIOLATIONS=$((VIOLATIONS+1))
    local_violations=$((local_violations+1))
  fi
done
[[ $local_violations -eq 0 ]] && echo "  ✓ all skill refs resolve"
echo

# 5. Rule references
echo "### Rule refs"
local_violations=0
mapfile -t rule_refs < <(
  grep -rhoE '\.claude/rules/[a-z][a-z0-9-]*' \
    .claude/agents/ .claude/commands/ .claude/skills/ CLAUDE.md 2>/dev/null \
    | sort -u
)
for rule in "${rule_refs[@]}"; do
  rule_file="${rule}.md"
  if [[ ! -f "$rule_file" ]]; then
    echo "  BROKEN: $rule referenced but ${rule_file} not found"
    VIOLATIONS=$((VIOLATIONS+1))
    local_violations=$((local_violations+1))
  fi
done
[[ $local_violations -eq 0 ]] && echo "  ✓ all rule refs resolve"
echo

# Summary
echo "---"
if [[ $VIOLATIONS -eq 0 ]]; then
  echo "✓ No broken refs"
  exit 0
else
  echo "✗ Found $VIOLATIONS broken ref(s)"
  exit 1
fi
