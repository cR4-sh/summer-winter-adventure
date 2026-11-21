#!/usr/bin/env python3
"""Export original Force poses to Epic Fight's biped animation format.

Pass the pinned Epic Fight jar to read the joint bind orientations. The
generated clips contain only relative rotations and visual pelvis offsets.
"""
from __future__ import annotations

import argparse
from dataclasses import dataclass
import json
import math
from pathlib import Path
from zipfile import ZipFile


X = (1, 0, 0)
Y = (0, 1, 0)
Z = (0, 0, 1)
ZERO = (0, 0, 0)
IDENTITY = (1, 0, 0, 0)
OUTPUT = Path(__file__).parent / "src/main/resources/assets/swa_space/animmodels/animations/force"


def dot(a, b):
    return sum(x * y for x, y in zip(a, b, strict=True))


def cross(a, b):
    x, y, z = a
    u, v, w = b
    return (y * w - z * v, z * u - x * w, x * v - y * u)


def unit(values):
    length = math.sqrt(dot(values, values))
    return tuple(x / length for x in values)


def rot(axis, degrees):
    angle = math.radians(degrees) / 2
    return (math.cos(angle), *(math.sin(angle) * x for x in axis))


def multiply(a, b):
    w, *v = a
    s, *u = b
    return (w * s - dot(v, u), *(w * y + s * x + z for x, y, z in zip(v, u, cross(v, u), strict=True)))


def inverse(q):
    return (q[0], *(-x for x in q[1:]))


def rotate(q, v):
    return multiply(multiply(q, (0, *v)), inverse(q))[1:]


def angles(pitch=0, yaw=0, roll=0):
    return multiply(multiply(rot(Y, -yaw), rot(X, -pitch)), rot(Z, roll))


def aim(current, target):
    return unit((1 + dot(current, target), *cross(current, target)))


def bind_quaternion(m):
    """Read the rotation from the row-major bind matrix in the armature."""
    w = math.sqrt(max(0, 1 + m[0] + m[5] + m[10])) / 2
    if w > 0.01:
        d = 4 * w
        return unit((w, (m[9] - m[6]) / d, (m[2] - m[8]) / d, (m[4] - m[1]) / d))
    if m[0] > m[5] and m[0] > m[10]:
        d = 2 * math.sqrt(1 + m[0] - m[5] - m[10])
        return unit(((m[9] - m[6]) / d, d / 4, (m[1] + m[4]) / d, (m[2] + m[8]) / d))
    if m[5] > m[10]:
        d = 2 * math.sqrt(1 + m[5] - m[0] - m[10])
        return unit(((m[2] - m[8]) / d, (m[1] + m[4]) / d, d / 4, (m[6] + m[9]) / d))
    d = 2 * math.sqrt(1 + m[10] - m[0] - m[5])
    return unit(((m[4] - m[1]) / d, (m[2] + m[8]) / d, (m[6] + m[9]) / d, d / 4))


@dataclass(frozen=True)
class Joint:
    name: str
    parent: str | None
    local: tuple
    bind: tuple


def armature(jar):
    with ZipFile(jar) as file:
        raw = json.loads(file.read("assets/epicfight/animmodels/entity/biped.json"))
    joints = {}

    def visit(node, parent=None):
        name = node["name"]
        local = bind_quaternion(node["transform"])
        if parent is None:
            local = multiply(rot(X, -90), local)
        bind = multiply(joints[parent].bind, local) if parent else local
        joints[name] = Joint(name, parent, local, bind)
        for child in node["children"]:
            visit(child, name)

    visit(raw["armature"]["hierarchy"][0])
    return joints


@dataclass(frozen=True)
class Arm:
    # Vectors use anatomical coordinates: outward, up, forward.
    upper: tuple
    forearm: tuple
    twist: int = 0


@dataclass(frozen=True)
class Pose:
    offset: tuple = ZERO
    hips: tuple = ZERO
    torso: tuple = ZERO
    chest: tuple = ZERO
    head: tuple = ZERO
    # First pair is thigh pitch, second pair shank pitch, then lateral splay.
    legs: tuple = (0, 0, 0, 0, 0, 0)
    right: Arm | None = None
    left: Arm | None = None


REST = Pose()

