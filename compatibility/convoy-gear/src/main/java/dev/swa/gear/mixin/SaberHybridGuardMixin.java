package dev.swa.gear.mixin;

import dev.swa.gear.SaberTogglePayload;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.client.events.engine.ControlEngine;

@Mixin(value = ControlEngine.class, remap = false)
abstract class SaberHybridGuardMixin {
    @Inject(method = "maybeGuard", at = @At("HEAD"), cancellable = true)
    private void preview$allowCrouchedHybridFire(CallbackInfo callback) {
        var player = Minecraft.getInstance().player;
        if (player != null && SaberTogglePayload.usesHybridFire(player, player.getMainHandItem())) {
            callback.cancel();
        }
    }
}
