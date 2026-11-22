#!/usr/bin/env python3
"""Build the Convoy addon using the jars of the pinned NeoForge server."""

import argparse
import csv
import json
import math
import os
import runpy
import struct
import subprocess
import tempfile
import zipfile
import zlib
from pathlib import Path


ROOT = Path(__file__).resolve().parent
PACK = ROOT.parents[1]
ARTIFACT = ROOT / "dist" / "swa-space-1.2.2.jar"


def png_chunk(kind, data):
    return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data))


def planet_chart(world):
    """Bake a seamless equirectangular surface chart for Unbound's globe renderer."""
    width, height = 128, 64
    phase = zlib.crc32(world["id"].encode()) % 400 / 30
    pattern = world["pattern"]
    dark, light = [bytes.fromhex(world[key]) for key in ("primary", "secondary")]
    pixels = bytearray()
    for y in range(height):
        pixels.append(0)  # PNG row with no filter
        latitude = math.pi * ((y + 0.5) / height - 0.5)
        pole = abs(math.sin(latitude))
        for x in range(width):
            longitude = math.tau * (x + 0.5) / width + phase
            terrain = math.sin(3 * longitude + 2 * math.sin(4 * latitude)) * math.cos(latitude)
            terrain += 0.45 * math.sin(7 * longitude + 9 * latitude) * (1 - pole)
            grain = math.sin(19 * longitude - 28 * latitude) * 0.08 * (1 - pole)
            shade = 0.45 + 0.32 * terrain + grain
            match pattern:
                case "ice":
                    shade = max(shade + 0.18, pole ** 2)
                case "ocean":
                    shade = max(0.06, shade - 0.23) + 0.36 * (terrain > 0.72)
                case "forest":
                    shade += 0.17 if terrain > 0.0 else -0.16
                case "dunes" | "clouds":
                    strength = 0.33 if pattern == "clouds" else 0.16
                    shade += strength * math.cos(24 * latitude + 1.4 * terrain)
                case "lava":
                    shade = 0.95 if abs(terrain + grain) < 0.11 else shade * 0.24
                case "city":
                    street = x % 5 == 0 or y % 4 == 0
                    shade = shade * 0.70 + 0.42 * street * (1 - pole)
                case "station":
                    shade = 0.30 + grain + 0.32 * (x % 6 == 0 or y % 5 == 0)
                    if abs(y - height // 2) <= 1:
                        shade = 0.08
                    if (x - 32) ** 2 + (y - 21) ** 2 <= 36:
                        shade = 0.80 if (x - 32) ** 2 + (y - 21) ** 2 > 20 else 0.13
            shade = min(1, max(0, shade))
            pixels.extend(round(low + (high - low) * shade) for low, high in zip(dark, light))
    header = struct.pack(">IIBBBBB", width, height, 8, 2, 0, 0, 0)
    return (b"\x89PNG\r\n\x1a\n" + png_chunk(b"IHDR", header)
            + png_chunk(b"IDAT", zlib.compress(pixels, 9)) + png_chunk(b"IEND", b""))


def load_worlds(starwcraft):
    with (ROOT / "worlds.tsv").open(encoding="utf-8", newline="") as stream:
        worlds = list(csv.DictReader(stream, delimiter="\t"))
    dimensions = {world["id"] for world in worlds}
    with zipfile.ZipFile(starwcraft) as jar:
        prefix = "data/starwarsmod/dimension/"
        expected = {Path(name).stem for name in jar.namelist()
                    if name.startswith(prefix) and name.endswith(".json")} - {"space"}
    if dimensions != expected or len(worlds) != len(dimensions):
        raise ValueError(f"Catalog mismatch, missing {expected - dimensions}, extra {dimensions - expected}")
    return worlds


def yavin_biome(starwcraft):
    name = "data/starwarsmod/worldgen/biome/yavin4_massassi_ridges.json"
    with zipfile.ZipFile(starwcraft) as jar:
        biome = json.loads(jar.read(name))
    # Match the order of shared vegetation in Yavin's jungle to avoid a feature cycle.
    vegetation = biome["features"][9]
    flower = "minecraft:flower_warm"
    vegetation.remove(flower)
    vegetation.insert(vegetation.index("minecraft:patch_grass_jungle"), flower)
    return name, json.dumps(biome).encode("utf-8")


def assets(worlds):
    catalog = []
    translations = {lang: {} for lang in ("en_us", "ru_ru")}
    for world in worlds:
        name = world["id"]
        entry = {key: float(world[key]) for key in ("orbit", "angle", "radius", "gravity")}
        entry.update(id=name, primary=int(world["primary"], 16), secondary=int(world["secondary"], 16))
        catalog.append(entry)
        for lang, strings in translations.items():
            strings[f"destination.swa_space.{name}"] = world[lang]
        yield f"assets/swa_space/textures/space/{name}.png", planet_chart(world)
    for lang, strings in translations.items():
        yield f"assets/swa_space/lang/{lang}.json", json.dumps(strings, ensure_ascii=False).encode("utf-8")
    yield "swa_space_worlds.json", json.dumps(catalog).encode("utf-8")


def build(libraries, mods, javac, client_libraries):
    lock = json.loads((PACK / "mods.lock.json").read_text(encoding="utf-8"))
    unbound = next(entry["file"] for entry in lock["mods"] if entry["file"].startswith("unbound-"))
    starwcraft = next(entry["file"] for entry in lock["additions"].values()
                     if entry["file"].startswith("StarWCraft-"))
    epic_fight = lock["additions"]["epic-fight"]["file"]
    hbm = next(entry["file"] for entry in lock["mods"] if entry["file"].startswith("ntm-decentralized-"))
    worlds = load_worlds(mods / starwcraft)
    forge = libraries / "net/neoforged/neoforge" / lock["neoforge"]
    server = forge / f"neoforge-{lock['neoforge']}-server.jar"
    universal = forge / f"neoforge-{lock['neoforge']}-universal.jar"
    client = list((client_libraries / "net/minecraft/client").glob(f"{lock['minecraft']}-*/client-*-srg.jar"))
    if len(client) != 1:
        raise ValueError("Use --client-libraries with the libraries directory from the pinned NeoForge client.")
    required = [server, universal, *client, mods / unbound, mods / starwcraft, mods / epic_fight, mods / hbm]
    for path in required:
        if not path.is_file():
            raise FileNotFoundError(path)

    source = ROOT / "src" / "main"
    ARTIFACT.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(dir=ARTIFACT.parent) as folder:
        temporary = Path(folder)
        classes = temporary / "classes"
        with zipfile.ZipFile(universal) as forge_jar:
            mixin_extras = next(name for name in forge_jar.namelist()
                                if name.startswith("META-INF/jarjar/mixinextras-neoforge-") and name.endswith(".jar"))
            embedded = Path(forge_jar.extract(mixin_extras, temporary))
        classpath = os.pathsep.join(str(jar) for jar in [*required, embedded, *sorted(libraries.rglob("*.jar"))])
        subprocess.run([javac, "--release", str(lock["java"]), "-proc:none", "-encoding", "UTF-8",
                        "-cp", classpath, "-d", str(classes),
                        *map(str, sorted((source / "java").rglob("*.java")))], check=True)
        files = {}
        for directory in (classes, source / "resources"):
            files.update((file.relative_to(directory).as_posix(), file.read_bytes())
                         for file in directory.rglob("*") if file.is_file())
        name, content = yavin_biome(mods / starwcraft)
        files[name] = content
        installer = runpy.run_path(str(PACK / "install.py"))
        key_changes = installer["CONVOY_KEY_CHANGES"] | installer["RESEARCH_KEY_CHANGES"]
        files["swa_convoy_key_defaults.tsv"] = b"".join(
            action + b"\tkey.keyboard." + destination + b"\n"
            for action, (_, destination) in sorted(key_changes.items())
        )
        for name, content in assets(worlds):
            if name in files:
                strings = json.loads(files[name]) | json.loads(content)
                content = json.dumps(strings, ensure_ascii=False).encode("utf-8")
            files[name] = content
        stage = temporary / ARTIFACT.name
        with zipfile.ZipFile(stage, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=9) as jar:
            for name, content in sorted(files.items()):
                entry = zipfile.ZipInfo(name)
                entry.external_attr = 0o100644 << 16
                jar.writestr(entry, content, compress_type=zipfile.ZIP_DEFLATED, compresslevel=9)
        stage.replace(ARTIFACT)
    print(ARTIFACT)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("libraries", type=Path, help="libraries directory from a NeoForge server install")
    parser.add_argument("mods", type=Path, help="mods directory from an installed Convoy profile")
    parser.add_argument("--javac", default="javac", help="path to the Java 21 compiler")
    parser.add_argument("--client-libraries", type=Path, help="libraries directory from the pinned NeoForge client")
    args = parser.parse_args()
    build(args.libraries.resolve(), args.mods.resolve(), args.javac, (args.client_libraries or args.libraries).resolve())
