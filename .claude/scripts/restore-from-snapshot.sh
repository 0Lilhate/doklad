#!/bin/bash
# restore-from-snapshot.sh — UX wrapper for git tag snapshots created by pre-mass-op-snapshot.sh.
# Lists recent snapshots, shows diff, restores after confirmation.
#
# Usage:
#   bash .claude/scripts/restore-from-snapshot.sh             # interactive: list + pick
#   bash .claude/scripts/restore-from-snapshot.sh <tag>       # specific snapshot

set -uo pipefail

if ! git rev-parse --git-dir > /dev/null 2>&1; then
  echo "ERROR: not inside a git repository." >&2
  exit 1
fi

# Function: list snapshots
list_snapshots() {
  echo "Recent snapshots (most recent first):"
  git tag -l 'snapshot/*' --sort=-creatordate \
    --format='%(refname:short)%09%(creatordate:short)%09%(subject)' 2>/dev/null \
    | head -20 \
    | awk -F'\t' '{ printf "  %-50s %s  %s\n", $1, $2, $3 }'
}

# Function: show diff
show_diff() {
  local tag="$1"
  echo ""
  echo "Diff: HEAD vs $tag"
  echo "==================="
  git diff --stat "$tag" HEAD 2>/dev/null | tail -30
  echo ""
  echo "Files changed since $tag:"
  git diff --name-status "$tag" HEAD 2>/dev/null | head -30
}

# Function: confirm and restore
restore() {
  local tag="$1"

  show_diff "$tag"
  echo ""
  echo "WARNING: this will reset HEAD to $tag, discarding all changes after that point."
  echo "Uncommitted changes will be lost. Stash or commit them first if needed."
  echo ""
  echo "Current uncommitted changes:"
  if [[ -z "$(git status --porcelain)" ]]; then
    echo "  (none — working tree clean)"
  else
    git status --short | head -20
  fi
  echo ""
  read -p "Type 'restore' to confirm reset, anything else to cancel: " confirm
  if [[ "$confirm" != "restore" ]]; then
    echo "Cancelled."
    exit 0
  fi

  echo "Creating safety backup tag before reset..."
  ts=$(date +%Y%m%d-%H%M%S)
  git tag -a "snapshot/before-restore-${ts}" -m "Auto-backup before restore from $tag"
  echo "  Backup tag: snapshot/before-restore-${ts}"
  echo ""

  git reset --hard "$tag"
  echo ""
  echo "✓ Restored to $tag"
  echo ""
  echo "If this was a mistake — restore the auto-backup:"
  echo "  git reset --hard snapshot/before-restore-${ts}"
}

# Main
if [[ $# -eq 1 ]]; then
  tag="$1"
  if ! git rev-parse "$tag" > /dev/null 2>&1; then
    echo "ERROR: tag '$tag' not found." >&2
    echo ""
    list_snapshots
    exit 1
  fi
  restore "$tag"
else
  list_snapshots
  echo ""
  echo "To restore specific snapshot:"
  echo "  bash .claude/scripts/restore-from-snapshot.sh <tag-name>"
  echo ""
  echo "To delete old snapshots manually:"
  echo "  git tag -d <tag-name>"
fi
