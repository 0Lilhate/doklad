#!/usr/bin/env python3
"""
vault-audit.py — Naming/structure audit for obsidian-mind vault.

Replaces .claude/skills/obsidian-organizer/scripts/obsidian_audit.py.

Per DP2 (decided 2026-04-30):
- TitleCase enforced for durable folders (work, org, perf, brain, Sources, templates, bases)
- kebab-case allowed in _archive, thinking, reference
- ISO-date format for temporal files (daily, 1-1, meetings)

Per DP3 + safety:
- Default --exclude includes _archive, .claude, .obsidian, .git, .idea, node_modules
- --apply requires --backup (creates git tag snapshot via pre-mass-op-snapshot.sh)
- Does NOT auto-rename. Reports issues; user does git mv manually.

Usage:
  vault-audit.py <vault-path>
  vault-audit.py <vault-path> --exclude _custom another-folder
  vault-audit.py <vault-path> --apply --backup     # report-only (no auto-rename anyway)
"""
import argparse
import re
import subprocess
import sys
from datetime import datetime
from pathlib import Path

DURABLE_FOLDERS = {"work", "org", "perf", "brain", "Sources", "templates", "bases"}
KEBAB_ALLOWED_FOLDERS = {"_archive", "thinking", "reference"}
TEMPORAL_FOLDERS = {"journal", "1-1", "meetings", "incidents"}

DATE_NAME_RE = re.compile(r"^\d{4}-\d{2}-\d{2}( .+)?$")
RESERVED = {
    "README.md", "CLAUDE.md", "MEMORY.md", "Home.md",
    "vault-manifest.json", "CHANGELOG.md", "LICENSE",
    ".gitignore", ".mcp.json",
}
DEFAULT_EXCLUDES = {
    ".obsidian", ".git", ".idea", "node_modules",
    "_archive", ".claude", "venv", ".venv", "__pycache__",
}

BAD_SUFFIXES = (" Final", " v2", " v3", " v4", " copy", " new", " (1)", " (2)", " (3)")


def is_titlecase_ok(stem: str) -> tuple[bool, str | None]:
    """
    Return (ok, reason_if_not).
    TitleCase: starts with uppercase letter (Latin or Cyrillic),
               only letters/digits/spaces/hyphens, no bad suffixes.
    """
    if DATE_NAME_RE.match(stem):
        return True, None

    if not stem:
        return False, "empty stem"

    if not re.match(r"^[A-ZА-ЯЁ]", stem):
        return False, "must start with uppercase letter (TitleCase)"

    if not re.match(r"^[A-Za-zА-Яа-яЁё0-9 \-_]+$", stem):
        return False, "contains invalid characters (allowed: letters, digits, space, hyphen, underscore)"

    if any(stem.endswith(s) for s in BAD_SUFFIXES):
        return False, f"deprecated suffix (one of: {', '.join(BAD_SUFFIXES).strip()})"

    if stem.startswith(" ") or stem.endswith(" "):
        return False, "leading/trailing whitespace"

    return True, None


def is_temporal_ok(stem: str) -> tuple[bool, str | None]:
    """ISO date prefix expected for temporal files."""
    if DATE_NAME_RE.match(stem):
        return True, None
    return False, "expected ISO date format YYYY-MM-DD or YYYY-MM-DD <Topic>"


def scan(vault: Path, excludes: set[str]) -> list[tuple[Path, str, str]]:
    """Return list of (path, kind, message)."""
    issues: list[tuple[Path, str, str]] = []

    for p in vault.rglob("*.md"):
        if any(part in excludes for part in p.parts):
            continue
        if p.name in RESERVED:
            continue

        rel = p.relative_to(vault)
        if not rel.parts:
            continue
        top = rel.parts[0]

        # Temporal folders
        if top in TEMPORAL_FOLDERS or any(t in rel.parts for t in TEMPORAL_FOLDERS):
            ok, reason = is_temporal_ok(p.stem)
            if not ok:
                issues.append((p, "temporal-violation", reason or "bad name"))
            continue

        # Durable folders
        if top in DURABLE_FOLDERS:
            ok, reason = is_titlecase_ok(p.stem)
            if not ok:
                issues.append((p, "naming-violation-durable", reason or "bad name"))
            continue

        # Kebab-allowed folders — only check bad suffixes
        if top in KEBAB_ALLOWED_FOLDERS:
            if any(p.stem.endswith(s) for s in BAD_SUFFIXES):
                issues.append((p, "deprecated-suffix", f"remove '{p.stem}' suffix"))

    return issues


def make_snapshot(vault: Path) -> str | None:
    """Create git tag snapshot via pre-mass-op-snapshot.sh."""
    script = vault / ".claude" / "scripts" / "pre-mass-op-snapshot.sh"
    if not script.exists():
        print("WARN: pre-mass-op-snapshot.sh not found; skipping snapshot.", file=sys.stderr)
        return None
    try:
        result = subprocess.run(
            ["bash", str(script), "vault-audit"],
            cwd=vault,
            capture_output=True,
            text=True,
            check=True,
        )
        for line in result.stdout.splitlines():
            print(line)
        return result.stdout.strip()
    except subprocess.CalledProcessError as e:
        print(f"ERROR: snapshot failed: {e.stderr}", file=sys.stderr)
        sys.exit(2)


def main() -> None:
    ap = argparse.ArgumentParser(
        description=__doc__,
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    ap.add_argument("vault", help="Path to vault root")
    ap.add_argument(
        "--exclude",
        nargs="*",
        default=[],
        help="Additional folder names to exclude (in addition to defaults)",
    )
    ap.add_argument(
        "--apply",
        action="store_true",
        help="(Reserved for future auto-fix; currently report-only). Requires --backup.",
    )
    ap.add_argument(
        "--backup",
        action="store_true",
        help="Create git tag snapshot before --apply (no-op without --apply)",
    )
    ap.add_argument(
        "--json",
        action="store_true",
        help="Output as JSON",
    )
    args = ap.parse_args()

    vault = Path(args.vault).expanduser().resolve()
    if not vault.exists():
        print(f"ERROR: vault not found: {vault}", file=sys.stderr)
        sys.exit(1)

    excludes = DEFAULT_EXCLUDES | set(args.exclude)

    if args.apply and not args.backup:
        print(
            "ERROR: --apply requires --backup (creates git tag snapshot first).",
            file=sys.stderr,
        )
        sys.exit(2)

    if args.apply and args.backup:
        make_snapshot(vault)

    issues = scan(vault, excludes)

    if args.json:
        import json as _json
        out = [
            {"path": str(p.relative_to(vault)), "kind": k, "message": m}
            for p, k, m in issues
        ]
        print(_json.dumps({"vault": str(vault), "issues": out, "count": len(out)}, ensure_ascii=False, indent=2))
        return

    if not issues:
        print(f"✓ OK: no naming/structure issues in {vault}")
        return

    print(f"Found {len(issues)} issue(s) in {vault}:\n")
    by_kind: dict[str, list[tuple[Path, str]]] = {}
    for p, kind, msg in issues:
        by_kind.setdefault(kind, []).append((p, msg))

    for kind, items in sorted(by_kind.items()):
        print(f"## {kind} ({len(items)})")
        for p, msg in items:
            rel = p.relative_to(vault)
            print(f"  {rel}: {msg}")
        print()

    print("Note: this script does NOT auto-rename.")
    print("Manual fix: review, then `git mv <old> <new>` after `pre-mass-op-snapshot.sh`.")


if __name__ == "__main__":
    main()
