package dev.swa.gear;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.world.entity.LivingEntity;

final class ArmorModels {
    enum Material { FABRIC, SHELL, TRIM, SHADE, MARK, VISOR, METAL, SIGNAL }

    static final int MATERIAL_WIDTH = 64;
    static final int ATLAS_HEIGHT = 48;
    static final int ATLAS_WIDTH = Material.values().length * MATERIAL_WIDTH;
    private static final int[] SIDES = {-1, 1};

    private ArmorModels() {}

    static HumanoidModel<LivingEntity> model(StarWarsArmor.Set set, boolean leggings) {
        var parts = new LinkedHashMap<String, ModelPart>();
        Part head = new Part();
        Part body = new Part();
        if (leggings) {
            waist(body, set);
        } else {
            helmet(head, set);
            chest(body, set);
        }
        parts.put("head", head.bake(PartPose.ZERO));
        parts.put("body", body.bake(PartPose.ZERO));
        parts.put("hat", new Part().bake(PartPose.ZERO));

        for (int side : SIDES) {
            String prefix = side < 0 ? "right_" : "left_";
            Part arm = new Part();
            Part leg = new Part();
            if (leggings) {
                thigh(leg, set, side);
            } else {
                sleeve(arm, set, side);
                boot(leg, set, side);
            }
            parts.put(prefix + "arm", arm.bake(PartPose.offset(side * 5F, 2, 0)));
            parts.put(prefix + "leg", leg.bake(PartPose.offset(side * 1.9F, 12, 0)));
        }

        return new HumanoidModel<>(new ModelPart(List.of(), parts));
    }

    private static void helmet(Part head, StarWarsArmor.Set set) {
        if (set != StarWarsArmor.Set.JEDI && set != StarWarsArmor.Set.SITH && set != StarWarsArmor.Set.REBEL_PILOT
                && set != StarWarsArmor.Set.VADER && set != StarWarsArmor.Set.ROYAL_GUARD) {
            head.box(Material.SHADE, 0, -8.04F, 0, 8.13F, 8.13F, 8.12F);
        }
        switch (set) {
            case MANDALORIAN, BOBA_FETT -> mandalorianHelmet(head, set == StarWarsArmor.Set.BOBA_FETT);
            case CLONE_TROOPER -> cloneHelmet(head);
            case DEATH_TROOPER -> deathTrooperHelmet(head);
            case SANDTROOPER -> sandtrooperHelmet(head);
            case SCOUT -> scoutHelmet(head);
            case SNOWTROOPER -> snowtrooperHelmet(head);
            case VADER -> FeaturedArmor.vaderHelmet(head);
            case INQUISITOR -> inquisitorHelmet(head);
            case ROYAL_GUARD -> FeaturedArmor.guardHelmet(head);
            case REBEL_PILOT -> pilotHelmet(head);
            case JEDI, SITH -> FeaturedArmor.hood(head, set == StarWarsArmor.Set.SITH);
            case TEMPLE_GUARD, SCAVENGER -> hood(head, set);
            case SCRAP, PLASTOID -> utilityHelmet(head, set == StarWarsArmor.Set.SCRAP);
            case STORMTROOPER -> throw new IllegalArgumentException("Stormtrooper uses the original Convoy model");
        }
    }

    private static void shell(Part head, float width, float top, float depth) {
        head.box(Material.SHELL, 0, top + .84F, .9F, width, 6.85F, depth - 1.32F)
            .box(Material.SHELL, 0, top, -.14F, width - 1.32F, 1.34F, depth - 1.22F)
            .box(Material.SHELL, 0, top + .68F, -3.72F, width - .76F, 2.14F, 1.36F)
            .box(Material.TRIM, 0, top - .33F, -.35F, width - 3.25F, .47F, depth - 3.06F);
        for (int side : SIDES) {
            head.angle(Material.SHELL, side * (width / 2 - .52F), top + .92F, .64F,
                    1.5F, 1.4F, depth - 1.84F, 0, 0, side * .38F);
        }
    }

