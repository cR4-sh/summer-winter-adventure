#!/usr/bin/env python3
"""Install the pinned mods and overrides into a Minecraft game directory."""

import argparse
import hashlib
import json
import shutil
import tempfile
from pathlib import Path
from urllib.request import Request, urlopen


ROOT = Path(__file__).resolve().parent


def digest(path):
    with path.open("rb") as stream:
        return hashlib.file_digest(stream, "sha256").hexdigest()


def install(instance):
    lock = json.loads((ROOT / "mods.lock.json").read_text(encoding="utf-8"))
    mods = instance / "mods"
    mods.mkdir(parents=True, exist_ok=True)
    expected = {entry["file"] for entry in lock["mods"]}
    extra = sorted(path.name for path in mods.glob("*.jar") if path.name not in expected)
    if extra:
        raise ValueError("Use a separate game directory. Other JARs found: " + ", ".join(extra))

    with tempfile.TemporaryDirectory(prefix="tech-", dir=instance) as folder:
        staging = Path(folder)
        downloaded = []
        for entry in lock["mods"]:
            name = entry["file"]
            target = mods / name
            if target.is_file() and digest(target) == entry["sha256"]:
                continue
            print(f"Downloading {name}", flush=True)
            partial = staging / name
            request = Request(entry["url"], headers={"User-Agent": "summer-winter-adventure-tech/1.0"})
            with urlopen(request, timeout=60) as response, partial.open("wb") as output:
                shutil.copyfileobj(response, output)
            if digest(partial) != entry["sha256"]:
                raise ValueError(f"SHA-256 mismatch: {name}")
            downloaded.append(partial)

        for partial in downloaded:
            partial.replace(mods / partial.name)

    shutil.copytree(ROOT / "overrides", instance, dirs_exist_ok=True)
    print(f"Installed {len(expected)} pinned mods and overrides in {instance}")
    print(f"Minecraft {lock['minecraft']} / NeoForge {lock['neoforge']} / Java {lock['java']}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("instance", type=Path, help="Minecraft game directory for the tech pack")
    args = parser.parse_args()
    try:
        install(args.instance.resolve())
    except (OSError, ValueError) as error:
        parser.exit(1, f"Installation failed: {error}\n")
