#!/usr/bin/env python3
"""Build the Convoy Force training addon with the pinned NeoForge client libraries."""

import argparse
import json
import os
import subprocess
import tempfile
from pathlib import Path
from zipfile import ZIP_DEFLATED, ZipFile


ROOT = Path(__file__).resolve().parent
LOCK = ROOT.parents[1] / "mods.lock.json"


def build(libraries, extra_libraries, mods, javac, output):
    lock = json.loads(LOCK.read_text(encoding="utf-8"))
    forge = libraries / "net/neoforged/neoforge" / lock["neoforge"]
    required = [forge / f"neoforge-{lock['neoforge']}-{component}.jar" for component in ("client", "universal")]
    required.extend(mods / lock["additions"][name]["file"] for name in ("starwcraft", "epic-fight", "convoy-space"))
    for path in required:
        if not path.is_file():
            raise FileNotFoundError(path)

    source = ROOT / "src" / "main"
    dependencies = [jar for directory in (libraries, *extra_libraries) for jar in sorted(directory.rglob("*.jar"))]
    classpath = os.pathsep.join(str(jar) for jar in [*required, *dependencies])
    output.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(dir=output.parent) as staging:
        classes = Path(staging) / "classes"
        subprocess.run([javac, "--release", str(lock["java"]), "-proc:none", "-encoding", "UTF-8", "-cp", classpath,
                        "-d", str(classes), *map(str, sorted((source / "java").rglob("*.java")))], check=True)
        artifact = Path(staging) / output.name
        with ZipFile(artifact, "w", ZIP_DEFLATED) as archive:
            for directory in (classes, source / "resources"):
                for file in sorted(directory.rglob("*")):
                    if file.is_file():
                        archive.write(file, file.relative_to(directory))
        artifact.replace(output)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--libraries", type=Path, required=True)
    parser.add_argument("--extra-libraries", type=Path, action="append", default=[])
    parser.add_argument("--mods", type=Path, required=True)
    parser.add_argument("--javac", default="javac")
    parser.add_argument("--output", type=Path, default=ROOT / "dist" / "swa-force-training-1.1.0.jar")
    args = parser.parse_args()
    build(args.libraries, args.extra_libraries, args.mods, args.javac, args.output)
