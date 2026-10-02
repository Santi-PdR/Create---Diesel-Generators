#!/usr/bin/env python3
"""Do not accept a clean launch with zero tests as a successful regression run."""
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
expected = sum(len(re.findall(r"@GameTest\(", p.read_text()))
               for p in (ROOT / "src/gameTest/java").rglob("*.java"))
log = Path(sys.argv[1]).read_text(errors="replace")
passed = re.findall(r"All (\d+) required tests passed", log)
if not expected or not passed or int(passed[-1]) != expected:
    print(f"Expected {expected} successful required GameTests; summaries: {passed}", file=sys.stderr)
    sys.exit(1)
print(f"Verified completion of all {expected} required Forge GameTests.")
