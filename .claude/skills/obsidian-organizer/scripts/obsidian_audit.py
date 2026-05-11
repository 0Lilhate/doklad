#!/usr/bin/env python3
# ============================================================================
# DEPRECATED 2026-04-30
# ============================================================================
# Этот скрипт больше не должен использоваться.
#
# Причины:
# - Принудительно ребейсит все .md в kebab-case без exclude для
#   bases/, templates/, work/, org/, perf/, brain/ — ломает wikilinks
#   к TitleCase-нотам этого vault'а.
# - Не делает backup перед --apply.
# - Не имеет per-file confirmation.
#
# Замена: .claude/scripts/vault-audit.py
#   - TitleCase-aware (per DP2)
#   - --exclude по умолчанию правильный
#   - --apply требует --backup (создаёт git tag snapshot)
#   - Не делает auto-rename, только reports
#
# Файл оставлен как reference. Запуск с --apply ВЫЗОВЕТ exit 1.
# ============================================================================
import argparse
import re
import sys
from pathlib import Path

print(
    "DEPRECATED: see .claude/scripts/vault-audit.py instead.\n"
    "This script is kept for reference only.",
    file=sys.stderr,
)

ALLOWED_EXT = {".md"}
DATE_NAME = re.compile(r"^\d{4}-\d{2}-\d{2}(-[a-z0-9-]+)?$")
RESERVED = {"README.md", "MEMORY.md", "SOUL.md", "USER.md", "AGENTS.md", "HEARTBEAT.md"}
IGNORE_DIRS = {".obsidian"}


def kebab(s: str) -> str:
    s = s.strip().lower().replace("_", "-").replace(" ", "-")
    s = re.sub(r"[^a-z0-9-]", "", s)
    s = re.sub(r"-+", "-", s).strip("-")
    return s or "untitled"


def expected_name(path: Path) -> str:
    stem = path.stem
    # Allow ISO date journal format as-is if valid
    if DATE_NAME.match(stem):
        return stem + path.suffix
    return kebab(stem) + path.suffix


def scan(vault: Path):
    issues = []
    for p in vault.rglob("*"):
        if p.is_dir():
            continue
        if any(d in p.parts for d in IGNORE_DIRS):
            continue
        if p.name in RESERVED:
            continue
        if p.suffix.lower() not in ALLOWED_EXT:
            continue
        exp = expected_name(p)
        if p.name != exp:
            issues.append((p, "rename", exp))
    return issues


def main():
    ap = argparse.ArgumentParser(description="Audit Obsidian vault naming consistency [DEPRECATED — use .claude/scripts/vault-audit.py]")
    ap.add_argument("vault", help="Path to vault root")
    ap.add_argument("--apply", action="store_true", help="Apply safe renames [BLOCKED in deprecated mode]")
    args = ap.parse_args()

    if args.apply:
        print(
            "ERROR: --apply is BLOCKED in deprecated obsidian_audit.py.\n"
            "Use .claude/scripts/vault-audit.py instead (TitleCase-aware, requires --backup).",
            file=sys.stderr,
        )
        sys.exit(1)

    vault = Path(args.vault).expanduser().resolve()
    if not vault.exists():
        raise SystemExit(f"Vault not found: {vault}")

    issues = scan(vault)
    if not issues:
        print("OK: no naming issues found")
        return

    print(f"Found {len(issues)} issue(s)")
    for p, kind, target in issues:
        rel = p.relative_to(vault)
        print(f"- {kind}: {rel} -> {target}")

    if not args.apply:
        print("\nDry-run only. Re-run with --apply to rename files.")
        return

    for p, kind, target in issues:
        if kind != "rename":
            continue
        new_path = p.with_name(target)
        if new_path.exists():
            print(f"skip (exists): {new_path}")
            continue
        p.rename(new_path)
        print(f"renamed: {p.name} -> {new_path.name}")


if __name__ == "__main__":
    main()