    private static void mandalorianHelmet(Part head, boolean boba) {
        shell(head, 8.83F, -8.75F, 9.05F);
        head.box(Material.SHADE, 0, -6.57F, -4.27F, 8.04F, 6.33F, .83F)
            .box(Material.MARK, 0, -6.76F, -4.66F, 8.11F, 2.09F, .57F)
            .box(Material.VISOR, 0, -6.17F, -5.00F, 7.42F, 1.02F, .28F)
            .box(Material.MARK, 0, -5.31F, -4.80F, 2.2F, 5.08F, .54F)
            .box(Material.VISOR, 0, -5.39F, -5.047F, 1.30F, 4.85F, .25F);
        for (int side : SIDES) {
            head.angle(Material.TRIM, side * 2.84F, -2.68F, -4.26F, 2.75F, 3.69F, .82F,
                    -.12F, -side * .28F, -side * .18F)
                .angle(Material.SHELL, side * 3.28F, -3.24F, -4.75F, 1.39F, 2.74F, .47F,
                    -.10F, -side * .31F, -side * .20F)
                .box(Material.METAL, side * 4.46F, -6.58F, -.11F, .62F, 3.88F, 1.72F);
        }
        if (boba) {
            head.box(Material.METAL, -4.72F, -10.15F, -.48F, .27F, 5.56F, .34F)
                .box(Material.SHADE, -4.72F, -10.21F, -.94F, .94F, .52F, 1.67F)
                .angle(Material.METAL, 2.1F, -7.48F, -4.40F, .72F, .44F, .16F,
                        0, 0, -.35F);
        }
        head.box(Material.SHADE, 0, -3.54F, 4.42F, 3.05F, 2.3F, .48F);
        for (int grille = 0; grille < 3; grille++) {
            head.box(Material.METAL, 0, -3.29F + grille * .67F, 4.68F, 2.47F, .18F, .17F);
        }
    }

    private static void cloneHelmet(Part head) {
        shell(head, 8.80F, -8.62F, 8.83F);
        head.angle(Material.SHELL, 0, -8.13F, .52F, .85F, 2.23F, 6.90F, -.09F, 0, 0)
            .box(Material.SHELL, 0, -6.65F, -4.32F, 8.48F, 1.26F, .96F)
            .box(Material.VISOR, 0, -5.76F, -4.73F, 7.80F, .77F, .36F)
            .box(Material.VISOR, 0, -5.27F, -4.58F, 1.51F, 1.96F, .73F)
            .box(Material.TRIM, 0, -2.64F, -4.77F, 3.31F, 2.04F, 1.08F)
            .box(Material.SHADE, 0, -1.88F, -5.36F, 2.13F, .66F, .22F);
        for (int side : SIDES) {
            head.angle(Material.SHELL, side * 3.05F, -3.02F, -4.15F, 2.80F, 3.54F, 1.56F,
                    -.04F, -side * .32F, side * .18F)
                .box(Material.TRIM, side * 4.33F, -5.48F, -.41F, .76F, 3.35F, 1.86F)
                .box(Material.MARK, side * 2.8F, -1.69F, -4.41F, 1.2F, .26F, .23F);
        }
    }

    private static void deathTrooperHelmet(Part head) {
        shell(head, 8.70F, -8.86F, 9.09F);
        head.box(Material.SHADE, 0, -6.47F, -4.2F, 7.66F, 6.42F, 1.05F)
            .box(Material.SHELL, 0, -6.91F, -4.46F, 7.38F, 1.37F, .83F)
            .box(Material.TRIM, 0, -3.86F, -4.95F, 3.42F, 3.49F, 1.02F)
            .box(Material.SHADE, 0, -2.15F, -5.51F, 2.68F, 1.20F, .43F);
        for (int side : SIDES) {
            head.angle(Material.VISOR, side * 1.79F, -5.13F, -4.83F,
                        3.60F, .64F, .50F, 0, -side * .08F, -side * .11F)
                .angle(Material.SHELL, side * 3.13F, -2.14F, -4.20F,
                        2.36F, 3.73F, 1.56F, .09F, -side * .29F, side * .14F)
                .box(Material.SIGNAL, side * 1.46F, -1.43F, -5.59F, .46F, .40F, .35F)
                .box(Material.TRIM, side * 4.30F, -5.74F, -.7F, 1.14F, 3.63F, 1.51F);
        }
    }