# Angles use degrees and joint vectors describe the silhouette in the model.
GUARD = Arm((.24, -.95, -.13), (.05, -.20, .98), 10)
OPEN = Arm((.64, -.74, .19), (.48, -.17, .86), -14)

PUSH_WINDUP = Pose(
    offset=(.021, -.023, -.023), hips=(-2, -8, 2), torso=(-8, -13, 5),
    chest=(-6, -8, 5), head=(4, 25, -6), legs=(2, -16, 4, 17, 4, 7),
    right=Arm((.48, -.77, -.39), (.25, -.32, .92), 17),
    left=Arm((.39, -.83, -.40), (-.17, .77, .61), -35),
)
PUSH_CONTACT = Pose(
    offset=(-.035, -.035, .041), hips=(4, 13, -4), torso=(9, 13, -3),
    chest=(5, 5, -4), head=(-13, -30, 6), legs=(7, -22, 7, 33, 8, 4),
    right=Arm((.40, -.82, -.40), (.32, -.22, .92), 12),
    left=Arm((.07, -.05, 1), (.11, .07, 1), -29),
)
PUSH_FOLLOW = Pose(
    offset=(-.037, -.027, .048), hips=(5, 15, -4), torso=(11, 15, -4),
    chest=(5, 5, -4), head=(-14, -32, 6), legs=(5, -19, 7, 28, 8, 4),
    right=Arm((.44, -.76, -.47), (.32, -.29, .90), 12),
    left=Arm((.12, -.10, 1), (.17, .12, 1), -19),
)
PUSH_SETTLE = Pose(
    offset=(-.019, -.018, .019), hips=(2, 6, -3), torso=(4, 7, -2),
    chest=(3, 2, -2), head=(-8, -14, 4), legs=(1, -11, 4, 18, 4, 4),
    right=GUARD, left=Arm((.25, -.48, .84), (.24, -.30, .93), -11),
)

PULL_REACH = Pose(
    offset=(.023, -.031, .024), hips=(3, -9, 4), torso=(10, -10, 4),
    chest=(7, -5, 5), head=(-11, 21, -5), legs=(-21, 4, 29, 5, 4, 8),
    right=Arm((.20, -.18, .96), (.11, .08, .99), 26),
    left=Arm((.54, -.82, -.16), (.43, -.22, .87), 10),
)
PULL_DRAW = Pose(
    offset=(.038, -.046, -.047), hips=(-8, -14, 1), torso=(-13, -13, 4),
    chest=(-7, -9, 4), head=(12, 28, -8), legs=(-15, -17, 19, 29, 6, 7),
    right=Arm((.56, -.45, -.69), (-.19, .65, .73), 42),
    left=Arm((.67, -.67, .30), (.38, -.51, .77), -6),
)
PULL_CHECK = Pose(
    offset=(.032, -.030, -.034), hips=(-5, -10, 1), torso=(-8, -10, 4),
    chest=(-5, -6, 4), head=(10, 21, -7), legs=(-10, -13, 14, 23, 5, 7),
    right=Arm((.62, -.51, -.60), (-.26, .67, .69), 31),
    left=OPEN,
)
PULL_SETTLE = Pose(
    offset=(.012, -.017, -.012), hips=(-2, -4, 1), torso=(-4, -5, 0),
    chest=(-2, -2, 0), head=(5, 10, 0), legs=(-6, -8, 7, 12, 4, 5),
    right=Arm((.37, -.85, -.38), (-.13, .54, .83), 17), left=GUARD,
)

