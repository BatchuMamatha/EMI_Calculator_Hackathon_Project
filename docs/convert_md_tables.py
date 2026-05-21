"""Convert GitHub-flavoured pipe tables in README.md to HTML <table> tags.

Eclipse Mylyn's Markdown preview does not render GFM pipe tables (they
collapse into a single inline paragraph). HTML tables render correctly in
BOTH Eclipse AND GitHub.

Run from project root:
    python docs/convert_md_tables.py
"""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
README = ROOT / "README.md"

SEP_RE = re.compile(r"^\s*\|?\s*:?-{2,}:?\s*(\|\s*:?-{2,}:?\s*)+\|?\s*$")

CODE_RE   = re.compile(r"`([^`]+)`")
LINK_RE   = re.compile(r"\[([^\]]+)\]\(([^)]+)\)")
BOLD_RE   = re.compile(r"\*\*([^*]+)\*\*")
ITALIC_RE = re.compile(r"(?<!\*)\*([^*\s][^*]*?)\*(?!\*)")


def split_row(line: str):
    line = line.strip()
    if line.startswith("|"):
        line = line[1:]
    if line.endswith("|"):
        line = line[:-1]
    placeholder = "\x00ESCPIPE\x00"
    safe = line.replace(r"\|", placeholder)
    return [c.strip().replace(placeholder, "|") for c in safe.split("|")]


def inline_md_to_html(text: str) -> str:
    text = CODE_RE.sub(r"<code>\1</code>", text)
    text = LINK_RE.sub(r'<a href="\2">\1</a>', text)
    text = BOLD_RE.sub(r"<strong>\1</strong>", text)
    text = ITALIC_RE.sub(r"<em>\1</em>", text)
    return text


def to_table_html(headers, rows):
    parts = ['<table>', '  <thead>', '    <tr>']
    for h in headers:
        parts.append(f'      <th>{inline_md_to_html(h)}</th>')
    parts.append('    </tr>')
    parts.append('  </thead>')
    parts.append('  <tbody>')
    for row in rows:
        while len(row) < len(headers):
            row.append('')
        parts.append('    <tr>')
        for cell in row:
            parts.append(f'      <td>{inline_md_to_html(cell)}</td>')
        parts.append('    </tr>')
    parts.append('  </tbody>')
    parts.append('</table>')
    return "\n".join(parts)


def in_code_fence(line: str, state: dict) -> bool:
    """Track whether we are inside a ```...``` code block (don't touch its content)."""
    if line.strip().startswith("```"):
        state["in"] = not state["in"]
        return True
    return state["in"]


def convert(content: str):
    lines = content.split("\n")
    output = []
    fence = {"in": False}
    i = 0
    count = 0
    while i < len(lines):
        line = lines[i]
        # Skip table detection inside fenced code blocks
        if in_code_fence(line, fence):
            output.append(line)
            i += 1
            continue
        if (line.strip().startswith("|")
                and line.strip().endswith("|")
                and i + 1 < len(lines)
                and SEP_RE.match(lines[i + 1])):
            headers = split_row(line)
            j = i + 2
            rows = []
            while (j < len(lines)
                   and lines[j].strip().startswith("|")
                   and lines[j].strip().endswith("|")
                   and not SEP_RE.match(lines[j])):
                rows.append(split_row(lines[j]))
                j += 1
            output.append(to_table_html(headers, rows))
            count += 1
            i = j
        else:
            output.append(line)
            i += 1
    return "\n".join(output), count


def main():
    src = README.read_text(encoding="utf-8")
    new, count = convert(src)
    README.write_text(new, encoding="utf-8")
    print(f"Converted {count} markdown table(s) to HTML in {README}")


if __name__ == "__main__":
    main()
