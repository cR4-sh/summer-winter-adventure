package dev.swa.gear.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.starwarsmod.client.render.entity.BlasterBoltRenderer;
import com.starwarsmod.entity.BlasterBolt;
import dev.swa.gear.BlasterMuzzle;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlasterBoltRenderer.class, remap = false)
abstract class BlasterBoltViewMixin {
    @Inject(method = "render(Lcom/starwarsmod/entity/BlasterBolt;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"), cancellable = true)
    private void preview$showTheLocalMuzzleBeforeTheWorldBolt(BlasterBolt bolt, float yaw, float partialTick,
            PoseStack pose, MultiBufferSource buffer, int light, CallbackInfo callback) {
        if (BlasterMuzzle.hideWorldBolt(bolt)) callback.cancel();
    }
}
