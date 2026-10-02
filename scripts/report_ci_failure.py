#!/usr/bin/env python3
"""Expose Gradle/runtime diagnostics as Actions annotations on a failed check."""

import sys
from pathlib import Path

for name in sys.argv[1:]:
    path = Path(name)
    if not path.is_file():
        continue
    lines = path.read_text(encoding="utf-8", errors="replace").splitlines()[-160:]
    # Chunk to keep each annotation comfortably below GitHub's size limit.
    for start in range(0, len(lines), 40):
        message = "\n".join(lines[start:start + 40])
        message = message.replace("%", "%25").replace("\r", "%0D").replace("\n", "%0A")
        print(f"::error title={path.name} (tail {start // 40 + 1})::{message}")
