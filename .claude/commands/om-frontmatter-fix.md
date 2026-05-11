---
description: "Normalize YAML frontmatter across notes. Args: [path] (default: whole vault). Dry-run by default. Apply requires snapshot."
---

# om-frontmatter-fix

Normalize YAML frontmatter using `obsidian-frontmatter` skill. Resolves field aliases, deduplicates tags, validates enum values, adds missing required fields.

## Usage

```
/om-frontmatter-fix              # whole vault (excludes _archive, .claude, .obsidian, .git)
/om-frontmatter-fix work/active/ # specific path
```

## Workflow

1. **Determine scope** — path argument or whole vault. Apply default excludes.
2. **Walk files** — all `.md` in scope.
3. **For each file**:
   - Parse frontmatter
   - Apply rules from `obsidian-frontmatter` skill:
     - Resolve aliases (preserve original, add canonical)
     - Dedup tags
     - Convert string-tags to array
     - Validate `type` ∈ allowed_type_values
     - Validate `status` ∈ allowed_status_values
     - Add missing required fields per type (`null` placeholder)
     - Normalize date format to ISO
   - Collect violations + planned changes.
4. **Write dry-run report** to `thinking/frontmatter-fix-YYYY-MM-DD.md`:
   - Summary: N files scanned, M violations, K auto-fixable, L need user input
   - Per-file diff for first 20 files
   - Categories of issues (alias-resolution, dedup, missing-required, invalid-enum)
5. **Show summary to user**, ask:
   - **Apply all** auto-fixable
   - **Apply per-file** (interactive)
   - **Apply category** (e.g. only dedup tags)
   - **Cancel**
6. **If apply on > 10 files** — invoke `pre-mass-op-snapshot.sh` first.
7. **Write changes**, preserve original fields, add canonical.
8. **Re-verify** through `vault-audit.py` after apply.

## Output

```
Scope: vault root
Scanned: 47 .md files
Violations: 12

### Auto-fixable (8)
- 3 files: dedup tags
- 2 files: created → ISO format
- 3 files: alias `date_created` → `created`

### Need user input (4)
- work/active/Old Project.md: missing required `quarter`, `description`
- brain/Concept.md: invalid type "knowledge" (not in allowed list)
- ...

Apply all auto-fixable? [Y/n/per-file]
```

## Important

- **Always dry-run first.** No silent writes.
- **Mass-op > 10 files requires snapshot** (pre-mass-op-snapshot.sh).
- **Never remove existing fields** — only add canonical alongside aliases.
- **Never change content body**, только frontmatter.
- **Never auto-promote `status: raw` → `evergreen`**.

## References

- [[.claude/skills/obsidian-frontmatter|obsidian-frontmatter skill]]
- [[.claude/rules/frontmatter|frontmatter rules]]
- [[.claude/rules/backup|backup rules]]
- `vault-manifest.json` — `field_aliases`, `frontmatter_required`, allowed enums