    private static void sandtrooperHelmet(Part head) {
        shell(head, 9.05F, -8.79F, 8.95F);
        head.box(Material.SHELL, 0, -7.18F, -4.24F, 8.35F, 1.03F, 1.18F)
            .box(Material.SHELL, 0, -3.94F, -4.45F, 5.86F, 3.87F, 1.50F)
            .box(Material.TRIM, 0, -2.83F, -5.21F, 4.21F, .90F, .50F)
            .box(Material.SHADE, 0, -2.57F, -5.53F, 2.77F, .37F, .21F);
        for (int side : SIDES) {
            head.angle(Material.VISOR, side * 2.08F, -5.57F, -4.64F, 3.73F, 1.10F, .38F,
                        0, -side * .13F, -side * .15F)
                .angle(Material.SHELL, side * 3.25F, -3.18F, -4.25F, 2.44F, 2.40F, 1.38F,
                        .24F, -side * .36F, -side * .08F)
                .angle(Material.SHELL, side * 3.14F, -1.36F, -4.46F, 3.14F, 1.35F, 1.42F,
                        0, -side * .43F, 0)
                .box(Material.METAL, side * 1.44F, -1.49F, -5.57F, .99F, .96F, .39F);
        }
    }

    private static void scoutHelmet(Part head) {
        shell(head, 9.02F, -8.81F, 8.81F);
        head.box(Material.SHELL, 0, -6.91F, -4.96F, 8.65F, 1.15F, 2.02F)
            .box(Material.VISOR, 0, -5.92F, -4.55F, 7.82F, 2.73F, .70F)
            .angle(Material.SHELL, 0, -3.26F, -4.97F, 4.80F, 2.05F, 2.28F, -.22F, 0, 0)
            .box(Material.TRIM, 0, -3.06F, -6.19F, 3.09F, 1.23F, .69F)
            .box(Material.SHADE, 0, -2.91F, -6.58F, 2.24F, .53F, .25F);
        for (int side : SIDES) {
            head.angle(Material.SHELL, side * 4.01F, -3.99F, -4.33F, 1.41F, 4.5F, 2.9F,
                    -.1F, -side * .12F, -side * .14F);
        }
    }

    private static void snowtrooperHelmet(Part head) {
        shell(head, 9.11F, -8.86F, 9.25F);
        head.box(Material.FABRIC, 0, -5.2F, -.4F, 8.72F, 6.7F, 8.80F)
            .box(Material.SHELL, 0, -7.12F, -4.45F, 8.71F, 1.36F, 1.51F)
            .box(Material.VISOR, 0, -5.94F, -4.66F, 6.79F, .63F, .36F)
            .box(Material.TRIM, 0, -5.3F, -4.48F, 6.29F, 5.05F, .68F)
            .box(Material.SHELL, 0, -4.69F, -4.88F, 1.34F, 2.88F, .76F);
        for (int side : SIDES) {
            head.angle(Material.SHELL, side * 3.78F, -3.13F, -4.29F, 1.53F, 5.85F, 1.50F,
                        0, -side * .21F, -side * .11F)
                .angle(Material.FABRIC, side * 2.08F, -1.67F, -4.82F, .75F, 3.82F, .48F,
                        .07F, 0, -side * .10F);
        }
    }

    private static void inquisitorHelmet(Part head) {
        shell(head, 8.88F, -8.68F, 8.59F);
        head.box(Material.SHADE, 0, -6.64F, -4.15F, 7.85F, 6.59F, 1.10F)
            .box(Material.TRIM, 0, -4.51F, -4.8F, 5.42F, 4.58F, .68F)
            .box(Material.MARK, 0, -7.54F, -4.32F, .48F, 2.03F, .45F)
            .box(Material.VISOR, 0, -5.80F, -4.77F, 7.07F, .60F, .35F);
        for (int side : SIDES) {
            head.angle(Material.SHELL, side * 3.67F, -3.17F, -4.10F, 1.63F, 4.57F, 1.33F,
                    0, -side * .25F, side * .18F);
        }
    }

