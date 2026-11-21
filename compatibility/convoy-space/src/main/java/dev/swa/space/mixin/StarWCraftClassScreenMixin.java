package dev.swa.space.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.starwarsmod.client.gui.ClassSelectScreen", remap = false)
abstract class StarWCraftClassScreenMixin {
    @Inject(method = "open", at = @At("HEAD"), cancellable = true)
    private static void swa$skipClassPrompt(CallbackInfo callback) {
        callback.cancel();
    }
}