CHOKE_DRAW = Pose(
    offset=(.01, -.018, -.01), hips=(-3, -8, 1), torso=(-6, -8, 4),
    chest=(-3, -4, 2), head=(4, 18, -4), legs=(-8, -5, 12, 8, 4, 4),
    right=Arm((.46, -.80, -.38), (-.14, .86, .49), 36), left=GUARD,
)
CHOKE_SNATCH = Pose(
    offset=(.027, -.025, .025), hips=(2, -11, 2), torso=(7, -13, 4),
    chest=(4, -8, 3), head=(-7, 29, -5), legs=(-16, -5, 24, 12, 5, 4),
    right=Arm((.20, -.20, .96), (.06, .66, .75), 45),
    left=Arm((.44, -.87, -.19), (.15, -.55, .82), -7),
)
CHOKE_HOLD = Pose(
    offset=(.023, -.022, .01), hips=(1, -10, 2), torso=(4, -12, 4),
    chest=(2, -7, 3), head=(-5, 26, -5), legs=(-14, -5, 21, 11, 5, 4),
    right=Arm((.25, -.31, .92), (.07, .70, .71), 42),
    left=Arm((.40, -.89, -.21), (.12, -.62, .77), -9),
)
CHOKE_BREATHE = Pose(
    offset=(.023, -.018, .007), hips=(0, -10, 2), torso=(3, -12, 4),
    chest=(1, -7, 3), head=(-3, 26, -5), legs=(-13, -5, 19, 10, 5, 4),
    right=Arm((.26, -.29, .92), (.04, .73, .68), 44),
    left=Arm((.41, -.89, -.20), (.12, -.58, .80), -9),
)
CHOKE_RELEASE = Pose(
    offset=(.015, -.025, .012), hips=(1, -7, 1), torso=(6, -8, 2),
    chest=(4, -4, 2), head=(-3, 18, -3), legs=(-12, -6, 18, 11, 4, 4),
    right=Arm((.39, -.38, .84), (.21, -.43, .88), 2),
    left=GUARD,
)

LIGHTNING_DRAW = Pose(
    offset=(.004, -.033, -.028), hips=(-7, 0, 0), torso=(-11, -3, -2),
    chest=(-6, -4, -2), head=(10, 6, 2), legs=(-13, -14, 24, 27, 7, 8),
    right=Arm((.65, -.65, -.39), (-.30, .55, .78), -27),
    left=Arm((.61, -.66, -.44), (-.28, .53, .81), -24),
)
LIGHTNING_SNAP = Pose(
    offset=(-.006, -.050, .034), hips=(5, 0, 0), torso=(16, 2, 2),
    chest=(8, -3, 1), head=(-20, 2, -2), legs=(-22, -23, 35, 37, 7, 8),
    right=Arm((.32, -.12, .94), (-.12, .17, .98), -46),
    left=Arm((.35, -.18, .92), (-.10, .11, .99), -40),
)
LIGHTNING_HOLD = Pose(
    offset=(-.005, -.047, .021), hips=(4, 0, 0), torso=(12, 2, 2),
    chest=(6, -3, 1), head=(-16, 2, -2), legs=(-20, -21, 33, 35, 7, 8),
    right=Arm((.36, -.22, .91), (-.18, .24, .95), -39),
    left=Arm((.40, -.26, .89), (-.15, .19, .97), -35),
)
LIGHTNING_FEEDBACK = Pose(
    offset=(-.004, -.055, .014), hips=(2, 2, -1), torso=(9, -2, 3),
    chest=(4, 1, -1), head=(-12, 1, -3), legs=(-21, -22, 35, 37, 7, 8),
    right=Arm((.45, -.28, .85), (-.21, .33, .92), -47),
    left=Arm((.43, -.30, .85), (-.20, .20, .96), -26),
)
LIGHTNING_UNWIND = Pose(
    offset=(-.009, -.035, -.018), hips=(-5, 1, -1), torso=(-8, 2, 2),
    chest=(-5, 1, -2), head=(9, -2, 1), legs=(-14, -15, 26, 28, 6, 7),
    right=Arm((.48, -.64, -.60), (.08, .64, .77), -16),
    left=Arm((.55, -.63, -.55), (.11, .55, .83), -12),
)

CLIPS = {
    "push": [(0, REST), (.049, PUSH_WINDUP), (.10, PUSH_CONTACT), (.18, PUSH_FOLLOW),
             (.32, PUSH_SETTLE), (.55, REST)],
    "pull": [(0, REST), (.056, PULL_REACH), (.11, PULL_DRAW), (.205, PULL_CHECK),
             (.33, PULL_SETTLE), (.56, REST)],
    "choke_start": [(0, REST), (.055, CHOKE_DRAW), (.102, CHOKE_SNATCH), (.27, CHOKE_HOLD)],
    "choke_hold": [(0, CHOKE_HOLD), (.54, CHOKE_BREATHE), (1.12, CHOKE_HOLD)],
    "choke_release": [(0, CHOKE_HOLD), (.092, CHOKE_RELEASE), (.25, PUSH_SETTLE), (.40, REST)],
    "lightning_start": [(0, REST), (.052, LIGHTNING_DRAW), (.105, LIGHTNING_SNAP), (.27, LIGHTNING_HOLD)],
    "lightning_hold": [(0, LIGHTNING_HOLD), (.18, LIGHTNING_FEEDBACK), (.32, LIGHTNING_SNAP),
                       (.47, LIGHTNING_HOLD), (.68, LIGHTNING_FEEDBACK), (.91, LIGHTNING_HOLD)],
    "lightning_release": [(0, LIGHTNING_HOLD), (.10, LIGHTNING_UNWIND), (.24, LIGHTNING_DRAW), (.44, REST)],
}


