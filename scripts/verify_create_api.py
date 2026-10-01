#!/usr/bin/env python3
"""Check the source API against the exact Create/Flywheel jars in libs/.

This offline check complements (and does not replace) Java compilation and
Forge runtime tests. It needs only Python's standard library.
"""

import re
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
API_PACKAGES = ("com.simibubi.create.", "com.jozufozu.flywheel.", "com.tterrag.registrate.")
INCOMPATIBLE_PACKAGES = ("net.createmod.", "dev.engine_room.flywheel.", "net.neoforged.")


def find_class(name, classes):
    """Resolve Java source notation, including nested classes, to a jar entry."""
    parts = name.split(".")
    for split in range(len(parts), 0, -1):
        candidate = ".".join(parts[:split])
        if split < len(parts):
            candidate += "$" + "$".join(parts[split:])
        if candidate in classes:
            return candidate
    return None


def main():
    jars = [
        ROOT / "libs/create-0.5.1.j.jar",
        ROOT / "libs/flywheel-forge-0.6.11-13.jar",
        ROOT / "libs/Registrate-MC1.20-1.3.3.jar",
    ]
    classes = set()
    errors = []
    for jar in jars:
        with zipfile.ZipFile(jar) as archive:
            classes.update(
                entry[:-6].replace("/", ".")
                for entry in archive.namelist()
                if entry.endswith(".class")
            )
    checked = 0
    for source in sorted((ROOT / "src/main/java").rglob("*.java")):
        text = source.read_text(encoding="utf-8")
        for match in re.finditer(r"^import\s+(static\s+)?([^;]+);", text, re.MULTILINE):
            is_static, name = match.groups()
            location = f"{source.relative_to(ROOT)}:{text.count(chr(10), 0, match.start()) + 1}"
            if name.startswith(INCOMPATIBLE_PACKAGES):
                errors.append(f"{location}: incompatible API import {name}")
            if not name.startswith(API_PACKAGES):
                continue
            if is_static:
                name = name.rsplit(".", 1)[0]
            if name.endswith(".*"):
                if not any(c.startswith(name[:-1]) for c in classes):
                    errors.append(f"{location}: package not present in target jars: {name}")
            elif find_class(name, classes) is None:
                errors.append(f"{location}: class not present in target jars: {name}")
            checked += 1

    # Create 0.5.1.j owns its Ponder registry; standalone Ponder/Catnip belongs
    # to the newer API and must not mask incompatible imports at compile time.
    build = (ROOT / "build.gradle").read_text(encoding="utf-8")
    if "net.createmod.ponder" in build or "dev.engine_room.flywheel" in build:
        errors.append("build.gradle: dependency on the newer Create/Ponder/Flywheel API")
    if errors:
        print("\n".join(errors), file=sys.stderr)
        return 1
    print(f"Verified {checked} imports against Create 0.5.1.j / Flywheel 0.6.11-13 / Registrate 1.3.3.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
