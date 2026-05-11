---
description: "Capture URL → defuddle → Sources/<sub>/<title>.md as type:source, status:raw with populated frontmatter."
---

# om-source-clip

Single-URL capture as raw source-note. Lighter than Web Clipper (no browser extension needed).

## Usage

```
/om-source-clip <url>
```

## Workflow

1. **Validate URL** — http(s) only. Reject local paths.
2. **Determine subfolder**:
   - GitHub repo / issue / PR → `Sources/GitHub/`
   - PDF, papers, journals → `Sources/Papers/`
   - YouTube / Vimeo / talk → `Sources/Videos/`
   - Documentation domain (docs.*, *.io/docs, help.*, kb.*) → `Sources/Docs/`
   - Default → `Sources/Web/`
   Ask user to confirm if ambiguous.
3. **Defuddle** — `defuddle parse <url> --md` to get clean markdown.
4. **Extract metadata**: title, author, published date, site (via `defuddle -p title/description/domain`).
5. **Generate filename**: TitleCase from title, sanitize for filesystem (remove `:`, `?`, `|`, `<`, `>`).
6. **qmd-check duplicate**: search by title and URL; warn if hit ≥ 0.85.
7. **Apply template** `templates/Source.md`:
   - `{{title}}` → extracted title
   - `{{url}}` → original URL
   - `author`, `published`, `site` → extracted
   - `created`, `captured` → today
   - `status: raw`, `type: source`
8. **Body**: defuddle-cleaned markdown. Don't truncate; user may want full source.
9. **Write** to `Sources/<sub>/<Title>.md`.
10. **Suggest follow-up**:
    - "Run `/om-process-inbox` later to normalize into structured note"
    - 2-3 related notes from qmd ("might be related to:")

## Output

```
Captured: Sources/Docs/OAuth Spec RFC 6749.md
Source: https://datatracker.ietf.org/doc/html/rfc6749
Title: The OAuth 2.0 Authorization Framework
Author: D. Hardt
Published: 2012-10
Word count: ~24000
Related (from qmd):
  - [[Auth Refactor]] (0.78)
  - [[OAuth Token Refresh]] (0.81)

Next: review, then run /om-process-inbox to extract concepts.
```

## Important

- **Always defuddle**, не raw HTML — экономит токены при последующей обработке.
- **status: raw** обязательно. **Никогда не promote напрямую в `brain/` без `note-normalizer`.**
- If defuddle fails (404, dynamic page) — fallback to WebFetch + manual cleanup, but warn user.
- For Confluence pages — рекомендуй `/om-confluence-import` (Block J) если внутренняя страница.

## References

- [[.claude/skills/defuddle|defuddle skill]]
- [[templates/Source|Source.md]]
- [[.claude/skills/obsidian-atomic|obsidian-atomic — lifecycle]]
