package dev.swa.gear;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.starwarsmod.client.render.SaberRenderer;
import com.starwarsmod.item.LightsaberItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

import static dev.swa.gear.GearViewGeometry.CHARCOAL;
import static dev.swa.gear.GearViewGeometry.GLOVE;
import static dev.swa.gear.GearViewGeometry.KNUCKLE;
import static dev.swa.gear.GearViewGeometry.arm;
import static dev.swa.gear.GearViewGeometry.box;

@EventBusSubscriber(modid = GearPreview.ID, value = Dist.CLIENT)
public final class SaberViewmodel {
    private static final ResourceLocation WHITE = ResourceLocation.fromNamespaceAndPath(GearPreview.ID, "textures/viewmodel_white.png");
    private static final Mount READY = new Mount(.42F, -.365F, -.73F, -39F, 16F, -10F);
    private static final Mount STAFF_READY = new Mount(.27F, -.40F, -.76F, -41F, 48F, -6F);
    private static final Mount SECOND_READY = new Mount(.38F, -.44F, -.78F, -52F, 14F, 13F);

    private SaberViewmodel() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void renderOutsideEpicFight(RenderHandEvent event) {
        var client = Minecraft.getInstance();
        var player = client.player;
        if (player == null || !client.options.getCameraType().isFirstPerson() || !supports(player)) return;
        var patch = EpicFightCapabilities.getLocalPlayerPatch(player);
        if (patch == null || (patch.isEpicFightMode() && ClientConfig.enableAnimatedFirstPersonModel)) return;

        if (event.getHand() == InteractionHand.OFF_HAND) {
            if (canRender(player.getMainHandItem()) && canRender(player.getOffhandItem())) event.setCanceled(true);
            return;
        }
        if (render(player, patch, event.getMultiBufferSource(), event.getPoseStack(),
                event.getPackedLight(), event.getPartialTick(), event.getSwingProgress())) event.setCanceled(true);
    }

    public static boolean render(LocalPlayer player, LocalPlayerPatch patch, MultiBufferSource buffers,
            PoseStack pose, int light, float partialTick) {
        return render(player, patch, buffers, pose, light, partialTick, 0F);
    }

    private static boolean supports(LocalPlayer player) {
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        return (main.getItem() instanceof LightsaberItem || main.isEmpty())
                && (off.getItem() instanceof LightsaberItem || off.isEmpty())
                && !(main.isEmpty() && off.isEmpty()) && !player.isPassenger()
                && !player.isFallFlying() && !player.isVisuallySwimming();
    }

    private static boolean render(LocalPlayer player, LocalPlayerPatch patch, MultiBufferSource buffers,
            PoseStack pose, int light, float partialTick, float vanillaSwing) {
        if (!supports(player)) return false;
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        SaberRenderer mainRenderer = renderer(main);
        SaberRenderer offRenderer = renderer(off);
        if ((!main.isEmpty() && mainRenderer == null) || (!off.isEmpty() && offRenderer == null)) return false;

        float side = player.getMainArm() == HumanoidArm.RIGHT ? 1F : -1F;
        var motion = patch.isEpicFightMode() ? jointMotion(patch, partialTick) : null;
        if (mainRenderer != null) {
            drawSaber(main, mainRenderer, buffers, pose, light, side, motion, patch, InteractionHand.MAIN_HAND,
                    false, offRenderer == null, player.swingingArm == InteractionHand.MAIN_HAND ? vanillaSwing : 0F);
        }
        if (offRenderer != null) {
            drawSaber(off, offRenderer, buffers, pose, light, -side, motion, patch, InteractionHand.OFF_HAND,
                    mainRenderer != null, mainRenderer == null, player.swingingArm == InteractionHand.OFF_HAND ? vanillaSwing : 0F);
        }
        return true;
    }

    private static boolean canRender(ItemStack stack) {
        return stack.isEmpty() || renderer(stack) != null;
    }

    private static SaberRenderer renderer(ItemStack stack) {
        if (stack.isEmpty()) return null;
        var renderer = IClientItemExtensions.of(stack).getCustomRenderer();
        return renderer instanceof SaberRenderer saberRenderer ? saberRenderer : null;
    }

