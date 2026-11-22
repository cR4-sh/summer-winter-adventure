package dev.swa.gear.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.starwarsmod.client.BlasterClientExtensions;
import com.starwarsmod.item.BlasterItem;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlasterClientExtensions.class)
abstract class BlasterMotionMixin {
    @Inject(method = "applyForgeHandTransform", at = @At("HEAD"), cancellable = true)
    private void preview$frameAndRecoil(PoseStack pose, LocalPlayer player, HumanoidArm arm, ItemStack stack,
            float partialTick, float equip, float swing, CallbackInfoReturnable<Boolean> callback) {
        if (!(stack.getItem() instanceof BlasterItem blaster)) return;
        int side = arm == HumanoidArm.RIGHT ? 1 : -1;
        float weight = blaster.getDamage() >= 12F ? 1.2F : 1F;
        float time = (player.level().getGameTime() - BlasterItem.lastShot + partialTick) / 20F;
        float kick = 0F;
        if (time >= 0F && time < 0.28F) {
            float rise = Math.min(1F, time / 0.033F);
            kick = rise * (float) Math.exp(-15F * Math.max(0F, time - 0.033F));
        }
        float drop = equip * equip;
        pose.translate(side * (0.31F + 0.08F * drop), -0.35F - 0.63F * drop,
                -0.92F + 0.13F * weight * kick);
        pose.mulPose(Axis.YP.rotationDegrees(11F * side));
        pose.mulPose(Axis.XP.rotationDegrees(-4F + 9F * weight * kick + 35F * drop));
        pose.mulPose(Axis.ZP.rotationDegrees(-side * (4F + 2.5F * kick)));
        callback.setReturnValue(true);
    }
}
