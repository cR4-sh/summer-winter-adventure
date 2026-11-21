package dev.swa.space.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.starwarsmod.client.StarWarsKeybinds", remap = false)
abstract class StarWCraftOnboardingKeysMixin {
    @ModifyConstant(method = "<clinit>",
        constant = {@Constant(intValue = 74), @Constant(intValue = 75), @Constant(intValue = 77)}, require = 3)
    private static int swa$unbindLegacySelectors(int original) {
        // GLFW defaults for the old quest book (J), squad role (K), and saga (M).
        return -1;
    }

    @Redirect(method = "onClientTick", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 7))
    private static boolean swa$ignoreSavedSagaBinding(@Coerce Object binding) {
        // Existing options may still have M assigned to the Saga screen.
        return false;
    }
}
