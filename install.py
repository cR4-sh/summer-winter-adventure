#!/usr/bin/env python3
"""Install the pinned mods, resource packs and overrides into a Minecraft game directory."""

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


def migrate_convoy_keys(options):
    if not options.is_file():
        return
    defaults = {
        b"key_key.starwarsmod.squad_role:key.keyboard.k": b"key.keyboard.unknown",
        b"key_key.starwarsmod.saga:key.keyboard.m": b"key.keyboard.unknown",
        b"key_key.starwarsmod.saber_guard:key.keyboard.g": b"key.keyboard.unknown",
        b"key_key.starwarsmod.dodge:key.keyboard.left.alt": b"key.keyboard.unknown",
        b"key_key.starwarsmod.cycle_force:key.keyboard.r": b"key.keyboard.left.bracket",
        b"key_key.starwarsmod.open_questbook:key.keyboard.j": b"key.keyboard.unknown",
        b"key_key.ftbquests.quests:key.keyboard.unknown": b"key.keyboard.j",
        b"key_key.encounterdirector.open_gui:key.keyboard.k": b"key.keyboard.unknown",
        b"key_key.encounterdirector.toggle_hud:key.keyboard.f8": b"key.keyboard.unknown",
    }
    original = options.read_bytes()
    lines = []
    for line in original.splitlines(keepends=True):
        value = line.rstrip(b"\r\n")
        if value in defaults:
            line = value.split(b":", 1)[0] + b":" + defaults[value] + line[len(value):]
        lines.append(line)
    updated = b"".join(lines)
    if updated != original:
        options.write_bytes(updated)


def enable_resource_packs(options, names):
    original = options.read_bytes() if options.is_file() else b""
    lines = original.splitlines(keepends=True)
    prefix = b"resourcePacks:"
    index = next((i for i, line in enumerate(lines) if line.startswith(prefix)), len(lines))
    active = json.loads(lines[index][len(prefix):]) if index < len(lines) else []
    missing = [name for name in names if name not in active]
    if not missing:
        return

    # A later pack has higher priority. Keep the player's optional packs above the default.
    position = next((i for i, name in enumerate(active) if name.startswith("file/")), len(active))
    active[position:position] = missing
    newline = b"\r\n" if b"\r\n" in original else b"\n"
    if index == len(lines) and lines and not lines[-1].endswith(b"\n"):
        lines[-1] += newline
    lines[index:index + 1] = [prefix + json.dumps(active, separators=(",", ":")).encode("utf-8") + newline]
    options.write_bytes(b"".join(lines))


def install(instance, profile="tech"):
    lock = json.loads((ROOT / "mods.lock.json").read_text(encoding="utf-8"))
    selected = lock["profiles"][profile]
    entries = lock["mods"] + [lock["additions"][name] for name in selected["mods"]]
    packs = [lock["resource_packs"][name] for name in selected.get("resource_packs", ())]
    mods = instance / "mods"
    mods.mkdir(parents=True, exist_ok=True)
    expected = {entry["file"] for entry in entries}
    profile_file = instance / ".swa-profile"
    if profile_file.is_file() and profile_file.read_text(encoding="utf-8").strip() != profile:
        raise ValueError("Install each profile into its own game directory.")
    replaces = {name: sha256 for entry in entries for name, sha256 in entry.get("replaces", {}).items()}
    retired = []
    extra = []
    for path in mods.glob("*.jar"):
        if path.name in expected:
            continue
        if path.name in replaces and digest(path) == replaces[path.name]:
            retired.append(path)
        else:
            extra.append(path.name)
    if extra:
        raise ValueError("Use a separate game directory. Other JARs found: " + ", ".join(sorted(extra)))

    with tempfile.TemporaryDirectory(prefix="tech-", dir=instance) as folder:
        staging = Path(folder)
        downloaded = []
        targets = [(entry, mods / entry["file"]) for entry in entries]
        targets += [(entry, instance / "resourcepacks" / entry["file"]) for entry in packs]
        for entry, target in targets:
            name = entry["file"]
            if target.is_file() and digest(target) == entry["sha256"]:
                continue
            partial = staging / name
            if "path" in entry:
                shutil.copyfile(ROOT / entry["path"], partial)
            else:
                print(f"Downloading {name}", flush=True)
                request = Request(entry["url"], headers={"User-Agent": "summer-winter-adventure-tech/1.0"})
                with urlopen(request, timeout=60) as response, partial.open("wb") as output:
                    shutil.copyfileobj(response, output)
            if digest(partial) != entry["sha256"]:
                raise ValueError(f"SHA-256 mismatch: {name}")
            downloaded.append((partial, target))

        for partial, target in downloaded:
            target.parent.mkdir(parents=True, exist_ok=True)
            partial.replace(target)

    shutil.copytree(ROOT / "overrides", instance, dirs_exist_ok=True)
    for overlay in selected["overrides"]:
        shutil.copytree(ROOT / "profiles" / overlay, instance, dirs_exist_ok=True)
    if profile == "convoy" and any(old_mod.name == "swa-space-1.0.0.jar" for old_mod in retired):
        migrate_convoy_keys(instance / "options.txt")
    if packs:
        enable_resource_packs(instance / "options.txt", ["file/" + entry["file"] for entry in packs])
    for old_mod in retired:
        old_mod.unlink()
    profile_file.write_text(profile + "\n", encoding="utf-8")
    print(f"Installed profile {profile}: {len(expected)} pinned mods and overrides in {instance}")
    print(f"Minecraft {lock['minecraft']} / NeoForge {lock['neoforge']} / Java {lock['java']}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("instance", type=Path, help="Minecraft game directory for the tech pack")
    profiles = json.loads((ROOT / "mods.lock.json").read_text(encoding="utf-8"))["profiles"]
    parser.add_argument("--profile", choices=profiles, default="tech")
    args = parser.parse_args()
    try:
        install(args.instance.resolve(), args.profile)
    except (OSError, ValueError) as error:
        parser.exit(1, f"Installation failed: {error}\n")
