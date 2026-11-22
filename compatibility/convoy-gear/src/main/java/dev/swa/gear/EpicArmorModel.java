package dev.swa.gear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.MeshPartDefinition;
import yesman.epicfight.api.client.model.SingleGroupVertexBuilder;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.transformer.HumanoidModelTransformer;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec2f;
import yesman.epicfight.api.utils.math.Vec3f;

final class EpicArmorModel extends HumanoidModelTransformer {
    private static final Bones HEAD = new Bones(9, 9, 0);
    private static final Bones CHEST = new Bones(8, 7, 18F / 16F);
    private static final Bones WAIST = new Bones(7, 7, 0);
    private static final Bones RIGHT_ARM = new Bones(11, 12, 19F / 16F);
    private static final Bones LEFT_ARM = new Bones(16, 17, 19F / 16F);
    private static final Bones RIGHT_LEG = new Bones(1, 2, 6F / 16F);
    private static final Bones LEFT_LEG = new Bones(4, 5, 6F / 16F);
    private static final Bones RIGHT_BOOT = new Bones(2, 2, 0);
    private static final Bones LEFT_BOOT = new Bones(5, 5, 0);

    @Override
    public SkinnedMesh transformArmorModel(HumanoidModel<?> model) {
        return null;
    }

    @Override
    public SkinnedMesh transformArmorModel(HumanoidModel<?> model, LivingEntity entity, ItemStack stack,
            EquipmentSlot slot) {
        var set = StarWarsArmor.set(stack);
        if (set == null) return null;

        // Bake the same geometry in its rest pose. Epic Fight's general cube splitter
        // assumes axis-aligned faces and can stretch rotated pauldrons and helmet panels.
        HumanoidModel<?> rest = StarWarsArmor.model(set, slot == EquipmentSlot.LEGS);
        var vertices = new ArrayList<SingleGroupVertexBuilder>();
        var parts = new LinkedHashMap<MeshPartDefinition, IntList>();
        var collector = new ArmorVertices(vertices, parts);
        switch (slot) {
            case HEAD -> collector.add("head", rest.head, HEAD);
            case CHEST -> {
                collector.add("body", rest.body, CHEST);
                collector.add("rightArm", rest.rightArm, RIGHT_ARM);
                collector.add("leftArm", rest.leftArm, LEFT_ARM);
            }
            case LEGS -> {
                collector.add("body", rest.body, WAIST);
                collector.add("rightLeg", rest.rightLeg, RIGHT_LEG);
                collector.add("leftLeg", rest.leftLeg, LEFT_LEG);
            }
            case FEET -> {
                collector.add("rightLeg", rest.rightLeg, RIGHT_BOOT);
                collector.add("leftLeg", rest.leftLeg, LEFT_BOOT);
            }
            default -> { return null; }
        }
        return SingleGroupVertexBuilder.loadVertexInformation(vertices, parts);
    }

    private record Bones(int upper, int lower, float jointY) {
        float weight(float y, boolean spansJoint) {
            if (upper == lower) return 1;
            if (!spansJoint) return y >= jointY ? 1 : 0;
            return Mth.clamp((y - jointY) * 8F + .5F, 0F, 1F);
        }
    }

    private record Part(String partName) implements MeshPartDefinition {
        @Override
        public Mesh.RenderProperties renderProperties() {
            return null;
        }

        @Override
        public Supplier<OpenMatrix4f> getModelPartAnimationProvider() {
            return null;
        }
    }

    private record Vertex(float x, float y, float z, float u, float v, float nx, float ny, float nz) {
        SingleGroupVertexBuilder skin(Bones bones, float weight) {
            int first = weight > 0 ? bones.upper() : bones.lower();
            boolean blended = weight > 0 && weight < 1;
            return new SingleGroupVertexBuilder()
                .setPosition(new Vec3f(x, y, z))
                .setNormal(new Vec3f(nx, ny, nz))
                .setTextureCoordinate(new Vec2f(u, v))
                .setEffectiveJointIDs(new Vec3f(first, blended ? bones.lower() : 0, 0))
                .setEffectiveJointWeights(new Vec3f(blended ? weight : 1, blended ? 1 - weight : 0, 0))
                .setEffectiveJointNumber(blended ? 2 : 1);
        }
    }

    private static final class ArmorVertices implements VertexConsumer {
        private final List<SingleGroupVertexBuilder> mesh;
        private final Map<MeshPartDefinition, IntList> parts;
        private final PartTransformer.IndexCounter index = new PartTransformer.IndexCounter();
        private final List<Vertex> cubeVertices = new ArrayList<>(24);

        ArmorVertices(List<SingleGroupVertexBuilder> mesh, Map<MeshPartDefinition, IntList> parts) {
            this.mesh = mesh;
            this.parts = parts;
        }

        void add(String name, ModelPart modelPart, Bones bones) {
            Part part = new Part(name);
            var pose = new PoseStack();
            pose.mulPose(Axis.YP.rotationDegrees(180));
            pose.mulPose(Axis.XP.rotationDegrees(180));
            pose.translate(0F, -1.5F, 0F);
            modelPart.visit(pose, (transform, path, cubeIndex, cube) -> {
                cubeVertices.clear();
                cube.compile(transform, this, 0, 0, 0xffffffff);
                float top = Float.NEGATIVE_INFINITY;
                float bottom = Float.POSITIVE_INFINITY;
                for (Vertex vertex : cubeVertices) {
                    top = Math.max(top, vertex.y());
                    bottom = Math.min(bottom, vertex.y());
                }
                // Keep small plates rigid. The undersuit and long cloth panels can bend.
                boolean rigid = cube instanceof ArmorMesh surface && surface.isRigid();
                boolean flexible = !rigid && top - bottom > 5F / 16F;
                float center = (top + bottom) / 2F;
                for (int corner = 0; corner < cubeVertices.size(); corner++) {
                    Vertex vertex = cubeVertices.get(corner);
                    float weight = rigid ? 1F : bones.weight(flexible ? vertex.y() : center, flexible);
                    mesh.add(vertex.skin(bones, weight));
                    if (corner % 4 == 3) {
                        PartTransformer.triangluatePolygon(parts, part, index);
                    }
                }
            });
        }

        @Override
        public void addVertex(float x, float y, float z, int color, float u, float v,
                int overlay, int light, float nx, float ny, float nz) {
            cubeVertices.add(new Vertex(x, y, z, u, v, nx, ny, nz));
        }

        @Override public VertexConsumer addVertex(float x, float y, float z) { throw new UnsupportedOperationException(); }
        @Override public VertexConsumer setColor(int r, int g, int b, int a) { throw new UnsupportedOperationException(); }
        @Override public VertexConsumer setUv(float u, float v) { throw new UnsupportedOperationException(); }
        @Override public VertexConsumer setUv1(int u, int v) { throw new UnsupportedOperationException(); }
        @Override public VertexConsumer setUv2(int u, int v) { throw new UnsupportedOperationException(); }
        @Override public VertexConsumer setNormal(float x, float y, float z) { throw new UnsupportedOperationException(); }
    }
}