# Buffs are compact on the body so their animation can end as movement begins.
CLIPS.update({
    "jump": [
        (0, REST),
        (.060, Pose((0, -.068, -.025), (8, -6, 0), (11, -5, 0), (8, 0, 1), (-10, 9, -1),
                    (-26, -28, 45, 46, 4, 5),
                    Arm((.42, -.49, -.76), (.35, -.44, -.82), 10),
                    Arm((.46, -.46, -.75), (.37, -.38, -.85), 10))),
        (.125, Pose((0, .038, .021), (-5, 7, 0), (-7, 8, 3), (-6, 4, 0), (-10, -15, -2),
                    (-31, 12, 29, 5, 4, 3),
                    Arm((.32, -.83, -.42), (.32, -.66, -.68), 12),
                    Arm((.35, .38, .85), (.19, .72, .66), -14))),
        (.24, Pose((0, .027, .012), (-3, 8, 0), (-5, 5, 2), (-3, 3, 0), (-8, -12, -1),
                   (-23, 8, 22, 6, 3, 3),
                   Arm((.35, -.90, -.26), (.22, -.84, -.45), 8),
                   Arm((.31, .07, .95), (.16, .69, .69), -10))),
        (.45, REST),
    ],
    "speed": [
        (0, REST),
        (.049, Pose((.013, -.044, -.032), (-3, -12, 3), (-7, -14, 5), (-2, -4, 2), (4, 24, -5),
                    (-21, 4, 30, 9, 4, 3),
                    Arm((.22, -.74, .63), (.16, .73, .66), 12),
                    Arm((.41, -.57, -.71), (.32, -.30, .90), 7))),
        (.102, Pose((-.019, -.040, .050), (12, 11, -2), (16, 13, -2), (9, 7, -1), (-26, -27, 5),
                    (16, -29, 15, 37, 5, 4),
                    Arm((.37, -.46, -.81), (.09, .07, .99), 14),
                    Arm((.20, -.44, .87), (.06, .70, .71), 7))),
        (.23, Pose((.012, -.031, .028), (8, -9, 2), (11, -10, 0), (7, -4, 0), (-19, 19, -2),
                   (-21, 10, 31, 12, 4, 4),
                   Arm((.20, -.51, .84), (.03, .51, .86), 8),
                   Arm((.35, -.54, -.77), (.25, -.17, .95), 11))),
        (.42, REST),
    ],
    "heal": [
        (0, REST),
        (.07, Pose((0, -.018, -.012), (-2, 0, 0), (2, 4, 2), (3, 2, 1), (14, -4, -2),
                   (-5, -7, 10, 14, 3, 3),
                   Arm((.39, -.90, .17), (-.27, .13, .95), 34),
                   Arm((.28, -.89, .36), (-.43, .15, .90), 40))),
        (.17, Pose((-.009, -.032, .011), (2, 2, 1), (6, 5, 2), (6, 0, 1), (15, -7, 0),
                   (-9, -13, 16, 21, 4, 5),
                   Arm((.32, -.77, .55), (-.53, .60, .63), 48),
                   Arm((.24, -.62, .75), (-.50, .42, .75), 35))),
        (.35, Pose((-.009, -.026, .008), (0, 2, 1), (3, 4, 1), (3, 0, 1), (11, -6, 0),
                   (-7, -11, 13, 17, 4, 5),
                   Arm((.37, -.72, .58), (-.42, .63, .65), 39),
                   Arm((.31, -.65, .70), (-.44, .48, .76), 32))),
        (.52, Pose((0, -.011, .004), (-1, 0, 0), (0, 2, 1), (0, 0, 1), (4, -2, 0),
                   (-3, -4, 6, 7, 2, 3), OPEN, OPEN)),
        (.69, REST),
    ],
    "stun": [
        (0, REST),
        (.047, Pose((.016, -.013, -.012), (-2, -7, 2), (-4, -12, 3), (1, -5, 2), (2, 21, -4),
                    (-7, -4, 12, 7, 4, 2),
                    Arm((.54, -.74, -.39), (.12, .94, .32), 29), GUARD)),
        (.093, Pose((.021, -.017, .030), (4, -8, 1), (8, -14, 3), (4, -7, 0), (-11, 26, -3),
                    (-15, 0, 20, 7, 4, 3),
                    Arm((.29, .25, .92), (-.10, -.23, .97), 19),
                    Arm((.43, -.87, -.23), (.25, -.52, .81), 8))),
        (.16, Pose((.018, -.013, .027), (3, -7, 1), (7, -12, 3), (3, -5, 0), (-9, 22, -3),
                   (-13, -1, 18, 7, 4, 3),
                   Arm((.28, .20, .94), (-.06, -.31, .95), 16), OPEN)),
        (.33, REST),
    ],
    "shield": [
        (0, REST),
        (.058, Pose((0, -.024, -.018), (-3, 1, 0), (-5, 2, 1), (0, -1, 1), (5, -1, 0),
                    (-11, -11, 18, 18, 4, 4),
                    Arm((.43, -.76, .49), (-.21, .81, .55), 13),
                    Arm((.42, -.71, .57), (-.25, .84, .48), 16))),
        (.121, Pose((-.013, -.043, -.021), (-4, 8, -2), (5, 8, -3), (4, 5, -2), (4, -18, 4),
                    (-16, -18, 28, 28, 7, 7),
                    Arm((.35, -.19, .92), (-.74, .63, .28), 28),
                    Arm((.31, -.31, .90), (-.68, .71, .19), 22))),
        (.30, Pose((-.009, -.039, -.025), (-3, 7, -1), (3, 6, -2), (3, 3, -2), (7, -14, 3),
                   (-14, -15, 24, 25, 6, 6),
                   Arm((.42, -.25, .87), (-.69, .69, .22), 25),
                   Arm((.38, -.40, .83), (-.60, .75, .27), 18))),
        (.49, Pose((-.004, -.020, -.011), (-2, 3, 0), (0, 3, -1), (1, 1, 0), (3, -5, 0),
                   (-7, -8, 13, 14, 4, 4), OPEN, OPEN)),
        (.64, REST),
    ],
    "drain": [
        (0, REST),
        (.056, Pose((-.017, -.031, .036), (5, 11, -3), (12, 14, -4), (6, 8, -2), (-15, -28, 4),
                    (-3, -20, 12, 30, 6, 4), GUARD,
                    Arm((.30, -.45, .84), (.11, -.38, .92), 48))),
        (.113, Pose((-.025, -.039, -.037), (-6, 8, -4), (-11, 9, -7), (-8, 6, -2), (10, -19, 6),
                    (-12, -16, 20, 27, 6, 5),
                    Arm((.66, -.71, -.23), (.32, -.46, .83), -5),
                    Arm((.56, -.74, -.37), (-.65, .07, .76), 56))),
        (.275, Pose((-.022, -.030, -.025), (-4, 6, -4), (-7, 7, -6), (-5, 3, -2), (7, -16, 5),
                    (-10, -13, 17, 23, 5, 5),
                    Arm((.52, -.82, -.20), (.38, -.36, .86), -3),
                    Arm((.48, -.81, -.34), (-.61, .17, .77), 50))),
        (.43, Pose((-.007, -.012, -.008), (-1, 3, -2), (-3, 3, -2), (-1, 1, -1), (4, -6, 1),
                   (-5, -6, 9, 12, 4, 4), GUARD, GUARD)),
        (.62, REST),
    ],
    "saber_throw": [
        (0, REST),
        (.047, Pose((.026, -.027, -.025), (-3, -21, 3), (-7, -22, 5), (-5, -17, 5), (6, 35, -5),
                    (-8, -14, 16, 23, 4, 5),
                    Arm((.77, -.17, -.60), (.12, .85, -.51), 25),
                    Arm((.48, -.63, .61), (.32, .35, .88), -5))),
        (.106, Pose((-.029, -.034, .044), (7, 17, -4), (12, 19, -7), (5, 14, -5), (-15, -41, 11),
                    (-19, 5, 30, 7, 5, 7),
                    Arm((-.44, .12, .89), (-.31, -.12, .94), 31),
                    Arm((.53, -.66, -.54), (.38, -.40, .83), -8))),
        (.22, Pose((-.026, -.023, .034), (5, 14, -3), (9, 16, -5), (4, 10, -3), (-13, -33, 9),
                   (-16, 3, 26, 7, 5, 6),
                   Arm((-.31, -.21, .93), (-.29, -.29, .91), 26),
                   Arm((.48, -.78, -.39), (.28, -.48, .83), -6))),
        (.38, Pose((-.010, -.013, .013), (2, 6, -1), (4, 7, -2), (2, 5, -1), (-7, -15, 4),
                   (-8, -3, 15, 8, 4, 5),
                   Arm((.30, -.65, .70), (.22, -.50, .83), 10), GUARD)),
        (.60, REST),
    ],
    "leap_strike": [
        (0, REST),
        (.057, Pose((.016, -.061, -.024), (5, -10, 3), (11, -14, 1), (3, -8, 0), (-11, 23, -2),
                    (-25, -18, 39, 35, 5, 6),
                    Arm((.61, .63, -.48), (.20, .58, -.78), 31),
                    Arm((.49, -.37, .79), (.22, .26, .94), -12))),
        (.126, Pose((-.010, .043, .053), (9, 9, -4), (17, 17, -4), (9, 12, -5), (-24, -30, 8),
                    (-33, 16, 28, 9, 4, 4),
                    Arm((.13, .26, .95), (-.28, -.57, .77), 37),
                    Arm((.63, -.40, -.64), (.33, -.50, -.80), -6))),
        (.235, Pose((-.014, .018, .037), (8, 9, -3), (13, 14, -4), (7, 10, -5), (-18, -28, 6),
                    (-24, 10, 23, 11, 4, 5),
                    Arm((-.25, -.62, .75), (-.24, -.53, .81), 26),
                    Arm((.56, -.63, -.54), (.37, -.57, -.73), -9))),
        (.380, Pose((-.010, -.044, .016), (4, 4, -2), (7, 7, -2), (3, 7, -2), (-8, -15, 3),
                    (-12, -15, 22, 24, 4, 5),
                    Arm((.24, -.85, .46), (-.08, -.33, .94), 14), GUARD)),
        (.56, REST),
    ],
})


