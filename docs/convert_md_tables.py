"""
Convert GitHub-flavored pipe-tables in README.md to HTML <table> tags.

Why: Eclipse's built-in Markdown preview (Mylyn WikiText) does NOT understand
GFM pipe tables, so they collapse to a single paragraph of "|" characters.
HTML tables render correctly in both Eclipse AND GitHub.

Run from project root:
    python docs/convert_md_tables.py
"""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
README = ROOT / "README.md"

SEP_RE = re.compile(r"^\s*\|?\s*:?-{2,}:?\s*(\|\s*:?-{2,}:?\s*)+\|?\s*$")


def split_row(line: str):
    """Split a markdown table row into its cells, handling escaped \\| ."""
    line = line.strip()
    if line.startswith("|"):
        line = line[1:]
    if line.endswith("|"):
        line = line[:-1]
    # Escape \| temporarily so we don't split on escaped pipes
    placeholder = "\x00ESCPIPE\x00"
    safe = line.replace(r"\|", placeholder)
    return [c.strip().replace(placeholder, "|") for c in safe.split("|")]


CODE_RE   = re.compile(r"`([^`]+)`")
LINK_RE   = re.compile(r"\[([^\]]+)\]\(([^)]+)\)")
BOLD_RE   = re.compile(r"\*\*([^*]+)\*\*")
ITALIC_RE = re.compile(r"(?<!\*)\*([^*\s][^*]*?)\*(?!\*)")
BR_RE     = re.compile(r"  +\n")


def md_inline_to_html(text: str) -> str:
    """Convert the inline markdown that commonly appears inside table cells.

    Eclipse Mylyn does NOT re-parse markdown inside HTML block elements like
    <td>, so we have to inline these conversions ourselves."""
    text = CODE_RE.sub(r"<code>\1</code>", text)
    text = LINK_RE.sub(r'<a href="\2">\1</a>', text)
    text = BOLD_RE.sub(r"<strong>\1</strong>", text)
    text = ITALIC_RE.sub(r"<em>\1</em>", text)
    text = BR_RE.sub("<br>", text)
    return text


def md_to_table_html(header_cells, rows):
    """Render an HTML <table>. Inline markdown is converted to HTML so the
    cells render correctly in Eclipse Mylyn (which does not re-parse markdown
    inside HTML block elements) as well as on GitHub."""
    parts = ['<table>', '  <thead>', '    <tr>']
    for h in header_cells:
        parts.append(f'      <th>{md_inline_to_html(h)}</th>')
    parts.append('    </tr>')
    parts.append('  </thead>')
    parts.append('  <tbody>')
    for row in rows:
        parts.append('    <tr>')
        # Pad short rows so column count matches the header
        while len(row) < len(header_cells):
            row.append('')
        for cell in row:
            parts.append(f'      <td>{md_inline_to_html(cell)}</td>')
        parts.append('    </tr>')
    parts.append('  </tbody>')
    parts.append('</table>')
    return "\n".join(parts)


def convert(content: str) -> tuple[str, int]:
    lines = content.split("\n")
    output = []
    i = 0
    table_count = 0
    while i < len(lines):
        line = lines[i]
        # A markdown table needs at least: header | sep | (no data rows allowed too)
        if (
            line.strip().startswith("|")
            and line.strip().endswith("|")
            and i + 1 < len(lines)
            and SEP_RE.match(lines[i + 1])
        ):
            header_cells = split_row(line)
            j = i + 2
            rows = []
            while (
                j < len(lines)
                and lines[j].strip().startswith("|")
                and lines[j].strip().endswith("|")
                and not SEP_RE.match(lines[j])
            ):
                rows.append(split_row(lines[j]))
                j += 1
            output.append(md_to_table_html(header_cells, rows))
            table_count += 1
            i = j
        else:
            output.append(line)
            i += 1
    return "\n".join(output), table_count


def main():
    src = README.read_text(encoding="utf-8")
    new, count = convert(src)
    README.write_text(new, encoding="utf-8")
    print(f"Converted {count} markdown table(s) to HTML in {README}")


if __name__ == "__main__":
    main()
