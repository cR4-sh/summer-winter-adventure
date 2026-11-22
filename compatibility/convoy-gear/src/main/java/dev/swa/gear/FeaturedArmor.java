package dev.swa.gear;

import static dev.swa.gear.ArmorModels.Material.FABRIC;
import static dev.swa.gear.ArmorModels.Material.MARK;
import static dev.swa.gear.ArmorModels.Material.METAL;
import static dev.swa.gear.ArmorModels.Material.SHADE;
import static dev.swa.gear.ArmorModels.Material.SHELL;
import static dev.swa.gear.ArmorModels.Material.SIGNAL;
import static dev.swa.gear.ArmorModels.Material.TRIM;
import static dev.swa.gear.ArmorModels.Material.VISOR;

import dev.swa.gear.ArmorMesh.Builder;
import dev.swa.gear.ArmorMesh.Point;
import dev.swa.gear.ArmorModels.Material;
import dev.swa.gear.ArmorModels.Part;

final class FeaturedArmor {
    private static final int HELMET_SEGMENTS = 24;
    private static final int HOOD_SEGMENTS = 18;
    private static final int THRUSTER_SEGMENTS = 16;
    private static final int[] SIDES = {-1, 1};

    private FeaturedArmor() {}

    static void vaderHelmet(Part head) {
        // The back of the dome flares below the jaw. Its front sweeps up over the brow.
        Builder dome = new Builder();
        loft(dome, SHELL, 0, HELMET_SEGMENTS, .67F,
                ring(-9.67F, .05F, .07F, .12F),
                ring(-9.54F, 1.73F, 1.92F, .12F),
                ring(-9.16F, 3.36F, 3.16F, .10F),
                ring(-8.63F, 4.33F, 4.04F, .02F),
                ring(-7.83F, 4.77F, 4.55F, -.05F),
                new Ring(-5.25F, 4.92F, 4.72F, .09F, 2.03F),
                new Ring(-1.83F, 5.08F, 4.97F, .31F, 5.10F),
                new Ring(.01F, 5.28F, 5.03F, .48F, 6.80F),
                new Ring(.37F, 5.25F, 4.97F, .47F, 7.24F));
        Point[] rim = ringPoints(new Ring(.37F, 5.25F, 4.97F, .47F, 7.24F), 0, HELMET_SEGMENTS, .67F);
        dome.strip(SHADE, rim, ringPoints(new Ring(.22F, 4.99F, 4.62F, .48F, 7.15F),
                0, HELMET_SEGMENTS, .67F), true);
        loft(dome, SHADE, 0, HELMET_SEGMENTS, .24F,
                ring(-8.22F, 4.45F, 3.78F, -.62F),
                ring(-7.77F, 4.58F, 3.86F, -.64F),
                ring(-5.97F, 4.58F, 3.87F, -.65F),
                ring(-2.34F, 4.53F, 3.80F, -.65F),
                ring(.04F, 4.28F, 3.63F, -.68F));
        head.mesh(dome);
        Builder mask = new Builder();
        mask.quad(SHELL, point(-3.37F, -6.95F, -4.16F), point(-2.77F, -5.95F, -4.65F),
                point(2.77F, -5.95F, -4.65F), point(3.37F, -6.95F, -4.16F));
        for (int side : SIDES) {
            face(mask, TRIM, side, point(.56F, -5.60F, -4.78F), point(1.71F, -4.13F, -5.06F),
                    point(3.55F, -4.50F, -4.69F), point(3.14F, -5.89F, -4.41F));
            face(mask, VISOR, side, point(.81F, -5.80F, -4.85F), point(1.51F, -4.79F, -5.05F),
                    point(3.46F, -4.99F, -4.73F), point(3.08F, -5.96F, -4.61F));
            face(mask, SHADE, side, point(3.48F, -5.04F, -4.64F), point(2.22F, -3.95F, -5.13F),
                    point(2.61F, -.60F, -4.61F), point(4.20F, -1.67F, -3.42F));
            face(mask, TRIM, side, point(2.96F, -4.35F, -4.87F), point(2.14F, -3.55F, -5.24F),
                    point(2.48F, -1.41F, -4.85F), point(3.76F, -2.21F, -4.06F));
            face(mask, SHELL, side, point(0, -6.17F, -4.73F), point(0, -4.11F, -5.56F),
                    point(.79F, -3.71F, -5.16F), point(1.26F, -4.88F, -4.93F));
            head.angle(METAL, side * 2.03F, -1.05F, -4.98F, .41F, .49F, .56F,
                    -.11F, -side * .18F, side * .12F);
        }
        mask.triangle(SHADE, point(0, -4.20F, -5.52F), point(-2.08F, -1.24F, -4.88F),
                point(2.08F, -1.24F, -4.88F));
        mask.quad(SHELL, point(-2.08F, -1.24F, -4.90F), point(-1.46F, -.43F, -4.62F),
                point(1.46F, -.43F, -4.62F), point(2.08F, -1.24F, -4.90F));
        for (int grille = -2; grille <= 2; grille++) {
            float x = grille * .45F;
            float height = 2.19F - .45F * Math.abs(grille);
            mask.quad(METAL, point(x - .065F, -1.42F - height, -5.03F - height * .20F),
                    point(x - .065F, -1.42F, -4.99F), point(x + .065F, -1.42F, -4.99F),
                    point(x + .065F, -1.42F - height, -5.03F - height * .20F));
        }
        head.mesh(mask);
    }

