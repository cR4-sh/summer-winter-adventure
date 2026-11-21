package dev.swa.space.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.starwarsmod.client.gui.SagaScreen", remap = false)
abstract class StarWCraftSagaScreenMixin {
    @Shadow public abstract void onClose();

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void swa$skipLegacySagaSelector(CallbackInfo callback) {
        onClose();
        callback.cancel();
    }
}
