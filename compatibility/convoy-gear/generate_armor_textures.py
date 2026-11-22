#!/usr/bin/env python3
"""Rebuild the material atlases used by the wearable StarWCraft models."""

from pathlib import Path
import struct
import zlib


OUTPUT = Path(__file__).parent / "src/main/resources/assets/swa_gear_preview/textures/armor"
SWATCH_WIDTH = 64
HEIGHT = 48
SCALE = 2

# Fabric, shell, trim, shade, markings, visor, metal, signal.
PALETTES = {
    "boba_fett":       "665f51 5f7350 435e4b 30292c b88a44 171e27 838986 a24e39",
    "clone_trooper":   "303235 dbdcd3 bfc5c3 343b45 476c95 17212d 777d81 6a9cb5",
    "death_trooper":   "22282d 39464a 526064 151c21 3d5454 101d24 666e71 71ca94",
    "inquisitor":     "30343b 525861 3b434a 242631 934148 2a161e 85848a c34f59",
    "jedi":           "b9a17e 76513a c6ae8a 574233 a28b61 4e4434 9a855f d5be89",
    "mandalorian":    "3d393a 9da8b0 c6cbd0 43454b 747e87 19242c 727e86 80acb8",
    "plastoid":       "333a42 71868a 9bada9 2d3b40 baf1d5 23464b 9a9e93 ecaa4e",
    "rebel_pilot":    "bb5e35 e1aa75 dbd4b8 3f3d39 9b3e35 d5a34e 81887d b1d5b8",
    "royal_guard":    "7c232b ae3540 921e36 47151e c05b58 2e1721 853c40 a74649",
    "sandtrooper":    "3d3931 cec5ab b9ac91 4b4236 b97b39 20272a 847a6a aab6ae",
    "scavenger":      "786956 9b8265 b7a487 463c35 76664e 554038 8f6e47 c6a168",
    "scout":          "383a3c cccec4 abb5b5 383d40 707c7d 1f2631 849096 a4b4ae",
    "scrap":          "4f4742 696b64 828374 383638 a4624b 2c4149 9a8972 af8d4e",
    "sith":           "312f39 4d4049 62525a 28222c a04446 211921 88828a b1535a",
    "snowtrooper":    "b6b0a0 e1ddcf c5c0ac 4b4f52 86958f 293337 949b9a 7f9eab",
    "temple_guard":   "a29882 cdc5af e2d9c1 595037 b38f4e 403731 99825a d8bc74",
    "vader":          "28282c 33363e 53565d 22252d ad4346 2d2931 89898c 88aeb8",
}


def png_chunk(kind, payload):
    return (struct.pack(">I", len(payload)) + kind + payload
            + struct.pack(">I", zlib.crc32(kind + payload)))


def write_atlas(path, colors):
    colors = [bytes.fromhex(color) for color in colors.split()]
    width = len(colors) * SWATCH_WIDTH * SCALE
    height = HEIGHT * SCALE
    rows = bytearray()
    for y in range(height):
        rows.append(0)
        for x in range(width):
            tile, u = divmod(x, SWATCH_WIDTH * SCALE)
            grain = ((u * 113 ^ y * 179 ^ u * y * 17) % 11 - 5) / 5
            shade = 1.025 - .10 * y / height + .025 * (u / (SWATCH_WIDTH * SCALE))
            if tile == 0:
                grain *= 2.6
                shade -= .023 * ((u + y) // 2 % 2)
            elif tile in (1, 2, 6):
                grain *= 1.8
                shade += .012 * (y % 5 - 2)
            elif tile == 5:
                grain *= .4
                shade += .15 * max(0, 1 - abs(y - 12) / 13)
            else:
                grain *= 1.35
            rows.extend(max(0, min(255, round(value * shade + grain))) for value in colors[tile])
    header = struct.pack(">IIBBBBB", width, height, 8, 2, 0, 0, 0)
    path.write_bytes(b"\x89PNG\r\n\x1a\n" + png_chunk(b"IHDR", header)
                     + png_chunk(b"IDAT", zlib.compress(rows, 9)) + png_chunk(b"IEND", b""))


if __name__ == "__main__":
    OUTPUT.mkdir(parents=True, exist_ok=True)
    for name, palette in PALETTES.items():
        write_atlas(OUTPUT / f"{name}.png", palette)