    static void guardHelmet(Part head) {
        Builder helm = new Builder();
        loft(helm, SHELL, 0, HELMET_SEGMENTS, .55F,
                ring(-9.50F, .05F, .05F, .27F),
                ring(-9.39F, 2.26F, 1.95F, .22F),
                ring(-9.00F, 3.63F, 3.37F, .17F),
                ring(-8.45F, 4.33F, 4.12F, .13F),
                ring(-7.85F, 4.63F, 4.58F, .16F),
                ring(-5.45F, 4.57F, 4.76F, .10F),
                ring(-2.29F, 4.74F, 4.58F, .12F),
                ring(.40F, 4.79F, 4.35F, .15F));
        Point[] jaw = ringPoints(ring(.40F, 4.79F, 4.35F, .15F), 0, HELMET_SEGMENTS, .55F);
        helm.strip(TRIM, jaw, ringPoints(ring(.49F, 4.31F, 3.76F, .15F), 0, HELMET_SEGMENTS, .55F), true);
        Point[] upper = new Point[13];
        Point[] lower = new Point[13];
        for (int i = 0; i < upper.length; i++) {
            float angle = -.69F + 1.38F * i / (upper.length - 1);
            float x = signedPower(Math.sin(angle), .55F) * 4.61F;
            float z = .10F - signedPower(Math.cos(angle), .55F) * 4.93F;
            float lift = .22F * Math.abs(angle);
            upper[i] = point(x, -5.83F + lift, z);
            lower[i] = point(x, -5.18F - lift, z + .035F);
        }
        helm.strip(VISOR, upper, lower, false);
        head.mesh(helm);
    }

    static void hood(Part head, boolean sith) {
        // The opening has a rolled edge and a separate lining. Leave space for the player's face.
        Point[] back = hoodOutline(3.22F, 4.08F, 4.37F, .02F, -.03F);
        Point[] crown = hoodOutline(4.97F, 4.91F, 2.98F, .01F, 0);
        Point[] middle = hoodOutline(5.15F, 5.09F, -.16F, .12F, .14F);
        Point[] lip = hoodOutline(sith ? 5.18F : 5.06F, sith ? 5.24F : 4.92F,
                -4.78F, .42F, .23F);
        Point[] opening = hoodOutline(sith ? 3.88F : 3.81F, 4.13F, -4.70F, .36F, .14F);
        Point[] lining = hoodOutline(4.35F, 4.59F, -.29F, 0, .10F);
        Builder hood = new Builder();
        hood.strip(FABRIC, back, crown, false)
            .strip(SHELL, crown, middle, false)
            .strip(SHELL, middle, lip, false)
            .strip(TRIM, lip, opening, false)
            .strip(SHADE, opening, lining, false);
        Point rearCenter = point(0, -3.83F, 4.70F);
        for (int i = 0; i < back.length - 1; i++) {
            hood.triangle(FABRIC, rearCenter, back[i], back[i + 1]);
        }
        for (int i : new int[] {0, lip.length - 1}) {
            hood.quad(SHADE, lip[i], middle[i], lining[i], opening[i]);
        }
        head.mesh(hood);
    }

