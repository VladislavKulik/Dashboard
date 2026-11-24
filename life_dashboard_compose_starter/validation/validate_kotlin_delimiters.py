from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PAIRS = {')': '(', ']': '[', '}': '{'}


def check(path: Path) -> None:
    source = path.read_text(encoding='utf-8')
    stack = []
    i = 0
    line = 1
    while i < len(source):
        ch = source[i]
        nxt = source[i + 1] if i + 1 < len(source) else ''
        if ch == '\n':
            line += 1
            i += 1
            continue
        if ch == '/' and nxt == '/':
            i += 2
            while i < len(source) and source[i] != '\n':
                i += 1
            continue
        if ch == '/' and nxt == '*':
            i += 2
            depth = 1
            while i < len(source) and depth:
                if source.startswith('/*', i):
                    depth += 1
                    i += 2
                elif source.startswith('*/', i):
                    depth -= 1
                    i += 2
                else:
                    if source[i] == '\n':
                        line += 1
                    i += 1
            assert depth == 0, f'{path}: unclosed block comment'
            continue
        if source.startswith('"""', i):
            i += 3
            end = source.find('"""', i)
            assert end >= 0, f'{path}: unclosed raw string'
            line += source[i:end].count('\n')
            i = end + 3
            continue
        if ch in ('"', "'"):
            quote = ch
            i += 1
            while i < len(source):
                if source[i] == '\\':
                    i += 2
                    continue
                if source[i] == quote:
                    i += 1
                    break
                if source[i] == '\n':
                    line += 1
                i += 1
            else:
                raise AssertionError(f'{path}: unclosed quoted literal')
            continue
        if ch in '([{':
            stack.append((ch, line))
        elif ch in ')]}':
            assert stack and stack[-1][0] == PAIRS[ch], f'{path}:{line}: unmatched {ch}'
            stack.pop()
        i += 1
    assert not stack, f'{path}: unclosed delimiters {stack[-5:]}'

files = list(ROOT.rglob('*.kt'))
for file in files:
    check(file)
print(f'Kotlin delimiter scan: {len(files)} files passed')