# Sense, stasis, absorption, repulse, and meditation use distinct hand shapes.
CLIPS.update({
    "force_sense": [
        (0, REST),
        (.071, Pose((.014, -.015, 0), (-1, -5, 1), (-3, -6, 2), (-2, -2, 2), (5, 4, -4),
                    (-5, -7, 8, 12, 3, 4), GUARD,
                    Arm((.64, -.39, .65), (-.63, .69, .35), 34))),
        (.175, Pose((.012, -.015, -.004), (-2, -6, 1), (-3, -7, 2), (-2, -3, 1), (8, -11, 6),
                    (-4, -6, 8, 11, 3, 4),
                    Arm((.51, -.82, -.25), (.30, -.31, .90), 12),
                    Arm((.50, .03, .87), (-.78, .58, -.23), 36))),
        (.37, Pose((.009, -.016, .005), (0, 6, 2), (-2, 4, 2), (-2, 1, 1), (4, 22, 3),
                   (-6, -7, 11, 12, 4, 4), OPEN,
                   Arm((.52, .04, .85), (-.72, .60, -.35), 37))),
        (.54, Pose((.009, -.012, 0), (0, 4, 1), (-2, 3, 1), (0, 1, 1), (5, 13, 1),
                   (-3, -5, 7, 9, 3, 4), GUARD, OPEN)),
        (.70, REST),
    ],
    "force_stasis": [
        (0, REST),
        (.057, Pose((-.009, -.023, -.018), (-4, 2, -1), (-6, 4, -1), (-5, 4, -2), (6, -8, 1),
                    (-12, -13, 18, 21, 5, 5),
                    Arm((.44, -.65, -.62), (-.25, .69, .68), 38),
                    Arm((.51, -.54, -.67), (-.24, .56, .79), 42))),
        (.115, Pose((-.012, -.036, .020), (2, 10, -1), (8, 12, -1), (4, 4, -2), (-9, -22, 2),
                    (-12, -17, 19, 28, 6, 6),
                    Arm((.30, -.54, .79), (-.26, .74, .62), -31),
                    Arm((.25, .22, .94), (-.23, -.08, .97), -44))),
        (.29, Pose((-.011, -.032, .018), (1, 9, -1), (6, 9, -1), (3, 4, -2), (-7, -18, 2),
                   (-11, -15, 18, 25, 6, 6),
                   Arm((.27, -.58, .77), (-.24, .73, .64), -30),
                   Arm((.26, .17, .95), (-.24, -.09, .97), -41))),
        (.47, Pose((-.008, -.016, .007), (1, 4, -1), (3, 3, 0), (1, 2, -1), (-3, -8, 1),
                   (-6, -7, 10, 13, 4, 4), GUARD, OPEN)),
        (.65, REST),
    ],
    "tutaminis": [
        (0, REST),
        (.055, Pose((.004, -.030, .016), (3, 2, 0), (7, 4, 2), (4, 1, 1), (-4, -6, -1),
                    (-13, -11, 20, 19, 5, 5),
                    Arm((.41, -.45, .79), (-.17, .49, .85), -41),
                    Arm((.61, -.73, .29), (-.30, .35, .88), 34))),
        (.115, Pose((-.006, -.048, -.028), (-7, 6, -1), (-11, 7, 2), (-7, 3, 1), (13, -15, -1),
                    (-14, -20, 24, 31, 6, 6),
                    Arm((.50, -.72, -.45), (-.19, .78, .58), -45),
                    Arm((.60, -.60, .52), (-.58, .35, .73), 35))),
        (.265, Pose((-.007, -.037, -.016), (-4, 4, -1), (-7, 5, 2), (-4, 3, 0), (9, -11, -1),
                    (-11, -15, 20, 26, 6, 6),
                    Arm((.47, -.73, -.49), (-.26, .76, .60), -39),
                    Arm((.51, -.69, .50), (-.52, .33, .79), 29))),
        (.46, Pose((0, -.016, -.005), (-1, 1, 0), (-2, 2, 0), (-1, 1, 0), (3, -4, 0),
                   (-5, -7, 10, 12, 4, 4), OPEN, GUARD)),
        (.62, REST),
    ],
    "force_repulse": [
        (0, REST),
        (.049, Pose((.003, -.060, -.018), (6, -3, 0), (12, -3, 0), (10, 1, 1), (9, 4, -1),
                    (-22, -23, 37, 38, 7, 7),
                    Arm((.37, -.89, .26), (-.63, .42, .65), 44),
                    Arm((.38, -.88, .27), (-.66, .40, .64), 44))),
        (.107, Pose((-.001, -.022, .006), (-4, 0, -1), (-9, 1, 0), (-6, -1, 0), (-5, 0, 0),
                    (-9, -8, 15, 14, 7, 7),
                    Arm((.81, .33, .48), (.93, .19, .31), -40),
                    Arm((.83, .29, .47), (.92, .24, .30), -43))),
        (.225, Pose((-.004, -.027, .009), (-3, 1, -1), (-6, 2, 0), (-5, -1, 0), (-3, -1, 1),
                    (-9, -10, 15, 16, 6, 6),
                    Arm((.90, .08, .43), (.94, .11, .32), -31),
                    Arm((.90, .04, .43), (.94, .09, .33), -32))),
        (.40, Pose((-.002, -.017, .006), (0, 0, -1), (-2, 1, 0), (-1, 0, 0), (-1, -1, 0),
                   (-7, -7, 11, 12, 4, 4), OPEN, OPEN)),
        (.60, REST),
    ],
    "battle_meditation": [
        (0, REST),
        (.080, Pose((0, -.023, -.005), (2, 0, 0), (4, 1, 0), (2, -1, 0), (12, 0, 0),
                    (-9, -10, 14, 15, 4, 4),
                    Arm((.47, -.84, .27), (-.23, .22, .95), 51),
                    Arm((.48, -.83, .27), (-.21, .20, .95), 49))),
        (.21, Pose((0, -.046, -.010), (3, 0, 0), (6, 0, 0), (4, 0, 0), (15, 0, 0),
                   (-16, -16, 28, 28, 6, 6),
                   Arm((.45, -.83, .32), (-.32, .53, .79), 54),
                   Arm((.45, -.83, .33), (-.33, .52, .79), 54))),
        (.43, Pose((0, -.038, -.009), (1, 0, 0), (4, 0, 0), (0, 0, 0), (7, 0, 0),
                   (-13, -13, 23, 23, 5, 5),
                   Arm((.55, -.82, .16), (.24, .21, .95), 52),
                   Arm((.55, -.82, .16), (.24, .21, .95), 52))),
        (.61, Pose((0, -.021, 0), (-1, 0, 0), (1, 0, 0), (-2, 0, 0), (1, 0, 0),
                   (-8, -8, 14, 14, 4, 4),
                   Arm((.73, -.66, .16), (.58, -.04, .81), 37),
                   Arm((.73, -.66, .16), (.58, -.04, .81), 37))),
        (.83, REST),
    ],
})