    static void vaderCollar(Part body) {
        Builder collar = new Builder().rigid();
        loft(collar, SHELL, 0, HELMET_SEGMENTS, .62F,
                ring(-.37F, 3.87F, 2.12F, 0),
                ring(.46F, 4.26F, 2.43F, 0),
                ring(2.64F, 4.55F, 2.58F, 0),
                ring(2.94F, 4.40F, 2.54F, 0));
        for (int side : SIDES) {
            face(collar, TRIM, side, point(.46F, .58F, -2.44F), point(.50F, 2.86F, -2.58F),
                    point(1.58F, 2.79F, -2.51F), point(1.47F, .61F, -2.40F));
            face(collar, TRIM, side, point(2.09F, .65F, -2.31F), point(2.38F, 2.77F, -2.42F),
                    point(3.46F, 2.67F, -2.20F), point(3.18F, .90F, -2.10F));
        }
        body.mesh(collar);
    }

    static void vaderShoulder(Part arm, float center, int side) {
        Builder shoulder = new Builder().rigid();
        loft(shoulder, SHELL, center + side * .35F, THRUSTER_SEGMENTS, .76F,
                ring(-2.62F, .29F, .33F, 0),
                ring(-2.24F, 1.61F, 1.84F, .04F),
                ring(-1.55F, 2.16F, 2.30F, .08F),
                ring(-.25F, 2.52F, 2.61F, .11F),
                ring(1.23F, 2.47F, 2.56F, .11F),
                ring(1.58F, 2.21F, 2.28F, .10F));
        arm.mesh(shoulder);
    }

    static void guardRobe(Part body) {
        Builder robe = new Builder();
        Point[] collar = robeFront(-.18F, 3.55F, 2.20F);
        Point[] chest = robeFront(2.10F, 4.28F, 2.68F);
        Point[] waist = robeFront(7.52F, 3.96F, 2.43F);
        Point[] hips = robeFront(12.01F, 4.32F, 2.59F);
        robe.strip(SHELL, collar, chest, false).strip(SHELL, chest, waist, false)
            .strip(SHELL, waist, hips, false);
        body.mesh(robe);
        body.angle(TRIM, 0, 1.50F, -2.62F, 2.34F, .82F, .43F, -.23F, 0, 0);
        cape(body, true);
    }

    static void guardSkirt(Part leg, int side) {
        Builder skirt = new Builder();
        Point[] top = robeFront(-.07F, 2.07F, 2.59F);
        Point[] fold = robeFront(4.83F, 2.12F, 2.99F);
        Point[] end = robeFront(9.41F - side * .19F, 2.21F, 3.36F);
        skirt.strip(SHELL, top, fold, false).strip(TRIM, fold, end, false);
        leg.mesh(skirt);
    }

    private static Point[] robeFront(float y, float width, float depth) {
        return new Point[] {
            point(-width, y, -depth + .51F), point(-width * .68F, y, -depth - .09F),
            point(-width * .27F, y, -depth + .07F), point(width * .09F, y, -depth - .20F),
            point(width * .39F, y, -depth + .04F), point(width * .73F, y, -depth - .04F),
            point(width, y, -depth + .52F)
        };
    }

