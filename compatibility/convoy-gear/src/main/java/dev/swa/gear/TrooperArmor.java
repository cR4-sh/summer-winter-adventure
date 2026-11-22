package dev.swa.gear;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

final class TrooperArmor {
    private static final int CERAMIC = 0;
    private static final int GRAPHITE = 48;
    private static final int GREY = 96;
    private static final int GLASS = 144;
    private static final int VENT = 192;
    private static final int STEEL = 240;
    private static final int WIDTH = 288;
    private static final int HEIGHT = 32;

    static HumanoidModel<LivingEntity> model(boolean inner) {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        helmet(root);
        torso(root);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        for (int side : new int[] {-1, 1}) {
            arm(root, side);
            leg(root, side, inner);
        }
        return new HumanoidModel<>(LayerDefinition.create(mesh, WIDTH, HEIGHT).bakeRoot());
    }

    private static void helmet(PartDefinition root) {
        var helmet = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(GRAPHITE, 0).addBox(-4.14F, -8.13F, -4.17F, 8.28F, 8.19F, 8.35F)
                .texOffs(CERAMIC, 0).addBox(-4.36F, -7.93F, -2.35F, 8.72F, 7.09F, 6.86F)
                .texOffs(CERAMIC, 0).addBox(-4.57F, -8.39F, -3.76F, 9.14F, 1.94F, 7.59F)
                .texOffs(CERAMIC, 0).addBox(-3.72F, -8.84F, -3.65F, 7.44F, 1.17F, 7.30F)
                .texOffs(CERAMIC, 0).addBox(-2.83F, -9.05F, -2.78F, 5.66F, 0.63F, 5.63F)
                .texOffs(CERAMIC, 0).addBox(-4.13F, -1.36F, -2.46F, 8.26F, 1.37F, 6.52F)
                .texOffs(GRAPHITE, 0).addBox(-4.29F, -6.64F, -4.67F, 8.58F, 0.39F, 0.64F)
                .texOffs(CERAMIC, 0).addBox(-2.52F, -4.28F, -4.69F, 5.04F, 3.88F, 1.34F)
                .texOffs(CERAMIC, 0).addBox(-0.53F, -6.21F, -4.77F, 1.06F, 1.95F, 0.84F)
                .texOffs(CERAMIC, 0).addBox(-0.88F, -4.61F, -5.08F, 1.76F, 1.18F, 0.79F)
                .texOffs(CERAMIC, 0).addBox(-2.00F, -0.87F, -4.70F, 4.00F, 0.92F, 1.39F)
                .texOffs(GRAPHITE, 0).addBox(-1.58F, -2.69F, -5.15F, 3.16F, 0.85F, 0.82F)
                .texOffs(STEEL, 0).addBox(-0.45F, -1.49F, -5.09F, 0.90F, 0.58F, 0.56F),
                PartPose.ZERO);
        child(helmet, "forehead", 0, -7.67F, -4.22F, -0.23F, 0, 0,
                CubeListBuilder.create().texOffs(CERAMIC, 0)
                        .addBox(-3.96F, -0.82F, -0.43F, 7.92F, 1.97F, 1.64F));
        for (int side : new int[] {-1, 1}) {
            child(helmet, "temple_" + side, side * 3.95F, -7.66F, -3.39F, -0.10F,
                    -side * 0.30F, -side * 0.21F, CubeListBuilder.create()
                            .texOffs(CERAMIC, 0).addBox(-0.58F, -0.54F, -0.72F, 1.16F, 2.14F, 1.80F)
                            .texOffs(GRAPHITE, 0).addBox(-0.62F, 1.15F, -0.74F, 1.24F, 0.35F, 1.18F));

            child(helmet, "eye_" + side, side * 2.14F, -5.52F, -4.55F, 0.05F,
                    -side * 0.20F, -side * 0.16F, CubeListBuilder.create()
                            .texOffs(CERAMIC, 0).addBox(-1.89F, -0.70F, -0.22F, 3.78F, 1.49F, 0.88F)
                            .texOffs(GRAPHITE, 0).addBox(-1.71F, -0.46F, -0.39F, 3.42F, 0.82F, 0.30F)
                            .texOffs(GRAPHITE, 0).addBox(-1.42F, 0.16F, -0.39F, 2.84F, 0.47F, 0.33F)
                            .texOffs(GLASS, 0).addBox(-1.53F, -0.28F, -0.435F, 3.06F, 0.48F, 0.19F)
                            .texOffs(GLASS, 0).addBox(-1.22F, 0.18F, -0.435F, 2.44F, 0.27F, 0.20F));

            child(helmet, "cheek_skirt_" + side, side * 4.21F, -2.94F, -2.89F, 0.08F,
                    -side * 0.13F, -side * 0.07F, CubeListBuilder.create()
                            .texOffs(CERAMIC, 0).addBox(-0.40F, -1.52F, -1.24F, 0.80F, 3.15F, 2.72F));
            child(helmet, "cheek_" + side, side * 3.29F, -3.27F, -4.19F, 0.31F,
                    -side * 0.33F, side * 0.12F, CubeListBuilder.create()
                            .texOffs(GRAPHITE, 0).addBox(-1.14F, -0.85F, -0.49F, 2.28F, 1.75F, 1.16F)
                            .texOffs(CERAMIC, 0).addBox(-1.15F, -1.14F, -0.68F, 2.30F, 1.27F, 1.04F)
                            .texOffs(CERAMIC, 0).addBox(-0.87F, -0.37F, -0.64F, 1.74F, 0.77F, 0.80F));

            child(helmet, "frown_" + side, side * 1.43F, -2.41F, -4.98F, 0,
                    -side * 0.16F, -side * 0.36F, CubeListBuilder.create()
                            .texOffs(GRAPHITE, 0).addBox(-1.19F, -0.38F, -0.32F, 2.38F, 0.76F, 0.54F)
                            .texOffs(STEEL, 0).addBox(-0.72F, -0.31F, -0.38F, 0.31F, 0.55F, 0.15F)
                            .texOffs(STEEL, 0).addBox(-0.18F, -0.31F, -0.38F, 0.31F, 0.55F, 0.15F)
                            .texOffs(STEEL, 0).addBox(0.36F, -0.31F, -0.38F, 0.31F, 0.55F, 0.15F));

            ventTube(helmet, side);
            child(helmet, "aerator_" + side, side * 1.49F, -0.98F, -4.96F, 0.07F,
                    -side * 0.11F, 0, CubeListBuilder.create()
                            .texOffs(STEEL, 0).addBox(-0.58F, -0.62F, -0.31F, 1.16F, 1.24F, 0.60F)
                            .texOffs(STEEL, 0).addBox(-0.71F, -0.43F, -0.32F, 1.42F, 0.86F, 0.61F)
                            .texOffs(GRAPHITE, 0).addBox(-0.44F, -0.43F, -0.43F, 0.88F, 0.86F, 0.23F)
                            .texOffs(GRAPHITE, 0).addBox(-0.53F, -0.27F, -0.45F, 1.06F, 0.54F, 0.24F)
                            .texOffs(STEEL, 0).addBox(-0.27F, -0.05F, -0.49F, 0.54F, 0.12F, 0.12F));

            child(helmet, "ear_" + side, side * 4.37F, -4.84F, 0.08F, -0.10F,
                    side * 0.12F, -side * 0.06F, CubeListBuilder.create()
                            .texOffs(GRAPHITE, 0).addBox(-0.29F, -0.69F, -1.0F, 0.58F, 3.15F, 2.17F)
                            .texOffs(CERAMIC, 0).addBox(-0.42F, -0.46F, -0.79F, 0.84F, 2.71F, 1.74F)
                            .texOffs(CERAMIC, 0).addBox(-0.55F, -0.24F, -0.57F, 1.10F, 1.18F, 1.34F)
                            .texOffs(STEEL, 0).addBox(-0.58F, 0.13F, -0.26F, 1.16F, 0.28F, 0.73F));
        }
    }

    private static void ventTube(PartDefinition helmet, int side) {
        var vents = CubeListBuilder.create()
                .texOffs(CERAMIC, 0).addBox(-1.59F, -0.66F, -0.60F, 3.18F, 1.30F, 1.36F)
                .texOffs(CERAMIC, 0).addBox(-1.63F, -0.47F, -0.80F, 3.26F, 0.91F, 1.74F)
                .texOffs(GRAPHITE, 0).addBox(-1.06F, -0.28F, -0.84F, 2.12F, 0.54F, 0.18F);
        for (int streak = -2; streak <= 2; streak++) {
            vents.texOffs(VENT, 0).addBox(streak * 0.365F - 0.085F, -0.21F, -0.885F, 0.17F, 0.42F, 0.14F);
        }
        child(helmet, "filter_" + side, side * 3.50F, -1.38F, -3.85F, 0,
                -side * 0.57F, -side * 0.05F, vents);
    }

    private static void torso(PartDefinition root) {
        var chest = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(GRAPHITE, 0).addBox(-4.10F, 0.08F, -2.15F, 8.20F, 11.88F, 4.30F)
                .texOffs(CERAMIC, 0).addBox(-3.79F, 0.56F, 1.89F, 7.58F, 8.33F, 0.94F)
                .texOffs(CERAMIC, 0).addBox(-2.42F, 1.86F, 2.70F, 4.84F, 4.71F, 0.70F)
                .texOffs(STEEL, 0).addBox(-1.89F, 2.29F, 3.27F, 3.78F, 2.48F, 0.24F)
                .texOffs(CERAMIC, 0).addBox(-1.26F, 2.69F, 3.43F, 0.87F, 1.55F, 0.23F)
                .texOffs(CERAMIC, 0).addBox(0.20F, 3.04F, 3.39F, 1.31F, 0.83F, 0.23F)
                .texOffs(CERAMIC, 0).addBox(-2.95F, 0.94F, -2.87F, 5.90F, 4.53F, 1.12F)
                .texOffs(CERAMIC, 0).addBox(-3.13F, 5.95F, -2.71F, 6.26F, 3.48F, 1.08F)
                .texOffs(CERAMIC, 0).addBox(-2.39F, 6.81F, -3.04F, 2.67F, 1.51F, 0.54F)
                .texOffs(CERAMIC, 0).addBox(0.63F, 6.92F, -2.97F, 1.48F, 1.25F, 0.45F)
                .texOffs(GREY, 0).addBox(-1.91F, 7.28F, -3.12F, 0.29F, 0.34F, 0.16F)
                .texOffs(STEEL, 0).addBox(-1.37F, 7.31F, -3.11F, 0.26F, 0.27F, 0.16F)
                .texOffs(CERAMIC, 0).addBox(-4.20F, 9.57F, -2.42F, 8.40F, 1.31F, 4.93F)
                .texOffs(CERAMIC, 0).addBox(-3.91F, 9.60F, -2.96F, 7.82F, 1.23F, 0.76F)
                .texOffs(STEEL, 0).addBox(-0.68F, 9.54F, -3.08F, 1.36F, 1.29F, 0.25F)
                .texOffs(CERAMIC, 0).addBox(-0.48F, 9.75F, -3.17F, 0.96F, 0.86F, 0.28F),
                PartPose.ZERO);
        for (int side : new int[] {-1, 1}) {
            child(chest, "chest_flank_" + side, side * 2.44F, 1.28F, -2.29F,
                    -0.13F, -side * 0.22F, -side * 0.105F, CubeListBuilder.create()
                            .texOffs(CERAMIC, 0).addBox(-1.42F, 0, -0.63F, 2.84F, 3.80F, 1.28F));
            child(chest, "belt_case_" + side, side * 2.85F, 9.85F, -2.70F,
                    0, -side * 0.09F, 0, CubeListBuilder.create()
                            .texOffs(CERAMIC, 0).addBox(-0.63F, -0.15F, -0.34F, 1.26F, 1.74F, 0.85F)
                            .texOffs(GREY, 0).addBox(-0.49F, 0.11F, -0.38F, 0.98F, 0.15F, 0.22F));
        }
    }

    private static void arm(PartDefinition root, int side) {
        String name = side == -1 ? "right_arm" : "left_arm";
        float sleeve = side * 0.78F;
        var arm = root.addOrReplaceChild(name, CubeListBuilder.create()
                .texOffs(GRAPHITE, 0).addBox(sleeve - 2.40F, -2.22F, -2.16F, 4.80F, 12.57F, 4.32F)
                .texOffs(CERAMIC, 0).addBox(sleeve - 2.22F, 0.47F, -2.25F, 4.44F, 3.24F, 4.5F)
                .texOffs(CERAMIC, 0).addBox(sleeve - 2.33F, 5.05F, -2.40F, 4.66F, 4.15F, 4.77F)
                .texOffs(CERAMIC, 0).addBox(sleeve - 1.88F, 5.28F, -2.64F, 3.76F, 3.18F, 0.52F)
                .texOffs(CERAMIC, 0).addBox(sleeve - 1.40F, 9.36F, -2.47F, 2.80F, 0.79F, 0.45F),
                PartPose.offset(side * 5F, 2, 0));
        child(arm, "pauldron", side * 1.14F, -1.61F, 0, 0, 0, -side * 0.18F,
                CubeListBuilder.create()
                        .texOffs(CERAMIC, 0).addBox(-2.01F, -0.48F, -2.52F, 4.02F, 2.64F, 5.04F)
                        .texOffs(CERAMIC, 0).addBox(-1.65F, -0.82F, -2.13F, 3.30F, 1.13F, 4.26F));
    }

    private static void leg(PartDefinition root, int side, boolean inner) {
        String name = side == -1 ? "right_leg" : "left_leg";
        float center = side * 0.11F;
        var shell = CubeListBuilder.create();
        if (inner) {
            shell.texOffs(GRAPHITE, 0).addBox(-2.03F, 0, -2.04F, 4.06F, 8.89F, 4.08F)
                    .texOffs(CERAMIC, 0).addBox(center - 1.99F, 0.42F, -2.38F, 3.98F, 4.78F, 4.59F)
                    .texOffs(CERAMIC, 0).addBox(center - 1.68F, 0.74F, -2.64F, 3.36F, 3.88F, 0.51F);
        } else {
            shell.texOffs(GRAPHITE, 0).addBox(-2.00F, 6.31F, -2.02F, 4.00F, 5.80F, 4.04F)
                    .texOffs(CERAMIC, 0).addBox(center - 1.99F, 6.70F, -2.26F, 3.98F, 3.82F, 4.37F)
                    .texOffs(CERAMIC, 0).addBox(center - 1.58F, 6.48F, -2.67F, 3.16F, 3.70F, 0.63F)
                    .texOffs(CERAMIC, 0).addBox(center - 2.02F, 10.32F, -3.01F, 4.04F, 1.31F, 5.20F)
                    .texOffs(GRAPHITE, 0).addBox(center - 2.08F, 11.59F, -3.09F, 4.16F, 0.44F, 5.36F);
            if (side == 1) {
                shell.texOffs(CERAMIC, 0).addBox(center - 1.27F, 5.87F, -2.74F, 2.54F, 1.45F, 0.70F);
            }
        }
        root.addOrReplaceChild(name, shell, PartPose.offset(side * 1.9F, 12, 0));
    }

    private static void child(PartDefinition parent, String name, float x, float y, float z,
            float xRot, float yRot, float zRot, CubeListBuilder geometry) {
        parent.addOrReplaceChild(name, geometry, PartPose.offsetAndRotation(x, y, z, xRot, yRot, zRot));
    }
}