    private static void pilotHelmet(Part head) {
        shell(head, 8.86F, -8.64F, 8.57F);
        head.box(Material.SHELL, 0, -7.46F, -3.96F, 8.68F, 1.45F, 1.39F)
            .box(Material.MARK, 0, -7.84F, -4.18F, 2.28F, 1.89F, .77F)
            .angle(Material.VISOR, 0, -4.91F, -4.56F, 7.02F, 2.12F, .44F, -.16F, 0, 0);
        for (int side : SIDES) {
            head.angle(Material.SHELL, side * 3.89F, -3.10F, -2.90F, 1.50F, 4.80F, 3.63F,
                        .1F, -side * .10F, -side * .09F)
                .box(Material.MARK, side * 2.88F, -7.01F, -4.76F, 1.15F, .94F, .34F)
                .box(Material.METAL, side * 4.52F, -5.32F, -.19F, .76F, 2.26F, 2.49F)
                .angle(Material.SHADE, side * 2.81F, -.79F, -4.13F, 2.77F, .46F, .40F,
                        0, -side * .37F, 0);
        }
    }

    private static void hood(Part head, StarWarsArmor.Set set) {
        head.box(Material.FABRIC, 0, -7.68F, 1.48F, 8.80F, 7.80F, 5.92F)
            .angle(Material.SHELL, 0, -7.63F, -1.23F, 8.21F, 1.96F, 6.81F, -.11F, 0, 0)
            .angle(Material.TRIM, 0, -7.09F, -4.39F, 7.58F, .83F, 1.51F, -.33F, 0, 0);
        for (int side : SIDES) {
            head.angle(Material.SHELL, side * 3.78F, -3.56F, -2.92F, 1.89F, 6.89F, 3.7F,
                    .07F, -side * .17F, -side * .18F)
                .angle(Material.FABRIC, side * 4.12F, -2.78F, .19F, 1.19F, 5.6F, 3.43F,
                    .09F, 0, side * .14F);
        }
        head.box(Material.SHADE, 0, -5.95F, -4.04F, 6.67F, 5.87F, .70F);

        switch (set) {
            case TEMPLE_GUARD -> {
                head.angle(Material.TRIM, 0, -3.26F, -4.56F, 6.06F, 5.18F, .79F, -.13F, 0, 0)
                    .box(Material.MARK, 0, -5.65F, -5.0F, 1.13F, 5.05F, .37F)
                    .box(Material.VISOR, 0, -4.82F, -5.14F, 5.82F, .42F, .21F);
                for (int side : SIDES) {
                    head.angle(Material.MARK, side * 2.25F, -2.24F, -4.99F, 1.28F, 2.13F, .34F,
                            .04F, -side * .2F, side * .19F);
                }
            }
            case SCAVENGER -> {
                head.box(Material.TRIM, 0, -3.14F, -4.39F, 5.82F, 2.81F, .83F);
                for (int side : SIDES) {
                    head.angle(Material.METAL, side * 1.69F, -4.71F, -4.40F, 2.96F, 1.93F, .74F,
                                0, -side * .17F, -side * .08F)
                        .angle(Material.VISOR, side * 1.69F, -4.71F, -4.86F, 2.34F, 1.38F, .31F,
                                0, -side * .17F, -side * .08F);
                }
            }
            default -> { }
        }
    }

    private static void utilityHelmet(Part head, boolean scrap) {
        shell(head, 8.78F, -8.55F, 8.60F);
        head.box(Material.SHADE, 0, -6.21F, -4.27F, 7.28F, 6.23F, .83F)
            .box(Material.SHELL, 0, -6.93F, -4.57F, 8.44F, .95F, 1.47F)
            .box(Material.VISOR, 0, -5.88F, -4.84F, 6.97F, 1.78F, .41F)
            .box(Material.SHELL, 0, -3.62F, -4.68F, 7.55F, 3.33F, 1.15F);
        for (int side : SIDES) {
            head.angle(Material.TRIM, side * 4.05F, -3.45F, -3.99F, 1.52F, 3.65F, 1.73F,
                    0, -side * .29F, side * .15F);
        }
        if (scrap) {
            head.angle(Material.MARK, 2.8F, -3.92F, -5.15F, 2.57F, 5.31F, .76F, 0, -.28F, -.21F)
                .box(Material.METAL, 3.2F, -1.88F, -5.69F, .47F, .66F, .29F);
        } else {
            head.box(Material.MARK, 0, -2.61F, -5.32F, 4.46F, .46F, .22F)
                .box(Material.METAL, 0, -2.02F, -5.31F, 2.91F, .75F, .25F);
        }
    }