    static void cape(Part body, boolean guard) {
        // The hem flares and the edges roll forward independently of the sleeves.
        Point[][] rows = {
            capeRow(.55F, guard ? 4.59F : 4.30F, 2.82F, 1.48F, .14F, 0),
            capeRow(2.80F, guard ? 5.50F : 5.04F, 3.02F, 1.43F, .23F, .22F),
            capeRow(7.36F, guard ? 5.79F : 5.30F, 3.65F, .95F, .31F, .06F),
            capeRow(13.41F, guard ? 6.28F : 5.94F, 4.23F, .82F, .45F, -.12F),
            capeRow(18.64F, guard ? 6.71F : 6.44F, 4.83F, .93F, .56F, -.43F),
            capeRow(23.08F, guard ? 7.22F : 6.85F, 5.28F, 1.43F, .64F, -.89F)
        };
        Builder cloak = new Builder();
        for (int row = 0; row < rows.length - 1; row++) {
            for (int column = 0; column < rows[row].length - 1; column++) {
                Point a = rows[row][column];
                Point b = rows[row + 1][column];
                Point c = rows[row + 1][column + 1];
                Point d = rows[row][column + 1];
                cloak.quad(guard ? TRIM : FABRIC, a, d, c, b);
                cloak.quad(guard ? SHELL : SHADE, offset(a, -.12F), offset(b, -.12F),
                        offset(c, -.12F), offset(d, -.12F));
            }
        }
        body.mesh(cloak);
        if (guard) {
            for (int side : SIDES) {
                body.angle(TRIM, side * 3.89F, 1.09F, -1.24F, 2.74F, 1.75F, 3.2F,
                        -.14F, 0, -side * .24F);
            }
        }
    }

    static void jetpack(Part body, boolean boba) {
        Builder pack = new Builder().rigid();
        Material housing = boba ? SHELL : TRIM;
        Material collar = boba ? MARK : METAL;
        loft(pack, SHADE, 0, THRUSTER_SEGMENTS, .58F,
                ring(.13F, 2.22F, .70F, 2.64F), ring(.79F, 3.21F, .92F, 2.76F),
                ring(8.83F, 3.16F, 1.00F, 2.80F), ring(9.69F, 2.62F, .83F, 2.69F));
        loft(pack, housing, 0, THRUSTER_SEGMENTS, .42F,
                ring(.24F, 1.60F, .66F, 4.24F), ring(.83F, 2.59F, 1.55F, 4.45F),
                ring(2.05F, 3.07F, 1.90F, 4.48F), ring(7.52F, 2.94F, 1.96F, 4.61F),
                ring(9.24F, 2.49F, 1.58F, 4.56F), ring(9.52F, 1.77F, .84F, 4.35F));
        loft(pack, SHADE, 0, THRUSTER_SEGMENTS, .49F,
                ring(2.48F, 1.57F, .28F, 6.35F), ring(3.08F, 2.03F, .46F, 6.48F),
                ring(7.00F, 1.88F, .50F, 6.54F), ring(7.91F, 1.39F, .19F, 6.42F));
        loft(pack, METAL, 0, THRUSTER_SEGMENTS, 1,
                ring(-2.59F, .58F, .58F, 4.83F), ring(-1.62F, .91F, .91F, 4.83F),
                ring(4.30F, .96F, .96F, 4.83F), ring(4.91F, 1.19F, 1.19F, 4.83F));
        loft(pack, boba ? SIGNAL : SHELL, 0, THRUSTER_SEGMENTS, 1,
                ring(-4.85F, .06F, .06F, 4.83F), ring(-4.39F, .33F, .33F, 4.83F),
                ring(-3.43F, .64F, .64F, 4.83F), ring(-2.54F, .82F, .82F, 4.83F));

        for (int side : SIDES) {
            float x = 3.27F * side;
            loft(pack, housing, x, THRUSTER_SEGMENTS, 1,
                    ring(.69F, .38F, .45F, 4.44F), ring(1.29F, .89F, .98F, 4.45F),
                    ring(2.45F, 1.25F, 1.32F, 4.46F), ring(7.95F, 1.25F, 1.34F, 4.56F),
                    ring(8.64F, .92F, 1.04F, 4.57F));
            loft(pack, collar, x, THRUSTER_SEGMENTS, 1,
                    ring(2.14F, 1.16F, 1.24F, 4.45F), ring(2.45F, 1.32F, 1.42F, 4.46F),
                    ring(3.21F, 1.33F, 1.43F, 4.48F), ring(3.51F, 1.26F, 1.35F, 4.49F));
            loft(pack, METAL, x, THRUSTER_SEGMENTS, 1,
                    ring(8.03F, .91F, .98F, 4.56F), ring(8.81F, .91F, .98F, 4.56F),
                    ring(9.88F, 1.34F, 1.44F, 4.57F), ring(10.27F, 1.33F, 1.43F, 4.57F),
                    ring(10.27F, .96F, .99F, 4.57F));
            loft(pack, SHADE, x, THRUSTER_SEGMENTS, 1,
                    ring(10.27F, .96F, .99F, 4.57F), ring(9.32F, .61F, .66F, 4.57F),
                    ring(9.12F, .07F, .07F, 4.57F));
        }
        body.mesh(pack);
        for (int grille = 0; grille < 4; grille++) {
            body.box(collar, 0, 3.16F + grille * .81F, 7.03F, 2.45F - grille * .14F, .26F, .35F);
        }
    }