    private static JointMotion jointMotion(LocalPlayerPatch patch, float partialTick) {
        var animator = patch.getAnimator();
        var idle = animator.getLivingAnimation(LivingMotions.IDLE, null);
        if (idle == null || !idle.isPresent()) return null;
        var armature = patch.getArmature();
        return new JointMotion(
                armature.getPoseAsTransformMatrix(idle.get().getPoseByTime(patch, 0F, 0F), false),
                armature.getPoseAsTransformMatrix(animator.getPose(partialTick), false));
    }

    private static void drawSaber(ItemStack stack, SaberRenderer renderer, MultiBufferSource buffers,
            PoseStack pose, int light, float side, JointMotion motion, LocalPlayerPatch patch, InteractionHand hand,
            boolean secondary, boolean freeHand, float vanillaSwing) {
        var style = LightsaberItem.styleOf(stack);
        boolean staff = style == LightsaberItem.Style.DOUBLE || style == LightsaberItem.Style.INQUISITOR;
        Mount ready = staff ? STAFF_READY : secondary ? SECOND_READY : READY;
        var rotation = new Quaternionf().rotationZ(ready.roll * Mth.DEG_TO_RAD)
                .rotateX(ready.pitch * Mth.DEG_TO_RAD).rotateY(ready.twist * Mth.DEG_TO_RAD);
        var position = new Vector3f(ready.x, ready.y, ready.z);
        if (motion != null) {
            var item = RenderEngine.getInstance().getItemRenderer(stack);
            if (item != null) {
                // Read the same item socket that Epic Fight uses for the third-person renderer.
                var rest = OpenMatrix4f.exportToMojangMatrix(item.getCorrectionMatrix(patch, hand, motion.rest));
                var moving = OpenMatrix4f.exportToMojangMatrix(item.getCorrectionMatrix(patch, hand, motion.animated));
                var movement = moving.getTranslation(new Vector3f()).sub(rest.getTranslation(new Vector3f()));
                var delta = moving.getNormalizedRotation(new Quaternionf())
                        .mul(rest.getNormalizedRotation(new Quaternionf()).conjugate()).normalize();
                if (hand == InteractionHand.OFF_HAND) {
                    movement.x = -movement.x;
                    delta.set(delta.x, -delta.y, -delta.z, delta.w);
                }
                // Preserve the wrist rotation and timing. Adapt the reach to an eye-level camera.
                position.add(movement.mul(.38F, .28F, .32F));
                rotation.premul(delta);
            }
        } else if (vanillaSwing > 0F) {
            float swing = Mth.sin(vanillaSwing * Mth.PI);
            position.add(-.20F * swing, -.12F * swing, -.10F * swing);
            rotation.rotateZ(.65F * swing);
        }

        keepBladeClearOfCamera(rotation, staff);
        float gripY = staff ? .07F : -.015F;
        var mainContact = gripContact(rotation, gripY, false, motion, patch, hand);
        var supportContact = staff && freeHand ? gripContact(rotation, -.14F, true, motion, patch, hand) : null;
        position = SaberArmRig.fitMount(position, mainContact, supportContact);

        VertexConsumer gloves = buffers.getBuffer(RenderType.entityCutoutNoCull(WHITE));
        pose.pushPose();
        try {
            if (side < 0F) pose.scale(-1F, 1F, 1F);
            drawSleeve(pose, gloves, light, SaberArmRig.solve(position, mainContact));
            if (supportContact != null) drawSleeve(pose, gloves, light, SaberArmRig.solve(position, supportContact));

            pose.translate(position.x, position.y, position.z);
            pose.mulPose(rotation);
            drawGrip(pose, gloves, light, gripY, false);
            if (supportContact != null) drawGrip(pose, gloves, light, -.14F, true);
            float saberLength = staff ? 1.20F : secondary ? 1.11F : 1.36F;
            pose.scale(1.12F, saberLength, 1.12F);
            // The native item renderer centers its model in a one-block display box.
            pose.translate(-.5F, -.5F, -.5F);
            renderer.renderByItem(stack, side > 0 ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                    : ItemDisplayContext.FIRST_PERSON_LEFT_HAND, pose, buffers, light, OverlayTexture.NO_OVERLAY);
        } finally {
            pose.popPose();
        }
    }