    private static void chest(Part body, StarWarsArmor.Set set) {
        body.box(Material.FABRIC, 0, -.02F, 0, 8.18F, 12.05F, 4.22F);
        switch (set) {
            case MANDALORIAN, BOBA_FETT -> {
                for (int side : SIDES) {
                    body.angle(Material.SHELL, side * 1.99F, 2.84F, -2.44F, 3.44F, 3.74F, .82F,
                                -.06F, -side * .13F, side * .15F)
                        .angle(Material.TRIM, side * 1.81F, 5.80F, -2.36F, 3.15F, 1.6F, .65F,
                                0, -side * .12F, -side * .12F);
                }
                body.angle(Material.METAL, 0, 3.52F, -2.92F, .93F, .93F, .39F, 0, 0, .79F);
                FeaturedArmor.jetpack(body, set == StarWarsArmor.Set.BOBA_FETT);
                belt(body, Material.SHADE);
            }
            case VADER, INQUISITOR -> {
                if (set == StarWarsArmor.Set.VADER) {
                    FeaturedArmor.vaderCollar(body);
                } else {
                    body.box(Material.SHELL, 0, .18F, 0, 8.43F, 2.53F, 4.72F);
                }
                body.box(Material.SHADE, 0, 3.3F, -2.49F, 4.01F, 3.88F, .78F)
                    .box(Material.METAL, 0, 3.70F, -2.93F, 3.57F, .46F, .28F)
                    .box(Material.MARK, -.85F, 4.72F, -2.96F, .76F, .91F, .31F)
                    .box(Material.SIGNAL, .76F, 4.72F, -2.96F, .76F, .91F, .31F);
                for (int stripe = -3; stripe <= 3; stripe++) {
                    body.box(Material.TRIM, stripe * .86F, 7.15F, -2.26F, .31F, 2.95F, .22F);
                }
                if (set == StarWarsArmor.Set.VADER) FeaturedArmor.cape(body, false);
                belt(body, Material.SHADE);
            }
            case ROYAL_GUARD -> FeaturedArmor.guardRobe(body);
            case JEDI, SITH, TEMPLE_GUARD, SCAVENGER -> robe(body, set);
            case REBEL_PILOT -> {
                body.box(Material.TRIM, 0, 1.8F, 0, 7.7F, 7.09F, 4.58F)
                    .box(Material.SHADE, .35F, 4.65F, -2.61F, 3.88F, 3.05F, 1.16F)
                    .box(Material.METAL, .35F, 4.98F, -3.27F, 2.99F, 1.88F, .38F)
                    .box(Material.SIGNAL, .87F, 5.24F, -3.52F, 1.36F, .42F, .21F)
                    .angle(Material.SHADE, -2.20F, 5.87F, -2.60F, .52F, 5.96F, .67F, -.09F, 0, -.35F)
                    .angle(Material.SHADE, 2.33F, 9.02F, -2.37F, .74F, 5.05F, .54F, 0, 0, -.84F);
                belt(body, Material.SHADE);
            }
            case SCRAP -> {
                body.angle(Material.SHELL, -1.31F, 3.67F, -2.32F, 5.24F, 6.3F, .93F, -.12F, .07F, .17F)
                    .angle(Material.MARK, 2.76F, 2.81F, -2.62F, 2.47F, 4.53F, .85F, .08F, -.23F, -.20F)
                    .angle(Material.TRIM, 2.19F, 7.58F, -2.48F, 3.30F, 1.67F, .77F, .04F, -.19F, .23F)
                    .box(Material.METAL, -2.43F, 3.35F, -2.93F, .52F, .55F, .29F)
                    .box(Material.SHADE, -2.84F, 2.37F, 2.58F, 2.17F, 5.34F, 1.55F);
                belt(body, Material.SHADE);
            }
            default -> troopChest(body, set);
        }
    }