def pose_transforms(pose, joints):
    thigh_r, thigh_l, shin_r, shin_l, splay_r, splay_l = pose.legs
    body_rotations = {
        "Root": pose.hips, "Torso": pose.torso, "Chest": pose.chest, "Head": pose.head,
        "Thigh_R": (thigh_r, 0, splay_r), "Leg_R": (shin_r, 0, -splay_r),
        "Thigh_L": (thigh_l, 0, -splay_l), "Leg_L": (shin_l, 0, splay_l),
    }
    for side in ("R", "L"):
        # Corrective joints follow half of the shank motion to round the knee.
        body_rotations[f"Knee_{side}"] = tuple(angle / 2 for angle in body_rotations[f"Leg_{side}"])
    rotations = {}
    transformed = {}
    for name, joint in joints.items():
        parent = transformed.get(joint.parent, IDENTITY)
        base = multiply(parent, joint.local)
        local = None
        if name in body_rotations:
            desired = angles(*body_rotations[name])
            local = multiply(multiply(inverse(joint.bind), desired), joint.bind)
        elif name.startswith(("Arm_", "Hand_")):
            right = name.endswith("_R")
            arm = pose.right if right else pose.left
            if arm is not None:
                upper = name.startswith("Arm_")
                x, y, z = arm.upper if upper else arm.forearm
                # Epic Fight biped forward is model -Z.
                target = unit((x if right else -x, y, -z))
                world = multiply(aim(rotate(base, Y), target), base)
                local = multiply(inverse(base), world)
                if not upper:
                    local = multiply(local, rot(Y, arm.twist if right else -arm.twist))
            else:
                local = IDENTITY
        elif name.startswith("Elbow_"):
            hand = joints[f"Hand_{name[-1]}"]
            hand_bind = multiply(parent, hand.local)
            swing = aim(rotate(hand_bind, Y), rotate(transformed[hand.name], Y))
            # Follow the middle of the arm bend without inheriting wrist twist.
            halfway = unit((1 + swing[0], *swing[1:]))
            local = multiply(multiply(inverse(base), halfway), base)
        if local is None:
            transformed[name] = base
            continue
        local = unit(local)
        transformed[name] = multiply(base, local)
        rotations[name] = {
            "loc": [pose.offset[0], pose.offset[1], -pose.offset[2]] if name == "Root" else list(ZERO),
            # ATTRIBUTES takes w first and converts to Epic Fight matrix convention.
            "rot": [round(value, 7) or 0 for value in local],
            "sca": [1, 1, 1],
        }
    return rotations


def write_clip(name, keys, joints):
    times, poses = zip(*keys, strict=True)
    frames = [pose_transforms(pose, joints) for pose in poses]
    tracks = [
        {"name": joint, "time": times, "transform": [frame[joint] for frame in frames]}
        for joint in frames[0]
    ]
    data = ",\n".join("    " + json.dumps(track, separators=(",", ":")) for track in tracks)
    (OUTPUT / f"{name}.json").write_text(
        '{\n  "format": "ATTRIBUTES",\n  "animation": [\n' + data + "\n  ]\n}\n", encoding="utf-8"
    )


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("epic_fight_jar", type=Path, help="Pinned Epic Fight jar")
    args = parser.parse_args()
    joints = armature(args.epic_fight_jar)
    OUTPUT.mkdir(parents=True, exist_ok=True)
    for name, keys in CLIPS.items():
        write_clip(name, keys, joints)


if __name__ == "__main__":
    main()
