#!/usr/bin/env python3
"""Build the Convoy research addon against pinned NeoForge, Thaumaturge and Epic Fight."""

import argparse
import hashlib
import json
import os
import subprocess
import tempfile
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parent
ARTIFACT = ROOT / "dist" / "swa-research-1.0.0.jar"


def build(libraries, mods, javac):
    lock = json.loads((ROOT.parents[1] / "mods.lock.json").read_text(encoding="utf-8"))
    forge_version = lock["neoforge"]
    forge = libraries / "net/neoforged/neoforge" / forge_version
    required = [forge / f"neoforge-{forge_version}-server.jar",
                forge / f"neoforge-{forge_version}-universal.jar"]
    for path in required:
        if not path.is_file():
            raise FileNotFoundError(path)
    for name in ("thaumaturge", "epic-fight"):
        entry = lock["additions"][name]
        path = mods / entry["file"]
        with path.open("rb") as stream:
            if hashlib.file_digest(stream, "sha256").hexdigest() != entry["sha256"]:
                raise ValueError(f"Unexpected pinned jar: {path}")
        required.append(path)

    source = ROOT / "src" / "main"
    ARTIFACT.parent.mkdir(parents=True, exist_ok=True)
    classpath = os.pathsep.join(str(jar) for jar in [*required, *sorted(libraries.rglob("*.jar"))])
    with tempfile.TemporaryDirectory(dir=ARTIFACT.parent) as folder:
        temporary = Path(folder)
        classes = temporary / "classes"
        subprocess.run([javac, "--release", str(lock["java"]), "-proc:none", "-encoding", "UTF-8",
                        "-cp", classpath, "-d", str(classes),
                        *map(str, sorted((source / "java").rglob("*.java")))], check=True)
        stage = temporary / ARTIFACT.name
        with zipfile.ZipFile(stage, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=9) as jar:
            for directory in (classes, source / "resources"):
                for file in sorted(directory.rglob("*")):
                    if file.is_file():
                        entry = zipfile.ZipInfo(file.relative_to(directory).as_posix())
                        entry.external_attr = 0o100644 << 16
                        jar.writestr(entry, file.read_bytes(), compress_type=zipfile.ZIP_DEFLATED, compresslevel=9)
        stage.replace(ARTIFACT)
    print(ARTIFACT)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("libraries", type=Path, help="libraries directory from the pinned NeoForge server")
    parser.add_argument("mods", type=Path, help="directory with the pinned Thaumaturge and Epic Fight jars")
    parser.add_argument("--javac", default="javac", help="path to the Java 21 compiler")
    args = parser.parse_args()
    build(args.libraries.resolve(), args.mods.resolve(), args.javac)