    private static Point[] capeRow(float y, float width, float back, float wrap, float folds, float scallop) {
        Point[] points = new Point[17];
        for (int i = 0; i < points.length; i++) {
            float across = 2F * i / (points.length - 1) - 1F;
            float edge = across * across;
            float wave = (float) Math.cos(across * 10.8F + .06F * y);
            points[i] = point(width * across, y + scallop * edge + .08F * wave, back - wrap * edge + folds * wave);
        }
        return points;
    }

    private static Point[] hoodOutline(float width, float height, float z, float peak, float fold) {
        Point[] points = new Point[HOOD_SEGMENTS + 1];
        for (int i = 0; i <= HOOD_SEGMENTS; i++) {
            double angle = Math.toRadians(-153 + 306D * i / HOOD_SEGMENTS);
            float crease = (float) Math.cos(5 * angle);
            float x = (width + fold * crease) * signedPower(Math.sin(angle), .50F);
            float cos = (float) Math.cos(angle);
            float y = -3.87F - (height + fold * crease) * signedPower(cos, .57F) - peak * Math.max(cos, 0);
            float depth = z - peak * Math.max(0, cos) * 1.7F + fold * (1 - crease);
            points[i] = point(x, y, depth);
        }
        return points;
    }

    private static void face(Builder builder, Material material, int side, Point a, Point b, Point c, Point d) {
        if (side > 0) {
            builder.quad(material, a, b, c, d);
        } else {
            builder.quad(material, mirror(d), mirror(c), mirror(b), mirror(a));
        }
    }

    private static Point mirror(Point vertex) {
        return point(-vertex.x(), vertex.y(), vertex.z());
    }

    private static Point offset(Point vertex, float z) {
        return point(vertex.x(), vertex.y(), vertex.z() + z);
    }

    private static Point point(float x, float y, float z) {
        return new Point(x, y, z);
    }

    private static void loft(Builder builder, Material material, float x, int sides, float curvature, Ring... rings) {
        Point[] above = ringPoints(rings[0], x, sides, curvature);
        for (int i = 1; i < rings.length; i++) {
            Point[] below = ringPoints(rings[i], x, sides, curvature);
            builder.strip(material, above, below, true);
            above = below;
        }
    }

    private static Point[] ringPoints(Ring ring, float x, int sides, float curvature) {
        Point[] points = new Point[sides];
        for (int i = 0; i < sides; i++) {
            double angle = i * 2 * Math.PI / sides;
            double cos = Math.cos(angle);
            points[i] = point(x + signedPower(Math.sin(angle), curvature) * ring.width(),
                    ring.y() - (float) (.5 + .5 * cos) * ring.frontLift(),
                    ring.z() - signedPower(cos, curvature) * ring.depth());
        }
        return points;
    }

    private static float signedPower(double value, float exponent) {
        return (float) Math.copySign(Math.pow(Math.abs(value), exponent), value);
    }

    private static Ring ring(float y, float width, float depth, float z) {
        return new Ring(y, width, depth, z, 0);
    }

    private record Ring(float y, float width, float depth, float z, float frontLift) {}
}
