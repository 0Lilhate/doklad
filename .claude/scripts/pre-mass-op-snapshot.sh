#!/bin/bash
# pre-mass-op-snapshot.sh — create git tag snapshot before mass-op.
# Used by: vault-audit.py --apply, om-vault-upgrade, om-archive-stale, om-frontmatter-fix (>10 files).
# Required by: rules/backup.md
#
# Usage: bash .claude/scripts/pre-mass-op-snapshot.sh [tag-prefix]
#   default prefix: snapshot
#   examples:
#     bash .claude/scripts/pre-mass-op-snapshot.sh                  # snapshot/<ts>
#     bash .claude/scripts/pre-mass-op-snapshot.sh vault-audit      # snapshot/vault-audit-<ts>
#     bash .claude/scripts/pre-mass-op-snapshot.sh vault-upgrade    # snapshot/vault-upgrade-<ts>

set -euo pipefail

prefix="${1:-snapshot}"
ts=$(date +%Y%m%d-%H%M%S)

if [[ "$prefix" == "snapshot" ]]; then
  tag="snapshot/${ts}"
else
  tag="snapshot/${prefix}-${ts}"
fi

# Verify we're in a git repo
if ! git rev-parse --git-dir > /dev/null 2>&1; then
  echo "ERROR: not inside a git repository." >&2
  exit 1
fi

# Verify clean working tree
dirty=$(git status --porcelain)
if [[ -n "$dirty" ]]; then
  echo "ERROR: working tree is dirty. Commit or stash before mass-op." >&2
  echo "" >&2
  echo "Uncommitted changes:" >&2
  echo "$dirty" >&2
  echo "" >&2
  echo "Quick fix:" >&2
  echo "  git stash push -m 'pre-mass-op stash $ts'" >&2
  echo "  bash .claude/scripts/pre-mass-op-snapshot.sh $prefix" >&2
  echo "  # ... do the mass-op ..." >&2
  echo "  git stash pop" >&2
  exit 1
fi

# Check tag doesn't already exist (very unlikely with timestamp)
if git rev-parse "$tag" > /dev/null 2>&1; then
  echo "ERROR: tag $tag already exists. Wait 1 second and retry." >&2
  exit 1
fi

# Create annotated tag
git tag -a "$tag" -m "Pre mass-op snapshot at $(date -Iseconds)"

echo "✓ Created snapshot: $tag"
echo ""
echo "Restore with:"
echo "  git reset --hard $tag"
echo ""
echo "Recent snapshots:"
git tag -l 'snapshot/*' --sort=-creatordate | head -5
echo ""
echo "Old snapshot cleanup (manual, after review):"
echo "  git tag -d <snapshot/old-tag-name>"