    private static void troopChest(Part body, StarWarsArmor.Set set) {
        body.box(Material.TRIM, 0, .41F, 2.13F, 7.57F, 7.93F, 1.19F)
            .box(Material.SHELL, 0, 1.05F, -2.27F, 6.75F, 4.63F, 1.22F)
            .box(Material.SHELL, 0, 6.30F, -2.21F, 6.41F, 2.64F, .91F);
        for (int side : SIDES) {
            body.angle(Material.SHELL, side * 3.13F, 2.85F, -2.20F, 2.39F, 4.53F, .95F,
                        -.12F, -side * .37F, -side * .12F)
                .angle(Material.TRIM, side * 2.79F, 7.27F, -2.20F, 2.21F, 2.53F, .68F,
                        .07F, -side * .32F, 0);
        }
        switch (set) {
            case CLONE_TROOPER ->
                body.angle(Material.MARK, .36F, 2.95F, -2.91F, .48F, 3.95F, .21F, 0, 0, .27F);
            case SNOWTROOPER, SANDTROOPER -> {
                body.box(Material.SHELL, 0, .56F, 3.17F, 7.23F, 8.76F, 2.08F)
                    .box(Material.SHADE, 0, 2.24F, 4.41F, 6.24F, 4.09F, .96F)
                    .box(Material.METAL, -2.08F, 2.88F, 5.01F, 1.15F, 2.80F, .26F)
                    .box(Material.TRIM, 2.04F, 1.55F, -3.03F, 1.81F, 2.22F, .31F);
                if (set == StarWarsArmor.Set.SANDTROOPER) {
                    body.box(Material.SHADE, -2.51F, -2.54F, 3.79F, .31F, 5.43F, .40F)
                        .box(Material.METAL, 0, .32F, 5.12F, 5.51F, 1.06F, 1.18F);
                }
            }
            case DEATH_TROOPER -> body.box(Material.SIGNAL, -2.16F, 2.08F, -2.99F, .96F, .35F, .34F)
                .box(Material.SHADE, -2.64F, 2.53F, -2.96F, 1.58F, 2.38F, .66F);
            case SCOUT -> body.box(Material.SHELL, 0, 2.06F, 2.95F, 3.79F, 4.46F, .93F)
                .box(Material.FABRIC, 0, 6.65F, -2.82F, 5.68F, 2.67F, .71F);
            default -> body.box(Material.MARK, 0, 3.45F, -2.94F, 3.01F, .43F, .22F);
        }
        belt(body, set == StarWarsArmor.Set.SANDTROOPER ? Material.SHELL : Material.SHADE);
    }

    private static void robe(Part body, StarWarsArmor.Set set) {
        body.box(Material.SHELL, 0, .32F, .13F, 8.30F, 10.65F, 4.45F);
        Material tabard = set == StarWarsArmor.Set.TEMPLE_GUARD ? Material.MARK : Material.TRIM;
        for (int side : SIDES) {
            body.angle(tabard, side * 1.79F, 4.13F, -2.19F, 2.47F, 8.47F, .54F,
                    -.02F, -side * .03F, -side * .30F)
                .angle(Material.FABRIC, side * 2.39F, 7.74F, -2.38F, .75F, 5.73F, .39F,
                    .02F, -side * .14F, side * .08F);
        }
        if (set == StarWarsArmor.Set.SITH) {
            shortCape(body);
        }
        if (set == StarWarsArmor.Set.SCAVENGER) {
            body.angle(Material.METAL, 0, 4.2F, -2.63F, .82F, 9.2F, .38F, 0, 0, .53F)
                .box(Material.TRIM, .78F, 2.38F, 2.97F, 4.15F, 5.53F, 1.49F)
                .box(Material.METAL, .26F, 3.44F, 3.86F, 1.91F, 3.57F, .44F);
        }
        belt(body, Material.SHADE);
    }

    private static void shortCape(Part body) {
        body.angle(Material.TRIM, 0, 5.69F, 2.50F, 8.38F, 11.3F, .72F, .06F, 0, 0);
        for (int side : SIDES) {
            body.angle(Material.SHELL, side * 3.39F, 5.28F, 2.78F, 1.69F, 10.49F, .65F,
                    .09F, side * .11F, -side * .10F);
        }
    }

    private static void belt(Part body, Material material) {
        body.box(material, 0, 9.35F, 0, 8.48F, 1.34F, 4.60F)
            .box(Material.METAL, 0, 9.53F, -2.45F, 1.63F, .97F, .52F);
        for (int side : SIDES) {
            body.box(material, side * 2.86F, 9.34F, -2.48F, 1.62F, 1.91F, .78F);
        }
    }

    private static void waist(Part body, StarWarsArmor.Set set) {
        body.box(Material.FABRIC, 0, 10.75F, 0, 8.26F, 1.44F, 4.37F);
        if (isRobe(set) || set == StarWarsArmor.Set.SNOWTROOPER) {
            body.box(Material.TRIM, 0, 10.78F, .15F, 8.16F, 1.23F, 4.41F);
        }
    }