    private static void keepBladeClearOfCamera(Quaternionf rotation, boolean staff) {
        var blade = rotation.transform(0F, 1F, 0F, new Vector3f());
        // The blade may leave the sides of the frame, but the actor's swing cannot pass through the lens.
        float depth = Mth.clamp(blade.z, staff ? -.25F : -1F, staff ? .25F : .10F);
        if (depth == blade.z) return;
        var screen = new Vector3f(blade.x, blade.y, 0F);
        if (screen.lengthSquared() < .012F) screen.add(-.11F, .055F, 0F);
        screen.normalize((float) Math.sqrt(1F - depth * depth)).z = depth;
        rotation.premul(new Quaternionf().rotationTo(blade, screen)).normalize();
    }

    private static SaberArmRig.Contact gripContact(Quaternionf rotation, float gripY, boolean support,
            JointMotion motion, LocalPlayerPatch patch, InteractionHand gripHand) {
        float side = support ? -1F : 1F;
        var shoulder = new Vector3f(.345F * side, -.42F, -.195F);
        var offset = rotation.transform(.070F * side, gripY - .039F, .057F, new Vector3f());
        var elbowPole = new Vector3f(.30F * side, -.84F, .20F);
        if (motion != null) {
            boolean right = (gripHand == InteractionHand.MAIN_HAND) != support;
            var elbow = patch.getArmature().searchJointByName(right ? "Arm_R" : "Arm_L");
            if (elbow != null) {
                var idle = OpenMatrix4f.toTranslationVector(motion.rest[elbow.getId()]);
                var animated = OpenMatrix4f.toTranslationVector(motion.animated[elbow.getId()]);
                float mirror = gripHand == InteractionHand.MAIN_HAND ? 1F : -1F;
                elbowPole.add(.35F * mirror * (animated.x - idle.x), .35F * (animated.y - idle.y),
                        .35F * (animated.z - idle.z));
            }
        }
        return new SaberArmRig.Contact(shoulder, offset, elbowPole);
    }

    private static void drawSleeve(PoseStack pose, VertexConsumer v, int light, SaberArmRig.Arm limb) {
        arm(pose, v, light, limb.elbow(), limb.wrist(), .79F);
        GearViewGeometry.upperSleeve(pose, v, light, limb.shoulder(), limb.elbow());
        if (Boolean.getBoolean("swa.preview.traceSaberReach")) {
            System.out.printf(java.util.Locale.ROOT, "QA_REACH shoulder=%.3f,%.3f,%.3f upper=%.5f lower=%.5f%n",
                    limb.shoulder().x, limb.shoulder().y, limb.shoulder().z,
                    limb.shoulder().distance(limb.elbow()), limb.elbow().distance(limb.wrist()));
        }
    }

    private static void drawGrip(PoseStack pose, VertexConsumer v, int light, float y, boolean support) {
        pose.pushPose();
        pose.translate(0F, y, 0F);
        if (support) pose.mulPose(Axis.YP.rotationDegrees(172F));
        box(pose, v, light, GLOVE, .036F, -.070F, -.024F, .103F, .054F, .091F);
        box(pose, v, light, CHARCOAL, .051F, -.072F, .054F, .106F, .004F, .105F);
        box(pose, v, light, KNUCKLE, .064F, -.013F, -.045F, .107F, .049F, .050F);
        for (int finger = 0; finger < 4; finger++) {
            float lower = -.061F + finger * .027F;
            box(pose, v, light, GLOVE, -.030F, lower, -.065F, .077F, lower + .022F, -.038F);
        }
        box(pose, v, light, KNUCKLE, -.040F, .045F, .035F, .050F, .072F, .075F);
        pose.popPose();
    }

    private record Mount(float x, float y, float z, float pitch, float roll, float twist) {}

    private record JointMotion(OpenMatrix4f[] rest, OpenMatrix4f[] animated) {}
}
