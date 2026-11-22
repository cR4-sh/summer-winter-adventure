package dev.swa.gear.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.starwarsmod.item.LightsaberItem;
import dev.swa.gear.SaberTogglePayload;
import dev.swa.gear.SaberViewmodel;
import net.minecraft.Util;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.renderer.FirstPersonRenderer;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.skill.SkillSlots;

@Mixin(value = RenderEngine.class, remap = false)
abstract class EpicSaberViewMixin {
    @Unique private long preview$lastView;
    @Unique private float preview$readyWeight;
    @Unique private float preview$guardWeight;

    @Redirect(method = "epicfight$renderHand", at = @At(value = "INVOKE",
            target = "Lyesman/epicfight/client/renderer/FirstPersonRenderer;render(Lnet/minecraft/client/player/LocalPlayer;Lyesman/epicfight/client/world/capabilites/entitypatch/player/LocalPlayerPatch;Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/PoseStack;IF)V"))
    private void preview$frameSaber(FirstPersonRenderer renderer, LocalPlayer player, LocalPlayerPatch patch,
            LivingEntityRenderer<LocalPlayer, PlayerModel<LocalPlayer>> vanillaRenderer,
            MultiBufferSource buffers, PoseStack pose, int light, float partialTick) {
        if (SaberViewmodel.render(player, patch, buffers, pose, light, partialTick)) {
            preview$lastView = 0L;
            return;
        }
        ItemStack saber = SaberTogglePayload.heldBlade(player);
        if (saber.isEmpty() || player.isVisuallySwimming() || player.isFallFlying()) {
            preview$lastView = 0L;
            renderer.render(player, patch, vanillaRenderer, buffers, pose, light, partialTick);
            return;
        }

        boolean guarding = patch.getSkill(SkillSlots.GUARD).isActivated();
        float guardTarget = guarding ? 1F : 0F;
        float readyTarget = !guarding && !patch.getEntityState().inaction() ? 1F : 0F;
        long now = Util.getMillis();
        long elapsed = now - preview$lastView;
        preview$lastView = now;
        if (elapsed > 200) {
            preview$readyWeight = readyTarget;
            preview$guardWeight = guardTarget;
        } else {
            float blend = 1F - (float) Math.exp(-elapsed / 82F);
            preview$readyWeight += (readyTarget - preview$readyWeight) * blend;
            preview$guardWeight += (guardTarget - preview$guardWeight) * blend;
        }

        float ready = preview$readyWeight;
        float guard = preview$guardWeight;
        float action = 1F - ready - guard;
        float side = saber == player.getMainHandItem() ? 1F : -1F;
        var style = LightsaberItem.styleOf(saber);
        boolean staff = style == LightsaberItem.Style.DOUBLE || style == LightsaberItem.Style.INQUISITOR;
        float readyX = staff ? -.04F : .20F;
        float readyY = staff ? -.44F : -.51F;
        float readyDepth = staff ? -.97F : -.73F;
        float readyYaw = staff ? 18F : 12F;
        float readyRoll = staff ? 44F : 36F;

        // Frame the animated arms and saber together so the weapon stays attached to the grip.
        pose.pushPose();
        try {
            // Epic Fight's main-hand joint is always the right hand in this renderer.
            if (player.getMainArm() == HumanoidArm.LEFT) pose.scale(-1F, 1F, 1F);
            pose.translate(side * (readyX * ready + .22F * guard + .10F * action),
                    readyY * ready - .42F * guard - .11F * action,
                    readyDepth * ready - .80F * guard - .38F * action);
            pose.mulPose(Axis.ZP.rotationDegrees(side * (readyRoll * ready + 9F * guard + 6F * action)));
            pose.mulPose(Axis.YP.rotationDegrees(side * (readyYaw * ready + 3F * guard + 4F * action)));
            pose.mulPose(Axis.XP.rotationDegrees(54F * ready + 13F * guard + 12F * action));
            renderer.render(player, patch, vanillaRenderer, buffers, pose, light, partialTick);
        } finally {
            pose.popPose();
        }
    }
}