    private static void sleeve(Part arm, StarWarsArmor.Set set, int side) {
        float center = side * .91F;
        arm.box(Material.FABRIC, center, -2.08F, 0, 4.30F, 12.22F, 4.32F);
        if (isRobe(set) || set == StarWarsArmor.Set.REBEL_PILOT) {
            arm.angle(Material.SHELL, center, 1.19F, 0, 4.54F, 6.61F, 4.56F, 0, 0, -side * .05F)
                .angle(Material.TRIM, center, 6.47F, -.22F, 4.27F, 4.92F, 4.68F, .05F, 0, side * .05F)
                .box(Material.SHADE, center, 8.22F, -.07F, 4.48F, 1.46F, 4.62F);
            if (set == StarWarsArmor.Set.REBEL_PILOT && side == -1) {
                arm.box(Material.METAL, center, 3.63F, -2.26F, 1.85F, 2.46F, .76F);
            }
            return;
        }

        boolean rounded = set == StarWarsArmor.Set.VADER || set == StarWarsArmor.Set.DEATH_TROOPER;
        boolean mandalorian = set == StarWarsArmor.Set.MANDALORIAN || set == StarWarsArmor.Set.BOBA_FETT;
        Material shoulder = set == StarWarsArmor.Set.BOBA_FETT || (set == StarWarsArmor.Set.SANDTROOPER && side < 0)
                || (set == StarWarsArmor.Set.SCRAP && side > 0) ? Material.MARK : Material.SHELL;
        float shoulderWidth = rounded ? 4.9F : mandalorian ? 3.81F : 4.51F;
        if (set == StarWarsArmor.Set.VADER) {
            FeaturedArmor.vaderShoulder(arm, center, side);
        } else {
            arm.angle(shoulder, center + side * .28F, -.19F, 0,
                        shoulderWidth, 3.52F, 4.92F, .01F, 0, -side * (rounded ? .23F : .13F))
                .angle(Material.TRIM, center + side * .33F, -.93F, 0,
                        shoulderWidth - .71F, 2.31F, 4.06F, 0, 0, -side * .18F);
        }
        arm.angle(Material.SHELL, center, 2.65F, 0, 4.20F, 2.69F, 4.29F, 0, 0, side * .04F)
            .box(Material.SHELL, center, 5.14F, -.07F, 4.50F, 4.50F, 4.60F)
            .box(Material.TRIM, center, 5.48F, -2.46F, 3.68F, 3.29F, .57F);
        if (mandalorian || set == StarWarsArmor.Set.PLASTOID) {
            arm.box(Material.SHADE, center, 5.80F, -2.82F, 2.02F, 2.32F, .55F)
                .box(Material.SIGNAL, center, 6.06F, -3.14F, 1.26F, .46F, .22F);
        }
        if (set == StarWarsArmor.Set.SANDTROOPER && side < 0) {
            arm.angle(Material.MARK, center + side * .56F, -.91F, -.40F, 5.20F, 3.03F, 5.25F,
                    .13F, side * .1F, -.32F);
        }
    }

    private static void thigh(Part leg, StarWarsArmor.Set set, int side) {
        leg.box(Material.FABRIC, 0, .01F, 0, 4.04F, 6.37F, 4.04F);
        if (set == StarWarsArmor.Set.ROYAL_GUARD) {
            FeaturedArmor.guardSkirt(leg, side);
            return;
        }
        if (isRobe(set) || set == StarWarsArmor.Set.REBEL_PILOT || set == StarWarsArmor.Set.SNOWTROOPER) {
            leg.angle(Material.TRIM, side * .06F, 2.77F, -1.95F, 3.96F, 5.51F, .91F,
                        .09F, -side * .10F, -side * .05F)
                .box(Material.SHELL, side * 1.75F, .54F, .29F, .83F, 4.92F, 3.28F);
            if (set != StarWarsArmor.Set.REBEL_PILOT) {
                leg.angle(Material.SHELL, side * .14F, 3.08F, 2.30F, 3.95F, 6.15F, .73F,
                        -.08F, side * .10F, 0);
            }
            return;
        }

        leg.angle(Material.SHELL, side * .08F, 2.82F, -1.97F, 3.99F, 5.14F, 1.16F,
                    .05F, -side * .12F, side * .06F)
            .angle(Material.TRIM, side * 1.57F, 2.52F, -.67F, 1.11F, 4.59F, 2.45F,
                    -.12F, side * .26F, side * .10F);
        if (set == StarWarsArmor.Set.SCRAP && side == -1) {
            leg.angle(Material.MARK, -.32F, 2.97F, -2.62F, 3.07F, 3.37F, .52F,
                    -.03F, .09F, -.26F);
        }
    }

