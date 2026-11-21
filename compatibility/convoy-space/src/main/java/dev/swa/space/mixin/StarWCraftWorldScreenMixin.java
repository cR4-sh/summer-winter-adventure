package dev.swa.space.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.starwarsmod.client.gui.ThemedScreens", remap = false)
abstract class StarWCraftWorldScreenMixin {
    @Inject(method = {"onOpen", "onLogin"}, at = @At("HEAD"), cancellable = true, require = 2)
    private static void swa$useVanillaWorldCreation(CallbackInfo callback) {
        // Retain the original Title and Create World screens, and skip the pending Journey packet.
        callback.cancel();
    }
}