    private static void boot(Part leg, StarWarsArmor.Set set, int side) {
        boolean soft = isRobe(set) || set == StarWarsArmor.Set.REBEL_PILOT;
        Material upper = soft ? Material.SHADE : Material.SHELL;
        leg.box(Material.FABRIC, 0, 6.16F, 0, 4.07F, 5.88F, 4.08F)
            .angle(upper, side * .05F, 8.63F, -1.94F, 4.10F, 4.85F, 1.21F,
                        -.07F, -side * .08F, 0)
            .box(upper, side * .06F, 9.73F, -.59F, 4.23F, 1.90F, 5.38F)
            .box(Material.SHADE, side * .06F, 11.63F, -.61F, 4.32F, .46F, 5.46F);
        if (soft) {
            for (int strap = 0; strap < 2; strap++) {
                leg.angle(Material.TRIM, 0, 7.42F + strap * 1.67F, -2.42F,
                        3.63F, .46F, .29F, 0, 0, (strap - .5F) * .23F);
            }
        } else {
            Material knee = (set == StarWarsArmor.Set.BOBA_FETT || set == StarWarsArmor.Set.SCRAP)
                    ? Material.MARK : Material.TRIM;
            leg.angle(knee, side * .13F, 6.53F, -2.66F, 3.34F, 1.55F, .75F,
                    .07F, -side * .1F, side * .09F);
        }
    }

    private static boolean isRobe(StarWarsArmor.Set set) {
        return switch (set) {
            case JEDI, SITH, TEMPLE_GUARD, ROYAL_GUARD, SCAVENGER -> true;
            default -> false;
        };
    }

    static final class Part {
        private final CubeListBuilder cubes = CubeListBuilder.create();
        private final List<Panel> panels = new ArrayList<>();
        private final List<ArmorMesh> meshes = new ArrayList<>();

        Part mesh(ArmorMesh.Builder mesh) {
            meshes.add(mesh.build());
            return this;
        }

        // Boxes are centered in X and Z. Y marks the top in the part's local coordinates.
        Part box(Material material, float x, float y, float z, float width, float height, float depth) {
            cubes.texOffs(material.ordinal() * MATERIAL_WIDTH, 0)
                    .addBox(x - width / 2, y, z - depth / 2, width, height, depth);
            return this;
        }

        // Angled panels rotate around their center so joints and seams stay aligned.
        Part angle(Material material, float x, float y, float z, float width, float height, float depth,
                float pitch, float yaw, float roll) {
            CubeListBuilder cube = CubeListBuilder.create().texOffs(material.ordinal() * MATERIAL_WIDTH, 0)
                    .addBox(-width / 2, -height / 2, -depth / 2, width, height, depth);
            panels.add(new Panel(cube, PartPose.offsetAndRotation(x, y, z, pitch, yaw, roll)));
            return this;
        }

        ModelPart bake(PartPose pose) {
            var children = new LinkedHashMap<String, ModelPart>();
            for (int i = 0; i < panels.size(); i++) {
                Panel panel = panels.get(i);
                children.put("panel_" + i, bake(panel.geometry(), List.of(), new LinkedHashMap<>(), panel.pose()));
            }
            return bake(cubes, meshes, children, pose);
        }

        private static ModelPart bake(CubeListBuilder geometry, List<ArmorMesh> meshes,
                LinkedHashMap<String, ModelPart> children, PartPose pose) {
            var all = new ArrayList<ModelPart.Cube>();
            for (var cube : geometry.getCubes()) {
                all.add(cube.bake(ATLAS_WIDTH, ATLAS_HEIGHT));
            }
            all.addAll(meshes);
            var model = new ModelPart(all, children);
            model.setInitialPose(pose);
            model.loadPose(pose);
            return model;
        }
    }

    private record Panel(CubeListBuilder geometry, PartPose pose) {}
}
